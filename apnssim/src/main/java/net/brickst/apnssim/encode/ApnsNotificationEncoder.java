package net.brickst.apnssim.encode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import net.brickst.apnssim.message.ApnsNotification;

public class ApnsNotificationEncoder extends OneToOneEncoder {

    // constructors ---------------------------------------------------------------------------------------------------

    private ApnsNotificationEncoder() {
    }

    // public static methods ------------------------------------------------------------------------------------------

    public static ApnsNotificationEncoder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static ChannelBuffer encodeMessage(ApnsNotification message) throws IllegalArgumentException {
        // you can move these verifications "upper" (before writing to the channel) in order not to cause a
        // channel shutdown.
        if ((message.getNotificationType() != ApnsNotification.SIMPLE_APNS_NOTIFICATION) &&
                (message.getNotificationType() == ApnsNotification.ENHANCED_APNS_NOTIFICATION)) {
            throw new IllegalArgumentException("Unknown message type");
        }

        if ((message.getDeviceToken() == null) || (message.getDeviceToken().length == 0)) {
            throw new IllegalArgumentException("Device token cannot be null or empty");
        }

        if ((message.getPayload() == null) || (message.getPayload().length == 0)) {
            throw new IllegalArgumentException("Message payload cannot be null or empty");
        }

        // version(1b) + type(1b) + payload length(4b) + payload(nb)
        int size = message.getNotificationType() == ApnsNotification.ENHANCED_APNS_NOTIFICATION ?
                15 + message.getDeviceToken().length + message.getPayload().length :
                7  + message.getDeviceToken().length + message.getPayload().length ;

        ChannelBuffer buffer = ChannelBuffers.buffer(size);
        buffer.writeByte(message.getNotificationType());
        if(message.getNotificationType() == ApnsNotification.ENHANCED_APNS_NOTIFICATION)
        {
            buffer.writeInt(message.getIdentifier());
            buffer.writeInt(message.getExpiry());
        }
        buffer.writeShort(message.getDeviceToken().length);
        buffer.writeBytes(message.getDeviceToken());
        buffer.writeInt(message.getPayload().length);
        buffer.writeBytes(message.getPayload());

        return buffer;
    }

    // OneToOneEncoder ------------------------------------------------------------------------------------------------

    @Override
    protected Object encode(ChannelHandlerContext channelHandlerContext, Channel channel, Object msg) throws Exception {
        if (msg instanceof ApnsNotification) {
            return encodeMessage((ApnsNotification) msg);
        } else {
            return msg;
        }
    }

    // private classes ------------------------------------------------------------------------------------------------

    private static final class InstanceHolder {
        private static final ApnsNotificationEncoder INSTANCE = new ApnsNotificationEncoder();
    }
}

