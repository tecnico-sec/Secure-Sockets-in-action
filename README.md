Instituto Superior Técnico, Universidade de Lisboa

**Network and Computer Security**

# Lab guide: Secure Socket connection

## Goals

- Create a server and user key
- Use the keys to create certificates
- Use those to create a secure channel

## Introduction

This laboratory assignment uses Java Development Kit (JDK) version 7 or later, running on Linux.
The Java platform strongly emphasizes security, including language safety, cryptography, public key infrastructure, secure communication, authentication and access control.
In this laboratory we use [OpenSSL](https://www.openssl.org/) to create keys and the certificate, and then use the Java implementationof TLS/SSL to create a secure channel for communication.

## Setup
To try to make the connection work, you will need a few steps:

Clone the repository into your workspace:

```bash
git clone https://github.com/tecnico-sec/Secure-Sockets-in-action.git
```

or via SSH:

```bash
git clone git@github.com:tecnico-sec/Secure-Sockets-in-action.git
```

Go to that directory and compile the code directly using the Java compiler:

```bash
javac *.java
```

## Asymmetric ciphers

The goal now is to use asymmetric ciphers, with separate private and public keys. RSA is going to be the algorithm that we will use, it is also the most well known algorithm to generate key pairs.

#### Generating a pair of keys with OpenSSL

Generate the server key pair:

```bash
openssl genrsa -out server.key
```

Now create the keys for the user:

```bash
openssl genrsa -out user.key
```

#### Generating a self-signed certificate

Create a Certificate Signing Request, using same key (click "enter" to everything that is asked, except when asked for "A challenge passoword[]" use the word "password"):

```bash
openssl req -new -key server.key -out server.csr
```

```bash
openssl req -new -key user.key -out user.csr
```

The server will self-sign because there's no CA(Certificate Authority) yet.
Self-sign:

```bash
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

For our certificate to be able to sign other certificates, OpenSSL requires that a database exists (a .srl file). Create it:

```bash
echo 01 > server.srl
```

The user cannot self-sign itself because there is now a CA (the server) that will sign the user.
So it's replaced by:

```bash
openssl x509 -req -days 365 -in user.csr -CA server.crt -CAkey server.key -out user.crt
```

#### Convert certificate

For Java to be able to read the certificates that were created is necessary to convert them to the right format.

```bash
openssl x509 -in server.crt -out server.pem
```

```bash
openssl x509 -in user.crt -out user.pem
```

#### Create a p12 file

With the certificates and the keys we created, we can use them to create a p12 file (password should be "password").
A p12 file contains a digital certificate that uses PKCS#12 (Public Key Cryptography Standard #12) encryption.

```bash
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12
```

```bash
openssl pkcs12 -export -in user.crt -inkey user.key -out user.p12
```

#### Import the certificate

Now you will add to the jks file of the server the certificate of the user so he trusts that user.
jks file is the default keystore type in the Sun/Oracle Java security provider. And in this case it will store the certificates the server trusts.

```bash
keytool -import -trustcacerts -file user.pem -keypass password -storepass password -keystore servertruststore.jks
```

## Connecting the user to the server

Now that we have compiled and created a certificate for both user, we can try make a secure socket connection with the server.
To do this we first start the server:

```bash
java -cp . Server
```

The `cp` option specifies the *classpath*, i.e., where to look for the Java class files.
In this case, the current directory is specified as classpath.

The server is now waiting for a user to connect, to do this we start the user (on a new terminal but in the same folder):

```bash
java -cp . User 5001
```

The connection should fail because the port that is being used is wrong, what should appear if you were trying to open a website is [this](https://wrong.host.badssl.com/).

Now try using the correct port (you will need to restart the server):

```bash
java -cp . User 5000
```

The connection should fail again, what happened this time was that the certificate of the server wasn't sent to the user. If you were trying to open a website what should appear is [this](https://untrusted-root.badssl.com/).

So now try adding the server certificate to the user jks so he can trust the server:

```bash
keytool -import -trustcacerts -file server.pem -keypass password -storepass password -keystore usertruststore.jks
```

Try connecting both of them with each other using this commands.

Server:

```bash
java -cp . Server
```

User:

```bash
java -cp . User 5000
```

The connection should work and messages can now be sent.
What you should see when trying to open a website is [this](https://https-everywhere.badssl.com/).


**Acknowledgments**

Revisions: Diogo Peres Castilho, David R. Matos, Miguel Pardal, Ricardo Chaves

Second Revisions: Diogo Fernandes, Guilherme Santos, Pedro Ferreira, Lucas Figueiredo, João Pereira

----

[SIRS Faculty](mailto:meic-sirs@disciplinas.tecnico.ulisboa.pt)
