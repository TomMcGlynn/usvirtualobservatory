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

/**
 *
 * @author omarlaurino
 */
public class FileFormat implements IFileFormat {

    private Class filterClass;
    private String name;
    private String pluginLocation = "NATIVE";

    public FileFormat(String name, Class<? extends IFilter> filterClass) {
        this.name = name;
        this.filterClass = filterClass;
    }

    public FileFormat(Class<? extends IFilter> filterClass) {
        try {
            this.name = FilterCache.getInstance(filterClass).getDescription();
        } catch (FilterException ex) {
            this.name = "UNKNOWN";
        }
        this.filterClass = filterClass;
    }

    public FileFormat(Class<?> filterClass, IFilter filterInstance) {
        this.name = filterInstance.getDescription();
        this.filterClass = filterClass;
        FilterCache.put(filterClass, filterInstance);
    }

    @Override
    public IFilter getFilter() throws FilterException {
        return FilterCache.getInstance(filterClass);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPlugin() {
        return pluginLocation;
    }

    @Override
    public void setPlugin(String pluginLocation) {
        this.pluginLocation = pluginLocation;
    }

    @Override
    public String toString() {
        if(pluginLocation.equals("NATIVE"))
            return name;
        return pluginLocation;
    }
}
