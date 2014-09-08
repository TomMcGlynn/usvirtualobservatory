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

package cfa.vo.sherpa;

import cfa.vo.interop.SAMPFactory;
import cfa.vo.interop.SAMPController;
import cfa.vo.interop.SAMPMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author olaurino
 */
public class SherpaClientTest {

    public SherpaClientTest() {
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
    }

     @Test
     public void modelTest() throws Exception {
        SherpaClient c = new SherpaClient(null);

        Model m = c.createModel(Models.PowerLaw1D);
        Model m2 = c.createModel(Models.PowerLaw1D);
        Assert.assertEquals("powlaw1d.m1", m.getName());
        Assert.assertEquals("powlaw1d.m2", m2.getName());

        Parameter p = m.getPars().get(0);
        Assert.assertEquals("m1.gamma", p.getName());
        p = m.getPars().get(1);
        Assert.assertEquals("m1.ampl", p.getName());
        p = m.getPars().get(2);
        Assert.assertEquals("m1.ref", p.getName());

        p = m2.getPars().get(0);
        Assert.assertEquals("m2.gamma", p.getName());
        p = m2.getPars().get(1);
        Assert.assertEquals("m2.ampl", p.getName());
        p = m2.getPars().get(2);
        Assert.assertEquals("m2.ref", p.getName());

        Model m3 = c.createModel(Models.Gaussian1D);
        Assert.assertEquals("gauss1d.m3", m3.getName());

        p = m3.getPars().get(0);
        Assert.assertEquals("m3.fwhm", p.getName());
        p = m3.getPars().get(1);
        Assert.assertEquals("m3.pos", p.getName());
        p = m3.getPars().get(2);
        Assert.assertEquals("m3.ampl", p.getName());

        CompositeModel cm = (CompositeModel) SAMPFactory.get(CompositeModel.class);

        cm.addPart(m);
        cm.addPart(m2);
        cm.addPart(m3);

        cm.setName("m1*m2+m3");

        SAMPMessage message = SAMPFactory.createMessage("load.table.votable", cm, CompositeModel.class);

        message.get().check();

        cm = (CompositeModel) SAMPFactory.get(message, CompositeModel.class);

        m = cm.getParts().get(0);

        m2 = cm.getParts().get(1);

        m3 = cm.getParts().get(2);

        Assert.assertEquals("m1*m2+m3", cm.getName());

        p = m3.getPars().get(0);
        Assert.assertEquals("m3.fwhm", p.getName());
        p = m3.getPars().get(1);
        Assert.assertEquals("m3.pos", p.getName());
        p = m3.getPars().get(2);
        Assert.assertEquals("m3.ampl", p.getName());

        cm = c.createCompositeModel("m1*m2+m3", m, m2, m3);

        m = cm.getParts().get(0);

        m2 = cm.getParts().get(1);

        m3 = cm.getParts().get(2);

        Assert.assertEquals("m1*m2+m3", cm.getName());

        p = m3.getPars().get(0);
        Assert.assertEquals("m3.fwhm", p.getName());
        p = m3.getPars().get(1);
        Assert.assertEquals("m3.pos", p.getName());
        p = m3.getPars().get(2);
        Assert.assertEquals("m3.ampl", p.getName());

     }

     @Test
     public void unsuccessfullFitTest() throws Exception {

         SAMPController controller = new SAMPController("TestController", "An SED builder from the Virtual Astronomical Observatory", this.getClass().getResource("/iris_button_tiny.png").toString());

         controller.start(false);

         SherpaClient c = new SherpaClient(controller);
         try {
             c.startSherpa("/Users/olaurino/VAO/IRIS/packaging/2.5/iris-beta-2.5-macosx-x86_64/lib/sherpa/");

             Data dataset = c.createData("test");

             dataset.setX(new double[]{1.0, 2.0});
             dataset.setY(new double[]{1.0, 2.0});
             dataset.setStatError(new double[]{1.0, 2.0});
             dataset.setSysError(new double[]{1.0, 2.0});

             Model m1 = c.createModel(Models.Gaussian1D);
             Model m2 = c.createModel(Models.PowerLaw1D);

             String expression = c.createExpression("m1+m2", new Model[]{m1, m2});

             CompositeModel cm = c.createCompositeModel(expression, m1, m2);

             Stat stat = Stats.Chi2Gehrels;

             Method method = c.getMethod(OptimizationMethod.LevenbergMarquardt);
             
             FitResults results = c.fit(dataset, cm, stat, method);

             Assert.assertEquals(Boolean.FALSE, results.getSucceeded());

         } catch(Exception ex) {
             c.stopSherpa();
             controller.stop();
             Logger.getLogger(SherpaClientTest.class.getName()).log(Level.SEVERE, null, ex);
             Assert.fail();
         } finally {
             c.stopSherpa();
             controller.stop();
         }
     }

     @Test
     public void successfullFitTest() {
         SAMPController controller = new SAMPController("TestController", "An SED builder from the Virtual Astronomical Observatory", this.getClass().getResource("/iris_button_tiny.png").toString());

         controller.start(false);

         SherpaClient c = new SherpaClient(controller);
         try {
             c.startSherpa("/Users/olaurino/VAO/IRIS/packaging/2.5/iris-beta-2.5-macosx-x86_64/lib/sherpa/");

             Data dataset = c.createData("test");

             double[] x = new double[100];
             double[] err = new double[100];
             double[] syserr = new double[100];
             
             for(int i=0; i<100; i++) {
                 x[i] = 0.1+0.1*i;
                 err[i] = 0.4;
                 syserr[i] = 0;
             }

             double[] y = new double[]{114., 47., 35., 30., 40., 27., 30., 26., 24., 20., 26., 35.,
                29., 28., 34., 36., 43., 39., 33., 47., 44., 46., 53., 56.,
                52., 53., 49., 57., 49., 36., 33., 42., 49., 45., 42., 32.,
                31., 34., 18., 24., 25., 11., 17., 17., 11.,  9.,  8.,  5.,
                4., 10., 3.,  4.,  6.,  3.,  0.,  2.,  4.,  4.,  0.,  1.,
                2.,  0.,  3.,  3.,  0.,  2.,  1.,  2.,  3.,  0.,  1.,  0.,
                1.,  0.,  0.,  1.,  3.,  3.,  0.,  2.,  0.,  0.,  1.,  2.,
                0.,  1.,  0.,  1.,  1.,  0.,  1.,  1.,  1.,  1.,  1.,  1.,
                1.,  0.,  1.,  0.};

             dataset.setX(x);
             dataset.setY(y);
             dataset.setStatError(err);
             dataset.setSysError(syserr);

             AbstractModel m1 = c.createModel(Models.Gaussian1D);
             AbstractModel m2 = c.createModel(Models.PowerLaw1D);

             c.getParameter(m1, "fwhm").setVal(1.0);
             c.getParameter(m1, "pos").setVal(1.0);
             c.getParameter(m1, "ampl").setVal(1.0);

             c.getParameter(m2, "gamma").setVal(1.0);
             c.getParameter(m2, "ampl").setVal(1.0);
             c.getParameter(m2, "ref").setVal(1.0);

             String expression = c.createExpression("m1+m2", new Model[]{m1, m2});

             CompositeModel cm = c.createCompositeModel(expression, m1, m2);

             Stat stat = Stats.Chi2Gehrels;

             Method method = c.getMethod(OptimizationMethod.LevenbergMarquardt);

             Config config = method.getConfig();

             config.setMaxfev(1000);
             config.setFtol(1.1920929e-07);
             config.setEpsfcn(1.1920929e-07);
             config.setGtol(1.1920929e-07);
             config.setXtol(1.1920929e-07);
             config.setFactor(100.);


             FitResults results = c.fit(dataset, cm, stat, method);

             Assert.assertEquals(Boolean.TRUE, results.getSucceeded());

             Assert.assertEquals(2.586, results.getParvals()[0], 0.001);
             Assert.assertEquals(2.601, results.getParvals()[1], 0.001);
             Assert.assertEquals(47.26, results.getParvals()[2], 0.01);
             Assert.assertEquals(1.070, results.getParvals()[3], 0.001);
             Assert.assertEquals(9.18, results.getParvals()[4], 0.01);


         } catch(Exception ex) {
             c.stopSherpa();
             controller.stop();
             Logger.getLogger(SherpaClientTest.class.getName()).log(Level.SEVERE, null, ex);
             Assert.fail();
         } finally {
             c.stopSherpa();
             controller.stop();
         }
     }
}