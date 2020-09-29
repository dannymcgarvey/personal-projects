package conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
public class ImageConverter {

	static final String header = "TOP TEXT";
	static final String end = "BOTTOM TEXT";
	public static boolean writeToBXT(File pngFile, File bxtFile) throws IOException {

		if(!pngFile.exists()) {
			return false;
		}
		bxtFile.delete();
		
		BufferedImage png = ImageIO.read(pngFile);
		FileOutputStream bxt = new FileOutputStream(bxtFile);
		
		//Write Header
		byte[] buffer = new byte[header.length()];
		for(int i = 0; i < header.length(); i++) {
			buffer[i] = (byte) header.charAt(i);
		}
		bxt.write(buffer);
		
		
		int width = png.getWidth();
		int height = png.getHeight();
		
		//Initialize metadata
		writeIntToFile(bxt,0);
		writeIntToFile(bxt,width);
		writeIntToFile(bxt,height);
		
		
		
		
		buffer = new byte[0x100];
		int bufferPos = 0;
		for (int c = 0; c < 3; c++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) { 
					buffer[bufferPos++] = (byte)((png.getRGB(x, y)) >> (c * 8));
					if (bufferPos >= 0x100) {
						bufferPos = 0;
						bxt.write(buffer);
						buffer = new byte[0x100];
					}
				}
			}
		}
		if (bufferPos != 0) {
			bxt.write(buffer);
		}
		
		//Write End
		buffer = new byte[end.length() + 1];
		buffer[0] = 0x0A;
		for (int i = 0; i < end.length(); i++) {
			buffer[i + 1] = (byte) end.charAt(i);
		}
		bxt.write(buffer);
		bxt.flush();
		bxt.close();
		return true;
	}
	
	protected static void writeIntToFile(FileOutputStream file, int value) throws IOException {
		byte[] intBytes = new byte[4];
		for (int i = 0; i < intBytes.length; i++) {
			intBytes[intBytes.length - 1 - i] = (byte)(value >> (i * 8));
		}
		file.write(intBytes);
	}

}
