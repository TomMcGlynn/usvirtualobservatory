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

package cfa.vo.sed.filters;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author olaurino
 */
public class FilterCache {
    private static Map<Class, IFilter> filterMap = new HashMap();

    public static IFilter getInstance(Class filterClass) throws FilterException {
        if(!filterMap.containsKey(filterClass)) {
            try {
                filterMap.put(filterClass, (IFilter) filterClass.newInstance());
            } catch(Exception ex) {
                throw new FilterException(ex);
            }
        }

        return filterMap.get(filterClass);
    }

    static void put(Class filterClass, IFilter filterInstance) {
        filterMap.put(filterClass, filterInstance);
    }

}
