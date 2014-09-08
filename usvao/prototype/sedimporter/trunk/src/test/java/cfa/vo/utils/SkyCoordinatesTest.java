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

package cfa.vo.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author olaurino
 */
public class SkyCoordinatesTest {

    public SkyCoordinatesTest() {
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

    /**
     * Test of getRaDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetRaHMSSpaces() {
        System.out.println("getRaDeg");
        String ra = "42 15 42.75";
        Double expResult = 42.261875;
        Double result = SkyCoordinates.getRaDeg(ra);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of getDecDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetDecDMSSpaces() {
        System.out.println("getDecDeg");
        String dec = "-13 42 15.72";
        Double expResult = -13.704367;
        Double result = SkyCoordinates.getDecDeg(dec);
        assertEquals(expResult, result, 0.00001);
    }

    @Test
    public void testGetRaHMSColumns() {
        System.out.println("getRaDeg");
        String ra = "42:15:42.75";
        Double expResult = 42.261875;
        Double result = SkyCoordinates.getRaDeg(ra);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of getDecDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetDecDMSColumns() {
        System.out.println("getDecDeg");
        String dec = "-13:42:15.72";
        Double expResult = -13.704367;
        Double result = SkyCoordinates.getDecDeg(dec);
        assertEquals(expResult, result, 0.00001);
    }

    @Test
    public void testGetRaDecimal() {
        System.out.println("getRaDeg");
        String ra = "42.261875";
        Double expResult = 42.261875;
        Double result = SkyCoordinates.getRaDeg(ra);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of getDecDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetDecDecimal() {
        System.out.println("getDecDeg");
        String dec = "-13.704367";
        Double expResult = -13.704367;
        Double result = SkyCoordinates.getDecDeg(dec);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of getDecDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetWrongDec() {
        System.out.println("getWrongDecDeg");
        String dec = "pippo";
        Double expResult = Double.NaN;
        Double result = SkyCoordinates.getDecDeg(dec);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDecDeg method, of class SkyCoordinates.
     */
    @Test
    public void testGetWrongRaDeg() {
        System.out.println("getWrongRaDeg");
        String ra = "pippo";
        Double expResult = Double.NaN;
        Double result = SkyCoordinates.getRaDeg(ra);
        assertEquals(expResult, result);
    }

}