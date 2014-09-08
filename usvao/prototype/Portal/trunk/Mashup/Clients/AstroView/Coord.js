ASTROVIEW = ASTROVIEW || {};
/////////////////////////
// Coord
/////////////////////////
ASTROVIEW.Coord = function ( controller, normal )
{   
    // Initialize all helper classes 
    if (controller) this.setController(controller);
    
    // Coordinates are represented in the following systems: 
    this.screen = new THREE.Vector3();
    this.normal = new THREE.Vector3();
    
    // If normal vector passed in, initialize ourselves with it and 
    // return with smaller Coord object created.
    if (normal)
    {
        this.normal.copy(normal).normalize();   // Ensure sure this vector is Normalized
        return;
    }  
    
    this.world = new THREE.Vector3();
    this.radec =
    {
        "ra": 0.0, "dec": 0.0,
        "sra": "00:00:00", "sdec": "00:00:00",
        "dra": "0.0", "ddec": "0.0"
    };   
};

ASTROVIEW.Coord.prototype = new Object();
ASTROVIEW.Coord.prototype.constructor = ASTROVIEW.Coord;

// Static Vector3 Used below for intermediate results
ASTROVIEW.Coord.tempv = new THREE.Vector3();
    
ASTROVIEW.Coord.prototype.setController = function (controller)
{
    //
    // These 3 helper classes are REQUIRED for ALL coordinate transformation methods 
    //
    this.canvas = controller.canvas;       // HTML5 Canvas Element
    this.camera = controller.camera;       // THREE.PerspectiveCamera
    this.projector = controller.projector; // THREE.Projector
}

ASTROVIEW.Coord.prototype.copy = function (coord)
{    
    // Copy Helper References
    this.canvas = coord.canvas;       // HTML5 Canvas Element
    this.camera = coord.camera;       // THREE.PerspectiveCamera
    this.projector = coord.projector; // THREE.Projector
    
    // Copy all THREE.Vector3() instances
    this.screen.copy(coord.screen);
    this.normal.copy(coord.normal);
    this.world.copy(coord.world);
    
    // Copy RA/DEC Values
    this.radec.ra = coord.radec.ra;
    this.radec.dec = coord.radec.dec;
    this.radec.sra = coord.radec.sra;
    this.radec.sdec = coord.radec.sdec;
    this.radec.dra = coord.radec.dra;
    this.radec.ddec = coord.radec.ddec;
};

/////////////////////////
// updateAllCoords()
/////////////////////////  
ASTROVIEW.Coord.prototype.updateAllCoords = function(event)
{  
    // Event ====> Screen (local component coords)
    if (event) this.eventToScreen(event);

    // Screen ===> World (Normalized Unit Sphere)
    this.screenToNormal();   

    // World (Normalized) ===> World (Scaled to Diamond Radius)
    this.normalToWorld();

    // World (Normalized) ===> RA/DEC
    this.normalToRaDec();
}
    
ASTROVIEW.Coord.prototype.eventToScreen = function(event)
{    
    if (event.touches && event.touches.length > 0) // Works in Safari on iPad/iPhone
    {   
        var e = event.touches[ 0 ];
        var rect = (this.canvas.getBoundingClientRect ? this.canvas.getBoundingClientRect() : undefined);

        if (e.clientX != undefined && e.clientY != undefined && rect)
        {          
            this.screen.set( e.clientX - rect.left, 
                             e.clientY - rect.top,
                             1.0);
        }
        else
        {
            console.error("Coord:eventToScreen() Unable to determine canvas coordinates using event.touches[]");
        }
    }
    else if (event.offsetX != undefined && event.offsetY != undefined) // Works in Chrome / Safari (except on iPad/iPhone)
    {  
        this.screen.set(event.offsetX, event.offsetY, 1.0);
    }
    else if (event.layerX != undefined && event.layerY != undefined) // Works in Firefox
    {  
        this.screen.set(event.layerX, event.layerY, 1.0);
    }
    else
    {
        console.error ("Coord:eventToScreen() Unable to determine screen coordinates using event.");
    }
}

ASTROVIEW.Coord.prototype.screenToNormal = function()
{
    tempv = ASTROVIEW.Coord.tempv;
    tempv.set( (this.screen.x/this.canvas.clientWidth)*2-1,
              -(this.screen.y/this.canvas.clientHeight)*2+1, 
                1.0);
    this.projector.unprojectVector(tempv, this.camera);
    tempv.normalize();  
    this.normal.copy(tempv);  
}
    
ASTROVIEW.Coord.prototype.normalToScreen = function()
{
    tempv = ASTROVIEW.Coord.tempv;
    tempv.copy(this.normal);
    this.projector.projectVector(tempv, this.camera);
    this.screen.x = Math.round((tempv.x + 1)/2.0 * this.canvas.clientWidth);
    this.screen.y = Math.round(-(tempv.y - 1)/2.0 * this.canvas.clientHeight);
    this.screen.z = 0; 
}
    
ASTROVIEW.Coord.prototype.normalToWorld = function()
{
    this.world.copy(this.normal).multiplyScalar(ASTROVIEW.RADIUS);
}
    
ASTROVIEW.Coord.prototype.worldToScreen = function()
{
    this.normal.copy(this.world).normalize();
    this.normalToScreen();
}
 
ASTROVIEW.Coord.prototype.normalToRaDec = function()
{
    this.radec.dec = Math.asin(this.normal.y) * TO_DEGREES;         
    this.radec.ra = Math.atan2(-this.normal.x, -this.normal.z) * TO_DEGREES;
    if (this.radec.ra < 0) this.radec.ra += 360.0;
}
    
ASTROVIEW.Coord.prototype.radecToNormal = function()
{
    this.normal.y = Math.sin(this.radec.dec * TO_RADIANS);
    this.normal.x = -Math.sin(this.radec.ra * TO_RADIANS);
    this.normal.z = -Math.cos(this.radec.ra * TO_RADIANS);
}
    
ASTROVIEW.Coord.prototype.screenToRaDec = function()
{
    this.screenToNormal();
    this.normalToRaDec();
}
    
ASTROVIEW.Coord.prototype.radecToScreen = function()
{
    this.radecToNormal();
    this.normalToScreen();
}

ASTROVIEW.Coord.prototype.radecToString = function(format, fixed, round)
{
    // RA/DEC ===> Sexagesimal (String Representation)
    ASTROVIEW.Coord.radecToString(this.radec, fixed, round);
    return (format == "sexagesimal" ? 
        " " + this.radec.sra + "  " + this.radec.sdec + " " : 
        " " + this.radec.dra + "  " + this.radec.ddec + " ");
}

ASTROVIEW.Coord.prototype.raToString = function(format, fixed, round)
{
    // RA ===> Sexagesimal/Decimal (String Representation)
    ASTROVIEW.Coord.radecToString(this.radec, fixed, round);
    return (format == "sexagesimal" ? this.radec.sra : this.radec.dra);
}

ASTROVIEW.Coord.prototype.decToString = function(format, fixed, round)
{
    // DEC ===> Sexagesimal/Decimal (String Representation)
    ASTROVIEW.Coord.radecToString(this.radec, fixed, round);
    return (format == "sexagesimal" ? this.radec.sdec : this.radec.ddec);
}
    
////////////////////////////////
// "Static" Formatting Methods
////////////////////////////////
ASTROVIEW.Coord.radecToString = function(radec, fixed, round)
{
    if (typeof(fixed) == 'undefined') fixed = 3;
    
    var ra = round ? Math.round(radec.ra) : radec.ra;
    var dec = round ? Math.round(radec.dec) : radec.dec;
    
    radec.sra = ASTROVIEW.Coord.degToHMS(ra, fixed);
    radec.sdec = ASTROVIEW.Coord.degToDMS(dec, fixed);
    radec.dra = ra.toFixed(fixed);
    radec.ddec = dec.toFixed(fixed);
}

ASTROVIEW.Coord.degToDMS = function(degrees, fixed)
{       
    if (typeof(fixed) == 'undefined') fixed = 3;
    var pad = (fixed > 0 ? fixed+3 : 2);
     
    var deg = Math.abs(degrees);
    var deg_floor = Math.floor(deg);
    var min = 60 * (deg - deg_floor);
    var min_floor = Math.floor(min);
    var sec = 60 * (min - min_floor);
    
    var dms = ((degrees < 0.0) ? '-' : '+') +
              ASTROVIEW.Coord.pad(deg_floor, 2) + 
              ":" + ASTROVIEW.Coord.pad(min_floor, 2) +       
              ":" + ASTROVIEW.Coord.pad(sec.toFixed(fixed), pad);
    
    return dms;
}
    
ASTROVIEW.E = .00000001;
ASTROVIEW.Coord.degToHMS = function(degrees, fixed)
{
    if (typeof(fixed) == 'undefined') fixed = 3;
    var pad = (fixed > 0 ? fixed+3 : 2);
    
    var hours = degrees/15.0;
    var hours_floor = Math.floor(hours);
    var min = 60 * (hours - hours_floor + ASTROVIEW.E);
    var min_floor = Math.floor(min);
    var sec = 60 * (min - min_floor);
   
    var hms = ASTROVIEW.Coord.pad(hours_floor, 2) + 
              ":" + ASTROVIEW.Coord.pad(min_floor, 2) + 
              ":" + ASTROVIEW.Coord.pad(sec.toFixed(fixed), pad);
    
    return hms;
}
    
ASTROVIEW.Coord.pad = function (num, size)
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