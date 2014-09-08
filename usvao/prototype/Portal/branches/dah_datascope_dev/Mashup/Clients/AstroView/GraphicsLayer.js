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
        this.matrixAutoUpdate = false;
        
        // Ensure LayerData Object is correctly linked to Us for future reference.
        this.layerData.lid = this.lid;
        this.layerData.name = this.name;
         
        // Graphics Objects
        this.lmaterial = null;
        this.pmaterial = null;
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
ASTROVIEW.GraphicsLayer.cross = THREE.ImageUtils.loadTexture("../AstroView/textures/cross99.png");

// Material Defaults
ASTROVIEW.GraphicsLayer.DEFAULT_COLOR      = 0xff00ff;
ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL     = 'stop';
ASTROVIEW.GraphicsLayer.DEFAULT_SIZE       = 31;
ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH = 0.8;
ASTROVIEW.GraphicsLayer.CROSS_LINE_WIDTH   = 1.4;

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

    this.lmaterial = undefined;
    this.pmaterial = undefined;
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
    if (!this.lmaterial)
    {
        this.lmaterial = this.createLineMaterial(this.layerData.attribs);
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
            var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS);
            geometry.vertices.push(v);
        } // end for each coord position
        
        // Close the Polygon
        var v = geometry.vertices[0].clone();
        geometry.vertices.push(v);
            
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.lmaterial, THREE.LineStrip );
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
    if (!this.lmaterial)
    {
        this.lmaterial = this.createLineMaterial(this.layerData.attribs);
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
        var line = new THREE.Line( geometry, this.lmaterial, THREE.LineStrip );
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
        var v = this.raDecToVector3(x, y, ASTROVIEW.RADIUS);
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
    else
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
    if (this.particles && this.pmaterial)
    {
        var ps = new THREE.ParticleSystem(this.particles, this.pmaterial);
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
    if (!this.pmaterial)
    {
        this.pmaterial = this.createParticleMaterialWebGL(this.layerData.attribs);
    }
       
    // Create the particle geometry to hold each particle vertex
    if (!this.particles)
    {
        this.particles = new THREE.Geometry();
    }

    // Convert each RA/DEC coord to a Vertex and add to the Geometry       
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS);
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
    if (!this.pmaterial)
    {
        this.pmaterial = this.createParticleMaterialCanvas(this.layerData.attribs);
    }
     
    // Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene  
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS);
        var p = new THREE.Particle( this.pmaterial );
        p.userData = userData;
        p.position = v;
        this.add(p);
    }
};

/////////////////////////////
// moveParticleTo()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.moveParticleTo = function(coord)
{
    if (this.renderer instanceof THREE.WebGLRenderer)
    {
        this.moveParticleWebGL(coord);
    }
    else
    {
        this.moveParticleCanvas(coord);
    }  
}

/////////////////////////////
// moveToParticleCanvas()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.moveParticleCanvas = function(coord)
{   
    // Get First Child
    if (this.children && this.children.length > 0)
    {
        var p = this.children[0];
        if (p instanceof THREE.Particle)
        {          
            // Convert each RA/DEC coord to a Vertex, then move particle to Vertex location 
            if (coord)
            {
                var v = this.raDecToVector3(coord.ra, coord.dec, ASTROVIEW.RADIUS);
                p.position.set(v.x, v.y, v.z);
            }
        }
    }
};

/////////////////////////////
// moveParticleWebGL()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.moveParticleWebGL = function(coord)
{   
    // Get First Child
    if (this.children && this.children.length > 0)
    {
        var ps = this.children[0];
        if (ps instanceof THREE.ParticleSystem)
        {          
            // Convert each RA/DEC coord to a Vertex, then move vertices[0] to Vertex location
            if (coord)
            {
                var v = this.raDecToVector3(coord.ra, coord.dec, ASTROVIEW.RADIUS);
                ps.geometry.vertices[0].set(v.x, v.y, v.z);
                ps.geometry.verticesNeedUpdate = true;
            }
        }
    }
};

/////////////////////////////////
// 
//         Materials
//
/////////////////////////////////
// createLineMaterial()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createLineMaterial = function(attribs)
{
    var lmaterial;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var linewidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH; //(attribs && attribs.linewidth ? attribs.linewidth : ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH);
    
    // Convert "0xff00ff" color hex string to integer value
    if (typeof color == "string")
    {
        color = parseInt(color, 16);
    }
    
    lmaterial = new THREE.LineBasicMaterial( { color: color, linewidth: linewidth } );

    return lmaterial;
}
 
/////////////////////////////////
// createParticleMaterialWebGL()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleMaterialWebGL = function(attribs)
{
    var pmaterial;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);
    var size = (attribs && attribs.size ? attribs.size : ASTROVIEW.GraphicsLayer.DEFAULT_SIZE);
    
    // Convert "0xff00ff" color hex string to integer value
    if (typeof color == "string")
    {
        color = parseInt(color, 16);
    }

    switch (symbol)
    {
        case 'circle':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.circle,
                transparent:true,
                color: color});
            break;

        case 'square':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
            break;

        case 'plus':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.plus,
                transparent:true,
                color: color});
            break;

        case 'stop':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.stop,
                transparent:true,
                color: color});
            break;

        case 'diamond':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.diamond,
                transparent:true,
                color: color});
            break;
            
        case 'cross':
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.cross,
                transparent:true,
                color: color});
            break;

        default:
            pmaterial = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
            break;
    }

    return pmaterial;
}

//////////////////////////////////
// createParticleMaterialCanvas()
//////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleMaterialCanvas = function(attribs)
{
    var pmaterial;

    var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
    var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);
    
    // Convert "0xff00ff" color hex string to integer value
    if (typeof color == "string")
    {
        color = parseInt(color, 16);
    }

    switch (symbol)
    {
        case 'circle':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.circle)
            });
            break;

        case 'square':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.square)
            });
            break;

        case 'plus':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.plus)
            });
            break;
            
        case 'cross':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.cross)
            });
            break;

        case 'diamond':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.diamond)
            });
            break;

        case 'stop':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.stop)
            });
            break;

        default:
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: bind(this, this.square)
            });
            break;
    }
 
    return pmaterial;
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
    context.beginPath();
    //context.strokeRect(x, y, width, height)
    context.strokeRect( -2, -2, 5, 5);
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
    context.beginPath();
    context.moveTo( -3,  0);
    context.lineTo(  3,  0);
    context.moveTo(  0,  3);
    context.lineTo(  0, -3);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// target()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.cross = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = ASTROVIEW.GraphicsLayer.CROSS_LINE_WIDTH;
    context.beginPath();
        
    context.moveTo( -16, 0);
    context.lineTo( -6,  0);
    context.moveTo(  6,  0);
    context.lineTo(  16, 0);
    
    context.moveTo(  0,  16);
    context.lineTo(  0,  6);
    context.moveTo(  0, -6);
    context.lineTo(  0, -16);
    
    context.moveTo( -1,  0); 
    context.lineTo(  1,  0);
    context.moveTo(  0, -1); 
    context.lineTo(  0,  1);
    
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
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
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
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
this.cameraPostion = "";
ASTROVIEW.GraphicsLayer.prototype.getHits = function(mouse, controller, lineDistance, particleDistance)
{
    // Validate input
    if (!controller || !mouse) return null;
    
    var hits = null;
    var dd, margin;
    
    var cameraChanged = (this.cameraPosition != controller.cameraPosition);
    this.cameraPosition = controller.cameraPosition;
    
    for (var i=0; i<this.children.length; i++)
    {
        var object = this.children[i];
        if (object instanceof THREE.Particle)
        {
            margin = (particleDistance ? particleDistance : 6.0);
            dd = this.distancePointToParticleSquared(mouse, object, controller, cameraChanged);          
        }
        else if (object instanceof THREE.ParticleSystem)
        {
            margin = (particleDistance ? particleDistance : 6.0);
            dd = this.distancePointToParticleSystemSquared(mouse, object, controller, cameraChanged);    
        }
        else if (object instanceof THREE.Line)
        {
            margin = (lineDistance ? lineDistance : 4.0);
            dd = this.distancePointToLineSquared(mouse, object, controller, cameraChanged);  
        }

        // Check if Distance Squared is less than Margin Squared
        if (dd <= margin*margin) 
        {
            if (hits == null) hits = [];
            hits.push(object);
        }
    }

    return hits;        
}

ASTROVIEW.GraphicsLayer.prototype.distancePointToParticleSquared = function (mouse, particle, controller, cameraChanged)
{
    // Convert Particle World ===> Screen  (clear out Z)
    if (cameraChanged)
    {
        if (!particle.position.screen) particle.position.screen = {"x":0, "y":0, "z":0};
        controller.worldToScreen(particle.position, particle.position.screen);
    }

    // Calculate the distance in Screen Coordinates
    var distance = this.distancePointToLineSegmentSquared(mouse.screen, particle.position.screen, particle.position.screen); 
    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.distancePointToParticleSystemSquared = function (mouse, ps, controller, cameraChanged)
{
    var dd;
    var distance = 9999.9999;

    if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return null;

    var length = ps.geometry.vertices.length;
    for (var i=0; i<length; i++)
    {
        if (cameraChanged)
        {
            if (!ps.geometry.vertices[i].screen) ps.geometry.vertices[i].screen = {"x":0, "y":0, "z":0};
            controller.worldToScreen(ps.geometry.vertices[i], ps.geometry.vertices[i].screen);
        }

        // Calculate the distance in Screen Coordinates
        dd = this.distancePointToLineSegmentSquared(mouse.screen, ps.geometry.vertices[i].screen, ps.geometry.vertices[i].screen);  
        if (dd < distance) distance = dd;
    }

    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.distancePointToLineSquared = function (mouse, line, controller, cameraChanged)
{
    var dd;                      // Distance Squared
    var distance = 9999.9999;    // Minimum Distance Squared
    var inside;

    // validate input
    if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) 
        return null;

    var vertices = line.geometry.vertices;
    for (var i=0; i<vertices.length-1; i++)
    {
        // Compute new Screen coordinates if Camera Position has moved
        if (cameraChanged)
        {
            if (!vertices[i].screen) vertices[i].screen = {"x":0, "y":0, "z":0};
            if (!vertices[i+1].screen) vertices[i+1].screen = {"x":0, "y":0, "z":0};

            controller.worldToScreen(vertices[i], vertices[i].screen);
            controller.worldToScreen(vertices[i+1], vertices[i+1].screen);
        }
      
        dd = this.distancePointToLineSegmentSquared(mouse.screen, vertices[i].screen, vertices[i+1].screen);     
        if (dd < distance) distance = dd;
    }
    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.distancePointToLineSegmentSquared = function (P, A, B)
{
    return this.computeDistanceSquared(P.x, P.y, A.x, A.y, B.x, B.y);
}

ASTROVIEW.GraphicsLayer.prototype.computeDistanceSquared = function (px, py, x1, y1, x2, y2)
{
    var pd2 = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

    var x, y;
    if (pd2 == 0)
    {
        // Points are coincident.
        x = x1;
        y = y2;
    }
    else
    {
        var u = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / pd2;

        if (u < 0)
        {
            // "Off the end"
            x = x1;
            y = y1;
        }
        else if (u > 1.0)
        {
            x = x2;
            y = y2;
        }
        else
        {
            x = x1 + u * (x2 - x1);
            y = y1 + u * (y2 - y1);
        }
    }
    return (x - px) * (x - px) + (y - py) * (y - py);
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

ASTROVIEW.lid = 0;