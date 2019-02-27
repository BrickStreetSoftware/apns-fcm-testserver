package net.brickst.apnssim.encode;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import net.brickst.apnssim.message.FeedbackDeviceItem;

public class FeedbackDeviceItemEncoder extends OneToOneEncoder {

    // constructors ---------------------------------------------------------------------------------------------------

    private FeedbackDeviceItemEncoder() {
    }

    // public static methods ------------------------------------------------------------------------------------------

    public static FeedbackDeviceItemEncoder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static ChannelBuffer encodeItem(FeedbackDeviceItem message) throws IllegalArgumentException
    {
        // you can move these verifications "upper" (before writing to the channel) in order not to cause a
        // channel shutdown.

        if ((message.getFeedbackDeviceToken() == null) || (message.getFeedbackDeviceToken().length == 0)) {
            throw new IllegalArgumentException("Device token cannot be null or empty");
        }

        // time(4b) + device_token_length(2b) + device_token(nb)
        int size = 6 + message.getFeedbackDeviceToken().length;

        ChannelBuffer buffer = ChannelBuffers.buffer(size);
        buffer.writeInt(message.getFeedbackTime());
        buffer.writeShort(message.getFeedbackDeviceToken().length);
        buffer.writeBytes(message.getFeedbackDeviceToken());

        return buffer;
    }

    // OneToOneEncoder ------------------------------------------------------------------------------------------------

    @Override
    protected Object encode(ChannelHandlerContext channelHandlerContext, Channel channel, Object msg) throws Exception {
        if (msg instanceof FeedbackDeviceItem) {
            return encodeItem((FeedbackDeviceItem) msg);
        } else {
            return msg;
        }
    }

    // private classes ------------------------------------------------------------------------------------------------

    private static final class InstanceHolder {
        private static final FeedbackDeviceItemEncoder INSTANCE = new FeedbackDeviceItemEncoder();
    }
}