package org.usvao.samp.client;

import java.io.IOException;
import java.io.OutputStream;

import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.SampUtils;
import org.astrogrid.samp.httpd.UtilServer;

/** A client designed to listen to traffic from one or more other clients involved
 * in samp testing. Similar to the Snooper class in JSamp, but implements 2 mtypes
 * which allow a client to declare testing started, and then formats all of the
 * event output in JSON for easy test reporting later.
 * 
 * @author thomas
 *
 */
public class TestLoggingClient 
extends SimpleListeningClient
{

	private static final String INDENT = "  ";
	
	public TestLoggingClient(OutputStream logto) {
		super(logto);
	}
	
	private boolean isInTest = false;
	private boolean hasNoEventsYet = true;
	
	@Override
	public void log( String senderId, String clientName, Message msg, String msgId )
	throws IOException 
	{
		
		if (msg.getMType().equals("testing.start")) {
			writeOut("{ tests : [");
			writeOut( newline_ );
			return;
		}
		
		if (msg.getMType().equals("testing.end")) {
			writeOut("] }");
			writeOut( newline_ );
			return;
		}
		
		if (msg.getMType().equals("test.end")) {
			writeOut("  ] } }");
			writeOut( newline_ );
			writeOut(",");
			writeOut( newline_ );
			isInTest = false;
			hasNoEventsYet = true;
			return;
		}
		
		if (isInTest)
		{
			if (hasNoEventsYet) { 
				hasNoEventsYet = false;
			} else { 
				writeOut(","); writeOut( newline_ ); 
			}
		}
		
		if (msg.getMType().equals("test.start")) {
			writeOut(new StringBuffer()
					.append(INDENT)
			        .append("{ test : {")
					.append(INDENT).append(INDENT)
			        .append("testname : \"").append(msg.getParam("name")).append("\",")
					.toString());
			writeOut( newline_ );
			writeOut (new StringBuffer(INDENT).append(INDENT).append("events : [").toString());
			writeOut( newline_ );
			isInTest = true;
			hasNoEventsYet = true;
			return;
		}
		
		StringBuffer sbuf = new StringBuffer()
			.append(INDENT).append(INDENT).append(INDENT)
			.append(" { event : { clientid : \"") 
			.append( senderId )
			.append( "\", ");
		
		sbuf.append( "clientname :\"" )
						.append( clientName )
						.append( "\", " );
		
		sbuf.append( " type : " ); 
		if ( msgId == null ) {
			sbuf.append( "\"notify\", " );
		}
		else {
			sbuf.append( "\"call\", " )
			.append( " msgId : \"" )
			.append( msgId )
			.append( "\", " );
		}
		writeOut( newline_ );
		writeOut( sbuf.toString());
		writeOut( newline_ );
		writeOut (new StringBuffer(INDENT).append(INDENT).append(INDENT).append("body :").toString()); 
		writeOut( newline_ );
		writeOut( SampUtils.formatObject( msg, 3 ));
		writeOut( new StringBuffer(INDENT).append(INDENT).append(INDENT).append(" } }").toString());
		writeOut( newline_ );
	}

	public static Metadata createDefaultMetadata() {
		Metadata meta = new Metadata();
		meta.setName("TestLoggingClient");
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
		TestingClient client = new TestLoggingClient(System.out); 
		int status = runMain( client, args );
		if ( status != 0 ) {
			System.exit( status );
		}
	}
	
	
}
