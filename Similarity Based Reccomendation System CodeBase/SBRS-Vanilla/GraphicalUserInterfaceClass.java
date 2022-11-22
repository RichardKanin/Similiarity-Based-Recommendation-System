/*
 * GUI for Assignment 1
 */
package Assignment1;

import javax.swing.SwingUtilities;
import java.awt.*;


public class GraphicalUserInterfaceClass {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new ThreadForGUI());
    }

    private static class ThreadForGUI implements Runnable {

        public void run() {
            GraphicalUserInterfaceClass graphicalUserInterfaceClass = new GraphicalUserInterfaceClass();
        }
    }

    public GraphicalUserInterfaceClass() {
        FrameClass frameClass = new FrameClass("Richard Kanin");
    }

}