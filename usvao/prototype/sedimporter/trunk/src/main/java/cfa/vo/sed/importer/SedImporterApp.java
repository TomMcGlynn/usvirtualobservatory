/**
 * Copyright (C) 2011 Smithsonian Astrophysical Observatory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * IrisImporterApp.java
 */

package cfa.vo.sed.importer;

import cfa.vo.interop.SAMPConnectionListener;
import cfa.vo.interop.SedSAMPController;
import cfa.vo.sed.filters.FileFormatManager;
import cfa.vo.sed.setup.SetupManager;
import cfa.vo.sed.setup.ISetup;
import cfa.vo.sed.gui.MainView;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.sedlib.common.SedInconsistentException;
import cfa.vo.sedlib.common.SedNoDataException;
import cfa.vo.sedlib.io.SedFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.astrogrid.samp.client.MessageHandler;
import org.astrogrid.samp.client.SampException;
import org.jdesktop.application.Application;

/**
 * The main class of the application.
 */
public class SedImporterApp extends Application {

    public static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    public static final File CONFIGURATION_DIR = new File(System.getProperty("user.home")+"/.vao/iris/importer/");

    private static SedSAMPController sampController;

    private static boolean isTest = false;

    private static boolean SAMP_ENABLED = System.getProperty("samp", "true").toLowerCase().equals("false") ? false : true;

    public static final boolean READ_COMPLIANT_FILES = System.getProperty("readCompliant", "true").toLowerCase().equals("false") ? false : true;

    public static final boolean SAMP_FALLBACK = false;

    protected boolean isBatch;

    protected List<ISetup> confList;

    protected File outputFile;

    protected SedFormat format;

    /**
     * Startup Code
     */
    @Override protected void startup() {

        if(!CONFIGURATION_DIR.exists()) {
            CONFIGURATION_DIR.mkdirs();
        }

        FileFormatManager.getInstance().init();

        if(isBatch) {
            List<Segment> segments = null;
            try {
                System.out.println();
                System.out.println("Building segments...");
                System.out.println();
                segments = SegmentImporter.getSegments(confList);
            } catch (Exception ex) {
                System.err.println("Error while building segments: "+ex.getMessage());
                exit();
            }
            System.out.println();
            System.out.println("Building SED...");
            Sed sed = new Sed();
            try {
                try {
                    sed.addSegment(segments);
                } catch (SedNoDataException ex) {
                    Logger.getLogger(SedImporterApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SedInconsistentException ex) {
                System.err.println("Error: segments are inconsistent: "+ ex.getMessage());
                exit();
            }
            try {
                System.out.println();
                System.out.println("Writing SED to "+outputFile.getAbsolutePath()+"...");
                sed.write(new FileOutputStream(outputFile), format);
                System.out.println();
                System.out.println("DONE.");
            } catch (Exception ex) {
                System.err.println("Error while serializing SED: "+ex.getMessage());
            }

        } else {
            if(MAC_OS_X)
                System.setProperty("apple.laf.useScreenMenuBar", "true");

            System.out.println("Launching GUI...");

            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Logger.getLogger("").setLevel(Level.SEVERE);
                    sampSetup();
                    MainView.getInstance().setVisible(true);
                }
            });
            
        }
    }

    public static void sampSetup() {
        if(SAMP_ENABLED) {
            sampController = new SedSAMPController("SedImporter", "An SED builder from the Virtual Astronomical Observatory", SedImporterApp.class.getResource("/iris_button_tiny.png").toString());

            try {
                sampController.startWithResourceServer("sedImporter/", !isTest);
            } catch (Exception ex) {
                System.err.println("SAMP Error. Disabling SAMP support.");
                System.err.println("Error message: "+ex.getMessage());
                SAMP_ENABLED = false;
            }
        }
    }

    public static void sampShutdown() {
        if(sampController!=null) {
            Logger.getLogger(SedSAMPController.class.getName()).log(Level.INFO, "Shutting down SAMP");
            sampController.stop();
        }
    }

    public static void addConnectionListener(SAMPConnectionListener listener) {
        sampController.addConnectionListener(listener);
    }

    public static void addMessageHandler(MessageHandler handler) {
        sampController.addMessageHandler(handler);
    }

    public static void setAutoRunHub(boolean autoRunHub) {
        sampController.setAutoRunHub(autoRunHub);
    }

    public static void sendSedMessage(Sed sed, String sedId) throws SampException {
        sampController.sendSedMessage(sed, sedId);
    }

    /**
     * At startup parse command line options and decide whether to start in batch mode or as a gui.
     */
    @Override protected void initialize(String[] args) {
        if(args.length > 0) {
            if(args.length < 2) {
                System.err.println("Usage: sedimporter config_file output_file [output_format].");
                exit();
            } else {
                String formatS = args.length==2 ? "VOT" : args[2];
                try {
                    URL url;
                    if(args[0].contains("://")) {
                        url = new URL(args[0]);
                    } else {
                        File f = new File(args[0]);
                        url = new URL("file://"+f.getAbsolutePath());
                    }
                    confList = SetupManager.read(url);
                } catch (IOException ex) {
                    System.err.println("Error reading file "+args[0]+": "+ex.getMessage());
                    exit();
                } catch (Exception ex) {
                    System.err.println("Generic error reading file "+args[0]+": "+ex.getMessage());
                    exit();
                }
                try {
                    outputFile = new File(args[1]);
                    if(outputFile.exists() && !outputFile.canWrite()) {
                        System.err.println("Error: file "+args[1]+" is not writable.");
                        exit();
                    }
                } catch (Exception ex) {
                    System.err.println("Error opening file "+args[1]+": "+ex.getMessage());
                    exit();
                }
                try {
                    format = SedFormat.valueOf(formatS.toUpperCase());
                    isBatch = true;
                } catch (Exception ex) {
                    System.err.println("No such a format: "+formatS+". Please use 'vot' or 'fits'.");
                    exit();
                }
            }
        }
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of IrisImporterApp
     */
    public static SedImporterApp getInstance() {
        return Application.getInstance(SedImporterApp.class);
    }

    public static boolean isSampEnabled() {
        return SAMP_ENABLED;
    }

    public static void setTest(boolean isTest) {
        SedImporterApp.isTest=isTest;
    }

    public static void exitApp() {
        sampShutdown();
        System.exit(0);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SedImporterApp.class, args);
    }
}