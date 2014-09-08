/**
 * Copyright (C) Smithsonian Astrophysical Observatory
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cfa.vo.sed.gui;

import cfa.vo.sed.setup.SetupManager;
import cfa.vo.sed.setup.ISetup;
import cfa.vo.sed.filters.NativeFileFormat;
import cfa.vo.sed.importer.ISegmentColumn;
import cfa.vo.sed.importer.ISegmentMetadata;
import cfa.vo.sed.importer.SegmentImporter;
import cfa.vo.sed.test.Oracle;
import cfa.vo.sed.test.URLTestConverter;
import java.net.URL;
import java.util.List;
import junit.framework.Assert;
import org.uispec4j.ComboBox;
import org.uispec4j.RadioButton;
import org.uispec4j.Window;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

/**
 *
 * @author olaurino
 */
public class ConfigurationFrameTest extends UISpecTestCase {

    private Window newSedWindow;
    private Window configurationWindow;

    public ConfigurationFrameTest(){
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        newSedWindow = null;
        configurationWindow = null;
    }

     @Test
     public void testNewSegment() throws Exception {

         Oracle oracle = new Oracle();

         NewSedFrame sedFrame = new NewSedFrame("test sed");

         //Test sedFrame name resolver
         newSedWindow = new Window(sedFrame);

         TextBox targetName = newSedWindow.getInputTextBox("targetName");
         targetName.setText("3c273");

         TextBox targetRa = newSedWindow.getInputTextBox("targetRa");
         TextBox targetDec = newSedWindow.getInputTextBox("targetDec");

         Button resolveButton = newSedWindow.getButton("Resolve");
         resolveButton.click();

         Assert.assertEquals(targetRa.getText().split("\\.")[0], "187");
         Assert.assertEquals(targetDec.getText().split("\\.")[0], "2");

         //Test configuration window creation
         URL fileURL = URLTestConverter.getURL("test:///test_data/3c273.csv");

         ISegmentMetadata metadata = SegmentImporter.getSegmentsMetadata(fileURL, NativeFileFormat.CSV).get(0);
         
         SetupFrame configurationFrame = new SetupFrame("test segment", metadata, sedFrame, fileURL.getFile(), NativeFileFormat.CSV);

         configurationWindow = new Window(configurationFrame);

         //Test target information
         targetRa = configurationWindow.getInputTextBox("targetRa");
         targetDec = configurationWindow.getInputTextBox("targetDec");
         targetName = configurationWindow.getInputTextBox("targetName");

         Assert.assertEquals(targetRa.getText().split("\\.")[0], "187");
         Assert.assertEquals(targetDec.getText().split("\\.")[0], "2");
         Assert.assertEquals(targetName.getText(), "3c273");

         //Test name resolver
         resolveButton = configurationWindow.getButton("Resolve");
         targetName.setText("m1");
         resolveButton.click();

         Assert.assertEquals(targetRa.getText().split("\\.")[0], "83");
         Assert.assertEquals(targetDec.getText().split("\\.")[0], "22");

         oracle.put("targetName", "m1");
//         oracle.put("targetRa", "83.633125");

         //Test Initial Validation messages
         TextBox validation = configurationWindow.getTextBox("validation");
         Assert.assertTrue(validation.getText().contains("Please enter a valid quantity for X values (current quantity: null)"));
         Assert.assertTrue(validation.getText().contains("Please enter a valid quantity for Y values (current quantity: null)"));
         Assert.assertTrue(validation.getText().contains("Please choose a Y Error type."));
        
        ComboBox xColumnCombo = configurationWindow.getComboBox("xColumn");
        ComboBox xQuantityCombo = configurationWindow.getComboBox("xQuantity");
        ComboBox xUnitsCombo = configurationWindow.getComboBox("xUnits");

        ComboBox yColumnCombo = configurationWindow.getComboBox("yColumn");
        ComboBox yQuantityCombo = configurationWindow.getComboBox("yQuantity");
        ComboBox yUnitsCombo = configurationWindow.getComboBox("yUnits");

        //Test Column contents
        ISegmentColumn xColumn0 = (ISegmentColumn) xColumnCombo.getAwtComponent().getItemAt(0);
        ISegmentColumn xColumn7 = (ISegmentColumn) xColumnCombo.getAwtComponent().getItemAt(7);
        Assert.assertEquals("DataPointNumber", xColumn0.getName());
        Assert.assertEquals("DataFluxStatErr", xColumn7.getName());
        ISegmentColumn yColumn0 = (ISegmentColumn) yColumnCombo.getAwtComponent().getItemAt(0);
        ISegmentColumn yColumn7 = (ISegmentColumn) yColumnCombo.getAwtComponent().getItemAt(7);
        Assert.assertEquals("DataPointNumber", yColumn0.getName());
        Assert.assertEquals("DataFluxStatErr", yColumn7.getName());
        
        //Select options
        xColumnCombo.select("DataSpectralValue");
        oracle.put("xAxisColumnNumber", 5);
        xQuantityCombo.select("FREQUENCY");
        oracle.put("xAxisQuantity", "FREQUENCY");
        Assert.assertFalse(validation.getText().contains("Please enter a valid quantity for X values (current quantity: null)"));
        xUnitsCombo.select("Hz");
        oracle.put("xAxisUnit", "HERTZ");

        yColumnCombo.select("DataFluxValue");
        oracle.put("yAxisColumnNumber", 6);
        yQuantityCombo.select("SPVFLUXDENSITY");
        oracle.put("yAxisQuantity", "SPVFLUXDENSITY");
        Assert.assertFalse(validation.getText().contains("Please enter a valid quantity for Y values (current quantity: null)"));
        yUnitsCombo.select("erg/s/cm2/Hz");
        oracle.put("yAxisUnit", "FLUXDENSITYFREQ0");


        RadioButton constantValue = configurationWindow.getRadioButton("constantValue");
        constantValue.click();

        Assert.assertTrue(validation.getText().contains("The ErrorType is ConstantValue but no value has been provided."));
        Assert.assertTrue(validation.getText().contains("Invalid ConstantErrorValue"));

        TextBox constantValueValue = configurationWindow.getInputTextBox("constantValueValue");

        //Test wrong constant value
        constantValueValue.setText("pippo");
        Assert.assertTrue(validation.getText().contains("Invalid ConstantErrorValue"));

        //Test correct constant value
        constantValueValue.setText("2.0");
        Assert.assertTrue(validation.getText().isEmpty());

        //Test symmetric column
        configurationWindow.getRadioButton("symmetricColumn").click();
        configurationWindow.getComboBox("symmetricColumnValue").select("DataFluxStatErr");
        Assert.assertTrue(validation.getText().isEmpty());

        //Test symmetric parameter
//        RadioButton symmetricParameter = configurationWindow.getRadioButton("symmetricParameter");
//        symmetricParameter.click();
//        configurationWindow.getComboBox("symmetricParameterValue").select("DataFluxStatErr");
//        Assert.assertTrue(validation.getText().isEmpty());

        //Test configuration saving
        URL outUrl = getClass().getResource("/test_data/");
        final String outfile = outUrl.getFile()+"test.ini";

        WindowInterceptor inter = WindowInterceptor
            .init(configurationWindow.getButton("Save").triggerClick());

            inter.process(new WindowHandler() {
                @Override
                public Trigger process(Window window) throws Exception {
                    WindowInterceptor
                            .init(window.getButton("Browse...").triggerClick())
                            .process(FileChooserHandler.init()
                                .assertAcceptsFilesOnly()
                                .select(outfile)
                                )
                            .run();


                    return window.getButton("Save").triggerClick();

                    }
                })
            .run();

         outUrl = getClass().getResource("/test_data/test.ini");
         List<ISetup> confs = SetupManager.read(outUrl);

         oracle.test(confs.get(0));



     }

}