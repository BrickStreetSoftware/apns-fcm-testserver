package net.brickst.apnssim;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;

import net.brickst.apnssim.encode.FeedbackDeviceItemEncoder;
import net.brickst.apnssim.message.ApnsNotification;
import net.brickst.apnssim.message.FeedbackDeviceItem;
import net.brickst.apnssim.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApnsFeedbackSimServerHandler extends SimpleChannelUpstreamHandler 
{
    private static final Logger logger = Logger.getLogger(ApnsFeedbackSimServerHandler.class.getName());

    static final ChannelGroup channels = new DefaultChannelGroup();
    
    private boolean isLoggingEnabled = false;
    private String foldername;
 
    public ApnsFeedbackSimServerHandler(boolean isLoggingEnabled, String foldername)
    {
        this.isLoggingEnabled = isLoggingEnabled;
        this.foldername = foldername;
        assert  (foldername != null && new File(foldername).isDirectory());
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception 
    {
        if (e instanceof ChannelStateEvent) 
        {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
        // Get the SslHandler in the current pipeline.
        // We added it in SecureChatPipelineFactory.
        final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

        // Get notified when SSL handshake is done.
        ChannelFuture handshakeFuture = sslHandler.handshake();
        handshakeFuture.addListener(new FeedbackGreeter(sslHandler, foldername, isLoggingEnabled));
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
        // Unregister the channel from the global channel list
        // so the channel does not receive messages anymore.
        channels.remove(e.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        if (e.getMessage() instanceof ApnsNotification)
        {
            if (this.isLoggingEnabled)
            {
            	logger.info("Received feedback message:" + e.getMessage());
            }
        }
        else
        {
            super.messageReceived(ctx, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) 
    {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }

    private static final class FeedbackGreeter implements ChannelFutureListener
    {
        private final SslHandler sslHandler;
        private final String foldername;
        private final boolean isLoggingEnabled;

        FeedbackGreeter(SslHandler sslHandler, String foldername, boolean isLoggingEnabled)
        {
            this.sslHandler = sslHandler;
            this.foldername = foldername;
            this.isLoggingEnabled = isLoggingEnabled;
        }

        public void operationComplete(ChannelFuture future) throws Exception
        {
            if (future.isSuccess()) 
            {
                // Once session is secured, send all data - no greeting required.
                // The client opens the connection and reads the data available
                List<FeedbackDeviceItem> items = readFeedbackFolder(foldername);
                for(FeedbackDeviceItem fdi : items)
                {
                    ChannelBuffer cb = FeedbackDeviceItemEncoder.encodeItem(fdi);
                    future.getChannel().write(fdi);
                }
                // Register the channel to the global channel list
                // so the channel received the messages from others.
                channels.add(future.getChannel());
                //Thread.sleep(1000);
                future.getChannel().disconnect();
            }
            else 
            {
                future.getChannel().close();
            }
        }

        private List<FeedbackDeviceItem> readFeedbackFolder(String foldername)
                throws FileNotFoundException
        {
            ArrayList<FeedbackDeviceItem> items = new ArrayList<FeedbackDeviceItem>();
            File[] files = Utilities.findFiles(new File(foldername));
            if(files == null)
            {
                return items;
            }
            
            for (int i = 0; i < files.length; i++) 
            {
                File file = files[i];
                if(file.getName().startsWith("feedback"))
                {
                    items.addAll(readFeedbackItems(file));
                }
            }
            
            return items;
        }

        public List<FeedbackDeviceItem> readFeedbackItems(File file)
                throws FileNotFoundException
        {
        	ArrayList<FeedbackDeviceItem> items = new ArrayList<FeedbackDeviceItem>();
            int defaultDate = (int)(Calendar.getInstance().getTimeInMillis()/1000L);
            
            //Note that FileReader is used, not File, since File is not Closeable
            try (Scanner scanner = new Scanner(new FileReader(file)))
            {
	            //first use a Scanner to get each line
	            while ( scanner.hasNextLine() )
	            {
	              items.add(processLine(scanner.nextLine()));
	            }

	            for (Iterator<FeedbackDeviceItem> iterator = items.iterator(); iterator.hasNext(); )
	            {
	            	FeedbackDeviceItem next = iterator.next();
	            	if(next.getFeedbackTime() == 0)
	            	{
                      next.setFeedbackTime(defaultDate);
	            	}
	            }
	            return items;
            }
        }

         public static Calendar parseTimestamp(String timestamp) throws ParseException 
         {
        	 /*
        	  ** we specify Locale.US since months are in english
        	  */
        	 SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);
	         Date d = sdf.parse(timestamp);
	         Calendar cal = Calendar.getInstance();
	         cal.setTime(d);
	         return cal;
         }

         protected FeedbackDeviceItem processLine(String aLine)
         {
        	 //use a second Scanner to parse the content of each line
        	 FeedbackDeviceItem result = new FeedbackDeviceItem();
        	 try (Scanner scanner = new Scanner(aLine))
        	 {
        		 scanner.useDelimiter(",");
        		 
        		 if ( scanner.hasNext() )
        		 {
        			 result.setFeedbackDeviceToken(Utilities.decodeHex(scanner.next().trim()));
        			 try
        			 {
        				 if ( scanner.hasNext() )
        				 {
        					 result.setFeedbackTime((int)(parseTimestamp(scanner.next().trim()).getTimeInMillis()/1000L));
        				 }
        			 }
        			 catch(ParseException e)
        			 {
        				 logger.info(e.getMessage());
        			 }
        		 }
        		 else
        		 {
        			 if(this.isLoggingEnabled)
        			 {
        				 logger.info("Empty or invalid line. Skipping...");
        			 }
        		 }
        	 }
        	 
        	 if(this.isLoggingEnabled)
        	 {
        		 logger.info("processed line:" + aLine);
        		 logger.info("Device item:" + result.toString());
             }
             
        	 return result;
         }	
    }
}
