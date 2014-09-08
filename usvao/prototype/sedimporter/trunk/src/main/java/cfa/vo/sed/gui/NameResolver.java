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

package cfa.vo.sed.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author olaurino
 */
public class NameResolver {
    public static Double[] resolve(String name) {

        try { // This code block invokes the Sesame:sesame operation on web service
            cfa.vo.utils.SesameService sesameService = new cfa.vo.utils.SesameService_Impl();
            cfa.vo.utils.Sesame sesame = sesameService.getSesame();
            String response = sesame.sesame(name, "pi");
            Pattern pattern = Pattern.compile("%J (\\d*.\\d*) ([+-]\\d*.\\d*)", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(response);
            if(matcher.find())
                return new Double[]{Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2))};
        } catch(javax.xml.rpc.ServiceException ex) {
//            java.util.logging.Logger.getLogger(cfa.vo.utils.SesameService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch(java.rmi.RemoteException ex) {
//            java.util.logging.Logger.getLogger(cfa.vo.utils.SesameService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch(Exception ex) {
//            java.util.logging.Logger.getLogger(cfa.vo.utils.SesameService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        return null;

    }
}
