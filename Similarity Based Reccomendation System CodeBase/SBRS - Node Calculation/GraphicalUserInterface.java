/*
 * GUI for Assignment 3
 */
package Assignment3;

import javax.swing.SwingUtilities;

public class GraphicalUserInterface {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new ThreadForGUI());
    }
    private static class ThreadForGUI implements Runnable {
        public void run() {
            GraphicalUserInterface gui = new GraphicalUserInterface();
        }
    }
    public GraphicalUserInterface() {
        FrameGUI frame = new FrameGUI("Yelp Reviews");
    }
}