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
package org.usvao.descriptors.tapschema;

import java.util.List;
/**
 * These sets of classes are used to get TAP_Schema from database
 * @author deoyani nandrekar-heinis
 */
public class TableDescription {

    public String schemaName;

    public String tableName;

    public String description;

    public String utype;

    public List<ColumnDescription> columnDescs;

    public TableDescription() {}

    /**
     * Construct a Table using the specified parameters.
     *
     * @param schemaName The schema this Table belongs to.
     * @param tableName The fully qualified Table name.
     * @param description Describes the Table.
     * @param utype The utype of the Table.
     */
    public TableDescription(String schemaName, String tableName, String description, String utype)
    {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.description = description;
        this.utype = utype;
    }


    public String getSimpleTableName()
    {
        String simpleName = tableName;
        if (tableName.startsWith(schemaName + "."))
            simpleName = tableName.substring(tableName.indexOf(".")+1);
        return simpleName;
    }

    public final String getSchemaName()
    {
        return schemaName;
    }

    public final void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public final String getTableName()
    {
        return tableName;
    }

    public final void setTableName(String tableName)
    {
        this.tableName = tableName;
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

    public final List<ColumnDescription> getColumnDescs()
    {
        return columnDescs;
    }

    public final void setColumnDescs(List<ColumnDescription> columnDescs)
    {
        this.columnDescs = columnDescs;
    }


}
