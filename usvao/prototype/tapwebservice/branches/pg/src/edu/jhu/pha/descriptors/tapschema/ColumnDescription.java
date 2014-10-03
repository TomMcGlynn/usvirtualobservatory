/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.descriptors.tapschema;
/**
 * These sets of classes are used to get TAP_Schema from database
 * @author deoyani nandrekar-heinis
 */

public class ColumnDescription
{
   
    public String tableName;

    public String columnName;
   
    public String description;

    public String utype;

    public String ucd;

    public String unit;

    public String datatype;

    public Integer size;

    public ColumnDescription()
    {
    }
   
    public ColumnDescription(String tableName,
                  String columnName,
                  String description,
                  String utype,
                  String ucd,
                  String unit,
                  String datatype,
                  Integer size)
    {
        this.tableName = tableName;
        this.columnName = columnName;
        this.description = description;
        this.utype = utype;
        this.ucd = ucd;
        this.unit = unit;
        this.datatype = datatype;
        this.size = size;
    }

    public final String getTableName()
    {
        return tableName;
    }
    public final void setTableName(String tableName)
    {
       this.tableName = tableName;
    }
    public final String getColumnName()
    {
        return columnName;
    }
    public final void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }
    public final String getDescription()
    {
        return description;
    }
    public final void setDescription(String description)
    {
        this.description = description;
    }
    public final String getUtype()
    {
        return utype;
    }
    public final void setUtype(String utype)
    {
        this.utype = utype;
    }
    public final String getUcd()
    {
        return ucd;
    }
    public final void setUcd(String ucd)
    {
        this.ucd = ucd;
    }
    public final String getUnit()
    {
        return unit;
    }
    public final void setUnit(String unit)
    {
        this.unit = unit;
    }
    public final String getDatatype()
    {
        return datatype;
    }
    public final void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }
    public final Integer getSize()
    {
        return size;
    }
    public final void setSize(Integer size)
    {
        this.size = size;
    }
}
