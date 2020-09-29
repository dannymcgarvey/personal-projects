package opening;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PNGPanel extends ImagePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage img;

     public PNGPanel(File imageFile) throws IOException {
    	 super(imageFile);
    	 this.setLayout(new BorderLayout());
    	 this.img = ImageIO.read(imageFile);
     }

     @Override
     public Dimension getPreferredSize() {
         return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
     }

     @Override
     public void paint(Graphics g) {
         super.paint(g);
         if (img != null) {
             g.drawImage(img, 0, 0, this);
         }
     }
     
     public int getImageWidth() {
    	 return img.getWidth();
     }
     
     public int getImageHeight() {
    	 return img.getHeight();
     }

}
