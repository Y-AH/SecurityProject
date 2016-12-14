package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by youse on 12/14/2016.
 */
public class CryptoUtils
{

    public static String Encrypt(String data, String passCode)
    {
        String result = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] password = digest.digest(passCode.getBytes());
            password = Arrays.copyOf(password, 16);
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(password, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(data.getBytes());
            result = Base64.getEncoder().encodeToString(cipherText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }


    public static String Decrypt(String secret, String passCode)
    {
        String result = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] password = digest.digest(passCode.getBytes());
            password = Arrays.copyOf(password, 16);
            byte[] data = Base64.getDecoder().decode(secret);
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(password, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedText = cipher.doFinal(data);
            result = new String(decryptedText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
