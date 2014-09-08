/***********************************************************************
*
* File: io/VOTablePointGroup.java
*
* Author:  jmiller      Created: Mon Nov 29 11:57:14 2010
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.sedlib.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import org.w3c.dom.Document;

import uk.ac.starlink.votable.VOElement;
import cfa.vo.sedlib.DoubleParam;
import cfa.vo.sedlib.Field;
import cfa.vo.sedlib.IntParam;
import cfa.vo.sedlib.Param;
import cfa.vo.sedlib.common.VOTableKeywords;

class VOTablePointGroup
{
    protected List<Boolean> constants;
    protected List<Integer> fieldOrder;
    protected List<String> fieldIds;
    protected Map <Integer,Field> fields;
    protected List<List <Param>> data;
    protected int utype;

    // keep the insert order of the subgroups while
    // being able to search for them by utype 
    // we're not expecting large numbers of subgroups so
    // a list should be sufficient 
    protected List <VOTablePointGroup> subgroups;

    public VOTablePointGroup (int utype)
    {
        this.constants = new ArrayList <Boolean> ();
        this.data = new ArrayList <List<Param>> ();
        this.subgroups = new ArrayList <VOTablePointGroup> ();
        this.fields = new HashMap <Integer,Field> ();
        this.fieldOrder = new ArrayList <Integer> ();
        this.fieldIds = new ArrayList <String> ();
        this.utype = utype;
    }

    public int getUtype ()
    {
        return this.utype;
    }

    public boolean hasField (int utype)
    {
        return this.fields.containsKey (utype);
    }

    public void addField (Field field, int utype, String id)
    {
        this.constants.add (true);
        this.fields.put (utype, field);
        this.fieldOrder.add (utype);
        this.fieldIds.add (id);
    }

    public void addData (int row, Param param)
    {
        Param firstParam;
        List <Param> paramCols;
        int column;
        
        if (this.data.size () == row)
        	this.data.add (new Vector<Param> ());

        this.data.get (row).add (param);
        
        paramCols = this.data.get (0);
        column = this.data.get(row).size ()-1;
        firstParam = paramCols.get (column);

        if ((firstParam.getValue () != null) ^ (param.getValue () != null))
            this.constants.set (column, false);
        else if ((firstParam.getValue () != null) && 
                 (!firstParam.getValue ().equals (param.getValue ())))
            this.constants.set (column, false);
    }

    public VOTablePointGroup createGroup (int utype)
    {
        VOTablePointGroup subgroup = null;

        // check if the subgroup already exists
        for (VOTablePointGroup subgrp : this.subgroups)
        {
            if (subgrp.utype == utype)
            {
                subgroup = subgrp;
                break;
            }
        }
        
        if (subgroup == null)
        {
            subgroup = new VOTablePointGroup (utype);
            this.subgroups.add (subgroup);
        }

        return subgroup;
    }

    public void addToVOTable (VOElement parent, String namespace)
    {
        Document document = parent.getOwnerDocument ();
        VOElement group = (VOElement)document.createElement(VOTableKeywords._GROUP);

        group.setAttribute (VOTableKeywords._UTYPE, VOTableKeywords.getName (this.utype, namespace));   
      
        for (int ii=0; ii<fieldOrder.size (); ii++)
        {
            VOElement fieldRef = (VOElement)document.createElement("FIELDref");
            
            // create a field ref for non-constants or if there's only
            // a single row
            if ((!this.constants.get(ii)) || (this.data.size () == 1))
            {
                fieldRef.setAttribute("ref", this.fieldIds.get(ii));
                group.appendChild (fieldRef);
            }
            else
            {
                VOElement param = (VOElement) document.createElement(VOTableKeywords._PARAM);
                Param data = this.data.get (0).get(ii);
                Field field = this.fields.get (this.fieldOrder.get(ii));
                param.setAttribute( VOTableKeywords._UTYPE, field.getUtype ());
                param.setAttribute(VOTableKeywords._NAME, field.getName());
                param.setAttribute (VOTableKeywords._UCD, field.getUcd());
                param.setAttribute (VOTableKeywords._UNIT, field.getUcd());
                param.setAttribute (VOTableKeywords._VALUE, data.getValue ());
                
                if (data instanceof DoubleParam)
                    param.setAttribute(  VOTableKeywords._DATATYPE, "double" );
                else if (data instanceof IntParam)
                    param.setAttribute(  VOTableKeywords._DATATYPE, "int" );
                else 
                {
                    param.setAttribute(  VOTableKeywords._DATATYPE, "char" );
                    param.setAttribute(VOTableKeywords._ARRAYSIZE, "*");
                }
                group.appendChild (param);
            }
        }

        for(VOTablePointGroup subgroup : this.subgroups)
            subgroup.addToVOTable (group, namespace);

        if (group.getChildren ().length > 0)
            parent.appendChild (group);

    }

    public Param[][] getDataTable ()
    {
        Param dataTable[][];
        int rows;
        int columns = 0;
        int col = 0;
        List<Boolean> allConstants = new ArrayList <Boolean>();
        List<List <Param>> allData = new ArrayList <List <Param>>();

        this.getAllConstants (allConstants);
        this.getAllData (allData);

        if (allData.isEmpty ())
            return null;
        if (allData.get(0).isEmpty ())
            return null;

        // figure out the number of row and cols
        for (boolean constant : allConstants)
            if (!constant)
                columns++;

        rows = allData.size ();

        dataTable = new Param[rows][columns];

        // add data to the table
        for (int row=0; row<rows; row++)
        {
            List<Param> dataRow = allData.get(row);
            col = 0;
            for (int ii=0; ii<allConstants.size (); ii++)
            {
                if (!allConstants.get(ii))
                    dataTable[row][col++] = dataRow.get(ii);
            }
        }

        return dataTable;
    }

    public void getDataColumns (List<Field> fields, List<String> fieldIds)
    {
        List<Boolean> allConstants = new ArrayList <Boolean>();
        int fieldIndex = 0;

        this.getAllConstants (allConstants);
        this.getAllFields (fields, fieldIds);

        if (fields.isEmpty ())
            return;

        // remove any fields that are constants
        for (int ii=0; ii<allConstants.size (); ii++)
        {
            // if there's only one row then it's not considered
            // a constant
            if (allConstants.get(ii) && this.data.size () != 1)
            {
                fields.remove (fieldIndex);
                fieldIds.remove (fieldIndex);
            }
            else
            	fieldIndex++;
        }

    }

    private void getAllConstants (List <Boolean> allConstants)
    {
        for (boolean cc : this.constants)
        {
        	// if there's only one row then then the row is
        	// considered not to be constant
        	if (this.data.size () == 1)
        		allConstants.add(false);
        	else
                allConstants.add (cc);
        }
   
        for(VOTablePointGroup subgroup : this.subgroups)
            subgroup.getAllConstants (allConstants);
    }

    private void getAllData (List <List<Param>> allData)
    {
    	if (allData.isEmpty ())
    	{
            for (int row=0; row<this.data.size (); row++)
        	    allData.add (new Vector<Param> ());
    	}
        
        for (int row=0; row<allData.size (); row++)
        {
        	List<Param> currentRow = allData.get(row);
        	List<Param> dataRow = this.data.get(row);
        	for (Param pp : dataRow)
        		currentRow.add (pp);
        }

        for(VOTablePointGroup subgroup : this.subgroups)
            subgroup.getAllData (allData);
    }

    private void getAllFields (List<Field> allFields, List<String> allFieldIds)
    {
        for (Integer ii : this.fieldOrder)
            allFields.add (this.fields.get(ii));

        for (String id : this.fieldIds)
            allFieldIds.add(id);

        for(VOTablePointGroup subgroup : this.subgroups)
            subgroup.getAllFields (allFields, allFieldIds);
    }


}

