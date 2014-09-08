/////////////////////////////
// FootprintLayer()
/////////////////////////////
ASTROVIEW.FootprintLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );
    
    //
    // For each footprint in the layerData object, create a THREE.Line Object
    // and add it as a child to this layerData
    //
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {             
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            this.createFootprint(row);
        }
    }
           
    // NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.FootprintLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.FootprintLayer.prototype.constructor = ASTROVIEW.FootprintLayer;

/////////////////////////////
// createFootprint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(row)
{
    // Validate input
    if (row == undefined || row.footprint == undefined) return;

    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var type = "";
    var fplist = null;
    
    // Trim, Upper Case, Remove all parens: () 
    var upper = trim(row.footprint.toUpperCase());
    var fp = upper.replace(/\(/g,' ').replace(/\)/g, ' ');
    
    // Split the footprint string
    if (fp.indexOf(ASTROVIEW.POLYGON_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_J2000_RE);
        type = ASTROVIEW.POLYGON_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POLYGON_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_ICRS_RE);
        type = ASTROVIEW.POLYGON_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_ICRS_RE);
        type = ASTROVIEW.CIRCLE_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_J2000_RE);
        type = ASTROVIEW.CIRCLE_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POINT_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POINT_J2000_RE);
        type = ASTROVIEW.POINT_J2000;
    }
    
    // If we found no valid fplist, bail
    if (fplist == null || fplist.length < 2)
    {
        this.alert("Illegal Footprint:" + footprint + "\n\nExpected Syntax: \n\n POLYGON [J2000|ICRS] [ra dec]\n CIRCLE [J2000|ICRS] [ra dec]\n POINT [ra dec]", "Illegal Footprint");
        return;
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolygons(fplist, row);
            break;
        }
        case ASTROVIEW.CIRCLE_ICRS:
        case ASTROVIEW.CIRCLE_J2000:
        {
            this.createCircles(fplist, row);
            break;
        }
        case ASTROVIEW.POINT_J2000:
        {
            this.createPoints(fplist, row);
            break;
        }
        default:
        {
            this.alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolygons()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolygons = function(fplist, row)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var polyline = trim(fplist[i]);
        if (polyline.length > 0)
        {
            this.createPolyline(polyline, row);     // GraphicsLayer.createPolyline()
        }
    }
}

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(fplist, row)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var circle = trim(fplist[i]);
        if (circle.length > 0)
        {
            this.createCircle(circle, row);         // GraphicsLayer.createCircle()
        }
    }
}

/////////////////////////////
// createPoints()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoints = function(fplist, row)
{
    for (var i=0; i<fplist.length; i++)
    {
        var point = trim(fplist[i]);
        if (point.length > 0)
        {
            this.createPoint(point, row);
        }
    }
}

/////////////////////////////
// createPoint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoint = function(point, row)
{
    var coords = point.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
            this.createParticle(ra, dec, row, 'plus');     // GraphicsLayer.createParticle()
        }
    }
    else
    {
        this.alert("Footprint POINT Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + point, "Unable to Create Footprint");
    }
}

/////////////////////////////
// Footprint Constants
/////////////////////////////
ASTROVIEW.POLYGON_J2000 = 'POLYGON J2000';
ASTROVIEW.POLYGON_ICRS  = 'POLYGON ICRS';

ASTROVIEW.POLYGON_J2000_RE = new RegExp(ASTROVIEW.POLYGON_J2000 + "\\s+");
ASTROVIEW.POLYGON_ICRS_RE  = new RegExp(ASTROVIEW.POLYGON_ICRS + "\\s+");

ASTROVIEW.CIRCLE_J2000  = "CIRCLE J2000";
ASTROVIEW.CIRCLE_ICRS   = "CIRCLE ICRS";

ASTROVIEW.CIRCLE_J2000_RE = new RegExp(ASTROVIEW.CIRCLE_J2000 + "\\s+");
ASTROVIEW.CIRCLE_ICRS_RE  = new RegExp(ASTROVIEW.CIRCLE_ICRS + "\\s+");

ASTROVIEW.POINT_J2000  = "POINT";
ASTROVIEW.POINT_J2000_RE = new RegExp(ASTROVIEW.POINT_J2000 + "\\s+");