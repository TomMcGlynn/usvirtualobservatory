/**
 * 
 */
package org.usvao.samp.client;

/**
 * @author thomas
 *
 */
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.astrogrid.samp.Client;
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
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/** Basically a re-engineering of the Snooper class written by Mark Taylor
 * to make it more useful for VAO testing of samp applications.
 * 
 * @author   thomas
 */
public class SimpleListeningClient 
implements TestingClient
{

	protected final static byte[] newline_;
	private final static String charset = "UTF-8";

	protected Map clientMap_ = null;
	private OutputStream logout = System.out;

	@Option(name="-listen", usage="The name or id of client to listen to")
	private String listenOnlyToClientName = "";

	@Option(name="-v", usage="Make client more verbose")
	private boolean verbose = false;

	@Option(name="-h", usage="Get usage message")
	private boolean help;

	@Option(name="-clientname", usage="The samp name of the client")
	private String clientName = "client";

	@Option(name="-mtype", usage="Message type pattern defining which messages are received and logged") 
	private List<String> subscriptionList = new ArrayList<String>();
	
	@Argument
	private List<String> arguments = new ArrayList<String>();  

	static {
		byte[] nl;
		try { nl = System.getProperty( "line.separator", "\n" ).getBytes( charset ); }
		catch ( Exception e ) { nl = new byte[] { (byte) '\n' }; }
		newline_ = nl;
	}

	private static final Logger logger_ =
		Logger.getLogger( SimpleListeningClient.class.getName() );

	public SimpleListeningClient( OutputStream logto) 
	{
		this.logout = logto;
	}

	/**
	 * configureAndRun the client.
	 */
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
		connector.addMessageHandler( new MessageHandler() {
			public Map getSubscriptions() {
				return subs;
			}
			public void receiveNotification( HubConnection connection,
					String senderId,
					Message msg )
			throws IOException {
				log( senderId, msg, null );
			}
			public void receiveCall( HubConnection connection,
					String senderId,
					String msgId, Message msg )
			throws IOException {
				log( senderId, msg, msgId );
				connection.reply( msgId, response );
			}
		} );
		connector.declareSubscriptions( connector.computeSubscriptions() );
		clientMap_ = connector.getClientMap();

		// Connect and ready to log.
		connector.setActive( true );
		connector.setAutoconnect(2);
	}

	protected final String getSenderName (String senderId)
	{
		Client client = (Client) clientMap_.get( senderId );
		if ( client != null ) {
			Metadata meta = client.getMetadata();
			if ( meta != null && meta.getName() != null) {
				return meta.getName();
			}
		}
		return "unknown";
	}

	/**
	 * Logs a received message.
	 *
	 * @param   senderId  message sender public ID
	 * @param   msg   message object
	 * @param   msgId  message ID for call/response type messages
	 *                 (null for notify type messages)
	 */
	private void log( String senderId, Message msg, String msgId )
	throws IOException
	{
		String senderName = getSenderName(senderId);

		// Ignore all senders which DONT match the expected Id
		if (!listenOnlyToClientName.equals("") 
				&& !listenOnlyToClientName.equals(senderId)
				&& !listenOnlyToClientName.equals(senderName)
		)
		{
			return;
		}

		log (senderId, senderName, msg, msgId);
	}

	protected void log( String senderId, String senderName, Message msg, String msgId )
	throws IOException
	{
		StringBuffer sb = new StringBuffer("Event from senderId:").append(senderId)
		.append(" senderName:").append(senderName);
		writeOut (sb.toString());
		writeOut(newline_);
	}

	protected final void writeOut (byte[] msg) 
	throws IOException
	{
		logout.write(msg); 
	}

	protected final void writeOut (String msg) 
	throws IOException
	{
		logout.write(getBytes(msg)); 
	}

	public final String getClientName() { return clientName; }

	/**
	 * Returns the default metadata for the SimpleListeningClient client.
	 *
	 * @return  meta
	 */
	public static Metadata createDefaultMetadata() {
		Metadata meta = new Metadata();
		meta.setName( "SimpleListeningClient" );
		meta.setDescriptionText( "Listens in to messages"
				+ " for logging purposes" );
		try {
			meta.setIconUrl( UtilServer.getInstance()
					.exportResource( "/org/astrogrid/samp/images/ears.png" ).toString() );
		}
		catch ( IOException e ) {
			logger_.warning( "Can't export icon" );
		}
		meta.put( "Author", "VAO Testing Team (based heavily on work by Mark Taylor)" );
		return meta;
	}

	/**
	 * Main method.  Runs a client.
	 */
	public static void main( String[] args ) throws IOException {
		int status = runMain( new SimpleListeningClient(System.out), args );
		if ( status != 0 ) {
			System.exit( status );
		}
	}

	@Override
	public final boolean getUsage() { return this.help; }

	@Override
	public final boolean getVerbose() { return this.verbose; }

	@Override
	public final List<String> getSubscriptionList() { return this.subscriptionList; }

	/**
	 * Does the work for the main method.
	 * Use -help flag.
	 */
	public static int runMain( TestingClient client, String[] args ) 
	throws IOException 
	{

		CmdLineParser parser = new CmdLineParser(client);
		try {
			// Parse once to get the property file argument
			parser.parseArgument(args);

			if( client.getUsage())
			{
				parser.printUsage(System.out);
				System.exit(1);
			}

		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			System.exit(1);
		} 

		Subscriptions subs = new Subscriptions();
		if (client.getSubscriptionList().isEmpty())
		{ 
			System.err.println("USING DEFAULT SUBSCRIPTIONS");
			subs.addMType("*"); 
		}
		else
		{
			for (String subscription : client.getSubscriptionList()) { subs.addMType(subscription); }
		}
		client.configAndRun(createDefaultMetadata(), subs);

		// Adjust logging in accordance with verboseness flags.
	//	int logLevel = Level.SEVERE.intValue() + (200 * (client.getVerbose()? 1 : 0));
	//	Logger.getLogger( "org.usvao.samp.client" ).setLevel( Level.parse( Integer.toString( logLevel ) ) );
		Logger.getLogger( "org.astrogrid.samp" ).setLevel( Level.WARNING);
		
		// Wait indefinitely.
		Object lock = new String( "Forever" );
		synchronized( lock ) {
			try {
				lock.wait();
			}
			catch ( InterruptedException e ) {
			}
		}
		return 0;
	}

	private static byte[] getBytes (String str) 
	throws UnsupportedEncodingException
	{
		return str.getBytes(charset);
	}

}
