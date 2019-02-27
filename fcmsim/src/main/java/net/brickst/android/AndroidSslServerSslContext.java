package net.brickst.android;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Creates server side SSL context.
 */
public class AndroidSslServerSslContext
{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AndroidSslServerSslContext.class);
    private static final String PROTOCOL = "TLS";

    private final SSLContext _serverContext;

    /**
     * Returns the singleton instance for this class
     */
    public static AndroidSslServerSslContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     *
     * See http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private interface SingletonHolder {
        AndroidSslServerSslContext INSTANCE = new AndroidSslServerSslContext();
    }

    /**
     * Constructor for singleton
     */
    private AndroidSslServerSslContext() {
        SSLContext serverContext = null;
        try {
            // Key store (Server side certificate)
            String algorithm = System.getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null) {
                algorithm = "SunX509";
            }
            String keystoreType = System.getProperty("keystore.type");
            if(keystoreType == null) {
                keystoreType = "PKCS12";
            }

            try {
                String keyStoreFilePath = System.getProperty("keystore.file.path");
                String keyStoreFilePassword = System.getProperty("keystore.file.password");

                KeyStore ks = KeyStore.getInstance(keystoreType);

                FileInputStream fin = new FileInputStream(keyStoreFilePath);
                ks.load(fin, keyStoreFilePassword.toCharArray());

                // Set up key manager factory to use our key store
                // Assume key password is the same as the key store file
                // password
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(ks, keyStoreFilePassword.toCharArray());

                // Initialise the SSLContext to work with our key managers.
                serverContext = SSLContext.getInstance(PROTOCOL);
                serverContext.init(kmf.getKeyManagers(), null, null);
            } catch (Exception e) {
                throw new Error("Failed to initialize the server-side SSLContext", e);
            }
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("Error initializing SslContextManager. " + ex.getMessage(), ex);
            }
            System.exit(1);
        } finally {
            _serverContext = serverContext;
        }
    }

    /**
     * Returns the server context with server side key store
     */
    public SSLContext getServerContext() {
        return _serverContext;
    }
}
