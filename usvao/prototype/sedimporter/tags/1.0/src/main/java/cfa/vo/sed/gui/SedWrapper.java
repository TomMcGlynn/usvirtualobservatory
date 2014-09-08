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

package cfa.vo.sed.gui;

import cfa.vo.sed.filters.FilterCache;
import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.Segment;
import cfa.vo.sedlib.common.SedException;
import cfa.vo.sedlib.common.SedInconsistentException;
import cfa.vo.sedlib.common.SedNoDataException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author olaurino
 */
public class SedWrapper {

    private List<SegmentWrapper> segmentList = new ArrayList();

    private boolean sampListener = true;

    public SedWrapper() {

    }

    public SedWrapper(List<SegmentWrapper> segmentList) {
        this.segmentList = segmentList;
    }

    public boolean isSampLister() {
        return sampListener;
    }

    public Sed makeSed() throws SedException {
        Sed sed = new Sed();
        for(SegmentWrapper wp : segmentList)
            sed.addSegment(wp.getSegment());
        return sed;
    }

    public void addSegment(Segment segment, SetupFrame frame) throws SedInconsistentException, SedNoDataException {
        segmentList.add(new SegmentWrapper(segment, frame));
    }

    public void removeSegment(SegmentWrapper sw) {
        segmentList.remove(sw);
    }

//    public void removeSegmentAndCleanCache(SegmentWrapper sw) {
//        segmentList.remove(sw);
//        try {
//            if(sw.getSetupFrame()!=null)
//                FilterCache.remove(new URL(sw.getSetupFrame().getFileUrl()));
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(SedWrapper.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    public void setSampListener(boolean state) {
        sampListener = true;
    }

    public int getSize() {
        return segmentList.size();
    }

    public List<SegmentWrapper> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<SegmentWrapper> segmentList) {
        this.segmentList = segmentList;
    }

    public void clean() {
        for(Iterator<SegmentWrapper> i = segmentList.iterator(); i.hasNext(); ) {
            SegmentWrapper sw = i.next();
            i.remove();
            if(sw.getSetupFrame()!=null)
                try {
                FilterCache.remove(new URL(sw.getSetupFrame().getFileUrl()));
            } catch (MalformedURLException ex) {
                Logger.getLogger(SedWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


}
