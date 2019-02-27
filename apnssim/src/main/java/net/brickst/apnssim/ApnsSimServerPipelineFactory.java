package net.brickst.apnssim;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import net.brickst.apnssim.encode.ApnsNotificationDecoder;

import javax.net.ssl.SSLEngine;
import java.util.Properties;
import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 *
 */
public class ApnsSimServerPipelineFactory implements ChannelPipelineFactory
{
    private boolean isLoggingEnabled;
    private boolean useErrorDetection;
    private int useThisErrorPercentage;
    private int useThisErrorCode;

    public ApnsSimServerPipelineFactory(Properties props)
    {
        String logVal = props.getProperty("log.notifications", "true");
        this.useErrorDetection = Boolean.parseBoolean(props.getProperty("apns.service.use.error.detection", "false"));
        isLoggingEnabled = Boolean.parseBoolean(logVal);
        this.useThisErrorCode = Integer.parseInt(props.getProperty("apns.service.use.error.code", "-1"));
        this.useThisErrorPercentage = Integer.parseInt(props.getProperty("apns.service.use.error.percentage", "1"));
    }

    public ChannelPipeline getPipeline() throws Exception
    {
        ChannelPipeline pipeline = pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        //
        // Read SecureChatSslContextFactory
        // if you need client certificate authentication.

        SSLEngine engine =
            ApnsSimServerSslContextFactory.getServerContext().createSSLEngine();
        engine.setUseClientMode(false);

        pipeline.addLast("ssl", new SslHandler(engine));

        //pipeline.addLast("encoder", ApnsNotificationEncoder.getInstance());
        pipeline.addLast("decoder", new ApnsNotificationDecoder());
        // and then business logic.
        pipeline.addLast("handler", new ApnsSimServerHandler(isLoggingEnabled, useErrorDetection,
                                                               useThisErrorPercentage, useThisErrorCode));

        return pipeline;
    }
}
