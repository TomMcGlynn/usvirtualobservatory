/*
 *  Copyright 2011 olaurino.
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

package cfa.vo.utils.smoketest;

import cfa.vo.interop.AbstractSedMessageHandler;
import cfa.vo.interop.PingMessage;
import cfa.vo.interop.SedSAMPController;
import cfa.vo.sed.importer.SegmentImporter;
import cfa.vo.sed.setup.ErrorType;
import cfa.vo.sed.setup.SetupBean;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.sherpa.AbstractModel;
import cfa.vo.sherpa.CompositeModel;
import cfa.vo.sherpa.Data;
import cfa.vo.sherpa.FitResults;
import cfa.vo.sherpa.Method;
import cfa.vo.sherpa.Models;
import cfa.vo.sherpa.OptimizationMethod;
import cfa.vo.sherpa.SherpaClient;
import cfa.vo.sherpa.SherpaService;
import cfa.vo.sherpa.Stat;
import cfa.vo.sherpa.Stats;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.astrogrid.samp.Client;
import org.astrogrid.samp.Response;
import org.astrogrid.samp.client.ResultHandler;

/**
 *
 * @author olaurino
 */
public class NoGuiSmokeTest extends SmokeTest {

    private String sherpaDirS;

    private String testVotable;

    private SedSAMPController controller;

    private boolean working=false;

    private Boolean control;

    private SherpaService sherpa;

    public NoGuiSmokeTest(String sherpaDir, String testVotable) {
        this(sherpaDir, testVotable, 5);
    }

    public NoGuiSmokeTest(String sherpaDir, String testVotable, int timeout) {

        super(timeout);

        this.sherpaDirS = sherpaDir;
        this.testVotable = testVotable;
//        Logger.getLogger("").setLevel(Level.OFF);
    }

    @Override
    public void runTest() throws Exception {
        try {

            File sherpaDir = new File(sherpaDirS);

            //Verify sherpaDir is an existing directory
            check(sherpaDir.isDirectory(), sherpaDirS+" is not a directory");

            boolean isSherpaDir = false;

            //Verify sherpaDir contains sherpa
            for(File f : sherpaDir.listFiles()) {
                if(f.getName().equals("startsherpa.py")) {
                    isSherpaDir = true;
                }
            }

            check(isSherpaDir, "The directory does not contain Sherpa");

            //Verify we can read test file.
            File testFile = new File(testVotable);
            check(testFile.canRead(), "Can't read file "+testVotable);

            //Start a SAMPController
            controller = new SedSAMPController("TestController", "An SED builder from the Virtual Astronomical Observatory", this.getClass().getResource("/iris_button_tiny.png").toString());
            controller.startWithResourceServer("/test", false);

            log("Waiting for the SAMP controller...");
            waitUntil(controller, "isConnected", "SAMP controller never connected!");

            //Start Sherpa
            log("Starting Sherpa");
            sherpa = new SherpaService(sherpaDirS, 10);
            sherpa.start();

            Thread.sleep(10000);//sherpa needs some time to connect to the hub

            //check that sherpa can be pinged
            control = Boolean.FALSE;
            log("Pinging Sherpa...");
            controller.sendMessage(new PingMessage(), new PingResultHandler(), 10);
            waitUntil("control", true, "Sherpa didn't respond to ping");//give sherpa TIMEOUT seconds to reply

            //Import the file using SedImporter
            log("Creating a Setup for the SedImporter");
            SetupBean conf = createSetupBean(testFile);

            log("Importing the file");
            List<Segment> segments = SegmentImporter.getSegments(conf);

            Sed sed = new Sed();
            sed.addSegment(segments);

            //Setup a client that handles SEDs
            log("Setting up a SAMP SED receiver");
            SedSAMPController mockReceiver = new SedSAMPController("MockReceiver", "An SED builder from the Virtual Astronomical Observatory", this.getClass().getResource("/iris_button_tiny.png").toString());

            mockReceiver.start(false);
            mockReceiver.setAutoRunHub(false);

            log("Waiting for the SAMP SED receiver...");
            waitUntil(mockReceiver, "isConnected", "SAMP SED receiver never connected!");

            mockReceiver.addMessageHandler(new SmokeSedHandler());

            control = Boolean.FALSE;

            //Send the Sed
            log("Broadcasting the SED");
            controller.sendSedMessage(sed, "testSed");

            waitUntil("control", Boolean.TRUE, "It looks like the SED wasn't processed");//give the receiver TIMEOUT seconds to reply

            double[] x = sed.getSegment(0).getSpectralAxisValues();
            double[] y = sed.getSegment(0).getFluxAxisValues();

            double[] err = (double[]) sed.getSegment(0).getCustomDataValues("Spectrum.Data.FluxAxis.Accuracy.StatError");

            SherpaClient c = new SherpaClient(controller);

            Data data = c.createData("test");

            data.setX(x);
            data.setY(y);
            data.setStatError(err);

            AbstractModel m1 = c.createModel(Models.PowerLaw1D);

            c.getParameter(m1, "ref").setVal(5000.);
            c.getParameter(m1, "ampl").setVal(1.0);
            c.getParameter(m1, "gamma").setVal(-0.5);

            CompositeModel cm = c.createCompositeModel("m1", m1);

            Stat s = Stats.LeastSquares;

            Method method = c.getMethod(OptimizationMethod.NelderMeadSimplex);

            FitResults fr = c.fit(data, cm, s, method);

            Assert.assertEquals(Boolean.TRUE, fr.getSucceeded());

            working = true;

        } finally {
            exit();
        }
        
    }

    @Override
    protected void exit() {
        if(sherpa!=null)
            try {
            sherpa.stop();
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(NoGuiSmokeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NoGuiSmokeTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(controller!=null)
            controller.stop();
        
        String message = working ? "Everything seems to be working!" : "OOPS! Something went wrong!";

        System.out.println(); System.out.println("===============================");
        System.out.println(message);
        System.out.println("===============================");

        System.exit(0);
    }

    private SetupBean createSetupBean(File testFile) {
        SetupBean conf = new SetupBean();

        conf.setErrorType(ErrorType.SymmetricColumn.name());
        conf.setSymmetricErrorColumnNumber(7);
        conf.setFileLocation("file://"+testFile.getAbsolutePath());
        conf.setFormatName("VOTABLE");
        conf.setPublisher("NED");
        conf.setTargetName("3c273");
        conf.setTargetRa("187.27791798");
        conf.setTargetDec("2.05238729");
        conf.setXAxisColumnNumber(5);
        conf.setYAxisColumnNumber(6);
        conf.setXAxisQuantity("FREQUENCY");
        conf.setXAxisUnit("HERTZ");
        conf.setYAxisQuantity("SPVFLUXDENSITY");
        conf.setYAxisUnit("FLUXDENSITYFREQ0");

        return conf;
    }

    private class PingResultHandler implements ResultHandler {

        @Override
        public void result(Client client, Response rspns) {
            log(client.getMetadata().getName()+" response status: " +rspns.getStatus());
            if(client.getMetadata().getName().toLowerCase().equals("sherpa"))
                control = Boolean.TRUE;
        }

        @Override
        public void done() {
        }

    }

    private class SmokeSedHandler extends AbstractSedMessageHandler {

        @Override
        public void processSed(Sed sed, String sedId) {
            check(sed.getNumberOfSegments()==1, "Wrong Number of Segments in the sed");
            log("Sed received, and it looks good!");
            control = Boolean.TRUE;
        }

    }
}
