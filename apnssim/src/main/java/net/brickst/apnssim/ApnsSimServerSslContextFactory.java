package net.brickst.apnssim;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

/**
 * Creates a bogus {@link javax.net.ssl.SSLContext}.  A client-side context created by this
 * factory accepts any certificate even if it is invalid.  A server-side context
 * created by this factory sends a bogus certificate defined in {@link org.jboss.netty.example.securechat.SecureChatKeyStore}.
 * <p>
 * You will have to create your context differently in a real world application.
 *
 * <h3>Client Certificate Authentication</h3>
 *
 * To enable client certificate authentication:
 * <ul>
 * <li>Enable client authentication on the server side by calling
 *     {@link javax.net.ssl.SSLEngine#setNeedClientAuth(boolean)} before creating
 *     {@link org.jboss.netty.handler.ssl.SslHandler}.</li>
 * <li>When initializing an {@link javax.net.ssl.SSLContext} on the client side,
 *     specify the {@link javax.net.ssl.KeyManager} that contains the client certificate as
 *     the first argument of {@link javax.net.ssl.SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom)}.</li>
 * <li>When initializing an {@link javax.net.ssl.SSLContext} on the server side,
 *     specify the proper {@link javax.net.ssl.TrustManager} as the second argument of
 *     {@link javax.net.ssl.SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom)}
 *     to validate the client certificate.</li>
 * </ul>
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class ApnsSimServerSslContextFactory
{
    private static final String KEYSTORE_TYPE = "PKCS12";
//    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEY_ALGORITHM = "sunx509";

    private static final String PROTOCOL = "TLS";
    private static final SSLContext SERVER_CONTEXT;
    private static final SSLContext CLIENT_CONTEXT;

    static {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = KEY_ALGORITHM;
        }

        SSLContext serverContext = null;
        SSLContext clientContext = null;
        try {

          /*
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(ApnsSimKeyStore.getInstance().asInputStream(),
                    ApnsSimKeyStore.getInstance().getKeyStorePassword());

            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, ApnsSimKeyStore.getInstance().getCertificatePassword());

            // Initialize the SSLContext to work with our key managers.
            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), null, null);
            */
            serverContext = newSSLContext(ApnsSimKeyStore.getInstance().asInputStream(),
                            ApnsSimKeyStore.getInstance().getKeyStorePassword(),
                            KEYSTORE_TYPE, algorithm);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the server-side SSLContext", e);
        }

        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(null, ApnsSimTrustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }

        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    public static SSLContext newSSLContext(InputStream cert, char[] password,
         String ksType, String ksAlgorithm) throws IllegalStateException {
        try {
            KeyStore ks = KeyStore.getInstance(ksType);
            ks.load(cert, password);

            // Get a KeyManager and initialize it
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
            kmf.init(ks, password);

            // Get a TrustManagerFactory and init with KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
            tmf.init(ks);

            // Get the SSLContext to help create SSLSocketFactory
            SSLContext sslc = SSLContext.getInstance("TLS");
            sslc.init(kmf.getKeyManagers(), null, null);
            return sslc;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }

    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }
}
