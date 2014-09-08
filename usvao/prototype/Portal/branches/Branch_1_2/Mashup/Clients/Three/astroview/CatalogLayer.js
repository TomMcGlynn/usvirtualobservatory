/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( camera, renderer, layer, radius )
{ 
    ASTROVIEW.AstroviewLayer.call( this, layer );
    
    if (layer && layer.rows && radius && renderer)
    {
        this.camera = camera;
        this.radius = radius;
    
        //
        // NOTE: The Particle API is different for WebGLRenderer vs. CanvasRenderer
        //       so we separate the code accordingly.
        //
        if (layer.rows && layer.rows.length > 0)
        {
            if (renderer instanceof THREE.WebGLRenderer)
            {
                this.addCatalogObjectsWebGL(layer.rows);
            }
            else if (renderer instanceof THREE.CanvasRenderer)
            {
                this.addCatalogObjectsCanvas(layer.rows);
            }  
        }
    }
};

ASTROVIEW.CatalogLayer.prototype = new ASTROVIEW.AstroviewLayer();
ASTROVIEW.CatalogLayer.prototype.constructor = ASTROVIEW.CatalogLayer;

ASTROVIEW.CatalogLayer.circle = THREE.ImageUtils.loadTexture("textures/circle.png");
ASTROVIEW.CatalogLayer.square = THREE.ImageUtils.loadTexture("textures/square.png");

/////////////////////////////
// addCatalogObjectsWebGL()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.addCatalogObjectsWebGL = function(rows)
{
    // Load the sprite  
    this.material = new THREE.ParticleBasicMaterial({
        size: 28,
        map: ASTROVIEW.CatalogLayer.circle,
        transparent:true,
        color: 0xff0000
        //vertexColors: true // NOTE: Specified Color per Particle
    });
           
    // Create the particle geometry to hold each particle vertex
    var geometry = new THREE.Geometry();
    
    // Create colors[] array for Specified Color per Particle
    var colors = [];
    
    // Convert each RA/DEC coord to a Vertex and add to the Geometry
    for (var i=0; i<rows.length; i++)
    {
        var row = rows[i];
        
        // Check all mixed case variations for RA and DEC values
        var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
        var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
        
        if (ra != null && dec != null)
        {
            var v = this.raDecToVertex(ra, dec, this.radius);
            geometry.vertices.push(v);
            //colors.push(new THREE.Color(0xffff00));  // NOTE: Specified Color per Particle
        }
    }
    
    // Store the colors array
    //geometry.colors = colors;     // NOTE: Specified Color per Particle

    // Create the particle system
    var ps = new THREE.ParticleSystem(geometry, this.material);
    
    // Add it to the scene
    this.add(ps);  
};

/////////////////////////////
// addCatalogObjectsCanvas()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.addCatalogObjectsCanvas = function(rows)
{
    // Create Canvas Particle Material
    this.material = new THREE.ParticleCanvasMaterial(
    {
        color: 0xffff00,
        program: bind(this, this.drawCircle)
    });
			
	// Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene
    for (var i=0; i<rows.length; i++)
    {
        var row = rows[i];
        
       // Check all mixed case variations for RA and DEC values
        var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
        var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
        
        if (ra != null && dec != null)
        {
            var v = this.raDecToVertex(ra, dec, this.radius);
            var p = new THREE.Particle( this.material );
            p.position.x = v.position.x;
            p.position.y = v.position.y;
            p.position.z = v.position.z;
            this.add(p);
        }
    }
};

/////////////////////////////
// drawCircle()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.drawCircle = function (context)
{
    var scale = this.camera.fov/FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    // context.arc(centerX, centerY, radius, startingAngle, endingAngle, counterclockwise);
    context.arc( 0, 0, 3, 0, PI2, true );
    context.closePath();
    context.stroke();
};



