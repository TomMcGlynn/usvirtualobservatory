/////////////////////////////
// ImageLayer()
/////////////////////////////
ASTROVIEW.ImageLayer = function ( layerData, radius )
{ 
    THREE.GraphicsLayer.call( this );
    
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {    
        //
        // For each image in the layerData object, create a Plane Object with Image textured to it,
        // and add it as a child to this Layer
        //
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            if (row.image)
            {
                this.addImage(row.image);
            }
        }
    }
};

ASTROVIEW.ImageLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.ImageLayer.prototype.constructor = ASTROVIEW.ImageLayer;

ASTROVIEW.ImageLayer.prototype.addFootprint = function(footprint)
{
    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var polygons = [];
    var footprintUpper = trim(footprint.toUpperCase());
    if (footprintUpper.indexOf("POLYGON J2000 ") >= 0)
    {
        polygons = footprintUpper.split("POLYGON J2000 ");
    }
    else if (footprintUpper.indexOf("POLYGON ICRS ") >= 0)
    {
        polygons = footprintUpper.split("POLYGON ICRS ");
    }
    else if (footprintUpper.indexOf("CIRCLE ICRS ") >= 0)
    {
        polygons = footprintUpper.split("CIRCLE ICRS ");
    }
    
    // If we found no valid polygons, bail
    if (polygons == null || polygons.length == 0)
    {
        alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [RA] [DEC]' or 'Polygon ICRS [RA] [DEC]' \n\n" + sSTCS, "Unable to Create Footprint");
        return;
    }
    
    // Create material used for all lines
    if (!this.material)
    {
        this.material = new THREE.LineBasicMaterial( { color: 0xff0000, linewidth: 1 } );
    }
    
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            var coords = polygon.split(" ");
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
                    var v = this.raDecToVector3(ra, dec, this.radius);
                    geometry.vertices.push(v);
                } // end for each coord position
                
                // Close the Polygon
                var v = geometry.vertices[0].clone();
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
}
     
ASTROVIEW.ImageLayer.prototype.raDecToVector3 = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vector3(x, y, z);
    return v;
}

