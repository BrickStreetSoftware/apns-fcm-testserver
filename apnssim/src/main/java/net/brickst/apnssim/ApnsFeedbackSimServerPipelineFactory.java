package net.brickst.apnssim;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import net.brickst.apnssim.encode.FeedbackDeviceItemDecoder;
import net.brickst.apnssim.encode.FeedbackDeviceItemEncoder;
import javax.net.ssl.SSLEngine;
import java.util.Properties;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Created by IntelliJ IDEA.
 * User: berbece
 * Date: 08/09/11
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApnsFeedbackSimServerPipelineFactory implements ChannelPipelineFactory
{
    private boolean isLoggingEnabled;
    private String dataFolder;

    public ApnsFeedbackSimServerPipelineFactory(Properties props)
    {
        String logVal = props.getProperty("log.notifications", "true");
        isLoggingEnabled = Boolean.parseBoolean(logVal);
        dataFolder = props.getProperty("apns.feedback.data.folder", "feedbackData");

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

        pipeline.addLast("encoder", FeedbackDeviceItemEncoder.getInstance());
        pipeline.addLast("decoder", new FeedbackDeviceItemDecoder());
        // and then business logic.
        pipeline.addLast("handler", new ApnsFeedbackSimServerHandler(isLoggingEnabled, dataFolder));

        return pipeline;
    }

}