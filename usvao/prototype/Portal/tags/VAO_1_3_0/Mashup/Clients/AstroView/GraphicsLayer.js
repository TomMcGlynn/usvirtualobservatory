/////////////////////////////
// GraphicsLayer
/////////////////////////////
ASTROVIEW.GraphicsLayer = function ( av, layerData)
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
        this.layerData = layerData;
        this.lid = ASTROVIEW.lid++;
        this.layerData.lid = this.lid;
        this.name = this.lid;
		
		// Load AstroView Objects
		this.camera = av.camera;
		this.renderer = av.renderer;
			
		// Initialize Temporary Points used for determining line distances below
		this.A = {};
		this.A.world = new THREE.Vector3();
		this.A.screen = new THREE.Vector3();
		
		this.B = {};
		this.B.world = new THREE.Vector3();
		this.B.screen = new THREE.Vector3();
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
ASTROVIEW.GraphicsLayer.DEFAULT_SIZE = 38;
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
	this.camera = undefined;
	this.renderer = undefined;
}


/////////////////////////////
// alert()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.alert = function()
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
ASTROVIEW.GraphicsLayer.prototype.createPolyline = function(polyline, row)
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
            var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
            geometry.vertices.push(v);
        } // end for each coord position
        
        // Close the Polygon
        var v = new THREE.Vertex( geometry.vertices[0].position.clone() );
        geometry.vertices.push(v);
            
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.row = row;
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
ASTROVIEW.GraphicsLayer.prototype.createCircle = function(circle, row)
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
        line.row = row;
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
        var v = this.raDecToVertex(x, y, ASTROVIEW.RADIUS_LAYER);
        geometry.vertices.push(v);
    }
}

/////////////////////////////
// createParticle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticle = function(ra, dec, row, symbol)
{
	if (this.renderer instanceof THREE.WebGLRenderer)
	{
		this.createParticleWebGL(ra, dec, row, symbol);
	}
	else if (this.renderer instanceof THREE.CanvasRenderer)
	{
		this.createParticleCanvas(ra, dec, row, symbol);
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
ASTROVIEW.GraphicsLayer.prototype.createParticleWebGL = function(ra, dec, row, symbol)
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
    
    // Create colors[] array for Specified Color per Particle
    //var colors = [];
    
    // Convert each RA/DEC coord to a Vertex and add to the Geometry       
	if (ra != null && dec != null)
	{
		var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
		v.row = row;
		this.particles.vertices.push(v);
		//colors.push(new THREE.Color(0xffff00));  // NOTE: Specified Color per Particle
	}
    
    // Store the colors array
    //geometry.colors = colors;     // NOTE: Specified Color per Particle
};

/////////////////////////////
// createParticleCanvas()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleCanvas = function(ra, dec, row, symbol)
{
    // Create Canvas Particle Material
	if (!this.material)
	{
		this.material = this.createMaterialParticleCanvas(this.layerData.attribs);
	}
			
	// Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene  
	if (ra != null && dec != null)
	{
		var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
		var p = new THREE.Particle( this.material );
		p.row = row;
		p.position.x = v.position.x;
		p.position.y = v.position.y;
		p.position.z = v.position.z;
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
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'square':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.square,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'plus':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.plus,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'stop':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.stop,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
			
		case 'diamond':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.diamond,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'diamond':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.diamond,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		default:
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.square,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
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
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
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
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
	//context.strokeRect(x, y, width, height)
    context.strokeRect( -4, -4, 9, 9);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
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
// diamond()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.diamond = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -5, -1);
	context.lineTo( -5,  1);
	context.lineTo( -1,  5);
	context.lineTo(  1,  5);
	context.lineTo(  5,  1);
	context.lineTo(  5, -1);
	context.lineTo(  1, -5);
	context.lineTo( -1, -5);
	context.lineTo( -5, -1);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// stop()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.stop = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -5, -2);
	context.lineTo( -5,  2);
	context.lineTo( -2,  5);
	context.lineTo(  2,  5);
	context.lineTo(  5,  2);
	context.lineTo(  5, -2);
	context.lineTo(  2, -5);
	context.lineTo( -2, -5);
	context.lineTo( -5, -2);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
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
// raDecToVertex()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.raDecToVertex = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vertex(new THREE.Vector3(x, y, z));
    return v;
};

/////////////////////////////
//
//     Object Selection
//
/////////////////////////////
// getSelected()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.getSelected = function(mouse, controller)
{
	// Validate input
	if (!controller || !mouse) return null;
	
	var D = 99999.99;
	var distance;
	for (var i=0; i<this.children.length; i++)
	{
		var object = this.children[i];
		if (object instanceof THREE.Particle)
		{
			distance = this.distanceToParticle2D(mouse, object, controller);
		}
		else if (object instanceof THREE.ParticleSystem)
		{
			distance = this.distanceToParticleSystem2D(mouse, object, controller);
		}
		else if (object instanceof THREE.Line)
		{
			distance = this.distanceToLine2D(mouse, object, controller);
		}
		D = (distance < D ? distance : D);
	}
	console.debug("D [" + this.layerData.name + "] : " + D);
	return null;		
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticle2D = function (mouse, particle, controller)
{
	// Convert Particle World ===> Screen  (clear out Z)
	controller.worldToScreen(particle.position, this.A.screen);
	this.A.screen.z = 0;
	
	// Copy selection mouse in Screen Coordinates (clear out Z)
	this.B.screen.copy(mouse.screen);
	this.B.screen.z = 0;
	
	// Calculate the distance in Screen Coordinates
	var D = this.A.screen.distanceTo(this.B.screen);
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticleSystem2D = function (mouse, ps, controller)
{
	var D = 99999.99;
	
	if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return null;
	
	var length = ps.geometry.vertices.length;
	for (var i=0; i<length; i++)
	{
		var v = ps.geometry.vertices[i];
		controller.worldToScreen(v.position, this.A.screen);
		this.A.screen.z = 0;
		
		// Copy selection mouse in Screen Coordinates (clear out Z)
		this.B.screen.copy(mouse.screen);
		this.B.screen.z = 0;
		
		// Calculate the distance in Screen Coordinates
		var distance = this.A.screen.distanceTo(this.B.screen);
		D = (distance < D ? distance : D);
	}

	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToLine2D = function (mouse, line, controller)
{
	var D = 99999.99;
	
	// validate input
	if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) return null;
	
	var vertices = line.geometry.vertices;
	for (var i=0; i<vertices.length-1; i++)
	{
		this.A.world = vertices[i].position;
		this.B.world = vertices[i+1].position;
		
		controller.worldToScreen(this.A.world, this.A.screen);
		controller.worldToScreen(this.B.world, this.B.screen);
		
		var inside = this.insideBoundingBox(mouse.screen, this.A.screen, this.B.screen);
		if (inside)
		{
			var distance = this.distanceToLinePoints2D(mouse.screen, this.A.screen, this.B.screen, controller);
			D = (distance < D ? distance : D);
		}
	}
	return D;
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

ASTROVIEW.GraphicsLayer.prototype.distanceToLinePoints2D = function (P, A, B, controller)
{
	// From Wikipedia: http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
	var normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
	var distance = Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
	return distance;
}

/////////////////////////
// 3D Distance Routines
/////////////////////////
ASTROVIEW.GraphicsLayer.prototype.distanceToParticle3D = function (mouse, particle)
{
	var D = mouse.layer.distanceTo(particle.position);
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticleSystem3D = function (mouse, ps)
{
	var D = 99999.99;
	
	if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return D;
	
	var length = ps.geometry.vertices.length;
	for (var i=0; i<length; i++)
	{
		var v = ps.geometry.vertices[i];
		var distance = mouse.layer.distanceTo(v.position);
		D = (distance < D ? distance : D);
	}
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToPolyline3D = function (mouse, line)
{
	var D = 99999.99;
	
	// validate input
	if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) return null;
	
	var length = line.geometry.vertices.length;
	for (var i=0; i<length-1; i++)
	{
		var v1 = line.geometry.vertices[i];
		var v2 = line.geometry.vertices[i+1];
		var distance = this.distancePointToLineSegment3D(mouse.layer, v1.position, v2.position);
		D = (distance < D ? distance : D);
	}
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToLineSegment3D = function (x0, x1, x2)
{
	//
	// From: http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
	//
	//      |(x0-x1) x (x0-x2)|
	// D = -------------------
	//          |x2-x1|
	//
	// (All input arguments are assumed to be THREE.Vector3)
	
	this.x0_x1.sub(x0, x1);
	this.x0_x2.sub(x0, x2);
	this.x2_x1.sub(x2, x1);
	
	var num = (this.numerator.cross(this.x0_x1, this.x0_x2)).length();	
	var den = this.x2_x1.length();
	var D = num/den;
	
	return D;
}

ASTROVIEW.lid = 0;