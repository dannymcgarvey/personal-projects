#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <algorithm>
#include <iterator>

using namespace std;

#define GOAL_EXTRACT 1
#define SWITCH_EXTRACT 2
#define SPECIFIC_MODEL 3
#define LIST_MODELS 4

bool isLittleEndian();
uint32_t fileIntPluck (ifstream& bif, uint32_t offset);
uint16_t fileShortPluck (ifstream& bif, uint32_t offset);
int helpText();
void copyBytes(ifstream& bif, ofstream& bof, uint32_t offset, uint32_t length);
void saveIntToFileEnd(ofstream& bof, uint32_t newint);
void saveShortToFileEnd(ofstream& bof, uint16_t newint);
uint32_t getFileLength(ifstream& bif);
uint32_t getModelNameLength(ifstream& bif, uint32_t modelnameoffset);
void padZeroes(ofstream& bof, uint32_t zeronumber);
string readNameFromGma(ifstream& gma, uint32_t modellistpointer, uint32_t modelnamelength);

int modelNumberWithEmpties(ifstream& oldgma, int modelnumber);
int modelAmountWithoutEmpties(ifstream& oldgma, int modelamount);
int indexOfFinalNonEmptyEntry(ifstream& oldgma, int modelamount);
int nextNonEmptyTextureOffset(ifstream& oldtpl, uint32_t headerposition);

void modelWriteToFiles(string filename, ifstream& oldgma, ifstream& oldtpl, int modelamount, int modelnumber, uint32_t modelnamelength, string modelname, string suffix);
int modelExtract(string filename, int type, string specificmodel);
int gmatplMerge(string filename1, string filename2);