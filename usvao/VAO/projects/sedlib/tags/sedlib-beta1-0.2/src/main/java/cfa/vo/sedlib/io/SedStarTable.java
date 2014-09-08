package cfa.vo.sedlib.io;

import uk.ac.starlink.table.*;
import uk.ac.starlink.votable.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import cfa.vo.sedlib.*;

class SedStarTable extends RandomStarTable
{
    private final List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
    Param[][] dataMatrix = null;

    public SedStarTable (Field []columns, String []columnIds, Param [][]data)
    {
        if ((columns == null) || (data == null))
            return;

        List <String> datatypes = new ArrayList<String> (data[0].length);

        for (int ii=0; ii<data[0].length; ii++)
        {
            if (data[0][ii] instanceof DoubleParam)
                datatypes.add ("double");
            else if (data[0][ii] instanceof IntParam)
                datatypes.add ("int");
            else
                datatypes.add ("char");
        }


        for (int ii=0; ii<columns.length; ii++)
        {
            this.columnInfos.add (setColumnInfo (columnIds[ii],
                                         columns[ii].getName (),
                                         columns[ii].getUtype (),
                                         datatypes.get(ii),
                                         columns[ii].getUcd (),
                                         columns[ii].getUnit ()));
        }

        this.dataMatrix = data;
    }

    /**
       Creates a ColumnInfo and sets the metadata for a column of this table.
    */
    ColumnInfo setColumnInfo( String id, String name, String utype, String datatype,
			      String ucd, String unit )
    {
	ColumnInfo colInfo = new ColumnInfo( name );
	colInfo.setAuxData( new Vector<Object>() );
	colInfo.setUtype( utype );
	colInfo.setAuxDatum( new DescribedValue( VOStarTable.ID_INFO, id ) );
	Class dt = this._getDTClass( datatype );
	colInfo.setContentClass(dt);
	colInfo.setUCD( ucd );
	colInfo.setUnitString( unit );

	// We set nullable to false to keep the starlink code from
	// inserting a VALUE element ( <VALUES null='-2147483648'/> )
	// into the FIELD elements in the VOTable.  This VALUES
	// element defines the values to use to indicate null data if
	// it is acceptable to have null data in the column.
	colInfo.setNullable( false );

	return colInfo;
    }


    public long getRowCount()
    {
        if (this.dataMatrix == null)
            return 0;
	return (long) this.dataMatrix.length;
    }

    public int getColumnCount()
    {
        if (this.columnInfos == null)
            return 0;
	return this.columnInfos.size();
    }

    public ColumnInfo getColumnInfo( int col )
    {
        if (this.columnInfos == null)
            return null;

	ColumnInfo columnInfo = (ColumnInfo)this.columnInfos.get(col);
	
	return columnInfo;
    }

    public Object getCell( long irow, int icol )
    {
        if (this.dataMatrix == null)
            return null;

	return this.dataMatrix[ (int)irow ][ icol ].getCastValue ();
    }

    private Class _getDTClass(String dt)
    {
        if(dt.equalsIgnoreCase("DOUBLE"))
            return Double.class;
        else if (dt.equalsIgnoreCase("INT")
                 ||  dt.equalsIgnoreCase("INTEGER"))
            return Integer.class;
        else if (dt.equalsIgnoreCase("BYTE"))
            return Byte.class;
        else if (dt.equalsIgnoreCase("FLOAT"))
            return Float.class;
        else if (dt.equalsIgnoreCase("LONG"))
            return Long.class;
        else if (dt.equalsIgnoreCase("SHORT"))
            return Short.class;
        else if (dt.equalsIgnoreCase("STRING"))
            return String.class;
        throw new IllegalArgumentException( "Invalid datatype: '" + dt + "'" );
    }

}
