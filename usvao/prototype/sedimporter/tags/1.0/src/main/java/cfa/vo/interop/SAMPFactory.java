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

package cfa.vo.interop;

import cfa.vo.interop.SAMPMessage;
import cfa.vo.interop.SimpleSAMPMessage;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.astrogrid.samp.Message;

/**
 *
 * @author olaurino
 */
public class SAMPFactory {
    public static Object get(Class clazz) {
        return Proxy.newProxyInstance(SAMPFactory.class.getClassLoader(), new Class[]{clazz}, new SAMPProxy(clazz));
    }

    public static SAMPMessage createMessage(String mtype, Object instance, Class clazz) throws Exception {
        Message message = new Message(mtype);

        message.setParams(SAMPProxy.serialize(instance, clazz));

        return new SimpleSAMPMessage(message);
    }

    public static Object get(SAMPMessage message, Class clazz) throws Exception {
        return Proxy.newProxyInstance(SAMPFactory.class.getClassLoader(), new Class[]{clazz}, new SAMPProxy(message.get().getParams(), clazz));
    }

    public static Object get(Map map, Class clazz) throws Exception {
        return Proxy.newProxyInstance(SAMPFactory.class.getClassLoader(), new Class[]{clazz}, new SAMPProxy(map, clazz));
    }
}
