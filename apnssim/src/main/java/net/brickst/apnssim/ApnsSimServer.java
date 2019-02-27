package net.brickst.apnssim;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Simple APNS simulator server modified from chat server sample.
 *
 */
public class ApnsSimServer
{
    private Properties props;
    private DefaultChannelGroup channelGroup;
    private ServerChannelFactory serverFactory;
    private ServerChannelFactory feedbackServerFactory;

    public ApnsSimServer(Properties props)
    {
        this.props = props;
    }

    public static void main1(String[] args) throws Exception
    {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        Properties props = new Properties();
        props.load(new FileInputStream("apns.properties"));

        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new ApnsSimServerPipelineFactory(props));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(2195));
    }

    /*
    private void loadCertFile(String certFile)
    {
		// read certificate from class path
		InputStream certStream = null;
        try {
			certStream = this.getClass().getResourceAsStream("/" + certFile);
			// "/" means "use filename as is without prepending package".
        }
		catch (Exception x) {
			throw new RuntimeException("Error Loading Certificate: " + certFile, x);
		}
        if(certStream == null)
        {
            System.out.println("Couldn't load '" + certFile + "' as a resource - trying as a file");
            try {
                certStream = new FileInputStream(certFile);
            } catch (FileNotFoundException e) {
            	System.out.println("Couldn't load '" + certFile + "' as a file");
                throw new RuntimeException("Error Loading Certificate from file: " + certFile, e);
            }
        }
    }
    */
    
    public static void main(String[] args)
    {
        final ApnsSimServer server;
        FileInputStream fis = null;
        try {
            Properties props = new Properties();
            fis = new FileInputStream(args[0]);
            props.load(fis);
            server = new ApnsSimServer(props);
            String certFile = props.getProperty("apns.certificate.filename");
            String certPass = props.getProperty("apns.certificate.password");

            ApnsSimKeyStore.init(certFile, certPass, certPass);
            if (!server.start() || !server.startFeedbackService()) {

                System.exit(-1);
                return; // not really needed...
            }

            System.out.println("Server started...");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    server.stop();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
        }
    }

    public boolean start() {
    //    if(true) return true;
        this.serverFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                                                               Executors.newCachedThreadPool());
        if(this.channelGroup == null)
            this.channelGroup = new DefaultChannelGroup(this + "-channelGroup");

        ChannelPipelineFactory pipelineFactory = new ApnsSimServerPipelineFactory(props);
        ServerBootstrap bootstrap = new ServerBootstrap(this.serverFactory);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setPipelineFactory(pipelineFactory);

        String host = props.getProperty("apns.gateway.host", "localhost");
        int port = Integer.parseInt(props.getProperty("apns.gateway.port", "2195"));

        Channel channel = bootstrap.bind(new InetSocketAddress(host, port));
        if (!channel.isBound()) {
            this.stop();
            return false;
        }

        this.channelGroup.add(channel);
        return true;
    }

    public boolean startFeedbackService() {

        this.feedbackServerFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                                                               Executors.newCachedThreadPool());
        if(this.channelGroup == null)
            this.channelGroup = new DefaultChannelGroup(this + "-channelGroup");
        ChannelPipelineFactory pipelineFactory = new ApnsFeedbackSimServerPipelineFactory(props);
        ServerBootstrap bootstrap = new ServerBootstrap(this.feedbackServerFactory);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setPipelineFactory(pipelineFactory);

        String host = props.getProperty("apns.feedback.gateway.host", "localhost");
        int port = Integer.parseInt(props.getProperty("apns.feedback.gateway.port", "2196"));

        Channel channel = bootstrap.bind(new InetSocketAddress(host, port));
        if (!channel.isBound()) {
            this.stop();
            return false;
        }

        this.channelGroup.add(channel);
        return true;
    }

    public void stop() {
        if (this.channelGroup != null) {
            this.channelGroup.close();
        }
        if (this.serverFactory != null) {
            this.serverFactory.releaseExternalResources();
        }
        if (this.feedbackServerFactory != null) {
            this.feedbackServerFactory.releaseExternalResources();
        }
    }

}