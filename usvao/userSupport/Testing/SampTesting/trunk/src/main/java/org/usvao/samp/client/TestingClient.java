/**
 * 
 */
package org.usvao.samp.client;

import java.util.List;

import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.Subscriptions;

/**
 * @author thomas
 *
 */
public interface TestingClient 
{
	
	public void configAndRun ( Metadata meta, final Subscriptions subs); 
	
	public boolean getUsage();
	
	public boolean getVerbose();
	
	public List<String> getSubscriptionList();
	
}
