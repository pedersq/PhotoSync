import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import javax.net.ssl.SSLSocket;
import java.security.MessageDigest;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileNotFoundException;

public class Server {

  private int port;
  private String password;

  private int numPhotos;

  private PhotoData pd;

  private InputStream inputStream;
  private OutputStream outputStream;
  private DataInputStream in;
  private DataOutputStream out;

  public static void main(String[] args) throws IOException {

    new Server();

  }

  public Server() throws IOException {
    // Sets up the server
    ServerSocket serverSocket = null;
    Socket client = null;
    loadServerProperties();
    numPhotos = getPhotoNumFromLog();
    try {
      serverSocket = new ServerSocket(port);
      log("Sucessfully setup server on port: " + port);
    } catch (Exception e) {
      log("Could not setup server on port: " + port);
      System.exit(-1);
    }

    // Accepts clients
    while (true) {
      log("\nWaiting for connection:");
      try {
        client = serverSocket.accept();

        log("Connection found");
        log("Connected to: " + client.getInetAddress() + ":" + port);

        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();
        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        if (login()) {

          boolean shouldClose = false;
          while (!shouldClose) {
            int i = in.readInt();
            switch (i) {
              case 1:
                recieveFile();
                break;
              case 2:
                sendAllFilesAsZip();
              default:
                shouldClose = true;
                log("Closing connection");
                break;
            }
          }

        }

        client.close();

      } catch (IOException e) {
        log("Client closed connection.");
      }

    }
  }

  private void log(String s) {
    try {
      String filename= "Log.txt";
      FileWriter fw = new FileWriter(filename, true); //the true will append the new data
      fw.write(s + "\n");//appends the string to the file
      fw.close();
    } catch(IOException ioe) {
      System.err.println("IOException: " + ioe.getMessage());
    }
  }

  private boolean login() {
    try {
      String attemptedPassword = recieveString();
      if (attemptedPassword.equals(password)) {
        log("Successfully logged in.");
        out.writeInt(1);
        return true;
      } else {
        log("User submitted incorrect password.");
        out.writeInt(-1);
        return false;
      }
    } catch (Exception e) {
      log("Login failed");
      e.printStackTrace();
    }
    return false;

  }

  private void recieveFile() {

    try {
      String hash = recieveString();
      if (pd.hasFile(hash)) {
        out.writeInt(0);
        return;
      } else {
        out.writeInt(1);
      }

      String user = recieveString();
      log("Recieving file from: " + user);
      new File("photos/"+user).mkdir();

      numPhotos = numPhotos + 1;
      String filename = String.valueOf(numPhotos);
      updatePhotoNumLog();
      String category = recieveString();

      String fileExtension = recieveString();
      new File("photos/"+user+"/"+category).mkdir();

      String newFilePath = "photos/" + user + "/" + category + "/" + filename + fileExtension;
      File fileObject = new File(newFilePath);
      FileOutputStream newFile = new FileOutputStream(fileObject);

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

  private void sendAllFilesAsZip() {
    try {
      File f = new File("photos.zip");
      ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(f));

      ArrayList<String> paths = pd.getFilePaths(new File("photos"), "photos");
      for (String path : paths) {
        ZipEntry e = new ZipEntry(path);
        outZip.putNextEntry(e);
        FileInputStream tempFileStream = new FileInputStream(new File(path));
        byte[] data = new byte[1024];
        int count;
        while ((count = tempFileStream.read(data)) > 0) {
          outZip.write(data, 0, count);
        }
        tempFileStream.close();
        outZip.closeEntry();
      }
      outZip.close();

      sendFile("photos.zip");

    } catch (Exception e) {

    }
  }

  public void sendFile(String path) throws IOException {

    File f = new File(path);
    FileInputStream fis = new FileInputStream(f);

    long fileSize = f.length();
    out.writeLong(fileSize);

    byte[] data = new byte[4096];
    int count = fis.read(data, 0, data.length);
    while (count != -1) {
      out.write(data, 0, count);
      count = fis.read(data, 0, data.length);
    }

  }

  private String recieveString() throws IOException {
    byte[] bytes = new byte[in.readInt()];
    in.read(bytes, 0, bytes.length);
    return new String(bytes, "UTF-8");
  }

  private int getPhotoNumFromLog() {
    try {
      Scanner s = new Scanner(new File("PhotoData.data"));
      return s.nextInt();
    } catch (FileNotFoundException e) {
      File newLog = new File("PhotoData.data");
    }
    return 0;
  }

  private void updatePhotoNumLog() {
    try {
      FileWriter fw = new FileWriter("PhotoData.data");
      fw.write(String.valueOf(numPhotos));
      fw.close();
    } catch (Exception e) {
      log("Could not update num photo log\n");
      System.exit(-1);
    }
  }

  private void loadServerProperties() {
    File prop = new File("server.properties");
    pd = new PhotoData();
    if (prop.exists() && !prop.isDirectory()) {
      try {
        Scanner s = new Scanner(prop);
        String portText = s.nextLine();
        String passwordText = s.nextLine();
        port = Integer.parseInt(portText.split(":")[1]);
        password = passwordText.split(":")[1];
        s.close();
      } catch (Exception e) {
        System.out.println("Couldn't read from server.properties, exiting.");
        System.exit(-1);
      }
    } else {
      try {
        FileWriter fw = new FileWriter(prop);
        fw.write("port:4445\n", 0, 10);
        fw.write("password:TempPassword");
        fw.close();
        System.out.println("No properties file was found: one has been created.");
        System.out.println("It has been filled with temporary values.");
      } catch (IOException e) {
        System.out.println("No properties file was found, and one couldn't be created. Exiting.");
        System.exit(-1);
      }
    }
  }


}
