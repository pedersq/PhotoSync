import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.UIManager;
import javax.swing.JTextArea;
import java.util.ArrayList;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.*;
import javax.swing.*;

public class ServerGUI extends JFrame {

  private Server server;
  private String password;
  private JButton connectButton;
  private JButton sendButton;
  private JButton closeButton;
  private JButton categoryButton;
  private JButton nameButton;
  private JTextArea log;
  private JMenuItem sendDirItem;
  private JMenu downloadMenu;
  private int port;
  private String userName = null;
  private String category = null;
  private Thread serverThread;
  private PhotoData pd;

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}
      new ServerGUI();
  }

  private void startServer() {
    pd = new PhotoData();
    serverThread = new Thread(new Runnable() {
      public void run() {
        try {
          server = new Server(port, password, log, pd);
        } catch (IOException e) {
          e.printStackTrace();
          log.append("Couldn't start server on port: " + port + "\n");
        }
      }
    });
    serverThread.start();
  }

  private void stopServer() {
    serverThread.stop();
  }

  public ServerGUI() {
    this.setSize(new Dimension(500, 480));
    this.setTitle("Quinn's File Uploader");
    JPanel framePanel = new JPanel(new BorderLayout());

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(5, 5, 10, 10));
    buttonPanel.setLayout(new GridLayout(2, 1, 2, 2));

    JPanel textPanel = new JPanel();
    log = new JTextArea("", 18, 36);
    JScrollPane jsp = new JScrollPane(log);
    textPanel.setBorder(new EmptyBorder(1, 0, 0, 0));
    log.setEditable(false);
    textPanel.add(jsp);

    connectButton = new JButton("Start Server");
    connectButton.setMaximumSize(new Dimension(40, 40));
    connectButton.setFocusPainted(false);
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (aquireServerInfo()) {
            startServer();
            log.append("Server is online on port: " + port + "\n");
            closeButton.setEnabled(true);
        }
      }
    });
    buttonPanel.add(connectButton);

    closeButton = new JButton("Stop Server");
    closeButton.setMinimumSize(new Dimension(40, 40));
    closeButton.setMaximumSize(new Dimension(40, 40));
    closeButton.setFocusPainted(false);
    closeButton.setEnabled(false);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeButton.setEnabled(false);
        stopServer();
        log.append("Disconnected.\n");
      }
    });
    buttonPanel.add(closeButton);

    framePanel.add(buttonPanel, BorderLayout.NORTH);
    framePanel.add(textPanel, BorderLayout.CENTER);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
    this.setResizable(false);
    this.add(framePanel);
    this.setVisible(true);

  }

  private boolean aquireServerInfo() {
    JTextField portField = new JTextField(6);
    JTextField passwordField = new JTextField(15);

    JPanel portPanel = new JPanel();
    portPanel.add(new JLabel("Server Port:"));
    portPanel.add(portField);

    JPanel passwordPanel = new JPanel();
    passwordPanel.add(new JLabel("Choose a Server Password:"));
    passwordPanel.add(passwordField);

    int result = JOptionPane.showConfirmDialog(null, portPanel,
             "Please Enter Server Information", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
       try {
         port = Integer.parseInt(portField.getText());
         result = JOptionPane.showConfirmDialog(null, passwordPanel,
           "Please Enter Server Information", JOptionPane.OK_CANCEL_OPTION);
         if (result == JOptionPane.OK_OPTION) {
           password = passwordField.getText();
         }
       } catch (Exception e) {
         log.append("Port must be a number.\n");
         return false;
       }
       return true;
    }
    return false;
  }

  private void displayHelperMessage() {
    log.append("Thank's for using Quinn's File Uploader!\n");
    log.append("To send files to be archived you must first:\n");
    log.append("  - Use 'Set Name' to tell the archive who is saving files\n");
    log.append("  - Use 'Set Category' to tell the archive what to label\n");
    log.append("      the files you will be sending, make sure to update\n");
    log.append("      the category based on the photo's you are sending\n");
  }


  private String getTextFromUser(String question, String title) {
    JTextField name = new JTextField(10);

    JPanel myPanel = new JPanel();
    myPanel.add(new JLabel(question));
    myPanel.add(name);
    int result = JOptionPane.showConfirmDialog(null, myPanel,
            title, JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      return name.getText();
    } else {
      return null;
    }
  }

  private String getFileExtension(File file) {
    String name = file.getName();
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
        return ""; // empty extension
    }
    return name.substring(lastIndexOf);
  }

}