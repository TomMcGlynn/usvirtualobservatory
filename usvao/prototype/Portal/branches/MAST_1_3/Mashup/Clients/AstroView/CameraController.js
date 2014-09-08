/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( camera, scene, container, renderer )
{
	// Pull out camera and container from AstroBasicView
	this.camera = camera;
	this.scene = scene;
	this.container = ( container !== undefined ) ? container : document;
	this.renderer = renderer;
	
	// Initialize Field of View Ranges
	this.fovmax = ASTROVIEW.FOV_LEVEL_MAX;
	this.fovmin = ASTROVIEW.FOV_LEVEL_MIN;
	
	// Initialize Zoom Level
    this.zlevel = ASTROVIEW.ZOOM_LEVEL_MIN;
	this.vizlevel = ASTROVIEW.ZOOM_LEVEL_MIN;

    //
    // Fov ===> Zoom Level Lookup Table
    //
    //                      Fov Value       Zoom Level
	//                      --------------  ----------
    this.zoomLookupTable =  [this.fovmax+1,	// 0
							 this.fovmax+1,	// 1
							 this.fovmax+1,	// 2
							 this.fovmax+1,	// 3
							 10,		    // 4
							 5,			    // 5
							 2.5,		    // 6
							 1.25,		    // 7
							 0.75,		    // 8
							 0.355,		    // 9
							 0.125,		    // 10
							 0.0625,	    // 11
							 0.00001];	    // 12
	
	this.visibleLayers = "4, 5, 6, 7, 8, 9, 10";
	//this.visibleLayers = "4, 6, 8, 10";

    // Camera Position
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = this.fovmax;    
    this.zoomSpeed = 0.1;  // How fast you zoom on each mouse wheel
    this.panSpeed = 0.2;   // How fast you pan on each mouse move.
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
	this.frustumMatrix = new THREE.Matrix4();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.down = new THREE.Vector2();
    this.last = new THREE.Vector2();
    this.delta = new THREE.Vector2();
    this.mouseDown = false;
    
    // Position Events
    this.windowHalfX = window.innerWidth/2.0;
    this.windowHalfY = window.innerHeight/2.0;
	
	// Current Mouse Position in ALL Coordinate Systems
	this.mouse = {};
	this.mouse.screen = new THREE.Vector3();
	this.mouse.normal = new THREE.Vector3();
	this.mouse.world = new THREE.Vector3();
	this.mouse.layer  = new THREE.Vector3();
	this.mouse.ra = 0.0;
	this.mouse.dec = 0.0;
	this.mouse.sra = "";
	this.mouse.sdec = "";

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
		// Resize Events
		window.addEventListener( 'resize',  bind(this, this.onResize),  false );
		
        // Mouse Events
        this.container.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        this.container.addEventListener( 'mousemove', bind(this, this.onMouseMove), false );
        this.container.addEventListener( 'mousedown', bind(this, this.onMouseDown), false );
        this.container.addEventListener( 'mouseup',   bind(this, this.onMouseUp),   false );
        
        // Touch Events
        this.container.addEventListener( 'touchstart', bind(this, this.onTouchStart), false );
        this.container.addEventListener( 'touchmove',  bind(this, this.onTouchMove),  false );
        this.container.addEventListener( 'touchend',   bind(this, this.onTouchEnd),   false );
        
        // Mouse Wheel Events: Need both for WebKit and FF
        window.addEventListener('DOMMouseScroll', bind(this, this.onMouseWheel), false);
        window.addEventListener('mousewheel',     bind(this, this.onMouseWheel), false);
        
        // Keypress events
        document.addEventListener( "keypress" , bind(this, this.onKeyPress), false);
        //document.onkeypress=bind(this, this.onKeyPress);
    }
	
	/////////////////////////
    // Resize Event
    /////////////////////////
	this.onResize = function( event )
    {
		this.renderer.setSize( window.innerWidth, window.innerHeight );
		this.camera.aspect = window.innerWidth / window.innerHeight;
		this.camera.updateProjectionMatrix();
	}
      
    /////////////////////////
    // Mouse Events
    /////////////////////////
    this.onMouseDown = function(event)
    {
        event.preventDefault();
        this.mouseDown=true;
        
        this.last.x = event.clientX;
        this.last.y = event.clientY;
        this.down.x = event.clientX;
        this.down.y = event.clientY;		
    }
    
    this.onMouseMove = function(event)
    {
        event.preventDefault();
        
        if (this.mouseDown)
        {
            var rotationX = (event.clientY - this.last.y) * TO_RADIANS * (this.camera.fov/(this.fovmax) * this.panSpeed);
            var rotationY = (event.clientX - this.last.x) * TO_RADIANS * (this.camera.fov/(this.fovmax) * this.panSpeed);
            
            this.setCameraRotationDeltaXY(rotationX, rotationY);
                
            this.last.x = event.clientX;
            this.last.y = event.clientY;
        }
         
        this.updatePosition(event);
    }
        
    this.onMouseUp = function(event)
    {
        event.preventDefault();
        this.mouseDown=false;
        
        // Stop Rotation if User clicked single point
        if (event.clientX == this.down.x &&
            event.clientY == this.down.y)
        {
            this.setCameraRotationDeltaXY(0, 0); // Stop spinning rotation
        }
        
        // Debug Info on mouse click mouse
        this.dumpPosition();
		
		// Check for Object Selection
		this.updateSelected();
    }
    
    this.onMouseOut = function(event)
    {
        event.preventDefault();
        this.mouseDown = false;
    }
    
    this.onMouseWheel = function(event)
    {		  
        // Get wheel direction for both WebKit or FF
        var delta = ((typeof event.wheelDelta != "undefined") ? (-event.wheelDelta) : event.detail );
        this.zoom(delta);
    }
    
    /////////////////////////
    // Keyboard Events
    /////////////////////////
    this.onKeyPress = function(event)
    {
        var unicode=event.keyCode? event.keyCode : event.charCode;
        //alert("onKeyPress: " + unicode); // find the char code		
        switch(unicode)
        {
            case 37:  	this.rotateRight(); break;
            case 39: 	this.rotateLeft(); break;
            case 38: 	this.rotateDown(); break;
            case 40: 	this.rotateUp(); break;
            case 105: 	this.zoomIn(); break;
            case 111:  	this.zoomOut(); break;
        }
    }
    
    /////////////////////////
    // Touch Events
    /////////////////////////
    this.onTouchStart = function(event)
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
            this.mouseDown=true;

            this.last.x = event.touches[ 0 ].pageX;
            this.last.y = event.touches[ 0 ].pageY;
            
            this.down.x = event.touches[ 0 ].pageX;
            this.down.y = event.touches[ 0 ].pageY;
        }
    }
    
    this.onTouchMove = function( event )
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
        
            if (this.mouseDown)
            {
                var rotationX = (event.touches[ 0 ].pageY - this.last.y) * TO_RADIANS * (this.camera.fov/(this.fovmax));
                var rotationY = (event.touches[ 0 ].pageX - this.last.x) * TO_RADIANS * (this.camera.fov/(this.fovmax));
                
                this.setCameraRotationDeltaXY(rotationX, rotationY);
                    
                this.last.x = event.touches[ 0 ].pageX;
                this.last.y = event.touches[ 0 ].pageY;
            }
        }
    }
    
    this.onTouchEnd = function(event)
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
            this.mouseDown=false;
            
            // Stop Rotation if User tapped single point
            if (event.touches[ 0 ].pageX == this.down.x &&
                event.touches[ 0 ].pageY == this.down.y)
            {
                this.setCameraRotationDeltaXY(0, 0);
            }
        }
    }
    
    /////////////////////////
    // Actions
    /////////////////////////   
    this.rotateLeft = function()
    {
        var deltaY = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateRight = function()
    {
        var deltaY = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateUp = function()
    {
        var deltaX = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.rotateDown = function()
    {
        var deltaX = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.setCameraRotationDeltaXY = function(deltax, deltay)
    {
        // Update the Target Rotation(s)
        this.cameraRotationX = this.camera.rotation.x + deltax;
        this.cameraRotationY = this.camera.rotation.y + deltay;
    }
    
    this.zoom = function(delta)
    {
        (delta > 0 ? this.zoomOut() : this.zoomIn());
    }
    
    this.zoomIn = function()
    {
        var deltaFov = this.cameraFov * this.zoomSpeed;
        this.cameraFov -= deltaFov;
        this.cameraFov = (this.cameraFov < this.fovmmin ? this.fovmmin : this.cameraFov);
    }
    
    this.zoomOut = function()
    {
        var deltaFov = this.cameraFov * this.zoomSpeed;
        this.cameraFov += deltaFov;
        this.cameraFov = (this.cameraFov > this.fovmax ? this.fovmax : this.cameraFov);
    }
		
    this.moveTo = function(ra, dec, zlevel)
    {
        console.debug("moveTo: [" + ra + ", " + dec + ", " +  zlevel + "]");
        
        // Check optional zlevel param
        if (!zlevel) zlevel = 10;
        
        // point camera to ra, dec
        this.camera.rotation.y = this.cameraRotationY = ra * TO_RADIANS;
        this.camera.rotation.x = this.cameraRotationX = dec * TO_RADIANS;
		
		// Set the Zoom Level
		this.zoomTo(zlevel);
	}
	
	this.zoomTo = function(zlevel)
	{
        // now zoom in to max field of view for zlevel
		if (zlevel > 0 && zlevel < this.zoomLookupTable.length)
		{
			this.camera.fov = this.cameraFov = (this.zoomLookupTable[zlevel-1] + this.zoomLookupTable[zlevel])/2.0;
			this.camera.updateProjectionMatrix();
		}
    }
    
    /////////////////////////
    // render()
    /////////////////////////  
    this.render = function()
    {
        // Bounds Check on RotationX (Declination)
        if (this.cameraRotationX > RADIANS_90) this.cameraRotationX = RADIANS_90;
        if (this.cameraRotationX < -RADIANS_90) this.cameraRotationX = -RADIANS_90;
        
        // Rotate the Camera
        if (this.camera.rotation.x !== this.cameraRotationX)
        {
            this.camera.rotation.x += (this.cameraRotationX - this.camera.rotation.x) * 0.5;
        }
        
        if (this.camera.rotation.y !== this.cameraRotationY)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
           
        // Update the Field of View
        if (this.camera.fov !== this.cameraFov)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
			this.camera.updateProjectionMatrix();
		}
		
		//
		// Update the frustum matrix, and zoom level(s) 
		//
		this.updateController();
    }
	
	/////////////////////////
    // updateController()
    /////////////////////////
	this.updateController = function()
    {
		this.updateFrustumMatrix();
		this.updateZoomLevel();
		this.updateVisibleZoom();
	}
	
	/////////////////////////
    // updateFrustumMatrix()
    ///////////////////////// 
    this.updateFrustumMatrix = function()
    {
		// Only update the frustum matrix if the camera has changed
		this.frustumMatrix.multiply(this.camera.projectionMatrix, this.camera.matrixWorldInverse);
		this.frustum.setFromMatrix( this.frustumMatrix );
    }
	
    /////////////////////////
    // updateZoomLevel()
    /////////////////////////
    this.updateZoomLevel = function()
    {
		var i;
		
		// Search Zoom Lookup Table until:
		// Camera fov is within specified range AND zoom layer is visible 
		for (i=1; i<this.zoomLookupTable.length; i++)
		{
			if (this.zoomLookupTable[i] <= this.camera.fov)
			{
				break;										
			}
		}
		
		if (this.zlevel !== i)
		{
			this.zlevel = i;
			console.debug("*** Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel + " ***");
		}
    }
	
	this.getZoomLevel = function()
    {
		return this.zlevel;
	}
	
	///////////////////////////
    // updateVisibleZoom()
    ///////////////////////////
    this.updateVisibleZoom = function()
    {
		var zlevel = this.zlevel;
		do 
		{
			if (this.visibleLayers.indexOf(zlevel) != -1)
			{
				break;
			}
		} while (--zlevel > 0);
		
		if (this.vizlevel != zlevel)
		{
			this.vizlevel = zlevel;
			console.debug("*** Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel + " ***");
		}
    }
	
	this.getVisibleZoom = function()
    {
		return this.vizlevel;
	}
	
	/////////////////////////
    // updateSelected()
    ///////////////////////// 
	this.updateSelected = function( )
	{
		/*
		event.preventDefault();
		var vector = new THREE.Vector3( mouse.x, mouse.y, 0.5 );
		projector.unprojectVector( vector, camera );
		var ray = new THREE.Ray( camera.mouse, vector.subSelf( camera.mouse ).normalize() );
		*/
		
		this.selected = null;
	
		if (this.scene)
		{
			for (var i=0; i<this.scene.children.length; i++)
			{
				var child = this.scene.children[i];
				if (child instanceof ASTROVIEW.GraphicsLayer)
				{
					this.selected = child.getSelected(this.mouse, this);
					if (this.selected && this.selected.length > 0)
					{
						break;
					}
				}
			}
		}
		
		if ( this.selected && this.selected.length > 0 )
		{		
			console.debug("Got Selected:" + this.selected.length);
		}
		else
		{
			console.debug("Crap, No Selection.");
		}
	}
    
    /////////////////////////
    // updatePosition()
    /////////////////////////  
    this.updatePosition = function(event)
    {
		// Store Screen Vector
        this.mouse.screen.x = event.clientX;
        this.mouse.screen.y = event.clientY;
        this.mouse.screen.z = 1.0; 

		// Screen ===> World (Normalized)
        this.screenToWorld(this.mouse.screen, this.mouse.normal);
        this.mouse.normal.normalize();
		
		// World (Normalized) ===> World (Scaled)
		this.mouse.world.copy(this.mouse.normal);
        this.mouse.world.multiplyScalar(ASTROVIEW.RADIUS_SPHERE);
		
		// World (Normalized) ===> Layer (Scaled)
		this.mouse.layer.copy(this.mouse.normal);
        this.mouse.layer.multiplyScalar(ASTROVIEW.RADIUS_LAYER);
		
		// World (Normalized) ===> RA/DEC
        this.worldToRaDec(this.mouse.normal, this.mouse);

        // RA/DEC ===> Sexagesimal (String Representation)
        this.mouse.sra = this.degToHMS(this.mouse.ra);
        this.mouse.sdec = this.degToDMS(this.mouse.dec);
    }
    
    this.dumpPosition = function()
    {
       console.debug("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
					 " [layer x,y,z: " + this.mouse.layer.x.toFixed(3) + "," + this.mouse.layer.y.toFixed(3) + "," + this.mouse.layer.z.toFixed(3) + "]" + 
                     " [ra,dec: " + this.mouse.ra.toFixed(3) + "," + this.mouse.dec.toFixed(3) + "]" +
					 " [fov: " + this.camera.fov.toFixed(3) + "]" +
					 " [zoom/visible: " + this.zlevel + "/" + this.vizlevel + "]" );
    }
		
    this.screenToWorld = function(screen, world)
    {
		world.copy(screen);   
        world.x = world.x / this.windowHalfX - 1;
        world.y = - world.y / this.windowHalfY + 1;
        this.projector.unprojectVector( world, this.camera );
    }
    
    this.worldToScreen = function(world, screen)
    {
		screen.copy(world);
        this.projector.projectVector( screen, this.camera );
        screen.x = ( screen.x + 1 ) * this.windowHalfX;
        screen.y = ( - screen.y + 1) * this.windowHalfY;
    }
    
    this.worldToRaDec = function(world, coord)
    {
        // Store the ra, dec values
        coord.dec = Math.asin(world.y) * TO_DEGREES;			
        coord.ra = Math.atan2(-world.x, -world.z) * TO_DEGREES;
        if (coord.ra < 0) coord.ra += 360.0;
    }
    
    this.degToDMS = function(degrees)
    {        
        var deg = Math.abs(degrees);
        var deg_floor = Math.floor(deg);
        var min = 60 * (deg - deg_floor);
        var min_floor = Math.floor(min);
        var sec = 60 * (min - min_floor);
        
        var dms = ((degrees < 0.0) ? '-' : '+') +
                  this.pad(deg_floor, 2) + ":" +
                  this.pad(min_floor, 2)  + ":" +
                  this.pad(sec.toFixed(3), 2);
        
        return dms;
    }
    
    this.degToHMS = function(degrees)
    {
        var hours = degrees/15.0;
        var hours_floor = Math.floor(hours);
        var min = 60 * (hours - hours_floor);
        var min_floor = Math.floor(min);
        var sec = 60 * (min - min_floor);
        
        var hms = this.pad(hours_floor, 2) + ":" +
                  this.pad(min_floor, 2) + ":" +
                  this.pad(sec.toFixed(3), 2);
        
        return hms;
    }
    
    this.pad = function (num, size)
    {
		var s = num.toString();
		
		switch (size - s.length)
		{
			case 1: s = '0' + s; break;
			case 2: s = '00' + s; break;
			case 3: s = '000' + s; break;
			case 4: s = '0000' + s; break;
			case 5: s = '00000' + s; break;
			case 6: s = '000000' + s; break;
			case 7: s = '0000000' + s; break;
			case 8: s = '00000000' + s; break;
		}
        return s;
    }
			   
    ///////////////
    // Main
    ///////////////
    this.addEvents();
};
