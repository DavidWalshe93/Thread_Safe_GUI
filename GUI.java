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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
  Author: David Walshe
  Date: 01/10/2017
  Description: This program is used to create a thread safe GUI using the SwingWorker Class. It allows for asynchronous time consuming operations to process in the
               background while still allowing full control of the GUI to the user without signs of lag or unresponsiveness. It also includes the logger class to
               log important events during the application's life cycle.

               The GUI makes use of some basic components as well as use of MigLayout.

               The GUI allows for the loading of a large text file into a JTextArea using a SwingWorker.
               It also allows for a large text file to be zipped and show the compression ratio and size reduction of the original and compressed file.

 */

public class GUI implements ActionListener {

    //Create all GUI element reference holders
    JTextArea txtArea;
    JButton loadBtn;
    JButton cancelBtn;
    JProgressBar progressBar;
    JLabel timeStamp;
    JButton zipBtn;
    JFileChooser fileChooser;

    private static final String CLASS_NAME = GUI.class.getName();

    //Create Swing Workers to process asynchronous blocking requests in the GUI.
    public SwingWorker<Integer, String> worker;
    public SwingWorker<Integer, String> workerZip;
    private static final Logger logger = Logger.getLogger(GUI.class.getName());
    private FileHandler fileHandler;
    private ConsoleHandler cs;

    public GUI()
    {
        this.addFileHandler(logger);
        logger.log(Level.INFO, "");

        //Kick off a die thread when the application finishes to format the output from the logger.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                logger.entering(CLASS_NAME, "");
                Process proc;
                BufferedReader br;
                try{
                    String path = System.getProperty("user.dir");
                    while(path.contains("\\")) {
                        path = path.replace("\\", "/");
                    }
                    path += "/GUI.log";
                    logger.log(Level.INFO, "Executing Perl Formatting Script on GUI Exit");
                    proc = Runtime.getRuntime().exec("Perl C:\\Users\\David\\Desktop\\Client-Server\\Assignment_1_Thread_Safe_GUI\\src\\Parse.pl " + path);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Execption: " + e.getMessage());
                }
            }
        }, "Shutdown-thread"));
    }

    private void addFileHandler(Logger logger) {
        try {
            fileHandler = new FileHandler(CLASS_NAME + ".log");

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Execption: " + ex.getMessage());
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, "Execption: " + ex.getMessage());
        }
        logger.addHandler(fileHandler);
        cs = new ConsoleHandler();
        logger.addHandler(cs);
        logger.setLevel(Level.FINER);
        fileHandler.setLevel(Level.ALL);
        cs.setLevel(Level.ALL);
    }

    //GUI maker method called by main.
    public static void createAndShowGUI() {
        logger.entering(CLASS_NAME, "createAndShowGUI");

        logger.log(Level.INFO, "Creating GUI");
        JFrame frame = new JFrame("Thread Safe GUI");
        frame.setSize(600, 600);
        frame.setLocation(600, 300);

        GUI demo = new GUI();

        frame.setContentPane(demo.createContentPane());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        logger.exiting(CLASS_NAME, "createAndShowGUI");
    }

    //Create the GUI look and feel. Init and setup up all components in the GUI.
    private JPanel createContentPane() {
        logger.entering(CLASS_NAME, "createContentPane");
        JPanel totalGUI = new JPanel();
        totalGUI.setSize(600, 600);
        totalGUI.setLayout(new MigLayout("", "", ""));

        // The main story for the JTextArea
        String text = "";

        //Instantiate the TextArea to suit the GUI
        txtArea = new JTextArea(text, 5, 30);
        txtArea.setEditable(false);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) txtArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);


        // Create the ScrollPane and instantiate it with the TextArea as an argument
        // along with two constants that define the behaviour of the scrollbars.
        JScrollPane area = new JScrollPane(txtArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        timeStamp = new JLabel("Time Elapsed: ");
        loadBtn = new JButton("Load");
        loadBtn.addActionListener(this);
        zipBtn = new JButton("Zip");
        zipBtn.addActionListener(this);
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        cancelBtn.setEnabled(false);


        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setString("Progress: 0%");
        progressBar.setStringPainted(true);

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Where the GUI is constructed:
        totalGUI.add(area, "span, push, grow");
        totalGUI.add(timeStamp, "span, grow");
        totalGUI.add(progressBar, "span, grow");
        totalGUI.add(loadBtn, "span, grow, split");
        totalGUI.add(zipBtn, "span, grow, split");
        totalGUI.add(cancelBtn, "span, grow, split");

        totalGUI.setOpaque(true);
        logger.exiting(CLASS_NAME, "createContentPane");
        return totalGUI;
    }

    // For the Action Events attached to Button elements.
    public void actionPerformed(ActionEvent e) {
        logger.entering(CLASS_NAME, "actionPerformed");
        if (e.getSource() == loadBtn) {
            logger.log(Level.INFO, "Load Button Pressed");
            if (fileChooser.showOpenDialog(loadBtn) == JFileChooser.APPROVE_OPTION) {
                logger.log(Level.INFO, "File Chooser Opened - User Selected File: " + fileChooser.getSelectedFile());
                long startTime = System.currentTimeMillis();
                txtArea.setText("");
                progressBar.setValue(0);
                progressBar.setString("Progress: 0%");
                Load(startTime, fileChooser.getSelectedFile());
                loadBtn.setEnabled(false);
                cancelBtn.setEnabled(true);
            } else {
                logger.log(Level.INFO, "User Cancelled File Chooser");
                txtArea.append(">> Text Area File Selection Cancelled\r\n");
            }
        } else if (e.getSource() == cancelBtn) {
            logger.log(Level.INFO, "Cancel Button Pressed");
            worker.cancel(true);
            loadBtn.setEnabled(true);
            cancelBtn.setEnabled(false);
            progressBar.setValue(0);
            progressBar.setString("Progress: 0%");
        } else if (e.getSource() == zipBtn) {
            logger.log(Level.INFO, "Zip Button Pressed");
            if (fileChooser.showOpenDialog(zipBtn) == JFileChooser.APPROVE_OPTION) {
                logger.log(Level.INFO, "File Chooser Opened - User Selected File: " + fileChooser.getSelectedFile());
                txtArea.setText("");
                Zip(System.currentTimeMillis(), fileChooser.getSelectedFile());
            } else {
                logger.log(Level.INFO, "User Cancelled File Chooser");
                txtArea.append(">> Zip File Selection Cancelled\r\n");
            }
        }
        logger.exiting(CLASS_NAME, "actionPerformed");
    }

    //Static method to return a structured time format to the GUI.
    private static String timeFormatter(long milliseconds) {
        logger.entering(CLASS_NAME, "timeFormatter");
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        milliseconds %= 1000;
        String timeFormattedString = "";
        timeFormattedString = String.format("%02d", hours);
        timeFormattedString += ":";
        timeFormattedString += String.format("%02d", minutes);
        timeFormattedString += ":";
        timeFormattedString += String.format("%02d", seconds);
        timeFormattedString += ".";
        timeFormattedString += String.format("%03d", milliseconds);
        logger.exiting(CLASS_NAME, "timeFormatter");
        logger.exiting(CLASS_NAME, "timeFormatter");
        return timeFormattedString;
    }


    // Zip functionality that makes use of the SwingWorker to zip files on a separate thread.
    private void Zip(long time, File fileName) {
        logger.entering(CLASS_NAME, "Zip");
        File zipFileName = new File(fileName.toString().substring(0, fileName.toString().lastIndexOf(".") + 1) + "zip");
        String fileNameStr = fileName.getName();
        String zipNameStr = zipFileName.getName();

        workerZip = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                logger.entering(CLASS_NAME, "Zip->SwingWorker->doInBackground");
                //try with resources for the IO streams for the zip functionality.
                try (
                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
                        BufferedReader br = new BufferedReader(new FileReader(fileName))
                ) {
                    ZipEntry e = new ZipEntry(zipNameStr);
                    out.putNextEntry(e);

                    //Create a zip entry to place the compressed data into.
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\r\n");
                    }

                    //Write data into the zip entry.
                    byte[] data = sb.toString().getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                    out.close();

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Execption: " + e.getMessage());
                }
                logger.exiting(CLASS_NAME, "Zip->SwingWorker->doInBackground");
                return 1;
            }

            @Override
            protected void done() {
                logger.entering(CLASS_NAME, "Zip->SwingWorker->done");
                txtArea.setText("Zip Complete");
                Path file = Paths.get(fileName.toString());
                Path zipFile = Paths.get(zipFileName.toString());

                BasicFileAttributes fileAttr = null;
                BasicFileAttributes zipFileAttr = null;
                try {
                    fileAttr = Files.readAttributes(file, BasicFileAttributes.class);
                    zipFileAttr = Files.readAttributes(zipFile, BasicFileAttributes.class);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Execption: " + e.getMessage());
                }

                //Write to the JTextArea the time of zip, the compression ratio and the size of both files along with the space saved.
                StringBuilder sb = new StringBuilder();
                sb.append("########################################################\r\n");
                sb.append("Zip Information\r\n");
                sb.append("########################################################\r\n");
                sb.append("Original File Name: " + fileNameStr + "\r\n");
                sb.append("Zip File Name: " + zipNameStr + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("Creation Time for " + zipNameStr + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + zipFileAttr.lastModifiedTime() + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(fileNameStr + " Size\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + fileAttr.size() + " B\r\n");
                sb.append(">\t" + (float) fileAttr.size() / (1024 * 1024) + " MB\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(zipNameStr + " Size\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + zipFileAttr.size() + " B\r\n");
                sb.append(">\t" + (float) zipFileAttr.size() / (1024 * 1024) + " MB\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("Compression Ratio\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + (float) zipFileAttr.size() / (float) fileAttr.size() + "\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append("File Size Reduction\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                sb.append(">\t" + (100 - ((float) zipFileAttr.size() / (float) fileAttr.size()) * 100) + "%\r\n");
                sb.append("--------------------------------------------------------------------------------------------------\r\n");
                txtArea.setText(sb.toString());
                long timeElapsed = System.currentTimeMillis() - time;
                timeStamp.setText("File Zip Time: " + GUI.timeFormatter(timeElapsed));
                logger.log(Level.INFO, "File Zip Time: " + GUI.timeFormatter(timeElapsed));
                logger.exiting(CLASS_NAME, "Zip->SwingWorker->done");
            }
        };
        workerZip.execute();
        logger.entering(CLASS_NAME, "Zip");
    }

    //Used to load
    private void Load(long time, File fileName) {
        logger.entering(CLASS_NAME, "Load");
        worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                logger.entering(CLASS_NAME, "Load->SwingWorker->doInBackground");
                LineNumberReader lnr = new LineNumberReader(new FileReader(fileName));
                lnr.skip(Long.MAX_VALUE);
                float lineCount = lnr.getLineNumber() + 1;
                // Finally, the LineNumberReader object should be closed to prevent resource leak
                lnr.close();


                try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    while ((line = br.readLine()) != null) {
                        count++;
                        sb.append(count + "- " + line + "\r\n");
                        setProgress(Math.round((count / lineCount) * 100f));
                    }
                    publish(sb.toString());
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Execption: " + ioe.getMessage());
                }
                logger.exiting(CLASS_NAME, "Load->SwingWorker->doInBackground");
                return 1;
            }

            @Override
            protected void process(List<String> chunks) {
                logger.entering(CLASS_NAME, "Load->SwingWorker->process");
                for (String lines : chunks) {
                    txtArea.append(lines + "\r\n");
                }
                int prog = worker.getProgress();
                long timeElapsed = System.currentTimeMillis() - time;
                timeStamp.setText("File Load Time: " + GUI.timeFormatter(timeElapsed));
                logger.log(Level.INFO, "File Load Time: " + GUI.timeFormatter(timeElapsed));
                progressBar.setValue(prog);
                progressBar.setString("Progress: " + prog + "%");
                logger.exiting(CLASS_NAME, "Load->SwingWorker->process");
            }

            @Override
            protected void done() {
                logger.entering(CLASS_NAME, "Load->SwingWorker->done");
                loadBtn.setEnabled(true);
                cancelBtn.setEnabled(false);
                if (isCancelled()) {
                    txtArea.append("\n!! Cancelled !!\n");
                } else {
                    txtArea.append("\n>> Done <<\n");
                }
                logger.exiting(CLASS_NAME, "Load->SwingWorker->done");
            }
        };
        worker.execute();
        logger.exiting(CLASS_NAME, "Load");
    }
}

