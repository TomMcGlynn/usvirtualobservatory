/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( av, camera, scene, canvas, renderers )
{
    // Save the objects we need from the AstroBasicView
    this.av = av;
    this.camera = camera;
    this.scene = scene;
    this.canvas = canvas;
    this.renderers = renderers;

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
    this.zoomLookupTable =  [this.fovmax+1, // 0
                            this.fovmax+1,  // 1
                            this.fovmax+1,  // 2
                            this.fovmax+1,  // 3
                            10,             // 4
                            5,              // 5
                            2.5,            // 6
                            1.25,           // 7
                            0.75,           // 8
                            0.355,          // 9
                            0.125,          // 10
                            0.0625,         // 11
                            0.00001];       // 12
 
    //this.visibleLayers = "4, 5, 6, 7, 8, 9, 10, 11, 12";
    this.visibleLayers = "4, 6, 8, 10, 12";
 
    // Camera mouse
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = this.fovmax;    
    this.mouseZoomInFactor = 0.1;   // How fast you zoom in each mouse wheel
    this.mouseZoomOutFactor = 0.1;  // How fast you zoom out each mouse wheel
    this.mouseMoveFactor = 0.2;      // How fast you move/rotate scene on each mouse move.
    this.mouseLineDistance = 4.0;
    this.mouseParticleDistance = 6.0;
   
    this.touchZoomInFactor = 0.2;   // How fast you zoom on each touch gesture
    this.touchZoomOutFactor = 1.0;  // How fast you zoom on each touch gesture
    this.touchMoveFactor = 0.1;     // How fast you move/rotate scene on each touch move.
    this.touchLineDistance = 12.0;
    this.touchParticleDistance = 18.0;
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.frustumMatrix = new THREE.Matrix4();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.delta = new THREE.Vector2();
 
    // Current mouse position in ALL Coordinate Systems
    this.mouse = {};
    this.mouse.screen   = new THREE.Vector3();
    this.mouse.start    = new THREE.Vector3();
    this.mouse.last     = new THREE.Vector3();
    this.mouse.normal   = new THREE.Vector3();
    this.mouse.world    = new THREE.Vector3();
    this.mouse.graphics = new THREE.Vector3();

    this.mouse.down = false;
    this.mouse.over = false;
    this.mouse.scale = 1.0;

    // RA/DEC
    this.mouse.ra = 0.0;
    this.mouse.dec = 0.0;
    this.mouse.sra = "";
    this.mouse.sdec = "";

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {        
        // Mouse Events
        this.canvas.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        this.canvas.addEventListener( 'mouseover', bind(this, this.onMouseOver),  false );
        this.canvas.addEventListener( 'mousemove', bind(this, this.onMouseMove), false );
        this.canvas.addEventListener( 'mousedown', bind(this, this.onMouseDown), false );
        this.canvas.addEventListener( 'mouseup',   bind(this, this.onMouseUp),   false );
        
        // Touch Events
        this.canvas.addEventListener( 'touchstart', bind(this, this.onTouchStart), false );
        this.canvas.addEventListener( 'touchmove',  bind(this, this.onTouchMove),  false );
        this.canvas.addEventListener( 'touchend',   bind(this, this.onTouchEnd),   false );
        
        // Gesture Events
        this.canvas.addEventListener( 'gesturestart', bind(this, this.onGestureStart), false );
        this.canvas.addEventListener( 'gesturechange', bind(this, this.onGestureChange), false );
        this.canvas.addEventListener( 'gestureend', bind(this, this.onGestureEnd), false );
        
        // Mouse Wheel Events: Need both for WebKit and FF
        this.canvas.addEventListener('DOMMouseScroll', bind(this, this.onMouseWheel), false);
        this.canvas.addEventListener('mousewheel',     bind(this, this.onMouseWheel), false);
        
        // Keypress events
        document.addEventListener( "keypress" , bind(this, this.onKeyPress), false);
    }
 
    /////////////////////////
    // Mouse Events
    /////////////////////////
    this.onMouseDown = function(event)
    {
        this.mouseDown(event, this.mouseMoveFactor);
    }
        
    this.onMouseMove = function(event)
    {
        this.mouseMove(event, this.mouseMoveFactor);
    }
    
    this.onMouseUp = function(event)
    {
        this.mouseUp(event, this.mouseMoveFactor, this.mouseLineDistance, this.mouseParticleDistance);
    }
         
    this.onMouseOver = function(event)
    {
        event.preventDefault();
        this.mouse.over = true;
    }
    
    this.onMouseOut = function(event)
    {
        event.preventDefault();
        this.mouse.down = false;
        this.mouse.over = false;
    }
    
    this.onMouseWheel = function(event)
    {          
        // Get wheel direction for both WebKit or FF
        var delta = ((typeof event.wheelDelta != "undefined") ? (-event.wheelDelta) : event.detail );
        this.zoom(delta);
    }
    
    /////////////////////////
    // Touch Events
    /////////////////////////
    this.onTouchStart = function(event)
    {    
        this.mouseDown(event, this.touchMoveFactor);
    }
    
    this.onTouchMove = function( event )
    {   
        this.mouseMove(event, this.touchMoveFactor);
    }
    
    this.onTouchEnd = function(event)
    {
        this.mouseUp(event, this.touchMoveFactor, this.touchLineDistance, this.touchParticleDistance);
    }
    
    /////////////////////////
    // Move Actions
    /////////////////////////
    this.mouseDown = function (event, mouseSpeed)
    {
        event.preventDefault();
        this.updateMouse(event);
        this.mouse.down=true;
        this.mouse.start.x = this.mouse.last.x = this.mouse.screen.x;
        this.mouse.start.y = this.mouse.last.y = this.mouse.screen.y;        
    }
    
    this.mouseMove = function(event, mouseSpeed)
    {
        event.preventDefault();
    
        if (this.mouse.down)
        {
            this.updateMouse(event);
         
            var speed = (this.camera.fov/this.fovmax) * mouseSpeed;
            var rotationX = (this.mouse.screen.y - this.mouse.last.y) * TO_RADIANS * speed;
            var rotationY = (this.mouse.screen.x - this.mouse.last.x) * TO_RADIANS * speed;
            
            this.setCameraRotationDeltaXY(rotationX, rotationY);
                
            this.mouse.last.x = this.mouse.screen.x;
            this.mouse.last.y = this.mouse.screen.y;
        }
        
        // Check for Object Selection
        //var hits = this.getSelected();
    }
        
    this.selectStats = {};
    this.mouseUp = function(event, mouseSpeed, lineDistance, particleDistance)
    {
        event.preventDefault();        
        this.mouse.down=false;
        
        // Stop Rotation if User clicked single point
        if (this.mouse.last.x == this.mouse.start.x &&
            this.mouse.last.y == this.mouse.start.y)
        {
            this.setCameraRotationDeltaXY(0, 0); // Stop any spinning rotation
            
            // Debug Info on mouse click mouse
            this.dumpMouse();   
         
            // Check for Object Selection
            var start = new Date().getTime(); 
            var selected = this.getSelected(lineDistance, particleDistance);
            this.selectStats.getSelected = new Date().getTime() - start;
         
            // Send Hits Event
            if (selected && selected.length > 0) 
            {
                var start = new Date().getTime(); 
                this.sendAstroViewSelectEvent(selected);
                this.selectStats.sendAstroViewEvent = new Date().getTime() - start;
            }
         
            console.log("SELECT STATS: " + ASTROVIEW.toJson(this.selectStats));
        }
    }
    
    /////////////////////////
    // updateMouse()
    /////////////////////////  
    this.updateMouse = function(event)
    {  
        // Event ====> Screen (local component coords)
        this.eventToScreen(event, this.mouse.screen);

        // Screen ===> World (Normalized Unit Sphere)
        this.screenToNormal(this.mouse.screen, this.mouse.normal);

        // World (Normalized) ===> Graphics (Scaled to Graphics Radius)
        this.mouse.graphics.copy(this.mouse.normal).multiplyScalar(ASTROVIEW.RADIUS_GRAPHICS);      

        // World (Normalized) ===> World (Scaled to Diamond Radius)
        this.mouse.world.copy(this.mouse.normal).multiplyScalar(ASTROVIEW.RADIUS_SPHERE);

        // World (Normalized) ===> RA/DEC
        this.worldToRaDec(this.mouse.normal, this.mouse);

        // RA/DEC ===> Sexagesimal (String Representation)
        this.mouse.sra = this.degToHMS(this.mouse.ra);
        this.mouse.sdec = this.degToDMS(this.mouse.dec);
    }
    
    this.eventToScreen = function(event, screen)
    {    
        if (event.touches && event.touches.length > 0) // Works in Safari on iPad/iPhone
        {   
            var e = event.touches[ 0 ];
            var rect = (this.canvas.getBoundingClientRect ? this.canvas.getBoundingClientRect() : null);

            if (e.clientX && e.clientY && rect)
            {
                screen.set( e.clientX - rect.left, 
                            e.clientY - rect.top,
                            1.0);
            }
            else
            {
                console.error("CameraController:eventToScreen() Unable to determine canvas coordinates using event.touches[]");
            }
        }
        else if (event.offsetX && event.offsetY) // Works in Chrome / Safari (except on iPad/iPhone)
        {  
            screen.set(event.offsetX, event.offsetY, 1.0);
        }
        else if (event.layerX && event.layerY) // Works in Firefox
        {  
            screen.set(event.layerX, event.layerY, 1.0);
        }
        else
        {
            console.error ("CameraController:eventToScreen() Unable to determine canvas coordinates using event.");
        }
    }
    
    /////////////////////////
    // Keyboard Events
    /////////////////////////
    this.onKeyPress = function(event)
    {
        if (this.mouse.over)
        {
            var unicode=event.keyCode? event.keyCode : event.charCode;
            //alert("onKeyPress: " + unicode); // find the char code        
            switch(unicode)
            {
                case 37:    this.rotateRight(); break;
                case 39:    this.rotateLeft(); break;
                case 38:    this.rotateDown(); break;
                case 40:    this.rotateUp(); break;
                case 105:   this.zoomIn(this.mouseZoomInFactor); break;
                case 111:   this.zoomOut(this.mouseZoomOutFactor); break;
            }
        }
    }
       
    /////////////////////////
    // Gesture Events
    /////////////////////////
    this.onGestureStart = function( event )
    {
        if (event.scale)
        {
            event.preventDefault();          
            this.mouse.scale = event.scale;
        }
    }
    
    this.onGestureChange = function( event )
    {
        if (event.scale)
        {    
            event.preventDefault();

            var scaleDelta = event.scale - this.mouse.scale;
            if (scaleDelta > 0)
            {
                this.zoomIn(scaleDelta * this.touchZoomInFactor);
            }
            else if (scaleDelta < 0)
            {
                this.zoomOut(-scaleDelta * this.touchZoomOutFactor);
            }
         
            this.mouse.scale = event.scale;
        }
    }
    
    this.onGestureEnd = function( event )
    {
        if (event.scale)
        {
            event.preventDefault();
         
            var scaleDelta = event.scale - this.mouse.scale;
            if (scaleDelta > 0)
            {
                this.zoomIn(scaleDelta * this.touchZoomInFactor);
            }
            else if (scaleDelta < 0)
            {
                this.zoomOut(-scaleDelta * this.touchZoomOutFactor);
            }
         
            this.mouse.scale = event.scale;
        }
    }
    
    /////////////////////////
    // dumpMouse()
    /////////////////////////  
    this.dumpMouse = function()
    {
       console.debug("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
                     " [ra,dec: " + this.mouse.ra.toFixed(3) + "," + this.mouse.dec.toFixed(3) + "]" +
                     " [sra,sdec: " + this.mouse.sra + "," + this.mouse.sdec + "]" );
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
        (delta > 0 ? this.zoomOut(this.mouseZoomOutFactor) : this.zoomIn(this.mouseZoomInFactor));
    }
    
    this.zoomIn = function(zoomFactor)
    {
        var deltaFov = this.cameraFov * zoomFactor;
        this.cameraFov -= deltaFov;
        this.cameraFov = (this.cameraFov < this.fovmmin ? this.fovmmin : this.cameraFov);
    }
    
    this.zoomOut = function(zoomFactor)
    {
        var deltaFov = this.cameraFov * zoomFactor;
        this.cameraFov += deltaFov;
        this.cameraFov = (this.cameraFov > this.fovmax ? this.fovmax : this.cameraFov);
    }
     
    this.moveTo = function(ra, dec, zlevel)
    {        
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
    // updateController()
    /////////////////////////
    this.updateController = function()
    {
        var cameraChanged = this.updateCamera();
        var sizeChanged = this.updateSize();
        this.updateFrustumMatrix();
        this.updateZoomLevel();
        this.updateVisibleLevel();
        return (cameraChanged || sizeChanged);
    }
 
    /////////////////////////
    // updateCamera()
    /////////////////////////  
    this.cameraPosition = "";
    this.updateCamera = function()
    {  
        // Bounds Check on RotationX (Declination)
        if (this.cameraRotationX > RADIANS_90) this.cameraRotationX = RADIANS_90;
        if (this.cameraRotationX < -RADIANS_90) this.cameraRotationX = -RADIANS_90;
        
        // Update the Rotation
        if (Math.abs(this.camera.rotation.x - this.cameraRotationX) > .001)
        {
            this.camera.rotation.x += (this.cameraRotationX - this.camera.rotation.x) * 0.5;
        }
        else
        {
            this.camera.rotation.x = this.cameraRotationX;
        }
        
        if (Math.abs(this.camera.rotation.y - this.cameraRotationY) > .001)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
        else
        {
            this.camera.rotation.y = this.cameraRotationY;
        }
           
        // Update the Field of View
        if (Math.abs(this.camera.fov - this.cameraFov) > .001)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
            this.camera.updateProjectionMatrix();
        }
        else
        {
            this.camera.fov = this.cameraFov;
        }
     
        // Check if camera has changed since last update
        var cameraPosition = this.camera.rotation.x + ":" +
                            this.camera.rotation.y + ":" +
                            this.camera.fov;
        var cameraChanged = (cameraPosition != this.cameraPosition);
        this.cameraPosition = cameraPosition;
     
        // Return if the Scene Viewport has changed
        return (cameraChanged);
    }
    
    /////////////////////////
    // updateSize 
    /////////////////////////   
    this.parentNode = null; 
    this.updateSize = function( )
    {    
        var sizeChanged = false;
        var width=-1; var height=-1;

        // Get the first Canvas Parent that contains a width and height value (only need to do this once)
        if (!this.parentNode)
        {     
            var p = this.canvas.parentNode;
            while (p)
            {  
                if (p.style && p.style.width && p.style.height)
                {
                    this.parentNode = p;
                    break;
                }
                p = p.parentNode;       
            }
        }

        // Get parent node's width and height
        if (this.parentNode)
        {
            width = parseInt(this.parentNode.style.width);  // removes 'px' on the end
            height = parseInt(this.parentNode.style.height); // removes 'px' on the end
        }

        // Use the window if all else fails
        if (width === -1 && height === -1)
        {
            width = window.innerWidth;
            height = window.innerHeight;
        }

        // Update ALL the canvas elements with new width and height     
        if (width != this.canvas.width || height != this.canvas.height)
        {   
            if (this.renderers && this.renderers.length > 0)
            {
                for (var i=0; i<this.renderers.length; i++)
                {
                    this.renderers[i].setSize(width, height);
                }
            }

            this.camera.aspect = width/height;
            this.camera.updateProjectionMatrix();

            sizeChanged = true;
        }

        return sizeChanged;
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
    // ZoomLevel()
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
            console.debug("AstroView: Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel);
        }
    }
 
    this.getZoomLevel = function()
    {
        return this.zlevel;
    }
 
    ///////////////////////////
    // VisibleLevel()
    ///////////////////////////
    this.updateVisibleLevel = function()
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
            console.debug("AstroView: Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel);
        }
    }
 
    this.getVisibleLevel = function()
    {
        return this.vizlevel;
    }
 
    /////////////////////////
    // getSelected()
    /////////////////////////  
    this.getSelected = function(lineDistance, particleDistance)
    { 
        var selected = null;
        
        // Calculate the Distance from 3D mouse point on sphere to each line segment
        if (this.scene && this.scene.children)
        {
            for (var i=0; i<this.scene.children.length; i++)
            {
                var layer = this.scene.children[i];
                if (layer instanceof ASTROVIEW.GraphicsLayer)
                {
                    var hits = layer.getHits(this.mouse, this, lineDistance, particleDistance);
                    console.log ("CameraController: getSelected() layer : " + layer.name + " hits[] " + (hits ? hits.length : 0));
                    if (hits && hits.length)
                    {  
                        if (selected == null) selected = [];
                        for (var h=0; h<hits.length; h++)
                        {
                            selected.push(hits[h]);
                        }  
                    }
                }
            }
        }
     
        return selected;
    }
    
    /////////////////////////////
    // sendAstroViewSelectEvent()
    /////////////////////////////
    this.sendAstroViewSelectEvent = function(hits)
    {  
        var dict = {};
        var layers = null;
        for (var i=0; i<hits.length; i++) 
        {
            var hit = hits[i];
            if (hit && hit.userData && hit.userData.name && hit.userData.rows)
            {
                var layer = dict[hit.userData.name];
                if (layer && layer.rows)  // Found existing layer, add the rows from the hit item.
                {   
                    for (var r=0; r<hit.userData.rows.length; r++)
                    {
                        layer.rows.push(hit.userData.rows[r]);
                    }
                }   
                else // New layer item, add it to our layers list
                {   
                    layer = hit.userData;                       
                    dict[layer.name] = layer;
                    if (layers == null) layers = [];
                    layers.push(layer);
                }
            }
        }
     
        //
        // Notify AstroView of the Selected Layers
        //
        if (layers && this.av)
        {
            this.av.sendEvent('AstroView.Objects.Selected', layers);
        }
    }   
     
    this.tempv = new THREE.Vector3();   // Temp Vector used below
    this.screenToNormal = function(screen, normal)
    {
        this.tempv.set( (screen.x/this.canvas.width)*2-1,
                       -(screen.y/this.canvas.height)*2+1, 
                        1.0);
        this.projector.unprojectVector(this.tempv, this.camera);
        this.tempv.normalize();  
        normal.copy(this.tempv);  
    }
    
    this.worldToScreen = function(world, screen)
    {
        this.tempv.copy(world).normalize();
        this.normalToScreen(this.tempv, screen);
    }
    
    this.normalToScreen = function(normal, screen)
    {
        this.tempv.copy(normal);
        this.projector.projectVector(this.tempv, this.camera);
        screen.x = Math.round((this.tempv.x + 1)/2.0 * this.canvas.width);
        screen.y = Math.round(-(this.tempv.y - 1)/2.0 * this.canvas.height);
        screen.z = 0.0;
    }
    
    this.worldToRaDec = function(world, coord)
    {
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

