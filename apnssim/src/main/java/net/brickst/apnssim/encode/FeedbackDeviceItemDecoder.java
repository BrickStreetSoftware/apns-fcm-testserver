package net.brickst.apnssim.encode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import net.brickst.apnssim.message.FeedbackDeviceItem;

public class FeedbackDeviceItemDecoder extends ReplayingDecoder<FeedbackDeviceItemDecoder.DecodingState>
{
    // internal vars --------------------------------------------------------------------------------------------------

    private FeedbackDeviceItem message;
    private DecodingState state;

    // constructors ---------------------------------------------------------------------------------------------------

    public FeedbackDeviceItemDecoder() {
        this.reset();
    }

    // ReplayingDecoder -----------------------------------------------------------------------------------------------

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer, DecodingState state)
            throws Exception
    {
        // notice the switch fall-through
        int size = -1;
        byte[] content = null;
        switch (state) {
            case FEEDBACK_TIME:
                this.message.setFeedbackTime(buffer.readInt());
                checkpoint(this.state = DecodingState.DEVICE_ID_LENGTH);
            case DEVICE_ID_LENGTH:
                size = buffer.readShort();
                if (size <= 0)
                {
                    throw new Exception("Invalid device ID size");
                }
                content = new byte[size];
                this.message.setFeedbackDeviceToken(content);
                checkpoint(this.state = DecodingState.DEVICE_ID_PAYLOAD);
            case DEVICE_ID_PAYLOAD:
                buffer.readBytes(this.message.getFeedbackDeviceToken(), 0,
                                 this.message.getFeedbackDeviceToken().length);
                this.state = DecodingState.FINISHED;
                // This is the only exit point of this method (except for the other exceptions that
                // should never occur).
                // Whenever there aren't enough bytes, a special exception is thrown by the channel buffer
                // and automatically handled by netty. That's why all conditions in the switch fall through
                try {
                    // return the instance var and reset this decoder state after doing so.
                    FeedbackDeviceItem result = this.message;
                    return result;
                } finally {
                    this.reset();
                }
            default:
                throw new IllegalStateException("Unknown decoding state: " + state);
        }
    }

    // private helpers ------------------------------------------------------------------------------------------------

    private void reset() {
        checkpoint(DecodingState.FEEDBACK_TIME);
        this.message = new FeedbackDeviceItem();
    }

    // private classes ------------------------------------------------------------------------------------------------

    public enum DecodingState {

        // constants --------------------------------------------------------------------------------------------------

        FEEDBACK_TIME,
        DEVICE_ID_LENGTH,
        DEVICE_ID_PAYLOAD,
        FINISHED
    }
}

