/*
 *  Copyright 2011 Smithsonian Astrophysical Observatory.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package cfa.vo.interop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.client.HubConnector;
import org.astrogrid.samp.client.MessageHandler;
import org.astrogrid.samp.client.SampException;

/**
 * This class implements a generic controller for SAMP connections.
 * 
 * In particular, given a client Name and Description it will register a new client
 * to the client.
 * 
 * The class also starts a thread that periodically checks the connection status.
 * 
 * Through this class one can add and remove both message handlers and connection listeners.
 *
 * A MessageHandler is invoked each time the Hub sends a new message for which
 * that MessageHandler has been registered.
 *
 * A SAMPConnectionListener, instead, is called each time the controller detects
 * a change in the connection status. The listeners can then perform operations
 * according to the new status. For example, they can update a connection status icon or
 * warn the user.
 *
 * Notice that client's do not need to specify the subscriptions. They are automatically
 * computed and communicated to the Hub when new MessageHandlers are added or removed.
 *
 * @author olaurino
 */
public class SAMPController {

    private String name;

    private HubConnector conn;

    private List<SAMPStatusListener> listeners = new ArrayList();

    /**
     *
     * Construct a new SAMPController, opens a connection to the SAMP Hub, or waits
     * for a SAMP Hub to start.
     *
     * @param name The Name to use for the new registered client
     * @param description The description string for the new client
     * @param iconUrl The location of the icon to show as associated to the new client.
     */
    public SAMPController(String name, String description, String iconUrl) {
        this.name = name;

        ClientProfile profile = DefaultClientProfile.getProfile();

        conn = new HubConnector(profile);

        Metadata meta = new Metadata();

        meta.setName(name);
        meta.setDescriptionText(description);

        conn.declareMetadata(meta);

        conn.declareSubscriptions(conn.computeSubscriptions());

        meta.setIconUrl(iconUrl);

        new Thread(new CheckConnection()).start();

    }

    /*
     * This implementation wraps the original JSAMP HubConnector class. this method allows
     * to retrieve a reference to the wrapped instance this controller is using.
     */
    public HubConnector getConn() {
        return conn;
    }

    /**
     *
     * Add a new MessageHandler to the controller. Notice that each time a client
     * adds an handler, the subscriptions are recalculated and sent to the Hub.
     *
     * @param handler
     * @throws SampException
     */
    public void addMessageHandler(MessageHandler handler) throws SampException {
        conn.addMessageHandler(handler);
        if(conn.isConnected())
            conn.declareSubscriptions(conn.computeSubscriptions());
    }

    /*
     * Remove a MessageHandler from the controller. Notice that each time a client
     * removes an handler, the subscriptions are recalculated and sent to the Hub.
     *
     */
    public void removeMessageHandler(MessageHandler handler) throws SampException {
        conn.removeMessageHandler(handler);
        if(conn.isConnected())
            conn.declareSubscriptions(conn.computeSubscriptions());
    }

    /*
     * Add a SAMPStatusListener to the Controller. SAMPStatusListeners get notified
     * each time the controller detects a connection status change.
     * 
     */
    public void addConnectionListener(SAMPStatusListener listener) {
        listeners.add(listener);
    }

    /*
     * Remove a SAMPStatusListener from the controller.
     */
    public void removeConnectionListener(SAMPStatusListener listener) {
        listeners.remove(listener);
    }

    private class CheckConnection implements Runnable {

        @Override
        public void run() {
            HubConnector conn = SAMPController.this.getConn();

            while(true) {

                boolean oldState = conn.isConnected();

                if(!conn.isConnected()) {
                    conn.setActive(true);
                    conn.declareSubscriptions(conn.computeSubscriptions());
                }

                boolean stateChanged = oldState != conn.isConnected();

                if(stateChanged)
                    try {
                        conn.getConnection().notifyAll(new Message("updated status for "+name));
                    } catch (SampException ex) {
                        Logger.getLogger(SAMPController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                for(SAMPStatusListener listener : listeners) {
                    listener.run(conn.isConnected());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SAMPController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

}
