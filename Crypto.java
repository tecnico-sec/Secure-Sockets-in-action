

import java.io.FileInputStream;
import java.io.IOException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {   

    public static Key readKeyOrExit(String keyPath, String type) {
		try {
			return readKey(keyPath, type);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e) {
			e.printStackTrace();
			System.exit(1);
			// For the compiler
			return null;
		}
	}

	public static Key readKey(String keyPath, String type) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] encoded;
		try (FileInputStream fis = new FileInputStream(keyPath)) {
			encoded = new byte[fis.available()];
			fis.read(encoded);
		}
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		if (type.equals("pub")) {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
			return keyFactory.generatePublic(keySpec);
		}

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		return keyFactory.generatePrivate(keySpec);
	}

    public static SecretKey generate(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static byte[] wrapKey(PublicKey pubKey, SecretKey symKey) throws InvalidKeyException, IllegalBlockSizeException {
        try {
            final Cipher cipher = Cipher
                    .getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.WRAP_MODE, pubKey);
            final byte[] wrapped = cipher.wrap(symKey);
            return wrapped;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(
                    "Java runtime does not support RSA/ECB/OAEPWithSHA1AndMGF1Padding",
                    e);
        }
    }
    
    public static Key unWrapKey(PrivateKey privKey, byte[] wrappedKey) throws InvalidKeyException {
        try {
            final Cipher cipher = Cipher
                    .getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.UNWRAP_MODE, privKey);
            Key unwrapped = cipher.unwrap(wrappedKey, "RSA/ECB/OAEPWithSHA1AndMGF1Padding", Cipher.SECRET_KEY);
            return unwrapped;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(
                    "Java runtime does not support RSA/ECB/OAEPWithSHA1AndMGF1Padding",
                    e);
        }
    }
/* 
    public static byte[] encrypt(String algorithm, String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return cipherText;
    }

    public static String decrypt(String algorithm, byte[] cipherText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, 
        InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }*/

    public static byte[] encrypt(String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(key.getEncoded(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, spec, iv);
        byte[] encryptData = cipher.doFinal(input.getBytes());
        return encryptData;
    }

    public static String decrypt(byte[] cipherText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        SecretKeySpec spec = new SecretKeySpec(key.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, spec, iv);
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }
}
