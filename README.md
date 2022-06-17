Instituto Superior Técnico, Universidade de Lisboa

**Network and Computer Security**

# Lab guide: Secure Socket connection

## Goals

- Create a public and private key
- Use the keys created to make a secure connection between a server and a client

## Introduction

This laboratory assignment uses Java Development Kit (JDK) version 7 or later, running on Linux. 
The Java platform strongly emphasizes security, including language safety, cryptography, public key infrastructure, secure communication, authentication and access control.
In this laboratory we use [OpenSSL](https://www.openssl.org/) to create keys. And use those same keys to create a secure connection.

MP - no, the goal was to create the keys, then the certificate, and then use the Java implementation of TLS/SSL or HTTPS.  
See `javax.net.ssl.SSLSocketFactory`

MP - the current solution uses the same constant IV, which is wrong.

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

Compile the code directly using the Java compiler:

```bash
javac *.java
```

## Asymmetric ciphers

The goal now is to use asymmetric ciphers, with separate private and public keys. RSA is the most well known of these algorithms.

#### Generating a pair of keys with OpenSSL

Generate the key pair:

```bash
openssl genrsa -out server.key
```

Save the public key:

```bash
openssl rsa -in server.key -pubout > public.key
```

#### Reading the generated pair of keys with Java

To read the generated keys in Java it is necessary to convert them to the right format.

Convert server.key to .pem

```bash
openssl rsa -in server.key -text > private_key.pem
```

Convert private Key to PKCS#8 format (so Java can read it)

```bash
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
```

Output public key portion in DER format (so Java can read it)

```bash
openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
```

## Connecting the client to the server

Now that we have compiled and created a public and a private key, we can make a secure socket connection with the server.
To do this we first start the server:

```bash
java -cp . Server
```

The `cp` option specifies the *classpath*, i.e., where to look for the Java class files.
In this case, the current directory is specified as classpath.

MP - server starts without keys and without errors for this case

The server is now waiting for a client to connect to do this we start the client (on a new terminal but in the same folder):

```bash
java -cp . Client
```

The connection should start, you can type in the client and will appear on the server the same message.
Although the connection seems normal, it is being encrypted with a secret key shared only by those 2.
This secret key is shared to the client by the server, with him wrapping the secret key with the public key generated above, and sending this wrapped key to the client.
The client receiving it, unwraps it and is able to save the secret key for the connection.
Now every mesage sent by the client to the server is encrypted with this secret key.

To stop the connection, type Exit on the client side and both will stop.

MP - if the client and server already share a secret key, why do they need the public keys?
Usually, in asymmetric crypto, the public keys are known and used to exchange a generated secret key.
This is what SSL\TLS does.

**Acknowledgments**

Revisions: Diogo Peres Castilho, David R. Matos, Miguel Pardal, Ricardo Chaves

Second Revisions: Diogo Fernandes, Guilherme Santos, Pedro Ferreira, Lucas Figueiredo, João Pereira

----

[SIRS Faculty](mailto:meic-sirs@disciplinas.tecnico.ulisboa.pt)
