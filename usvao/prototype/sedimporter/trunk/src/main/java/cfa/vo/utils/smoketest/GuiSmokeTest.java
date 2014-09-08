package cfa.vo.utils.smoketest;

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

import cfa.vo.interop.AbstractSedMessageHandler;
import cfa.vo.interop.SAMPController;
import cfa.vo.sed.filters.NativeFileFormat;
import cfa.vo.sed.gui.NewSedFrame;
import cfa.vo.sed.gui.SetupFrame;
import cfa.vo.sed.importer.ISegmentMetadata;
import cfa.vo.sed.importer.SedImporterApp;
import cfa.vo.sed.importer.SegmentImporter;
import cfa.vo.sedlib.Sed;
import cfa.vo.sherpa.AbstractModel;
import cfa.vo.sherpa.CompositeModel;
import cfa.vo.sherpa.Data;
import cfa.vo.sherpa.FitResults;
import cfa.vo.sherpa.Method;
import cfa.vo.sherpa.Models;
import cfa.vo.sherpa.OptimizationMethod;
import cfa.vo.sherpa.SherpaClient;
import cfa.vo.sherpa.Stat;
import cfa.vo.sherpa.Stats;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.uispec4j.Button;
import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

/**
 *
 * @author olaurino
 */
public class GuiSmokeTest extends SmokeTest {

    private URL fileURL;

    private Sed sed;

    private String sedId;

    private String sherpaDirS;

    private SAMPController controller;

    private SherpaClient c;

    public GuiSmokeTest(String sherpaDir, URL url, int timeout) {
        super(timeout);
        this.sherpaDirS = sherpaDir;
        this.fileURL = url;
    }

    public GuiSmokeTest(String sherpaDir, URL url) {
        this(sherpaDir, url, 5);
    }

    @Override
     public void runTest() {
        try {

            Logger.getLogger("").setLevel(Level.SEVERE);

            log("\n\n\nThis test will assess whether your installation is capable of performing the basic"
                    + " operations of the SED tool.\n"
                    + "The test should take less than a minute. However, the actual time will depend on your system properties.\n\n");

            log("\n\n========================================");
            log("Starting Smoke Test with timeout: "+TIMEOUT);
            log("========================================");

            log("Starting SedImporter");
            SedImporterApp.setTest(true);

            SedImporterApp.sampSetup();

            log("Sleeping to wait for the SedImporter to settle down");
            Thread.sleep(5000);

            log("Starting the SAMP agent");
            controller = new SAMPController("TestController", "Test SAMP Controller", this.getClass().getResource("/iris_button_tiny.png").toString());

            SedHandler sh = new SedHandler();

            controller.addMessageHandler(sh);

            controller.start(false);

            log("Starting Sherpa");
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


            c = new SherpaClient(controller);

            c.startSherpa(sherpaDirS);

            log("Emulating user GUI interaction for importing a file");
            NewSedFrame sedFrame = new NewSedFrame("testsed");

            //Test sedFrame name resolver
            Window newSedWindow = new Window(sedFrame);

            TextBox targetName = newSedWindow.getInputTextBox("targetName");
            targetName.setText("3c273");

            ISegmentMetadata metadata = SegmentImporter.getSegmentsMetadata(fileURL, NativeFileFormat.VOTABLE).get(0);

            SetupFrame configurationFrame = new SetupFrame("test segment", metadata, sedFrame, fileURL.toString(), NativeFileFormat.VOTABLE, 0);

            Window configurationWindow = new Window(configurationFrame);

            //Test target information
            targetName = configurationWindow.getInputTextBox("targetName");

            ComboBox xColumnCombo = configurationWindow.getComboBox("xColumn");
            ComboBox xQuantityCombo = configurationWindow.getComboBox("xQuantity");
            ComboBox xUnitsCombo = configurationWindow.getComboBox("xUnits");

            ComboBox yColumnCombo = configurationWindow.getComboBox("yColumn");
            ComboBox yQuantityCombo = configurationWindow.getComboBox("yQuantity");
            ComboBox yUnitsCombo = configurationWindow.getComboBox("yUnits");


            //Select options
            xColumnCombo.select("DataSpectralValue");
            xQuantityCombo.select("FREQUENCY");
            xUnitsCombo.select("Hz");

            yColumnCombo.select("DataFluxValue");
            yQuantityCombo.select("FLUXDENSITY");
            yUnitsCombo.select("Jy");

            //Test symmetric column
            configurationWindow.getRadioButton("symmetricColumn").click();
            configurationWindow.getComboBox("symmetricColumnValue").select("DataFluxStatErr");

            Button importButton = configurationWindow.getButton("Add Segment to SED");

            importButton.click();

            log("SED imported. Waiting for the broadcast button to be enabled.");
            Button broadcast = newSedWindow.getButton("Broadcast SED");

            this.waitUntil(broadcast.isEnabled(), "isTrue", "Broadcast button wasn't enabled, something is wrong...");
            this.waitUntil(controller, "isConnected", "Sherpa client controller never connected");

            log("Sleeping to allow SAMP agents to settle down.");
            Thread.sleep(5000);

            log("Broadcasting SED.");
            broadcast.click();

            log("Waiting for the SED to be received through SAMP.");
            this.waitUntilNotNull("sed", "The Sed wasn't received before the timeout. You could retry with a longer timeout.");
            this.waitUntilNotNull("sedId", "The Sed wasn't received before the timeout. You could retry with a longer timeout.");

            log("SED received, building fit configuration.");
            Data data = c.createData(sedId);

            data.setX(sed.getSegment(0).getSpectralAxisValues());
            data.setY(sed.getSegment(0).getFluxAxisValues());
            data.setStatError((double[]) sed.getSegment(0).getCustomDataValues("Spectrum.Data.FluxAxis.Accuracy.StatError"));

            AbstractModel m1 = c.createModel(Models.PowerLaw1D);

            c.getParameter(m1, "ref").setVal(5000.);
            c.getParameter(m1, "ampl").setVal(1.0);
            c.getParameter(m1, "gamma").setVal(-0.5);

            CompositeModel cm = c.createCompositeModel("m1", m1);

            Stat s = Stats.LeastSquares;

            Method method = c.getMethod(OptimizationMethod.NelderMeadSimplex);

            log("Using Sherpa to perform the fit.");
            FitResults fr = c.fit(data, cm, s, method);

            log("Parsing Sherpa response");
            Assert.assertEquals(Boolean.TRUE, fr.getSucceeded());

            log("==============");
            log("Test Completed");
            log("==============");

            log("Everything seems to be working!");

        } catch(Exception ex) {
            Logger.getLogger(GuiSmokeTest.class.getName()).log(Level.SEVERE, null, ex);
            
            log("Something went wrong. If a timeout occurred, try re-running the test with a longer timeout (e.g. ./smoketest 20).");
            log("If the Smoke Test is not working, your system may not be supported, or you have downloaded"
                    + " a distribution that does not match your Operating System.");
            log("\n\nChecking Architecture");
            try {
                checkArch();
            } catch (Exception ex1) {
                Logger.getLogger(GuiSmokeTest.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Assert.fail();
        } finally {
            exit();
        }
     }

    @Override
    protected void exit() {
        if(c!=null) {
                c.stopSherpa();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                
            }

            SedImporterApp.exitApp();
    }

    private void checkArch() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("file", sherpaDirS+"/bin/python2.6");

        Process p = pb.start();

        InputStream is = p.getInputStream();

        p.waitFor();

        String s = new Scanner(is).useDelimiter("\\A").next().toLowerCase();

        String arch = System.getProperty("os.arch").toLowerCase();

        if(arch.equals("amd64"))
            arch = "x86_64";

        String os = System.getProperty("os.name").toLowerCase();

        if(os.equals("mac os x"))
            os = "mach-o";

        if(s.contains(os)) {
            if(!(s.contains(arch) || s.contains(arch.replaceAll("_", "-"))))
                Logger.getLogger("").log(Level.SEVERE, "\nIris may be installed for the wrong architecture. However, Iris could still work, so the test will continue...");
        } else {
            Logger.getLogger("").log(Level.SEVERE, "\nIt seems like you installed Iris for the wrong Operating System. However, the test will continue, since there is the unlikely possibility"
                    + " that Iris will work anyway or that there was an error probing the running operating system.");
        }

        log("\nOperating system: "+os);
        log("Architecture: "+arch);
        log("Python Executable: "+s);

    }

    private class SedHandler extends AbstractSedMessageHandler {

        @Override
        public void processSed(Sed sed, String sedId) {
            GuiSmokeTest.this.sed = sed;
            GuiSmokeTest.this.sedId = sedId;
        }

    }

}