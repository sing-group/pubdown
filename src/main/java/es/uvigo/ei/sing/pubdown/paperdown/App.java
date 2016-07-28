package es.uvigo.ei.sing.pubdown.paperdown;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import es.uvigo.ei.sing.pubdown.paperdown.gui.PaperDownSwing;


public class App {

	public static void main(String args[]) {

		// final String userScopusQuery = "vigo AND ourense".replace(" ", "+");
		// final String userDOIQuery = "10.1016/j.bjm.2016.04.019".replace(" ",
		// "");
		// final String userDOIQuery2 = "10.1093/femspd/ftw036".replace(" ",
		// "");
		// final String userPubMedQuery = "vigo AND ourense".replace(" ", "+");

		// final String jarvestApiKey = "a1549163d9b16421237ec29c9bbbdf29";
		// final String userApiKey = "b3a71de2bde04544495881ed9d2f9c5b";

		SwingUtilities.invokeLater(() -> {
			final JFrame frame = new JFrame("PaperDown");

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new PaperDownSwing(frame));
			frame.pack();
			frame.setLocationRelativeTo(null);

			frame.setVisible(true);
		});
	}
}
