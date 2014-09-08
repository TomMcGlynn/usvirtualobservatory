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

import cfa.vo.sedlib.ArrayOfPoint;
import cfa.vo.sedlib.Curation;
import cfa.vo.sedlib.Segment;
import cfa.vo.sedlib.Target;

/**
 *
 * @author olaurino
 */
public class SegmentWrapper {

    private Segment segment;

    private SetupFrame setupFrame;

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public SetupFrame getSetupFrame() {
        return setupFrame;
    }

    public void setSetupFrame(SetupFrame setupFrame) {
        this.setupFrame = setupFrame;
    }

    public Target getTarget() {
        return segment.getTarget();
    }

    public Curation getCuration() {
        return segment.getCuration();
    }

    public ArrayOfPoint getData() {
        return segment.getData();
    }

    public SegmentWrapper(Segment segment, SetupFrame setupFrame) {
        this.setupFrame = setupFrame;
        this.segment = segment;
    }

}
