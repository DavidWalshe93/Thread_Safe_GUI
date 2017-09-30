import javafx.scene.control.ProgressBar;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
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
    JLabel timeStamp;


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
        DefaultCaret caret = (DefaultCaret)txtArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);


        // We create the ScrollPane and instantiate it with the TextArea as an argument
        // along with two constants that define the behaviour of the scrollbars.
        JScrollPane area = new JScrollPane(txtArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // We then set the preferred size of the scrollpane.
        //area.setPreferredSize(new Dimension(300, 200));

        timeStamp = new JLabel("Time Elapsed: ");
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
        totalGUI.add(timeStamp, "span, grow");
        totalGUI.add(progressBar, "span, grow");
        totalGUI.add(runBtn, "span, grow, split");
        totalGUI.add(cancelBtn, "span, grow, split");


        totalGUI.setOpaque(true);
        return totalGUI;
    }

    // For the Action Events.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == runBtn) {
            long startTime = System.currentTimeMillis();
            Run(startTime);
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

    private static String timeFormatter(long milliseconds){
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
        milliseconds %= 1000;
        String timeFormattedString = hours + ":" + minutes + ":" + seconds + "." + milliseconds;
        return timeFormattedString;
    }

    // This rewrites the story depending on the actions so far in the various
    // widgets then re-sends the story to the TextArea.
    private void Run(long time) {

        worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {

                LineNumberReader lnr = new LineNumberReader(new FileReader(new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt")));
                lnr.skip(Long.MAX_VALUE);
                float lineCount = lnr.getLineNumber() + 1;
                // Finally, the LineNumberReader object should be closed to prevent resource leak
                lnr.close();


                try ( BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt"))))
                {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    while((line = br.readLine()) != null)
                    {
                        count++;
                        sb.append(count + "- " + line + "\r\n");
                        setProgress(Math.round((count/lineCount) * 100f));
                    }
                    publish(sb.toString());
                } catch ( IOException ioe )
                {
                    ioe.getMessage();
                }
                return 1;
            }

            @Override
            protected void process(List<String> chunks) {
                System.out.println(chunks.size());
                for (String lines : chunks) {
                    txtArea.append(lines + "\r\n");
                }
                int prog = worker.getProgress();
                long timeElapsed = System.currentTimeMillis() - time;
                timeStamp.setText("Time Elapsed: " + GUI.timeFormatter(timeElapsed));
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
    }
}

