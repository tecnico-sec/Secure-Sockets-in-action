Instituto Superior Técnico, Universidade de Lisboa

**Network and Computer Security**

# Lab guide: Secure Sockets in action

This lab guide focuses on using an existing and robust secure sockets implementation.
The protocol was originally called SSL, standing for Secure Sockets Layer.
In most recent versions of the prototol were renamed TLS, Transport Layer Security.

In this case guide, the SSL/TLS implementation used is the one available in the Java platform itself.
The guide covers key pair generation with OpenSSL, self-signed certificate creation, and provides example code for a client and a server using TLS/SSL.

The goals of the laboratory are:

- Create a server and user key;
- Use the keys to create certificates;
- Use those to establish a secure channel;
- Exchange data using the established channel.

## Setup

This laboratory assignment uses Java Development Kit (JDK) version 7 or later, running on Linux.
The Java platform strongly emphasizes security, including language safety, cryptography, public key infrastructure, secure communication, authentication and access control.
In this laboratory we use [OpenSSL](https://www.openssl.org/) to create keys and the certificate, and then use the Java implementation of TLS/SSL to create a secure channel for communication.

To try to make the connection work, you will need to compile the source code.
Since we only require the Java platform, we use the java compiler directly:

```sh
javac *.java
```

## Asymmetric keys

The current task involves generating key pairs for asymmetric encryption, requiring separate private and public keys. This process will employ the RSA algorithm, a standard method in cryptographic systems for creating such key pairs.

### Generating a pair of keys with OpenSSL

Generate the server key pair:

```sh
openssl genrsa -out server.key
```

Now create the keys for the user:

```sh
openssl genrsa -out user.key
```

### Generating a self-signed certificate

Create a Certificate Signing Request, using same key (click "enter" to everything that is asked, except when asked for "A challenge password[]" use "changeme"):

```sh
openssl req -new -key server.key -out server.csr
```

```sh
openssl req -new -key user.key -out user.csr
```

The server will self-sign because there is no CA(Certificate Authority) yet.
Self-sign:

```sh
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

For our certificate to be able to sign other certificates, OpenSSL requires that a database exists (a .srl file). Create it:

```sh
echo 01 > server.srl
```

The user cannot self-sign itself because there is now a CA (the server) that will sign the user.
So it's replaced by:

```sh
openssl x509 -req -days 365 -in user.csr -CA server.crt -CAkey server.key -out user.crt
```

### Convert certificate

For Java to be able to read the certificates that were created is necessary to convert them to the right format.

```sh
openssl x509 -in server.crt -out server.pem
```

```sh
openssl x509 -in user.crt -out user.pem
```

### Create a p12 file

With the certificates and the keys we created, we can use them to create a p12 file (password should be "changeme").
A p12 file contains a digital certificate that uses PKCS#12 (Public Key Cryptography Standard #12) encryption.

```sh
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12
```

```sh
openssl pkcs12 -export -in user.crt -inkey user.key -out user.p12
```

### Import the certificate

Now you will add to the jks file of the server the certificate of the user so he trusts that user.
jks file is the default keystore type in the Sun/Oracle Java security provider. And in this case it will store the certificates the server trusts.
If promted with "Trust this certificate? [no]:  yes", confirm by typing "yes".

```sh
keytool -import -trustcacerts -file user.pem -keypass changeme -storepass changeme -keystore servertruststore.jks
```

## Connecting the user to the server

Now that we have compiled and created a certificate for both user, we can try make a secure socket connection with the server.
To do this we first start the server:

```sh
java -cp . Server
```

The `cp` option specifies the *classpath*, i.e., where to look for the Java class files.
In this case, the current directory is specified as classpath.

The server is now waiting for a user to connect, to do this we start the user (on a new terminal but in the same folder):

```sh
java -cp . Client 5001
```

The connection should fail because the port that is being used is wrong, what should appear if you were trying to open a website is [this](https://wrong.host.badssl.com/).

Now try using the correct port (you will need to restart the server):

```sh
java -cp . Client 5000
```

The connection should fail again, what happened this time was that the certificate of the server wasn't sent to the user.
If you were trying to open a website what should appear is [this](https://untrusted-root.badssl.com/).

So now try adding the server certificate to the user jks so he can trust the server:

```sh
keytool -import -trustcacerts -file server.pem -keypass changeme -storepass changeme -keystore usertruststore.jks
```

Try connecting both of them with each other using this commands.

Server:

```sh
java -cp . Server
```

Client:

```sh
java -cp . Client 5000
```

The connection should work and messages can now be sent.  
What you should see when trying to open a website is [this](https://https-everywhere.badssl.com/).

----

## Conclusion

This lab guide covered secure socket communication using the Java implementation of SSL/TLS.
Participants generated RSA key pairs, created self-signed certificates, and established a communication channel.
The approach involved OpenSSL configuration and Java code compilation.

There are many other libraries that also implement the SSL/TLS protocol.
Because the protocol has a standard specification, clients and servers using different libraries should still be able to communicate correctly.

In many specific cases, applications can incorporate SSL/TLS to support a more specific protocol between its clients and servers, like a secure database access protocol, for example.
In these cases, what should be done is to refer to the documentation of the involved tools and see how SSL/TLS can be enabled.
Please consider the options carefully and make sure you perform a correct configuration of trusted keys and other important parameters.

----

[SIRS Faculty](mailto:meic-sirs@disciplinas.tecnico.ulisboa.pt)
