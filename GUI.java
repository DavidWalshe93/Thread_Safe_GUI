import javafx.scene.control.ProgressBar;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GUI implements ActionListener {
    // Class implements three different types of listener.
    JTextArea txtArea;
    JButton runBtn;
    JButton cancelBtn;
    JProgressBar progressBar;
    JLabel timeStamp;
    JButton zipBtn;


    public SwingWorker<Integer, String> worker;
    public SwingWorker<Integer, String> workerZip;

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
        runBtn = new JButton("Load");
        runBtn.addActionListener(this);
        zipBtn = new JButton("Zip");
        zipBtn.addActionListener(this);
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
        totalGUI.add(zipBtn, "span, grow, split");
        totalGUI.add(cancelBtn, "span, grow, split");



        totalGUI.setOpaque(true);
        return totalGUI;
    }

    // For the Action Events.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == runBtn) {
            long startTime = System.currentTimeMillis();
            txtArea.setText("");
            progressBar.setValue(0);
            progressBar.setString("Progress: 0%");
            Run(startTime);
            runBtn.setEnabled(false);
            cancelBtn.setEnabled(true);

        } else if (e.getSource() == cancelBtn) {
            worker.cancel(true);
            runBtn.setEnabled(true);
            cancelBtn.setEnabled(false);
            progressBar.setValue(0);
            progressBar.setString("Progress: 0%");
        } else if(e.getSource() == zipBtn) {
            txtArea.setText("");
            Zip(System.currentTimeMillis());
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

    private void Zip(long time)
    {

        workerZip = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                File f = new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.zip");

                try(
                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
                        BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt")))
                )
                {
                    ZipEntry e = new ZipEntry("big.txt");
                    out.putNextEntry(e);

                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while((line = br.readLine()) != null)
                    {
                        sb.append(line + "\r\n");
                    }
                    byte[] data = sb.toString().getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                    out.close();

                }catch (Exception e)
                {
                    e.getMessage();
                }
                return 1;
            }

            @Override
            protected void done() {
                txtArea.setText("Zip Complete");
                Path file = Paths.get("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.txt");
                Path zipFile = Paths.get("C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\big.zip");

                BasicFileAttributes fileAttr = null;
                BasicFileAttributes zipFileAttr = null;
                try {

                    fileAttr = Files.readAttributes(file, BasicFileAttributes.class);
                    zipFileAttr = Files.readAttributes(zipFile, BasicFileAttributes.class);
                } catch (Exception e) {
                    e.getMessage();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("########################################################\r\n");
                sb.append("Zip Information\r\n");
                sb.append("########################################################\r\n");
                sb.append("Creation Time \"big.zip\"\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + zipFileAttr.lastModifiedTime() + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("\"big.txt\" Size\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + fileAttr.size() + " B\r\n");
                sb.append(">\t" + (float)fileAttr.size()/(1024*1024) + " MB\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("\"big.zip\" Size\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + zipFileAttr.size() + " B\r\n");
                sb.append(">\t" + (float)zipFileAttr.size()/(1024*1024) + " MB\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("Compression Ratio\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + (float)zipFileAttr.size()/(float)fileAttr.size() + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("File Size Reduction\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + (100 - ((float)zipFileAttr.size()/(float)fileAttr.size())*100) + "%\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                txtArea.setText(sb.toString());
            }
        };
        workerZip.execute();

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

