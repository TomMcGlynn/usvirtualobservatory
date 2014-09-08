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

import cfa.vo.sedlib.DoubleParam;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.sedlib.io.SedFormat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author olaurino
 */
public class NEDImporter {

    public static final String NED_DATA_DEFAULT_ENDPOINT =
            "http://vaobeta.ipac.caltech.edu/services/accessSED?REQUEST=getData&TARGETNAME=:targetName";

    public static Sed getSedFromName(String targetName) throws SegmentImporterException {
        return getSedFromName(targetName, NED_DATA_DEFAULT_ENDPOINT);
    }

    public static Sed getSedFromName(String targetName, String endpoint) throws SegmentImporterException {
        try {
            targetName = URLEncoder.encode(targetName, "UTF-8");
            endpoint = endpoint.replace(":targetName", targetName);
            URL nedUrl = new URL(endpoint);

            return Sed.read(nedUrl.openStream(), SedFormat.VOT);
//            //FIXME UGLY STOPGAP MEASURE
//            File tempNed = File.createTempFile("ned", "vot");
//
//            ReadableByteChannel rbc = Channels.newChannel(nedUrl.openStream());
//            FileOutputStream fos = new FileOutputStream(tempNed);
//            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//
//            Sed sed = Sed.read(tempNed.getAbsolutePath(), SedFormat.VOT);
//
//            File temp = File.createTempFile("tempnedsed", "vot");
//            sed.write(temp.getAbsolutePath(), SedFormat.VOT);
//
//            try {
//                Sed.read(temp.getAbsolutePath(), SedFormat.VOT);
//                return sed;
//            } catch (Exception ex) {
//                return Sed.read(tempFilter(tempNed), SedFormat.VOT);
//            }
//            //FIXME UGLY STOPGAP MEASURE

        } catch (Exception ex) {
            throw new SegmentImporterException(ex);
        }
    }

    private static InputStream tempFilter(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
        } catch (IOException ex) {
            Logger.getLogger(NEDImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String file = new Scanner(is).useDelimiter("\\A").next();
        Pattern p = Pattern.compile("<!\\[CDATA\\[(.*)\\]\\]>");
        Matcher m = p.matcher(file);
        while(m.find()) {
            try {
                file = file.replace(m.group(1), URLEncoder.encode(m.group(1), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(NEDImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        file = file.replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "");
        return new ByteArrayInputStream(file.getBytes());
    }

    public static Segment fixNEDSegment(Segment segment) {//FIXME STOPGAP MEASURE
        DoubleParam[] coordsParam = segment.getTarget().getPos().getValue();
        segment.createChar().createSpatialAxis().createCoverage().createLocation().setValue(coordsParam);
        return segment;
    }
}
