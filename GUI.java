import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.UIManager;
import javax.swing.JTextArea;
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



public class GUI extends JFrame {

  private Client client;
  private JButton connectButton;
  private JButton sendButton;
  private JButton closeButton;
  private JButton categoryButton;
  private JButton nameButton;
  private JTextArea log;
  private JMenu downloadMenu;

  private String userName = null;
  private String category = null;

  public static void main(String[] args) {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    new GUI();

  }

  public GUI() {
    this.setSize(new Dimension(610, 400));
    this.setTitle("Quinn's File Uploader");
    JPanel framePanel = new JPanel(new BorderLayout());

    JMenuBar menuBar = new JMenuBar();
    downloadMenu = new JMenu("Download");

    JMenuItem i1 = new JMenuItem("Download Photo Archive");
    i1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        log.append("Beginning download of archive: this may take a minute.\n");

        client.getAllFilesAsZip();
      }
    });
    downloadMenu.add(i1);
    downloadMenu.setEnabled(false);
    menuBar.add(downloadMenu);
    this.setJMenuBar(menuBar);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(5, 5, 10, 10));
    buttonPanel.setLayout(new GridLayout(5, 1, 5, 5));

    JPanel textPanel = new JPanel();
    log = new JTextArea("", 18, 36);
    JScrollPane jsp = new JScrollPane(log);
    textPanel.setBorder(new EmptyBorder(1, 0, 0, 0));
    log.setEditable(false);
    textPanel.add(jsp);


    connectButton = new JButton("Connect");
    connectButton.setMaximumSize(new Dimension(40, 40));
    connectButton.setFocusPainted(false);
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        connect();
      }
    });
    buttonPanel.add(connectButton);

    nameButton = new JButton("Set Name");
    nameButton.setMinimumSize(new Dimension(40, 40));
    nameButton.setMaximumSize(new Dimension(40, 40));
    nameButton.setFocusPainted(false);
    nameButton.setEnabled(false);
    nameButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String temp = getTextFromUser("Who is sending?", "Acquiring Name");
        if (temp != null) {
          userName = temp;
          log.append("Set name to: " + userName + "\n");
          if (category != null && userName != null) {
            sendButton.setEnabled(true);
          }
        }
      }
    });
    buttonPanel.add(nameButton);

    categoryButton = new JButton("Set Category");
    categoryButton.setMinimumSize(new Dimension(40, 40));
    categoryButton.setMaximumSize(new Dimension(40, 40));
    categoryButton.setFocusPainted(false);
    categoryButton.setEnabled(false);
    categoryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String temp = getTextFromUser("Category of photo", "What folder to store photos in?");
        if (temp != null) {
          category = temp;
          log.append("Set category to: " + category + "\n");
          if (category != null && userName != null) {
            sendButton.setEnabled(true);
          }
        }
      }
    });
    buttonPanel.add(categoryButton);

    sendButton = new JButton("Send File(s)");
    sendButton.setMinimumSize(new Dimension(40, 40));
    sendButton.setMaximumSize(new Dimension(40, 40));
    sendButton.setFocusPainted(false);
    sendButton.setEnabled(false);
    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendFile();
      }
    });
    buttonPanel.add(sendButton);

    closeButton = new JButton("Disconnect");
    closeButton.setMinimumSize(new Dimension(40, 40));
    closeButton.setMaximumSize(new Dimension(40, 40));
    closeButton.setFocusPainted(false);
    closeButton.setEnabled(false);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        client.closeConnection();
        closeButton.setEnabled(false);
        sendButton.setEnabled(false);
        nameButton.setEnabled(false);
        categoryButton.setEnabled(false);
        downloadMenu.setEnabled(false);
        log.append("Disconnected.\n");
      }
    });
    buttonPanel.add(closeButton);

    framePanel.add(buttonPanel, BorderLayout.WEST);
    framePanel.add(textPanel, BorderLayout.CENTER);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
    this.setResizable(false);
    this.add(framePanel);
    this.setVisible(true);

  }

  private void displayHelperMessage() {
    log.append("Thank's for using Quinn's File Uploader!\n");
    log.append("To send files to be archived you must first:\n");
    log.append("  - Use 'Set Name' to tell the archive who is saving files\n");
    log.append("  - Use 'Set Category' to tell the archive what to label\n");
    log.append("      the files you will be sending, make sure to update\n");
    log.append("      the category based on the photo's you are sending\n");
  }

  private void connect() {
    JPasswordField password = new JPasswordField(10);

    JPanel myPanel = new JPanel();
    myPanel.add(new JLabel("password:"));
    myPanel.add(password);
    int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
       client = new Client();
       client.assignLogger(log);
       if (client.login(password.getText())) {
         sendButton.setEnabled(false);
         closeButton.setEnabled(true);
         nameButton.setEnabled(true);
         categoryButton.setEnabled(true);
         downloadMenu.setEnabled(true);
         displayHelperMessage();
       }
    }
  }

  private void sendFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(true);
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
       "Media Images", "jpg", "gif", "mp4", "mov", "png");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(new JFrame());
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      log.append("Beginning to send:\n");
      File[] files = chooser.getSelectedFiles();
      for (File f : files) {
        log.append("Sending the file: " +
             f.getName() + "\n");
        try {
          client.sendFile(userName,
                          category,
                          getFileExtension(f),
                          f.getAbsoluteFile().getPath());
        } catch (IOException e) {
          log.append("Failed to send file");
          e.printStackTrace();
          client.closeConnection();
          sendButton.setEnabled(false);
          closeButton.setEnabled(false);
          break;
        }
      }
      log.append("Sent Successfully");
    }

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
