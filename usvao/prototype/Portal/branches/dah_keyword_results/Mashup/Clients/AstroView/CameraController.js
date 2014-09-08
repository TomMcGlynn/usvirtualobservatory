/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( av )
{
    // Save the objects we need from the AstroBasicView
    this.av = av;
    this.camera = av.camera;
    
    this.graphicsScene = av.graphicsScene;
    this.selectScene = av.selectScene;
    
    this.renderers = av.renderers;
    this.canvas = av.canvas;
     
    this.radecView = av.radecView;
    this.crossLayer = av.crossLayer;
    this.spinLayer = av.spinLayer;

    // Load the Survey 
    this.survey = av.survey;
    this.zoomLevel = this.survey.zoomLevel;
    var len = this.survey.zoomLevel.length;
    this.fovmax = this.survey.zoomLevel[0].fov; 
    this.fovmin = this.survey.zoomLevel[len-1].fov; 
    this.zlevel = this.survey.zoomLevel[0].level; 
 
    // Camera mouse
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = this.fovmax;    
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.frustumMatrix = new THREE.Matrix4();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.delta = new THREE.Vector2();
 
    // Current Mouse Position 
    this.mouse = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };

    // Mouse State
    this.mouse.down = false;
    this.mouse.over = false;
    this.mouse.scale = 1.0;
    this.mouse.spin = false;
    
    // Last Mouse Position
    this.last = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };
    
    // Start Mouse Position
    this.start = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };
    
    // Used to Scale Screen/RA,DEC
    this.scaleStart = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };
    
    this.scaleEnd = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };
    
    this.pole = 
    {
        "screen": new THREE.Vector3(),
        "normal": new THREE.Vector3(),
        "world" : new THREE.Vector3(),
        "radec" :
        {
            "ra": 0.0, "dec": 0.0,
            "sra": "00:00:00", "sdec": "00:00:00",
            "dra": "0.0", "ddec": "0.0"
        }      
    };
    
    this.A = new THREE.Vector3();
    this.B = new THREE.Vector3();
    this.C = new THREE.Vector3();
    
    this.raXpixelScale = 0.0;
    this.decYpixelScale = 0.0;
    
    this.rotation = new THREE.Vector3();

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
        this.mouseDown(event, ASTROVIEW.CameraController.MouseParams);
    }
        
    this.onMouseMove = function(event)
    {
        this.mouseMove(event,  ASTROVIEW.CameraController.MouseParams);
    }
    
    this.onMouseUp = function(event)
    {
        this.mouseUp(event,  ASTROVIEW.CameraController.MouseParams);
    }
         
    this.onMouseOver = function(event)
    {
        event.preventDefault();
        this.mouse.over = true;
    }
    
    this.onMouseOut = function(event)
    {
        event.preventDefault(); 
        this.clearCanvas();
        this.mouse.down = false;
        this.mouse.over = false;
    }
    
    this.onMouseWheel = function(event)
    {      
        this.clearCanvas();
            
        // Get wheel direction for both WebKit or FF
        var delta = ((typeof event.wheelDelta != "undefined") ? (-event.wheelDelta) : event.detail );
        
        if (delta > 2) 
            delta = 2;
        else if (delta < -2) 
            delta = -2;
        
        if (delta > 0)
            this.zoomOut( ASTROVIEW.CameraController.MouseParams.zoomOutSpeed*delta);
        else
            this.zoomIn( ASTROVIEW.CameraController.MouseParams.zoomInSpeed*(-delta));
    }
    
    /////////////////////////
    // Touch Events
    ///////////////////////// 
    
    // NOTE: 
    // Gesture Events (2 finger pinch/zoom) also fire off Touch Events.
    // So in order to discern between the 2, be use the flags defined below: this.gestureMode and this.touchMode.
    // For our application, the Gesture Events (pinch/zoom) will take precedence over touch events (select/pan).
    // Therefore we ignore all touch events when we are in "Gesture" Mode.
    
    this.gestureMode = false;
    this.touchMode = false;
    this.onTouchStart = function(event)
    {    
        if (!this.gestureMode) // Ignore All Touch Events that are the result of a Gesture Event
        {
            this.touchMode = true;
            this.mouseDown(event,  ASTROVIEW.CameraController.TouchParams);
        }
    }
    
    this.onTouchMove = function( event )
    {     
        if (this.touchMode) // Ignore All Touch Events that are the result of a Gesture Event
        {
            this.mouseMove(event,  ASTROVIEW.CameraController.TouchParams);
        }
    }
    
    this.onTouchEnd = function(event)
    {   
        if (this.touchMode) // Ignore All Touch Events that are the result of a Gesture Event
        {
            this.touchMode = false;
            this.mouseUp(event,  ASTROVIEW.CameraController.TouchParams);
        }
    }
             
    /////////////////////////
    // Gesture Events
    /////////////////////////
    this.onGestureStart = function( event )
    {      
        this.gestureMode = true; 
        this.touchMode = false;
        if (event.scale)
        {
            event.preventDefault();          
            this.mouse.scale = event.scale;
        }
    }
    
    this.onGestureChange = function( event )
    {
        if (this.gestureMode)
        {
            this.touchMode = false;
            if (event.scale)
            {    
                event.preventDefault();

                var scaleDelta = event.scale - this.mouse.scale;
                if (scaleDelta > 0)
                {
                    this.zoomIn(scaleDelta *  ASTROVIEW.CameraController.TouchParams.zoomInSpeed);
                }
                else if (scaleDelta < 0)
                {
                    this.zoomOut(-scaleDelta *  ASTROVIEW.CameraController.TouchParams.zoomOutSpeed);
                }
             
                this.mouse.scale = event.scale;
            }
        }
    }
    
    this.onGestureEnd = function( event )
    {
        if (this.gestureMode)
        {
            this.gestureMode = false;
            this.touchMode = false;
            if (event.scale)
            {
                event.preventDefault();
             
                var scaleDelta = event.scale - this.mouse.scale;
                if (scaleDelta > 0)
                {
                    this.zoomIn(scaleDelta *  ASTROVIEW.CameraController.TouchParams.zoomInSpeed);
                }
                else if (scaleDelta < 0)
                {
                    this.zoomOut(-scaleDelta *  ASTROVIEW.CameraController.TouchParams.zoomOutSpeed);
                }
             
                this.mouse.scale = event.scale;
            }
        }
    }
    
    /////////////////////////
    // mouseDown()
    /////////////////////////
    this.mouseDown = function (event, params)
    {
        event.preventDefault();
           
        // Update all the coordinates on the mouse     
        this.updateMouseCoords(this.mouse, event);
        
        // Clear previous Hover Graphics
        this.clearCanvas();
        
        // Save the start and last mouse coords (used by mouseMove() and mouseUp())
        this.mouse.down=true;
        this.last.screen.copy(this.mouse.screen);
        this.start.screen.copy(this.mouse.screen);   
        
        // Update Xpixel scale and Ypixel scale (Used by panScene() below)
        this.updatePixelScale();    

        // Save this current camera Rotation, (Used by panScene() below)
        this.rotation.copy(this.camera.rotation);
    }
    
    /////////////////////////
    // mouseMove()
    /////////////////////////
    this.mouseMove = function(event, params)
    {
        event.preventDefault();
           
        // Update all the coordinates on the mouse     
        this.updateMouseCoords(this.mouse, event);
        
        // Clear previous Hover Graphics
        this.clearCanvas();
        
        // Update Live RA/DEC View
        this.radecView.updateLive(this.mouse.radec);
        
        if (this.mouse.down) 
        {
            this.panScene();
        }
        else if (!this.cameraChanged) 
        {
            // If camera has stopped moving, check for Selected Objects 
            // under the current mouse position for hovering 
            var selected = this.getSelected(params);  
            if (selected && selected.length > 0)
            {
                this.drawHoverGraphics(selected);
            }
        }
    }
        
    /////////////////////////
    // mouseUp()
    /////////////////////////
    this.selectStats = {};
    this.mouseUp = function(event, params)
    {
        event.preventDefault();   
        
        // Only process a mouseUp if we received the mouseDown
        if (this.mouse.down)
        {            
            this.mouse.down=false;
            this.mouse.spin=false;
            
            // Clear Spin Layer Graphics
            this.spinLayer.visible = false;
            this.av.render("CROSS");
            
            // Stop Rotation if User clicked single point
            if (this.last.screen.x == this.start.screen.x &&
                this.last.screen.y == this.start.screen.y)
            {
                // Stop any spinning graphicsScene rotation
                this.setCameraRotationDeltaXY(0, 0); 
                
                // Move the graphicsScene Select Point ('+') 
                this.moveCrossPoint(this.mouse.radec);
             
                // Check for Object Selection
                var start = new Date().getTime(); 
                var selected = this.getSelected(params);
                this.selectStats.getSelected = new Date().getTime() - start;
                this.selectStats.hits = (selected ? selected.length : 0);
             
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
    }
    
    //////////////////////
    // updatePixelScale()
    //////////////////////
    this.updatePixelScale = function()
    {
        // Determine Scale RA/X Pixel Scale in Radians/Pixel and DEC/Y Pixel Scale in Radians/Pixel
        // (Used by panScene() below)
        if (this.camera.rotation.x > 0)
        {
            this.scaleStart.screen.x = Math.round( this.canvas.width/2.0 );
            this.scaleStart.screen.y = Math.round( this.canvas.height - this.canvas.height/4.0 );
            this.updateMouseCoords(this.scaleStart); 
            
            this.scaleEnd.screen.x = this.scaleStart.screen.x + Math.round( this.canvas.height/4.0 );
            this.scaleEnd.screen.y = this.scaleStart.screen.y + Math.round( this.canvas.height/4.0 );
            this.updateMouseCoords(this.scaleEnd); 
        }
        else
        {
            this.scaleStart.screen.x = Math.round( this.canvas.width/2.0 );
            this.scaleStart.screen.y = Math.round( this.canvas.height/4.0 );
            this.updateMouseCoords(this.scaleStart); 
            
            this.scaleEnd.screen.x = this.scaleStart.screen.x + Math.round( this.canvas.height/4.0 );
            this.scaleEnd.screen.y = 1;
            this.updateMouseCoords(this.scaleEnd);
        }
        
        var xPixels = (this.scaleEnd.screen.x - this.scaleStart.screen.x);      // delta X in canvas Pixels
        var ra = (this.scaleStart.radec.ra - this.scaleEnd.radec.ra);           // delta RA in degrees
        if (ra < 0.0) ra += 360.0;
        this.raXpixelScale = ra/xPixels * TO_RADIANS;                           // Scale RA/X in Radians/Pixel
        
        var yPixels = (this.scaleEnd.screen.y - this.scaleStart.screen.y);      // delta Y in canvas Pixels
        var dec = (this.scaleStart.radec.dec - this.scaleEnd.radec.dec);        // delta DEC in Degrees
        this.decYpixelScale = dec/yPixels * TO_RADIANS;                         // delta DEC/Y in Radians/Pixel
    }
    
    /////////////////////////
    // panScene()
    /////////////////////////
    this.panScene = function()
    {
        var deltaY = this.mouse.screen.y - this.start.screen.y;
        var deltaX = this.mouse.screen.x - this.start.screen.x;
        var deltaDec = deltaY * this.decYpixelScale;
        var deltaRa = deltaX * this.raXpixelScale;
                
        // If not in spin mode, check if we should start it up
        if (!this.mouse.spin)
        {
            // Check (1): Rotation exceeds either the North or South Pole
            if ((this.rotation.x + deltaDec) > RADIANS_90)
            {
                this.cameraRotationX = RADIANS_90;
                console.log ("Spin mode 1: North Exceeded");
                this.mouse.spin = true;
            }
            else if ((this.rotation.x + deltaDec) < -RADIANS_90)
            {
                this.cameraRotationX = -RADIANS_90;
                console.log ("Spin mode 2: South Exceeded");
                this.mouse.spin = true;
            }
            
            // If pole point is visible on the screen and mouse moved horizontally dominant : Switch to Spin Mode
            this.pole.world.set(0, (this.rotation.x > 0 ? ASTROVIEW.RADIUS : -ASTROVIEW.RADIUS), 1);
            this.normalToScreen(this.pole.world, this.pole.screen);
        
            if ( this.pole.screen.x >= 0 && this.pole.screen.x <= this.canvas.width &&
                 this.pole.screen.y >= 0 && this.pole.screen.y <= this.canvas.height && 
                 Math.abs(deltaX)/Math.abs(deltaY) > 2.0 && Math.abs(deltaX) > 40)
            {
                console.log ("Spin mode 3: Horizontal + Pole Visible");
                this.mouse.spin = true;
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        //
        //  SPIN MODE: Used at the poles to spin the scene around the pole.
        //
        //  NOTE: Law of Cosines
        //  See Wikipedia : http://en.wikipedia.org/wiki/Law_of_cosines
        //
        //  Use the following to calculate the angle (r) between our mouse points:
        //
        //     A := last mouse
        //     B := current mouse
        //     C := north/south pole
        //
        //                     C
        //                     +
        //                   / r\
        //                 /     \
        //             b /        \ a
        //             /           \
        //           /              \
        //         /                 \
        //        +-------------------+
        //        A         c          B
        //
        //                   a*a + b*b - c*c
        //     r = arccos ( ---------------- )
        //                         2ab
        //
        ///////////////////////////////////////////////////////////////////////////
        
        // If we are in 'Spin Mode' rotate the camera by the angle created by the mouse to the pole (see Note Above)
        if (this.mouse.spin)
        {
            this.A.set(this.last.screen.x, this.last.screen.y, 0);
            this.B.set(this.mouse.screen.x, this.mouse.screen.y, 0);
            this.C.set(this.pole.screen.x, this.pole.screen.y, 0);
            
            var a = this.C.distanceTo(this.B);
            var b = this.C.distanceTo(this.A);
            var c = this.A.distanceTo(this.B);
                            
            var x = (a*a + b*b - c*c)/(2*a*b);
            
            if (isNumber(x) && x <= 1.0)
            {
                // rotation angle 
                var r = Math.acos(x);
                                 
                // Now determine Clockwise or Counterwise movement by the User Mouse
                var clockwise = this.isClock(this.pole.screen, this.last.screen, this.mouse.screen);
                if (clockwise) r = -r;              // Clockwise movement flips spin direction
                if (this.rotation.x < 0) r = -r;    // Sout Pole spins opposite direction as nNrth
                this.cameraRotationY += r;
            }      
        }
        else // Not in Spin Mode: rotate the sphere as normal
        {   
            // Update Camera Rotation on Y-axis in 3D Space
            this.cameraRotationY = this.rotation.y + deltaRa;
            this.cameraRotationX = this.rotation.x + deltaDec;
        }
        
        // Hide/Show Spin Layer based on Spin Mode
        this.spinLayer.visible = (this.mouse.spin); 
            
        // Save Last Mouse Position for re-entrant processing
        this.last.screen.copy(this.mouse.screen);
    }
    
    //////////////////////////////////////////////////////////////
    // isClock: (See http://tinyurl.com/bgandtl)
    //
    // Determines if current mouse position is moving clockwise or 
    // counter-clockwise relative to last mouse position
    // using the center of the pole as the Rotation Center
    //  
    //    A                 
    //    |\                A = Rotation Center (at the pole)
    //    | \               B = Previous Mouse Position
    //    |  C              C = Current Mouse Position
    //    B
    //
    //////////////////////////////////////////////////////////////
    this.isClock = function(a, b, c)
    {
         return ((b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x)) > 0;
    }
    
    /////////////////////////
    // drawHoverGraphics()
    /////////////////////////   
    this.drawHoverGraphics = function(selected)
    {
        // So now, draw the graphics in 'Hover Mode'
        var context = (this.canvas ? this.canvas.getContext( '2d' ) : null);
        if (context)
        {          
            // Set up Context for Hover Appearance, 
            context.save();
            context.lineWidth = 3;
                            
            for (var i=0; i<selected.length; i++)
            {
                var object = selected[i];
                
                // Set the stroke color
                if (object.material && object.material.color)
                {
                    context.strokeStyle = '#' + this.color2hex(object.material.color); //converts {r:0, g:1, b:0} to '#00FF00'
                }
                
                if (object instanceof THREE.Line)
                {       
                    context.beginPath();
                    
                    // Draw Line Segments (move to first point)
                    var vertices = object.geometry.vertices;
                    for (var j=0; j<vertices.length; j++)
                    {
                        var v = vertices[j];
                        if (j==0)
                            context.moveTo(v.screen.x, v.screen.y);
                        else
                            context.lineTo(v.screen.x, v.screen.y);
                    }
                       
                    // Done! Now fill the shape, and draw the stroke.
                    // Note: your shape will not be visible until you call any of the two methods.
                    context.closePath(); 
                    context.stroke();                        
                }
                else if (object instanceof THREE.Particle)
                {                           
                    // Draw Particle Graphic (move to first point)
                    var screen = object.position.screen;                          
                    context.beginPath();
                    context.strokeRect( screen.x-4, screen.y-4, 8, 8);
                    context.closePath();
                    context.stroke();
                }
                else
                {
                    console.log("object Type is Unknown = " + object);
                }
            }        
            // Restore previous context
            context.restore(); 
            
            // Set property on canvas to indicate canvas is no longer clear
            this.canvas.clear = false;
        }
    }
    
    /////////////////////////
    // clearCanvas()
    /////////////////////////
    this.clearCanvas = function ()
    {
        // Only clear the canvas if necessary
        if (!this.canvas.clear)
        {
            var context = (this.canvas ? this.canvas.getContext( '2d' ) : null);
            if (context)
            {
                // Set up Context for Hover Appearance, Clear out last Hover Graphics
                context.save();
                context.setTransform(1, 0, 0, 1, 0, 0);
                context.clearRect(0, 0, this.canvas.width, this.canvas.height);
                context.restore();
            }
            this.canvas.clear = true;
        }
    }
    
    this.color2hex = function (color) 
    {
        return this.rgb2hex(color.r*255, color.g*255, color.b*255);
    }
    
    this.rgb2hex = function (r, g, b) 
    {
        return Number(0x1000000 + r*0x10000 + g*0x100 + b).toString(16).substring(1);
    }
    
    /////////////////////////
    // updateMouseCoords()
    /////////////////////////  
    this.updateMouseCoords = function(mouse, event)
    {  
        // Event ====> Screen (local component coords)
        if (event)
        this.eventToScreen(event, mouse.screen);

        // Screen ===> World (Normalized Unit Sphere)
        if (mouse.screen && mouse.normal)
        this.screenToNormal(mouse.screen, mouse.normal);   

        // World (Normalized) ===> World (Scaled to Diamond Radius)
        if (mouse.normal && mouse.world)
        this.normalToWorld(mouse.normal, mouse.world);

        // World (Normalized) ===> RA/DEC
        if (mouse.normal && mouse.radec)
        this.normalToRaDec(mouse.normal, mouse.radec);

        // RA/DEC ===> Sexagesimal (String Representation)
        if (mouse.radec)
        this.raDecToString(mouse.radec);
    }
    
    this.eventToScreen = function(event, screen)
    {    
        if (event.touches && event.touches.length > 0) // Works in Safari on iPad/iPhone
        {   
            var e = event.touches[ 0 ];
            var rect = (this.canvas.getBoundingClientRect ? this.canvas.getBoundingClientRect() : undefined);

            if (e.clientX != undefined && e.clientY != undefined && rect)
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
        else if (event.offsetX != undefined && event.offsetY != undefined) // Works in Chrome / Safari (except on iPad/iPhone)
        {  
            screen.set(event.offsetX, event.offsetY, 1.0);
        }
        else if (event.layerX != undefined && event.layerY != undefined) // Works in Firefox
        {  
            screen.set(event.layerX, event.layerY, 1.0);
        }
        else
        {
            console.error ("CameraController:eventToScreen() Unable to determine screen coordinates using event.");
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
    
    this.normalToWorld = function(normal, world)
    {
        world.copy(normal).multiplyScalar(ASTROVIEW.RADIUS);
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
        screen.z = 0; 
    }
    
    this.normalToRaDec = function(normal, radec)
    {
        radec.dec = Math.asin(normal.y) * TO_DEGREES;         
        radec.ra = Math.atan2(-normal.x, -normal.z) * TO_DEGREES;
        if (radec.ra < 0) radec.ra += 360.0;
    }
    
    this.raDecToString = function(radec)
    {
        radec.sra = this.degToHMS(radec.ra);
        radec.sdec = this.degToDMS(radec.dec);
        radec.dra = this.pad(radec.ra.toFixed(3), 7);
        radec.ddec = this.pad(radec.dec.toFixed(3), 6);
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
                  this.pad(sec.toFixed(3), 6);
        
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
                  this.pad(sec.toFixed(3), 6);
        
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
    
    /////////////////////////
    // Keyboard Events
    /////////////////////////
    this.onKeyPress = function(event)
    {
        if (this.mouse.over)
        {
            var unicode=event.keyCode? event.keyCode : event.charCode;
            switch(unicode)
            {
                case 37:    this.rotateRight(); break;
                case 39:    this.rotateLeft(); break;
                case 38:    this.rotateDown(); break;
                case 40:    this.rotateUp(); break;
                case 105:   this.zoomIn( ASTROVIEW.CameraController.MouseParams.zoomInSpeed); break;
                case 111:   this.zoomOut( ASTROVIEW.CameraController.MouseParams.zoomOutSpeed); break;
            }
        }
    }
    
    /////////////////////////
    // stats()
    /////////////////////////  
    this.stats = function()
    {
       console.debug("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
                     " [ra,dec: " + this.mouse.radec.dra + "," + this.mouse.radec.ddec + "]" +
                     " [sra,sdec: " + this.mouse.radec.sra + "," + this.mouse.radec.sdec + "]" +
                     " [rot x: " + this.camera.rotation.x.toFixed(3) + " rot y:" + this.camera.rotation.y.toFixed(3) + "]" +
                     " [fov: " + this.camera.fov + "]" +
                     " [zlevel: " + this.zlevel + "]" );
    }
      
    /////////////////////////
    // Other Actions
    /////////////////////////   
    this.rotateLeft = function()
    {
        this.clearCanvas();
        var deltaY = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateRight = function()
    {
        this.clearCanvas();
        var deltaY = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateUp = function()
    {
        this.clearCanvas();
        var deltaX = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.rotateDown = function()
    {
        this.clearCanvas();
        var deltaX = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.setCameraRotationDeltaXY = function(deltax, deltay)
    {
        // Update the Target Rotation(s)
        this.cameraRotationX = this.camera.rotation.x + deltax;
        this.cameraRotationY = this.camera.rotation.y + deltay;
    }

    this.zoomIn = function(zoomSpeed)
    {
        this.clearCanvas();
        var deltaFov = this.cameraFov * zoomSpeed;
        this.cameraFov -= deltaFov;
        if (this.cameraFov < this.fovmmin) this.cameraFov = this.fovmmin;
    }
    
    this.zoomOut = function(zoomSpeed)
    {
        this.clearCanvas();
        var deltaFov = this.cameraFov * zoomSpeed;
        this.cameraFov += deltaFov;
        if (this.cameraFov > this.fovmax) this.cameraFov = this.fovmax;
    }
     
    this.moveTo = function(coord)
    {   
        this.clearCanvas();
          
        // Check all mixed case variations for RA and DEC and Zoom Properties
        if (coord.ra == undefined) coord.ra = (coord.RA != undefined ? coord.RA : (coord.Ra != undefined ? coord.Ra : undefined));
        if (coord.dec == undefined) coord.dec = (coord.DEC != undefined ? coord.DEC : (coord.Dec != undefined ? coord.Dec : undefined));
        if (coord.zoom == undefined) coord.zoom = (coord.ZOOM != undefined ? coord.ZOOM : (coord.Zoom != undefined ? coord.Zoom : undefined));
        
        // Tack on the Sexagesimal String values
        this.raDecToString(coord);
        
        // point camera to specified ra, dec
        this.camera.rotation.y = this.cameraRotationY = coord.ra * TO_RADIANS;
        this.camera.rotation.x = this.cameraRotationX = coord.dec * TO_RADIANS;
     
        // Set the Zoom Level
        this.setZoomLevel(coord.zoom);
        
        // Move the graphicsScene Cross Point ('+')
        this.moveCrossPoint(coord); 
    }
 
    /////////////////////////
    // setZoomLevel()
    /////////////////////////
    this.setZoomLevel = function(zlevel)
    {
        // Look up the Camera Fov based on zlevel
        for (var i=0; i<this.zoomLevel.length-1; i++)
        {
            if (this.zoomLevel[i].level <= zlevel && zlevel < this.zoomLevel[i+1].level)
            {
                this.camera.fov = this.cameraFov = this.zoomLevel[i].fov;
                this.camera.updateProjectionMatrix();
                console.debug("AstroView: setZoomLevel() zlevel:[" + this.zlevel +"]==>[" + zlevel + "] fov:[" + this.camera.fov + "]");
                this.zlevel = zlevel;
                break;
            }
        }
    }
    
    /////////////////////////
    // updateZoomLevel()
    /////////////////////////
    this.updateZoomLevel = function()
    {
        // Lookup the zlevel based on current Camera Fov        
        for (var i=0; i<this.zoomLevel.length-1; i++)
        {
            if (this.camera.fov <= this.zoomLevel[i].fov &&
                this.camera.fov >= this.zoomLevel[i+1].fov)
            {
                var zlevel = this.zoomLevel[i].level;
                if (zlevel != this.zlevel)
                {
                    console.debug("AstroView: updateZoomLevel() zlevel:[" + this.zlevel +"]==>[" + zlevel + "] fov:[" + this.camera.fov + "]");
                    this.zlevel = zlevel;
                }
                break;
            }
        }
    }
 
    this.getZoomLevel = function()
    {
        return this.zlevel;
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
        return (cameraChanged || sizeChanged);
    }
 
    /////////////////////////
    // updateCamera()
    /////////////////////////  
    this.cameraPosition = "";
    this.cameraChanged = false;
    this.updateCamera = function()
    {  
        // Bounds Check on RotationX (Declination)
        if (this.cameraRotationX > RADIANS_90) this.cameraRotationX = RADIANS_90;
        if (this.cameraRotationX < -RADIANS_90) this.cameraRotationX = -RADIANS_90;
        
        // Update the Rotation
        if (Math.abs(this.camera.rotation.x - this.cameraRotationX) > .0001)
        {
            this.camera.rotation.x += (this.cameraRotationX - this.camera.rotation.x) * 0.5;
        }
        else
        {
            this.camera.rotation.x = this.cameraRotationX;
        }
        
        if (Math.abs(this.camera.rotation.y - this.cameraRotationY) > .0001)
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
        else if (this.camera.fov !== this.cameraFov)
        {
            this.camera.fov = this.cameraFov;
            this.camera.updateProjectionMatrix();
        }
     
        // Check if camera position has changed since last update
        var cameraPosition = this.camera.rotation.x + ":" +
                             this.camera.rotation.y + ":" +
                            (this.camera.fov ? this.camera.fov : "") + 
                            (this.camera.aspect ? this.camera.aspect : "");
        this.cameraChanged = (cameraPosition != this.cameraPosition);
        this.cameraPosition = cameraPosition;
     
        // Return if the graphicsScene Viewport has changed
        return (this.cameraChanged);
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
        this.frustumMatrix.multiplyMatrices(this.camera.projectionMatrix, this.camera.matrixWorldInverse);
        this.frustum.setFromMatrix( this.frustumMatrix );
    }
    
    /////////////////////////
    // moveCrossPoint()
    /////////////////////////  
    this.moveCrossPoint = function(radec)
    {
        this.crossLayer.moveParticleTo(radec);  // Move the Select Point Location
        this.radecView.update(radec);
        this.av.render("CROSS");
    }
 
    /////////////////////////
    // getSelected()
    /////////////////////////  
    this.getSelected = function(params)
    {     
        if (!params) params = ASTROVIEW.CameraController.MouseParams;
           
        // Check for Selected Objects first 
        var selected = this.getSelectedScene(this.selectScene, params.lineDistance, params.particleDistance);  
        
        // Now check for any Unselected Objects if none found in the Selected Scene
        if (!selected || selected.length == 0)
        {
            selected = this.getSelectedScene(this.graphicsScene, params.lineDistance, params.particleDistance);
        }
        
        return selected;
    }
    
    this.getSelectedScene = function(scene, lineDistance, particleDistance)
    { 
        var selected = null;
        
        // Calculate the Distance from 3D mouse point on sphere to each line segment
        if (scene && scene.children)
        {
            for (var i=0; i<scene.children.length; i++)
            {
                var layer = scene.children[i];
                if (layer instanceof ASTROVIEW.GraphicsLayer)
                {
                    var hits = layer.getHits(this.mouse, this, lineDistance, particleDistance);
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
        //
        // Loop through all hits (which are objects in the graphicsScene)
        // Each hit contains a userData Object which contains the layer name and the original layer row[s].
        // Group the row[s] into layers[] array based on layer name
        // Send layers[] array as event data in the Select Event.
        //
        var layers = [];
        for (var i=0; i<hits.length; i++) 
        {
            var userData = hits[i].userData;
            if (userData && userData.name && userData.rows)
            {
                var layer = layers[userData.name];  
                if (layer !== undefined)
                {       
                    for (var r=0; r<userData.rows.length; r++)
                    {
                        layer.rows.push(userData.rows[r]);
                    }
                }   
                else // New layer item, add it to our layers list
                {   
                    layer = {"name" : userData.name, "rows": []};
                    for (var r=0; r<userData.rows.length; r++)
                    {
                        layer.rows.push(userData.rows[r]);
                    }               
                    layers.push(layer);
                }
            }
        }
     
        //
        // Notify AstroView of the Selected Layers
        //
        if (layers && layers.length > 0 && this.av)
        {
            this.av.sendEvent('AstroView.Objects.Selected', layers);
        }
    }   
            
    ///////////////
    // Main
    ///////////////
    this.addEvents();
};

// Camera Controller Params: Desktop (Mouse) vs. Mobile (Touch)
ASTROVIEW.CameraController.MouseParams = {
    "zoomInSpeed"       : 0.1,  // How fast you zoom in each mouse wheel
    "zoomOutSpeed"      : 0.2,  // How fast you zoom out each mouse wheel
    "lineDistance"      : 4.0,  // Line Distance in Pixels
    "particleDistance"  : 6.0};  // Particle Distance in Pixels

ASTROVIEW.CameraController.TouchParams = {
    "zoomInSpeed"       : 0.6,  // How fast you zoom in each mouse wheel
    "zoomOutSpeed"      : 3.0,  // How fast you zoom out each mouse wheel
    "lineDistance"      : 12.0, // Line Distance in Pixels
    "particleDistance"  : 18.0}; // Particle Distance in Pixels