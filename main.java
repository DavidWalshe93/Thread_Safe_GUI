import javax.swing.*;

public class main {
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               GUI gui = new GUI();
               gui.createAndShowGUI();
            }
        });
    }
}
