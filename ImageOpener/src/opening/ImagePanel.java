package opening;

import java.awt.LayoutManager;
import java.io.File;

import javax.swing.JPanel;

public abstract class ImagePanel extends JPanel {
	
	private File imageFile;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImagePanel(File imageFile) {
		this.imageFile = imageFile;
	}
	
	public File getFile() {
		return imageFile;
	}

	public ImagePanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public ImagePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public ImagePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public abstract int getImageWidth();

	public abstract int getImageHeight();

}
