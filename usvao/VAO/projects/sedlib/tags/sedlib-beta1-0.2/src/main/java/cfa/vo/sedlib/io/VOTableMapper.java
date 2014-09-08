/***********************************************************************
*
* File: io/VOTableMapper.java
*
* Author:  jmiller              Created: Fri Nov 12 12:26:00 EST 2010
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.sedlib.io;

import cfa.vo.sedlib.*;
import cfa.vo.sedlib.common.SedConstants;
import cfa.vo.sedlib.common.VOTableKeywords;
import cfa.vo.sedlib.common.SedException;

import org.w3c.dom.NodeList;
import uk.ac.starlink.table.*;
import uk.ac.starlink.votable.*;
import org.w3c.dom.NamedNodeMap;
import uk.ac.starlink.votable.dom.DelegatingAttr;

import java.util.List;
import java.io.IOException;
import java.util.logging.Logger;

/**
  Maps Sed objects to VOTable objects
*/
public class VOTableMapper extends SedMapper
{

    static Logger logger = Logger.getLogger ("cfa.vo.sedlib");
    String namespace;

    /**
      * Populates a Sed object with information from a VOTable object.
      *
      * @param data
      *    {@link VOElement}
      * @throws SedException 
      * @throws IOException
      */
    public Sed populateSed (Object data, Sed sed) 
    	throws SedException, IOException
    {

        if (sed == null)
           sed = new Sed ();

        VOElement root = (VOElement)data;

        // Get the RESOURCE elements using standard DOM methods.
        NodeList resources = root.getElementsByTagName (VOTableKeywords._RESOURCE);

        if (resources.getLength () > 1)
            throw new SedException ("There cannot be multiple resources in the VOTable. Only one SED per document");
        if (resources.getLength () == 0)
            throw new SedException ("The VOTable must have at least one resource.");
        
        VOElement resource = (VOElement) resources.item(0);
        VOElement[] tables = resource.getChildrenByName (VOTableKeywords._TABLE);
        TableElement te;
        Segment segment;
        
/*        // clear the sed before populating it
        sed.clear ();
*/
        this.namespace = this.extractNamespace (root);
        sed.setNamespace (this.namespace);
        // Creates a Segment from each TABLE and adds it to the sed
        for (int tblIdx = 0; tblIdx < tables.length; tblIdx++)
        {
            te = (TableElement)tables[tblIdx];
            segment =  this.extractSegmentFromTable (te);
            sed.addSegment (segment);
        }

        return sed;
    }


    /**
     * Populate the segment with the non-data parameter found in the table.
     * Creates a new segment for the table.
     * @param table
     *    {@link TableElement}
     * @return
     *    {@link Segment}
     */
    Segment extractSegmentFromTable(TableElement te)
       throws SedException, IOException
    {
        Segment currentSegment = new Segment ();

        VOElement[] groups = te.getChildrenByName(VOTableKeywords._GROUP);

        this.processSegmentTableData (currentSegment, te);

        this.setSegmentParams( currentSegment, te.getParams() );

        // loop through all the groups and populate the segment
        for(int grpIdx=0; grpIdx < groups.length; grpIdx++)
        {
            this.recurseGroup((GroupElement)groups[grpIdx], 
                              currentSegment, currentSegment);
        }

        return currentSegment;
    }


    /**
     * Populate the segment with data specific information. The input segment
     * will be updated.
     * @param table
     *    {@link TableElement}
     * @param segment
     *     {@link Segment}
     */
    private void  processSegmentTableData( Segment segment, TableElement te) 
           throws SedException, IOException
    {
        StarTable starTable = new VOStarTable( te );

        //
        //  Get a fresh array of data points for us to fill in and
        //  stick it in the segment.  We need to do this because the
        //  list will be traversed for each column and data added to
        //  the points so we cannot simply add a new Point as we loop
        //  over the rows.
        ArrayOfPoint pointArray = new ArrayOfPoint ();
        List<Point> pointList = pointArray.createPoint();

        for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++)
            pointList.add( iRow, new Point () );

        //TODO TEST that error of now all rows same length is handled
        //  A case for each column type in the table.  We find the
        //  right method for putting the data in the data list and
        //  iterate over the rows.
        // Now set the values of all the datapoints for this column
        this.setPointDataField(pointList, starTable);
      

        if (!pointList.isEmpty ())
            segment.setData( pointArray );

    }

    /**
     * Loop through the groups and subgroups, updating the segment 
     * with relevant parameters.
     */
    private void recurseGroup(GroupElement topgroup,
                              Object parent,
                              Segment currentSegment) throws SedException
    {

        // This will be used in several cases.
        Segment segment = null;
        String utype = topgroup.getAttribute(VOTableKeywords._UTYPE);
        GroupElement[] subgroups = topgroup.getGroups();
        Object newParent = parent;

        int utypeIdx = VOTableKeywords.getUtypeFromString( utype, this.namespace );

        //Figure out which GROUP we are dealing with
        switch(utypeIdx)
        {
            case VOTableKeywords.TARGET:
                if (parent instanceof Segment)
                    this.setTargetParams( ((Segment)parent).createTarget (), topgroup.getParams());
                else
                    logger.warning ("Invalid parent. Expected segment");

                newParent = ((Segment)parent).getTarget ();
                break;

            case VOTableKeywords.SEG_CS:
                if (parent instanceof Segment)
                {
                    CoordSys coordSys = new CoordSys();
                    ((Segment)parent).setCoordSys( coordSys );
                    coordSys.createCoordFrame().clear();
                    ParamElement[] params = topgroup.getParams();
                    this.setCoordSysParams( coordSys, params );
                    newParent = coordSys;
                }
                else
                    logger.warning ("Invalid parent. Expected segment");
                break;
            case VOTableKeywords.SEG_CS_SPACEFRAME:
                if (parent instanceof CoordSys)
                {
                    CoordSys coordSys = (CoordSys)parent;
                    SpaceFrame spaceFrame = new SpaceFrame();
                    coordSys.createCoordFrame().add( spaceFrame );   
                    this.setCoordFrameParams( spaceFrame, topgroup.getParams());
                }
                else
                    logger.warning ("Invalid parent. Expected CoordSys");
                break;

            case VOTableKeywords.SEG_CS_TIMEFRAME:
                if (parent instanceof CoordSys)
                {
                    CoordSys coordSys = (CoordSys)parent;
                    TimeFrame timeFrame = new TimeFrame();
                    coordSys.createCoordFrame().add( timeFrame );
                    this.setCoordFrameParams( timeFrame, topgroup.getParams());
                }
                else
                    logger.warning ("Invalid parent. Expected CoordSys");
                break;

            case VOTableKeywords.SEG_CS_SPECTRALFRAME:
                if (parent instanceof CoordSys)
                {
                    CoordSys coordSys = (CoordSys)parent;
                    SpectralFrame spectralFrame = new SpectralFrame();
                    coordSys.createCoordFrame().add( spectralFrame );
                    this.setCoordFrameParams( spectralFrame, topgroup.getParams());
                }
                else
                    logger.warning ("Invalid parent. Expected CoordSys");
                break;

            case VOTableKeywords.SEG_CS_REDFRAME:
                if (parent instanceof CoordSys)
                {
                    CoordSys coordSys = (CoordSys)parent;
                    RedshiftFrame redshiftFrame = new RedshiftFrame();
                    coordSys.createCoordFrame().add( redshiftFrame );
                    this.setCoordFrameParams( redshiftFrame, topgroup.getParams());
                }
                else
                    logger.warning ("Invalid parent. Expected CoordSys");
                break;

            case VOTableKeywords.SEG_CS_GENFRAME:
                if (parent instanceof CoordSys)
                {
                    CoordSys coordSys = (CoordSys)parent;
                    CoordFrame coordFrame = new CoordFrame();
                    coordSys.createCoordFrame().add( coordFrame );
                    this.setCoordFrameParams( coordFrame, topgroup.getParams());
                }
                else
                    logger.warning ("Invalid parent. Expected CoordSys");
                break;
            case VOTableKeywords.SEG_CHAR:
                if (parent instanceof Segment)
                {
 
                    segment = (Segment)parent;
 //                   this.setCharAxisParams(segment.createChar(), topgroup.getParams ());

                    newParent = segment.createChar ();
                }
                else
                    logger.warning ("Invalid parent. Expected Segment");
                break;

            case VOTableKeywords.SEG_CURATION:
            case VOTableKeywords.SEG_CURATION_CONTACT:
                Curation curation = null;
                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    curation = segment.createCuration ();
                }
                else if (parent instanceof Curation)
                    curation = (Curation)parent;
                else
                    logger.warning ("Invalid parent. Expected Segment or Curation");
                this.setCurationParams( curation, topgroup.getParams ());
                newParent = curation;
                break;
            case VOTableKeywords.SEG_DATAID:
                DataID dataId = null;

                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    if (segment.getDataID() == null)
                        segment.setDataID( new DataID());
                    dataId = segment.getDataID();
                }
                else if (parent instanceof DerivedData)
                    dataId = (DataID)parent;
                else
                    logger.warning ("Invalid parent. Expected Segment or DerivedData");

                this.setDataIDParams(dataId, topgroup.getParams ());

                newParent = dataId;
                break;

            case VOTableKeywords.SEG_DD:
            case VOTableKeywords.SEG_DD_REDSHIFT:
            case VOTableKeywords.SEG_DD_REDSHIFT_VALUE:
            case VOTableKeywords.SEG_DD_REDSHIFT_QUALITY:
            case VOTableKeywords.SEG_DD_REDSHIFT_RESOLUTION:
            case VOTableKeywords.SEG_DD_REDSHIFT_ACC:
            case VOTableKeywords.SEG_DD_REDSHIFT_ACC_CONFIDENCE:
            case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERR:
                DerivedData derived = null;
                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    derived = segment.createDerived();
                }
                else if ( parent instanceof DerivedData )
                    derived = (DerivedData)parent;
                else
                    logger.warning ("Invalid parent. Expected Segment or DerivedData");

                this.setDerivedDataParams(derived, topgroup.getParams ());

                newParent = derived;
                break;

            case VOTableKeywords.SEG_CHAR_SPATIALAXIS:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_SAMPPREC:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPPRECREFVAL:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_BOUNDS:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_SUPPORT:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC:
            case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC:

                CharacterizationAxis spatialAxis = null;
                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    spatialAxis = segment.createChar ().createSpatialAxis ();
                }
                else if (parent instanceof Characterization)
                    spatialAxis = ((Characterization)parent).createSpatialAxis();
                else if (parent instanceof CharacterizationAxis)
                    spatialAxis = (CharacterizationAxis)parent;
                else
                {
                    logger.warning ("Invalid parent. Expected Segment or Characterization or CharacterizationAxis");
                }

                if (utypeIdx == VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC)
                    this.setAccuracyParams( spatialAxis.createAccuracy (), 
                                          topgroup.getParams() );
                else if (utypeIdx == VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC)
                    this.setAccuracyParams( spatialAxis.createCoverage().
                                          createLocation().createAccuracy(),
                                          topgroup.getParams() );

                else
                    this.setCharAxisParams( spatialAxis, topgroup.getParams () );

                newParent = spatialAxis;
                break;

            case VOTableKeywords.SEG_CHAR_TIMEAXIS:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_SAMPPREC:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPPRECREFVAL:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_SUPPORT:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC:
            case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC:
                CharacterizationAxis timeAxis = null;

                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    timeAxis = segment.createChar ().createTimeAxis ();
                }
                else if (parent instanceof Characterization)
                    timeAxis = ((Characterization)parent).createTimeAxis();
                else if (parent instanceof CharacterizationAxis)
                    timeAxis = (CharacterizationAxis)parent;
                else
                    logger.warning ("Invalid parent. Expected Segment or Characterization or CharacterizationAxis");
               


                if (utypeIdx == VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC)
                    this.setAccuracyParams( timeAxis.createAccuracy (), 
                                          topgroup.getParams() );
                else if (utypeIdx == VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC)
                    this.setAccuracyParams( timeAxis.createCoverage().
                                          createLocation().createAccuracy(),
                                          topgroup.getParams() );
                else
                    this.setCharAxisParams( timeAxis, topgroup.getParams () );

                newParent = timeAxis;
                break;
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_SAMPPREC:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPPRECREFVAL:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_SUPPORT:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_RESOLUTION:
            case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_BINSIZE:

                SpectralCharacterizationAxis spectralAxis = null;
                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    spectralAxis = segment.createChar ().createSpectralAxis ();
                }
                else if (parent instanceof Characterization)
                    spectralAxis = ((Characterization)parent).createSpectralAxis();
                else if (parent instanceof SpectralCharacterizationAxis)
                    spectralAxis = (SpectralCharacterizationAxis)parent;
                else
                {
                    logger.warning ("Invalid parent. Expected Segment or Characterization or SpectralCharacterizationAxis");
                }

                // handle accuracy params specially
                if (utypeIdx == VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC)
                    this.setAccuracyParams( spectralAxis.createAccuracy (), 
                                          topgroup.getParams() );
                else if (utypeIdx == VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC)
                    this.setAccuracyParams( spectralAxis.createCoverage().
                                          createLocation().createAccuracy(),
                                          topgroup.getParams() );

                else
                    this.setCharAxisParams( spectralAxis, topgroup.getParams() );
                newParent = spectralAxis;
                break;

            case VOTableKeywords.SEG_CHAR_FLUXAXIS:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_SAMPPREC:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPPRECREFVAL:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_BOUNDS:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_SUPPORT:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC:
            case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC:
                CharacterizationAxis fluxAxis = null;

                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    fluxAxis = segment.createChar ().createFluxAxis ();
                }
                else if (parent instanceof Characterization)
                    fluxAxis = ((Characterization)parent).createFluxAxis();
                else if (parent instanceof CharacterizationAxis)
                    fluxAxis = (CharacterizationAxis)parent;
                else
                {
                    logger.warning ("Invalid parent. Expected Segment or Characterization or CharacterizationAxis");
                }
            
                if (utypeIdx == VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC)
                    this.setAccuracyParams( fluxAxis.createAccuracy (), 
                                          topgroup.getParams() );
                else if (utypeIdx == VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC)
                    this.setAccuracyParams( fluxAxis.createCoverage().
                                          createLocation().createAccuracy(),
                                          topgroup.getParams() );
                else

                    this.setCharAxisParams( fluxAxis, topgroup.getParams() );
                newParent = fluxAxis;
                break;
            case VOTableKeywords.SEG_CHAR_CHARAXIS:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_SAMPPREC:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_SAMPPREC_SAMPPRECREFVAL:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_COV:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_SUPPORT:
            case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC:
 //           case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC:
                List<CharacterizationAxis> charAxisList = null;
                CharacterizationAxis charAxis;

                if (parent instanceof Segment)
                {
                    segment = (Segment)parent;
                    charAxisList = segment.createChar ().createCharacterizationAxis ();
                }
                else if (parent instanceof Characterization)
                    charAxisList = ((Characterization)parent).createCharacterizationAxis();
                else if (parent instanceof List) {
					charAxisList = (List<CharacterizationAxis>)parent;
				} else
                {
                    logger.warning ("Invalid parent. Expected Segment or Characteriztion or CharacterizationAxis");
                }

                // if this is a new axis add it to the list
                // or if there are no axes on the list
                // otherwise use the last one on the list
                if ((utypeIdx == VOTableKeywords.SEG_CHAR_CHARAXIS) ||
                     charAxisList.isEmpty ())
                {
                    charAxis = new CharacterizationAxis ();
                    charAxisList.add (charAxis);
                }
                else
                    charAxis = charAxisList.get (charAxisList.size () - 1);


                if (utypeIdx == VOTableKeywords.SEG_CHAR_CHARAXIS_ACC)
                    this.setAccuracyParams( charAxis.createAccuracy (),
                                          topgroup.getParams() );
 /*               else if (utypeIdx == VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC)
                    this.setAccuracyParams( charAxis.createCoverage().
                                          createLocation().createAccuracy(),
                                          topgroup.getParams() );
 */
                else

                    this.setCharAxisParams( charAxis, topgroup.getParams() );
                newParent = charAxisList;
                break;

            case VOTableKeywords.SEG_DATA:
            case VOTableKeywords.SEG_DATA_SPECTRALAXIS:
            case VOTableKeywords.SEG_DATA_SPECTRALAXIS_RESOLUTION:
            case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC:
            case VOTableKeywords.SEG_DATA_FLUXAXIS:
            case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC:
            case VOTableKeywords.SEG_DATA_TIMEAXIS:
            case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC:
            case VOTableKeywords.SEG_DATA_BGM:
            case VOTableKeywords.SEG_DATA_BGM_ACC:

                List<Point> data = null;

                if (parent instanceof Segment )
                {
                	ArrayOfGenPoint points;
                    segment = (Segment)parent;
                    if (!segment.isSetData ())
                        segment.setData (new ArrayOfPoint ());
                    points = ((Segment)parent).getData();
                    if (points instanceof ArrayOfPoint)
                        data = ((ArrayOfPoint)points).createPoint ();
                }
                else if ( parent instanceof ArrayOfPoint)
                {
                    data = ((ArrayOfPoint)parent).createPoint ();
                }
                else if ( parent instanceof List )
                {
                    data = (List<Point>)parent;
                }
                else
                    logger.warning ("Invalid parent. Expected Segment or ArrayOfPoint or List<Point>");

                this.setDataParams( data, topgroup.getParams() );
                newParent = data;
                break;

            case VOTableKeywords.CUSTOM:
                if (parent instanceof Segment )
                    this.setSegmentParams ((Segment)parent, topgroup.getParams());
                else if (parent instanceof Target )
                    this.setTargetParams ((Target)parent, topgroup.getParams());
                else
                    logger.warning ("Invalid parent. Expected Segment or Target");
                break;

            default:
                String msg = null;
                //logger.warning ( msg );
                //SEDMessager.addMessage( msg , SEDMessager.FINEST);
                if ( utype.equals( "" ) )
                    msg = "The utype has not been set. The group could be determined. The group's data will be ignored.";
                else
                    msg = "The utype, "+utype+", is not recognized. The group could not be determined. The group's data will be ignored.";

                logger.warning (msg);
        }

        for(int ii=0; ii<subgroups.length; ii++)
            this.recurseGroup(subgroups[ii],
                               newParent,
                               currentSegment);

    }

    /**
     * Populate the target from the list params 
     */
    private void setTargetParams(Target target, ParamElement[] params) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch ( utypeIdx )
            {
                case VOTableKeywords.TARGET_NAME:
                    target.setName( this.newTextParam( params[ pii ]));
                    break;
                case VOTableKeywords.TARGET_DESCRIPTION:
                    target.setDescription( this.newTextParam( params[ pii ]));
                    break;
                case VOTableKeywords.TARGET_CLASS:
                    target.setTargetClass( this.newTextParam( params[ pii ]));
                    break;
                case VOTableKeywords.TARGET_SPECTRALCLASS:
                    target.setSpectralClass( this.newTextParam( params[ pii ]));
                    break;
                case VOTableKeywords.TARGET_POS:
                    target.setPos( this.newPositionParam( params[ pii ] ));
                    break;
                case VOTableKeywords.TARGET_VARAMPL:
                    target.setVarAmpl( this.newDoubleParam (params[ pii ]));
                    break;
                case VOTableKeywords.TARGET_REDSHIFT:
                    target.setRedshift( this.newDoubleParam( params[ pii ]));
                    break;
                default:
                    String datatype = params[pii].getDatatype ();
                    String utypeNamespace = VOTableKeywords.getNamespace(paramUtype);
                    Param pp = null;

                    if (!utypeNamespace.equals(this.namespace))
                    {
                    	logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
                    	break;
                    }

                    if (datatype.equals ("int"))
                        pp = this.newIntParam (params[pii]);
                    else if (datatype.equals ("double"))
                        pp = this.newDoubleParam (params[pii]);
                    else if (datatype.equals ("char"))
                        pp = this.newTextParam (params[pii]);
                    else if (datatype.equals ("date"))
                        pp = new DateParam (params[pii].getValue(),
                                               params[pii].getName(),
                                               params[pii].getUcd());
                    else
                        logger.warning ("The data type, "+datatype+" for the custom param is not supported. Supported datatypes include char, int, double, date");


                    if (pp != null)
                        target.createCustomParams ().getParam ().add (pp);

                    break;
            }
        }
    }

    /**
     * Populate the Segment from the list params
     */
    private void setSegmentParams( Segment segment,
                ParamElement[] params ) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );
            
            switch (utypeIdx)
            {
                case VOTableKeywords.DATAMODEL:
                    // validate against the data model
                    break;
                case VOTableKeywords.TYPE:
                    segment.setType( this.newTextParam( params[ pii ] ));
                    break;
                case VOTableKeywords.LENGTH:
                    break;
                case VOTableKeywords.TIMESI:
                    segment.setTimeSI( this.newTextParam( params[ pii ] ));
                    break;
                case VOTableKeywords.SPECTRALSI:
                    segment.setSpectralSI( this.newTextParam( params[ pii ] ));
                    break;
                case VOTableKeywords.FLUXSI:
                    segment.setFluxSI( this.newTextParam( params[ pii ] ));
                    break;
                default:

                    String datatype = params[pii].getDatatype ();
                    String utypeNamespace = VOTableKeywords.getNamespace(paramUtype);
                    Param pp = null;

                    if (!utypeNamespace.equals(this.namespace))
                    {
                    	logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
                    	break;
                    }
                    
                    if (datatype.equals ("int"))
                        pp = this.newIntParam (params[pii]);
                    else if (datatype.equals ("double"))
                        pp = this.newDoubleParam (params[pii]);
                    else if (datatype.equals ("char"))
                        pp = this.newTextParam (params[pii]);
                    else if (datatype.equals ("date"))
                        pp = new DateParam (params[pii].getValue(),
                                               params[pii].getName(),
                                               params[pii].getUcd());
                    else
                        logger.warning ("The data type, "+datatype+" for the custom param is not supported. Supported datatypes include char, int, double, date");

                    if (pp != null)
                        segment.createCustomParams ().createParam ().add (pp);
                    break;
            }
        }
    }


    /**
     * This method takes care of PARAMs found within the Data section.
     * This means that the data IS constant for each row of the table.
     */
    private void setDataParams( List<Point> data, ParamElement[] params) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch ( utypeIdx )
            {
                // for Flux
                case VOTableKeywords.SEG_DATA_FLUXAXIS_VALUE :
                	for (Point pnt : data)
                        pnt.createFluxAxis().setValue(
                                     this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERRLOW:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setStatErrLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERRHIGH:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setStatErrHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_SYSERR:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setSysError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_QUALITY:
                    for (Point pnt : data)
                        pnt.createFluxAxis().setQuality( 
                                    this.newIntParam( params[pii] ) );
                    break;
                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINLOW:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setBinLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINHIGH:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setBinHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINSIZE:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setBinSize(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERR:
                    for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setStatError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_RESOLUTION:
                    for (Point pnt : data)
                        pnt.createFluxAxis().setResolution(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_CONFIDENCE:
                	for (Point pnt : data)
                        pnt.createFluxAxis().createAccuracy().setConfidence(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                // for Spectral
                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_VALUE :
                    for (Point pnt : data)
                        pnt.createSpectralAxis().setValue(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERRLOW:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setStatErrLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERRHIGH:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setStatErrHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;
                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_SYSERR:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setSysError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_RESOLUTION:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().setResolution(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINLOW:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setBinLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINHIGH:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setBinHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINSIZE:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setBinSize(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERR:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setStatError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_CONFIDENCE:
                    for (Point pnt : data)
                        pnt.createSpectralAxis().createAccuracy().setConfidence(
                                    this.newDoubleParam( params[pii] ) );
                    break;


                // for Time
                case VOTableKeywords.SEG_DATA_TIMEAXIS_VALUE :
                    for (Point pnt : data)
                        pnt.createTimeAxis().setValue(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERRLOW:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setStatErrLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERRHIGH:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setStatErrHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_SYSERR:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setSysError(
                                    this.newDoubleParam( params[pii] ) );
                    break;
                case VOTableKeywords.SEG_DATA_TIMEAXIS_RESOLUTION:
                    for (Point pnt : data)
                        pnt.createTimeAxis().setResolution(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINLOW:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setBinLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINHIGH:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setBinHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINSIZE:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setBinSize(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERR:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setStatError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_CONFIDENCE:
                    for (Point pnt : data)
                        pnt.createTimeAxis().createAccuracy().setConfidence(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                // for background model
                case VOTableKeywords.SEG_DATA_BGM_VALUE :
                    for (Point pnt : data)
                        pnt.createBackgroundModel().setValue(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERRLOW:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setStatErrLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERRHIGH:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setStatErrHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_SYSERR:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setSysError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_QUALITY:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().setQuality( 
                                    this.newIntParam( params[pii] ) );
                    break;
                case VOTableKeywords.SEG_DATA_BGM_ACC_BINLOW:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setBinLow(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_BINHIGH:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setBinHigh(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_BINSIZE:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setBinSize(
                                    this.newDoubleParam( params[pii] ) );
                    break;
                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERR:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setStatError(
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_RESOLUTION:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().setResolution (
                                    this.newDoubleParam( params[pii] ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_CONFIDENCE:
                    for (Point pnt : data)
                        pnt.createBackgroundModel().createAccuracy().setConfidence( 
                                    this.newDoubleParam( params[pii] ) );
                    break;

                default:
                    //TODO store unknown fields
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
            }
        }
    }

    /**
     * Populate the DerivedData from the list params
     */
    private void setDerivedDataParams(DerivedData derived, ParamElement[] params)
           throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);

            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch(utypeIdx)
            {
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINHIGH:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINLOW:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINSIZE:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERRLOW:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_SYSERR:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERR:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_CONFIDENCE:
                    Accuracy acc = derived.createRedshift ().createAccuracy ();

                    ParamElement[] paramArray = { params[pii] };
                    this.setAccuracyParams( acc, paramArray );
                    break;

                case VOTableKeywords.SEG_DD_REDSHIFT_RESOLUTION:
                case VOTableKeywords.SEG_DD_REDSHIFT_QUALITY:
                case VOTableKeywords.SEG_DD_REDSHIFT_VALUE:
                {
                    SedQuantity sqt = derived.createRedshift ();
                    switch( utypeIdx )
                    {
                        case VOTableKeywords.SEG_DD_REDSHIFT_RESOLUTION:
                            sqt.setResolution ( this.newDoubleParam(params[pii]));
                            break;

                        case VOTableKeywords.SEG_DD_REDSHIFT_QUALITY:
                            sqt.setQuality ( this.newIntParam(params[pii]));
                            break;

                        case VOTableKeywords.SEG_DD_REDSHIFT_VALUE:
                            sqt.setValue ( this.newDoubleParam(params[pii]));
                            break;
                    }
                    break;
                }
                case VOTableKeywords.SEG_DD_VARAMPL:
                    derived.setVarAmpl( this.newDoubleParam(params[pii]));
                    break;

                case VOTableKeywords.SEG_DD_SNR:
                    derived.setSNR( this.newDoubleParam(params[pii]));
                    break;

                default:
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
            }
        }
    }

    /**
     * Populate the DataId from the list params
     */
    private void setDataIDParams(DataID dataId, ParamElement[] params) throws SedException
    {
        for(int ii=0; ii < params.length; ii++)
        {
            String paramUtype = params[ii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );


            switch( utypeIdx )
            {
                case VOTableKeywords.SEG_DATAID_TITLE:
                    dataId.setTitle ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_CREATOR:
                    dataId.setCreator ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_CREATORDID:
                    dataId.setCreatorDID ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_VERSION:
                    dataId.setVersion ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_BANDPASS:
                    dataId.setBandpass ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_INSTRUMENT:
                    dataId.setInstrument ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_LOGO:
                    dataId.setLogo( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_DATASOURCE:
                    dataId.setDataSource ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_CREATIONTYPE:
                    dataId.setCreationType ( this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_DATE:
                    dataId.setDate ( new DateParam (params[ii].getValue(),
                                                    params[ii].getName(),
                                                    params[ii].getUcd()));
                    break;

                case VOTableKeywords.SEG_DATAID_DATASETID:
                    dataId.setDatasetID (  this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_COLLECTION:
                    dataId.createCollection().add(  this.newTextParam(params[ii]));
                    break;

                case VOTableKeywords.SEG_DATAID_CONTRIBUTOR:
                    dataId.createContributor().add(  this.newTextParam(params[ii]));
                    break;

                default:
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");

            }
        }
    }


    /**
     * Populate the Curation from the list params
     */
    private void setCurationParams( Curation curation, ParamElement[] params) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch (utypeIdx)
            {
                case VOTableKeywords.SEG_CURATION_PUBLISHER:
                    curation.setPublisher( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_REF:
                    curation.setReference( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_PUBID:
                    curation.setPublisherID( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_PUBDID:
                    curation.setPublisherDID( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_DATE:
                    curation.setDate( new DateParam(
                                params[ pii ].getValue(),
                                params[ pii ].getName(),
                                params[ pii ].getUcd()
                                ) );
                    break;
                case VOTableKeywords.SEG_CURATION_RIGHTS:
                    curation.setRights( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_VERSION:
                    curation.setVersion( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_CONTACT_NAME:
                    curation.createContact().setName( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CURATION_CONTACT_EMAIL:
                    curation.createContact().setEmail( this.newTextParam(params[pii]));
                    break;
                default:
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
                    break;

            }
        }
    }


    /**
     * Populate the CoordFrame from the list params
     */
    private void setCoordFrameParams( CoordFrame coordFrame, ParamElement[] params) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String utype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( utype, this.namespace );

            switch (utypeIdx)
            {
                case VOTableKeywords.SEG_CS_GENFRAME_ID:
                case VOTableKeywords.SEG_CS_REDFRAME_ID:
                case VOTableKeywords.SEG_CS_SPECTRALFRAME_ID:
                case VOTableKeywords.SEG_CS_TIMEFRAME_ID:
                case VOTableKeywords.SEG_CS_SPACEFRAME_ID:
                    coordFrame.setId( params[ pii ].getValue() );
                    break;
                case VOTableKeywords.SEG_CS_GENFRAME_UCD:
                case VOTableKeywords.SEG_CS_REDFRAME_UCD:
                case VOTableKeywords.SEG_CS_SPECTRALFRAME_UCD:
                case VOTableKeywords.SEG_CS_TIMEFRAME_UCD:
                case VOTableKeywords.SEG_CS_SPACEFRAME_UCD:
                    if (params[ pii ].getValue() != null)
                        coordFrame.setUcd( params[ pii ].getValue() );
                    break;
                case VOTableKeywords.SEG_CS_GENFRAME_NAME:
                case VOTableKeywords.SEG_CS_REDFRAME_NAME:
                case VOTableKeywords.SEG_CS_SPECTRALFRAME_NAME:
                case VOTableKeywords.SEG_CS_TIMEFRAME_NAME:
                case VOTableKeywords.SEG_CS_SPACEFRAME_NAME:
                    coordFrame.setName( params[ pii ].getValue() );

                    // account for when the ucd falls in the same
                    // param as the name
                    if (params[ pii ].getUcd () != null)
                        coordFrame.setUcd( params[ pii ].getUcd ());
                    break;
                case VOTableKeywords.SEG_CS_GENFRAME_REFPOS:
                case VOTableKeywords.SEG_CS_REDFRAME_REFPOS:
                case VOTableKeywords.SEG_CS_SPECTRALFRAME_REFPOS:
                case VOTableKeywords.SEG_CS_TIMEFRAME_REFPOS:
                case VOTableKeywords.SEG_CS_SPACEFRAME_REFPOS:
                    coordFrame.setReferencePosition(params[pii].getValue());
                    break;
                case VOTableKeywords.SEG_CS_SPACEFRAME_EQUINOX:
                    try {
                        ((SpaceFrame)coordFrame).setEquinox (
                                          this.newDoubleParam (params[pii]));
                    } catch (ClassCastException e) {
                        logger.warning ("The utype, "+ utype + " is expected to be part of SpaceFrame. The parameter will be ignored.");

                    }
                    break;
                case VOTableKeywords.SEG_CS_TIMEFRAME_ZERO:
                    try {
                        ((TimeFrame)coordFrame).setZero (
                                          this.newDoubleParam (params[pii]));
                    } catch (ClassCastException e) {
                        logger.warning ("The utype, "+ utype + " is expected to be part of TimeFrame. The parameter will be ignored.");
                    }
                    break;
                case VOTableKeywords.SEG_CS_SPECTRALFRAME_REDSHIFT:
                    try {
                        ((SpectralFrame)coordFrame).setRedshift ( 
                                          this.newDoubleParam (params[pii]));
                    } catch (ClassCastException e) {
                        logger.warning ("The utype, "+ utype + " is expected to be part of SpectralFrame. The parameter will be ignored.");
                    }
                    break;
                case VOTableKeywords.SEG_CS_REDFRAME_DOPPLERDEF:
                    try {
                        ((RedshiftFrame)coordFrame).setDopplerDefinition(params[pii].getValue());
                    } catch (ClassCastException e) {
                        logger.warning ("The utype, "+ utype + " is expected to be part of RedshiftFrame. The parameter will be ignored.");
                    }
                    break;
                default:
                    logger.warning ("The utype, "+ utype + " is not recognized and will be ignored.");
            }
        }
    }


    /**
     * Populate the CoordSys from the list params excluding the CoordFrame members.
     */
    private void setCoordSysParams( CoordSys coordSys, ParamElement[] params ) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {

            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch (utypeIdx)
            {
                case VOTableKeywords.SEG_CS_ID:
                    coordSys.setId( params[pii].getValue() );
                    break;
                case VOTableKeywords.SEG_CS_UCD:
                    coordSys.setUcd( params[pii].getValue() );
                    break;
                case VOTableKeywords.SEG_CS_TYPE:
                    coordSys.setType( params[pii].getValue() );
                    break;
                case VOTableKeywords.SEG_CS_HREF:
                    coordSys.setHref( params[pii].getValue() );
                    break;
                default:
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");

            }
        }
    }

    /**
     * Populate the CharacterizationAxis from the list params
     */
    private void setCharAxisParams( CharacterizationAxis charAxis, ParamElement[] params) throws SedException
    {
        for(int pii=0; pii < params.length; pii++)
        {
            String paramUtype = params[pii].getAttribute(VOTableKeywords._UTYPE);

            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );


            switch(utypeIdx)
            {
                case VOTableKeywords.SEG_CHAR_CHARAXIS_NAME:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_NAME:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_NAME:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_NAME:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_NAME:
                    charAxis.setName( params[ pii ].getValue() );
                    if (params[ pii ].getUcd() != null)
                        charAxis.setUcd (params[ pii ].getUcd());
                    if (params[ pii ].getUnit() != null)
                        charAxis.setUnit (params[ pii ].getUnit());
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_UNIT:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_UNIT:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_UNIT:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_UNIT:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_UNIT:
                    if (params[ pii ].getValue () != null)
                        charAxis.setUnit( params[ pii ].getValue() );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_UCD:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_UCD:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_UCD:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_UCD:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_UCD:
                    if (params[ pii ].getValue () != null)
                        charAxis.setUcd( params[ pii ].getValue() );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_CAL:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_CAL:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_CAL:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_CAL:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_CAL:
                    charAxis.setCalibration( this.newTextParam(params[pii]));
                    break;
                case VOTableKeywords.SEG_CHAR_CHARAXIS_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_RESOLUTION:
                    charAxis.setResolution( this.newDoubleParam(params[pii]));
                    break;

                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_RESPOW:
                    ((SpectralCharacterizationAxis)charAxis).setResPower( 
                                  this.newDoubleParam(params[ pii ]));
                    break;

                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_SYSERR:

                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_SYSERR:

                    logger.warning ("The accuracy utype, "+ paramUtype + " is expected to be part of the Accuracy group. This parameter will be ignored.");
                
//                    charAxis.getAccuracy().setConfidence(
//                                ParamUtils.doubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_BINSIZE:
                    this.setAccuracyParams( charAxis.getAccuracy(), params );
                    break;

//                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_RESOLUTION:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_RESOLUTION:

                    CoverageLocation location = charAxis.createCoverage().createLocation();
                    location.setResolution(this.newDoubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS_EXTENT:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_BOUNDS_EXTENT:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS_EXTENT:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_BOUNDS_EXTENT:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_EXTENT:
                    charAxis.createCoverage().createBounds().setExtent(
                                this.newDoubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS_START:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS_START:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_START:
                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS_MIN:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_BOUNDS_MIN:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS_MIN:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_BOUNDS_MIN:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MIN:
                    charAxis.createCoverage().createBounds().createRange().setMin(
                                this.newDoubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS_STOP:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS_STOP:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_STOP:
                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_BOUNDS_MAX:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_BOUNDS_MAX:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_BOUNDS_MAX:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_BOUNDS_MAX:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MAX:
                    charAxis.createCoverage().createBounds().createRange().setMax(
                                this.newDoubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_VALUE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_VALUE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_VALUE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_VALUE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_VALUE:

                    String paramValues = params[pii].getValue ();

                    if (paramValues != null)
                    {
                         DoubleParam valueList[] = charAxis.createCoverage().createLocation ().createValue ();
         
                         String[] values = paramValues.split( IOConstants.LIST_SEPARATOR_PATTERN );                
                         for (int ii=0; ii<values.length; ii++)
                         {
                             valueList[ii] = new DoubleParam (values[ii],
                                                       params[pii].getName (),
                                                       params[pii].getUcd (),
                                                       params[pii].getUnit ());
                         }
                    }
                    break;

//                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_SUPPORT_EXTENT:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_SUPPORT_EXTENT:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_SUPPORT_EXTENT:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_SUPPORT_EXTENT:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_EXTENT:
                    charAxis.createCoverage().createSupport().setExtent(
                                this.newDoubleParam( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_SUPPORT_AREA:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_SUPPORT_AREA:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_SUPPORT_AREA:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_SUPPORT_AREA:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_AREA:
                    charAxis.createCoverage().createSupport().setArea(
                                new SkyRegion (params[pii].getValue(),
                                               params[pii].getName(),
                                               params[pii].getUcd()));
                    break;

 //               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_SUPPORT_RANGE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_SUPPORT_RANGE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_SUPPORT_RANGE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_SUPPORT_RANGE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_RANGE:
                    charAxis.createCoverage().createSupport().createRange().add(
                                this.newInterval( params[ pii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_SAMPPREC_SAMPEXT:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPEXT:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPEXT:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPEXT:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPEXT:
                    charAxis.createSamplingPrecision().setSampleExtent(
                                this.newDoubleParam( params[ pii ]) );
                    break;
                case VOTableKeywords.SEG_CHAR_CHARAXIS_SAMPPREC_SAMPPRECREFVAL_FILL:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPPRECREFVAL_FILL:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPPRECREFVAL_FILL:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL:
                    charAxis.createSamplingPrecision()
                        .createSamplingPrecisionRefVal().setFillFactor(
                                this.newDoubleParam( params[ pii ]) );
                    break;

                default:
                    logger.warning ("The utype, "+paramUtype+", is not recognized and will be ignored.");
            }
        }
    }

    /**
     * Populate the Accuracy from the list params
     */
    private void setAccuracyParams( Accuracy acc, ParamElement[] params ) throws SedException
    {
        for( int ii = 0; ii < params.length; ii++ )
        {
            String paramUtype = params[ ii ].getAttribute(VOTableKeywords._UTYPE);
            int utypeIdx = VOTableKeywords.getUtypeFromString( paramUtype, this.namespace );

            switch( utypeIdx )
            {
                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_BINHIGH:
 //               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINHIGH:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINHIGH:
                    acc.setBinHigh ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_BINLOW:
//                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINLOW:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINLOW:
                    acc.setBinLow ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_BINSIZE:
 //               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINSIZE:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_BINSIZE:
                    acc.setBinSize ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_STATERRHIGH:
//                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRHIGH:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERRHIGH:
                    acc.setStatErrHigh ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_STATERRLOW:
//                case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERRLOW:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRLOW:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERRLOW:
                    acc.setStatErrLow ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_SYSERR:
 //               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_SYSERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_SYSERR:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_SYSERR:
                    acc.setSysError ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_STATERR:
 //               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_STATERR:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERR:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_STATERR:
                    acc.setStatError ( this.newDoubleParam( params[ ii ]) );
                    break;

                case VOTableKeywords.SEG_CHAR_CHARAXIS_ACC_CONFIDENCE:
//               case VOTableKeywords.SEG_CHAR_CHARAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_FLUXAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_TIMEAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_CONFIDENCE:
                case VOTableKeywords.SEG_DD_REDSHIFT_ACC_CONFIDENCE:
                    acc.setConfidence ( this.newDoubleParam( params[ ii ]) );
                    break;

                default:
                    logger.warning ("The utype, "+ paramUtype + " is not recognized and will be ignored.");
 
            }
        }
    }

    /**
     * Populate the data points from the data stored in the StarTable.
     * The point
     */
    private void setPointDataField(List<Point> pointList,
                StarTable starTable)
                throws SedException, IOException
    {

        for ( int iCol = 0; iCol < starTable.getColumnCount(); iCol++ )
        {
            ColumnInfo infoCol = starTable.getColumnInfo(iCol);
            String utype = infoCol.getUtype ();
            int utypeIdx = VOTableKeywords.getUtypeFromString( utype,
                                                               this.namespace );

            switch ( utypeIdx )
            {
                // for Flux
                case VOTableKeywords.SEG_DATA_FLUXAXIS_VALUE :
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++)
                        pointList.get( iRow ).createFluxAxis().setValue( 
                                     this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERRLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setStatErrLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERRHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setStatErrHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_SYSERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setSysError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_QUALITY:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().setQuality( this.newIntParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setBinLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setBinHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_BINSIZE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setBinSize( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_STATERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setStatError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_RESOLUTION:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().setResolution( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_FLUXAXIS_ACC_CONFIDENCE:
                	for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createFluxAxis().createAccuracy().setConfidence( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                // for Spectral
                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_VALUE :
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().setValue( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERRLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setStatErrLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERRHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setStatErrHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;
                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_SYSERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setSysError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_RESOLUTION:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().setResolution( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setBinLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setBinHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_BINSIZE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setBinSize( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_STATERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setStatError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_SPECTRALAXIS_ACC_CONFIDENCE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createSpectralAxis().createAccuracy().setConfidence( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;


                // for Time
                case VOTableKeywords.SEG_DATA_TIMEAXIS_VALUE :
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().setValue( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERRLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setStatErrLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERRHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setStatErrHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_SYSERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setSysError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;
                case VOTableKeywords.SEG_DATA_TIMEAXIS_RESOLUTION:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().setResolution( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setBinLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setBinHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_BINSIZE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setBinSize( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_STATERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setStatError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_TIMEAXIS_ACC_CONFIDENCE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createTimeAxis().createAccuracy().setConfidence( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                // for background model
                case VOTableKeywords.SEG_DATA_BGM_VALUE :
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().setValue( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERRLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setStatErrLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERRHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setStatErrHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_SYSERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setSysError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_QUALITY:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().setQuality( this.newIntParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;
                case VOTableKeywords.SEG_DATA_BGM_ACC_BINLOW:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setBinLow( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_BINHIGH:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setBinHigh( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_BINSIZE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setBinSize( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_STATERR:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setStatError( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_RESOLUTION:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().setResolution ( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                case VOTableKeywords.SEG_DATA_BGM_ACC_CONFIDENCE:
                    for ( int iRow = 0; iRow < starTable.getRowCount(); iRow++ )
                        pointList.get( iRow ).createBackgroundModel().createAccuracy().setConfidence( 
                                    this.newDoubleParam( starTable.getCell( iRow, iCol ), infoCol ) );
                    break;

                default:
                    //TODO store unknown fields 
                    logger.warning ("The utype, "+ utype + " is not recognized and will be ignored.");
                break;
            }
        }
    }


    /**
     *  Create a DoubleParam from a ParamElement
     */
    private DoubleParam newDoubleParam (ParamElement param)
    {
        if (param == null)
            return null;
        return new DoubleParam (param.getValue(),
                             param.getName(),
                             param.getUcd(),
                             param.getUnit());
    }

    /**
     *  Create a DoubleParam from StarTable information
     */
    private DoubleParam newDoubleParam (Object value, ColumnInfo colInfo)
    {
        return new DoubleParam (value.toString (),
                             colInfo.getName(),
                             colInfo.getUCD(),
                             colInfo.getUnitString());
    }

    /**
     *  Create an IntParam from a ParamElement
     */
    private IntParam newIntParam (ParamElement param)
    {
        if (param == null)
            return null;
        return new IntParam (param.getValue(),
                             param.getName(),
                             param.getUcd(),
                             param.getUnit());
    }

    /**
     *  Create a IntParam from a StarTable information
     */
    private IntParam newIntParam (Object value, ColumnInfo colInfo)
    {
        return new IntParam (value.toString (),
                             colInfo.getName(),
                             colInfo.getUCD(),
                             colInfo.getUnitString());
    }


    /**
     *  Create a TextParam from a ParamElement
     */
    private TextParam newTextParam (ParamElement param)
    {
        if (param == null)
            return null;
        return new TextParam (param.getValue(),
                             param.getName(),
                             param.getUcd());
    }


    /**
     *  Create an IntervalParam from a ParamElement
     */
    private Interval newInterval (ParamElement param)
    {
        if (param == null)
            return null;

        Interval interval = new Interval();
        String[] values = param.getValue().split( IOConstants.LIST_SEPARATOR_PATTERN );
        String ucd = param.getUcd();
        String unit = param.getUnit();
        
       
        interval.setMin (new DoubleParam (values[0], "", ucd, unit));
        interval.setMax (new DoubleParam (values[1], "", ucd, unit));

        return interval;
    }


    /**
     *  Create a PositionParam from a ParamElement
     */
    private PositionParam newPositionParam ( ParamElement param )
    {

        String pos = param.getValue();
        String name = param.getName();
        String ucd = param.getUcd();
        String unit = param.getUnit();

        if ( pos == null )
            return null;
        


        //Note that a PositionParamType has no name field so we
        //store the name in each of the DoubleParamtype.
        PositionParam newPos = new PositionParam();

        String[] values = pos.split( IOConstants.LIST_SEPARATOR_PATTERN );

        DoubleParam posValues[] = newPos.createValue();

        for ( int ii = 0; ii < values.length; ii++ )
            posValues[ii] = new DoubleParam( values[ii], name, ucd, unit );

        return newPos;
    }

    /**
     *  Get the namespace from the VOTable associated with the spectrum schema.
     */
    private String extractNamespace (VOElement element)
    {
        NamedNodeMap map = element.getAttributes();
        String name, value;
        String namespace = null;
        for (int ii=0; ii<map.getLength  (); ii++)
        {
            DelegatingAttr attr = (DelegatingAttr)map.item(ii);
            name = attr.getName();
            value = attr.getValue();
            
            if (name.matches ("^xmlns:.*$") && value.matches ("^.*Spectrum.*\\.xsd$"))
            {
                if (namespace == null)
                    namespace = name.replaceFirst ("^.*xmlns:","");
                else
                    logger.warning ("Multiple namespace declarations were found for " +
                             SedConstants.SPECTRUM_SCHEMA_VERSION +
                             " while searching for Spectrum*.xsd." +
                             " The following namespace will be used, "+
                             namespace);
            }
        }
        return namespace;
    }
}  
