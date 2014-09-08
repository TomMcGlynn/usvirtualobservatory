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
public class SpaceTrimmerTest {

    public SpaceTrimmerTest() {
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
     * Test of leadingTrim method, of class SpaceTrimmer.
     */
    @Test
    public void testLeadingTrim() {
        System.out.println("leadingTrim");
        String string = "  foo  bar  ";
        String expResult = "foo  bar  ";
        String result = SpaceTrimmer.leadingTrim(string);
        assertEquals(expResult, result);
    }

    /**
     * Test of trailingTrim method, of class SpaceTrimmer.
     */
    @Test
    public void testTrailingTrim() {
        System.out.println("trailingTrim");
        String string = "  foo  bar  ";
        String expResult = "  foo  bar";
        String result = SpaceTrimmer.trailingTrim(string);
        assertEquals(expResult, result);
    }

    /**
     * Test of sideTrim method, of class SpaceTrimmer.
     */
    @Test
    public void testSideTrim() {
        System.out.println("sideTrim");
        String string = "  foo  bar  ";
        String expResult = "foo  bar";
        String result = SpaceTrimmer.sideTrim(string);
        assertEquals(expResult, result);
    }

}