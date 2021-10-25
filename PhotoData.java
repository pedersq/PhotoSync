import java.util.HashSet;
import java.security.MessageDigest;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.lang.StringBuilder;

public class PhotoData {

  private HashSet<String> photos;
  private int numPhotos;

  public PhotoData() {

    File hashesFile = new File("data/hashes.ser");
    if (hashesFile.exists()) {
      try {
        FileInputStream fileIn = new FileInputStream(hashesFile);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        photos = (HashSet<String>) objIn.readObject();
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    } else {
      System.out.println("Loading existing photos (may take a while)");
      photos = new HashSet<String>();
      for (String s : getFilePaths(new File("photos"), "photos")) {
        this.addFile(s);
      }
    }

    numPhotos = photos.size();
    System.out.println("Loaded " + numPhotos + " photos.");
  }

  public void serialize() {
    System.out.print("Saving hashes data: ");
    try {
      FileOutputStream fileOut = new FileOutputStream("data/hashes.ser");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(photos);
      out.close();
      fileOut.close();
      System.out.println("Done.");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Couldn't save hashes file!");
    }
  }

  public int getNumPhotos() {
    return numPhotos;
  }

  public boolean hasFile(String hash) {
    return photos.contains(hash);
  }

  public boolean addFile(String path) {

    String hash = getHash(path);
    if (!photos.contains(hash)) {
      photos.add(hash);
      return true;
    }
    return false;

  }

  // For adding all files to a zip
  public static ArrayList<String> getFilePaths(File startDirectory, String prefix) {
    ArrayList<String> paths = new ArrayList<String>();
    File[] filesInDir = startDirectory.listFiles();

    for (File f : filesInDir) {
      if (f.isDirectory()) {
        ArrayList<String> deeperPaths = getFilePaths(f, prefix + "/" + f.getName());
        for (String s : deeperPaths) {
          paths.add(s);
        }
      } else {
        paths.add(prefix + "/" + f.getName());
      }
    }

    return paths;
  }

  public static String getHash(String path) {

    try {
      File file = new File(path);
      FileInputStream fis = new FileInputStream(file);
      MessageDigest md5 = MessageDigest.getInstance("MD5");

      byte[] bytes = new byte[1024];
      int count = 0;

      while ((count = fis.read(bytes)) != -1) {
        md5.update(bytes, 0, count);
      }
      fis.close();

      bytes = md5.digest();
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < bytes.length; i++)
      {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
      }
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    return null;

  }

}
