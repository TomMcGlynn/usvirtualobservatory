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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author olaurino
 */
public class FileFormatManager {

    private List<IFileFormat> customFormats = new ArrayList();

    public void addFormat(URL jarLocation, Class<? extends IFilter> filterClass) {
        FileFormat f = new FileFormat(filterClass);
        f.setPlugin(jarLocation.toString());
        customFormats.add(f);
    }

    private void addFormat(URL jarLocation, Class<?> filterClass, boolean byProxy) {

        if(!byProxy) {

            addFormat(jarLocation, (Class<? extends IFilter>) filterClass);

        } else {

            Method dataMethod = null;
            Method metadataMethod = null;

            Method[] methods = filterClass.getMethods();

            for(Method m : methods) {
                if(m.isAnnotationPresent(Metadata.class))
                    metadataMethod = m;
                if(m.isAnnotationPresent(Data.class))
                    dataMethod = m;
            }

            String name = filterClass.getAnnotation(Filter.class).name();

            IFilter filter = (IFilter) Proxy.newProxyInstance(FileFormatManager.class.getClassLoader(),
                new Class[] {IFilter.class}, new FilterProxy(name, filterClass, dataMethod, metadataMethod));

            FileFormat f = new FileFormat(filterClass, filter);
            f.setPlugin(jarLocation.toString());

            customFormats.add(f);

            return;
        }

        

        
    }

    public static FileFormatManager getInstance() {
        return FileFormatManagerHolder.INSTANCE;
    }

    public List<IFileFormat> getFormats() {
        List<IFileFormat> list = new ArrayList();

        IFileFormat[] nff = NativeFileFormat.values();

        List<IFileFormat> nffList = Arrays.asList(nff);

        list.addAll(nffList);

        list.addAll(customFormats);

        return list;
    }

    public List<IFileFormat> getCustomFormats() {
        return customFormats;
    }

    public void addFormatsFromJar(URL url) throws IOException {
        ClassLoader loader = URLClassLoader.newInstance(
            new URL[] { url },
            FileFormatManager.class.getClassLoader()
        );

        JarInputStream jis = new JarInputStream(url.openStream());

        JarEntry entry;

        while(true) {
            entry = jis.getNextJarEntry();
            if(entry == null)
                break;
            String name = entry.getName().replace("/", ".");
            if(name.endsWith(".class")) {
                name = name.replace(".class", "");
                Class clazz;
                
                try {
                    clazz = loader.loadClass(name);

                    Class[] interfaces = clazz.getInterfaces();

                    for(Class intface : interfaces) {

                        if(intface.equals(IFilter.class)) {
                        addFormat(url, clazz, false);
                        return;
                        }
                    }

                    if(clazz.isAnnotationPresent(Filter.class)) {
                        addFormat(url, clazz, true);
                        return;
                    }

                    
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(FileFormatManager.class.getName()).log(Level.SEVERE, null, ex);
                }


            }
        }
    }

    public IFileFormat[] getFormatsArray() {
        return getFormats().toArray(new NativeFileFormat[]{NativeFileFormat.VOTABLE});
    }

    private static class FileFormatManagerHolder {
        public static FileFormatManager INSTANCE = new FileFormatManager();
    }

}
