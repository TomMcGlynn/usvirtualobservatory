/**
 * Copyright (C) 2012 Smithsonian Astrophysical Observatory
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

package spv.fit;

import java.util.HashMap;

/**
 *
 * @author olaurino
 */
public class FittingEngineFactory {
    private Engines engines = new Engines();


    public FittingEngine get(String type) throws NoSuchEngineException {
        if(engines.containsKey(type))
            return engines.get(type);

        throw new NoSuchEngineException();
    }

    private class Engines extends HashMap<String, FittingEngine> {
        public Engines() {
            super();
            put("sherpa", new StartSherpa());
            put("test", new MockupSherpa());
        }
    }
}