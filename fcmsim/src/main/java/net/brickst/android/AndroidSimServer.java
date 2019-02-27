package net.brickst.android;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * This is the class which starts up the Android simulator
 */
public class AndroidSimServer
{
    public static final String SIM_URL_KEY = "android.sim.url";
    public static final String SIM_VERBOSE_KEY = "android.sim.verbose";
    public static final String KEYMANAGERFACTORY_ALGORITHM_KEY = "android.sim.ssl.KeyManagerFactory.algorithm";
    public static final String KEYMANAGERFACTORY_ALGORITHM = "SunX509";
    public static final String KEYSTORE_TYPE_KEY = "android.sim.keystore.type";
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final String KEYSTORE_FILE_PATH_KEY = "android.sim.keystore.file.path";
    public static final String KEYSTORE_FILE_PASSWORD_KEY = "android.sim.keystore.file.password";
    private final int port;

    public AndroidSimServer(int port, Properties props) {
        this.port = port;
    }

    public void run() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new AndroidSslServerPipelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));

        System.out.println("Android sim server started at port " + port + '.');
        System.out.println("Use this URL for GCM service access: https://localhost:" + port + '/');
    }

    public static void main(String[] args)
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        try
        {
            if (args.length > 0)
            {
                fis = new FileInputStream(args[0]);
            }
            else
            {
                printUsage();
            }
            props.load(fis);
            String simURLs = props.getProperty(SIM_URL_KEY);
            URL url = new URL(simURLs);
            int port = url.getPort();

            String keyMgrFAlg = props.getProperty(KEYMANAGERFACTORY_ALGORITHM_KEY);
            if (keyMgrFAlg == null || keyMgrFAlg.length() == 0) {
                System.out.println("Assuming property " + KEYMANAGERFACTORY_ALGORITHM_KEY + " = " + KEYMANAGERFACTORY_ALGORITHM);
                keyMgrFAlg = KEYMANAGERFACTORY_ALGORITHM;
            }
            System.setProperty("ssl.KeyManagerFactory.algorithm", keyMgrFAlg);

            String keystoreType = props.getProperty(KEYSTORE_TYPE_KEY);
            if (keystoreType == null || keystoreType.length() == 0) {
                System.out.println("Assuming property " + KEYSTORE_TYPE_KEY + " = " + KEYSTORE_TYPE);
                keystoreType = KEYSTORE_TYPE;
            }
            System.setProperty("keystore.type", keystoreType);

            String keyStoreFilePath = props.getProperty(KEYSTORE_FILE_PATH_KEY);
            if (keyStoreFilePath == null || keyStoreFilePath.length() == 0) {
                System.out.println("ERROR: property " + KEYSTORE_FILE_PATH_KEY + " not set. Exiting now!");
                System.exit(1);
            }
            System.setProperty("keystore.file.path", keyStoreFilePath);

            String keyStoreFilePassword = props.getProperty(KEYSTORE_FILE_PASSWORD_KEY);
            if (keyStoreFilePassword == null || keyStoreFilePassword.length() == 0) {
                System.out.println("ERROR: property " + KEYSTORE_FILE_PASSWORD_KEY + " not set. Exiting now!");
                System.exit(1);
            }
            System.setProperty("keystore.file.password", keyStoreFilePassword);

            String verbose = props.getProperty(SIM_VERBOSE_KEY);
            if (verbose != null && (verbose.equalsIgnoreCase("true") || verbose.equalsIgnoreCase("yes"))) {
                Tracer.getANSIM().setEnabled(true);
            }

            new AndroidSimServer(port, props).run();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(fis != null) try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\tjava net.brickst.android.AndroidSimServer <props_file_name>");
        System.exit(-1);
    }
}
