package net.brickst.apnssim.encode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import net.brickst.apnssim.message.ApnsNotification;


public class ApnsNotificationDecoder extends ReplayingDecoder<ApnsNotificationDecoder.DecodingState>
{

    // internal vars --------------------------------------------------------------------------------------------------

    private ApnsNotification message;
    private DecodingState state;
    // constructors ---------------------------------------------------------------------------------------------------

    public ApnsNotificationDecoder() {
        this.reset();
    }

    // ReplayingDecoder -----------------------------------------------------------------------------------------------

    protected Object decodeEnhancedNotification(ChannelBuffer buffer, DecodingState state)
            throws Exception
    {
        int size = -1;
        byte[] content = null;
        switch (state) {
            case IDENTIFIER:
                this.message.setIdentifier(buffer.readInt());
                checkpoint(this.state = DecodingState.EXPIRY_TIME);
            case EXPIRY_TIME:
                this.message.setExpiry(buffer.readInt());
                checkpoint(this.state = DecodingState.DEVICE_ID_LENGTH);
            case DEVICE_ID_LENGTH:
                size = buffer.readShort();
                if (size <= 0)
                {
                    throw new Exception("Invalid device ID size");
                }
                content = new byte[size];
                this.message.setDeviceToken(content);
                checkpoint(this.state = DecodingState.DEVICE_ID_PAYLOAD);
            case DEVICE_ID_PAYLOAD:
                buffer.readBytes(this.message.getDeviceToken(), 0,
                                 this.message.getDeviceToken().length);
                checkpoint(this.state = DecodingState.PAYLOAD_LENGTH);
            case PAYLOAD_LENGTH:
                size = buffer.readShort();
                if (size <= 0) {
                    throw new Exception("Invalid content size");
                }
                // pre-allocate content buffer
                 content = new byte[size];
                this.message.setPayload(content);
                checkpoint(this.state = DecodingState.PAYLOAD);
            case PAYLOAD:
                // drain the channel buffer to the message content buffer
                // I have no idea what the contents are, but I'm sure you'll figure out how to turn these
                // bytes into useful content.
                buffer.readBytes(this.message.getPayload(), 0,
                                 this.message.getPayload().length);
                this.state = DecodingState.FINISHED;
                // This is the only exit point of this method (except for the two other exceptions that
                // should never occur).
                // Whenever there aren't enough bytes, a special exception is thrown by the channel buffer
                // and automatically handled by netty. That's why all conditions in the switch fall through
                // return the instance var and reset this decoder state after doing so.
                return this.message;
            default:
                throw new Exception("Unknown decoding state: " + state);
        }
    }

    protected Object decodeSimpleNotification(ChannelBuffer buffer, DecodingState state)
            throws Exception
    {
        int size = -1;
        byte[] content = null;
        switch (state) {
            case DEVICE_ID_LENGTH:
                size = buffer.readShort();
                if (size <= 0)
                {
                    throw new Exception("Invalid device ID size");
                }
                content = new byte[size];
                this.message.setDeviceToken(content);
                checkpoint(this.state = DecodingState.DEVICE_ID_PAYLOAD);
            case DEVICE_ID_PAYLOAD:
                buffer.readBytes(this.message.getDeviceToken(), 0,
                                 this.message.getDeviceToken().length);
                checkpoint(this.state = DecodingState.PAYLOAD_LENGTH);
            case PAYLOAD_LENGTH:
                size = buffer.readShort();
                if (size <= 0) {
                    throw new Exception("Invalid content size");
                }
                // pre-allocate content buffer
                 content = new byte[size];
                this.message.setPayload(content);
                checkpoint(this.state = DecodingState.PAYLOAD);
            case PAYLOAD:
                // drain the channel buffer to the message content buffer
                // I have no idea what the contents are, but I'm sure you'll figure out how to turn these
                // bytes into useful content.
                buffer.readBytes(this.message.getPayload(), 0,
                                 this.message.getPayload().length);
                this.state = DecodingState.FINISHED;
                // return the instance var and reset this decoder state after doing so.
                return this.message;
            default:
                throw new Exception("Unknown decoding state: " + state);
        }
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer, DecodingState state)
            throws Exception
    {
        byte notificationType = -1;
        // notice the switch fall-through
        switch (state)
        {
            case COMMAND:
                notificationType = buffer.readByte();
                checkpoint(DecodingState.IDENTIFIER);
                if(notificationType == ApnsNotification.ENHANCED_APNS_NOTIFICATION)
                {
                    message = new ApnsNotification();
                    message.setNotificationType(ApnsNotification.ENHANCED_APNS_NOTIFICATION);
                    decodeEnhancedNotification(buffer, state = DecodingState.IDENTIFIER);
                }
                else if(notificationType == ApnsNotification.SIMPLE_APNS_NOTIFICATION)
                {
                    message = new ApnsNotification();
                    message.setNotificationType(ApnsNotification.SIMPLE_APNS_NOTIFICATION);
                    decodeSimpleNotification(buffer, state = DecodingState.DEVICE_ID_LENGTH);
                }
                else
                    throw new IllegalStateException("Can't recognize notification type = " + notificationType);

            default:
                if(this.state != DecodingState.FINISHED)
                {
                    if(message.getNotificationType() == ApnsNotification.ENHANCED_APNS_NOTIFICATION)
                    {
                        decodeEnhancedNotification(buffer, state);
                    }
                    else if(message.getNotificationType() == ApnsNotification.SIMPLE_APNS_NOTIFICATION)
                    {
                        decodeSimpleNotification(buffer, state);
                    }
                    else
                        throw new IllegalStateException("Can't recognize notification type = " + notificationType);
                }
        }
        // This is the only exit point of this method (except for the two other exceptions that
        // should never occur).
        // Whenever there aren't enough bytes, a special exception is thrown by the channel buffer
        // and automatically handled by netty. That's why all conditions in the switch fall through
        try {
            // return the instance var and reset this decoder state after doing so.
            ApnsNotification result = this.message;
            return result;
        } finally {
            this.reset();
        }
    }

    // private helpers ------------------------------------------------------------------------------------------------

    private void reset() {
        checkpoint(DecodingState.COMMAND);
        this.message = new ApnsNotification();
    }

    // private classes ------------------------------------------------------------------------------------------------

    public enum DecodingState {

        // constants --------------------------------------------------------------------------------------------------

        COMMAND,
        IDENTIFIER,
        EXPIRY_TIME,
        DEVICE_ID_LENGTH,
        DEVICE_ID_PAYLOAD,
        PAYLOAD_LENGTH,
        PAYLOAD,
        FINISHED
    }
}
