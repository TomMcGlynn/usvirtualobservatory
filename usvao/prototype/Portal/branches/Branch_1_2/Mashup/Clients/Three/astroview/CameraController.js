// Minimum Zoom/Fov Level
const ZOOM_LEVEL_MIN = 4;
const ZOOM_LEVEL_MAX = 12;
const FOV_LEVEL_MAX = 30;
const FOV_LEVEL_MIN = 0.00001;
    
ASTROVIEW.CameraController = function ( camera, container, fovmin, fovmax, radius)
{
	this.camera = camera;
	this.container = ( container !== undefined ) ? container : document;
    this.fovmin = (fovmin !== undefined) ? fovmin : FOV_LEVEL_MIN;
    this.fovmax = (fovmax !== undefined) ? fovmax : FOV_LEVEL_MAX;
    this.radius = radius;
              
    //
    // Fov ===> ZoomLevel Lookup Table
    //
    // Zoom Level:         1,  2,  3,  4,   5,   6,   7,   8,    9,     10,    11,     12,
    this.zoomLevelTable = [30, 30, 30, 30, 10.0, 5.0, 2.5, 1.25, 0.75, 0.355, 0.125, 0.0625, 0.00001];
    this.zoomLevel = ZOOM_LEVEL_MIN;
    
    // Camera Position
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = fovmax;
    this.lastFov;
    
    this.zoomSpeed = 0.1;  // How fast you zoom on each mouse wheel
    this.panSpeed = 0.2;   // How fast you pan on each mouse move.
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.down = new THREE.Vector2();
    this.last = new THREE.Vector2();
    this.delta = new THREE.Vector2();
    this.mouseDown = false;
    
    // Postion Events
    this.windowHalfX = window.innerWidth/2.0;
    this.windowHalfY = window.innerHeight/2.0;
    this.screenv = new THREE.Vector3();
    this.worldv = new THREE.Vector3();
    this.radec = {};

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
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
         
        this.updateRaDec(event);
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
        
        // Debug Info on mouse click position
        this.dumpRaDec();
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
                var rotationX = (event.touches[ 0 ].pageY - this.last.y) * TO_RADIANS * (this.camera.fov/(fovmax));
                var rotationY = (event.touches[ 0 ].pageX - this.last.x) * TO_RADIANS * (this.camera.fov/(fovmax));
                
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
        this.cameraRotationX = camera.rotation.x + deltax;
        this.cameraRotationY = camera.rotation.y + deltay;
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
        
        if (this.camera.rotation.y != this.cameraRotationY)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
           
        // Update the Zoom
        if (this.camera.fov !== this.cameraFov)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
            this.camera.updateProjectionMatrix();
        }
    }
    
    /////////////////////////
    // getZoomLevel()
    /////////////////////////  
    this.getZoomLevel = function()
    {
        // Determine new zoomLevel *only* if the Fov has changed
        if (this.lastFov !== this.camera.fov)
        {
            this.lastFov = this.camera.fov;
            var i=ZOOM_LEVEL_MIN-1;
            while (this.camera.fov <= this.zoomLevelTable[i])
            {
                i++;
            }
            if (this.zoomLevel !== i)
            {
                this.zoomLevel = i;
                console.debug("ZoomLevel: " + this.zoomLevel + " Fov:" + this.camera.fov);
            }
        }
        
        return this.zoomLevel;
    }
    
    /////////////////////////
    // moveTo()
    /////////////////////////  
    this.moveTo = function(ra, dec, zlevel)
    {
        console.debug("moveTo: [" + ra + ", " + dec + ", " +  zlevel + "]");
        
        // Check optional zlevel param
        if (!zlevel || zlevel > ZOOM_LEVEL_MAX) zlevel = 10;
        
        // point camera to ra
        this.camera.rotation.y = this.cameraRotationY = ra * TO_RADIANS;
        
        // point camera to dec
        this.camera.rotation.x = this.cameraRotationX = dec * TO_RADIANS;
        
        // now zoom in to max field of view for zlevel
        this.camera.fov = this.cameraFov = (this.zoomLevelTable[zlevel-1] + this.zoomLevelTable[zlevel])/2.0;
        this.camera.updateProjectionMatrix();
    }
    
    this.getFrustum = function()
    {
        this.frustum.setFromMatrix( new THREE.Matrix4().multiply( camera.projectionMatrix, camera.matrixWorldInverse ) );
        return this.frustum;
    }
    
    /////////////////////////
    // updateRaDec()
    /////////////////////////  
    this.updateRaDec = function(event)
    {
        this.screenv.x = event.clientX;
        this.screenv.y = event.clientY;
        this.screenv.z = 1.0; 

        this.screenToWorld(this.screenv, this.worldv, this.camera, this.projector);
        this.worldv.normalize();
        this.worldToRaDec(this.worldv, this.radec);
        this.worldv.multiplyScalar(this.radius);
    }
    
    this.dumpRaDec = function()
    {
       console.debug(" [screen x,y]:" + this.screenv.x + ", " + this.screenv.y +
                     " [world x,y,z]:" + this.worldv.x.toFixed(3) + "," + this.worldv.y.toFixed(3) + "," + this.worldv.z.toFixed(3) + 
                     " [ra,dec]: " + this.radec.ra.toFixed(3) + ", " + this.radec.dec.toFixed(3));
    }
    
    this.vToString = function(v)
    {
        return  "[x,y,z]: [" + v.x + "," + v.y + "," + v.z + "]";
    }

    this.screenToWorld = function(screenv, worldv, camera, projector)
    {
        worldv.x = screenv.x;
        worldv.y = screenv.y;
        worldv.z = screenv.z;
        
        worldv.x = worldv.x / this.windowHalfX - 1;
        worldv.y = - worldv.y / this.windowHalfY + 1;
        projector.unprojectVector( worldv, camera );
    }
    
    this.worldToScreen = function(worldv, screenv, camera, projector)
    {
        screenv.x = worldv.x;
        screenv.y = worldv.y;
        screenv.z = worldv.z;
        
        projector.projectVector( screenv, camera );
        screenv.x = ( screenv.x + 1 ) * this.windowHalfX;
        screenv.y = ( - screenv.y + 1) * this.windowHalfY;
    }
    
    this.worldToRaDec = function(worldv, radec)
    {
        // Store the ra, dec values
        radec.dec = Math.asin(worldv.y) * TO_DEGREES;			
        radec.ra = Math.atan2(-worldv.x, -worldv.z) * TO_DEGREES;
        if (radec.ra < 0) radec.ra += 360.0;
        
        // Convert ra, dec to the Sexagesimal String Equivalent
        radec.sra = this.deghms(radec.ra);
        radec.sdec = this.degdms(radec.dec);
    }
    
    this.degdms = function(degrees)
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
    
    this.deghms = function(degrees)
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
        var s = "000" + num;
        return s.substr(s.length-size);
    }
		
    this.normalizeV = function(v) 
    {
        var mag = v.x * v.x +
                  v.y * v.y +
                  v.z * v.z;
        
        if (mag != 0.0)
        {
            mag = 1.0/Math.sqrt(mag);
            v.x *= mag;
            v.y *= mag;
            v.z *= mag;
        }  
    }
    
    this.scaleV = function(v, radius)
    {
        v.x *= radius;
        v.y *= radius;
        v.z *= radius;
    };
			   
    ///////////////
    // Main
    ///////////////
    this.addEvents();
};
