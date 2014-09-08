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

package cfa.vo.sed.importer;

import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.io.SedFormat;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author olaurino
 */
public class NEDImporter {

    private static final String NED_DATA_ENDPOINT =
            "http://vaobeta.ipac.caltech.edu/services/accessSED?REQUEST=getData&TARGETNAME=:targetName";

    public static Sed getSedFromName(String targetName) throws SegmentImporterException {
        try {
            targetName = URLEncoder.encode(targetName, "UTF-8");
            String endpoint = NED_DATA_ENDPOINT.replace(":targetName", targetName);
            URL nedUrl = new URL(endpoint);
            Sed sed = Sed.read(nedUrl.openStream(), SedFormat.VOT);
            return sed;
        } catch (Exception ex) {
            throw new SegmentImporterException(ex);
        }
        
    }
}
