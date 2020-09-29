package opening;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import conversion.BTextPanel;
import conversion.ImageConverter;

public class ContainerPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ImagePanel image;
	private JButton saveButton = new JButton("Save as .bxt");
	private boolean hasButton = false;

	public ContainerPanel(File imageFile) throws IOException {
		setLayout(new BorderLayout());
		String extension = imageFile.getAbsolutePath().toUpperCase();
		extension = extension.substring(extension.length() - 3);
		if (extension.equals("PNG") || extension.equals("JPG")) {
			image = new PNGPanel(imageFile);
			add(image, BorderLayout.CENTER);
			saveButton.addActionListener(new SaveListener(imageFile));
			add(saveButton, BorderLayout.SOUTH);
			hasButton = true;
		} else if(extension.equals("BXT")){
			image = new BTextPanel(imageFile);
			add(image, BorderLayout.CENTER);
		} else {
			System.exit(-1);
		}
		
	}
	
	@Override
	public int getWidth() {
		return (image.getImageWidth() > 200 ? image.getImageWidth() : 200);
	}
	@Override
	public int getHeight() {
		
		return image.getImageHeight() + (hasButton ? 28 : 0);
	}
	
	private class SaveListener implements ActionListener {
		
		private File imageFile;
		
		private SaveListener(File imageFile) {
			this.imageFile = imageFile;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FileDialog fd = new FileDialog(new JFrame(), "Save as .bxt", FileDialog.SAVE);
			fd.setFilenameFilter(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.toUpperCase().endsWith(".BXT");
				}
				
			});
			fd.setFile("image.bxt");
			fd.setVisible(true);
			File[] f = fd.getFiles();
			if (f.length > 0) {
				try {
					ImageConverter.writeToBXT(imageFile, f[0]);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}
		
	}

}
