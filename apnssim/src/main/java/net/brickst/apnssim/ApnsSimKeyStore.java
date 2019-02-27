package net.brickst.apnssim;

import java.io.*;

/**
 * A bogus key store which provides all the required information to
 * create an example SSL connection.
 *
 * To generate a bogus key store:
 * <pre>
 * keytool  -genkey -alias securechat -keysize 2048 -validity 36500
 *          -keyalg RSA -dname "CN=securechat"
 *          -keypass secret -storepass secret
 *          -keystore cert.jks
 * </pre>
 */
 public class ApnsSimKeyStore {

    private String certFilename;
    private String ksPassword;
    private String password;
    private static ApnsSimKeyStore instance;

    private ApnsSimKeyStore()
    {}

    public static ApnsSimKeyStore getInstance()
    {
        assert instance != null;
        return instance;
    }

    public static synchronized void init(String certFileName, String ksPassword, String password)
    {
        instance = new ApnsSimKeyStore();
        instance.certFilename = certFileName;
        instance.ksPassword = ksPassword;
        instance.password = password;
    }

    public InputStream asInputStream() throws FileNotFoundException
    {
        return new FileInputStream(certFilename);
    }

    public char[] getCertificatePassword() {
        return password.toCharArray();
    }

    public char[] getKeyStorePassword() {
        return ksPassword.toCharArray();
    }
}
