import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Hash {

  public static String fileToMD5(String path) throws IOException, NoSuchAlgorithmException {

    File file = new File(path);
    MessageDigest digest = MessageDigest.getInstance("MD5");

    FileInputStream fis = new FileInputStream(file);

    byte[] byteArray = new byte[1024];
    int bytesCount = 0;

    while ((bytesCount = fis.read(byteArray)) != -1) {
      digest.update(byteArray, 0, bytesCount);
    }

    fis.close();

    byte[] bytes = digest.digest();

    System.out.println(bytes.length);

    StringBuilder sb = new StringBuilder();
    for(int i=0; i< bytes.length ;i++) {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();


  }



}
