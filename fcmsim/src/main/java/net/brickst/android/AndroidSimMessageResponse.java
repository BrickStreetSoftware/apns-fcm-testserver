package net.brickst.android;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class creates response content based on the request content
 */
public class AndroidSimMessageResponse
{
    private static String NL = System.getProperty("line.separator");
    private static String ERROR_MSG_TEMPLATE =          "{ \"multicast_id\": 108," + NL +
            "\"success\": %1$s," + NL +
            "\"failure\": %2$s," + NL +
            "\"canonical_ids\": 0," + NL +
            "\"results\": [" + NL +
            "   { \"error\": \"%3$s\" }" + NL +
            "  ]" + NL +
            "}";
    
    private static String SUCCESS_MSG_TEMPLATE =          "{ \"multicast_id\": 108," + NL +
    		"\"success\": 1," + NL +
            "\"failure\": 0," + NL +
            "\"canonical_ids\": %1$d," + NL +
            "\"results\": [" + NL +
            "   { \"message_id\": \"1:08\" %2$s }" + NL +
            "  ]" + NL +
            "}";

    public static ChannelBuffer getOKContent(HttpRequest req, HashMap<Object, Object> objectMap)
    {
    	String registrationId = "";
    	Map dataItems = (Map) objectMap.get("data");
    	int canonical_ids = 0;
    	if (dataItems != null && dataItems.get("registration_id") != null) {
    		registrationId = ", \"registration_id\" : \"" + (String) dataItems.get("registration_id") + "\"";
    		canonical_ids = 1;
    	} else {
    		registrationId = "";
    	}
        return ChannelBuffers.copiedBuffer(
                String.format(SUCCESS_MSG_TEMPLATE, canonical_ids, registrationId),
                CharsetUtil.US_ASCII);
    }

    public static ChannelBuffer getContent(HttpRequest req, HashMap<Object, Object> objectMap, RequestedError re)
    {
        if(re == null)
        {
            return getOKContent(req, objectMap);
        }
        return  ChannelBuffers.copiedBuffer(
                String.format(ERROR_MSG_TEMPLATE, re.getSuccess(), re.getFailure(), re.getErrorCode()),
                CharsetUtil.US_ASCII);
    }

    public static RequestedError getRequestedError(HashMap<Object, Object> objectMap)
    {
        Map dataItems = (Map) objectMap.get("data");
        RequestedError result = null;
        int httpStatusCode = HttpResponseStatus.OK.getCode();
        int success = 0;
        int failure = 1;
        String errorCode = "Unknown";
        if(dataItems != null && dataItems.get("error") != null)
        {
            Map<String,String> errorItems = (Map<String, String>) dataItems.get("error");
            try
            {
                httpStatusCode = Integer.parseInt(errorItems.get("httpStatusCode"));
                success = Integer.parseInt(errorItems.get("success"));
                failure = Integer.parseInt(errorItems.get("failure"));
                errorCode = errorItems.get("errorCode");
                result = new RequestedError(httpStatusCode,success, failure, errorCode);
            }catch(Exception e){
                result = new RequestedError(httpStatusCode,success, failure, errorCode);
            }
        }
        return result;
    }

    public static class RequestedError
    {
        private int httpStatusCode;
        private int success;
        private int failure;
        private String errorCode;

        public RequestedError(int httpStatusCode, int success, int failure, String errorCode) {
            this.httpStatusCode = httpStatusCode;
            this.success = success;
            this.failure = failure;
            this.errorCode = errorCode;
        }

        public int getHttpStatusCode() {
            return httpStatusCode;
        }

        public int getSuccess() {
            return success;
        }

        public int getFailure() {
            return failure;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
