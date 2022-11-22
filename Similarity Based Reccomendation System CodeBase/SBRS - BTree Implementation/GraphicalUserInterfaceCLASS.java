/*
 * GUI for Assignment 2
 */
package Assignment2;

import javax.swing.SwingUtilities;


public class GraphicalUserInterfaceCLASS {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new ThreadForGUI());
    }

    private static class ThreadForGUI implements Runnable {

        public void run() {
            GraphicalUserInterfaceCLASS gui = new GraphicalUserInterfaceCLASS();
        }
    }

    public GraphicalUserInterfaceCLASS() {
        FrameGUICLASS frame = new FrameGUICLASS("Richard Kanin");
    }

}