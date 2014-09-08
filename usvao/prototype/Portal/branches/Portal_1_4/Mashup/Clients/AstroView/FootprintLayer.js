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
        var dict = [];  // Array of Footprints to prevent creating duplicates       
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            if (row.hasOwnProperty("footprint"))
            {
                var footprint = row["footprint"];
             
                // Check for Duplicate Footprint
                if (dict[footprint])
                {
                    var userData = dict[footprint];
                    userData.rows.push(row);
                }
                else // New Footprint
                {
                    var userData = {"rows" : [row], "name" : layerData.name};
                    dict[footprint] = userData;
                    this.createFootprint(row, userData);
                }
            }
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
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(row, userData)
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
    else if (fp.indexOf(ASTROVIEW.POSITION_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_ICRS_RE);
        type = ASTROVIEW.POSITION_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.POSITION_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_J2000_RE);
        type = ASTROVIEW.POSITION_J2000;
    }
    
    // If we found no valid fplist, bail
    if (fplist == null || fplist.length < 2)
    {
        this.alert("Illegal Footprint:" + fp + "\n\nExpected Syntax: \n\n POLYGON [J2000|ICRS] [ra dec]\n CIRCLE [J2000|ICRS] [ra dec]\n POSITION [J2000|ICRS] [ra dec]", "Illegal Footprint");
        return;
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolygons(fplist, userData);
            break;
        }
        case ASTROVIEW.CIRCLE_J2000:
        case ASTROVIEW.CIRCLE_ICRS:
        {
            this.createCircles(fplist, userData);
            break;
        }
        case ASTROVIEW.POSITION_J2000:
        case ASTROVIEW.POSITION_ICRS:
        {
            this.createPoints(fplist, userData);
            break;
        }
        default:
        {
            this.alert("Footprint Syntax Error\n\n   Valid Syntax: 'POLYGON J2000|ICRS [values]' 'POSITION J2000|ICRS [values]' or 'CIRCLE J2000|ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolygons()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolygons = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var polyline = trim(fplist[i]);
        if (polyline.length > 0)
        {
            this.createPolyline(polyline, userData);     // GraphicsLayer.createPolyline()
        }
    }
}

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var circle = trim(fplist[i]);
        if (circle.length > 0)
        {
            this.createCircle(circle, userData);         // GraphicsLayer.createCircle()
        }
    }
}

/////////////////////////////
// createPoints()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoints = function(fplist, userData)
{
    for (var i=0; i<fplist.length; i++)
    {
        var point = trim(fplist[i]);
        if (point.length > 0)
        {
            this.createPoint(point, userData);
        }
    }
}

/////////////////////////////
// createPoint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoint = function(point, userData)
{
    var coords = point.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
            this.createParticle(ra, dec, userData);     // GraphicsLayer.createParticle()
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

ASTROVIEW.POSITION_J2000 = "POSITION J2000";
ASTROVIEW.POSITION_ICRS  = "POSITION ICRS";

ASTROVIEW.POSITION_J2000_RE = new RegExp(ASTROVIEW.POSITION_J2000 + "\\s+");
ASTROVIEW.POSITION_ICRS_RE = new RegExp(ASTROVIEW.POSITION_ICRS + "\\s+");

