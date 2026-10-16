import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import android.util.Base64;

public class Main {

    private static final String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIj0O11NWyCHIuMjuZK+NPqbbOQQMnO3uxP8kVbpWL1jtsWusPYLTUvv1ZoO4jekazCJ9ulGdVqYA8wzhXaWPqsCAwEAAQ==";
    private static final String privateKeyStr = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAiPQ7XU1bIIci4yO5kr40+pts5BAyc7e7E/yRVulYvWO2xa6w9gtNS+/Vmg7iN6RrMIn26UZ1WpgDzDOFdpY+qwIDAQABAkBKcS7/hTe+U5Y0DswlJnyXp/olcWRrH/hxYb0Cl+4jawlriiSaGRTWzdmgztgMccsh4jH3ZsTkQnOU4RZKkBgxAiEA7rk1VxZxtLHoglEReFVnCTK2Re52DSGLvrlwFh/VGWkCIQCS3Y6vtn8JJfE3in9YHBsFRnqPVDFe5Q0axZ7BJNIg8wIgPRjPqwNGhK2HyVqAMyEAZt1AwzMdqYytXFnPcwogmwkCIAYHNg4cTYaQmiAPfT6emLVUuT8c05HzkOAicGu5GS7lAiAPjPTiL3pK5zSZsc/N03zZtqdkkyhOsgE/D7T8c08WUg==";

    public static void main(String[] args) {
        try {
            String plainText = "05ac4a,1769005599755,4102502399000";  
            System.out.println("加密成功: " + encrypt(plainText, publicKeyStr));
            System.out.println("解密成功: " + decrypt(encrypt(plainText, publicKeyStr), privateKeyStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decrypt(String encryptedText, String privateKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
        return new String(cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT)), "UTF-8");
    }

    public static String encrypt(String plainText, String publicKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
        return Base64.encodeToString(cipher.doFinal(padData(plainText.getBytes("UTF-8"), getPublicKey(publicKeyStr).getModulus().bitLength() / 8)), Base64.DEFAULT);
    }

    private static RSAPublicKey getPublicKey(String key) throws Exception {
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)));
    }

    private static RSAPrivateKey getPrivateKey(String key) throws Exception {
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)));
    }

    private static byte[] padData(byte[] data, int length) {
        byte[] paddedData = new byte[length];
        System.arraycopy(data, 0, paddedData, length - data.length, data.length);
        return paddedData;
    }
}
