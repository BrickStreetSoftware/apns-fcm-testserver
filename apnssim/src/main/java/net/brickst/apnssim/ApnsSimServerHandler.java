package net.brickst.apnssim;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;

import net.brickst.apnssim.message.ApnsNotification;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a server-side channel.
 *
 */
 public class ApnsSimServerHandler  extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            ApnsSimServerHandler.class.getName());

    static final ChannelGroup channels = new DefaultChannelGroup();
    private boolean isLoggingEnabled = false;
    private boolean useErrorDetection = false;
    private int useThisErrorCode = 0;
    private int useThisErrorPercentage = 0;
    private ApnsNotification lastProcessed = null;

    public ApnsSimServerHandler(boolean isLoggingEnabled, boolean useErrorDetection, int errPercent, int errorCode)
    {
        this.isLoggingEnabled = isLoggingEnabled;
        this.useErrorDetection = useErrorDetection;
        this.useThisErrorPercentage = errPercent;
        this.useThisErrorCode = errorCode;
    }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        // Get the SslHandler in the current pipeline.
        // We added it in SecureChatPipelineFactory.
        final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

        // Get notified when SSL handshake is done.
        ChannelFuture handshakeFuture = sslHandler.handshake();
        handshakeFuture.addListener(new Greeter(sslHandler, this.useErrorDetection));
    }

    @Override
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Unregister the channel from the global channel list
        // so the channel does not receive messages anymore.
        channels.remove(e.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        if (e.getMessage() instanceof ApnsNotification)
        {
            if(this.isLoggingEnabled)
                    logger.info("Received message:" + e.getMessage());
            ApnsNotification prev = this.lastProcessed;
            this.lastProcessed = (ApnsNotification) e.getMessage();

            //added support for error codes return
            //TODO: add sleep before returning message if needed
            if (this.useErrorDetection)
            {
                int errorCode = 8;
                if(useThisErrorCode >=0)
                {
                    errorCode = useThisErrorCode;
                }
                else
                {
                    Random randomGenerator = new Random();
                    int randomInt = randomGenerator.nextInt(100);
                    if(randomInt < useThisErrorPercentage)
                    {
                        if (randomInt < 3) {
                            //No errors encountered
                            errorCode = 0;
                        } else if (randomInt < 6) {
                            //Processing error
                            errorCode = 1;
                        } else if (randomInt < 9) {
                            //Missing device token
                            errorCode = 2;
                        } else if (randomInt < 12) {
                            //Missing topic
                            errorCode = 3;
                        } else if (randomInt < 15) {
                            //Missing payload
                            errorCode = 4;
                        } else if (randomInt < 18) {
                            //Invalid token size
                            errorCode = 5;
                        } else if (randomInt < 21) {
                            //Invalid topic size
                            errorCode = 6;
                        } else if (randomInt < 24) {
                            //Invalid payload size
                            errorCode = 7;
                        } else if (randomInt < 27) {
                            //Invalid token
                            errorCode = 8;
                        } else if (randomInt < 30) {
                            //Shutdown
                            errorCode = 10;
                        } else if (randomInt < 33) {
                            //None (unknown)
                            errorCode = 255;
                        } else {
                            return;
                        }
                    }
                    else
                    {
                        return;
                    }
                }
                ApnsNotification notification = (ApnsNotification)e.getMessage();
                ChannelBuffer cb = ChannelBuffers.buffer(6);
                byte[] buff = {8,(byte) errorCode};
                cb.writeBytes(buff);
                if(errorCode == 10 && prev != null)
                {
                    cb.writeBytes(intToByteArray(prev.getIdentifier()));
                }
                else
                {
                    cb.writeBytes(intToByteArray(notification.getIdentifier()));
                }
                ctx.getChannel().write(cb);
            }
        }
        else
        {
            super.messageReceived(ctx, e);
        }
    }
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }

    /**
     * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
     * @author <a href="http://gleamynode.net/">Trustin Lee</a>
     * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
     */
    private static final class Greeter implements ChannelFutureListener {

        private final SslHandler sslHandler;
        private boolean useErrorDetection = false;

        Greeter(SslHandler sslHandler, boolean useErrorDetection) {
            this.sslHandler = sslHandler;
            this.useErrorDetection = useErrorDetection;
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                // Once session is secured, send a greeting.
//                future.getChannel().write(
//                        "Welcome to " + InetAddress.getLocalHost().getHostName() +
//                        " secure APNS simulator!\n");
//                future.getChannel().write(
//                        "Your session is protected by " +
//                        sslHandler.getEngine().getSession().getCipherSuite() +
//                        " cipher suite.\n");
                // Register the channel to the global channel list
                // so the channel received the messages from others.
                channels.add(future.getChannel());
            } else {
                future.getChannel().close();
            }
        }
    }
}
