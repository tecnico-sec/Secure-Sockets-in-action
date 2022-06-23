import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class User {

    public static void startClient(String host, int port) throws IOException {

        SocketFactory factory = SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            
            socket.setEnabledCipherSuites(new String[] { "TLS_AES_128_GCM_SHA256" });
            socket.setEnabledProtocols(new String[] { "TLSv1.3" });
            
            String message = "This is a secure channel!";
            System.out.println("sending message: " + message);
            OutputStream os = new BufferedOutputStream(socket.getOutputStream());
            os.write(message.getBytes());
            os.flush();
            
            InputStream is = new BufferedInputStream(socket.getInputStream());
            byte[] data = new byte[2048];
            int len = is.read(data);
            System.out.printf("client received %d bytes: %s%n", len, new String(data, 0, len));
        }
    }

    public static void main(String args[]) throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "client.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        if (args.length == 1 ) {
            int port = Integer.parseInt(args[0]);
            startClient("localhost", port);
        }
        else return;
    }
}