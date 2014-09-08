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

package cfa.vo.utils.smoketest;

import java.net.URL;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * @author olaurino
 */
public class GuiSmokeTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String dir = args[0];

        String file = args[1];

        int timeout = 10;

        if(args.length==3)
            timeout = Integer.valueOf(args[2]);

        TestSuite suite = new TestSuite();
        suite.addTest(new GuiSmokeTest(dir, new URL("file://"+file), timeout));
        suite.run(new TestResult());

    }

}
