package cfa.vo.sedlib.io;

import uk.ac.starlink.votable.*;

import java.util.List;
import java.util.ArrayList;


/**
 *  Stores VOTable data including a VOElement and a list of star tables. The
 *  class contains the root of the document and then a startable for each
 *  table inside the document.
 */
class VOTableObject
{

    VOElement root;
    List<SedStarTable> starTableList;

    public VOTableObject ()
    {
        this.root = null;
        this.starTableList = new ArrayList<SedStarTable>();
    }

}
