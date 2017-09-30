import javafx.scene.control.ProgressBar;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class GUI implements ActionListener {
    // Class implements three different types of listener.

    JTextArea txtArea;
    JButton runBtn;
    JButton cancelBtn;
    JProgressBar progressBar;


    public SwingWorker<Integer, String> worker;

    public static void createAndShowGUI() {

        //Frame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Thread Safe GUI");
        frame.setSize(600, 600);
        frame.setLocation(600, 300);

        GUI demo = new GUI();
        frame.setContentPane(demo.createContentPane());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    private JPanel createContentPane() {

        JPanel totalGUI = new JPanel();
        totalGUI.setSize(600, 600);
        totalGUI.setLayout(new MigLayout("", "", ""));

        // The main story for the JTextArea
        String story = "";

        // Plus we instantiate the TextArea.
        txtArea = new JTextArea(story, 5, 30);
        txtArea.setEditable(false);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);


        // We create the ScrollPane and instantiate it with the TextArea as an argument
        // along with two constants that define the behaviour of the scrollbars.
        JScrollPane area = new JScrollPane(txtArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // We then set the preferred size of the scrollpane.
        //area.setPreferredSize(new Dimension(300, 200));


        runBtn = new JButton("Run");
        runBtn.addActionListener(this);
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        cancelBtn.setEnabled(false);

        //Where the GUI is constructed:
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setString("Progress: 0%");
        progressBar.setStringPainted(true);


        totalGUI.add(area, "span, push, grow");
        totalGUI.add(progressBar, "span, grow");
        totalGUI.add(runBtn, "span, grow, split");
        totalGUI.add(cancelBtn, "span, grow, split");


        totalGUI.setOpaque(true);
        return totalGUI;
    }

    // For the Action Events.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == runBtn) {
            Run("RUN");
            runBtn.setEnabled(false);
            cancelBtn.setEnabled(true);
        } else if (e.getSource() == cancelBtn) {
            worker.cancel(true);
            runBtn.setEnabled(true);
            cancelBtn.setEnabled(false);
            progressBar.setValue(0);
            progressBar.setString("Progress: 0%");
        }
    }

    // This rewrites the story depending on the actions so far in the various
    // widgets then re-sends the story to the TextArea.
    private void Run(String str) {
        String story = str;

        worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {

                LineNumberReader lnr = new LineNumberReader(new FileReader(new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt")));
                lnr.skip(Long.MAX_VALUE);
                System.out.println(lnr.getLineNumber() + 1); //Add 1 because line index starts at 0
                int lineCount = lnr.getLineNumber() + 1;
                // Finally, the LineNumberReader object should be closed to prevent resource leak
                lnr.close();

                try ( BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt"))))
                {
                    String line;
                    int count = 0;
                    while((line = br.readLine()) != null)
                    {
                        count++;
                        publish(line);
                        setProgress(count/lineCount * 100);
                    }
                } catch ( IOException ioe )
                {
                    ioe.getMessage();
                }


//                publish("Iterating");
//                setProgress(0);
//                for (int i = 0; i < 10; i++) {
//                    if (isCancelled()) {
//                        return 0;
//                    }
//                    setProgress(i * 10);
//                    Thread.sleep(1000);
//                    publish("" + i);
//                }
//                setProgress(100);
//                publish("Complete");
                return 1;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String lines : chunks) {
                    txtArea.append(lines + "\r\n");
                }
                int prog = worker.getProgress();
                progressBar.setValue(prog);
                progressBar.setString("Progress: " + prog + "%");
            }

            @Override
            protected void done() {
                runBtn.setEnabled(true);
                cancelBtn.setEnabled(false);
                if (isCancelled()) {
                    txtArea.append("\n!! Cancelled !!\n");
                } else
                {
                    txtArea.append("\n>> Done <<\n");
                }
            }
        };
        worker.execute();
        txtArea.setText(story);
    }
}

