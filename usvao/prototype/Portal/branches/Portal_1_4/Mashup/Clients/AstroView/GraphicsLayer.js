/////////////////////////////
// GraphicsLayer
/////////////////////////////
ASTROVIEW.GraphicsLayer = function (av, layerData)
{
    THREE.Object3D.call( this );
    if (av && layerData)
    {
        //
        // NOTE: We must ensure the Layer ID matches everwhere: 
        // layerData: The object contains the original data properties of the GraphicsLayer.
        // GraphicsLayer: The actual 3D Object added to the Scene.
        // Object3D.name: main key for extracting Objects from the Scene using scene.getObjectByName()
        //
        this.av = av;
        this.layerData = layerData;
        this.renderer = av.renderers[0];
        
        this.lid = ASTROVIEW.lid++;
        this.name = (layerData.name ? layerData.name : "GraphicsLayer_" + this.lid); 
        
        // Ensure LayerData Object is correctly linked to Us for future reference.
        this.layerData.lid = this.lid;
        this.layerData.name = this.name;
         
        // Graphics Objects
        this.material = null;
        this.particles = null;

        // Initialize Temporary Points used for determining Point to Line distances below
        this.A = {};
        this.A.world = new THREE.Vector3();
        this.A.screen = new THREE.Vector3();

        this.B = {};
        this.B.world = new THREE.Vector3();
        this.B.screen = new THREE.Vector3();

        this.minx = null;
        this.maxx = null;
        this.miny = null;
        this.maxy = null;
    }
};

ASTROVIEW.GraphicsLayer.prototype = new THREE.Object3D();
ASTROVIEW.GraphicsLayer.prototype.constructor = ASTROVIEW.GraphicsLayer;

//////////////////////////////////
// Image Textures (for particles)
//////////////////////////////////
ASTROVIEW.GraphicsLayer.plus = THREE.ImageUtils.loadTexture("../AstroView/textures/plus11.png");
ASTROVIEW.GraphicsLayer.circle = THREE.ImageUtils.loadTexture("../AstroView/textures/circle11.png");
ASTROVIEW.GraphicsLayer.square = THREE.ImageUtils.loadTexture("../AstroView/textures/square11.png");
ASTROVIEW.GraphicsLayer.stop = THREE.ImageUtils.loadTexture("../AstroView/textures/stop11.png");
ASTROVIEW.GraphicsLayer.diamond = THREE.ImageUtils.loadTexture("../AstroView/textures/diamond11.png");

// Material Defaults
ASTROVIEW.GraphicsLayer.DEFAULT_COLOR = 0xff00ff;
ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL = 'stop';
ASTROVIEW.GraphicsLayer.DEFAULT_SIZE = 31;
ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH = 1;

/////////////////////////////
// clean()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.clean = function()
{
    if (this.renderer instanceof THREE.WebGLRenderer)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var object = this.children[i];
            this.renderer.deallocateObject(object);
        }
    }
 
    // Remove all Object References that we created
    this.av = undefined;
    this.layerData = undefined;
    this.renderer = undefined;

    this.material = undefined;
    this.particles = undefined;

    this.A = undefined;
    this.B = undefined;

    this.minx = undefined;
    this.maxx = undefined;
    this.miny = undefined;
    this.maxy = undefined;
}

/////////////////////////////
// alert()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.alert = function(msg)
{
    if (!this.alertOnce)
    {
        alert(msg);
        this.alertOnce = true;
    }
}

/////////////////////////////
//
//    Graphics Creation 
//
/////////////////////////////
// createPolyline()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createPolyline = function(polyline, userData)
{
    // Create line material used for all lines
    if (!this.material)
    {
        this.material = this.createMaterialLine(this.layerData.attribs);
    }
         
    var coords = polyline.split(/\s+/);
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
            var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
            geometry.vertices.push(v);
        } // end for each coord position
        
        // Close the Polygon
        var v = geometry.vertices[0].clone();
        geometry.vertices.push(v);
            
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.userData = userData;
        this.add(line);                      
    }
    else
    {
        alert("Footprint Coords Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + footprint, "Unable to Create Footprint");
    }
};

/////////////////////////////
// createCircle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createCircle = function(circle, userData)
{
    // Create line material used for all lines
    if (!this.material)
    {
        this.material = this.createMaterialLine(this.layerData.attribs);
    }
        
    //
    // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
    //
    var coords = circle.split(/\s+/);
    if (coords.length == 3)
    {
        // Extract RA, DEC and RADIUS from coord Array
        var ra = Number(coords[0]);
        var dec = Number(coords[1]);
        var radius = Number(coords[2]);
        var geometry = new THREE.Geometry(); 
        
        // Create Circle storing vertices in geometry.vertices[] 
        this.createCircleGeometry(ra, dec, radius, geometry);
        
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.userData = userData;
        this.add(line);  
    }
    else
    {
        alert("Footprint Circle Error.\n\n Expecting 3 values containing [RA] [DEC] [RADIUS], actual length: " + coords.length + "\n\n" + circle, "Unable to Create Footprint");
    }
}

/////////////////////////////
// createCircleGeometry()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createCircleGeometry = function(ra, dec, radius, geometry)
{               
    // Build circle as polygon with 60 vertices (is this good enough???)
    for (var i=0; i<=60; i++)
    {
        var angle = 6.0*i*TO_RADIANS;
        var y = dec + radius*Math.sin(angle);
        var x = ra + (radius*Math.cos(angle)/Math.cos(y*TO_RADIANS));
        
        // Add the new point to the geometry array
        var v = this.raDecToVector3(x, y, ASTROVIEW.RADIUS_GRAPHICS);
        geometry.vertices.push(v);
    }
}

/////////////////////////////
// createParticle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticle = function(ra, dec, userData)
{
    if (this.renderer instanceof THREE.WebGLRenderer)
    {
        this.createParticleWebGL(ra, dec, userData);
    }
    else if (this.renderer instanceof THREE.CanvasRenderer)
    {
        this.createParticleCanvas(ra, dec, userData);
    }  
}

/////////////////////////////
// createParticleSystem()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleSystem = function()
{
    // Create the particle system for WebGL only
    if (this.particles && this.material)
    {
        var ps = new THREE.ParticleSystem(this.particles, this.material);
        this.add(ps);
    } 
}

/////////////////////////////
// hasParticleSystem()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.hasParticleSystem = function()
{
    return (this.particles && this.particles.vertices && this.particles.vertices.length > 0);
}
     
/////////////////////////////
// createParticleWebGL()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleWebGL = function(ra, dec, userData)
{
    // Create Material, if not already done
    if (!this.material)
    {
        this.material = this.createMaterialParticleWebGL(this.layerData.attribs);
    }
       
    // Create the particle geometry to hold each particle vertex
    if (!this.particles)
    {
        this.particles = new THREE.Geometry();
    }

    // Convert each RA/DEC coord to a Vertex and add to the Geometry       
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
        v.userData = userData;
        this.particles.vertices.push(v);
    }
};

/////////////////////////////
// createParticleCanvas()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleCanvas = function(ra, dec, userData)
{
    // Create Canvas Particle Material
    if (!this.material)
    {
        this.material = this.createMaterialParticleCanvas(this.layerData.attribs);
    }
     
    // Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene  
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
        var p = new THREE.Particle( this.material );
        p.userData = userData;
        p.position = v;
        this.add(p);
    }
};

/////////////////////////////////
// 
//         Materials
//
/////////////////////////////////
// createMaterialLine()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialLine = function(attribs)
{
    var material;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var linewidth = (attribs && attribs.linewidth ? attribs.linewidth : ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH);
    material = new THREE.LineBasicMaterial( { color: color, linewidth: linewidth } );

    return material;
}
 
/////////////////////////////////
// createMaterialParticleWebGL()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialParticleWebGL = function(attribs)
{
    var material;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);
    var size = (attribs && attribs.size ? attribs.size : ASTROVIEW.GraphicsLayer.DEFAULT_SIZE);

    switch (symbol)
    {
        case 'circle':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.circle,
                transparent:true,
                color: color});
            break;

        case 'square':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
            break;

        case 'plus':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.plus,
                transparent:true,
                color: color});
            break;

        case 'stop':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.stop,
                transparent:true,
                color: color});
            break;

        case 'diamond':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.diamond,
                transparent:true,
                color: color});
            break;

        default:
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
            break;
    }

    return material;
}

//////////////////////////////////
// createMaterialParticleCanvas()
//////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialParticleCanvas = function(attribs)
{
    var material;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);

    switch (symbol)
    {
        case 'circle':
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.circle)
            });
            break;

        case 'square':
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.square)
            });
            break;

        case 'plus':
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.plus)
            });
            break;

        case 'diamond':
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.diamond)
            });
            break;

        case 'stop':
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.stop)
            });
            break;

        default:
            material = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.square)
            });
            break;
    }
 
 return material;
}

/////////////////////////////
//
//    Canvas Rendering 
//
/////////////////////////////
// circle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.circle = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.9;
    context.beginPath();
    // context.arc(centerX, centerY, radiusLayer, startingAngle, endingAngle, counterclockwise);
    context.arc( 0, 0, 4, 0, PI2, true );
    context.closePath();
    context.stroke();
};

/////////////////////////////
// square()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.square = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    //context.strokeRect(x, y, width, height)
    context.strokeRect( -3, -3, 7, 7);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -3,  0);
    context.lineTo(  3,  0);
    context.moveTo(  0,  3);
    context.lineTo(  0, -3);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// diamond()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.diamond = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4, -1);
    context.lineTo( -4,  1);
    context.lineTo( -1,  4);
    context.lineTo(  1,  4);
    context.lineTo(  4,  1);
    context.lineTo(  4, -1);
    context.lineTo(  1, -4);
    context.lineTo( -1, -4);
    context.lineTo( -4, -1);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// stop()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.stop = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4, -2);
    context.lineTo( -4,  2);
    context.lineTo( -2,  4);
    context.lineTo(  2,  4);
    context.lineTo(  4,  2);
    context.lineTo(  4, -2);
    context.lineTo(  2, -4);
    context.lineTo( -2, -4);
    context.lineTo( -4, -2);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4,  0);
    context.lineTo(  4,  0);
    context.moveTo(  0,  4);
    context.lineTo(  0, -4);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// raDecToVector3()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.raDecToVector3 = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vector3(x, y, z);
    return v;
};

/////////////////////////////
//
//     Object Selection
//
/////////////////////////////
// getHits()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.getHits = function(mouse, controller, lineDistance, particleDistance)
{
    // Validate input
    if (!controller || !mouse) return null;

    var hits = null;
    var d, distance;
    for (var i=0; i<this.children.length; i++)
    {
        var object = this.children[i];
        if (object instanceof THREE.Particle)
        {
            d = this.particleDistance2D(mouse, object, controller);
            distance = (particleDistance ? particleDistance : 6.0);
        }
        else if (object instanceof THREE.ParticleSystem)
        {
            d = this.particleDistanceSystem2D(mouse, object, controller);
            distance = (particleDistance ? particleDistance : 6.0);
        }
        else if (object instanceof THREE.Line)
        {
            d = this.lineDistance2D(mouse, object, controller);
            distance = (lineDistance ? lineDistance : 4.0);
        }

        if (d <= distance) 
        {
            if (hits == null) hits = [];
            hits.push(object);
        }
    }

    return hits;        
}

ASTROVIEW.GraphicsLayer.prototype.particleDistance2D = function (mouse, particle, controller)
{
    // Convert Particle World ===> Screen  (clear out Z)
    this.A.world = particle.position;
    controller.worldToScreen(this.A.world, this.A.screen);
    this.A.screen.z = 0;

    // Calculate the distance in Screen Coordinates
    var distance = mouse.screen.distanceTo(this.A.screen);
    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.particleDistanceSystem2D = function (mouse, ps, controller)
{
    var distance = 9999.9999;

    if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return null;

    var length = ps.geometry.vertices.length;
    for (var i=0; i<length; i++)
    {
        this.A.world = ps.geometry.vertices[i];
        controller.worldToScreen(this.A.world, this.A.screen);
        this.A.screen.z = 0;

        // Calculate the distance in Screen Coordinates
        var d = mouse.screen.distanceTo(this.A.screen);
        if (d < distance) distance = d;
    }

    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.lineDistance2D = function (mouse, line, controller)
{
    var distance = 9999.9999;

    // validate input
    if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) 
        return null;

    var vertices = line.geometry.vertices;
    for (var i=0; i<vertices.length-1; i++)
    {
        this.A.world = vertices[i];
        this.B.world = vertices[i+1];

        controller.worldToScreen(this.A.world, this.A.screen);
        controller.worldToScreen(this.B.world, this.B.screen);

        // Check if Line Segement Points are on top of each other (Length == 0)
        var d;
        if (this.A.screen.x == this.B.screen.x && this.A.screen.y == this.B.screen.y)
        {
            d = mouse.screen.distanceTo(this.A.screen);
        }
        else // Line Segment Length > 0
        {
            var inside = this.insideBoundingBox(mouse.screen, this.A.screen, this.B.screen);
            if (inside)
            {
                d = this.lineDistancePoints2D(mouse.screen, this.A.screen, this.B.screen, controller);
            }
        }

        // Update minimum distance
        distance = (d < distance ? d : distance);
    }
    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.insideBoundingBox = function (P, A, B, margin)
{
    if (!margin) margin = 4;

    this.minx = (A.x < B.x ? A.x : B.x) - margin;
    this.maxx = (A.x > B.x ? A.x : B.x) + margin;
    this.miny = (A.y < B.y ? A.y : B.y) - margin;
    this.maxy = (A.y > B.y ? A.y : B.y) + margin;

    return (this.minx <= P.x && P.x <= this.maxx &&
            this.miny <= P.y && P.y <= this.maxy);
}

ASTROVIEW.GraphicsLayer.prototype.lineDistancePoints2D = function (P, A, B, controller)
{
    // From Wikipedia: http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    var normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
    var distance = Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
    return distance;
}

ASTROVIEW.lid = 0;

