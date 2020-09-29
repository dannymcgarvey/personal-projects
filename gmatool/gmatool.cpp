#include "gmatool.h"


/*

	gmatool provides two main functionalities:

	1- Extract goals, switches, or other specific models from a gma / tpl file
	2- Merge two gma / tpl files into one

	gma file format:
	https://craftedcart.github.io/SMBLevelWorkshop/documentation/index.html?page=gmaFormat

	tpl file format:
	https://craftedcart.github.io/SMBLevelWorkshop/documentation/index.html?page=tplFormat12

	original gmatool by Mechalico:
	https://github.com/Mechalico/gmatool

*/

/*

	Main body - read in arguments

*/
int main(int argc, char* argv[]) {
	int successval = 1;

	// Check Number of Arguments
	if (argc < 3 || argc > 4) {
		successval = helpText();
	} else {

		string operationtype(argv[1]);

		// Choose model to extract
		if (operationtype == "-le" && argc == 3) {
			
			string filename(argv[2]);
			successval = modelExtract(filename, LIST_MODELS, "");

		// Extract Goals
		} else if (operationtype == "-ge" && argc == 3) {

			string filename(argv[2]);
			successval = modelExtract(filename, GOAL_EXTRACT, "");

		// Extract Switches
		} else if (operationtype == "-se" && argc == 3) {

			string filename(argv[2]);
			successval = modelExtract(filename, SWITCH_EXTRACT, "");

		// Extract Specific Model
		} else if (operationtype == "-me" && argc == 4) {

			string filename(argv[2]);
			string specificmodelname(argv[3]);
			successval = modelExtract(filename, SPECIFIC_MODEL, specificmodelname);

		// Merge Models
		} else if (operationtype == "-m" && argc == 4) {

			string filename1(argv[2]);
			string filename2(argv[3]);
			successval = gmatplMerge(filename1, filename2);

		// Invalid Arguments
		} else {
			successval = helpText();
		}
	}

	// Finished Successfully
	if (successval == 0) {
		cout  << "Done!"<< endl;
	}

	return successval;
}

/*

	Part 1:
	Model Extraction

*/
void modelWriteToFiles(string filename, ifstream& oldgma, ifstream& oldtpl, int modelamount, int modelnumber, uint32_t modelnamelength, string modelname, string suffix) {
	/*
	These files will create standalone TPL and GMA files, designed to be easily integrated into the main file.
	*/
	//First delete files
	remove((filename + "_" + suffix + ".tpl").c_str());
	remove((filename + "_" + suffix + ".gma").c_str());
	//Write the GMA first, and we can get info for the TPL later
	ofstream newgma(filename + "_" + suffix + ".gma", ios::binary | ios::app);

	// Adjust model number for empty entries
	modelnumber = modelNumberWithEmpties(oldgma, modelnumber);

	// Writing GMA Header

	//Write the initial bytes (Number of Models)
	saveIntToFileEnd(newgma, 1); //1 model


	//Calculate remaining length
	/*
	The GMA header is always a multiple of 0x20 in length. (modelnamelength + 0x10) % 0x20 gives the remaining padding
	*/
	uint32_t gmapadding = (-(modelnamelength+0x10)) % 0x20;
	uint32_t newheaderlength = modelnamelength+0x10+gmapadding;

	// Write in the new header length
	saveIntToFileEnd(newgma, newheaderlength);

	//Now for the zero bytes. These point to the extra offsets, of which there isn't one
	padZeroes(newgma, 8);


	//Now write in the modelname
	newgma << modelname;

	//pad to a multiple of 0x20
	padZeroes(newgma, gmapadding+1); //extra 1 due to missing 00 byte from modelname

	
	/*
		Now the header is written, time for the main body
	*/
	
	uint32_t oldheaderlength = fileIntPluck(oldgma, 0x04);
	uint32_t oldstartextraoffset = (fileIntPluck(oldgma, 0x08+(0x08*modelnumber)));
	uint32_t oldstartpoint = oldheaderlength + oldstartextraoffset;
	uint32_t oldendpoint = 0;

	// Calculate the endpoint in the old gma for the model

	// If it's the last model
	if (modelamount == modelnumber + 1) {
		oldendpoint = getFileLength(oldgma);
	} else {

		// Need to find next non-empty model entry
		uint32_t nextNonEmptyModelNumber = modelnumber + 1;
		uint32_t oldendextraoffset = fileIntPluck(oldgma, 0x08 + 0x08 * nextNonEmptyModelNumber );

		// Adjust offset for empty models
		while (nextNonEmptyModelNumber < modelamount && oldendextraoffset == 0xffffffff) {
			nextNonEmptyModelNumber++;
			oldendextraoffset = fileIntPluck(oldgma, 0x08 + 0x08 * nextNonEmptyModelNumber );
		}
		
		// Case for when all subsequent models were empty
		if (nextNonEmptyModelNumber == modelamount) {
			oldendpoint = getFileLength(oldgma);
		} else {
			oldendpoint = oldheaderlength + oldendextraoffset;
		}
	}

	// Write Model Header
	copyBytes(oldgma, newgma, oldstartpoint, 0x40);

	//texture read and write, as well as copy
	uint16_t texturearray[0xff]; //no goals will be this long but it should be a generous measurement
	memset(texturearray, 0xff, sizeof(texturearray)); //255 initiation
	uint16_t texturearraypointer = 0;

	uint16_t materialamount = fileShortPluck(oldgma, oldstartpoint+0x18);

	uint32_t oldmodelheaderlength = 0x40;


	//Loop for each material
	for (uint32_t materialnumber = 0; materialnumber < materialamount; materialnumber++) {

		// Write material flags
		copyBytes(oldgma, newgma, oldstartpoint+0x40+0x20*materialnumber, 0x04);

		uint16_t materialvalue = fileShortPluck(oldgma, oldstartpoint+0x44+0x20*materialnumber);
		uint16_t materialvaluepointer = *find(begin(texturearray), end(texturearray), materialvalue);
		uint16_t texturearrayendpointer = *end(texturearray);
		
		// Write in texture index for the material and save for later
		if (materialvaluepointer == texturearrayendpointer) {
			//Not in array - add to array
			texturearray[texturearraypointer] = materialvalue;
			saveShortToFileEnd(newgma, texturearraypointer);
			texturearraypointer++;
		} else {
			//In array - write value to array
			uint16_t texturevalueindex = distance(texturearray, find(begin(texturearray), end(texturearray), materialvalue));
			saveShortToFileEnd(newgma, texturevalueindex);
		}
		

		// Copy data for material
		copyBytes(oldgma, newgma, oldstartpoint+0x46+0x20*materialnumber, 0x1A);
		oldmodelheaderlength += 0x20;
	}
	
	// Done with material list

	// Find start point for model data in old gma
	uint32_t oldmodeldatastart = oldstartpoint + oldmodelheaderlength;

	// Length of model data
	uint32_t oldmodeldatalength = oldendpoint - oldmodeldatastart;

	// Copy the rest of the model data
	copyBytes(oldgma, newgma, oldmodeldatastart, oldmodeldatalength);
	newgma.close();

	/* 

		Done with gma, start writing tpl

	*/
	
	ofstream newtpl(filename + "_" + suffix + ".tpl", ios::binary | ios::app);

	// Get number of textures from earlier
	uint32_t textureamount = texturearraypointer;
	saveIntToFileEnd(newtpl, textureamount);

	// Write Header Entries
	//also creating rolling offset
	uint32_t oldtexturestarts[textureamount];
	uint32_t oldtextureends[textureamount];
	uint32_t rollingoffset = 0;

	// Loop for each texture being copied over
	for (int texturenumber = 0; texturenumber < textureamount; texturenumber++) {

		// Texture position in the original tpl
		uint16_t oldtexturevalue = texturearray[texturenumber];
		uint32_t oldtextureheaderpos =  oldtexturevalue*0x10+0x04;

		
		// Save start point of texture data
		uint32_t nextStart = fileIntPluck(oldtpl, oldtextureheaderpos+0x04);
		oldtexturestarts[texturenumber] = nextStart;

		// Save endpoint of texture data
		if (oldtexturevalue < fileIntPluck(oldtpl, 0x0) - 1) {

			//if it's less than this then the texture ends at the next nonempty value

			// textureskips will be > 1 if there are empty textures
			int textureskips = nextNonEmptyTextureOffset(oldtpl, oldtextureheaderpos);

			if (texturenumber + textureskips < textureamount) {
				oldtextureends[texturenumber] = fileIntPluck(oldtpl, oldtextureheaderpos + (textureskips * 0x10) + 0x04);
			} else {
				// Account for case when all remaining textures are empty
				oldtextureends[texturenumber] = getFileLength(oldtpl);
			}

		} else {
			//else it ends at the end of the file
			oldtextureends[texturenumber] = getFileLength(oldtpl);
		}

		
		// copy initial bytes for texture format
		copyBytes(oldtpl, newtpl, oldtextureheaderpos, 0x4);

		// write texture data offset
		if (texturenumber == 0) {

			//the first offset will always be the length of the header
			rollingoffset = (textureamount + 1) * 0x10;

			//new header length will be aligned to 0x20 bytes, add 0x10 if it isn't
			if (rollingoffset % 0x20 != 0x0) {
				rollingoffset += 0x10;
			}
			
		} else {
			//the offset is based on the length of the previous one
			rollingoffset += (oldtextureends[texturenumber-1] - oldtexturestarts[texturenumber-1]);
		}

		// Write texture data offset
		saveIntToFileEnd(newtpl, rollingoffset);

		// Copy the rest of the data in the texture header
		copyBytes(oldtpl, newtpl,  oldtextureheaderpos+0x08, 0x08);
	}

	//padding with the 00010203... pattern
	int tplpaddingamount = (- 0x04 + (0x10*textureamount)) % 0x20;
	for (uint8_t tplpaddingpointer = 0x0; tplpaddingpointer < tplpaddingamount; tplpaddingpointer++) {
		newtpl << tplpaddingpointer;
	}

	// Texture header finished
	
	// Copying the Texture Data
	for (int texturenumber = 0; texturenumber < textureamount; texturenumber++) {
		copyBytes(oldtpl, newtpl,  oldtexturestarts[texturenumber], oldtextureends[texturenumber]-oldtexturestarts[texturenumber]);
	}

	// Done with tpl
	newtpl.close();

	cout << "saved to " << filename << "_" << suffix << endl;
}

int modelExtract(string filename, int type, string specificmodel) {

	int result = 0;
	ifstream gma;
	ifstream tpl;

	//open files and check that they're good
	gma.open(filename + ".gma", ios::binary);
	if (gma.good() == false) {
		cout << "No GMA found!" << endl;
		return -1;
	}
	tpl.open(filename + ".tpl", ios::binary);
	if (tpl.good() == false) {
		cout << "No TPL found!" << endl;
		return -1;
	}

	//If the files are good we can read the gma for the files
	uint32_t modelamount = fileIntPluck(gma, 0);
	uint32_t nonemptymodelamount = modelAmountWithoutEmpties(gma, modelamount);

	uint32_t modellistoffset = modelamount * 0x8 + 0x8; //Start of model list - 0x8 initial bytes plus 0x8 for each model
	uint32_t modellistpointer = modellistoffset;

	uint32_t modelnamelength = 0;
	string modelname = "";

	if (type == GOAL_EXTRACT) {
		//Goal extraction block

		bool hasGoal = false;

		for (int modelnumber = 0; modelnumber < nonemptymodelamount; modelnumber++) {

			// Read model name from model list
			modelnamelength = getModelNameLength(gma, modellistpointer);
			string modelname = readNameFromGma(gma, modellistpointer, modelnamelength);

			if (modelname.find("GOAL") != string::npos) {
				// Found a goal model
				hasGoal = true;

				// Determine color with last char of the name
				char goalColor = modelname[modelnamelength - 2];

				if (goalColor == 'B') {
					// Found the blue goal
					cout << modelname << " (Blue goal) ";
					modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, "GOAL_B");

				} else if (goalColor == 'G') {
					// Found the green goal
					cout << modelname << " (Green goal) ";
					modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, "GOAL_G");

				} else if (goalColor == 'R') {
					// Found the red goal
					cout << modelname << " (Red goal) ";
					modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, "GOAL_R");

				} else {
					// Found some other goal model
					cout << modelname << " ";
					modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, modelname);

				}
			}

			// Advance to next model name
			modellistpointer += modelnamelength;
		}
		if (hasGoal == false) {
			cout << "No goal found!";
		}

	} else if (type == SWITCH_EXTRACT) {
		//Switch extraction block
		bool hasSwitches = false;

		for (int modelnumber = 0; modelnumber < nonemptymodelamount; modelnumber++) {

			// Read model name from model list
			modelnamelength = getModelNameLength(gma, modellistpointer);
			string modelname = readNameFromGma(gma, modellistpointer, modelnamelength);

			if (modelname.substr(0,7) == "BUTTON_") {
				// Found a switch model
				cout << modelname << " ";
				modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, modelname);
				hasSwitches = true;
			}

			// advance to next model
			modellistpointer += modelnamelength;
		}
		if (hasSwitches == false) {
			cout << "No switches found!";
			result = 1;
		}

	} else if (type == SPECIFIC_MODEL) {
		//Specific model extraction block
		bool hasSpecificModel = false;
		for (int modelnumber = 0; modelnumber < nonemptymodelamount; modelnumber++) {

			// Read model name from model list
			modelnamelength = getModelNameLength(gma, modellistpointer);
			string modelname = readNameFromGma(gma, modellistpointer, modelnamelength);

			if (modelname == specificmodel) {
				// Found the model
				cout << modelname << " ";
				modelWriteToFiles(filename, gma, tpl, modelamount, modelnumber, modelnamelength, modelname, modelname);
				hasSpecificModel = true;
			}

			// advance to next model
			modellistpointer += (modelnamelength);
		}
		if (hasSpecificModel == false) {
			cout << "The model " << specificmodel << " wasn't found!";
			result = 1;
		}
	} else if (type == LIST_MODELS) {
		//Specify which model to extract.
		cout << filename << " models:" << endl;

		for (int modelnumber = 0; modelnumber < nonemptymodelamount; modelnumber++) {

			// Read model name from model list
			modelnamelength = getModelNameLength(gma, modellistpointer);
			string modelname = readNameFromGma(gma, modellistpointer, modelnamelength);

			//Print out model name
			cout << modelname << endl;

			// Advance model list pointer
			modellistpointer += modelnamelength;
		}

		// User inputs model name
		string chosenmodel;
		cout << endl << "Choose a model to extract: >";
		cin >> chosenmodel;

		// Restart with the chosen model
		gma.close();
		tpl.close();
		return modelExtract(filename, SPECIFIC_MODEL, chosenmodel);

	}
	gma.close();
	tpl.close();
	return result;
}


/*

	Part 2:
	Model Merge

*/
int gmatplMerge(string filename1, string filename2) {

	// Check if the files are good
	ifstream gma1;
	ifstream gma2;
	ifstream tpl1;
	ifstream tpl2;
	gma1.open(filename1 + ".gma", ios::binary);
	if (gma1.good() == false) {
		cout << "First GMA not found! (" << filename1 << ".gma)" << endl;
		return -1;
	}
	gma2.open(filename2 + ".gma", ios::binary);
	if (gma2.good() == false) {
		cout << "Second GMA not found! (" << filename2 << ".gma)" << endl;
		return -1;
	}
	tpl1.open(filename1 + ".tpl", ios::binary);
	if (tpl1.good() == false) {
		cout << "First TPL not found! (" << filename1 << ".tpl)" << endl;
		return -1;
	}
	tpl2.open(filename2 + ".tpl", ios::binary);
	if (tpl2.good() == false) {
		cout << "Second TPL not found! (" << filename2 << ".tpl)" << endl;
		return -1;
	}

	cout << "Merging GMAs and TPLs " << filename1 << " and " << filename2 << "..." << endl;

	//Remove old files
	remove((filename1 + "+" + filename2 + ".tpl").c_str());
	remove((filename1 + "+" + filename2 + ".gma").c_str());

	//First merge the GMA files.

	//append number of models
	ofstream newgma(filename1 + "+" + filename2 + ".gma", ios::binary | ios::app);
	uint32_t gma1modelamount = fileIntPluck(gma1, 0x0);
	uint32_t gma2modelamount = fileIntPluck(gma2, 0x0);
	uint32_t newgmamodelamount = gma1modelamount + gma2modelamount;
	saveIntToFileEnd(newgma, newgmamodelamount);

	//Calculate length and start positions of gma1 and gma2 modellists
	uint32_t gma1nameliststart = 0x08*gma1modelamount + 0x08;
	uint32_t gma2nameliststart = 0x08*gma2modelamount + 0x08;


	// Find position of final name in the model list
	int gma1lastnonempty = indexOfFinalNonEmptyEntry(gma1, gma1modelamount);
	int gma2lastnonempty = indexOfFinalNonEmptyEntry(gma2, gma2modelamount);

	uint32_t gma1lastnamestart = fileIntPluck(gma1, 0x08*gma1lastnonempty + 0x0C) + gma1nameliststart;
	uint32_t gma2lastnamestart = fileIntPluck(gma2, 0x08*gma2lastnonempty + 0x0C) + gma2nameliststart;

	uint32_t gma1namelistend = getModelNameLength(gma1, gma1lastnamestart) + gma1lastnamestart;
	uint32_t gma2namelistend = getModelNameLength(gma2, gma2lastnamestart) + gma2lastnamestart + 1;

	uint32_t gma1namelistlength = gma1namelistend - gma1nameliststart;
	uint32_t gma2namelistlength = gma2namelistend - gma2nameliststart;


	//With these we can create the new length of the GMA header
	//The pure header length is the initial bytes, plus the 8 times the number of of models, plus the sum of the lengths of the model name lists
	uint32_t newgmapureheaderlength = 0x8 + (newgmamodelamount)*0x8 + gma1namelistlength + gma2namelistlength;
	uint32_t newgmaheaderpadding = (-newgmapureheaderlength) % 0x20; //to pad it to 20
	uint32_t newgmaheaderlength = newgmapureheaderlength + newgmaheaderpadding;
	saveIntToFileEnd(newgma, newgmaheaderlength);


	//Here let's get the header and total lengths
	uint32_t gma1filelength = getFileLength(gma1);
	uint32_t gma1headerlength = fileIntPluck(gma1, 0x04);
	uint32_t gma1datalength = gma1filelength - gma1headerlength;
	uint32_t gma2filelength = getFileLength(gma2);
	uint32_t gma2headerlength = fileIntPluck(gma2, 0x04);
	uint32_t gma2datalength = gma2filelength - gma2headerlength;


	//The GMA1 bytes need no shifts as it comes first
	copyBytes(gma1, newgma, 0x8, gma1nameliststart-0x8);


	//The GMA2 bytes need an increase in both name list offset and data offset
	for (uint32_t gma2modelnumber = 0; gma2modelnumber < gma2modelamount; gma2modelnumber++) {

		uint32_t gma2modeldataoffset = fileIntPluck(gma2, 0x8+0x8*gma2modelnumber);

		// Don't change the offset if its an empty entry
		if (gma2modeldataoffset != 0xffffffff) {
			saveIntToFileEnd(newgma, gma2modeldataoffset + gma1datalength);
			uint32_t gma2modelnameoffset = fileIntPluck(gma2, 0xC+0x8*gma2modelnumber);
			saveIntToFileEnd(newgma, gma2modelnameoffset + gma1namelistlength);
		} else {
			// Write in an empty header entry
			saveIntToFileEnd(newgma, 0xffffffff);
			saveIntToFileEnd(newgma, 0x0);
		}
		
	}


	// Copy model name lists
	copyBytes(gma1, newgma, gma1nameliststart, gma1namelistlength);
	copyBytes(gma2, newgma, gma2nameliststart, gma2namelistlength);


	//Padding
	padZeroes(newgma, newgmaheaderpadding);


	//GMA1 Model data. Can all be copied over.
	copyBytes(gma1, newgma, gma1headerlength, gma1datalength);


	//GMA2 Model data needs all of its textures shifted up
	//get number of textures from TPL1 and TPL2
	uint32_t tpl1textureamount = fileIntPluck(tpl1, 0x0);
	uint32_t tpl2textureamount = fileIntPluck(tpl2, 0x0);



	//loop for each header
	for (uint32_t gma2modelnumber = 0; gma2modelnumber < gma2modelamount; gma2modelnumber++) {

		// Get model data offset from header
		uint32_t oldstartextraoffset = fileIntPluck(gma2, 0x08 + 0x08 * gma2modelnumber);

		// When this model is empty, oldstartextraoffset is 0xffffffff
		// In this case, we don't have to write any model data
		if (oldstartextraoffset == 0xffffffff) {
			continue;
		}

		uint32_t oldstartpoint = gma2headerlength + oldstartextraoffset; //start of the model data

		// end of model data
		uint32_t oldendpoint = 0;

		// Case for last model in list
		if (gma2modelamount == gma2modelnumber+1) { 
			oldendpoint = getFileLength(gma2);
		} else {
		
			// Need to find next non-empty model entry
			uint32_t nextNonEmptyModelNumber = gma2modelnumber + 1;
			uint32_t oldendextraoffset = fileIntPluck(gma2, 0x08 + 0x08 * nextNonEmptyModelNumber );

			// Adjust offset for empty models
			while (nextNonEmptyModelNumber < gma2modelamount && oldendextraoffset == 0xffffffff) {
				nextNonEmptyModelNumber++;
				oldendextraoffset = fileIntPluck(gma2, 0x08 + 0x08 * nextNonEmptyModelNumber );
			}
		
			// Case for when all subsequent models were empty
			if (nextNonEmptyModelNumber == gma2modelamount) {
				oldendpoint = getFileLength(gma2);
			} else {
				oldendpoint = gma2headerlength + oldendextraoffset;
			}

		}

		// Start with first 0x40 bytes of model header
		copyBytes(gma2, newgma, oldstartpoint, 0x40);
		uint32_t oldmodelheaderlength = 0x40;


		// Copy material entries next
		uint16_t materialamount = fileShortPluck(gma2, oldstartpoint+0x18);

		// Loop for each material
		for (uint32_t materialnumber = 0; materialnumber < materialamount; materialnumber++) {

			// Copy flags
			copyBytes(gma2, newgma, oldstartpoint+0x40+0x20*materialnumber, 0x04);

			// Write new texture index
			uint16_t textureindex = fileShortPluck(gma2, oldstartpoint+0x44+0x20*materialnumber);
			saveShortToFileEnd(newgma, textureindex + tpl1textureamount);

			// Copy rest of the data for the material
			copyBytes(gma2, newgma, oldstartpoint+0x46+0x20*materialnumber, 0x1A);

			// Keep track of the header length
			oldmodelheaderlength += 0x20;
		}

		// Copy the rest of the data for the model
		uint32_t oldmodeldatastart = oldstartpoint + oldmodelheaderlength;
		uint32_t oldmodeldatalength = oldendpoint - oldmodeldatastart;
		copyBytes(gma2, newgma, oldmodeldatastart, oldmodeldatalength);
	}


	//we're done here
	newgma.close();


	//Now for the TPL
	ofstream newtpl(filename1 + "+" + filename2 + ".tpl", ios::binary | ios::app);


	// Write in the new number of textures
	uint32_t newtpltextureamount = tpl1textureamount+tpl2textureamount;
	saveIntToFileEnd(newtpl, newtpltextureamount);

	// Get lengths of original files and headers
	uint32_t tpl1headerlength = fileIntPluck(tpl1, 0x08);
	uint32_t tpl2headerlength = fileIntPluck(tpl2, 0x08);
	uint32_t tpl1length = getFileLength(tpl1);
	uint32_t tpl2length = getFileLength(tpl2);

	//Now to work out the new file header length 
	int newtplpaddingamount = (- 0x04 + (0x10*newtpltextureamount)) % 0x20;
	uint32_t newtplheaderlength = 0x04 + (0x10*newtpltextureamount) + newtplpaddingamount;
	

	// Write in texture headers
	for (uint32_t newtpltexturenumber = 0; newtpltexturenumber < newtpltextureamount; newtpltexturenumber++) {

		if (newtpltexturenumber < tpl1textureamount) {

			//take from tpl1

			// Copy texture format
			copyBytes(tpl1, newtpl, newtpltexturenumber*0x10+0x04, 0x04);

			// Write new texture offset
			uint32_t oldtpl1textureoffset = fileIntPluck(tpl1, (newtpltexturenumber*0x10) + 0x08);

			// If offset is zero then this is an empty header entry, keep it at zero
			if (oldtpl1textureoffset == 0x0) {
				saveIntToFileEnd(newtpl, 0x0);
			} else {
				saveIntToFileEnd(newtpl, oldtpl1textureoffset - tpl1headerlength + newtplheaderlength);
			}

			// Copy rest of the texture header
			copyBytes(tpl1, newtpl, (newtpltexturenumber*0x10) + 0x0C, 0x08);

		} else { //newtpltexturenumber >= tpl1textureamount

			//take from tpl2

			// Copy texture format
			copyBytes(tpl2, newtpl, (newtpltexturenumber-tpl1textureamount)*0x10+0x04, 0x04);

			// Write new texture offset
			uint32_t oldtpl2textureoffset = fileIntPluck(tpl2, ((newtpltexturenumber-tpl1textureamount)*0x10) + 0x08);

			// If offset is zero then this is an empty header entry, keep it at zero
			if (oldtpl2textureoffset == 0x0) {
				saveIntToFileEnd(newtpl, 0x0);
			} else {
				saveIntToFileEnd(newtpl, oldtpl2textureoffset - tpl2headerlength + tpl1length - tpl1headerlength + newtplheaderlength); //AAAAAA
			}

			// Copy rest of the texture header
			copyBytes(tpl2, newtpl, ((newtpltexturenumber-tpl1textureamount)*0x10)+0x0C, 0x08);
		}

	}

	// Pad tpl header with 00010203... pattern
	for (uint8_t tplpaddingpointer = 0x0; tplpaddingpointer < newtplpaddingamount; tplpaddingpointer++) {
		newtpl << tplpaddingpointer;
	}


	//Copy remaining data bytes, tpl1
	if (tpl1textureamount != 0) {
		copyBytes(tpl1, newtpl, tpl1headerlength, tpl1length - tpl1headerlength);
	}


	//tpl2
	if (tpl2textureamount != 0) {
		copyBytes(tpl2, newtpl, tpl2headerlength, tpl2length - tpl2headerlength);
	}


	// Close all files
	newtpl.close();
	gma1.close();
	tpl1.close();
	gma2.close();
	tpl2.close();

	return 0;
}

/*

	Utility Functions

*/

bool isLittleEndian() {
	//shamelessly pinched from StackOverflow
	short int number = 0x1;
	char *numPtr = (char*)&number;
	return (numPtr[0] == 1);
	
}

uint32_t fileIntPluck (ifstream& bif, uint32_t offset) {
	bif.seekg(offset, bif.beg);
	char buffer[4]; //4 byte buffer
	bif.read(buffer, 0x4);
	//convert signed to unsigned
	unsigned char* ubuf = reinterpret_cast<unsigned char*>(buffer);
	uint32_t returnint = 0;
	if (isLittleEndian()) {
		returnint = (ubuf[3] << 0) | (ubuf[2] << 8) | (ubuf[1] << 16) | (ubuf[0] << 24);
	} else {
		returnint = (ubuf[0] << 0) | (ubuf[1] << 8) | (ubuf[2] << 16) | (ubuf[3] << 24);
	}
	return returnint;
}

uint16_t fileShortPluck (ifstream& bif, uint32_t offset) {
	bif.seekg(offset, bif.beg);
	char buffer[2]; //2 byte buffer
	bif.read(buffer, 0x2);
	//convert signed to unsigned
	unsigned char* ubuf = reinterpret_cast<unsigned char*>(buffer);
	uint16_t returnshort = 0;
	if (isLittleEndian()) {
		returnshort = (ubuf[1] << 0) | (ubuf[0] << 8);
	} else {
		returnshort = (ubuf[0] << 0) | (ubuf[1] << 8);
	}
	return returnshort;
}

int helpText() {
	cout << "How to use gmatool:" << endl 
		<< "Each of these saves extracted data to unique and readable gma and tpl files, and do not alter the input files." << endl
		<< "\"-ge <name>\" - Extracts goal data from <name>.gma and <name>.tpl." << endl 
		<< "\"-se <name>\" - Extracts switch data from <name>.gma and <name>.tpl, saving each switch to unique files, including switch bases." << endl 
		<< "\"-me <name> <modelname>\" - Extracts the data of the model called \"modelname\" from <name>.gma and <name>.tpl." << endl
		<< "\"-le <name>\" - Lists all models in <name>.gma, then accepts a model name from command line. Works the same as \"-me\"." << endl
		<< "\"-m <name1> <name2>\" - Extracts all data from <name1>.gma, <name2>.gma, <name1>.tpl and <name2>.tpl, and combines the data. "
		<< "The second file's data is always placed after the first." << endl;
	return 1;
}

void copyBytes(ifstream& bif, ofstream& bof, uint32_t offset, uint32_t length) {
	char bytes[length];
	bif.seekg(offset, bif.beg);
	bif.read(bytes, length);
	bof.write(bytes, length);
}

void saveIntToFileEnd(ofstream& bof, uint32_t newint) {
	char buffer[4];
	char* initbuffer = reinterpret_cast<char*>(&newint);
		//assigns values wrt endianness
		if (isLittleEndian()) {
			for (int i = 0; i < 4; i++) {
				buffer[i] = initbuffer[3-i];
			}
		} else {
			for (int i = 0; i < 4; i++) {
				buffer[i] = initbuffer[i];
			}
		}
		//write float
		bof.write(buffer, sizeof(uint32_t));
}

void saveShortToFileEnd(ofstream& bof, uint16_t newint) {
	char buffer[2];
	char* initbuffer = reinterpret_cast<char*>(&newint);
		//assigns values wrt endianness
		if (isLittleEndian()) {
			for (int i = 0; i < 2; i++) {
				buffer[i] = initbuffer[1-i];
			}
		} else {
			for (int i = 0; i < 2; i++) {
				buffer[i] = initbuffer[i];
			}
		}
		//write float
		bof.write(buffer, sizeof(uint16_t));
}

uint32_t getFileLength(ifstream& bif) {
	bif.seekg(0, bif.end);
	return bif.tellg();
}

// Length of a model name from a gma header, Includes the terminating byte.
uint32_t getModelNameLength(ifstream& bif, uint32_t modelnameoffset) {

	// Get to start of model name
	bif.seekg(modelnameoffset, bif.beg);

	int modelnamelength = 0;
	bool endofmodelname = false;
	char endingbyte;

	// Read name byte by byte
	while (endofmodelname == false) {
		modelnamelength++;
		bif.read(&endingbyte, 1);
		if (bif.eof() || endingbyte == '\0') {
			endofmodelname = true;
		}
	}
	
	return modelnamelength;
}

void padZeroes(ofstream& bof, uint32_t zeronumber) {
	char buffer[zeronumber];
	memset(buffer, 0x0, zeronumber);
	bof.write(buffer, zeronumber);
}

string readNameFromGma(ifstream& gma, uint32_t modellistpointer, uint32_t modelnamelength) {
	char bytes[modelnamelength];
	gma.seekg(modellistpointer, gma.beg);
	gma.read(bytes, modelnamelength);
	string modelname(bytes);
	return modelname;
}

/*

	Functions for dealing with empty model / texture entries

*/

// Convert from position in model list to position in header
int modelNumberWithEmpties(ifstream& oldgma, int modelnumber) {

	// Starts at -1 to deal with leading empty entries
	int currententry = -1;
	int entrywithempties = -1;

	// Keep separate counts for empty and nonempty entries, stopping when we find our original nonempty model
	while (currententry < modelnumber) {
		entrywithempties++;
		uint32_t emptyIndicator = fileIntPluck(oldgma, 0x08 + 0x8 * entrywithempties);
		if (emptyIndicator != 0xffffffff) {
			currententry++;
		}
		
	}

	return entrywithempties;
}

// Index of the last model header entry that isn't an empty entry
int indexOfFinalNonEmptyEntry(ifstream& oldgma, int modelamount) {
	int result = 0;
	for (int currententry = 0; currententry < modelamount; currententry++) {
		uint32_t emptyIndicator = fileIntPluck(oldgma, 0x08 + 0x8 * currententry);
		if (emptyIndicator != 0xffffffff) {
			result = currententry;
		}
	}
	return result;
}

// Counts the number of model header entries for non empty models
// This should be the same as the length of the model list
int modelAmountWithoutEmpties(ifstream& oldgma, int modelamount) {

	int currententry = 0;
	int entrywithempties = 0;
	uint32_t emptyIndicator = 0;

	while (entrywithempties < modelamount) {
		emptyIndicator = fileIntPluck(oldgma, 0x08 + 0x8 * entrywithempties);
		if (emptyIndicator != 0xffffffff) {
			currententry++;
		}
		entrywithempties++;
		
	}

	return currententry;

}

// Count the number of texture headers until the next non empty entry
// (Returns 1 when there are no empty entries)
int nextNonEmptyTextureOffset(ifstream& oldtpl, uint32_t headerposition) {
	int positionoffset = 1;

	// This will be 0 if the next texture is empty
	uint32_t emptyindicator = fileIntPluck(oldtpl, headerposition + 0x14);

	while (emptyindicator == 0x0) {
		positionoffset++;
		emptyindicator = fileIntPluck(oldtpl, headerposition + (positionoffset * 0x10) + 0x04);
	}
	return positionoffset;
}
