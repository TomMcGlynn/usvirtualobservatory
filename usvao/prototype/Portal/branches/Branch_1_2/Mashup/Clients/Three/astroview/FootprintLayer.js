/////////////////////////////
// FootprintLayer()
/////////////////////////////
ASTROVIEW.FootprintLayer = function ( layer, radius )
{ 
    ASTROVIEW.AstroviewLayer.call( this, layer );
    
    if (layer && layer.rows)
    {
        this.radius = radius;
    
        //
        // For each footprint in the layer object, create a THREE.Line Object
        // and add it as a child to this Layer
        //
        if (layer.rows)
        {
            for (var i=0; i<layer.rows.length; i++)
            {
                var row = layer.rows[i];
                if (row.footprint)
                {
                    this.createFootprint(row.footprint);
                }
            }
        }
    }
};

ASTROVIEW.FootprintLayer.prototype = new ASTROVIEW.AstroviewLayer();
ASTROVIEW.FootprintLayer.prototype.constructor = ASTROVIEW.FootprintLayer;

/////////////////////////////
// createFootprint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(footprint)
{
    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var type = "";
    var polygons = [];
    var footprintUpper = trim(footprint.toUpperCase());
    if (footprintUpper.indexOf(ASTROVIEW.POLYGON_J2000) >= 0)
    {
        polygons = footprintUpper.split(/POLYGON J2000\s+/);
        type = ASTROVIEW.POLYGON_J2000;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.POLYGON_ICRS) >= 0)
    {
        polygons = footprintUpper.split(/POLYGON ICRS\s+/);
        type = ASTROVIEW.POLYGON_ICRS;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.CIRCLE_ICRS) >= 0)
    {
        polygons = footprintUpper.split(/CIRCLE ICRS\s+/);
        type = ASTROVIEW.CIRCLE_ICRS;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.CIRCLE_J2000) >= 0)
    {
        polygons = footprintUpper.split(/CIRCLE J2000\s+/);
        type = ASTROVIEW.CIRCLE_J2000;
    }
    
    // If we found no valid polygons, bail
    if (polygons == null || polygons.length < 2)
    {
        alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + footprint, "Unable to Create Footprint");
        return;
    }
    
    // Create material used for all lines
    if (!this.material)
    {
        this.material = new THREE.LineBasicMaterial( { color: 0xff0000, linewidth: 1 } );
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolylines(polygons);
            break;
        }
        case ASTROVIEW.CIRCLE_ICRS:
        case ASTROVIEW.CIRCLE_J2000:
        {
            this.createCircles(polygons);
            break;
        }
        default:
        {
            alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolylines()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolylines = function(polygons)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            var coords = polygon.split(/\s+/);
            if (coords.length > 0 && coords.length%2 == 0)
            {
                //
                // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
                //
                var geometry = new THREE.Geometry();
                for (var j=0; j<coords.length; j+=2)
                {
                    // Extract RA and DEC from coord Array
                    var ra = coords[j];
                    var dec = coords[j+1];
                                                
                    // Convert ra, dec to vertex and add to geometry.vertices[] array
                    var v = this.raDecToVertex(ra, dec, this.radius);
                    geometry.vertices.push(v);
                } // end for each coord position
                
                // Close the Polygon
                var v = new THREE.Vertex( geometry.vertices[0].position.clone() );
                geometry.vertices.push(v);
                    
                // Create new Line from the geometry and material
                var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
                this.add(line);	        					
            }
            else
            {
                alert("Footprint Coords Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + footprint, "Unable to Create Footprint");
            }
        }
    } // for each polygon
};

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(polygons)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            //
            // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
            //
            var coords = polygon.split(/\s+/);
            if (coords.length == 3)
            {
                // Extract RA, DEC and RADIUS from coord Array
                var ra = Number(coords[0]);
                var dec = Number(coords[1]);
                var radius = Number(coords[2]);
                var geometry = new THREE.Geometry(); 
                
                // Create Circle storing vertices in geometry.vertices[] 
                this.createCircle(ra, dec, radius, geometry);
                
                // Create new Line from the geometry and material
                var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
                this.add(line);	
            }
            else
            {
                alert("Footprint Circle Error.\n\n Expecting 3 values containing [RA] [DEC] [RADIUS], actual length: " + coords.length + "\n\n" + polygon, "Unable to Create Footprint");
            }
        }
    } // for each polygon[]
}

/////////////////////////////
// createCircle()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircle = function(ra, dec, radius, geometry)
{               
    // Build circle as polygon with 60 vertices (is this good enough???)
    for (var i=0; i<=60; i++)
    {
        var angle = 6.0*i*TO_RADIANS;
        var y = dec + radius*Math.sin(angle);
        var x = ra + (radius*Math.cos(angle)/Math.cos(y*TO_RADIANS));
        
        // Add the new point to the geometry array
        var v = this.raDecToVertex(x, y, this.radius);
        geometry.vertices.push(v);
    }
}

ASTROVIEW.POLYGON_J2000 = "POLYGON J2000";
ASTROVIEW.POLYGON_ICRS  = "POLYGON ICRS";
ASTROVIEW.CIRCLE_J2000  = "CIRCLE J2000";
ASTROVIEW.CIRCLE_ICRS   = "CIRCLE ICRS";