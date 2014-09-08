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
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.MultiTableBuilder;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSequence;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.URLDataSource;

/**
 *
 * @author olaurino
 */
public abstract class AbstractMultiStarTableFilter extends AbstractSingleStarTableFilter {

    @Override
    public Object[] getData(URL url, int segment, int column) throws IOException, FilterException {
        DataSource ds = new URLDataSource(url);

        TableSequence seq = makeTables(ds);

        StarTable table = null;

        for(int i=1; i!=segment;) {
            seq.nextTable();
        }

        table = Tables.randomTable(seq.nextTable());

        int len = Tables.checkedLongToInt(table.getRowCount());


        Class contentClass = table.getColumnInfo(column).getContentClass();

        if(contentClass.isArray()) {
            if(!contentClass.getComponentType().isPrimitive())
                return (Object[]) table.getCell(0,column);
            else {

                Object arr = table.getCell(0,column);

                if(arr instanceof float[])
                    len = ((float[]) arr).length;

                if(arr instanceof double[])
                    len = ((double[]) arr).length;

                if(arr instanceof short[])
                    len = ((short[]) arr).length;

                if(arr instanceof long[])
                    len = ((long[]) arr).length;

                Number[] array = (Number[]) Array.newInstance(Number.class, len);

                for(int i=0; i<len; i++) {
                    array[i] = Array.getDouble(arr, i);
                }
                return array;
            }
        } else {

            Object[] array = (Object[]) Array.newInstance(contentClass, len);

            for(int i=0; i<len; i++) {
                array[i] = table.getCell(i, column);

            }

            return array;

        }


    }

    @Override
    public List<ISegmentMetadata> getMetadata(URL url) throws FilterException, IOException {
        
        List<ISegmentMetadata> list = new ArrayList();

        list.addAll(getMultipleMetadata(url));

        return list;
    }
    
    protected List<ISegmentMetadata> getMultipleMetadata(URL url) throws FilterException, IOException {
        DataSource ds = new URLDataSource(url);
        
        TableSequence seq = null;
        
        try {
            seq = makeTables(ds);

            StarTable table = null;

            List<ISegmentMetadata> metadataList = new ArrayList();

            while((table = seq.nextTable())!=null) {
                table = Tables.randomTable(table);
                List<ISegmentParameter> paramList = new ArrayList();
                List<ISegmentColumn> columnList = new ArrayList();

                for(DescribedValue v : (List<DescribedValue>) table.getParameters()) {
                    paramList.add(new StarTableSegmentParameter(v.getInfo(), v.getValue()));
                }

                int colCount = table.getColumnCount();

                for (int i = 0; i < colCount; i++) {

                    ColumnInfo c = table.getColumnInfo(i);
                    StarTableSegmentColumn column = new StarTableSegmentColumn(c, i);
                    columnList.add(column);

                }

                metadataList.add(new SegmentMetadata(paramList, columnList));
            }

            return metadataList;

        } catch (TableFormatException ex) {
            throw new FilterException("Error reading StarTable format", ex);
        }

        
    }
    
    public final StarTable makeStarTable(URL url) {
        throw new UnsupportedOperationException("Unsupported");
    }

    protected abstract MultiTableBuilder getTableBuilder();

    protected TableSequence makeTables(DataSource ds) throws IOException {
        return getTableBuilder().makeStarTables(ds, StoragePolicy.ADAPTIVE);
    }

}
