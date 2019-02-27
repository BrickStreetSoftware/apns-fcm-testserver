package net.brickst.android;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * The handler of the android message
 */
public class AndroidSslServerHandler extends SimpleChannelUpstreamHandler {
    //private static final InternalLogger logger = InternalLoggerFactory.getInstance(AndroidSslServerHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {

        if (req.getMethod() == POST)
        {
            handleRequest(ctx, req);
            return;
        }

        if (req.getMethod() == GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<Object, Object> readValue(String content) {
        HashMap<Object, Object> result = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = mapper.readValue(content, HashMap.class);
        } catch (IOException e) {
        }
        return result;
    }

    private void handleRequest(ChannelHandlerContext ctx, HttpRequest req)
    {
        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
        try
        {
            String reqContent = req.getContent().toString(CharsetUtil.UTF_8);
            Tracer.getANSIM().println("req=" + reqContent);
            HashMap<Object, Object> objectMap = null;
            ChannelBuffer content = null;
            if(reqContent.startsWith("{"))
            {
                objectMap = readValue(reqContent);
                AndroidSimMessageResponse.RequestedError re = AndroidSimMessageResponse.getRequestedError(objectMap);
                if(re != null)
                {
                    if(re.getHttpStatusCode() != OK.getCode())
                    {
                        res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(re.getHttpStatusCode()));
                    }
                    else
                    {
                        content = AndroidSimMessageResponse.getContent(req, objectMap, re);
                    }
                }
                else
                {
                    content = AndroidSimMessageResponse.getOKContent(req, objectMap);
                }
            }
            else
            {
                content = AndroidSimMessageResponse.getOKContent(req, null);
            }

            if(content != null)
            {
            	res.headers().add(CONTENT_TYPE, "application/json; charset=UTF-8");
                setContentLength(res, content.readableBytes());
                res.setContent(content);
                Tracer.getANSIM().println("response sent");
            }
        } catch(Throwable t) {
            res = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        }
        sendHttpResponse(ctx, req, res);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
//        e.getCause().printStackTrace();
        if(Tracer.getANSIM().isEnabled())
        {
            Tracer.getANSIM().println(e.getCause());
        }
        e.getChannel().close();
    }
}
