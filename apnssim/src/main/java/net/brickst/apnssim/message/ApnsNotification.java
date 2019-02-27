package net.brickst.apnssim.message;

import java.io.UnsupportedEncodingException;

public class ApnsNotification
{
    public static final int SIMPLE_APNS_NOTIFICATION = 0;
    public static final int ENHANCED_APNS_NOTIFICATION = 1;

    public ApnsNotification()
    {
        identifier = -1;
        expiry = -1;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public int getExpiry() {
        return expiry;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }

    public byte[] getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(byte[] deviceToken) {
        this.deviceToken = deviceToken;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        assert notificationType == SIMPLE_APNS_NOTIFICATION ||
                notificationType == ENHANCED_APNS_NOTIFICATION;
        this.notificationType = notificationType;
    }

    @Override
    public String toString() {
        String payloadStr = "<unknown>";
        try {
            payloadStr = new String(payload, "utf8");
        } catch (UnsupportedEncodingException e) {
            //ignore
        }
        StringBuilder sb = new StringBuilder();
        if(notificationType == SIMPLE_APNS_NOTIFICATION)
            sb.append("SimpleApnsNotification{").
               append("notificationType=").
               append(notificationType);
        else
            sb.append("EnhancedApnsNotification{").
                append("notificationType=").
                append(notificationType).
                append(", identifier=").
                append(identifier).
                append(", expiry=").
                append(expiry);

        sb.append(", deviceToken=").
            append(deviceToken).
            append(", payload=").
            append(payloadStr).
            append('}').toString();

        return sb.toString();
    }

    private int notificationType;
    private int identifier;
    private int expiry;
    private byte[] deviceToken;
    private byte[] payload;
}
