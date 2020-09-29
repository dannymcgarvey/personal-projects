package conversion;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

import opening.ImagePanel;


public class BTextPanel extends ImagePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int width;
	private int height;
	private int opens;

	public BTextPanel(File imageFile) throws IOException {

		super(imageFile);
		if (!isValid(imageFile)) {
			throw new IllegalArgumentException("Invalid image");
		}
		RandomAccessFile bxt = new RandomAccessFile(imageFile, "rw");
		try {
			bxt.seek(ImageConverter.header.length());
			opens = bxt.readInt();
			width = bxt.readInt();
			height = bxt.readInt();
			opens++;
			bxt.seek(ImageConverter.header.length());
			bxt.writeInt(opens);
		} finally {
			bxt.close();
		}

	}

	@Override
	public int getImageWidth() {
		// TODO Auto-generated method stub
		return width;
	}

	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return height;
	}

	public int getNumPixels() {
		return width * height;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		long blueOffset = ImageConverter.header.length() + (3 * 4);
		long greenOffset = blueOffset + getNumPixels();
		long redOffset = greenOffset + getNumPixels();
		try {
			RandomAccessFile redStream = new RandomAccessFile(getFile(), "r");
			RandomAccessFile greenStream = new RandomAccessFile(getFile(), "r");
			RandomAccessFile blueStream = new RandomAccessFile(getFile(), "r");
			try {
				redStream.seek(redOffset);
				greenStream.seek(greenOffset);
				blueStream.seek(blueOffset);
				Color currColor;
				for(int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						currColor = new Color(redStream.read(), greenStream.read(), blueStream.read());
						g.setColor(currColor);
						g.drawRect(x,y,1,1);
					}
				}
			} finally {
				redStream.close();
				greenStream.close();
				blueStream.close();
			}
		} catch (FileNotFoundException e) {
			g.clearRect(0, 0, width, height);
			System.out.println("Cant read image");
		} catch (IOException e1) {
			g.clearRect(0, 0, width, height);
			System.out.println("Cant read image");
		}
	}

	private boolean isValid(File imageFile) throws IOException {
		RandomAccessFile bxt = new RandomAccessFile(imageFile, "rw");
		try {
			byte[] buffer = new byte[ImageConverter.header.length()];
			bxt.read(buffer);
			if (!ImageConverter.header.equals(new String(buffer))) {
				return false;
			}
			buffer = new byte[ImageConverter.end.length()];
			bxt.seek(imageFile.length() - ImageConverter.end.length());
			bxt.read(buffer);
			if (!ImageConverter.end.equals(new String(buffer))) {
				return false;
			}
		} finally {
			bxt.close();
		}
		return true;
	}

}
