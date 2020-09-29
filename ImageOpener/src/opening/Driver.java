package opening;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;



public class Driver {

	private static final String BAT_NAME;
	private static final String JAR_NAME = "open.jar";
	private static final boolean isWin;
	private static JFrame frame;

	static {
		if(System.getProperty("os.name").toUpperCase().indexOf("WIN") >= 0) {
			isWin = true;
			BAT_NAME = "open.bat";
		} else {
			isWin = false;
			BAT_NAME = "open.sh";
		}
	}



	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable () {
			@Override
			public void run() {
				try {
					File batch = new File(BAT_NAME);
					if (!batch.exists() && isWin) {
						createLauncher();
					} else {
						createAndShowGUI(args);
					}
				} catch (IOException e) {
					System.out.println("Couldn't run.");
					e.printStackTrace();
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

			}
		}
				);
	}

	private static void createLauncher() throws IOException {
		File launcher = new File(BAT_NAME);
		launcher.createNewFile();
		launcher.setWritable(true);
		launcher.setReadable(true);
		launcher.setExecutable(true);
		ArrayList<String> text = new ArrayList<>();
		text.add("cd " + System.getProperty("user.dir"));
		text.add("java -jar " + System.getProperty("user.dir") + "\\" + JAR_NAME + " %1");
		Files.write(Paths.get(BAT_NAME), text);
	}

	private static void createAndShowGUI(String[] args) throws IOException {
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		if(args.length == 1) {

			displayImage(new File(args[0]));


		} else if (args.length == 0) {


			frame.setSize(new Dimension(500, 500));
			frame.setTitle("Bottom Text");
			JPanel temp = new JPanel(new BorderLayout());

			JButton openButton = new JButton("open image");
			openButton.addActionListener(new OpenListener());
			temp.add(openButton, BorderLayout.NORTH);
			frame.setContentPane(temp);


		}
		reCenter();
		frame.setVisible(true);
	}

	private static class OpenListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			FileDialog fd = new FileDialog(frame);
			fd.setVisible(true);
			File[] f = fd.getFiles();
			if (f.length > 0) {
				try {
					displayImage(f[0]);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}

	}

	public static void displayImage(File imageFile) throws IOException {
		ContainerPanel panel = new ContainerPanel(imageFile);
		frame.setSize(panel.getWidth() + 15, panel.getHeight() + 35);
		reCenter();
		frame.setTitle(imageFile.getAbsolutePath());
		frame.setContentPane(panel);
	}

	private static void reCenter() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int upperLeftCornerX = (screenSize.width - frame.getWidth()) / 2;
		int upperLeftCornerY = (screenSize.height - frame.getHeight()) / 2;
		frame.setLocation(upperLeftCornerX, upperLeftCornerY);
	}

}
