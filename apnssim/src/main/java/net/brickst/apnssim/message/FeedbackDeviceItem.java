package net.brickst.apnssim.message;

import java.io.UnsupportedEncodingException;

public class FeedbackDeviceItem
{
    public FeedbackDeviceItem() {
        feedbackDeviceToken = null;
        feedbackTime = -1;
    }
    public FeedbackDeviceItem(String token, int time) throws UnsupportedEncodingException {
        feedbackDeviceToken = token.getBytes("utf8");
        feedbackTime = time;
    }

    public int getFeedbackTime() {
        return feedbackTime;
    }

    public void setFeedbackTime(int feedbackTime) {
        this.feedbackTime = feedbackTime;
    }

    public byte[] getFeedbackDeviceToken() {
        return feedbackDeviceToken;
    }

    public void setFeedbackDeviceToken(byte[] feedbackDeviceToken) {
        this.feedbackDeviceToken = feedbackDeviceToken;
    }

    @Override
    public String toString() {
        return "FeedbackDeviceItem{" +
                "feedbackTime=" + feedbackTime +
                ", feedbackDeviceToken=" + feedbackDeviceToken +
                '}';
    }

    private int feedbackTime;
    private byte[] feedbackDeviceToken;

}
