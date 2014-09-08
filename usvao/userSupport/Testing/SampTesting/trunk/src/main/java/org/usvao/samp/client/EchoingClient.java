package org.usvao.samp.client;


import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.astrogrid.samp.ErrInfo;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.Response;
import org.astrogrid.samp.Subscriptions;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.client.HubConnection;
import org.astrogrid.samp.client.HubConnector;
import org.astrogrid.samp.client.MessageHandler;
import org.astrogrid.samp.httpd.UtilServer;

/** A client designed to echo back some mtype messages for testing.
 * 
 * @author thomas
 *
 */
public class EchoingClient 
extends SimpleListeningClient
{
	
	private static final Logger logger_ = Logger.getLogger( EchoingClient.class.getName() );
	
	public EchoingClient(OutputStream logto) {
		super(logto);
	}

	public static TestingClient newClient() {
		return new EchoingClient(System.out); // profile, subs, meta, System.out, 2 );
	}
	
	@Override
	public void configAndRun ( Metadata meta, final Subscriptions subs) 
	{

		HubConnector connector = new HubConnector(DefaultClientProfile.getProfile());
		connector.declareMetadata( meta );

		// Prepare all-purpose response to logged messages.
		final Response response = new Response();
		response.setStatus( Response.WARNING_STATUS );
		response.setResult( new HashMap() );
		response.setErrInfo( new ErrInfo( "Message logged, not acted on" ) );

		// Add a handler which will handle the subscribed messages.
		connector.addMessageHandler( new EchoHandler(subs));
		connector.declareSubscriptions( connector.computeSubscriptions() );
		clientMap_ = connector.getClientMap();

		// Connect and ready to log.
		connector.setActive( true );
		connector.setAutoconnect(2);
	}

	
	public static Metadata createDefaultMetadata() {
		Metadata meta = new Metadata();
		meta.setName("EchoingClient");
		meta.setDescriptionText( "Listens in to messages for echo purposes" );
		try {
			meta.setIconUrl( UtilServer.getInstance()
					.exportResource( "/org/astrogrid/samp/images/ears.png" ).toString() );
		}
		catch ( IOException e ) {
//			logger_.warning( "Can't export icon" );
		}
		meta.put( "Author", "VAO Testing Team (based heavily on work by Mark Taylor)" );
		
		return meta;
	}
	
	/**
	 * Main method.  Runs a client.
	 */
	public static void main( String[] args ) throws IOException {
		TestingClient client = new EchoingClient(System.out); 
		int status = runMain( client, args );
		if ( status != 0 ) {
			System.exit( status );
		}
	}
	
	class EchoResponse
	extends Response
	{
		
		public EchoResponse (String status, Map result) {
			setResult(result); setStatus(status); 
		}
		
		@Override
		public ErrInfo getErrInfo() { return new ErrInfo("Message echoed"); }
	}
	
	class EchoHandler 
	implements MessageHandler
	{

		Subscriptions subs = null;
		public EchoHandler (Subscriptions subs)
		{
			this.subs = subs;
		}
		
		@Override
		public Map getSubscriptions() { return subs; }

		@Override
		public void receiveCall(HubConnection hub, String senderId, String msgId, Message msg) 
		throws Exception 
		{
			System.out.println("receiveCall called hub:"+hub+" senderId:"+senderId+" msgId:"+msgId+" msg:"+msg);
			Map<String,String> result = new HashMap<String,String>();
//			result.put("samp.status", Response.OK_STATUS);
//			result.put("samp.status", Response.OK_STATUS);
			hub.reply( msgId, new EchoResponse (Response.OK_STATUS, result));
		}

		@Override
		public void receiveNotification(HubConnection arg0, String senderId, Message msg) 
		throws Exception 
		{
			System.out.println("receiveNotification called arg0:"+arg0+" senderId:"+senderId+" msg:"+msg);
		}
		
	}

}
