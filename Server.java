


import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.io.*;

public class Server     {

    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private String publicKeyPath = null;
    private PublicKey clientPublicKey = null;

    public Server(int port, String clientKeyPath) throws NoSuchAlgorithmException, InvalidKeyException, 
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException     {
        try     {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            socket = server.accept();
            System.out.println("Client accepted");
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            publicKeyPath = clientKeyPath;
            String line = "";

            //Getting pubblic Key of the client
            clientPublicKey = (PublicKey) Crypto.readKeyOrExit(publicKeyPath, "pub");

            //Generating the secret Key for connection
            SecretKey secretKey = Crypto.generate(256);

            System.out.println("Created secret key");

            //secretKey wrapped with client pub key
            byte[] wrappedKey = Crypto.wrapKey(clientPublicKey, secretKey);

            //Sending the wrapped Secret Key
            out.writeInt(wrappedKey.length);
            out.write(wrappedKey);

            String wrappedString = new String(wrappedKey);

            System.out.println("Sent wrapped key: " + wrappedString);


            // reads message from client until "Exit" is sent
            while (!line.equals("Exit"))    { //Not sure Se continua assim
                try     {
                    int encryptedSize = in.readInt();
                    byte[] encryptedKey = in.readNBytes(encryptedSize);
                    String encryptedString = new String(encryptedKey);
                    System.out.println("Received encrypted message: " + encryptedString);
                    line = Crypto.decrypt(encryptedKey, secretKey);
                    System.out.println(line);
                }
                catch(IOException i)    {
                    System.out.println(i);
                    return;
                }
            }
            System.out.println("Closing connection");
            // close connection
            socket.close();
            in.close();
            out.close();
        }
        catch(IOException i)    {
            System.out.println(i);
        }
    }
    public static void main(String args[]) throws InvalidKeyException, NoSuchAlgorithmException, 
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException{
        new Server(5000, "public_key.der");
    }
}