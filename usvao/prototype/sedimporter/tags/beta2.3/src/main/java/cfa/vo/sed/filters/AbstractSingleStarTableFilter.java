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

import cfa.vo.sed.importer.ISegmentColumn;
import cfa.vo.sed.importer.ISegmentMetadata;
import cfa.vo.sed.importer.ISegmentParameter;
import cfa.vo.sed.importer.SegmentMetadata;
import cfa.vo.sed.importer.StarTableSegmentColumn;
import cfa.vo.sed.importer.StarTableSegmentParameter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;

/**
 *
 * @author omarlaurino
 */
public abstract class AbstractSingleStarTableFilter implements IFilter {

    private List<ISegmentColumn> columnList = new ArrayList();

    private List<ISegmentParameter> paramList = new ArrayList();

    @Override
    public Object[] getData(URL url, int segment, int column) throws IOException, FilterException {
        if(segment>1)
            throw new IndexOutOfBoundsException("This is a Single Table filter. Trying to access more than one segment.");

        return getData(url, column);
    }

    public abstract StarTable makeStarTable(URL url) throws TableFormatException, IOException;

    public List<ISegmentMetadata> getMetadata(URL url) throws FilterException, IOException {
        
        List<ISegmentMetadata> list = new ArrayList();

        list.add(getSingleMetadata(url));

        return list;
    }

    protected ISegmentMetadata getSingleMetadata(URL url) throws FilterException, IOException {

        StarTable table;
        try {
            table = makeStarTable(url);
        } catch (TableFormatException ex) {
            throw new FilterException(ex);
        }

        paramList = new ArrayList();
        
        for(DescribedValue v : (List<DescribedValue>) table.getParameters()) {
            paramList.add(new StarTableSegmentParameter(v.getInfo(), v.getValue()));
        }

        columnList = new ArrayList();

        int colCount = table.getColumnCount();
        
        for (int i = 0; i < colCount; i++) {

            ColumnInfo c = table.getColumnInfo(i);
            StarTableSegmentColumn column = new StarTableSegmentColumn(c, i);
            columnList.add(column);
                
        }

        return new SegmentMetadata(paramList, columnList);
    }

    public Object[] getData(URL url, int column) throws IOException {
        StarTable table = makeStarTable(url);

        List array = new ArrayList();

        RowSequence rowSequence = table.getRowSequence();

        for(int i = 0; rowSequence.next(); i++) {
            Object[] row = (Object[]) rowSequence.getRow();

            array.add(row[column]);
        }

        Number[] arr = new Number[array.size()];

        return array.toArray(arr);
    }

}
