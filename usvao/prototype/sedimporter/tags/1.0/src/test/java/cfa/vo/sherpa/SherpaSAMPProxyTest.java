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
import cfa.vo.interop.SAMPMessage;
import java.util.List;
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
public class SherpaSAMPProxyTest {

    public SherpaSAMPProxyTest() {
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
     public void compositeModelTest() throws Exception {
        Parameter par1 = (Parameter) SAMPFactory.get(Parameter.class);
        par1.setAlwaysfrozen(Boolean.TRUE);
        par1.setMax(1.0);
        par1.setName("par1");
        Parameter par2 = (Parameter) SAMPFactory.get(Parameter.class);
        par2.setAlwaysfrozen(Boolean.FALSE);
        par2.setLink("par1");

        Model model1 = (Model) SAMPFactory.get(Model.class);

        model1.addPar(par1);
        model1.addPar(par2);

        model1.setName("model1");
        par2.setName("par2");

        Model model2 = (Model) SAMPFactory.get(Model.class);

        model2.setName("model2");

        CompositeModel cmodel = (CompositeModel) SAMPFactory.get(CompositeModel.class);

        cmodel.setName("Cmodel");

        cmodel.addPart(model1);
        cmodel.addPart(model2);

        model1 = cmodel.getParts().get(0);
        model2 = cmodel.getParts().get(1);

        Assert.assertEquals("Cmodel", cmodel.getName());

        Assert.assertEquals("model1", model1.getName());
        List<Parameter> pars = model1.getPars();

        Assert.assertNotNull(pars);

        Assert.assertEquals(2, pars.size());

        Assert.assertEquals("par1", pars.get(0).getName());
        Assert.assertEquals(1.0, pars.get(0).getMax());
        Assert.assertEquals(true, pars.get(0).getAlwaysfrozen());

        Assert.assertEquals("par2", pars.get(1).getName());
        Assert.assertEquals(null, pars.get(1).getMax());
        Assert.assertEquals(false, pars.get(1).getAlwaysfrozen());

        SAMPMessage message = SAMPFactory.createMessage("table.load.votable", cmodel, CompositeModel.class);
        message.get().check();

        cmodel = (CompositeModel) SAMPFactory.get(message, CompositeModel.class);

        Assert.assertEquals("Cmodel", cmodel.getName());

        model1 = cmodel.getParts().get(0);
        model2 = cmodel.getParts().get(1);

        Assert.assertEquals("model1", model1.getName());

        pars = model1.getPars();

        Assert.assertNotNull(pars);

        Assert.assertEquals(2, pars.size());

        Assert.assertEquals("par1", pars.get(0).getName());
        Assert.assertEquals(1.0, pars.get(0).getMax());
        Assert.assertEquals(true, pars.get(0).getAlwaysfrozen());

        Assert.assertEquals("par2", pars.get(1).getName());
        Assert.assertEquals(null, pars.get(1).getMax());

        Data data = (Data) SAMPFactory.get(Data.class);

        data.setX(new double[]{1.0, 2.0});

        double[] d = data.getX();
        Assert.assertEquals(1.0, d[0]);
        Assert.assertEquals(2.0, d[1]);

        message = SAMPFactory.createMessage("table.load.votable", data, Data.class);

        Assert.assertEquals("table.load.votable", message.get().getMType());

        data = (Data) SAMPFactory.get(message, Data.class);

        d = data.getX();
        Assert.assertEquals(1.0, d[0]);
        Assert.assertEquals(2.0, d[1]);





     }

}