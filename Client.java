

import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.io.*;
import java.util.Scanner;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Client     {

    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    Scanner scanner  = null;
    private String privateKeyPath = null;
    private PrivateKey clientPrivateKey = null;

    public Client(String address, int port, String clientKeyPath) throws IOException, InvalidKeyException, NoSuchPaddingException, 
    NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException     {
        try     {
            socket = new Socket(address, port);
            System.out.println("Connected\n");
            System.out.println("Type: 'Exit' to close the connection\n");
            // takes input from terminal
            scanner = new Scanner(System.in);
            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            privateKeyPath = clientKeyPath;

        }
        catch(UnknownHostException u)   {
            System.out.println(u);
        }
        catch(IOException i)    {
            System.out.println(i);
        }

        //Getting Client private Key from the path
        clientPrivateKey = (PrivateKey) Crypto.readKeyOrExit(privateKeyPath, "priv");
        
        //Reading the wrapped SecretKey and unwrapping it
        int keySize = in.readInt();
        byte []wrappedKey = in.readNBytes(keySize);


        SecretKey secretKey = (SecretKey) Crypto.unWrapKey(clientPrivateKey, wrappedKey);


        byte[] byte_simkey = secretKey.getEncoded();
        String str_simkey = Base64.getEncoder().encodeToString(byte_simkey);

        byte[] byte_prikey = clientPrivateKey.getEncoded();
        String str_prikey = Base64.getEncoder().encodeToString(byte_prikey);

        System.out.println("Decoding secret key: " + str_simkey + "\n");
        System.out.println("Using client private key: " + str_prikey + "\n");


        String line = "";
        byte[] encryptedLine = null;

        // keep reading until "Exit" is input
        while (!line.equals("Exit"))    {
            try     {
                line = scanner.nextLine();
                encryptedLine = Crypto.encrypt(line, secretKey);
                out.writeInt(encryptedLine.length);
                out.write(encryptedLine);
                System.out.println("Sent encrypted message: " + Base64.getEncoder().encodeToString(encryptedLine) + "\n");
            }
            catch(IOException i)    {
                System.out.println(i);
                return;
            }
        }
        // close the connection
        try     {
            out.close();
            in.close();
            socket.close();
        }
        catch(IOException i)    {
            System.out.println(i);
        }
    }
    public static void main(String args[]) throws InvalidKeyException, IOException, NoSuchPaddingException, 
    NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        new Client("127.0.0.1", 5000, "private_key.der");
    }
}