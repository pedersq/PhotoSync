import java.net.*;
import java.io.*;
import javax.swing.JTextArea;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.*;
import javax.swing.*;

public class Client {


  private Socket server = null;
  private int port;
  private String serverIP = null;
  private InputStream inputStream;
  private OutputStream outputStream;
  private DataInputStream in;
  private DataOutputStream out;

  private JTextArea log = null;

  public Client() {

    try {
      aquireServerInfo();
      Socket server = new Socket(serverIP, port);

      inputStream = server.getInputStream();
      outputStream = server.getOutputStream();
      in = new DataInputStream(inputStream);
      out = new DataOutputStream(outputStream);

    } catch (Exception e) {
      log("Failed to connect to server\n");
      e.printStackTrace();
      JOptionPane.showConfirmDialog(null, "No server found at the location provided.",
                   "", JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE);
      System.exit(-1);
    }

  }

  private void aquireServerInfo() {
    JTextField IPField = new JTextField(20);
    JTextField portField = new JTextField(6);

    JPanel ipPanel = new JPanel();
    ipPanel.add(new JLabel("Server IP Address:"));
    ipPanel.add(IPField);
    JPanel portPanel = new JPanel();
    portPanel.add(new JLabel("Server Port:"));
    portPanel.add(portField);

    int result = JOptionPane.showConfirmDialog(null, ipPanel,
             "Please Enter Server Information", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
       serverIP = IPField.getText();
       result = JOptionPane.showConfirmDialog(null, portPanel,
                "Please Enter Server Information", JOptionPane.OK_CANCEL_OPTION);
       if (result == JOptionPane.OK_OPTION) {
         port = Integer.parseInt(portField.getText());
       }
    }

  }

  public void assignLogger(JTextArea area) {
    log = area;
  }

  private void log(String s) {
    if (log != null) {
      log.append(s);
    }
  }

  public void closeConnection() {
    try {
      out.writeInt(0);
      server.close();
    } catch (Exception ignored) {}
  }

  public void sendFile(String userID, String category, String extension, String path) throws IOException {
    out.writeInt(1); // Tells the server a file is coming
    sendString(userID);
    sendString(category);
    sendString(extension);

    File f = new File(path);
    FileInputStream fis = new FileInputStream(f);

    long fileSize = f.length();
    out.writeLong(fileSize);
    System.out.println(fileSize);

    byte[] data = new byte[4096];
    int count = fis.read(data, 0, data.length);
    while (count != -1) {
      out.write(data, 0, count);
      count = fis.read(data, 0, data.length);
    }

  }

  public boolean login(String password) {
    try {

      sendString(password);

      if (in.readInt() == 1) {
        log("Login was successful\n");
        return true;
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
    log("Login failed.\n");
    return false;
  }

  public void getAllFilesAsZip() {
    try {
      out.writeInt(2); // Asks for all files as zip;
      recieveFile("archive.zip");
    } catch (IOException e) {

    }
  }

  private void sendString(String s) throws IOException {
    byte[] bytes = new String(s).getBytes();
    out.writeInt(bytes.length);
    out.write(bytes, 0, bytes.length);
  }

  private void recieveFile(String path) {

    try {
      FileOutputStream newFile = new FileOutputStream(new File(path));

      long fileSize = in.readLong();
      byte[] buff = new byte[4096];
      long bytesRead = 0;
      while (fileSize > 0) {

        if (fileSize > buff.length) {
          int c = in.read(buff, 0, buff.length);
          newFile.write(buff, 0, c);
          fileSize = fileSize - c;
          bytesRead += c;
        } else {
          int c = in.read(buff, 0, (int) fileSize);
          newFile.write(buff, 0, c);
          fileSize = fileSize - c;
          bytesRead += c;
        }
      }

      newFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
