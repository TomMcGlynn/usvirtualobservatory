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
 *
 * @author olaurino
 */
public class SAMPController {

    private String name;

    private HubConnector conn;

    private List<SAMPStatusListener> listeners = new ArrayList();

    public SAMPController(String name, String description) {
        this.name = name;

        ClientProfile profile = DefaultClientProfile.getProfile();

        conn = new HubConnector(profile);

        Metadata meta = new Metadata();

        meta.setName(name);
        meta.setDescriptionText(description);

        conn.declareMetadata(meta);

        conn.declareSubscriptions(conn.computeSubscriptions());

        meta.setIconUrl(getClass().getResource("/iris_button_tiny.png").toString());

        new Thread(new CheckConnection()).start();

    }

    public HubConnector getConn() {
        return conn;
    }

    public void addMessageHandler(MessageHandler handler) throws SampException {
        conn.addMessageHandler(handler);
        if(conn.isConnected())
            conn.declareSubscriptions(conn.computeSubscriptions());
    }

    public void addConnectionListener(SAMPStatusListener listener) {
        listeners.add(listener);
    }

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
