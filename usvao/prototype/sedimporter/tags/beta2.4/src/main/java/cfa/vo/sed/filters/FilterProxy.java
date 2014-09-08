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

import cfa.vo.sed.importer.ISegmentMetadata;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author olaurino
 */
public final class FilterProxy implements InvocationHandler {

    private Object filterInstance;
    private Map<String, Method> methodMap = new HashMap();
    private String name;

    public FilterProxy(Class filterClass) {
        try {
            this.filterInstance = filterClass.newInstance();
            methodMap.put("data", filterClass.getDeclaredMethod("getData", InputStream.class, int.class, int.class));
            methodMap.put("metadata", filterClass.getDeclaredMethod("getMetadata", InputStream.class));
        } catch (Exception ex) {
            Logger.getLogger(FilterProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FilterProxy(String name, Class filterClass, Method dataMethod, Method metadataMethod) {
        try {
            this.filterInstance = filterClass.newInstance();
            methodMap.put("data", dataMethod);
            methodMap.put("metadata", metadataMethod);
            this.name = name;
        } catch (Exception ex) {
            Logger.getLogger(FilterProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object[] getData(InputStream stream, int segment, int column) throws Exception {
        return (Object[]) methodMap.get("data").invoke(filterInstance, stream, segment, column);
    }

    private List<ISegmentMetadata> getMetadata(InputStream stream) throws Exception {
        return (List<ISegmentMetadata>) methodMap.get("metadata").invoke(filterInstance, stream);
    }

    private String getDescription() {
        if(name!=null)
            return name;

        return ((IFilter)filterInstance).getDescription();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("getData"))
            return getData((InputStream)args[0], (Integer)args[1], (Integer)args[2]);
        else if(method.getName().equals("getMetadata"))
            return getMetadata((InputStream)args[0]);
        else if(method.getName().equals("getDescription"))
            return getDescription();
        return null;
    }
    
}
