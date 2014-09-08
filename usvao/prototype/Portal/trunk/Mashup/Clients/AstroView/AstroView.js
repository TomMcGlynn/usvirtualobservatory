///////////////
// Constants
///////////////
var Constants = Constants || {};

/** The Constant PI. */
Constants.PI = Math.PI;//3.141592653589793238462643383279502884197;

/** The Constant cPr. */
Constants.cPr = Math.PI / 180;

/** The Constant vlev. */
Constants.vlev = 2;

/** The Constant EPS. */
Constants.EPS = 0.0000001;

/** The Constant c. */
Constants.c = 0.105;

/** The verbose. */
//public static int verbose = Integer.parseInt(System.getProperty("verbose", "1"));

/** The Constant ln10. */
Constants.ln10 = Math.log(10);

/** The Constant piover2. */
Constants.piover2 = Math.PI / 2.;

/** The Constant twopi. */
Constants.twopi = 2*Math.PI;//6.283185307179586476925286766559005768394;// 2 *
                                                                                // PI;
/** The Constant twothird. */
Constants.twothird = 2. / 3.;

/** The Constant 1 arcsecond in units of radians. */
Constants.ARCSECOND_RADIAN = 4.84813681109536e-6;

/////////////////////////
// assert
/////////////////////////
function assert(condition, msg) 
{
    if (!condition) {
        throw new Error('assert failed: ' + msg);
    }
}

/////////////////////////
// HealpixIndex
/////////////////////////
var HEALPIX = HEALPIX || {};

// 
// Construct healpix routines tied to a given nside
// 
// @param nSIDE2
//            resolution number
// @throws Exception
// 
HEALPIX.HealpixIndex = function(nside, level)
{
    // Determine the number of sides based on args
    if (level != undefined)
    {
        this.level = level;
        nside = Math.pow(2.0, level);
    }
    
    if (nside != undefined) 
    { 
        if ( nside > HEALPIX.HealpixIndex.ns_max || nside < 1 ) {
            throw new Error("nside must be between 1 and " + HEALPIX.HealpixIndex.ns_max);
        }
        this.nside = nside;
        this.init();
    }
}

HEALPIX.HealpixIndex.prototype = new Object();
HEALPIX.HealpixIndex.prototype.constructor = HEALPIX.HealpixIndex;

/////////////////////////
// static
/////////////////////////
HEALPIX.HealpixIndex.ns_max = ns_max = 536870912;// 1048576;

/////////////////////////
// init
/////////////////////////
HEALPIX.HealpixIndex.prototype.init = function( )
{
    // coordinate of the lowest corner of each face
    this.jrll = [ 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4 ];
    this.jpll = [ 1, 3, 5, 7, 0, 2, 4, 6, 1, 3, 5, 7 ];
    
    // tablefiller
    var tabmax=0x100;
    this.ctab=[];
    this.utab=[];
    
    for (var m=0; m<0x100; ++m)
    {
        var cval = ( (m&0x1 )       | ((m&0x2 ) << 7) | ((m&0x4 ) >> 1) | ((m&0x8 ) << 6)
                  | ((m&0x10) >> 2) | ((m&0x20) << 5) | ((m&0x40) >> 3) | ((m&0x80) << 4));
        this.ctab.push(cval);
        
        var uval = ( (m&0x1 )       | ((m&0x2 ) << 1) | ((m&0x4 ) << 2) | ((m&0x8 ) << 3)
                  | ((m&0x10) << 4) | ((m&0x20) << 5) | ((m&0x40) << 6) | ((m&0x80) << 7));
        this.utab.push(uval);
    }
    // end tablefiller
        
    this.nl2 = 2 * this.nside;
    this.nl3 = 3 * this.nside;
    this.nl4 = 4 * this.nside;
    
    this.npface = this.nside * this.nside;
    this.ncap = 2 * this.nside * ( this.nside - 1 ); // points in each polar cap, =0 for
    
    this.npix =  12 * this.npface ;
    this.fact2 = 4.0 / this.npix;
    this.fact1 = (this.nside << 1) * this.fact2;
    
    this.order = this.nside2order(this.nside);
}

/////////////////////////
// nside2order
/////////////////////////
HEALPIX.HealpixIndex.prototype.nside2order = function(nside) 
{
    var order=0;
    assert (nside > 0);
    if ( ((nside)&(nside-1)) > 0 ) {
        return -1;
    }
    // ok c++ uses a a log - lookup should be better and
    // we do not have iog2 in java 
    // the posiiton in the array of nsides is the order !
    
    // order = Arrays.binarySearch(nsidelist, nside);   // TWR: Why is this called !?!?
    order = this.log2(nside);
    return order;
}

/////////////////////////
// log2
/////////////////////////
HEALPIX.HealpixIndex.prototype.log2 = function (num) 
{
    return (Math.log(num) / Math.log(2));
}

/////////////////////////
// corners_nest 
/////////////////////////
// Returns set of points along the boundary of the given pixel in NEST
// scheme. step=1 gives 4 points on the corners.
//
// @param pix
//            pixel index number in nest scheme
// @param step
// @return {@link Vector} for each points
// @throws Exception
//
HEALPIX.HealpixIndex.prototype.corners_nest = function (pix, step) 
{
    if (!step) step = 1;
    var pixr = this.nest2ring(pix);
    var corners = this.corners_ring(pixr, step);
    return corners;
}
  
/////////////////////////
// nest2ring 
/////////////////////////
// performs conversion from NESTED to RING pixel number
//
// @param ipnest
//           pixel NEST index number
// @return RING pixel index number
// @throws Exception
//
HEALPIX.HealpixIndex.prototype.nest2ring = function(ipnest) 
{
    var xyf = this.nest2xyf(ipnest);
    var ipring = this.xyf2ring(xyf.ix, xyf.iy, xyf.face_num);
    return ipring;
}

/////////////////////////
// nest2xyf
/////////////////////////
HEALPIX.HealpixIndex.prototype.nest2xyf = function(ipix) 
{ 
    var xyf = {}; //Xyf ret = new HEALPIX.Xyf();
    xyf.face_num =(ipix>>(2*this.order));
    var pix = ipix& (this.npface-1);
    
    // need o check the & here - they were unsigned in cpp ...
    var raw = (((pix & 0x555500000000)>>16) 
             | ((pix & 0x5555000000000000)>>31)
             |  (pix & 0x5555)
             | ((pix & 0x55550000)>>15));
                 
    xyf.ix =  this.ctab[raw&0xff]
           | (this.ctab[(raw>>8)&0xff]<<4)
           | (this.ctab[(raw>>16)&0xff]<<16)
           | (this.ctab[(raw>>24)&0xff]<<20);
         
    pix >>= 1;
    raw = (((pix & 0x555500000000)>>16) 
         | ((pix & 0x5555000000000000)>>31)
         |  (pix & 0x5555)
         | ((pix & 0x55550000)>>15));
                 
    xyf.iy =  this.ctab[raw&0xff]
           | (this.ctab[(raw>>8)&0xff]<<4)
           | (this.ctab[(raw>>16)&0xff]<<16)
           | (this.ctab[(raw>>24)&0xff]<<20);
         
    return xyf;
}

/////////////////////////
// xyf2ring
/////////////////////////
HEALPIX.HealpixIndex.prototype.xyf2ring = function(ix, iy, face_num) 
{
    var jr = (this.jrll[face_num]*this.nside) - ix - iy  - 1;

    var nr, kshift, n_before;
    if (jr<this.nside)
    {
        nr = jr;
        n_before = 2*nr*(nr-1);
        kshift = 0;
    }
    else if (jr > 3*this.nside)
    {
        nr = this.nl4-jr;
        n_before = this.npix - 2*(nr+1)*nr;
        kshift = 0;
    }
    else
    {
        nr = this.nside;
        n_before = this.ncap + (jr-this.nside)*this.nl4;
        kshift = (jr-this.nside)&1;
    }

    var jp = (this.jpll[face_num]*nr + ix - iy + 1 + kshift) / 2;
    if (jp>this.nl4)
        jp-=this.nl4;
    else
        if (jp<1) jp+=this.nl4;

    return n_before + jp - 1;       
}

/////////////////////////
// corners_ring
/////////////////////////
// 
// Returns set of points along the boundary of the given pixel in RING
// scheme. Step 1 gives 4 points on the corners.
// Mainly for graphics = you may not want to use LARGE NSIDEs..
// 
// @param pix
//            pixel index number in ring scheme
// @param step
// @return {@link SpatialVector} for each points
// @throws Exception
// 
HEALPIX.HealpixIndex.prototype.corners_ring = function(pix, step) 
{ 
    var nPoints = step * 2 + 2;
    var points = []; //SpatialVector[] points = new SpatialVector[nPoints];
    var p0 = this.pix2ang_ring(pix);
    var cos_theta = Math.cos(p0[0]);
    var theta = p0[0];
    var phi = p0[1];

    var i_zone = parseInt (( phi / Constants.piover2 ));
    var ringno = this.ring(pix);
    var i_phi_count = Math.min(ringno, Math.min(this.nside, ( this.nl4 ) - ringno));
    var i_phi = 0;
    var phifac = Constants.piover2 / i_phi_count;
    
    if ( ringno >= this.nside && ringno <= this.nl3 ) {
        // adjust by 0.5 for odd numbered rings in equatorial since
        // they start out of phase by half phifac.
        i_phi = parseInt( phi / phifac + ( ( ringno % 2 ) / 2.0 ) ) + 1;
    } else {
        i_phi = parseInt( phi / phifac ) + 1;
    }
    
    // adjust for zone offset
    i_phi = i_phi - ( i_zone * i_phi_count );
    var spoint = parseInt( nPoints / 2 );
    
    // get north south middle - middle should match theta !
    var nms = this.integration_limits_in_costh(ringno);
    var ntheta = Math.acos(nms[0]);
    var stheta = Math.acos(nms[2]);
    var philr = this.pixel_boundaries(ringno, i_phi, i_zone, nms[0]);
    
    if ( i_phi > ( i_phi_count / 2 ) ) {
        points[0] = this.vector(ntheta, philr[1]);
    } else {
        points[0] = this.vector(ntheta, philr[0]);
    }
    
    philr = this.pixel_boundaries(ringno, i_phi, i_zone, nms[2]);
    if ( i_phi > ( i_phi_count / 2 ) ) {
        points[spoint] = this.vector(stheta, philr[1]);
    } else {
        points[spoint] = this.vector(stheta, philr[0]);
    }
    
    if ( step == 1 ) 
    {
        var mtheta = Math.acos(nms[1]);
        philr = this.pixel_boundaries(ringno, i_phi, i_zone, nms[1]);
        points[1] = this.vector(mtheta, philr[0]);
        points[3] = this.vector(mtheta, philr[1]);
    } 
    else 
    {
        var cosThetaLen = nms[2] - nms[0];
        var cosThetaStep = ( cosThetaLen / ( step + 1 ) ); // skip
        
        // North and south
        for ( var p = 1; p <= step; p++ ) 
        {
            // Integrate points along the sides 
            cos_theta = nms[0] + ( cosThetaStep * p );
            theta = Math.acos(cos_theta);
            philr = this.pixel_boundaries(ringno, i_phi, i_zone, cos_theta);
            points[p] = this.vector(theta, philr[0]);
            points[nPoints - p] = this.vector(theta, philr[1]);
        }
    }
    return points;
}
  
/////////////////////////
// pix2ang_ring
/////////////////////////
// 
// Convert from pix number to angle renders theta and phi coordinates of the
// nominal pixel center for the pixel number ipix (RING scheme) given the
// map resolution parameter nside
// 
// @param ipix
//            pixel index number
// @return double array of [theta, phi] angles in radians [0,Pi], [0,2*Pi]
// @throws Exception
// 
HEALPIX.HealpixIndex.prototype.pix2ang_ring = function(ipix) 
{
    var theta, phi;
    var iring, iphi, ip, ipix1;
    var fodd, hip, fihip;
    // -----------------------------------------------------------------------
    if ( ipix < 0 || ipix > this.npix - 1 )
        throw new Error("ipix out of range");

    ipix1 = ipix + 1;// in {1, npix}

    // North Polar cap -------------
    if ( ipix1 <= this.ncap ) 
    { 
        hip = ipix1 / 2.0;
        fihip = parseInt(hip);
        iring = parseInt( Math.sqrt(hip - Math.sqrt(fihip)) ) + 1;
        // counted from North pole
        iphi = ipix1 - 2 * iring * ( iring - 1 );

        theta = Math.acos(1.0 - (iring* iring * this.fact2));
        phi = ( iphi - 0.50 ) * Constants.PI / ( 2.0 * iring );
    } 
    else 
    {
        // Equatorial region
        if ( ipix < (this.npix - this.ncap)  ) 
        { 
            ip = ipix - this.ncap;
            iring = parseInt ( ip / this.nl4 ) + this.nside;// counted from North pole
            iphi = parseInt(ip) % this.nl4 + 1;

            fodd = (((iring + this.nside)&1)>0) ? 1 : 0.5; 
            // 1 if iring+nside is odd, 1/2 otherwise
            theta = Math.acos(( this.nl2 - iring ) * this.fact1);
            phi = ( iphi - fodd ) * Constants.PI / this.nl2;
        } 
        else // South Polar cap -----------------------------------
        { 
            ip = this.npix - ipix;
            iring = parseInt(0.5*(1+Math.sqrt(2*ip-1)));
            // counted from South pole
            iphi = 4 * iring + 1 - ( ip - 2 * iring * ( iring - 1 ) );

            theta = Math.acos(-1.0 + Math.pow(iring, 2) * this.fact2);
            phi = ( iphi - 0.50 ) * Constants.PI / ( 2.0 * iring );
        }
    }

    var ret = [ theta, phi ];
    return ret;
}

/////////////////////////
// ring
/////////////////////////
// 
// return ring number for given pix in ring scheme
// 
// @param ipix
//            pixel index number in ring scheme
// @return ring number
// @throws Exception
// 
HEALPIX.HealpixIndex.prototype.ring = function(ipix) 
{
    var iring = 0;
    var ipix1 = ipix + 1; // in {1, npix}
    var ip;
    var hip, fihip = 0;
    
    // North Polar cap -------------
    if ( ipix1 <= this.ncap ) 
    { 
        hip = ipix1 / 2.0;
        fihip = parseInt(hip);
        iring = parseInt( Math.sqrt(hip - Math.sqrt(fihip)) ) + 1; // counted from North pole
    } 
    else 
    {
        // Equatorial region
        if ( ipix1 <= this.nl2 * ( 5 * this.nside + 1 ) ) 
        { 
            ip = parseInt(ipix1 - this.ncap - 1);
            iring = parseInt(( ip / this.nl4 ) + this.nside); // counted from North pole
        } 
        else // South Polar cap -----------------------------------
        { 
            ip = parseInt(this.npix - ipix1 + 1);
            hip = ip / 2.0;
            fihip = parseInt(hip);
            iring = parseInt( Math.sqrt(hip - Math.sqrt(fihip)) ) + 1; // counted from South pole
            iring = this.nl4 - iring;
        }
    }
    return iring;
}

///////////////////////////////
// integration_limits_in_costh
///////////////////////////////
// 
// integration limits in cos(theta) for a given ring i_th, i_th > 0
// 
// @param i_th
//            ith ring
// @return limits
// 
HEALPIX.HealpixIndex.prototype.integration_limits_in_costh = function(i_th) 
{
    var a, ab, b, r_n_side;

    // integration limits in cos(theta) for a given ring i_th
    // i > 0 !!!

    r_n_side = 1.0 * this.nside;
    if ( i_th <= this.nside ) 
    {
        ab = 1.0 - ( Math.pow(i_th, 2.0) / 3.0 ) / this.npface;
        b = 1.0 - ( Math.pow(( i_th - 1 ), 2.0) / 3.0 ) / this.npface;
        if ( i_th == this.nside ) 
        {
            a = 2.0 * ( this.nside - 1.0 ) / 3.0 / r_n_side;
        } 
        else 
        {
            a = 1.0 - Math.pow(( i_th + 1 ), 2) / 3.0 / this.npface;
        }
    } 
    else 
    {
        if ( i_th < this.nl3 ) 
        {
            ab = 2.0 * ( 2 * this.nside - i_th ) / 3.0 / r_n_side;
            b = 2.0 * ( 2 * this.nside - i_th + 1 ) / 3.0 / r_n_side;
            a = 2.0 * ( 2 * this.nside - i_th - 1 ) / 3.0 / r_n_side;
        } 
        else 
        {
            if ( i_th == this.nl3 ) 
            {
                b = 2.0 * ( -this.nside + 1 ) / 3.0 / r_n_side;
            } 
            else 
            {
                b = -1.0 + Math.pow(( 4 * this.nside - i_th + 1 ), 2) / 3.0 / this.npface;
            }

            a = -1.0 + Math.pow(( this.nl4 - i_th - 1 ), 2) / 3.0 / this.npface;
            ab = -1.0 + Math.pow(( this.nl4 - i_th ), 2) / 3.0 / this.npface;
        }

    }
    // END integration limits in cos(theta)
    var ret = [ b, ab, a ];
    return ret;
}

///////////////////////////////
// pixel_boundaries
///////////////////////////////
// 
// calculate the points of crossing for a given theata on the boundaries of
// the pixel - returns the left and right phi crossings
// 
// @param i_th
//            ith pixel
// @param i_phi
//            phi angle
// @param i_zone
//            ith zone (0,...,3), a quarter of sphere
// @param cos_theta
//            theta cosinus
// @return the left and right phi crossings
// 
HEALPIX.HealpixIndex.prototype.pixel_boundaries = function(i_th, i_phi, i_zone, cos_theta) 
{
    var sq3th, factor, jd, ju, ku, kd, phi_l, phi_r;
    var r_n_side = 1.0 * this.nside;

    // HALF a pixel away from both poles
    if ( Math.abs(cos_theta) >= 1.0 - 1.0 / 3.0 / this.npface ) 
    {
        phi_l = i_zone * Constants.piover2;
        phi_r = ( i_zone + 1 ) * Constants.piover2;
        var ret = [ phi_l, phi_r ];
        return ret;
    }
    
    // NORTH POLAR CAP
    if ( 1.50 * cos_theta >= 1.0 ) 
    {
        sq3th = Math.sqrt(3.0 * ( 1.0 - cos_theta ));
        factor = 1.0 / r_n_side / sq3th;
        jd = i_phi;
        ju = jd - 1;
        ku = ( i_th - i_phi );
        kd = ku + 1;
        // System.out.println(" cos_theta:"+cos_theta+" sq3th:"+sq3th+"
        // factor:"+factor+" jd:"+jd+" ju:"+ju+" ku:"+ku+" kd:"+kd+ "
        // izone:"+i_zone);
        phi_l = Constants.piover2
                * ( Math.max(( ju * factor ), ( 1.0 - ( kd * factor ) )) + i_zone );
        phi_r = Constants.piover2
                * ( Math.min(( 1.0 - ( ku * factor ) ), ( jd * factor )) + i_zone );
    } 
    else 
    {
        if ( -1.0 < 1.50 * cos_theta ) 
        {
            // EQUATORIAL ZONE
            var cth34 = 0.50 * ( 1.0 - 1.50 * cos_theta );
            var cth34_1 = cth34 + 1.0;
            var modfactor = parseInt( this.nside + ( i_th % 2 ) );

            jd = i_phi - ( modfactor - i_th ) / 2.0;
            ju = jd - 1;
            ku = ( modfactor + i_th ) / 2.0 - i_phi;
            kd = ku + 1;

            phi_l = Constants.piover2
                    * ( Math.max(( cth34_1 - ( kd / r_n_side ) ),
                            ( -cth34 + ( ju / r_n_side ) )) + i_zone );

            phi_r = Constants.piover2
                    * ( Math.min(( cth34_1 - ( ku / r_n_side ) ),
                            ( -cth34 + ( jd / r_n_side ) )) + i_zone );
        } 
        else  // SOUTH POLAR CAP
        {
            sq3th = Math.sqrt(3.0 * ( 1.0 + cos_theta ));
            factor = 1.0 / r_n_side / sq3th;
            var ns2 = 2 * this.nside;

            jd = i_th - ns2 + i_phi;
            ju = jd - 1;
            ku = ns2 - i_phi;
            kd = ku + 1;

            phi_l = Constants.piover2
                    * ( Math.max(( 1.0 - ( ns2 - ju ) * factor ),
                            ( ( ns2 - kd ) * factor )) + i_zone );

            phi_r = Constants.piover2
                    * ( Math.min(( 1.0 - ( ns2 - jd ) * factor ),
                            ( ( ns2 - ku ) * factor )) + i_zone );
        }
    }
    
    // and that's it
    // System.out.println(" nside:"+nside+" i_th:"+i_th+" i_phi:"+i_phi+"
    // izone:"+i_zone+" cos_theta:"+cos_theta+" phi_l:"+phi_l+"
    // phi_r:"+phi_r);

    var ret = [ phi_l, phi_r ];
    return ret;
}

/////////////////////////
// vector
/////////////////////////
// 
// Construct a {@link SpatialVector} from the angle (theta,phi)
// 
// @param theta
//            angle (along meridian), in [0,Pi], theta=0 : north pole
// @param phi
//            angle (along parallel), in [0,2*Pi]
// @return vector {@link SpatialVector}
// 
HEALPIX.HealpixIndex.prototype.vector = function(theta, phi) 
{
    var x, y, z;
    x = 1 * Math.sin(theta) * Math.cos(phi);
    y = 1 * Math.sin(theta) * Math.sin(phi);
    z = 1 * Math.cos(theta);
    return {"x":x, "y":y, "z":z};
}

//////////////////////////
// JSON 
//////////////////////////
var JSON = JSON || {};

//////////////////////////
// toJson() 
//////////////////////////
JSON.toJson = function(obj) 
{  
    var t = typeof (obj);  
    if (t != "object" || obj === null) 
    {  
        // simple data type  
        if (t == "string") obj = '"'+obj+'"';  
            return String(obj);  
    }  
    else 
    {  
        // recurse array or object  
        var n, v, json = [], arr = (obj && obj.constructor == Array);  
        for (n in obj) 
        {  
            v = obj[n]; 
            t = typeof(v);  
            if (t == "string") 
                v = '"'+v+'"';  
            else if (t == "object" && v !== null) 
                v = JSON.stringify(v);  
            json.push((arr ? "" : '"' + n + '":') + String(v));  
        }  
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");  
    }  
};

//////////////////////////
// fromJson() 
//////////////////////////
JSON.fromJson = function(str) 
{  
    if (str === "") str = '""';  
    eval("var p=" + str + ";");  
    return p;  
};

////////////////////////
// ASTROVIEW Namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Math Constants
////////////////////////
PI2 = Math.PI * 2.0;
TO_RADIANS = Math.PI/180.0;
TO_DEGREES = 180.0/Math.PI;

RADIANS_10 = 10.0 * TO_RADIANS;
RADIANS_30 = 30.0 * TO_RADIANS;
RADIANS_90 = 90.0 * TO_RADIANS;
RADIANS_100 = 100.0 * TO_RADIANS;
RADIANS_180 = 180.0 * TO_RADIANS;
RADIANS_360 = 360.0 * TO_RADIANS;

////////////////////////
// Constants
////////////////////////
// Diamond Radius and Viewport Far Plane
ASTROVIEW.RADIUS           = 1000;
ASTROVIEW.FOV_LEVEL_MAX    = 30;

// Refresh Rate
ASTROVIEW.TIMER_TICKS_ACTIVE = 10;
ASTROVIEW.TIMER_TICKS_IDLE   = 40;

//////////////////////////
// ASTROVIEW.MOBILE
//////////////////////////
ASTROVIEW.MOBILE = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

//////////////////////////
// ASTROVIEW.log() 
//////////////////////////
ASTROVIEW.debug = false;
ASTROVIEW.log = function(msg)
{
    if (ASTROVIEW.debug)
        console.log("AstroView: " + msg);
}

//////////////////////////
// ASTROVIEW.toJson() 
//////////////////////////
ASTROVIEW.toJson = function(obj) 
{  
    var t = typeof (obj);  
    if (t != "object" || obj === null) 
    {  
        // simple data type  
        if (t == "string") obj = '"'+obj+'"';  
            return String(obj);  
    }  
    else 
    {  
        // recurse array or object  
        var n, v, json = [], arr = (obj && obj.constructor == Array);  
        for (n in obj) 
        {  
            v = obj[n]; 
            t = typeof(v);  
            if (t == "string") 
                v = '"'+v+'"';  
            else if (t == "object" && v !== null) 
                v = JSON.stringify(v);  
            json.push((arr ? "" : '"' + n + '":') + String(v));  
        }  
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");  
    }  
}

//////////////////////////
// ASTROVIEW.fromJson() 
//////////////////////////
ASTROVIEW.fromJson = function(str) 
{  
    if (str === "") str = '""';  
    eval("var p=" + str + ";");  
    return p;  
}


//////////////////////////
// ASTROVIEW.fromXml() 
//////////////////////////
ASTROVIEW.fromXml = function(xml) 
{  
    var xmlDoc;
    if (window.DOMParser)
    {
        parser=new DOMParser();
        xmlDoc=parser.parseFromString(xml,"text/xml");
    }
    else // Internet Explorer
    {
        xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async=false;
        xmlDoc.loadXML(xml); 
    }
    return xmlDoc;
}

////////////////////////
// ASTROVIEW.bind()
////////////////////////
ASTROVIEW.bind = function( scope, fn )
{
    return function ()
    {
        fn.apply( scope, arguments );
    };
}

////////////////////////
// ASTROVIEW.isString()
////////////////////////
ASTROVIEW.isString = function(s)
{
    return typeof(s) === 'string' || s instanceof String;
}

////////////////////////
// ASTROVIEW.isNumber()
////////////////////////
ASTROVIEW.isNumber = function(n) 
{
    return !isNaN(parseFloat(n)) && isFinite(n);
}

////////////////////////
// ASTROVIEW.trim()
////////////////////////
ASTROVIEW.trim = function(s)
{
    return s.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}   ////////////////////////
// AstroBasicView
////////////////////////
ASTROVIEW.AstroBasicView = function ( config )
{  
    //
    // Set Config Defaults, if Not specified
    //
    if (!config) config = {};
    this.config = config;
    
    // Create element if not specified
    if (!this.config.div)
    {
        this.config.div = document.createElement('div');
        document.body.appendChild(this.config.div);
    }
    
    // Set properties on the parent div 
    this.config.div.id = "divAstroView";
    this.config.div.style.backgroundColor = "#222222";

    // Validate Config RenderType
    if (!this.config.rendertype)
    {
        this.config.rendertype = "canvas";
    }
    if (this.config.rendertype !== "canvas" && this.config.rendertype !== "webgl")
    {
        alert ("Unknown Render Type: " + config.rendertype)
    }
    
    // Load initial survey
    if (!this.config.surveytype)
    {
        this.config.surveytype = "DSS";
    }
    
    // Save Config Properties
    this.config.avcontainer = (config.avcontainer ? config.avcontainer : null);
    this.config.debug = (config.debug ? config.debug : false);
    ASTROVIEW.debug = this.config.debug;
       
    // Core 3D objects
    this.scene = null; 
    this.camera = null;
    this.canvas = null;
    this.controller = null;
    this.projector = new THREE.Projector();

    // Renderers
    this.renderers = null;
    this.diamondRenderer = null;
    this.bufferRenderer = null;
    this.graphicsRenderer = null;
    this.crossRenderer = null;
    this.hoverRenderer = null;
   
    // Scenes
    this.diamondScene = null;
    this.graphicsScene = null;
    this.selectScene = null;
    this.crossScene = null;
    this.scenes = null;
    
    // Scene Objects
    this.toastSphere = null;
    this.healpixSphere = null;
    this.crossLayer = null;
    this.spinLayerClock = null;
    this.spinLayerCounter = null;
    this.gridLayer = null;
    this.radecView = null;
   
    // Rendering Stats
    this.stats = null;
    
    // Animation Timer
    this.timer = new ASTROVIEW.Timer();
    this.zoomInTimer = new ASTROVIEW.Timer();
    this.zoomOutTimer = new ASTROVIEW.Timer();

    /////////////////
    // createScene()
    /////////////////
    this.createScene = function() 
    {
        // Create Camera
        this.camera = new THREE.PerspectiveCamera(ASTROVIEW.FOV_LEVEL_MAX,
                                                  window.innerWidth / window.innerHeight,
                                                  1,
                                                  ASTROVIEW.RADIUS*10);
        this.camera.position.set( 0, 0, 0 );
        this.camera.eulerOrder = 'YXZ';
        this.camera.name = "PerspectiveCamera";
            
        // Create Renderers (one for each scene) 
        this.renderers = this.createRenderers(this.config.rendertype);
        if (!this.renderers || this.renderers.length == 0)
        {
            alert ("AstroView: Unable to create renderer(s) of type: " + this.config.rendertype);
            console.error("AstroView: Unable to create renderer(s) of type: " + this.config.rendertype);
            return;
        }
        else
        {   
            // Set default canvas to the last renderer created
            var last = this.renderers.length-1;
            this.canvas = this.renderers[last].domElement;
        }
        
        // Create Scenes (one for each renderer) 
        this.scenes = this.createScenes(this.config.rendertype);
        if (!this.scenes || this.scenes.length == 0 || !this.graphicsScene)
        {
            alert ("AstroView: Unable to create scenes(s) of type: " + this.config.rendertype);
            console.error("AstroView: Unable to create scenes(s) of type: " + this.config.rendertype);
            return;
        }
        else
        {   
            // Set default scene to the graphics Scene
            this.scene = this.graphicsScene;
        }
        
        // Create Grid Layer (invisible by default)
        this.gridLayer = this.createGridLayer();
        this.gridLayer.visible = false;
        
        // Create Spin Indicator Layers (invisible by default)
        this.spinLayerClock = this.createSpinLayer("SpinLayerClock", "spinClock");
        this.spinLayerClock.visible = false;
        this.spinLayerCounter = this.createSpinLayer("SpinLayerCounter", "spinCounter");
        this.spinLayerCounter.visible = false;
        
        // Create Cross Point Layer
        this.crossLayer = this.createCrossLayer();
        this.crossLayer.visible = true;
        
        // Create Camera Controller
        this.controller = new ASTROVIEW.CameraController( this );
                                                  
        if (this.config.debug)
        {
            // Create Stats View [Debug Only]
            this.stats = new Stats();
            this.stats.domElement.style.position = 'absolute';
            this.stats.domElement.style.top = '4px';
            this.stats.domElement.style.left = '4px';
            this.config.div.appendChild(this.stats.domElement);
        
            // Add Keyboard Icon [Debug Only]
            this.keyboardIcon = new Image();
            this.keyboardIcon.id = 'imgKeyboard';
            this.keyboardIcon.src = '../../Clients/AstroView/keyboard.png';
            this.keyboardIcon.style.position = 'absolute';
            this.keyboardIcon.style.top = '60px';
            this.keyboardIcon.style.left = '4px';
            this.keyboardIcon.style.zIndex = 10;
            this.keyboardIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onKeyboardClick),  false );
            this.config.div.appendChild( this.keyboardIcon );
        }
    }

    ////////////////////
    // createRenderers()
    ////////////////////
    this.createRenderers = function(rendertype)
    {     
        var renderers = [];
        
        //
        // (1) Create the DiamondRenderer
        //
        switch (rendertype)
        {
            // NOTE: We try to create a WebGL Renderer and fallback to the Canvas Renderer.   
            case "webgl":
            {
                try{
                    // Create WegGL Diamond Renderer
                    this.diamondRenderer = new THREE.WebGLRenderer();
                    this.diamondRenderer.setSize( window.innerWidth, window.innerHeight ); 
                    this.diamondRenderer.setPolygonOffset(true, 1.0, 1.0);
                    renderers.push(this.diamondRenderer);
                    
                    break;
                } catch(error) {this.config.rendertype = "canvas";}    // Try to create Canvas Renderer as fallback
            }
            case "canvas":
            {  
                try{   
                    // Create Canvas Diamond Renderer
                    this.diamondRenderer = new THREE.CanvasRendererMobile();
                    this.diamondRenderer.setSize( window.innerWidth, window.innerHeight );
                    this.diamondRenderer.sortElements = false;
                    renderers.push(this.diamondRenderer);
                    
                    break;
                } catch(error) {this.diamondRenderer=null;}            
            }         
        }
        
        //
        // Create all the other layers:
        //
        // Graphics Layer
        // Select Layer
        // Cross Layer
        // Hover Layer
        //
        if (this.diamondRenderer != null)
        {     
            // Buffer Renderer (Used for all graphics off screen rendering)   
            this.bufferRenderer = new THREE.CanvasRendererMobile();
            this.bufferRenderer.setSize( window.innerWidth, window.innerHeight );
            this.bufferRenderer.autoClear = false;
            this.bufferRenderer.sortElements = false;
            renderers.push(this.bufferRenderer);
            
            // Graphics Renderer
            this.graphicsRenderer = new THREE.CanvasRendererMobile();
            this.graphicsRenderer.setSize( window.innerWidth, window.innerHeight );
            this.graphicsRenderer.autoClear = false;
            this.graphicsRenderer.sortElements = false;
            renderers.push(this.graphicsRenderer);
            
            // Select Renderer
            this.selectRenderer = new THREE.CanvasRendererMobile();
            this.selectRenderer.setSize( window.innerWidth, window.innerHeight );
            this.selectRenderer.autoClear = false;
            this.selectRenderer.sortElements = false;
            renderers.push(this.selectRenderer);
            
            // Cross Renderer
            this.crossRenderer = new THREE.CanvasRendererMobile();
            this.crossRenderer.setSize( window.innerWidth, window.innerHeight );
            this.crossRenderer.autoClear = false;
            this.crossRenderer.sortElements = false;
            renderers.push(this.crossRenderer);
            
            // Hover Renderer
            this.hoverRenderer = new THREE.CanvasRendererMobile();
            this.hoverRenderer.setSize( window.innerWidth, window.innerHeight );
            this.hoverRenderer.autoClear = false;
            this.hoverRenderer.sortElements = false;
            renderers.push(this.hoverRenderer);                 
        }
        
        //
        // Set Canvas Properties so they stack up in the browser as separate Layers
        //
        var zIndex = 1;
        if (this.diamondRenderer && this.diamondRenderer.domElement)
        {       
            var canvas = this.diamondRenderer.domElement;                     
            canvas.id = "DiamondCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas );      
        }
        
        if (this.graphicsRenderer && this.graphicsRenderer.domElement)
        {                           
            var canvas = this.graphicsRenderer.domElement;
            canvas.id = "GraphicsCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.selectRenderer && this.selectRenderer.domElement)
        {                           
            var canvas = this.selectRenderer.domElement;
            canvas.id = "SelectCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.crossRenderer && this.crossRenderer.domElement)
        {                           
            var canvas = this.crossRenderer.domElement;
            canvas.id = "CrossCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.hoverRenderer && this.hoverRenderer.domElement)
        {                           
            var canvas = this.hoverRenderer.domElement;
            canvas.id = "HoverCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        // Return all renderers created
        return renderers; 
    }
    
    this.createScenes = function(rendertype)
    {
        var scenes = [];
        
        //////////////////////////////////////////////////////////////////////////////////////
        // CAMERA PARENT NOTE: 
        // To work around (THREE.Object3D) unnecessary THREE behavior, we do the following HACK:
        //
        // this.camera.parent = undefined;
        //
        // THREE.Object3D wants to perform a removal of the camera from the previous Scene if camera.parent is (!=undefined).
        // So we temporarily set it to 'undefined' to avoid its removal from the previous Scene.  
        //////////////////////////////////////////////////////////////////////////////////////
             
        // Diamond Scene
        this.camera.parent = undefined;  // See CAMERA PARENT NOTE (above)
        this.diamondScene = (rendertype == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.diamondScene.add (this.camera);
        this.diamondScene.name = "DiamondScene";  
        this.diamondScene.matrixAutoUpdate = false;
        scenes.push(this.diamondScene);
             
        // Create appropriate child Sphere based on survey type: 'toast' vs. 'healpix'      
        if (this.survey.type == "toast")
        {   
            // Create Toast Sphere 
            this.toastSphere = new ASTROVIEW.ToastSphere(ASTROVIEW.RADIUS, this.survey );  
            this.toastSphere.matrixAutoUpdate = true;
            this.toastSphere.name = "ToastSphere";
            this.diamondScene.add(this.toastSphere);
        }
        else if (this.survey.type == "healpix")
        {
            // Create Healpix Sphere   
            this.healpixSphere = new ASTROVIEW.HealpixSphere(ASTROVIEW.RADIUS, this.survey );
            this.healpixSphere.matrixAutoUpdate = true;
            this.healpixSphere.name = "HealpixSphere";
            this.diamondScene.add(this.healpixSphere);   
        }
             
        // Graphics Scene 
        this.camera.parent = undefined;  // See CAMERA PARENT NOTE (above)
        this.graphicsScene = new THREE.CanvasScene();
        this.graphicsScene.add(this.camera);
        this.graphicsScene.name = "GraphicsScene";
        this.graphicsScene.matrixAutoUpdate = false;
        scenes.push(this.graphicsScene);
        
        // Select Scene
        this.camera.parent = undefined; // See CAMERA PARENT NOTE (above)
        this.selectScene = new THREE.CanvasScene();
        this.selectScene.add(this.camera);
        this.selectScene.name = "SelectScene";
        this.selectScene.matrixAutoUpdate = false;
        scenes.push(this.selectScene);
        
        // Cross Scene
        this.camera.parent = undefined; // See CAMERA PARENT NOTE (above)
        this.crossScene = new THREE.CanvasScene();
        this.crossScene.add(this.camera);
        this.crossScene.name = "CrossScene";
        this.crossScene.matrixAutoUpdate = false;
        scenes.push(this.crossScene);
        
        return scenes;
    }

    /////////////////////////////
    // renderScene()
    /////////////////////////////
    this.renderScene = function() 
    {     
        // Update the Camera Position 
        var cameraChanged = this.controller.updateController();
        
        // Reset the Rendering State to "CAMERA" if the Camera Changed
        if (cameraChanged) this.render("CAMERA");
        
        // Update Each Diamond Sphere in the View Frustum, using the latest Canvas Size & Camera Position
        if (this.toastSphere) 
            this.toastSphere.renderScene(this);
        if (this.healpixSphere) 
            this.healpixSphere.renderScene(this);
          
        // Render Scene contents to HTML5 Canvas using WebGL or Canvas Context passing the Rendering Info      
        this.renderSceneCanvas(this.ri);
        
        // Update the stats window
        if (this.stats) this.stats.update();
        
        // Update the refresh rate based on Active Mouse and Current Render State
        var ticks = (this.ri.state == "IDLE" ? ASTROVIEW.TIMER_TICKS_IDLE : ASTROVIEW.TIMER_TICKS_ACTIVE);
   
        // Request another animation frame, if ticks is different
        if (ticks != this.timer.ticks)
        {
            this.timer.start(ASTROVIEW.bind(this, this.renderScene), ticks);
        }
    }
    
    /////////////////////////////
    // clearScene()
    /////////////////////////////
    this.clearScene = function()
    {
        if (this.toastSphere) 
            this.toastSphere.clearScene(this);
        if (this.healpixSphere) 
            this.healpixSphere.clearScene(this);
    }
    
    ////////////////////////
    // renderSceneWebGL()
    ////////////////////////
    this.renderSceneWebGL = function (ri)
    {
        this.diamondRenderer.render(this.diamondScene, this.camera);
        this.graphicsRenderer.render(this.graphicsScene, this.camera);
        this.crossRenderer.render(this.crossScene, this.camera);

        // Reset render State
        ri.state = "IDLE";
    }
    
    ////////////////////////
    // Render Info
    ////////////////////////
    this.ri = {};
    var o=0; // order
    this.ri.order = {"CAMERA":          o++, 
                     "DIAMOND":         o++, 
                     "CROSS":           o++, 
                     "GRAPHICS":        o++, 
                     "RENDER_GRAPHICS": o++, 
                     "SELECT":          o++,  
                     "RENDER_SELECT":   o++,                      
                     "COMPLETE":        o++, 
                     "IDLE":            o++};
                       
    this.ri.state = "CAMERA";
    this.ri.queue = [];
    
    this.ri.count = 0;
    this.ri.start = 0;
    this.ri.end = 0;
    this.ri.RENDER_SIZE = 200;
    
    this.ri.log = "";
    this.ri.logstate = "";

    ////////////////////////
    // render()
    ////////////////////////
    this.render = function(state)
    {
        // If canvas id was not specified, render camera layers
        if (!state || state == "") state = "CAMERA";
        
        if (this.ri.order.hasOwnProperty(state))
        {
            if (state == "CAMERA")
            {
                this.ri.state = state;
                this.ri.queue = [];
            }
            else if (this.ri.queue.indexOf(state) < 0)
            {
                this.ri.queue.push(state);
            }
        }
        else
        {
            console.error ("AstroView: Invalid render state passed to render(): " + state);
        }
    }
    
    ////////////////////////
    // renderSceneCanvas()
    ////////////////////////
    this.renderSceneCanvas = function(ri) 
    {   
        // If current state is before COMPLETE, we capture performance stats
        var time = ((ri.state != "COMPLETE" && ri.state != "IDLE") ? new Date().getTime(): undefined);
        var state = ri.state;
        
        switch (ri.state)
        { 
            case "CAMERA":   // (0): Clear camera existing Canvas Layer(s)
            {
                this.clearRenderer(this.graphicsRenderer); 
                this.clearRenderer(this.selectRenderer); 

                ri.all = true;
                ri.state = "CROSS";
                // Drop into "CROSS"
            }
            case "CROSS":   // (1) Cross: Render the select cross hair
            {    
                this.clearRenderer(this.crossRenderer); 
                this.crossRenderer.render(this.crossScene, this.camera); 
                if (this.gridLayer.visible) this.drawGridLabels(this.crossRenderer);
                
                ri.state = (ri.all ? "DIAMOND" : "COMPLETE");
                if (!ri.all) break;
                // Drop into "DIAMOND"
            }  
            case "DIAMOND":  // (2) Diamond: Render the background diamonds 
            {
                this.diamondRenderer.render(this.diamondScene, this.camera);
                ri.state = (ri.all ? "GRAPHICS" : "COMPLETE");
                break;
            }        
            case "GRAPHICS":  // (3) Project Graphics
            {   
                // This converts camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Delayed rendering of Graphics Objects Takes Place in 'RENDER_GRAPHICS' State
                this.clearRenderer(this.bufferRenderer);
                this.clearRenderer(this.graphicsRenderer); 

                ri.count = this.bufferRenderer.render(this.graphicsScene, this.camera, true); 

                if (ri.count > 0)
                {
                    ri.RENDER_SIZE = 200;
                    ri.start = 0;
                    ri.end = (ri.count >= ri.RENDER_SIZE ? ri.RENDER_SIZE-1 : ri.count-1);
                }      
                // Drop into "RENDER_GRAPHICS"
            }
            case "RENDER_GRAPHICS":  // (4) Render Projected Graphics
            {      
                if (ri.count > 0)
                {   
                    this.bufferRenderer.render(this.graphicsScene, this.camera, false, ri.start, ri.end); 
                    ri.count -= ri.RENDER_SIZE;
                    ri.start += ri.RENDER_SIZE;
                    ri.RENDER_SIZE *= 5;              // Scale up graphics size to complete in fewer iterations.
                    ri.end += ri.RENDER_SIZE;
                }
                
                // Blast Offscreen Buffer to the Screen (Graphics Canvas)
                var ctx = this.graphicsRenderer.domElement.getContext('2d');
                ctx.drawImage(this.bufferRenderer.domElement, 0 , 0);
                                
                ri.state = (ri.count > 0 ? "RENDER_GRAPHICS" : (ri.all ? "SELECT" : "COMPLETE"));
                break;
            }
            case "SELECT":   // (5) Project and Render Select Graphics
            {         
                // This converts camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Delayed rendering of Graphics Objects Takes Place in 'RENDER_GRAPHICS' State
                this.clearRenderer(this.bufferRenderer);
                this.clearRenderer(this.selectRenderer); 

                ri.count = this.bufferRenderer.render(this.selectScene, this.camera, true); 

                if (ri.count > 0)
                {
                    ri.RENDER_SIZE = 200;
                    ri.start = 0;
                    ri.end = (ri.count >= ri.RENDER_SIZE ? ri.RENDER_SIZE-1 : ri.count-1);
                }
                // Drop into "RENDER_SELECT"
            }
            case "RENDER_SELECT":  // (6) Render Projected Graphics
            {      
                if (ri.count > 0)
                {   
                    this.bufferRenderer.render(this.selectScene, this.camera, false, ri.start, ri.end); 
                    ri.count -= ri.RENDER_SIZE;
                    ri.start += ri.RENDER_SIZE;
                    ri.RENDER_SIZE *= 5;              // Scale up graphics size to complete in fewer iterations.
                    ri.end += ri.RENDER_SIZE;
                }
                
                // Blast Offscreen Buffer to the Screen (Select Canvas)
                var ctx = this.selectRenderer.domElement.getContext('2d');
                ctx.drawImage(this.bufferRenderer.domElement, 0 , 0);
                                
                ri.state = (ri.count > 0 ? "RENDER_SELECT" :"COMPLETE");
                break;
            }
            case "COMPLETE":  // (7) Dump Render Stats
            {  
                if (ri && ri.log)
                {
                    ASTROVIEW.log(ri.log);
                    ri.log = "";
                    ri.logstate = "";
                }    
                
                // Reset Render All Flag
                ri.all = false;   
                
                // NOTE: We Pre-compute all the Scene Vertices by simulating an Object Selection.
                // This will dramatically improve object Selection Performance on Mobile Platforms
                this.controller.getSelected();  
         
                ri.state = "IDLE";
                // Drop into "IDLE"
            }
            case "IDLE":
            {
                // Check Render Queue: Pop next value off                 
                if (ri.queue.length > 0) ri.state = ri.queue.shift();
                break;
            } 
        }
        
        // Update Render Stats
        if (time)
        {
            // Update Render Stats
            var et = new Date().getTime() - time;
            if (state != ri.logstate) ri.log += " " + state + ":";
            ri.log += et + ",";
            ri.logstate = state;
        }
    }
    
    this.drawGridLabels = function(renderer)
    {
        var rax = this.getRaScale();
        var decx = this.controller.getDecScale();
             
        this.drawGridLabelsRa(renderer, rax);
        this.drawGridLabelsDec(renderer, decx);
    }
    
    this.getRaScale = function() 
    {
        var zindex = this.controller.getZoomIndex();
        var rotx = Math.abs(this.controller.camera.rotation.x) * TO_DEGREES;
        if (rotx > 70) zindex--;
        if (rotx > 80) zindex--;
        if (rotx > 85) zindex--;
        if (rotx > 86) zindex--;
        if (rotx > 87) zindex--;
        if (rotx > 88) zindex--;
        if (rotx > 89) zindex--;
        if (rotx > 89.6) zindex-=1;
        if (rotx > 89.8) zindex-=1;
        if (rotx > 89.9) zindex-=1;
        if (zindex < 0) zindex = 0;  // Sanity check
        
        var rax = this.controller.getRaScaleFromZoomIndex(zindex);
        return rax;
    }
    
    this.drawGridLabelsRa = function(renderer, degrees)
    {
        if (!degrees) degrees = 5;                  // If not specified, Draw Tick Mark every 5 degrees
        
        // Use Math.round() for whole number intervals    
        var round = (parseInt(degrees) == degrees); 
        
        // Determine value for toFixed() size used below
        var array = String(degrees).split(".");
        var fixed = (array.length == 2 ? array[1].length : 0);
        // Rotate the RA Labels all the time.  There are just too many cases where they clobber each other.
        var rotate = true;  
        
        var ctx = renderer.domElement.getContext('2d');
        var width = renderer.domElement.clientWidth;
        var height = renderer.domElement.clientHeight;
        var sx = renderer.domElement.width/width;
        var sy = renderer.domElement.height/height;
        
        // Calculate RA/DEC for every horizontal pixel value, draw tickmark if multiple of 10 
        var decreasing = true;  
        var coord = new ASTROVIEW.Coord(this.controller);
        coord.mod = 999; 
        var lastcoord;
        
        var lastx = 0;
        for (var x=-20; x<width+20; x++)
        {
            coord.screen.x = x;
            coord.screen.y = height;
            coord.updateAllCoords();
            coord.mod = Math.abs(coord.radec.ra%degrees);
            
            if (lastcoord)
            {                
                if (decreasing) // Downward slope toward tick mark
                {
                    if (coord.mod >= lastcoord.mod)  // Found the bottom: This is a tick mark !!!
                    {
                        decreasing = false;

                        // Only draw this tickmark and label if we have 30 pixels since last one
                        lastcoord.radec.ra -= lastcoord.mod;
                        var text = lastcoord.raToString(this.radecView.format, fixed, round);
                        this.drawLabelRa(ctx, lastcoord.screen.x, lastcoord.screen.y, text, sx, sy, rotate);
                        lastx = lastcoord.screen.x;
                    }
                }
                else if (coord.mod < lastcoord.mod) // Look for apex
                {
                    decreasing = true;
                }
            }
            else  // Save very first lastcoord
            {
                lastcoord = new ASTROVIEW.Coord(this.controller);
            }
            lastcoord.copy(coord);
            lastcoord.mod = coord.mod;
        }
    }
    
    this.drawGridLabelsDec = function(renderer, degrees)
    {
        if (!degrees) degrees = 5;                  // If not specified, Draw Tick Mark every 5 degrees
        // Use Math.round() for whole number intervals    
        var round = parseInt(degrees) == degrees;  
        // Determine toFixed() size used below
        var array = String(degrees).split(".");
        var fixed = (array.length == 2 ? array[1].length : 0);
        
        var ctx = renderer.domElement.getContext('2d');
        var width = renderer.domElement.clientWidth;
        var height = renderer.domElement.clientHeight;
        var sx = renderer.domElement.width/width;
        var sy = renderer.domElement.height/height;
        
        // Determine Pole Position
        var pole = new ASTROVIEW.Coord(this.controller); 
        pole.north = this.controller.rotation.x > 0;
        pole.normal.set(0, (pole.north ? 1.0 : -1.0), 0);
        pole.normalToScreen();
        
        // Set Pole Visible flag, used many times below
        pole.visible = (pole.screen.y >= 0 && pole.screen.y <= height)
        
        // Draw Pole Tick Mark, if visible in the viewport
        if (pole.visible)
        {
             var text = (pole.north ? "+90" : "-90");
             this.drawLabelDec(ctx, 0, pole.screen.y, text, sx, sy);
        }
        
        // Calculate RA/DEC for every horizontal pixel value, draw tickmark if multiple of 10 
        var decreasing = true;  
        var coord = new ASTROVIEW.Coord(this.controller);
        coord.mod = 999; 
        var lastcoord;
        
        for (var y=-20; y<height; y++)
        {
            coord.screen.x = 0;
            coord.screen.y = y;
            coord.updateAllCoords();
            coord.mod = Math.abs(coord.radec.dec%degrees);
            
            if (lastcoord)
            {                
                if (decreasing) // Downward slope toward tick mark
                {
                    if (coord.mod >= lastcoord.mod)  // Found the bottom: This is a tick mark !!!
                    {
                        decreasing = false;
                        
                        // Skip any Tick Marks above/below the Poles
                        var renderLine = true;
                        if (pole.visible)
                        {
                            if (pole.north)
                                renderLine = (lastcoord.screen.y > pole.screen.y+20);
                            else
                                renderLine = (lastcoord.screen.y < pole.screen.y-20);
                        }
                        
                        // Render the Tick Mark + Label
                        if (renderLine)
                        {
                            var text = lastcoord.decToString(this.radecView.format, fixed, round);
                            if (parseFloat(text) == 0) text = "0"; 
                            this.drawLabelDec(ctx, lastcoord.screen.x, lastcoord.screen.y, text, sx, sy);
                        }
                    }
                }
                else if (coord.mod < lastcoord.mod) // Look for apex
                {
                    decreasing = true;
                }
            }
            else  // Save very first lastcoord
            {
                lastcoord = new ASTROVIEW.Coord(this.controller);
            }
            lastcoord.copy(coord);
            lastcoord.mod = coord.mod;
        }
    }
    
    this.drawLabelRa = function(c, x, y, text, sx, sy, rotate)
    {
        c.save();
        c.scale(sx, sy);
        c.translate(x, y);
        
        c.lineWidth = .8;
        c.strokeStyle = '#1BFF00';
        c.fillStyle = '#1B9900';
        
        c.beginPath();
        c.moveTo(   0, -18);
        c.lineTo(   0,   0);
        c.closePath();
        
        c.translate(2, (rotate ? -18 : -8));
        c.rotate((rotate ? -0.6 : 0.0));
        c.font = '10pt Verdana';
        c.fillText(text, 0, 0);
        
        c.stroke();
        c.restore();
    }
       
    this.drawLabelDec = function(c, x, y, text, sx, sy)
    {
        c.save();
        c.scale(sx, sy);
        c.translate(x, y);

        c.lineWidth = .8;
        c.strokeStyle = '#1BFF00';
        c.fillStyle = '#1B9900';
        
        c.beginPath();
        c.moveTo(   0,  0);
        c.lineTo(   34, 0);
        c.closePath();
        
        c.font = '10pt Verdana';
        c.fillText(text, 4, 14);
        
        c.stroke();
        c.restore();
    }
    
    ////////////////////////
    // updateRenderInfo()
    ////////////////////////
    this.updateRenderInfo = function(ri)
    {
        var et = new Date().getTime() - ri.stats.time;
        ri.stats.log += ri.stats.state + ":" + et + ", ";
    }
    
    ////////////////////////
    // clearRenderer()
    ////////////////////////   
    this.clearRenderer = function (renderer)
    {
        var canvas = renderer.domElement;
        c = canvas.getContext("2d");    
        c.save();
        c.setTransform(1, 0, 0, 1, 0, 0);
        // NOTE: Leave this as Width/Height: clientWidth, height does not work on Mobile
        c.clearRect(0, 0, canvas.width, canvas.height);
        c.restore();        
    }
    
    ////////////////////////
    // refreshRenderer()
    ////////////////////////   
    this.refreshRenderer = function (renderer)
    {
        var image = renderer.domElement;
        var ctx = renderer.domElement.getContext('2d');
        if (ctx && image)
        {
            ctx.drawImage(image, 0, 0);
        }      
    }
    
    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
        // Add Key Listener for Debug Shortcuts (debug only)
        if (this.config.debug) document.addEventListener( 'keypress' , ASTROVIEW.bind(this, this.onKeyPress), false);
        
        // Add Events attached to <Canvas> Element
        this.canvas.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onMouseOut),  false );
        this.canvas.addEventListener( 'mouseover',  ASTROVIEW.bind(this, this.onMouseOver),  false );
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////               
    //
    // Events
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    this.mouseOver = true;
    this.onKeyPress = function( event )
    {        
        if (event.ctrlKey)
        {
            var key = this.getChar(event);
            if (key && ASTROVIEW.trim(key) !== "")
            {
                event.preventDefault();
                this.onKey(key, event);
            }
        }
    }
    
    this.getChar = function( event ) 
    {
        if (event.keyIdentifier) // Chrome
        {
            return String.fromCharCode(parseInt(event.keyIdentifier.replace("U+",""),16)); 
        } 
        else // The Rest
        {
            return String.fromCharCode(event.which);
        } 
    }
    
    this.onKey = function( key, event )
    {          
        switch(key.toUpperCase())
        {
            // Testing of Viewer
            case  '1':   this.onM101(); break;           // '1'
            case  'A':   this.onActiveRate(); break;     // 'A'
            case  'B':   this.onBoxLayer(); break;       // 'B'
            case  'C':   this.onCatalogLayer(); break;   // 'C'
            case  'D':   this.onDeleteLayer(); break;    // 'D'
            case  'F':   this.onFootprintLayer(); break; // 'F'
            case  'G':   this.onGrid(); break;           // 'G'
            case  'H':   this.onHelp(); break;           // 'H'
            case  'I':   this.onImageLayer(); break;     // 'I'
            case  'M':   this.onMove(); break;           // 'M'
            case  'R':   this.onRotate(); break;         // 'R'
            case  'S':   this.onStats(); break;          // 'S'
            case  'T':   this.onTestLayer(); break;      // 'T'
            case  'U':   this.onUrl(); break;            // 'U'
            case  'V':   this.onVisibile(); break;       // 'V'
            case  'Z':   this.onZ(event); break;         // 'Z'
        }
    }
    
    this.onMouseOut = function (event)
    {
        this.mouseOver = false;
    }
    
    this.onMouseOver = function (event)
    {
        this.mouseOver = true;
    }
    
    this.onKeyboardClick = function (event)
    {
        var key = prompt("Enter Key Value (H for Help):");
        this.onKey(key, event);
    }
    
    this.createRaDecControls = function()
    {
        // Create RA/DEC View, specifying mobile or desktop layout
        this.radecView = new ASTROVIEW.RaDecView(this);   
        this.radecView.div.style.zIndex = 10;
        this.config.div.appendChild( this.radecView.div ); 
    }
    
    this.createZoomControls = function()
    {
        // Create Zoom In Icon 
        this.zoomInIcon = new Image();
        this.zoomInIcon.id = 'imgZoomIn';
        this.zoomInIcon.src = '../../Clients/AstroView/plus.png';
        this.zoomInIcon.style.position = 'absolute';
        this.zoomInIcon.style.bottom = '40px';
        this.zoomInIcon.style.left = '-10px';
        this.zoomInIcon.style.zIndex = 10;
        this.zoomInIcon.addEventListener( 'mousedown',  ASTROVIEW.bind(this, this.onZoomInDown),  false );
        this.zoomInIcon.addEventListener( 'mouseup',  ASTROVIEW.bind(this, this.onZoomInStop),  false );
        this.zoomInIcon.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onZoomInStop),  false );
        
        this.zoomInIcon.title = "Zoom In";
        this.config.div.appendChild( this.zoomInIcon );
        
        // Create Zoom Out Icon 
        this.zoomOutIcon = new Image();
        this.zoomOutIcon.id = 'imgZoomOut';
        this.zoomOutIcon.src = '../../Clients/AstroView/minus.png';
        this.zoomOutIcon.style.position = 'absolute';
        this.zoomOutIcon.style.bottom = '0px';
        this.zoomOutIcon.style.left = '-10px';
        this.zoomOutIcon.style.zIndex = 10;
        this.zoomOutIcon.addEventListener( 'mousedown',  ASTROVIEW.bind(this, this.onZoomOutDown),  false );
        this.zoomOutIcon.addEventListener( 'mouseup',  ASTROVIEW.bind(this, this.onZoomOutStop),  false );
        this.zoomOutIcon.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onZoomOutStop),  false );

        this.zoomOutIcon.title = "Zoom Out";
        this.config.div.appendChild( this.zoomOutIcon );
    }
        
    this.onZoomInDown = function (event)
    {
        this.controller.zoomIn();
        this.zoomInTimer.start(ASTROVIEW.bind(this, this.onZoomInDown), 200); 
    }
    
    this.onZoomInStop = function (event)
    {
        this.zoomInTimer.stop();
    } 
     
    this.onZoomOutDown = function (event)
    {
        this.controller.zoomOut();
        this.zoomOutTimer.start(ASTROVIEW.bind(this, this.onZoomOutDown), 200); 
    }
      
    this.onZoomOutStop = function (event)
    {
        this.zoomOutTimer.stop(); 
    }
    
    this.createSettingsControls = function()
    {
        if (typeof jo === 'undefined') return;    // If jo library is not loaded return;

        // Create Settings Icon 
        this.settingsIcon = new Image();
        this.settingsIcon.id = 'imgSettings';
        this.settingsIcon.src = '../../Clients/AstroView/gear.png';
        this.settingsIcon.style.position = 'absolute';
        this.settingsIcon.style.bottom = '4px';
        this.settingsIcon.style.right = '10px';
        this.settingsIcon.style.zIndex = 10;
        this.settingsIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onSettingsClick),  false );
        this.settingsIcon.title = "Settings Menu";
        this.config.div.appendChild( this.settingsIcon );
        
        // Create Settings Properties
        var av = this;
        
        var surveyNames = this.getSurveyNames(); 

        this.settings = new joRecord({
            search: ' ',
            grid: false,
            radec: true,
            crosshair: true,
            survey: 0,
            surveyNames: surveyNames
        });
    
        // Create Settings Popup
        jo.load();
        this.screen = new joScreen();
        this.screen.setContainer(this.config.div.id);

        joCache.set("popup", function() {
        
            var input, radec, crosshair, grid, survey, close, go;
            var popup = [
                new joTitle("AstroView Settings"),
                new joGroup([
                    new joCaption("<b>Move To:</b>"),
                    new joFlexrow([
                         input = new joInput(av.settings.link("search")).setStyle("flex"),
                         go = new joButton("&nbsp Go &nbsp").setStyle("noflex"),
                    ]),
                    new joFlexrow([
                        new joLabel(" Grid Lines:").setStyle("left"),
                        grid = new joToggle(av.settings.link("grid")).setLabels(["Off", "On"])
                    ]),
                    new joFlexrow([
                        new joLabel(" [RA, DEC]:").setStyle("left"),
                        radec = new joToggle(av.settings.link("radec")).setLabels(["Off", "On"])
                    ]),
                    new joFlexrow([
                        new joLabel(" Crosshair:").setStyle("left"),
                        crosshair = new joToggle(av.settings.link("crosshair")).setLabels(["Off", "On"])
                    ]),
                    new joLabel("<b>Survey:</b>"),
                    new joFlexrow([
                        survey = new joOption(av.settings.getProperty("surveyNames"), av.settings.link("survey"))
                    ]),
                ]),
                close = new joButton("Close")
            ];
            
            // Wire Up Settings Callbacks
            joEvent.on(input.container, "keydown", function(e)
            {
                var evt = e || window.event;
                // "e" is the standard event object (FF, Chrome, Safari, Opera), "window.event" (IE)
                
                // If we got [CR] move to named position
                if ( evt.keyCode === 13 ) { 
                    go.focus();
                    var name = av.settings.getProperty("search");
                    if (name && ASTROVIEW.trim(name).length > 0)
                        av.moveToName(name);
                    input.focus();
                    return false;  // Disable form submission
                }
            }, this);
                     
            go.selectEvent.subscribe(function() 
            {
                go.focus();
                var name = av.settings.getProperty("search");
                if (name && ASTROVIEW.trim(name).length > 0)
                    av.moveToName(name);
                go.blur();
            });
            
            grid.changeEvent.subscribe(function() 
            {
                if (av.gridLayer.visible != av.settings.getProperty("grid"))
                {
                    av.gridLayer.visible = av.settings.getProperty("grid");
                    av.render("CROSS");
                }
            });
            
            radec.changeEvent.subscribe(function() 
            {
                av.radecView.div.style.display = (av.settings.getProperty("radec") ? 'inherit' : 'none');
            });
            
            crosshair.changeEvent.subscribe(function() 
            {
                if (av.crossLayer.visible != av.settings.getProperty("crosshair"))
                {
                    av.crossLayer.visible = av.settings.getProperty("crosshair");
                    av.render("CROSS");
                }
            });
                     
            survey.selectEvent.subscribe(function() 
            {
                var index = av.settings.getProperty("survey");  // Returns index into surveyNames array
                var surveyNames = av.settings.getProperty("surveyNames");
                if (surveyNames && surveyNames[index])
                {
                   av.loadSurveyName(surveyNames[index]);
                }
            });
            
            close.selectEvent.subscribe(function() 
            {
                av.screen.hidePopup();
            });
            
            return popup;
        });
        
        // Init the popup to current settings
        popup = joCache.get("popup");
    }
   
    this.onSettingsClick = function (event)
    {
        var popup = joCache.get("popup");
        this.screen.showPopup(popup);
    }
    
    this.moveToName = function (name)
    {
        ASTROVIEW.log("moveToName: " + name);
        var ename = encodeURIComponent(name);
        var url = ASTROVIEW.urls.name.mastresolver + ename;
        var av = this;
        var file = joFile(url, function(data, error) {
            if (error) 
            {
                alert("Error while trying to resolve name:" + name + 
                      "<br>Error: " + error + 
                      "<br>Url: " + url);
            }
            else
            {
                var xmlDoc = ASTROVIEW.fromXml(data);
                var radec = av.xmlDocToCoord(xmlDoc);

                // If valid coord, move to the location
                if (radec && radec.sra && radec.sdec)
                {
                    if (av.controller.getZoomLevel() < 10) radec.zoom = 10;
                    av.moveTo(radec);
                    av.screen.hidePopup();
                }
                else
                {
                    alert("Unable to resolve name : " + name +
                        "\n\nExamples:\n\n" +
                        "Names:\n" + 
                        "   M101, NGC45, Orion, Dubhe\n\n" +
                        "Catalogs:\n" + 
                        "   BD+19 706\n" +
                        "   png 000.8-07.6\n" +
                        "   2MASS J04215943+1932063\n" +
                        "   TYC 1272-470-1\n\n" +
                        "Coordinates:\n" + 
                        "   14 03 12.6 54 20 56.7\n" + 
                        "   14:03.210 54:20.945\n" + 
                        "   14h03m12.6s +54d20m56.7s\n" + 
                        "   g102.0373+59.7711\n" + 
                        "   180.468 -18.866\n");
                }
            }
        }); // jofile()
    }
 
    this.xmlDocToCoord = function(xmlDoc)
    {
        var radec;
        if (xmlDoc && xmlDoc.firstChild && xmlDoc.firstChild.localName == "resolvedItems")
        {   
            var resolvedItems = xmlDoc.firstChild;
            if (resolvedItems.firstChild && resolvedItems.firstChild.localName == "resolvedCoordinate")
            {
                var resolvedCoordinate = resolvedItems.firstChild;
                if (resolvedCoordinate.childNodes && resolvedCoordinate.childNodes.length > 0)
                {
                    for (var i=0; i<resolvedCoordinate.childNodes.length; i++)
                    {
                        var child = resolvedCoordinate.childNodes[i];
                        if (child.localName && child.localName == "ra")
                        {
                            if (!radec) radec = {}; 
                            radec.sra = child.textContent;
                            radec.ra = parseFloat(radec.sra); 
                            if (radec.sdec) break;
                        }
                        if (child.localName && child.localName == "dec")
                        {
                            if (!radec) radec = {}; 
                            radec.sdec = child.textContent;
                            radec.dec = parseFloat(radec.sdec); 
                            if (radec.sra) break;
                        }
                    }
                }
            }
        }
        return radec;
    }
    
    this.getSurveyNames = function()
    {
        var names = [];
        for (var id in ASTROVIEW.surveys) 
        {
            if (ASTROVIEW.surveys[id].visible)
            {
                names.push(ASTROVIEW.surveys[id].name);
            }
        }
        return names;
    }
    
    this.loadSurveyName = function(surveyName)
    {
        var found = false;
        for (var id in ASTROVIEW.surveys) 
        {
            if (ASTROVIEW.surveys[id].name == surveyName)
            {
                this.loadSurvey(ASTROVIEW.surveys[id]);
                found = true;
            }
        }
        
        if (!found)
        {
            alert("Unable to load Survey Name: " + surveyName + "\n\n" + 
                  "Verify that survey is defined and visible in Surveys.js");
        }
    }
    
    this.loadSurvey = function(survey)
    {
        ASTROVIEW.log("loadSurvey():" + survey.name);   
        this.clearScene();
        this.survey = survey;
        if (this.controller)
        {
            this.controller.loadSurvey(survey);
        }
        this.render("DIAMOND");
    }
    
    //////////////////////////
    // 'H' : onHelp() 
    //////////////////////////
    this.onHelp = function()
    {
         alert( "'1': M101\n" +
                "'A': ActiveRate\n" +
                "'B': BoxLayer\n" +
                "'C': CatalogLayer\n" +
                "'D': DeleteLayer\n" +
                "'F': FootprintLayer\n" +
                "'H': Help\n" +
                "'I': ImageLayer\n" +
                "'M': Move\n" +
                "'R': Rotate\n" +
                "'S': Stats\n" +
                "'T': TestLayer\n" +
                "'U': Url\n" +
                "'V': Visibile\n" +
                "'Z': (+/-) Z Axis [Shift]\n");
    }
    
    //////////////////////////
    // '1' : onM101() 
    //////////////////////////
    this.onM101 = function()
    {
         if (ASTROVIEW.M101)
         {
            var radec={"ra":210.8023, "dec":54.3490, "zoom":10};
            this.moveTo(radec);
            var layerData = ASTROVIEW.M101;
            layerNames.push (this.createLayer(layerData));
         }
    }
    
    //////////////////////////
    // 'G' : onGrid() 
    //////////////////////////
    this.onGrid = function()
    {
         this.showGrid();
    }
    
    //////////////////////////
    // 'A' : onActiveRate() 
    //////////////////////////
    this.onActiveRate = function()
    {
         var active = prompt("Enter Active Rate in MS:");
         ASTROVIEW.TIMER_TICKS_ACTIVE = active;
    }
    
    //////////////////////////
    // 'U' : onUrl() 
    //////////////////////////
    this.onUrl = function()
    {
        this.render("DIAMOND");
    }
    
    //////////////////////////
    // 'V' : onVisibile() 
    //////////////////////////
    this.onVisibile = function()
    {
        this.toastSphere.visible = !this.toastSphere.visible;
        this.healpixSphere.visible = !this.healpixSphere.visible;
        this.render("DIAMOND");
    }    
    
    //////////////////////////
    // 'M' : onMove() 
    //////////////////////////
    var radecs = [{"ra":0, "dec":90},
                  {"ra":165.93196467,   "dec":61.75103469,  "zoom":10}, 
                  {"ra":84.05338894,    "dec":-1.20191914,  "zoom":10},      
                  {"ra":148.8882,       "dec":69.0653,      "zoom":10},       
                  {"ra":23.4620,        "dec":30.6602,      "zoom":10},
                  {"ra":210.8023,       "dec":54.3490,      "zoom":10},
                  {"ra":202.4842,       "dec":47.2306,      "zoom":10},
                  {"ra":0,              "dec":0,            "zoom":10}];
    var x=0;
    this.onMove = function()
    {
        this.moveTo(radecs[x]);
        x = (x+1) % radecs.length;
    }
    
    //////////////////////////
    // 'Z' : onZ() 
    //////////////////////////
    this.onZ = function(event)
    {
        this.camera.position.z += (event.shiftKey ? -100 : 100);
        this.render();
    }
    
    //////////////////////////
    // 'S' : onStats() 
    //////////////////////////
    this.onStats = function()
    {
        this.controller.stats();
    }
    
    //////////////////////////
    // 'R' : onRotate() 
    //////////////////////////
    this.onRotate = function()
    {
        //var sphere = (this.healpixSphere.visibile ? this.healpixSphere : this.toastSphere);
        this.healpixSphere.rotation.y += (10.0 * TO_RADIANS);
        this.healpixSphere.updateMatrix();
        this.render("CAMERA");
    }
    
    this.onReadLayer = function()
    {
        for (var lid=0; lid<ASTROVIEW.lid; lid++)
        {
            var layerData = this.readLayer(lid);
            if (layerData)
            {
                alert("layerData [" + layerData.lid + "]:\n" + ASTROVIEW.toJson(layerData));
            }
        }
    }
    
    //////////////////////////
    // 'D' : onDeletLayer() 
    //////////////////////////
    this.onDeleteLayer = function()
    {
        for (var i in layerNames)
        {   
            var name = layerNames[i];
            this.deleteLayer(name);
        }
        layerNames = [];
    }
      
    var smallRedBox = "Polygon J2000 1.0 -1.0 1.0 1.0 359.0 1.0 359.0 -1.0";
    var redBox = "Polygon J2000 5.0 -5.0 5.0 5.0 355.0 5.0 355.0 -5.0";
    
    var crossPoints = "Position ICRS 0 0";   
    var yellowPoints = "Position ICRS 5.0 -5.0 Position ICRS 5.0 5.0 Position ICRS 355.0 5.0 Postion ICRS 355.0 -5.0  ";   
    var redPoints = "Position ICRS 6.0 -6.0 Position ICRS 6.0 6.0 Position ICRS 354.0 6.0 Postion ICRS 354.0 -6.0  "; 
    var greenPoints = "Position ICRS 7.0 -7.0 Position ICRS 7.0 7.0 Position ICRS 353.0 7.0 Postion ICRS 353.0 -7.0  ";  
    var bluePoints = "Position ICRS 8.0 -8.0 Position ICRS 8.0 8.0 Position ICRS 352.0 8.0 Postion ICRS 352.0 -8.0  ";  
    var cyanPoints = "Position ICRS 9.0 -9.0 Position ICRS 9.0 9.0 Position ICRS 351.0 9.0 Postion ICRS 351.0 -9.0  ";  

    var acsM101 = "Polygon J2000 210.75890230 54.38019650 210.79889830 54.32921760 210.84012320 54.34291740 210.80016510 54.39391000      Polygon J2000  210.79858090 54.32875470 210.75693070 54.38001520 210.71409030 54.36600310 210.75577980 54.31475730";
    var greenPlus = "Polygon J2000 1.0 -0.0 359.0 0.0 Polygon J2000 0.0 1.0 0.0 -1.0";
    
    //var galexM101 = "POLYGON ICRS 211.43464545 54.358924 211.43122163 54.42425429 211.4209877 54.48886881 211.40405577 54.55205962 211.38061136 54.6131344 211.35091133 54.671424 211.31528107 54.72628978 211.27411097 54.77713063 211.22785208 54.82338952 211.17701123 54.86455962 211.12214545 54.90018988 211.06385585 54.92988991 211.00278107 54.95333432 210.93959026 54.97026625 210.87497574 54.98050018 210.80964545 54.983924 210.74431516 54.98050018 210.67970064 54.97026625 210.61650983 54.95333432 210.55543505 54.92988991 210.49714545 54.90018988 210.44227967 54.86455962 210.39143882 54.82338952 210.34517993 54.77713063 210.30400983 54.72628978 210.26837957 54.671424 210.23867954 54.6131344 210.21523513 54.55205962 210.1983032 54.48886881 210.18806927 54.42425429 210.18464545 54.358924 210.18806927 54.29359371 210.1983032 54.22897919 210.21523513 54.16578838 210.23867954 54.1047136 210.26837957 54.046424 210.30400983 53.99155822 210.34517993 53.94071737 210.39143882 53.89445848 210.44227967 53.85328838 210.49714545 53.81765812 210.55543505 53.78795809 210.61650983 53.76451368 210.67970064 53.74758175 210.74431516 53.73734782 210.80964545 53.733924 210.87497574 53.73734782 210.93959026 53.74758175 211.00278107 53.76451368 211.06385585 53.78795809 211.12214545 53.81765812 211.17701123 53.85328838 211.22785208 53.89445848 211.27411097 53.94071737 211.31528107 53.99155822 211.35091133 54.046424 211.38061136 54.1047136 211.40405577 54.16578838 211.4209877 54.22897919 211.43122163 54.29359371 211.43464545 54.358924";
    var galexM101 = "CIRCLE ICRS 210.80964545 54.35892400 0.625";
    var galexNGC5474 = "CIRCLE ICRS 211.25948613 53.67449567 0.625";
    var circle00 = "CIRCLE ICRS 0 0 1.0";
    var circle045 = "CIRCLE ICRS 0 45 1.0";
    var circle080 = "CIRCLE ICRS 0 80 1.0";
    
    ////////////////////////////
    // 'B' : onBoxLayer() 
    ////////////////////////////
    this.onBoxLayer = function()
    {       
        var rows = {"footprint":smallRedBox};
        var attribs = {"color":"0xff0000"};
        var name = "smallRedBox";
        var layerData = {"name":name, "type":"footprint", "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
    }
         
    ///////////////////////////////////
    // createGridLayer() 
    //////////////////////////////////
    this.createGridLayer = function()
    {   
        var layer = this.getLayer("GridLayer");
        if (!layer)
        {
            var layerData = {
               "name":"GridLayer",
               "type":"footprint",
               "canvas":"crossCanvas",
               "attribs":{"color":"1BEE00","stroke":1,"symbol":"square"},
               "rows":[]
            }
            
            // Create the Horizontal Lines for Right Ascension
            for (var ra = 0; ra<=170; ra+=10)
            {
                var row = {"footprint": "POLYGON ICRS "};
                var rac = ra+180;
                for (var dec = -90; dec<=90; dec+=10)
                {
                    row.footprint += " " + ra + " " + dec;
                }
                for (var dec = 80; dec>=-90; dec-=10)
                {
                    row.footprint += " " + rac + " " + dec;
                }
                layerData.rows.push(row);
            }
            
            // Create the Vertical Lines for Declination
            for (var dec = -80; dec<=80; dec+=10)
            {
                var row = {"footprint": "POLYGON ICRS "};
                for (var ra = 0; ra<=360; ra+=10)
                {
                    row.footprint += " " + ra + " " + dec;
                }
                layerData.rows.push(row);
            }
            this.createLayer(layerData);
            layer = this.getLayer("GridLayer");
        }
        return layer;
    }
    
    ///////////////////////////////////
    // createSpinLayer() 
    //////////////////////////////////
    this.createSpinLayer = function(name, symbol)
    {      
        var layer = this.getLayer(name);
        if (!layer)
        { 
            rows = [{"footprint": "Position ICRS 0 90" }, {"footprint": "Position ICRS 0 -90"}];
            attribs = {"color":"0xFFFF00", "symbol":symbol, "size":100};
            layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":rows, "canvas":"crossCanvas"};
            this.createLayer(layerData);
            layer = this.getLayer(name);
        }
        return layer;
    }
    
    ///////////////////////////////////
    // createCrossLayer() 
    //////////////////////////////////
    this.createCrossLayer = function()
    {      
        var layer = this.getLayer("CrossLayer");
        if (!layer)
        { 
            rows = [{"footprint": "Position ICRS 0 0" }];
            attribs = {"color":"0xff00ff", "symbol":"cross", "size":100};
            name = "CrossLayer";
            layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":rows, "canvas":"crossCanvas"};
            this.createLayer(layerData);
            layer = this.getLayer(name);
        }
        return layer;
    }
      
    ///////////////////////////////////
    // showGrid() 
    //////////////////////////////////
    this.showGrid = function(visible)
    {      
        if (this.gridLayer)
        {
            this.gridLayer.visible = (visible ? visbile : !this.gridLayer.visible);
            this.render("CROSS");
        }
    }
    
    //////////////////////////
    // 'T' : onTestLayer() 
    //////////////////////////
    this.onTestLayer = function()
    {        
        rows = {"footprint":redBox};
        attribs = {"color":"0xffff00"};
        name = "yellowBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows], "canvas":"selectCanvas"};
        layerNames.push (this.createLayer(layerData));
    }
    
    ////////////////////////////
    // 'C' : onCatalogLayer() 
    ////////////////////////////
    this.onCatalogLayer = function()
    { 
        rows = {"footprint":yellowPoints};
        attribs = {"color":"0xffff00", "symbol":"square"};
        name = "yellowPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":redPoints};
        attribs = {"color":"0xff0000", "symbol":"stop"};
        name = "redPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":greenPoints};
        attribs = {"color":"0x00ff00", "symbol":"diamond"};
        name = "greenPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":bluePoints};
        attribs = {"color":"0x0000ff", "symbol":"circle"};
        name = "bluePoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));     
        
        rows = {"footprint":cyanPoints};
        attribs = {"color":"0x00ffff", "symbol":"plus"};
        name = "cyanPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
    }
    
    var layerNames = [];
    ////////////////////////////
    // 'F' : onFootprintLayer() 
    ////////////////////////////
    this.onFootprintLayer = function()
    {       
        rows = {"footprint":greenPlus};
        attribs = {"color":"0x00ff00"};
        name = "greenPlus";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));     
        
        rows = {"footprint":redBox};
        attribs = {"color":"0xff0000"};
        name = "redBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle00};
        attribs = {"color":"0x00ff00"};
        name = "circle00";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle045};
        attribs = {"color":"0x0000ff"};
        name = "circle045";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle080};
        attribs = {"color":"0x00ffff"};
        name = "circle080";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        var rows = {"footprint":acsM101};
        var attribs = {"color":"0xff0000"};
        var name = "acsM101";
        var layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":galexNGC5474};
        attribs = {"color":"0x5555ff"};
        name = "galexNGC5474";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
    }
    
    //////////////////////////
    // 'I' : onImageLayer() 
    //////////////////////////
    this.onImageLayer = function()
    {
        var rows = {"url":"http://archive.stsci.edu/pub/stpr/hs-2009-29-b-large-jpg.jpg", 
                    "ul":"204.29002 -29.83628",
                    "ur":"204.24048 -29.84262",
                    "ll":"204.29741 -29.87950",
                    "lr":"204.24784 -29.88586"};
        var attribs = {"color":"0x00ff00"};
        var layerData = {"type":"image", "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////               
    //
    // AsroView API
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
        
    //////////////////////////
    // init() 
    //////////////////////////
    this.init = function(avcontainer)
    {
        if (typeof avcontainer != 'undefined')  // check container passed in
        {
            this.config.avcontainer = avcontainer;
            ASTROVIEW.log("init() Complete: using avcontainer passed in."); 
        }
        else if (typeof AstroViewContainer != 'undefined')  // check for global container
        {
            this.config.avcontainer = AstroViewContainer;
            ASTROVIEW.log("init() Complete: using AstroViewContainer.");
        }
        else
        {
            ASTROVIEW.log("init() Failed: avcontainer is undefined.");
        }
    }
    
    //////////////////////////
    // createLayer() 
    //////////////////////////
    this.createLayer = function(layerData)
    {
        var layer = null;
        if (layerData && layerData.type && ASTROVIEW.isString(layerData.type))
        {
            var type = ASTROVIEW.trim(layerData.type.toLowerCase());
            switch (type)
            {
                case "footprint":
                {
                    layer = new ASTROVIEW.FootprintLayer( this, layerData );
                    break;
                }
                case "catalog":
                {
                    layer = new ASTROVIEW.CatalogLayer( this, layerData );
                    break;
                }
                case "image":
                {
                    console.error("AstroBasicView:createImageLayer() not implemented yet");
                    //sphere = createImageLayer(layerData);
                    break;
                }
            }
        }
        
        // Add new AstroView layer to the Scene
        if (layer)
        {
            if (layer.layerData.canvas && layer.layerData.canvas.toLowerCase() == "selectcanvas")
            {
                this.selectScene.add(layer);
                this.render("SELECT");
            }
            else if (layer.layerData.canvas && layer.layerData.canvas.toLowerCase() == "crosscanvas")
            {
                this.crossScene.add(layer);
                this.render("CROSS");
            }
            else
            {
                this.graphicsScene.add(layer);
                this.render("GRAPHICS");
            }
        }
        
        // Return the Layer ID
        ASTROVIEW.log("createLayer() " + (layer ? layer.name : "?"));
        return (layer ? layer.name : "");
    }
    
    //////////////////////////
    // hasLayer() 
    //////////////////////////
    this.hasLayer = function (name)
    {
        var layer = this.getLayer(name);
        ASTROVIEW.log("hasLayer() " + name + " : " + (layer != null));
        return (layer != null);
    }
             
    //////////////////////////
    // readLayer() 
    //////////////////////////
    this.readLayer = function(name)
    {
        ASTROVIEW.log("readLayer() " + name);
        var layer = this.getLayer(name);
        return (layer ? layer.layerData : "");
    }
    
    //////////////////////////
    // getLayer() 
    //////////////////////////
    this.getLayer = function(name)
    {
        ASTROVIEW.log("getLayer() " + name);
        var layer = this.graphicsScene.getChildByName(name, true) ||
                    this.selectScene.getChildByName(name, true) ||
                    this.crossScene.getChildByName(name, true);
        return (layer);
    }
    
    //////////////////////////
    // udpateLayer() 
    //////////////////////////
    this.udpateLayer = function(name, layerData)
    {
        return (true);
    }
    
    //////////////////////////
    // deleteLayer() 
    //////////////////////////
    this.deleteLayer = function(name)
    {
        // Try to remove from Graphics Scene First
        var layer = this.graphicsScene.getChildByName(name, true);
        if (layer) 
        {
            layer.clean();
            this.graphicsScene.remove(layer, true);
            this.render("GRAPHICS");
        }

        // Try to remove from Select Scene Second
        if (!layer)
        {
            layer = this.selectScene.getChildByName(name, true);
            if (layer)
            {
                layer.clean();
                this.selectScene.remove(layer, true);
                this.render("SELECT");
            }
        }
        
        // Try to remove from Cross Scene Third
        if (!layer)
        {
            layer = this.crossScene.getChildByName(name, true);
            if (layer)
            {
                layer.clean();
                this.crossScene.remove(layer, true);
                this.render("CROSS");
            }
        }

        ASTROVIEW.log("deleteLayer() " + name + " : " + (layer != null));
        return (layer ? layer.name : "");
    }
    
    //////////////////////////
    // moveTo() 
    //////////////////////////
    this.moveTo = function(radec)
    {
        this.controller.moveTo(radec);   
        this.render("CAMERA");
    }
    
    //////////////////////////
    // sendEvent() 
    //////////////////////////
    this.sendEvent = function (type, objects)
    {
        if (this.config.avcontainer && this.config.avcontainer.onAstroViewEvent)
        {
            var msg = {"type" : type, "objects" : objects};
            this.config.avcontainer.onAstroViewEvent(msg);
        }
        else
        {
            ASTROVIEW.log ("sendAstroViewEvent() Not Sending Event: " + type + ". Missing onAstroViewEvent() callback.");
        }
    }
            
    ///////////////
    // Main
    ///////////////
    console.log("AstroView is a Go!" + (this.config.debug ? " [debug]" : " [no debug]"));
    this.loadSurveyName(this.config.surveytype);

    // Create Controls
    this.createRaDecControls();
    this.createSettingsControls();
    if (!ASTROVIEW.MOBILE) this.createZoomControls();
        
    // Create Scene 
    this.createScene();
    this.addEvents();
    this.renderScene();
    this.moveTo({"ra": 0, "dec": 0});
    this.sendEvent("AstroView.Initialization.Complete");
};

/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( av )
{
    // Save the objects we need from the AstroBasicView
    this.av = av;
    
    // These 3 MUST be specified for all ASTROVIEW.Coord() instances which 
    // relies on this triplet to perform coordinate transformations.
    this.canvas = av.canvas;
    this.camera = av.camera;
    this.projector = new THREE.Projector();
    
    this.graphicsScene = av.graphicsScene;
    this.selectScene = av.selectScene;    
    this.renderers = av.renderers;
     
    this.radecView = av.radecView;
    this.crossLayer = av.crossLayer;
    this.spinLayerClock = av.spinLayerClock;
    this.spinLayerCounter = av.spinLayerCounter;

    // Active Survey Params (see loadSurvey() below)
    this.survey = null;
    this.fovmax = null; 
    this.fovmin = null; 
    this.zlevel = null; 
    this.zindex = null;
 
    // Camera mouse
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;  
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.frustumMatrix = new THREE.Matrix4();
    
    // Mouse Events
    this.delta = new THREE.Vector2();
 
    // Current, Start, Last Mouse Position 
    this.mouse = new ASTROVIEW.Coord(this);;
    this.mouseLast = new ASTROVIEW.Coord(this);;          
    this.mouseStart = new ASTROVIEW.Coord(this);;  
    
    // Some properties on the mouse we use below
    this.mouse.down = false;
    this.mouse.over = false;
    this.mouse.spin = false;
    this.mouse.scale = 1.0; 

    // Scale Start/End Point
    this.scaleStart = new ASTROVIEW.Coord(this);;    
    this.scaleEnd = new ASTROVIEW.Coord(this);;     
    
    // North/South Pole Point
    this.pole = new ASTROVIEW.Coord(this);;          
    
    this.A = new THREE.Vector3();
    this.B = new THREE.Vector3();
    this.C = new THREE.Vector3();
    
    this.raXpixelScale = 0.0;
    this.decYpixelScale = 0.0;
    
    this.rotation = new THREE.Vector3();
    
    this.timer = new ASTROVIEW.Timer();

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {        
        // Mouse Events
        this.canvas.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onMouseOut),   false );
        this.canvas.addEventListener( 'mouseover', ASTROVIEW.bind(this, this.onMouseOver),  false );
        this.canvas.addEventListener( 'mousemove', ASTROVIEW.bind(this, this.onMouseMove),  false );
        this.canvas.addEventListener( 'mousedown', ASTROVIEW.bind(this, this.onMouseDown),  false );
        this.canvas.addEventListener( 'mouseup',   ASTROVIEW.bind(this, this.onMouseUp),    false );
        
        // Touch Events
        this.canvas.addEventListener( 'touchstart', ASTROVIEW.bind(this, this.onTouchStart), false );
        this.canvas.addEventListener( 'touchmove',  ASTROVIEW.bind(this, this.onTouchMove),  false );
        this.canvas.addEventListener( 'touchend',   ASTROVIEW.bind(this, this.onTouchEnd),   false );
        
        // Gesture Events
        this.canvas.addEventListener( 'gesturestart', ASTROVIEW.bind(this, this.onGestureStart), false );
        this.canvas.addEventListener( 'gesturechange', ASTROVIEW.bind(this, this.onGestureChange), false );
        this.canvas.addEventListener( 'gestureend', ASTROVIEW.bind(this, this.onGestureEnd), false );
        
        // Mouse Wheel Events: Need both for WebKit and FF
        this.canvas.addEventListener('DOMMouseScroll', ASTROVIEW.bind(this, this.onMouseWheel), false);
        this.canvas.addEventListener('mousewheel',     ASTROVIEW.bind(this, this.onMouseWheel), false);
        
        // Keypress events
        document.addEventListener( "keypress" , ASTROVIEW.bind(this, this.onKeyPress), false);
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
        this.timer.stop();
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
        this.timer.stop();
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
        this.timer.stop();
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
        
        // Clear previous Hover Graphics
        this.clearCanvas();
        
        // Update all the coordinates on the mouse    
        this.mouse.updateAllCoords(event);
        
        // Check if double click
        var double = false;
        if (this.timer.isActive())
        {
            var delta = (ASTROVIEW.MOBILE ? 20 : 5);
            double = (Math.abs(this.mouse.screen.x - this.mouseLast.screen.x) <= delta &&
                      Math.abs(this.mouse.screen.y - this.mouseLast.screen.y) <= delta);
            this.timer.stop();   
        }      
         
        if (double)
        {
            this.mouseDownDouble (event, params);
        }
        else
        {
            this.mouseDownSingle (event, params);
        }
        
        // Start new timer, (the old one is disabled if still active)
        this.timer.start(ASTROVIEW.bind(this, this.onTimer), (ASTROVIEW.MOBILE ? 200 : 250));
   }
        
   this.mouseDownSingle = function (event, params)
   {  
        // Save the start and last mouse coords (used by mouseMove() and mouseUp())
        this.mouse.down=true;
        this.mouseLast.copy(this.mouse);
        this.mouseStart.copy(this.mouse);   
        
        // Update Xpixel scale and Ypixel scale (Used by panScene() below)
        this.updatePixelScale();    

        // Save this current camera Rotation, (Used by panScene() below)
        this.rotation.copy(this.camera.rotation);
    }
    
    this.mouseDownDouble = function (event, params)
    {        
        this.moveTo(this.mouse.radec);
        this.zoomIn();
    }
    
    this.onTimer = function()
    {
        this.timer.stop();
    }
    
    /////////////////////////
    // mouseMove()
    /////////////////////////
    this.mouseMove = function(event, params)
    {
        event.preventDefault();
           
        // Clear previous Hover Graphics
        this.clearCanvas();
        
        // Update all the coordinates on the mouse     
        this.mouse.updateAllCoords(event);
 
        // Update Live RA/DEC View
        this.radecView.updateLive(this.mouse);
        
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
        
        // Only process a 'mouseUp' Event if we received the corresponding 'mouseDown' Event
        if (this.mouse.down)
        {            
            this.mouse.down=false; 
            this.mouse.spin=false;
            
            // Clear Spin Layer Graphics
            this.spinLayerClock.visible = false;
            this.spinLayerCounter.visible = false;
            this.av.render("CROSS");
            
            // Stop Rotation if User clicked single point
            if (this.mouseLast.screen.x == this.mouseStart.screen.x &&
                this.mouseLast.screen.y == this.mouseStart.screen.y)
            {
                // Stop any spinning graphicsScene rotation
                this.setCameraRotationDeltaXY(0, 0); 
                
                // Move the graphicsScene Select Point ('+') 
                this.moveCrossPoint(this.mouse);
             
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
                //ASTROVIEW.log("SELECT STATS: " + ASTROVIEW.toJson(this.selectStats));
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
            this.scaleStart.screen.x = Math.round( this.canvas.clientWidth/2.0 );
            this.scaleStart.screen.y = Math.round( this.canvas.clientHeight - this.canvas.clientHeight/4.0 );
            this.scaleStart.updateAllCoords();
            
            this.scaleEnd.screen.x = this.scaleStart.screen.x + Math.round( this.canvas.clientHeight/4.0 );
            this.scaleEnd.screen.y = this.scaleStart.screen.y + Math.round( this.canvas.clientHeight/4.0 );
            this.scaleEnd.updateAllCoords(); 
        }
        else
        {
            this.scaleStart.screen.x = Math.round( this.canvas.clientWidth/2.0 );
            this.scaleStart.screen.y = Math.round( this.canvas.clientHeight/4.0 );
            this.scaleStart.updateAllCoords(); 
            
            this.scaleEnd.screen.x = this.scaleStart.screen.x + Math.round( this.canvas.clientHeight/4.0 );
            this.scaleEnd.screen.y = 1;
            this.scaleEnd.updateAllCoords();
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
        // Determine Pole Location in Screen Coords
        this.pole.normal.set(0, (this.rotation.x > 0 ? 1.0 : -1.0), 0);
        this.pole.normalToScreen();
         
        // Determine Delta (Current - Start) in Screen and Astro Coords   
        var deltaY = this.mouse.screen.y - this.mouseStart.screen.y;
        var deltaX = this.mouse.screen.x - this.mouseStart.screen.x;
        
        var deltaDec = deltaY * this.decYpixelScale;
        var deltaRa = deltaX * this.raXpixelScale;
         
        //       
        // Check if we should Start 'Spin Mode'
        //
        if (!this.mouse.spin)
        {
            // Check (1): Rotation exceeds either the North Pole
            // Check (2): Rotation exceeds either the South Pole
            // Check (3): Pole point is inside screen viewport and mouse moved horizontally dominant
            if ((this.rotation.x + deltaDec) > RADIANS_90)
            {
                this.cameraRotationX = RADIANS_90;
                this.mouse.spin = true;
            }
            else if ((this.rotation.x + deltaDec) < -RADIANS_90)
            {
                this.cameraRotationX = -RADIANS_90;
                this.mouse.spin = true;
            }    
            else if ( this.pole.screen.x >= 0 && this.pole.screen.x <= this.canvas.clientWidth &&
                      this.pole.screen.y >= 0 && this.pole.screen.y <= this.canvas.clientHeight && 
                      Math.abs(deltaX)/Math.abs(deltaY) > 2.0 && Math.abs(deltaX) > 40)
            {
                this.mouse.spin = true;
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////
        //
        //  SPIN MODE: Used at the poles to spin the scene around the pole.
        //
        //  NOTE: Using Law of Cosines
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
        
        //
        // If 'Spin Mode' rotate the camera by the angle created by the mouse to the pole (see: Law of Cosines Note Above)
        //
        if (this.mouse.spin)
        {
            this.A.set(this.mouseLast.screen.x, this.mouseLast.screen.y, 0);
            this.B.set(this.mouse.screen.x, this.mouse.screen.y, 0);
            this.C.set(this.pole.screen.x, this.pole.screen.y, 0);
            
            var a = this.C.distanceTo(this.B);
            var b = this.C.distanceTo(this.A);
            var c = this.A.distanceTo(this.B);
                            
            var x = (a*a + b*b - c*c)/(2*a*b);
            
            if (ASTROVIEW.isNumber(x) && x <= 1.0)
            {
                // rotation angle 
                var r = Math.acos(x);
                                 
                // Now determine Clockwise or Counterwise movement by the User Mouse
                this.mouse.clockwise = this.isClock(this.pole.screen, this.mouseLast.screen, this.mouse.screen);
                if (this.mouse.clockwise) r = -r;   // Clockwise movement flips spin direction
                if (this.rotation.x < 0) r = -r;    // Sout Pole spins opposite direction as nNrth
                this.cameraRotationY += r;
            }      
        }
        else // Not 'Spin Mode': rotate the sphere as normal
        {   
            // Update Camera Rotation on Y-axis in 3D Space
            this.cameraRotationY = this.rotation.y + deltaRa;
            this.cameraRotationX = this.rotation.x + deltaDec;
        }
        
        // Hide/Show Clockwise/Counter Spin Arrow based on Spin Mode and Direction
        this.spinLayerClock.visible = (this.mouse.spin && this.mouse.clockwise); 
        this.spinLayerCounter.visible = (this.mouse.spin && !this.mouse.clockwise);
            
        // Save Last Mouse Position for next event processing
        this.mouseLast.copy(this.mouse);
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
                            context.moveTo(v.coord.screen.x, v.coord.screen.y);
                        else
                            context.lineTo(v.coord.screen.x, v.coord.screen.y);
                    }
                       
                    // Done! Now fill the shape, and draw the stroke.
                    // Note: your shape will not be visible until you call any of the two methods.
                    context.closePath(); 
                    context.stroke();                        
                }
                else if (object instanceof THREE.Particle)
                {                           
                    // Draw Particle Graphic (move to first point)
                    var screen = object.position.coord.screen;                          
                    context.beginPath();
                    context.strokeRect( screen.x-4, screen.y-4, 8, 8);
                    context.closePath();
                    context.stroke();
                }
                else
                {
                    console.error("CameraController: object Type is Unknown = " + object);
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
    
    this.color2hex = function (color) 
    {
        return this.rgb2hex(color.r*255, color.g*255, color.b*255);
    }
    
    this.rgb2hex = function (r, g, b) 
    {
        return Number(0x1000000 + r*0x10000 + g*0x100 + b).toString(16).substring(1);
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
                case 105:   this.zoomIn( ); break;
                case 111:   this.zoomOut( ); break;
            }
        }
    }
    
    /////////////////////////
    // stats()
    /////////////////////////  
    this.stats = function()
    {
       ASTROVIEW.log("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
                     " [ra,dec: " + this.mouse.radec.dra + "," + this.mouse.radec.ddec + "]" +
                     " [sra,sdec: " + this.mouse.radec.sra + "," + this.mouse.radec.sdec + "]" +
                     " [rot x: " + this.camera.rotation.x.toFixed(3) + " rot y:" + this.camera.rotation.y.toFixed(3) + "]" +
                     " [fov: " + this.camera.fov + "]" +
                     " [get: " + this.zlevel + "]" );
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
        if (!zoomSpeed) zoomSpeed = ASTROVIEW.CameraController.MouseParams.zoomInClick;
        var deltaFov = this.cameraFov * zoomSpeed;
        this.cameraFov -= deltaFov;
        if (this.cameraFov < this.fovmin) this.cameraFov = this.fovmin;
    }
    
    this.zoomOut = function(zoomSpeed)
    {
        this.clearCanvas();
        if (!zoomSpeed) zoomSpeed = ASTROVIEW.CameraController.MouseParams.zoomOutClick;
        var deltaFov = this.cameraFov * zoomSpeed;
        this.cameraFov += deltaFov;
        if (this.cameraFov > this.fovmax) this.cameraFov = this.fovmax;
    }
     
    this.moveTo = function(radec)
    {   
        this.clearCanvas();
          
        // Check all mixed case variations for RA and DEC and Zoom Properties
        if (radec.ra == undefined) radec.ra = (radec.RA != undefined ? radec.RA : (radec.Ra != undefined ? radec.Ra : undefined));
        if (radec.dec == undefined) radec.dec = (radec.DEC != undefined ? radec.DEC : (radec.Dec != undefined ? radec.Dec : undefined));
        if (radec.zoom == undefined) radec.zoom = (radec.ZOOM != undefined ? radec.ZOOM : (radec.Zoom != undefined ? radec.Zoom : undefined));
        
        // Convert RA/DEC object into a legitmate Coord Object
        var coord = new ASTROVIEW.Coord(this.controller);
        coord.radec.ra = radec.ra;
        coord.radec.dec = radec.dec;
        coord.radec.zoom = radec.zoom;
        
        // point camera to specified ra, dec
        this.camera.rotation.y = this.cameraRotationY = radec.ra * TO_RADIANS;
        this.camera.rotation.x = this.cameraRotationX = radec.dec * TO_RADIANS;
     
        // Set the Zoom Level
        if (coord.radec.zoom) this.setZoomLevel(coord.radec.zoom);
        
        // Move the graphicsScene Cross Point ('+')
        this.moveCrossPoint(coord); 
    }
 
    /////////////////////////
    // setZoomLevel()
    /////////////////////////
    this.setZoomLevel = function(zlevel)
    {        
        // Look up the Camera Fov based on zlevel
        for (var i=0; i<this.survey.zoomTable.length-1; i++)
        {
            if (this.survey.zoomTable[i].level <= zlevel && zlevel < this.survey.zoomTable[i+1].level)
            {
                this.camera.fov = this.cameraFov = (this.survey.zoomTable[i].fov + this.survey.zoomTable[i+1].fov) * 0.5;
                this.camera.updateProjectionMatrix();
                ASTROVIEW.log("setZoomLevel() zlevel:[" + this.zlevel +"]==>[" + zlevel + "] fov:[" + this.camera.fov + "]");
                this.zlevel = zlevel;
                this.zindex = i;
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
        for (var i=0; i<this.survey.zoomTable.length-1; i++)
        {
            if (this.survey.zoomTable[i].fov >= this.camera.fov && this.camera.fov >= this.survey.zoomTable[i+1].fov)
            {
                var zlevel = this.survey.zoomTable[i].level;
                if (zlevel != this.zlevel)
                {
                    ASTROVIEW.log("updateZoomLevel() zlevel:[" + this.zlevel +"]==>[" + zlevel + "] fov:[" + this.camera.fov + "]");
                    this.zlevel = zlevel;
                }
                this.zindex = i;
                break;
            }
        }
    }
 
    this.getZoomLevel = function()
    {
        return this.zlevel;
    }
    
    this.getZoomIndex = function()
    {
        return this.zindex;
    }
    
    this.getRaScale = function()
    {
        return this.survey.zoomTable[this.zindex].rax;
    }
    
    this.getDecScale = function()
    {
        return this.survey.zoomTable[this.zindex].decx;
    }
    
    //////////////////////////////////
    // getRaScaleFromZoomIndex()
    //////////////////////////////////
    this.getRaScaleFromZoomIndex = function(zindex)
    {        
        return this.survey.zoomTable[zindex].rax;
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
        if (width != this.canvas.clientWidth || height != this.canvas.clientHeight)
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
    this.moveCrossPoint = function(coord)
    {
        this.crossLayer.moveParticleTo(coord.radec);  // Move the Select Point Location
        this.radecView.update(coord);
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
    }; 
    
    /////////////////////////////
    // loadSurvey()
    /////////////////////////////
    this.loadSurvey = function(survey, first)
    {
        this.survey = survey;
        var len = survey.zoomTable.length;
        this.fovmax = survey.zoomTable[0].fov; 
        this.fovmin = survey.zoomTable[len-1].fov; 
        
        if (first)  // Set zlevel and cameraFov using first zoomTable entry
        {
            this.setZoomLevel(survey.zoomTable[0].level);
        }
        else        // Update zlevel based on active cameraFov 
        {
            this.updateZoomLevel();
        }
    }
            
    ///////////////
    // Main
    ///////////////
    this.loadSurvey(this.av.survey, true);
    this.addEvents();   
};

// Camera Controller Params: Desktop (Mouse) vs. Mobile (Touch)
ASTROVIEW.CameraController.MouseParams = {
    "zoomInClick"       : 0.4,  // How fast you zoom in each mouse click
    "zoomOutClick"      : 0.8,  // How fast you zoom out each mouse click
    "zoomInSpeed"       : 0.1,  // How fast you zoom in each mouse wheel
    "zoomOutSpeed"      : 0.3,  // How fast you zoom out each mouse wheel
    "lineDistance"      : 4.0,  // Line Distance in Pixels
    "particleDistance"  : 6.0};  // Particle Distance in Pixels

ASTROVIEW.CameraController.TouchParams = {
    "zoomInClick"       : 0.4,  // How fast you zoom in each mouse click
    "zoomOutClick"      : 0.8,  // How fast you zoom out each mouse click
    "zoomInSpeed"       : 0.3,  // How fast you zoom in each mouse gesture
    "zoomOutSpeed"      : 3.0,  // How fast you zoom out each mouse gesture
    "lineDistance"      : 12.0, // Line Distance in Pixels
    "particleDistance"  : 18.0}; // Particle Distance in PixelsASTROVIEW = ASTROVIEW || {};
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
}/////////////////////////
// Diamond
/////////////////////////
ASTROVIEW.Diamond = function( geometry, material )
{
    if (!material)
    {
        material = ASTROVIEW.Diamond.DefaultMaterial;
    }

    THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = geometry.name;
    this.matrixAutoUpdate = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image

    // Image Texture Properties
    this.texture = null;
    this.flipY = false;
    
    // Count of children diamonds that are actively loading images
    this.childLoadingCount = 0;
};

ASTROVIEW.Diamond.prototype = new THREE.Mesh();
ASTROVIEW.Diamond.prototype.constructor = ASTROVIEW.Diamond;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.Diamond.DefaultFile = ASTROVIEW.Diamond.DefaultFile || "DiamondDefault.png";
ASTROVIEW.Diamond.DefaultUrl = ASTROVIEW.Diamond.DefaultUrl || "../AstroView/" + ASTROVIEW.Diamond.DefaultFile;
ASTROVIEW.Diamond.DefaultMaterial = ASTROVIEW.Diamond.DefaultMaterial || new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.Diamond.DefaultUrl) } );

ASTROVIEW.Diamond.ErrorFile = ASTROVIEW.Diamond.ErrorFile || "DiamondError.png";
ASTROVIEW.Diamond.ErrorUrl = ASTROVIEW.Diamond.ErrorUrl || "../AstroView/" + ASTROVIEW.Diamond.ErrorFile;

/////////////////////////
// renderScene()
/////////////////////////
ASTROVIEW.Diamond.prototype.renderScene = function( av )
{   
    this.av = av;
    this.flipY = (av.renderType == 'webgl');
    var frustum = av.controller.frustum;
    var zlevel = av.controller.getZoomLevel();
 
    // Update Visibility if we are in the Viewing Frustum
    // (this.geometry.pixel == 15 || this.geometry.pixel == 219) && 
    this.viewable = this.inFrustum(frustum); 
        
    if (this.viewable)
    {         
        if (zlevel == this.geometry.zlevel)
        {    
            //
            // CASE I: We are in the Frustum, my level is active, Load my image, Remove any Children (for Zoom Out)
            //
                 
            // If survey url has changed, load new image
            if (av.survey.baseurl !== this.baseurl)
            {
                this.imageurl = this.geometry.getImageUrl(av.survey.baseurl);
                this.cleanTexture(av);
                this.loadTexture(this.imageurl);
                this.baseurl = av.survey.baseurl;
            }
     
            // Remove all Children below current vizible Level
            if (this.children.length > 0)
            {
                this.removeDiamondChildren(av);
            }
        }
        else if (zlevel > this.geometry.zlevel)
        {
            //
            // CASE II: We are in the Frustum, Zoom In Occured, Expand my Children, renderScene Children
            //
            if (this.children.length == 0)
            {
                this.expandDiamondChildren();
            }
                     
            // renderScene Children
            if (this.children.length > 0)
            {
                this.renderDiamondChildren(av);
            }
        }
        
        //
        // Update Material Opacity Flag:
        // Set to TRUE if our zoom level is currently active 
        // If our level is NOT active, then wait until all children are finished loading to clear out our image. 
        //
        // NOTE:
        // The opacity flag is used in THREE.CanvasMobileRenderer.js and substantially improves rendering performance
        // by reducing the number of calls to clipImage() which is fairly expensive.
        //
        if (this.material)
        {
            if (zlevel == this.geometry.zlevel)
            {   
                this.material.opacity = 1.0;
            }
            else if (this.material.opacity > 0.0 && this.childLoadingCount <= 0)
            {
                this.material.opacity = 0.0;
                this.childLoadingCount = 0;
            }
        }
    }
    else    
    {
        //
        // CASE III: We are Not in the Viewing Frustum, Remove all Children,
        //
        if (this.children.length > 0)
        {
            this.removeDiamondChildren(av);
        }
    }
};

ASTROVIEW.Diamond.prototype.clearScene = function( av )
{ 
    this.cleanTexture(av);
    this.baseurl = ASTROVIEW.Diamond.DefaultUrl;
    this.loadTexture(ASTROVIEW.Diamond.DefaultUrl);
    this.clearDiamondChildren();
};

ASTROVIEW.Diamond.prototype.loadTexture = function(url)
{  
    // Increment the number of active children loading images
    this.parent.childLoadingCount++;
      
    // Create Texture from Image
    this.texture = new THREE.Texture( new Image() );
    this.texture.image.onload = ASTROVIEW.bind(this, this.onLoad);
    this.texture.image.onerror = ASTROVIEW.bind(this, this.onError);
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
    
    // NOTE: We have to Flip the UV Mapping for WebGL
    this.texture.flipY = this.flipY;
}
             
ASTROVIEW.Diamond.prototype.onLoad = function(event)
{    
    //ASTROVIEW.log(this.name + " *** onLoad() url: " + this.texture.image.src);
    
    // Decrement the number of active children loading images
    this.parent.childLoadingCount--;
    
    // Clear out image callbacks
    this.texture.image.onload = undefined;
    this.texture.image.onerror = undefined;
    
    // Create Material from Image Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture, overdraw: this.geometry.overdraw, side: this.geometry.side } );
    
    // Wireframe 
    //this.material = new THREE.MeshBasicMaterial( { color: ASTROVIEW.Diamond.color[this.geometry.zlevel], wireframe: true, transparent: true, opacity: 1.0, side: THREE.DoubleSide } );
    
    // Solid Color 
    //this.material = new THREE.MeshBasicMaterial( { color: ASTROVIEW.Diamond.color[this.geometry.zlevel], transparent: true, opacity: 1.0, side: THREE.FrontSide } );
    
    // Update the Material
    this.texture.needsUpdate = true; 
          
    // Request full render
    this.av.render("DIAMOND");
}

ASTROVIEW.Diamond.prototype.onError = function(event)
{
    ASTROVIEW.log(this.name + " *** onError() url: " + this.texture.image.src);
    
    // Decrement the number of active children loading images
    this.parent.childLoadingCount--;
  
    // Clear out image callbacks
    this.texture.image.onload = undefined;
    this.texture.image.onerror = undefined;
    
    // If failed file is not our default file, load the default file (blank diamond)   
    var src = this.texture.image.src;
    var srcfile = src.substring(src.lastIndexOf('/')+1);
  
    if (srcfile != ASTROVIEW.Diamond.DefaultFile && 
        srcfile != ASTROVIEW.Diamond.ErrorFile)
    {
        this.loadTexture(ASTROVIEW.Diamond.ErrorUrl);
    }
}

ASTROVIEW.Diamond.prototype.setMaterialOpacity = function(visible)
{   
    if (this.material) 
    {
        this.material.opacity = (visible ? 1.0 : 0.0);
    }
}

ASTROVIEW.Diamond.prototype.expandDiamondChildren = function()
{
    // geometry.expandDiamond() returns array of THREE.DiamondGeometry Objects (dgs)
    var dgs = this.geometry.expandDiamond();
    for (i=0; i<dgs.length; i++)
    {
        var ddd = new ASTROVIEW.Diamond( dgs[i] );
        this.add(ddd);
    }
};

ASTROVIEW.Diamond.prototype.renderDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
    {
        var ddd = this.children[i];
        ddd.renderScene(av);
    }
};

ASTROVIEW.Diamond.prototype.clearDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
    {
        var ddd = this.children[i];
        ddd.clearScene(av);
    }
};

ASTROVIEW.Diamond.prototype.removeDiamondChildren = function( av )
{
    var names = "";
    while (this.children.length > 0)
    {
        var ddd = this.children[0];
        if (ddd.children.length > 0) ddd.removeDiamondChildren(av);
        names += ddd.name + " ";
        ddd.clean(av);
        this.remove(ddd);   // NOTE: Decrements this.children.length
    }
    this.children = [];
};

ASTROVIEW.Diamond.prototype.clean = function( av )
{
    // Remove the Texture, Image and Material
    this.cleanTexture(av);
 
    // Remove geometry and mesh from WebGL Graphics Memory 
    if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
    {
        if (this.geometry) av.renderer.deallocateObject(this.geometry);     
        av.renderer.deallocateObject(this);
    }
 
    // Remove all Object References
    if (this.geometry)
    {
        this.geometry.clean();
        this.geometry = undefined;
    }
}

ASTROVIEW.Diamond.prototype.cleanTexture = function( av )
{        
    // Texture is created from Image
    if (this.texture)
    {
        // Remove texture from WebGL Graphics Memory 
        if (av && av.renderer && av.renderer instanceof THREE.WebGLRenderer)
        {
            av.renderer.deallocateTexture(this.texture);
        }
     
        if (this.texture.image)
        {
            // Check if image loading is still underway 
            if (this.texture.image.onload || this.texture.image.onerror)
            {
                this.parent.childLoadingCount--;
                this.texture.image.onload = undefined;
                this.texture.image.onerror = undefined;
            }
        }
        this.texture = undefined;
    }
}

ASTROVIEW.Diamond.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
}

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

        // Initialize Temporary Values used for determining Point to Line distances below
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
                program: ASTROVIEW.bind(this, this.circle)
            });
            break;

        case 'square':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.square)
            });
            break;

        case 'plus':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.plus)
            });
            break;
            
        case 'cross':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.cross)
            });
            break;

        case 'diamond':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.diamond)
            });
            break;

        case 'stop':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.stop)
            });
            break;
            
        case 'spin':
        case 'spinClock':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.spinClock)
            });
            break;
            
        case 'spinCounter':
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.spinCounter)
            });
            break;

        default:
            pmaterial = new THREE.ParticleCanvasMaterial(
            {
                color: color,
                program: ASTROVIEW.bind(this, this.square)
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
// cross()
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
// spinClock()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.spinClock = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
    
    context.beginPath();    
    context.arc(0, 0, 12, 0.8*Math.PI, 0.2*Math.PI, false);
    context.stroke(); 
    context.closePath();
           
    this.drawArrowHead(context, -7,  9,  35*TO_RADIANS, 8, 6);      
};

/////////////////////////////
// spinCounter()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.spinCounter = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH;
    
    context.beginPath();    
    context.arc(0, 0, 12, 0.8*Math.PI, 0.2*Math.PI, false);
    context.stroke(); 
    context.closePath();
           
    this.drawArrowHead(context,  7,  9, 145*TO_RADIANS, 8, 6);    
};

ASTROVIEW.GraphicsLayer.prototype.drawArrowHead = function (ctx, locx, locy, angle, sizex, sizey) 
{
    var hx = sizex / 2;
    var hy = sizey / 2;
    
    ctx.save();
    ctx.beginPath();
    ctx.translate((locx ), (locy));
    ctx.rotate(angle);
    ctx.translate(-hx,-hy);
    ctx.moveTo(0,0);
    ctx.lineTo(0,1*sizey);    
    ctx.lineTo(1*sizex,1*hy);
    ctx.closePath();
    ctx.fill();
    ctx.restore();
}   

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
    
    var cameraChanged = (this.cameraPosition !== controller.cameraPosition);
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
    if (!particle.position.coord) 
        particle.position.coord = new ASTROVIEW.Coord(controller, particle.position);
        
    if (cameraChanged)
        particle.position.coord.normalToScreen();

    // Calculate the distance in Screen Coordinates
    var distance = this.distancePointToLineSegmentSquared(mouse.screen, 
                                                          particle.position.coord.screen, 
                                                          particle.position.coord.screen); 
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
        if (!ps.geometry.vertices[i].coord) 
            ps.geometry.vertices[i].coord = new ASTROVIEW.Coord(controller, particle.position);
            
        if (cameraChanged)
            ps.geometry.vertices[i].coord.normalToScreen();

        // Calculate the distance in Screen Coordinates
        dd = this.distancePointToLineSegmentSquared(mouse.screen, 
                                                    ps.geometry.vertices[i].coord.screen, 
                                                    ps.geometry.vertices[i].coord.screen);  
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
        // Create Screen Vector
        if (!vertices[i].coord) 
            vertices[i].coord = new ASTROVIEW.Coord(controller, vertices[i]);
        if (!vertices[i+1].coord) 
            vertices[i+1].coord = new ASTROVIEW.Coord(controller, vertices[i+1]);
        
        // Compute new Screen coordinates if Camera Position has moved
        if (cameraChanged)
        {
            vertices[i].coord.normalToScreen();
            vertices[i+1].coord.normalToScreen();
        }
      
        dd = this.distancePointToLineSegmentSquared(mouse.screen, 
                                                    vertices[i].coord.screen, 
                                                    vertices[i+1].coord.screen);     
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

ASTROVIEW.lid = 0;/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );

    if (layerData && layerData.rows && layerData.rows.length > 0)
    {
        this.createParticles(layerData.rows, layerData.name);
    }

    // NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.CatalogLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.CatalogLayer.prototype.constructor = ASTROVIEW.CatalogLayer;

/////////////////////////////
// createParticles()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.createParticles = function(rows, name)
{
    //
    // NOTE: The Particle API is different for WebGLRenderer vs. CanvasRenderer
    //       so we separate the code accordingly.
    //
    if (rows && rows.length > 0)
    {
        for (var i=0; i<rows.length; i++)
        {
            // Check all mixed case variations for RA and DEC values
            var row = rows[i];
            var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
            var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));

            // Add the particle at specified location
            var userData = {"rows" : [row], "name" : name};
            this.createParticle(ra, dec, userData); // invokes GraphicsLayer.createParticle()
        }
    }
}

/////////////////////////////
// FootprintLayer()
/////////////////////////////
ASTROVIEW.FootprintLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );
    
    //
    // For each footprint in the layerData object, create a THREE.Line Object
    // and add it as a child to this layerData
    //
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {    
        var uds = [];  // Array of UserData Objects hashed by "footprint" value to prevent creating duplicates       
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            if (row.hasOwnProperty("footprint"))
            {
                var footprint = row["footprint"];
             
                // Check for Existing Footprint
                if (uds[footprint] !== undefined)
                {
                    var userData = uds[footprint];
                    userData.rows.push(row);
                }
                else // New Footprint
                {
                    var userData = {"rows" : [row], "name" : layerData.name};
                    uds[footprint] = userData;
                    this.createFootprint(row, userData);
                }
            }
        }
    }
           
    // NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.FootprintLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.FootprintLayer.prototype.constructor = ASTROVIEW.FootprintLayer;

/////////////////////////////
// createFootprint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(row, userData)
{
    // Validate input
    if (row == undefined || row.footprint == undefined) return;

    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var type = "";
    var fplist = null;
    
    // ASTROVIEW.trim, Upper Case, Remove all parens: () 
    var upper = ASTROVIEW.trim(row.footprint.toUpperCase());
    var fp = upper.replace(/\(/g,' ').replace(/\)/g, ' ');
    
    // Split the footprint string
    if (fp.indexOf(ASTROVIEW.POLYGON_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_J2000_RE);
        type = ASTROVIEW.POLYGON_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POLYGON_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_ICRS_RE);
        type = ASTROVIEW.POLYGON_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_ICRS_RE);
        type = ASTROVIEW.CIRCLE_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_J2000_RE);
        type = ASTROVIEW.CIRCLE_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POSITION_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_ICRS_RE);
        type = ASTROVIEW.POSITION_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.POSITION_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_J2000_RE);
        type = ASTROVIEW.POSITION_J2000;
    }
    
    // If we found no valid fplist, bail
    if (fplist == null || fplist.length < 2)
    {
        this.alert("Illegal Footprint:" + fp + "\n\nExpected Syntax: \n\n POLYGON [J2000|ICRS] [ra dec]\n CIRCLE [J2000|ICRS] [ra dec]\n POSITION [J2000|ICRS] [ra dec]", "Illegal Footprint");
        return;
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolygons(fplist, userData);
            break;
        }
        case ASTROVIEW.CIRCLE_J2000:
        case ASTROVIEW.CIRCLE_ICRS:
        {
            this.createCircles(fplist, userData);
            break;
        }
        case ASTROVIEW.POSITION_J2000:
        case ASTROVIEW.POSITION_ICRS:
        {
            this.createPoints(fplist, userData);
            break;
        }
        default:
        {
            this.alert("Footprint Syntax Error\n\n   Valid Syntax: 'POLYGON J2000|ICRS [values]' 'POSITION J2000|ICRS [values]' or 'CIRCLE J2000|ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolygons()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolygons = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var polyline = ASTROVIEW.trim(fplist[i]);
        if (polyline.length > 0)
        {
            this.createPolyline(polyline, userData);     // GraphicsLayer.createPolyline()
        }
    }
}

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var circle = ASTROVIEW.trim(fplist[i]);
        if (circle.length > 0)
        {
            this.createCircle(circle, userData);         // GraphicsLayer.createCircle()
        }
    }
}

/////////////////////////////
// createPoints()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoints = function(fplist, userData)
{
    for (var i=0; i<fplist.length; i++)
    {
        var point = ASTROVIEW.trim(fplist[i]);
        if (point.length > 0)
        {
            this.createPoint(point, userData);
        }
    }
}

/////////////////////////////
// createPoint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoint = function(point, userData)
{
    var coords = point.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
            this.createParticle(ra, dec, userData);     // GraphicsLayer.createParticle()
        }
    }
    else
    {
        this.alert("Footprint POINT Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + point, "Unable to Create Footprint");
    }
}

/////////////////////////////
// Footprint Constants
/////////////////////////////
ASTROVIEW.POLYGON_J2000 = 'POLYGON J2000';
ASTROVIEW.POLYGON_ICRS  = 'POLYGON ICRS';

ASTROVIEW.POLYGON_J2000_RE = new RegExp(ASTROVIEW.POLYGON_J2000 + "\\s+");
ASTROVIEW.POLYGON_ICRS_RE  = new RegExp(ASTROVIEW.POLYGON_ICRS + "\\s+");

ASTROVIEW.CIRCLE_J2000  = "CIRCLE J2000";
ASTROVIEW.CIRCLE_ICRS   = "CIRCLE ICRS";

ASTROVIEW.CIRCLE_J2000_RE = new RegExp(ASTROVIEW.CIRCLE_J2000 + "\\s+");
ASTROVIEW.CIRCLE_ICRS_RE  = new RegExp(ASTROVIEW.CIRCLE_ICRS + "\\s+");

ASTROVIEW.POSITION_J2000 = "POSITION J2000";
ASTROVIEW.POSITION_ICRS  = "POSITION ICRS";

ASTROVIEW.POSITION_J2000_RE = new RegExp(ASTROVIEW.POSITION_J2000 + "\\s+");
ASTROVIEW.POSITION_ICRS_RE = new RegExp(ASTROVIEW.POSITION_ICRS + "\\s+");

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
    var footprintUpper = ASTROVIEW.trim(footprint.toUpperCase());
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
        var polygon = ASTROVIEW.trim(polygons[i]);
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

//////////////////////////
// HealpixGeometry()
//////////////////////////
ASTROVIEW.HealpixGeometry = function(uv, tx, ty, pixel12, zlevel, pixel, radius, dummy, expand)
{
    THREE.Geometry.call( this );

    this.uv = uv;    
    this.tx = tx;
    this.ty = ty;
    this.pixel12 = pixel12;
    this.zlevel = zlevel;
    this.pixel = pixel;                                       // Healpix Pixel ID
    this.name = "[" + zlevel + ":" + pixel + "]";             // Diamond Name: [zoom, pixel]
    this.radius = radius;
    this.dummy = dummy;                                       // Dummy Survey Loaded ?
    this.dynamic = true;
    
    // Texture Attributes (used by Diamond.js)
    this.side = THREE.FrontSide;                               // THREE.FrontSide, THREE.BackSide and THREE.DoubleSide; 
    this.overdraw = (dummy ? false : true);                    // Overdraw image over edge of Diamond to hide lines for *real* image surveys.  
    
    //
    // NOTE: If specified, expand our geometry to the next level for improved resolution and visualization
    //
    if (expand)
    {
        this.uv = this.expandDiamondUV();
    }
    
    // Create this.vertices[] array from uv[] unit vector3 array
    if (this.uv && this.uv.length > 0)
    {
        for (var i=0; i<this.uv.length; i++)
        {
            // Create new Vertex from the cloned Unit Vector.  Then scale it to the Sphere Radius
            var v = this.uv[i].clone();
            v.multiplyScalar(radius);   
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length > 0)
    {
        var fuv = this.getFaceUV(dummy);        // get Face UV[] mapping array: which differs for dummy images.
        this.createFaces(this.vertices, fuv);   // create this.faces[] based on this.vertices[] and Face UV[]
        this.computeCentroids();                // inherited call
    }
};

ASTROVIEW.HealpixGeometry.prototype = new THREE.Geometry();
ASTROVIEW.HealpixGeometry.prototype.constructor = ASTROVIEW.HealpixGeometry;

////////////////////////////////
// FaceUV: Static Arrays
////////////////////////////////
var uv00 = uv00 || new THREE.Vector2( 0, 0 );
var uv01 = uv01 || new THREE.Vector2( 0, 1 );
var uv10 = uv10 || new THREE.Vector2( 1, 0 );
var uv11 = uv11 || new THREE.Vector2( 1, 1 );

var uv50 = uv50 || new THREE.Vector2( 0.5,   0 );
var uv15 = uv15 || new THREE.Vector2(   1, 0.5 );
var uv51 = uv51 || new THREE.Vector2( 0.5,   1 );
var uv05 = uv05 || new THREE.Vector2(   0, 0.5 );
var uv55 = uv55 || new THREE.Vector2( 0.5, 0.5 );

ASTROVIEW.HealpixGeometry.faceUVNormal = ASTROVIEW.HealpixGeometry.faceUVNormal || 
    [uv11, uv10, uv00, uv01];       //  Works with Aladin Healpix Jpg Images INSIDE Sphere
    
ASTROVIEW.HealpixGeometry.faceUVDummy = ASTROVIEW.HealpixGeometry.faceUVDummy || 
    [uv00, uv10, uv11, uv01];       //  Works with Dummy Images INSIDE Sphere
     // [uv00, uv01, uv11, uv10];    //  Works with Dummy Images OUTSIDE Sphere

////////////////////////////////
// getFaceUV() 
////////////////////////////////
ASTROVIEW.HealpixGeometry.prototype.getFaceUV = function(dummy)
{   
    if (dummy)
        return ASTROVIEW.HealpixGeometry.faceUVDummy;     // Dummy Survey : Flip Image UV Mapping so that label is readable from inside Sphere
    else
        return ASTROVIEW.HealpixGeometry.faceUVNormal;    // Normal Healpix Survey UV Mapping
}

//////////////////////////
// clean()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.clean = function()
{
    for (var i=0; i<this.faces.length; i++)
    {
        this.cleanFace(this.faces[i]);
        this.faces[i] = null;
    }
        
    this.faces = undefined;
    this.uv = undefined;
    this.vertices = undefined;
    this.faceVertexUvs = undefined;
}

ASTROVIEW.HealpixGeometry.prototype.cleanFace = function(face)
{
    face.normal = undefined;
    face.vertexNormals = undefined;
    face.vertexColors = undefined;
    face.vertexTangents = undefined;
    face.centroid = undefined;
}

//////////////////////////
// createFaces()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.createFaces = function(v, fuv)
{
    if (v && v.length == 4)
    {
        this.createFaces4(v, fuv);
    }
    else if (v && v.length == 9)
    {
        this.createFaces9(v, fuv);
    }
}

ASTROVIEW.HealpixGeometry.prototype.createFaces4 = function(v, fuv)
{
    var normal = new THREE.Vector3( 0, 0, 1 );
    
    //
    // Create 'Upper' Face 0 (v0 -> v3 -> v1)
    //  
    var face0 = new THREE.Face3( 0, 3, 1);
    face0.normal.copy( normal );
    face0.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face0 );
    this.faceVertexUvs[ 0 ].push( [ fuv[0], fuv[3], fuv[1] ]);
    
    //
    // Create 'Lower' Face 1 (v2 -> v1 -> v3)
    //
    var face1 = new THREE.Face3( 2, 1, 3 );
    face1.normal.copy( normal );
    face1.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face1 );
    this.faceVertexUvs[ 0 ].push( [ fuv[2], fuv[1], fuv[3] ]);
};

ASTROVIEW.HealpixGeometry.prototype.createFaces9 = function(v, fuv)
{
    var normal = new THREE.Vector3( 0, 0, 1 );
    var face;
     
    face = new THREE.Face3( 0, 3, 1);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv11, uv51, uv15 ]);
    
    face = new THREE.Face3( 4, 1, 3);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv55, uv15, uv51 ]);

    face = new THREE.Face3( 1, 4, 2);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv15, uv55, uv10 ]);
        
    face = new THREE.Face3( 5, 2, 4);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv50, uv10, uv55 ]);
    
    face = new THREE.Face3( 3, 6, 4);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv51, uv01, uv55 ]);
    
    face = new THREE.Face3( 7, 4, 6);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv05, uv55, uv01 ]);
    
    face = new THREE.Face3( 4, 7, 5);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv55, uv05, uv50 ]);
    
    face = new THREE.Face3( 8, 5, 7);
    face.normal.copy( normal );
    face.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
    this.faces.push( face );
    this.faceVertexUvs[ 0 ].push( [ uv00, uv50, uv05 ]);
};

// Support color rotation in the dummy tiles so that you can easily see the boundaries.
ASTROVIEW.HealpixGeometry.COLORS = [
    "FB0204",
    "FC4604",
    "FC7E04",
    "FCB304",
    "FCFE04",
    "04FE04",
    "046904",
    "04AEAC",
    "0402FC",
    "7406A5",
    "BC02FB",
    "CC02AD",
    "00FFFF",
    "AAAAFF",
    "55FF55",
    "FF5555",
    "5555FF",
    "FFAAAA",
    "FFFF55",
    "AA0000"
];

//////////////////////////
// getImageUrl()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.getImageUrl = function(baseurl)
{
    // Healpix Url
    var dir = new String(parseInt(this.pixel/10000) * 10000);
    var url = new String(baseurl).replace("[LEVEL]", this.zlevel).replace("[DIR]", dir).replace("[PIXEL]", this.pixel).replace("[PIXEL12]", this.pixel12);
    
    if (this.dummy)
    {
        var color = ASTROVIEW.HealpixGeometry.COLORS[this.pixel%ASTROVIEW.HealpixGeometry.COLORS.length];
        url = new String(url).replace("[COLOR]", color);
    }
    return url;   
}

//////////////////////////
// expandDiamond()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.expandDiamondUV = function(zlevel, radius) 
{ 
    //
    // NOTE: 4 Expanded Diamond Geometry Objects (D0-D3) are arranged as follows
    //   
    //          uv0
    //          /\           
    //         /  \
    //    uv3 / D3 \ uv1        
    //       /\    /\            
    //      /  \  /  \                 
    // uv6 / D1 \/ D2 \ uv2            
    //     \    /\    /          
    //      \  /  \  /
    //   uv7 \/ D0 \/  
    //        \    / uv5
    //         \  /
    //          \/
    //          uv8
    //
    // We now add the Unit Vectors (uv) in the order shown above from (uv0 - uv8) onto the new uv[] array.
    // NOTE: uv4 is in the center.
    //
    var uv = [];
    var D = this.expandDiamond();
    
    uv.push(D[3].uv[0]);
    uv.push(D[3].uv[1]);
    uv.push(D[2].uv[1]);
    
    uv.push(D[3].uv[3]);
    uv.push(D[3].uv[2]);
    uv.push(D[2].uv[2]);
    
    uv.push(D[1].uv[3]);
    uv.push(D[1].uv[2]);
    uv.push(D[0].uv[2]);
    
    return uv;
}

ASTROVIEW.HealpixGeometry.hpix = ASTROVIEW.HealpixGeometry.hpix || 
[
    null,
    new HEALPIX.HealpixIndex(null, 1),
    new HEALPIX.HealpixIndex(null, 2),
    new HEALPIX.HealpixIndex(null, 3),
    new HEALPIX.HealpixIndex(null, 4),
    new HEALPIX.HealpixIndex(null, 5),
    new HEALPIX.HealpixIndex(null, 6),
    new HEALPIX.HealpixIndex(null, 7),
    new HEALPIX.HealpixIndex(null, 8),
    new HEALPIX.HealpixIndex(null, 9),
    new HEALPIX.HealpixIndex(null, 10),
    new HEALPIX.HealpixIndex(null, 11),
    new HEALPIX.HealpixIndex(null, 12)
];
       
//////////////////////////
// expandDiamond()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.expandDiamond = function(zlevel, radius) 
{    
    //
    //      NOTE: Diamond.uv[4] vector is the 4 corners of the diamond on the Unit Sphere
    //
    //          P (this)
    //          /\           
    //         /  \
    //        / P0 \           Construct 4 new "Pixels" P0-P3 from this pixel where  
    //       /\    /\            P0 = pixel*4 + 0;    
    //      /  \  /  \           P1 = pixel*4 + 1;       
    // uv3 / P3 \/ P1 \ uv1      P2 = pixel*4 + 2;       
    //     \    /\    /          P3 = pixel*4 + 3;      
    //      \  /  \  /
    //       \/ P2 \/  
    //        \    /
    //         \  /
    //          \/
    //          uv2
    //   
    //  NOTE: The above perspective is from INSIDE the sphere with v0 pointing true NORTH
    //              
        
    //
    // Expand this "nested" Pixel into 4 child "Pixels":
    //
    // (1) Calculate children pixels IDs: 0 - 3
    // (2) Calculate the Corner Geometry for each child pixel using HealpixIndex class
    // (3) Create HealpixGeometry for each corner
    // (4) Add each HealpixGeometry to the return Array.
    //

    // Child zlevel
    if (!zlevel) zlevel = this.zlevel + 1;
    if (!radius) radius = this.radius;
    
    // Child pixel[0-3] = (pixel * 4) + [0-3]
    var pixel4 = this.pixel*4;

    // Create Healpix Index for child level
    //var hpix = new HEALPIX.HealpixIndex(nside, zlevel);
    var hpix = ASTROVIEW.HealpixGeometry.hpix[zlevel];
    
    // Create the child HealpixGeometry Objects
    var dgs=[];
    for (var pixel=pixel4; pixel < pixel4+4; pixel++)
    {
        var step = null;   // step = 3:returns 8 points (step=null: returns 4 points)
        var corners = hpix.corners_nest(pixel, step); 
        var uv = this.cornersToVector3(corners);
        var expand = false; //(zlevel == 4);
        var dg = new ASTROVIEW.HealpixGeometry(uv, 0, 1, this.pixel12, zlevel, pixel, radius, this.dummy, expand);
        dgs.push(dg);
    }   
    
    return dgs;
};

ASTROVIEW.HealpixGeometry.prototype.cornersToVector3 = function(corners)
{  
    // Set up rotation (90 degrees) matrix
    var x = -90.0 * TO_RADIANS;
    var y = 90.0 * TO_RADIANS;
    var v90 = new THREE.Vector3(x, y, 0);
    var m90 = new THREE.Matrix4().identity().setRotationFromEuler( v90, 'YXZ' );;
    
    // Convert corners to Vertex3 Objects and rotate using rotation matrix
    var vv = [];
    for (var i=0; i<corners.length; i++)
    {
        var v = new THREE.Vector3(corners[i].x,  corners[i].y,  corners[i].z); 
        v.applyProjection(m90);
        vv.push(v);
    }
    return vv;
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.inFrustum = function(frustum)
{
    var faces = this.facesInFrustum(frustum);
    return (faces); 
};

//////////////////////////
// facesInFrustrum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.facesInFrustum = function(frustum)
{
    var inFrustum = false;
    for (var i=0; i<this.faces.length; i++)
    {
        inFrustum = this.faceInFrustum(this.faces[i], frustum);
        if (inFrustum) break;
    }
                      
    return (inFrustum);
}

//////////////////////////
// faceInFrustrum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.faceInFrustum = function(face, frustum)
{
    // THREE.Face3
    if (!face.__radius) face.__radius = this.computeFaceRadius(face);
    var sphere = {"center":face.centroid, "radius":face.__radius};
    return frustum.intersectsSphere(sphere);
};

//////////////////////////
// computeFaceRadius()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.computeFaceRadius = function(face)
{
    var v0 = this.vertices[face.a];
    var v1 = this.vertices[face.b];
    var v2 = this.vertices[face.c];

    var dv0 = face.centroid.distanceTo(v0);
    var dv1 = face.centroid.distanceTo(v1);
    var dv2 = face.centroid.distanceTo(v2);

    return Math.max(dv0, dv1, dv2);            
}/////////////////////////////
// HealpixSphere()
/////////////////////////////
ASTROVIEW.HealpixSphere = function ( radius, survey )
{ 
    THREE.Object3D.call( this );
    this.name = "HealpixSphere";
    this.matrixAutoUpdate = false;
    this.createSphere( radius, survey );
};

ASTROVIEW.HealpixSphere.prototype = new THREE.Object3D();
ASTROVIEW.HealpixSphere.prototype.constructor = ASTROVIEW.HealpixSphere;

/////////////////////////////
// debug()
/////////////////////////////
ASTROVIEW.HealpixSphere.prototype.debug = function()
{
    var level = 1;
    var pixel = 0;
    var hpi = new HEALPIX.HealpixIndex(null, level);
    for (var step=1; step<10; step++)
    {
        var corners = hpi.corners_nest(pixel, step); 
        ASTROVIEW.log("*** level: " + level + " step:" + step + " corners:" + corners.length);
        this.dump(corners);
    }
}

ASTROVIEW.HealpixSphere.prototype.dump = function(corners)
{
    for (var i=0; i<corners.length; i++)
    {
        var c = corners[i];
        ASTROVIEW.log("[x:" + c.x.toFixed(3) + " y:" + c.y.toFixed(3) + " z:" + c.z.toFixed(3) + "]");
    }
}
 
ASTROVIEW.HealpixSphere.prototype.createSphere = function( radius, survey )
{
    //
    // STEP 1: Create Array of HealpixGeometry Objects
    //
    var zlevel = 1;
    var step = 1;   // Step = 5 returns 12 'corners'
    var dgs=[];
      
    var hpi = new HEALPIX.HealpixIndex(null, zlevel);
    for (var pixel=0; pixel < hpi.npix; pixel++)
    {
        var corners = hpi.corners_nest(pixel, step); 
        var uv = this.cornersToVector3(corners);
        var dg = new ASTROVIEW.HealpixGeometry(uv, 0, 1, pixel, zlevel, pixel, radius, survey.dummy );
        dgs.push(dg);
    }
     
    //
    // STEP 2: Create Diamond objects from the HealpixGeometry[] array and
    //         add them as children to the HealpixSphere
    this.createDiamonds(dgs, null); 
}

ASTROVIEW.HealpixSphere.prototype.cornersToVector3 = function(corners)
{   
    // Set up rotation matrix
    var x = -90 * TO_RADIANS;
    var y = 90 * TO_RADIANS;
    var v90 = new THREE.Vector3(x, y, 0);
    var m90 = new THREE.Matrix4().identity().setRotationFromEuler( v90, 'YXZ' );;
    
    // Convert corners to Vertex3 Objects and rotate using rotation matrix
    var vv = [];
    for (var i=0; i<corners.length; i++)
    {
        var v = new THREE.Vector3(corners[i].x,  corners[i].y,  corners[i].z); 
        v.applyProjection( m90 );
        vv.push(v);
    }
    return vv;
}

/////////////////////////////
// createDiamondMeshMaterial()
/////////////////////////////
ASTROVIEW.HealpixSphere.prototype.createDiamonds = function(dgs, material)
{
    // Loop through THREE.HealpixGeometry Objects and create a Diamond for each one.
    for (i=0; i<dgs.length; i++)
    { 
        var dg = dgs[i];
        var ddd = new ASTROVIEW.Diamond( dg, material );
        this.add(ddd);
    }
}
  
/////////////////////////////
// renderScene()
/////////////////////////////
ASTROVIEW.HealpixSphere.prototype.renderScene = function( av )
{
    if (this.children && this.children.length > 0)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var ddd = this.children[i]; // ASTROVIEW.HealpixDiamond
            if (ddd instanceof ASTROVIEW.Diamond)
            {
                ddd.renderScene(av);
            }
        }
    }
}

/////////////////////////////
// clearScene()
/////////////////////////////
ASTROVIEW.HealpixSphere.prototype.clearScene = function( av )
{
    if (this.children && this.children.length > 0)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var ddd = this.children[i]; // ASTROVIEW.Diamond
            if (ddd instanceof ASTROVIEW.Diamond)
            {
                ddd.clearScene(av);
            }
        }
    }
}

var ASTROVIEW = ASTROVIEW || {};
/////////////////////////////
// RaDecView
/////////////////////////////
ASTROVIEW.RaDecView = function ( av )
{ 
    this.av = av;
     // Create div element if not specified   
    this.div = null;
    this.radecIcon = null;
    this.divLabel = null;
    this.divLive = null;
    this.divCross = null;
    this.format = "sexagesimal";
    
    this.coord = new ASTROVIEW.Coord();
    this.createRaDecView();
};

ASTROVIEW.RaDecView.prototype = new Object();
ASTROVIEW.RaDecView.prototype.constructor = ASTROVIEW.RaDecView;

/////////////////////////////
// createRaDecView()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.createRaDecView = function()
{    
    this.div = document.createElement('div');
    this.div.id = 'divRaDec';
    this.div.style.right = '4px';
    this.div.style.position = 'absolute';
    this.div.style.top = '0px';
    this.div.style.textAlign = 'right';
    this.div.style.fontFamily = 'Verdana'; 
    this.div.style.fontSize = '10pt'; 
    this.div.style.fontWight = 'bold';

    if (!ASTROVIEW.MOBILE)
    {
        this.divLive = document.createElement('div');
        this.divLive.id = 'divLive'; 
        this.divLive.style.position = 'absolute';
        this.divLive.style.right = '84px';
        this.divLive.style.top = '4px';
        this.divLive.style.width = '300px'; 
        this.divLive.style.color = 'rgb(248, 176, 72)'; 
        this.divLive.innerHTML = " 0.0 , 0.0 ";
        this.div.appendChild(this.divLive);
    }

    this.divCross = document.createElement('div');
    this.divCross.id = 'divCross'; 
    this.divCross.style.position = 'absolute';
    this.divCross.style.top = (ASTROVIEW.MOBILE ? '4px' : '20px');
    this.divCross.style.right = '84px';
    this.divCross.style.width = '300px';
    this.divCross.style.color = 'rgb(200, 50, 200)'; 
    this.divCross.innerHTML = " 0.0 , 0.0";
    this.div.appendChild(this.divCross); 
         
    // Create Settings Icon 
    this.radecIcon = new Image();
    this.radecIcon.id = 'imgRaDec';
    this.radecIcon.src = '../../Clients/AstroView/radec.png';
    this.radecIcon.style.position = 'absolute';
    this.radecIcon.style.top = '4px';
    this.radecIcon.style.right = '4px';
    this.radecIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onMouseDown),  false );
    this.radecIcon.title = "Click to change [RA,DEC] format to sexagesimal or degrees";
    this.div.appendChild( this.radecIcon ); 
    
    return this.div;
}

/////////////////////////
// Mouse Events
/////////////////////////
ASTROVIEW.RaDecView.prototype.onMouseDown = function(event)
{
    event.preventDefault();
    this.format = (this.format == "degrees" ? "sexagesimal" : "degrees");
    this.updateLive(this.coord);      // Update Live Div
    this.updateCross(this.coord);     // Update Cross Div
    this.av.render("CROSS");
}

/////////////////////////////
// update()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.update = function(coord)
{
    this.updateLive(coord);     // Update Live Div
    this.updateCross(coord);    // Update Cross Div
    this.coord.copy(coord);     // Save the Coord for later use in case the 'format' is switched (see onMouseDown)
}

/////////////////////////////
// udpateCross()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateCross = function(coord)
{
    if (this.divCross)
    {
        this.divCross.innerHTML = coord.radecToString(this.format);
    }
}

/////////////////////////////
// udpateLive()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateLive = function(coord)
{
    if (this.divLive)
    {
        this.divLive.innerHTML = coord.radecToString(this.format);
    }
}//////////////////////////////////////////////////////////
// ASTROVIEW URLS 
//
// Syntax: ASTROVIEW.urls.[type].[server].[name]
//  
//         [type]:=   [toast, healpix, proxy, name]
//         [server]:= [mastimg, dummy, wwt, aladin]
//         [name]:=   [DSS, GALEX, SDSS]
//
//////////////////////////////////////////////////////////
ASTROVIEW.urls = {
    "name" : {},
    "proxy": {},
    "toast": {
        "dummy":{},
        "mastimg":{},
        "wwt":{}
    },
    "healpix": {
        "dummy":{},
        "mastimg":{},
        "aladin":{}
    },
};

// Name
ASTROVIEW.urls.name.mastresolver    = "http://mastresolver.stsci.edu/Santa-war/query?outputFormat=xml&name=";

// Proxy
ASTROVIEW.urls.proxy.mastdev        = "http://mastdev.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";
ASTROVIEW.urls.proxy.masttest       = "http://masttest.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";
ASTROVIEW.urls.proxy.mast           = "http://mast.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";

// TOAST 
ASTROVIEW.urls.toast.dummy.image    = ASTROVIEW.urls.proxy.masttest + "http://dummyimage.com/256x256/[COLOR]/000000%26text=%5B" + "[LEVEL]" + ":" + "[TX]" + "," + "[TY]" + "%5D";
ASTROVIEW.urls.toast.mastimg.dss    = "http://mastimg.stsci.edu/surveys/toast/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
ASTROVIEW.urls.toast.mastimg.galex  = "http://mastimg.stsci.edu/surveys/toast/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
ASTROVIEW.urls.toast.mastimg.sdss   = "http://mastimg.stsci.edu/surveys/toast/sdss/[LEVEL]/[TX]/sdss_[LEVEL]_[TX]_[TY].jpg";  

// HEALPIX 
ASTROVIEW.urls.healpix.dummy.image  = ASTROVIEW.urls.proxy.masttest + "http://dummyimage.com/256x256/[COLOR]/000000%26text=%5B" + "[LEVEL]" + ":" + "[PIXEL]" + "%5D"; 
ASTROVIEW.urls.healpix.mastimg.dss  = "http://mastimg.stsci.edu/surveys/healpix/dss/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
ASTROVIEW.urls.healpix.aladin.dss   = ASTROVIEW.urls.proxy.masttest + "http://alasky.u-strasbg.fr/DssColor/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
ASTROVIEW.urls.healpix.aladin.iras  = ASTROVIEW.urls.proxy.masttest + "http://alasky.u-strasbg.fr/IRISColor/Norder3/Dir0/Npix[PIXEL].jpg";

//////////////////////////////////////////////////////////
//
// ASTROVIEW Surveys
//
//////////////////////////////////////////////////////////

ASTROVIEW.surveys = [
{
    "name"      : "DSS",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.dss,

    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":11, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":12, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":12, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":12, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":12, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":12, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":12, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":12, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":12, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":12, "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "GALEX",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.galex,
    
    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":10, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":10, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":10, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":10, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":10, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":10, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":10, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":10, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":10, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":10, "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "SDSS",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.sdss,
    
    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":9,  "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":9,  "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":9,  "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":9,  "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":9,  "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":9,  "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":9,  "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":9,  "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":9,  "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":9,  "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":9,  "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "HEALPIX",
    "visible"   : false,
    "type"      : "healpix",
    "baseurl"   : ASTROVIEW.urls.healpix.mastimg.dss,
    
    "zoomTable": [{"fov":     30, "level":2, "rax":   10, "decx":    10},
                  {"fov":     20, "level":3, "rax":    5, "decx":     5},
                  {"fov":     10, "level":4, "rax":    5, "decx":     5},
                  {"fov":      5, "level":5, "rax":    2, "decx":     2},
                  {"fov":    2.5, "level":6, "rax":    1, "decx":     1},
                  {"fov":   1.25, "level":7, "rax":  0.5, "decx":   0.5},
                  {"fov":   0.75, "level":7, "rax":  0.2, "decx":   0.2},
                  {"fov":  0.355, "level":7, "rax":  0.1, "decx":   0.1},
                  {"fov":  0.125, "level":7, "rax":  0.05,"decx":  0.05},
                  {"fov": 0.0625, "level":7, "rax":  0.01,"decx":  0.01},
                  {"fov":0.00001, "level":7, "rax": 0.005,"decx": 0.005}]
},
{
    "name"      : "healpix",
    "visible"   : false,
    "type"      : "healpix",
    "dummy"     : true,
    "baseurl"   : ASTROVIEW.urls.healpix.dummy.image,
    
    "zoomTable": [{"fov":     30, "level":2, "rax":   10, "decx":    10},
                  {"fov":     20, "level":3, "rax":    5, "decx":     5},
                  {"fov":     10, "level":4, "rax":    5, "decx":     5},
                  {"fov":      5, "level":5, "rax":    2, "decx":     2},
                  {"fov":    2.5, "level":6, "rax":    1, "decx":     1},
                  {"fov":   1.25, "level":7, "rax":  0.5, "decx":   0.5},
                  {"fov":   0.75, "level":7, "rax":  0.2, "decx":   0.2},
                  {"fov":  0.355, "level":7, "rax":  0.1, "decx":   0.1},
                  {"fov":  0.125, "level":7, "rax":  0.05,"decx":  0.05},
                  {"fov": 0.0625, "level":7, "rax":  0.01,"decx":  0.01},
                  {"fov":0.00001, "level":7, "rax": 0.005,"decx": 0.005}]
},
{
    "name"      : "toast",
    "visible"   : false,
    "type"      : "toast",
    "dummy"     : true,
    "baseurl"   : ASTROVIEW.urls.toast.dummy.image,        

    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":11, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":12, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":12, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":12, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":12, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":12, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":12, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":12, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":12, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":12, "rax":   0.0001,"decx":    0.0001}]
}];
// stats.js r8 - http://github.com/mrdoob/stats.js
var Stats=function(){var h,a,n=0,o=0,i=Date.now(),u=i,p=i,l=0,q=1E3,r=0,e,j,f,b=[[16,16,48],[0,255,255]],m=0,s=1E3,t=0,d,k,g,c=[[16,48,16],[0,255,0]];h=document.createElement("div");h.style.cursor="pointer";h.style.width="80px";h.style.opacity="0.9";h.style.zIndex="10001";h.addEventListener("mousedown",function(a){a.preventDefault();n=(n+1)%2;n==0?(e.style.display="block",d.style.display="none"):(e.style.display="none",d.style.display="block")},!1);e=document.createElement("div");e.style.textAlign=
"left";e.style.lineHeight="1.2em";e.style.backgroundColor="rgb("+Math.floor(b[0][0]/2)+","+Math.floor(b[0][1]/2)+","+Math.floor(b[0][2]/2)+")";e.style.padding="0 0 3px 3px";h.appendChild(e);j=document.createElement("div");j.style.fontFamily="Helvetica, Arial, sans-serif";j.style.fontSize="9px";j.style.color="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";j.style.fontWeight="bold";j.innerHTML="FPS";e.appendChild(j);f=document.createElement("div");f.style.position="relative";f.style.width="74px";f.style.height=
"30px";f.style.backgroundColor="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";for(e.appendChild(f);f.children.length<74;)a=document.createElement("span"),a.style.width="1px",a.style.height="30px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+b[0][0]+","+b[0][1]+","+b[0][2]+")",f.appendChild(a);d=document.createElement("div");d.style.textAlign="left";d.style.lineHeight="1.2em";d.style.backgroundColor="rgb("+Math.floor(c[0][0]/2)+","+Math.floor(c[0][1]/2)+","+Math.floor(c[0][2]/2)+")";d.style.padding=
"0 0 3px 3px";d.style.display="none";h.appendChild(d);k=document.createElement("div");k.style.fontFamily="Helvetica, Arial, sans-serif";k.style.fontSize="9px";k.style.color="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";k.style.fontWeight="bold";k.innerHTML="MS";d.appendChild(k);g=document.createElement("div");g.style.position="relative";g.style.width="74px";g.style.height="30px";g.style.backgroundColor="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";for(d.appendChild(g);g.children.length<74;)a=document.createElement("span"),
a.style.width="1px",a.style.height=Math.random()*30+"px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+c[0][0]+","+c[0][1]+","+c[0][2]+")",g.appendChild(a);return{domElement:h,update:function(){i=Date.now();m=i-u;s=Math.min(s,m);t=Math.max(t,m);k.textContent=m+" MS ("+s+"-"+t+")";var a=Math.min(30,30-m/200*30);g.appendChild(g.firstChild).style.height=a+"px";u=i;o++;if(i>p+1E3)l=Math.round(o*1E3/(i-p)),q=Math.min(q,l),r=Math.max(r,l),j.textContent=l+" FPS ("+q+"-"+r+")",a=Math.min(30,30-l/
100*30),f.appendChild(f.firstChild).style.height=a+"px",p=i,o=0}}};

ASTROVIEW = ASTROVIEW || {};
/////////////////////////
// Timer
/////////////////////////
ASTROVIEW.Timer = function ( )
{
    this.ticks = undefined;      
    this.timerID = undefined;
};

ASTROVIEW.Timer.prototype = new Object();
ASTROVIEW.Timer.prototype.constructor = ASTROVIEW.Timer;

ASTROVIEW.Timer.prototype.start = function (callback, ticks)
{    
    this.stop();
    this.timerID = window.setInterval(callback, ticks);
    this.ticks = ticks;
};

ASTROVIEW.Timer.prototype.stop = function ()
{    
    if (this.timerID) window.clearTimeout(this.timerID); 
    this.timerID = undefined;
    this.ticks = undefined;  
};

ASTROVIEW.Timer.prototype.isActive = function()
{
    return (this.timerID !== undefined);
};//////////////////////////
// ToastGeometry()
//////////////////////////
ASTROVIEW.ToastGeometry = function(uv, tx, ty, quadrant, zlevel, did, radius, dummy)
{
    THREE.Geometry.call( this );

    this.uv = uv;                                           // Diamond Vertices Unit Vectors 
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.zlevel = zlevel;
    this.did = did;                                         // Diamond ID: 0 - 3
    this.name = "[" + zlevel + ":" + tx + "," + ty + "]";   // Toast ID: [zoom: tx, ty]
    this.radius = radius;
    this.dummy = dummy;                                     // dummy survey image(s) loaded 
    this.dynamic = true;
    
    // Texture Attributes (used by Diamond.js)
    this.side = THREE.BackSide;                             // THREE.FrontSide, THREE.BackSide and THREE.DoubleSide; 
    this.overdraw = (dummy ? false : true);                 // Overdraw image over edge of Diamond to hide lines for *real* image surveys.  
    
    // Create this.vertices[] array from uv[] unit vector3 array
    if (uv && uv.length > 0)
    {
        for (var i=0; i<uv.length; i++)
        {
            // Create new Vertex from the cloned Unit Vector.  Then scale it to the Sphere Radius
            var v = uv[i].clone();
            v.multiplyScalar(radius);   
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length > 0)
    {
        var fuv = this.getFaceUV(quadrant, dummy);
        this.createFaces(this.vertices, fuv);       // create this.faces[] based on this.vertices[] and Face UV[]
        this.computeCentroids();                    // inherited call
    }
};

ASTROVIEW.ToastGeometry.prototype = new THREE.Geometry();
ASTROVIEW.ToastGeometry.prototype.constructor = ASTROVIEW.ToastGeometry;

////////////////////////////////
// FaceUV: Static Arrays
////////////////////////////////
var uv00 = uv00 || new THREE.Vector2( 0, 0 );
var uv01 = uv01 || new THREE.Vector2( 0, 1 );
var uv10 = uv10 || new THREE.Vector2( 1, 0 );
var uv11 = uv11 || new THREE.Vector2( 1, 1 );

ASTROVIEW.ToastGeometry.faceUVNormal = ASTROVIEW.ToastGeometry.faceUVNormal || 
{
    "I"  :[uv10, uv11, uv01, uv00],
    "II" :[uv00, uv10, uv11, uv01],
    "III":[uv01, uv00, uv10, uv11],
    "IV" :[uv11, uv01, uv00, uv10]
}

ASTROVIEW.ToastGeometry.faceUVDummy = ASTROVIEW.ToastGeometry.faceUVDummy || 
{
    "I"  :[uv01, uv11, uv10, uv00],
    "II" :[uv11, uv10, uv00, uv01],
    "III":[uv10, uv00, uv01, uv11],
    "IV" :[uv00, uv01, uv11, uv10]
}

////////////////////////////////
// getFaceUV() 
////////////////////////////////
ASTROVIEW.ToastGeometry.prototype.getFaceUV = function(quadrant, dummy)
{   
    if (dummy)
        return ASTROVIEW.ToastGeometry.faceUVDummy[quadrant];       // Dummy Survey : Flip Image UV Mapping so that label is readable from inside the sphere
    else
        return ASTROVIEW.ToastGeometry.faceUVNormal[quadrant];      // Normal Toast Survey UV Mapping
}

//////////////////////////
// clean()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.clean = function()
{
    if (this.faces && this.faces.length > 1)
    {
        this.cleanFace(this.faces[0]);
        this.cleanFace(this.faces[1]);
    }
    this.faces = undefined;
    this.uv = undefined;
    this.vertices = undefined;
    this.faceVertexUvs = undefined;
}

ASTROVIEW.ToastGeometry.prototype.cleanFace = function(face)
{
    face.normal = undefined;
    face.vertexNormals = undefined;
    face.vertexclears = undefined;
    face.vertexTangents = undefined;
    face.centroid = undefined;
}

// Support color rotation in the dummy tiles so that you can easily see the boundaries.
ASTROVIEW.ToastGeometry.COLORS = [
    "FB0204",
    "FC4604",
    "FC7E04",
    "FCB304",
    "FCFE04",
    "04FE04",
    "046904",
    "04AEAC",
    "0402FC",
    "7406A5",
    "BC02FB",
    "CC02AD",
    "00FFFF",
    "AAAAFF",
    "55FF55",
    "FF5555",
    "5555FF",
    "FFAAAA",
    "FFFF55",
    "AA0000"
];

//////////////////////////
// getImageUrl()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.getImageUrl = function(baseurl)
{
    var url = new String(baseurl).replace("[LEVEL]", this.zlevel).replace("[TX]", this.tx).replace("[TY]", this.ty).replace("[LEVEL]", this.zlevel).replace("[TX]", this.tx).replace("[TY]", this.ty);   
    if (this.dummy)
    {
        var tid=this.zlevel*this.tx + this.ty;
        var color = ASTROVIEW.ToastGeometry.COLORS[tid%ASTROVIEW.ToastGeometry.COLORS.length];
        url = new String(url).replace("[COLOR]", color);
    }
    return url;   
}

//////////////////////////
// createFaces()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.createFaces = function(v, fuv)
{
    if ((v && v.length == 4) && (fuv && fuv.length == 4))
    {
        var normal = new THREE.Vector3( 0, 0, 1 );
        
        //
        // Create 'Upper' Face 0 (v0 -> v3 -> v1)
        //  
        var face0 = new THREE.Face3( 0, 3, 1);
        face0.normal.copy( normal );
        face0.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face0 );
        this.faceVertexUvs[ 0 ].push( [ fuv[0], fuv[3], fuv[1] ]);
        
        //
        // Create 'Lower' Face 1 (v2 -> v1 -> v3)
        //
        var face1 = new THREE.Face3( 2, 1, 3 );
        face1.normal.copy( normal );
        face1.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face1 );
        this.faceVertexUvs[ 0 ].push( [ fuv[2], fuv[1], fuv[3] ]);
    }
};
   
//////////////////////////
// expandDiamond()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.expandDiamond = function(zlevel, radius) 
{ 
    //
    //      Expand each Diamond by number of 'depth' passed in, starting at 'zlevel'
    //
    //      NOTE: Diamond.uv[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //       Make new points on the Diamond (a, b, c, d, e):
    //
    //      a = (uv0+uv1)/2
    //      b = (uv0+uv2)/2
    //      c = (uv2+uv3)/2
    //      d = (uv3+uv0)/2
    //      e = (uv3+uv1)/2
    //
    //          uv0
    //          /\           
    //         /  \
    //      d / D0 \ a         Construct 4 new diamonds    
    //       /\    /\            D0 = [uv0,   a,   e,   d]       
    //      /  \ e/  \           D1 = [  a, uv1,   b,   e]       
    // uv3 / D3 \/ D1 \ uv1      D2 = [  e,   b, uv2,   c]       
    //     \    /\    /          D3 = [  d,   e,   c, uv3]      
    //      \  /  \  /
    //     c \/ D2 \/ b
    //        \    /
    //         \  /
    //          \/
    //          uv2
    //   
    //  NOTE: The above perspective is from INSIDE the sphere with v0 pointing true NORTH
    //   
    
    // Store default args
    if (!zlevel) zlevel = this.zlevel + 1;
    if (!radius) radius = this.radius;
    
    // NOTE: Face order rendering problem in Chrome forced us to ensure each child Diamond is closer to the camera than its parent.
    //       We do this by moving the entire Diamond 10 units closer to the camera.
    radius -= 10;   
    
    //
    // Initialize the outArray which is used as input for looping below
    //
    var outArray = [];
    outArray.push(this);             
    
    //
    // Repeat for each level:
    //
    // Copy the existing output array to the input array
    // Loop through the diamonds in the input array and expand them
    // Store the expanded diamonds on the output array
    //
    // Net affect is only the final level of expanded diamonds are in the outArray
    // Add this expanded level as children to the current instance
    //
    for (var zl=this.zlevel+1; zl <= zlevel; zl++)
    {    
        var loopArray = outArray;
        outArray = [];   
        
        // 
        // Loop through all diamands to expand
        //   
        for (var i=0; i<loopArray.length; i++)
        {            
            var dg = loopArray[i];   // ASTROVIEW.ToastGeometry
            var uv0 = dg.uv[0];
            var uv1 = dg.uv[1];
            var uv2 = dg.uv[2];
            var uv3 = dg.uv[3];
            
            //
            //    Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = new THREE.Vector3().addVectors(uv0, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var b = new THREE.Vector3().addVectors(uv1, uv2).multiplyScalar(0.5).normalize();   // THREE.Vector3 
            var c = new THREE.Vector3().addVectors(uv2, uv3).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var d = new THREE.Vector3().addVectors(uv3, uv0).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var e = new THREE.Vector3().addVectors(uv3, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            
            //
            //   Construct new diamonds
            //
            //    [uv0,  a,   e,   d]
            //    [ a, uv1,   b,   e]
            //    [ e,   b, uv2,   c]
            //    [ d,   e,   c, uv3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.ToastGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 0, radius, this.dummy); // (x, 0)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 1, radius, this.dummy); // (x, y)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 2, radius, this.dummy); // (0, y)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 3, radius, this.dummy); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 0, radius, this.dummy); // (0, 0)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 1, radius, this.dummy); // (x, 0)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 2, radius, this.dummy); // (x, y)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 3, radius, this.dummy); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 0, radius, this.dummy); // (0, y)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 1, radius, this.dummy); // (0, 0)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 2, radius, this.dummy); // (x, 0)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 3, radius, this.dummy); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 0, radius, this.dummy); // (x, y)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 1, radius, this.dummy); // (0, y)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 2, radius, this.dummy); // (0, 0)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 3, radius, this.dummy); // (x, 0)
                    break;
                } 
            }
            
            //
            // Add the diamonds to the returned array
            //
            outArray.push(dg0);
            outArray.push(dg1);
            outArray.push(dg2);
            outArray.push(dg3);
        }
    }    
    
    return outArray;
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.inFrustum = function(frustum)
{
    var faces = this.facesInFrustum(frustum);
    return (faces); 
};

//////////////////////////
// facesInFrustrum()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.facesInFrustum = function(frustum)
{
    var inFrustum = this.faceInFrustum(this.faces[0], frustum) ||
                    this.faceInFrustum(this.faces[1], frustum);
                      
    return (inFrustum);
}

//////////////////////////
// faceInFrustrum()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.faceInFrustum = function(face, frustum)
{
    // THREE.Face3
    if (!face.__radius) face.__radius = this.computeFaceRadius(face);
    var sphere = {"center":face.centroid, "radius":face.__radius};
    return frustum.intersectsSphere(sphere);
};

//////////////////////////
// computeFaceRadius()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.computeFaceRadius = function(face)
{
    var v0 = this.vertices[face.a];
    var v1 = this.vertices[face.b];
    var v2 = this.vertices[face.c];

    var dv0 = face.centroid.distanceTo(v0);
    var dv1 = face.centroid.distanceTo(v1);
    var dv2 = face.centroid.distanceTo(v2);

    return Math.max(dv0, dv1, dv2);            
}/////////////////////////////
// ToastSphere()
/////////////////////////////
ASTROVIEW.ToastSphere = function ( radius, survey )
{ 
    THREE.Object3D.call( this );
    this.name = "ToastSphere";
    this.matrixAutoUpdate = false;
    this.createSphere( radius, survey );
};

ASTROVIEW.ToastSphere.prototype = new THREE.Object3D();
ASTROVIEW.ToastSphere.prototype.constructor = ASTROVIEW.ToastSphere;

/////////////////////////////
// createToastSphere3D()
/////////////////////////////
ASTROVIEW.ToastSphere.prototype.createSphere = function( radius, survey )
{
    //
    // 3D Quadrant (I, II, III, IV) ====> TOAST [tx,ty] mapping
    //
    ///////////////////////////////////////////////////
    //
    //                        -X
    //                         ^                    
    //                IV       |      III
    //              [0,0]      |     [1,0]
    //                         |
    //   <)  +Z <--------------+------------> -Z
    //  eye                    |
    //                I        |      II
    //              [0,1]      |     [1,1]
    //                         V  
    //                        +X
    //
    ////////////////////////////////////////////////////
    var zlevel = 1;
    
    //
    // STEP 1: Create Unit Vectors for the Unit Sphere
    //
    var YY = new THREE.Vector3(0.0,  1.0,  0.0); // +Y
    var _Y = new THREE.Vector3(0.0, -1.0,  0.0); // -Y
                    
    var XX = new THREE.Vector3( 1.0,  0.0,  0.0); // +X
    var _X = new THREE.Vector3(-1.0,  0.0,  0.0); // -X  

    var ZZ = new THREE.Vector3(0.0,  0.0,  1.0); // +Z
    var _Z = new THREE.Vector3(0.0,  0.0, -1.0); // +Z
    
    //
    // STEP 2: Create the Top Level ToastGeometry located in each 3D Qudrant (I, II, III, IV),
    //         mapped to a TOAST Image Coordinate [tx, ty] as shown in the Mapping above.
    //
    // Quadrant I: [+X,+Z] ===> TOAST: [0,1] 
    var dgI = new ASTROVIEW.ToastGeometry([YY, XX, _Y, ZZ], 0, 1, "I", zlevel, 1, radius, survey.dummy);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.ToastGeometry([YY, _Z, _Y, XX], 1, 1, "II", zlevel, 0, radius, survey.dummy);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.ToastGeometry([YY, _X, _Y, _Z], 1, 0, "III", zlevel, 2, radius, survey.dummy);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.ToastGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", zlevel, 3, radius, survey.dummy);
    
    //
    // STEP 3: Expand Each Top Level ToastGeometry Object to Level 4 Array of ToastGeometry[] objects
    //
    var zlevel = 4;       // expand...to zlevel '4'

    var dgsI = dgI.expandDiamond(zlevel, ASTROVIEW.RADIUS);            // Quadrant I
    var dgsII = dgII.expandDiamond(zlevel, ASTROVIEW.RADIUS);          // Quadrant II
    var dgsIII = dgIII.expandDiamond(zlevel, ASTROVIEW.RADIUS);        // Quadrant III
    var dgsIV = dgIV.expandDiamond(zlevel, ASTROVIEW.RADIUS);          // Quadrant IV      
    
    //
    // STEP 4: Create Diamond objects from the ToastGeometry[] array and
    //         add them as children to the ToastSphere
    //
    this.createDiamonds(dgsI, null); 
    this.createDiamonds(dgsII, null);
    this.createDiamonds(dgsIII, null);
    this.createDiamonds(dgsIV, null);
}

/////////////////////////////
// createDiamondMeshMaterial()
/////////////////////////////
ASTROVIEW.ToastSphere.prototype.createDiamonds = function(dgs, material)
{
    // Loop through THREE.ToastGeometry Objects and create a Diamond for each one.
    for (i=0; i<dgs.length; i++)
    { 
        var dg = dgs[i];
        var ddd = new ASTROVIEW.Diamond( dgs[i], material );
        this.add(ddd);
    }
}

/////////////////////////////
// renderScene()
/////////////////////////////
ASTROVIEW.ToastSphere.prototype.renderScene = function( av )
{
    if (this.children && this.children.length > 0)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var ddd = this.children[i]; // ASTROVIEW.Diamond
            if (ddd instanceof ASTROVIEW.Diamond)
            {
                ddd.renderScene(av);
            }
        }
    }
}

/////////////////////////////
// clearScene()
/////////////////////////////
ASTROVIEW.ToastSphere.prototype.clearScene = function( av )
{
    if (this.children && this.children.length > 0)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var ddd = this.children[i]; // ASTROVIEW.Diamond
            if (ddd instanceof ASTROVIEW.Diamond)
            {
                ddd.clearScene(av);
            }
        }
    }
}


////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

// M101 Footprint Layer
ASTROVIEW.M101 = 
{"name":"0-unselected","type":"footprint","canvas":undefined,
"attribs":{"color":"0xFF6600","stroke":1,"symbol":"square"},
"rows":[
{"footprint":"Circle J2000 210.8029717291 54.348920041 0.00300694444444","_id_":"ext-record-1231"},
{"footprint":"Circle J2000 210.802215085 54.343618117 0.00300694444444","_id_":"ext-record-1232"},
{"footprint":"Circle J2000 210.8071128496 54.3536303516 0.00300694444444","_id_":"ext-record-1233"},
{"footprint":"Circle J2000 210.8088029038 54.3536345426 0.00300694444444","_id_":"ext-record-1234"},
{"footprint":"Circle J2000 210.7769169912 54.3499554757 0.00300694444444","_id_":"ext-record-1235"},
{"footprint":"Circle J2000 210.7840826411 54.3352730976 0.00300694444444","_id_":"ext-record-1236"},
{"footprint":"Circle J2000 210.7291231863 54.3737373665 0.00300694444444","_id_":"ext-record-1237"},
{"footprint":"Circle J2000 210.7291231863 54.3737373665 0.00300694444444","_id_":"ext-record-1238"},
{"footprint":"Circle J2000 210.9221001311 54.3181156448 0.00300694444444","_id_":"ext-record-1239"},
{"footprint":"Circle J2000 210.9221001311 54.3181156448 0.00300694444444","_id_":"ext-record-1240"},
{"footprint":"Circle J2000 210.9221001311 54.3181156448 0.00300694444444","_id_":"ext-record-1241"},
{"footprint":"Circle J2000 210.9167864764 54.3106023567 0.00300694444444","_id_":"ext-record-1242"},
{"footprint":"Circle J2000 210.9167864764 54.3106023567 0.00300694444444","_id_":"ext-record-1243"},
{"footprint":"Circle J2000 210.9228976563 54.3178176265 0.00300694444444","_id_":"ext-record-1244"},
{"footprint":"Circle J2000 210.9233947383 54.3178188633 0.00300694444444","_id_":"ext-record-1245"},
{"footprint":"Circle J2000 210.9702671287 54.3657360088 0.00300694444444","_id_":"ext-record-1246"},
{"footprint":"Circle J2000 210.7545405861 54.2415990823 0.00300694444444","_id_":"ext-record-1247"},
{"footprint":"Circle J2000 210.7545427611 54.241299085 0.00300694444444","_id_":"ext-record-1248"},
{"footprint":"Circle J2000 210.6169841804 54.2912596625 0.00300694444444","_id_":"ext-record-1249"},
{"footprint":"Circle J2000 211.1191825023 54.3944081295 0.00300694444444","_id_":"ext-record-1250"},
{"footprint":"Circle J2000 211.1191825023 54.3944081295 0.00300694444444","_id_":"ext-record-1251"},
{"footprint":"Circle J2000 211.1191825023 54.3944081295 0.00300694444444","_id_":"ext-record-1252"},
{"footprint":"Circle J2000 211.1198614317 54.3967098499 0.00300694444444","_id_":"ext-record-1253"},
{"footprint":"Circle J2000 211.1199608476 54.3967100987 0.00300694444444","_id_":"ext-record-1254"},
{"footprint":"Circle J2000 211.1199608476 54.3967100987 0.00300694444444","_id_":"ext-record-1255"},
{"footprint":"Circle J2000 211.1199608476 54.3967100987 0.00300694444444","_id_":"ext-record-1256"},
{"footprint":"Circle J2000 211.1211523616 54.3969130825 0.00300694444444","_id_":"ext-record-1257"},
{"footprint":"Circle J2000 211.1220471048 54.3969153219 0.00300694444444","_id_":"ext-record-1258"},
{"footprint":"Circle J2000 211.1232400959 54.3969183077 0.00300694444444","_id_":"ext-record-1259"},
{"footprint":"Circle J2000 211.123635544 54.3972193003 0.00300694444444","_id_":"ext-record-1260"},
{"footprint":"POLYGON ICRS -149.14680500097128 54.354787000719213  -149.16077479138016 54.363261669101696  210.826864 54.370757  -149.17324108970206 54.370698203660048  -149.1909342494699 54.381222063036439  210.802612 54.385059  -149.22132773990666 54.37139192248123  210.778446 54.371525  -149.22155460449795 54.371524650679071  210.778438 54.371529  -149.22156947029239 54.371524690391659  -149.22156999913989 54.371525000349465  -149.24853000040744 54.355964999640719  -149.24852174822536 54.355960147591738  -149.2485220008447 54.355960000214054  -149.24841946890092 54.355899700387027  -149.22463710980324 54.341904869625239  -149.22355586968075 54.341268269190273  -149.22180099932572 54.340234999211823  -149.20807292108196 54.348157920041352  -149.19759199927836 54.341873999383559  -149.18755127485085 54.347565726375343  -149.17798339487484 54.341760745556662  -149.17418599917 54.339455999210024  -149.14681299820433 54.354782000501977  -149.14681344865954 54.354782273793049  -149.14680500097128 54.354787000719213","_id_":"ext-record-1261"},
{"footprint":"POLYGON ICRS -149.14680500097435 54.354787000710424  -149.16076839429849 54.363257789518691  210.826864 54.370757  -149.17324108970513 54.370698203651251  -149.19093424947297 54.381222063027636  210.802612 54.385059  -149.2213277399097 54.371391922472434  210.778446 54.371525  -149.22155460450102 54.371524650670281  210.778438 54.371529  -149.22156947035336 54.371524690398374  -149.22156999913989 54.371525000349465  -149.24853000041051 54.355964999631915  -149.24852174947421 54.355960146081735  -149.24852200084774 54.355960000205251  210.75158053 54.3558997  210.77536289 54.34190487  210.77644413 54.34126827  210.778199 54.340235  210.79192708 54.34815792  210.802408 54.341874  -149.18755127485085 54.347565726375329  -149.17798339487484 54.341760745556655  210.825814 54.339456  -149.14681299820739 54.354782000493181  -149.14681344866261 54.354782273784245  -149.14680500097435 54.354787000710424","_id_":"ext-record-1262"},
{"footprint":"POLYGON ICRS -149.14680499838644 54.354786999074911  -149.16076756569294 54.363257287354  -149.17313599855021 54.370756999938386  -149.17324109003806 54.370698203750564  -149.17746844081307 54.373213349789168  -149.19093424959522 54.381222063061514  -149.19738800134303 54.385058999824224  -149.221327738345 54.371391922200338  -149.22155400008637 54.371525000354488  -149.22155460609133 54.37152465078838  -149.22156200108566 54.3715290002707  -149.22156947081268 54.371524691571594  -149.22156999900417 54.371525000383166  -149.24852999932389 54.355965000065481  -149.24852174717915 54.355960147994736  -149.24852199979958 54.355960000617713  -149.24841946884902 54.355899700417531  -149.22463710975134 54.341904869655721  -149.22355586962885 54.341268269220762  -149.2218009992738 54.340234999242305  -149.20807292103004 54.348157920071827  -149.19759199922643 54.341873999414027  -149.18755127484621 54.347565726429337  -149.17798339487015 54.341760745610678  -149.17418599911804 54.339455999240485  -149.14681299823232 54.354781999606928  -149.14681344783045 54.354782273212251  -149.14680499838644 54.354786999074911","_id_":"ext-record-1263"},
{"footprint":"POLYGON ICRS -149.20219800024367 54.322603000657509  210.810951 54.343375  -149.18904900178768 54.343374999974834  -149.18904900154814 54.343375000046564  -149.1891864194944 54.343404579070267  -149.17756200042885 54.362333999766939  -149.20911157944454 54.368914949623239  -149.20900099936046 54.369098000725053  -149.24479100010319 54.376436000782221  -149.24483989152159 54.376355059644467  -149.25605781094134 54.357776740629994  -149.25656689076365 54.3569332309831  -149.25739500068426 54.355561000394047  -149.23917208127727 54.3518241695633  -149.24429400102505 54.343585000501477  210.76887105 54.34080811  -149.23780599942057 54.330260000167527  -149.20219800024367 54.322603000657509","_id_":"ext-record-1264"},
{"footprint":"POLYGON ICRS -149.20219800024367 54.322603000657509  210.810951 54.343375  -149.18904900178768 54.343374999974834  -149.18904900154814 54.343375000046564  -149.1891864194944 54.343404579070267  -149.17756200042885 54.362333999766939  -149.20911157944454 54.368914949623239  -149.20900099936046 54.369098000725053  -149.24479100010319 54.376436000782221  -149.24483989152159 54.376355059644467  -149.25605781094134 54.357776740629994  -149.25656689076365 54.3569332309831  -149.25739500068426 54.355561000394047  -149.23917208127727 54.3518241695633  -149.24429400102505 54.343585000501477  210.76887105 54.34080811  -149.23780599942057 54.330260000167527  -149.20219800024367 54.322603000657509","_id_":"ext-record-1265"},
{"footprint":"POLYGON ICRS -149.18514300031507 54.348931000012385  -149.19474983377123 54.354688105617335  210.802747 54.356188  -149.20545725531082 54.351538897362445  -149.20970100029871 54.349134000135543  -149.20969282448348 54.349129099751067  -149.20969300048932 54.349128999816621  -149.19759200039653 54.341873999886182  -149.18515100018814 54.348926000042916  -149.18515140025383 54.348926239877088  -149.18514300031507 54.348931000012385","_id_":"ext-record-1266"},
{"footprint":"POLYGON ICRS -149.18514299979321 54.34893099996858  -149.19474983214042 54.354688104736745  210.802747 54.356188  -149.20546011323287 54.351537277927854  -149.20970100042086 54.349134000260428  -149.20969282396189 54.349129099694267  -149.20969300061148 54.349128999941506  210.802408 54.341874  -149.1851510003099 54.348926000180938  -149.18515139990689 54.348926239734148  -149.18514299979321 54.34893099996858","_id_":"ext-record-1267"},
{"footprint":"POLYGON ICRS -149.18514299978605 54.3489310000787  -149.19475520154182 54.354691321873524  -149.19725299969525 54.356187999899561  -149.20545912962865 54.35153783541228  -149.20970100017706 54.34913400034258  -149.20969282438534 54.349129099944776  -149.20969300007997 54.349129000385823  -149.20969299952469 54.349129000005135  -149.19759199997111 54.341874000166882  -149.1851509999951 54.348925999747145  -149.18515100056038 54.348925999938466  -149.18515100025334 54.348926000112463  -149.18515139982458 54.348926239627517  -149.18514299978605 54.3489310000787","_id_":"ext-record-1268"},
{"footprint":"POLYGON ICRS -149.20141909976584 54.328754700685721  -149.24306930134622 54.38001519975807  -149.28590970009657 54.366003100780532  -149.24422020102898 54.314757300483492  -149.20141909976584 54.328754700685721POLYGON ICRS  -149.15987680078263 54.342917400106714  -149.19983489841454 54.393910000243174  -149.24109769969505 54.380196500184105  -149.24109740918652 54.380196130409104  -149.24109779982282 54.380196000473916  210.7988975 54.3292179  -149.2011020518627 54.329218049672285  -149.20110169975246 54.329217599068336  -149.15987680078263 54.342917400106714","_id_":"ext-record-1269"},
{"footprint":"POLYGON ICRS -149.20141909976584 54.328754700685721  -149.24306930134622 54.38001519975807  -149.28590970009657 54.366003100780532  -149.24422020102898 54.314757300483492  -149.20141909976584 54.328754700685721POLYGON ICRS  -149.15987680078263 54.342917400106714  -149.19983489841454 54.393910000243174  -149.24109769969505 54.380196500184105  -149.24109740918652 54.380196130409104  -149.24109779982282 54.380196000473916  210.7988975 54.3292179  -149.2011020518627 54.329218049672285  -149.20110169975246 54.329217599068336  -149.15987680078263 54.342917400106714","_id_":"ext-record-1270"},
{"footprint":"POLYGON ICRS -149.19389610105526 54.348272700202891  210.7895937 54.3742161  -149.21040639125636 54.374216084546823  -149.21040640099432 54.374216099669624  -149.25846496382928 54.366595584520489  -149.30325329960652 54.359473899600481  210.7133097 54.3335432  -149.28669020757266 54.333543214648138  -149.28669019866265 54.333543200520161  -149.19389610105526 54.348272700202891POLYGON ICRS  -149.17645869973703 54.321098799570713  -149.193164399287 54.348014199982615  -149.28670149859357 54.332380400220885  210.730059 54.3054779  -149.26994091087997 54.305477914584628  -149.26994090165135 54.305477899588745  -149.22479568929904 54.313032188357049  -149.17645869973703 54.321098799570713","_id_":"ext-record-1271"},
{"footprint":"POLYGON ICRS -149.19389800133089 54.348272099369183  -149.19389800208393 54.3482721005533  -149.19389800008369 54.348272100869927  -149.21040740011415 54.374214999742691  -149.30324949934118 54.359473299899747  -149.28668749876547 54.333543100228709  -149.2866874093931 54.3335431143935  210.7133126 54.3335431  -149.20104632366545 54.347140310985765  -149.19389800133089 54.348272099369183POLYGON ICRS  -149.17646019952468 54.3210985994007  -149.19316540173398 54.348013799713662  -149.28670000072498 54.332380601031744  -149.26993990002953 54.30547829928237  -149.17646019952468 54.3210985994007","_id_":"ext-record-1272"},
{"footprint":"POLYGON ICRS -149.1938992995324 54.348269999655216  -149.21040879982559 54.3742115998726  -149.3032465995183 54.359471999239794  -149.28668449978957 54.33354309990802  -149.28668440971538 54.333543114147822  210.7133156 54.3335431  -149.193899401037 54.348269900465837  -149.19389944906663 54.348269975986923  -149.1938992995324 54.348269999655216POLYGON ICRS  -149.17646250054503 54.3210986995144  -149.19316729876539 54.34801220045766  -149.19316739129778 54.3480121849352  210.8068326 54.3480122  -149.23994139047909 54.34020538988085  -149.28669770093452 54.332380499656225  -149.26993809977958 54.305479900743329  -149.26993800999261 54.305479915684437  210.730062 54.3054799  -149.22320916865488 54.31329833235737  -149.17646250054503 54.3210986995144","_id_":"ext-record-1273"},
{"footprint":"POLYGON ICRS -149.19389610041719 54.348272700154354  210.7895937 54.3742161  -149.21040639125636 54.374216084546823  -149.21040640099432 54.374216099669624  -149.25846496382928 54.366595584520489  -149.30325330189851 54.359473900412787  210.7133097 54.3335432  -149.28669020757266 54.333543214648138  -149.28669019866265 54.333543200520161  -149.28668780759315 54.333543580523184  210.7133125 54.3335431  -149.28668740847931 54.33354311479507  210.7133126 54.3335431  -149.28668476696873 54.333543519425383  210.7133155 54.3335431  -149.28668440975483 54.3335431138826  210.7133156 54.3335431  -149.19389940008153 54.348269900085839  -149.19389944886487 54.348269976791954  -149.19389929733177 54.348270000776637  -149.19390039264681 54.348271720014274  -149.19389799784102 54.348272100038521  -149.19389817136144 54.348272373219991  -149.19389610041719 54.348272700154354POLYGON ICRS  -149.17645870235791 54.321098799566009  -149.193164399287 54.348014199982615  -149.2867015002991 54.332380401167129  210.730059 54.3054779  -149.26994091087997 54.305477914584628  -149.26994090165135 54.305477899588745  -149.22479568929904 54.313032188357049  -149.17645870235791 54.321098799566009","_id_":"ext-record-1274"},
{"footprint":"POLYGON ICRS -149.10991110104624 54.365823399665146  -149.10991117376651 54.365823482348475  -149.10991110040587 54.365823499604815  -149.13158189917468 54.39044659908199  -149.22081120045667 54.369407899637473  -149.19909799855785 54.344800800233237  -149.19909791624281 54.344800819552432  210.8009021 54.3448008  -149.10991110104624 54.365823399665146POLYGON ICRS  -149.08707549994219 54.340042199645822  -149.1091316992763 54.365617200497631  -149.10913178351367 54.365617179787726  210.8908682 54.3656172  -149.15294075302 54.354902504063581  -149.19887329949302 54.343645400879169  -149.17677300003217 54.31808669977417  -149.08707549994219 54.340042199645822","_id_":"ext-record-1275"},
{"footprint":"POLYGON ICRS -149.10991300124059 54.36582260012274  -149.13158269910795 54.390445299749793  -149.2208071006738 54.369407500147396  -149.19909499976887 54.344800900253446  -149.19909491826584 54.344800919609874  210.8009051 54.3448009  -149.10991300124059 54.36582260012274POLYGON ICRS  -149.08707720268765 54.34004189915597  -149.1091328008352 54.365616600400244  -149.10913288377733 54.3656165802553  210.8908671 54.3656166  -149.15278899789135 54.354939430346882  -149.1988716022399 54.343645700692029  -149.17677199874282 54.318087300404059  -149.17677191754552 54.318087320439069  210.8232281 54.3180873  -149.13179217204691 54.329108258946761  -149.08707720268765 54.34004189915597","_id_":"ext-record-1276"},
{"footprint":"POLYGON ICRS -149.10991389942748 54.365820399308141  -149.13158339858137 54.390441799886446  -149.22080389997845 54.369406400455922  -149.19909199972824 54.344801100488567  -149.10991389942748 54.365820399308141POLYGON ICRS  -149.08707950042611 54.340041799514474  -149.10913439959216 54.365614999599849  -149.19886930091479 54.343645800652034  -149.17677040021209 54.318088900514084  -149.08707950042611 54.340041799514474","_id_":"ext-record-1277"},
{"footprint":"POLYGON ICRS -149.10991110124459 54.365823400337121  -149.10991117321595 54.365823481904485  -149.10991110060402 54.365823500276775  -149.13158189917468 54.39044659908199  -149.22081119935666 54.369407899038642  -149.19909799855785 54.344800800233237  -149.19909791624281 54.344800819552432  210.8009021 54.3448008  -149.1990954263286 54.344801383445251  210.800905 54.3448009  -149.19909491669813 54.344800920088453  210.8009051 54.3448009  -149.19909235417609 54.344801501058392  -149.19909200023449 54.344801099677269  -149.10991390075748 54.365820399682569  -149.10991534929772 54.365822046665571  -149.10991300087147 54.365822599655331  -149.10991325868832 54.365822893306195  -149.10991110124459 54.365823400337121POLYGON ICRS  -149.08707549934604 54.340042200172412  -149.1091316992763 54.365617200497631  -149.10913178351367 54.365617179787726  210.8908682 54.3656172  -149.15294075302 54.354902504063581  -149.19887329731517 54.343645400780638  -149.17677300003217 54.31808669977417  -149.08707549934604 54.340042200172412","_id_":"ext-record-1278"},
{"footprint":"POLYGON ICRS -149.14588719916392 54.333943900447842  210.8955019 54.3845502  -149.10450074277671 54.384550771266895  210.8955001 54.3845518  -149.14905699979371 54.394166799900376  -149.14910723527342 54.394105373759857  -149.14921869906487 54.394129400237432  -149.19056269988585 54.343503999940765  -149.14604989745047 54.333904199615134  -149.14604890859866 54.333905410041034  -149.14604839861545 54.333905300891828  -149.14599842309744 54.333966488805267  -149.14588869800465 54.333942799173144  -149.1458877087517 54.3339440104627  -149.14588719916392 54.333943900447842POLYGON ICRS  -149.09849320088117 54.323025800608427  210.9422548 54.3745388  -149.1036852996391 54.384707101395477  -149.10373447921057 54.384644921152542  -149.10384609825638 54.384669598872691  -149.14454699950505 54.333139999261867  -149.09865339887457 54.32298709837535  -149.09860352721546 54.323050235268511  -149.09849320088117 54.323025800608427","_id_":"ext-record-1279"},
{"footprint":"POLYGON ICRS -149.14588869984954 54.333942799316631  210.8955001 54.3845518  -149.14905700103034 54.394166799496965  -149.14910723472653 54.394105373704058  -149.14921869848041 54.394129400208222  -149.19056269988585 54.343503999940765  -149.14604989874485 54.333904198635196  -149.14599893668091 54.333966599722224  -149.14588869984954 54.333942799316631POLYGON ICRS  -149.09849320059578 54.323025800910195  210.9422548 54.3745388  -149.10368529951566 54.3847071001064  -149.10373448084064 54.384644920871892  -149.10384609889289 54.384669598680574  -149.14454699950505 54.333139999261867  -149.09865339936673 54.322987099776014  -149.09860352716919 54.323050235545807  -149.09849320059578 54.323025800910195","_id_":"ext-record-1280"},
{"footprint":"POLYGON ICRS -149.14588719949271 54.333943900512033  210.8955019 54.3845502  -149.14905399873476 54.394164399749705  -149.14910434382261 54.394102841776416  -149.14921550001665 54.394126800436084  -149.19055980107456 54.34350439946661  -149.14604840096646 54.333905300560922  -149.14599743721186 54.333967699658274  -149.14588719949271 54.333943900512033POLYGON ICRS  -149.09849390119012 54.323027299854985  210.9422537 54.3745381  -149.10368459852171 54.384705600286757  -149.10373383211683 54.384643357012571  -149.10384530126905 54.384668001039493  -149.1445457999443 54.333140800836112  -149.09865420100323 54.322988599565022  -149.09860431349492 54.323051752510807  -149.09849390119012 54.323027299854985","_id_":"ext-record-1281"},
{"footprint":"POLYGON ICRS -149.14587759887897 54.333949800193359  210.8955114 54.3845559  -149.10449125776921 54.3845564745015  210.8955095 54.3845574  -149.149047199323 54.394172299979864  -149.1490972834774 54.394111055356845  -149.14920930174361 54.394135199633737  -149.19055330056668 54.34350980057193  -149.14604050114033 54.333909999547373  -149.14603959465512 54.333911107240141  -149.1460391013575 54.333911000960676  -149.14598890979127 54.33397245210945  -149.14587890073602 54.333948698765425  -149.14587794090551 54.333949874033  -149.14587759887897 54.333949800193359POLYGON ICRS  -149.09848370270419 54.32303180140952  210.942264 54.3745445  -149.10367579892775 54.384712699161526  -149.10372484732633 54.384650691298916  -149.10383660140045 54.384675397992872  -149.14453759945377 54.333145799655711  -149.09864400312111 54.322992900158305  -149.09859398151451 54.323056224452806  -149.09848370270419 54.32303180140952","_id_":"ext-record-1282"},
{"footprint":"POLYGON ICRS -149.14587889935413 54.333948699452108  210.8955095 54.3845574  -149.14904719991091 54.394172300848126  -149.14909728593477 54.394111055696733  -149.1492093005044 54.394135200878978  -149.19055330056668 54.34350980057193  -149.1460404992099 54.333909999113395  -149.1459894078489 54.333972558366845  -149.14587889935413 54.333948699452108POLYGON ICRS  -149.09848370044097 54.323031800755217  210.942264 54.3745445  -149.1036758007277 54.384712699874711  -149.10372484711303 54.384650691148643  -149.10383660138115 54.3846753990738  -149.14453759945377 54.333145799655711  -149.09864400163949 54.322992900603609  -149.09859398033885 54.323056224973222  -149.09848370044097 54.323031800755217","_id_":"ext-record-1283"},
{"footprint":"POLYGON ICRS -149.14587760085294 54.33394979949319  210.8955114 54.3845559  -149.14904439865686 54.39417009982067  -149.14909458156978 54.394108739964324  -149.14920619929308 54.394132798295182  -149.19055069981209 54.34351010004422  -149.14603909896306 54.333910999910884  -149.14598795230225 54.333973623052934  -149.14587760085294 54.33394979949319POLYGON ICRS  -149.09848439894151 54.323033199994391  210.942263 54.3745438  -149.10367510012068 54.3847112994581  -149.10372421369723 54.384649208004234  -149.10383590018316 54.384673900218147  -149.14453649843182 54.333146500407544  -149.0986447001448 54.322994299841838  -149.09859467800086 54.323057623193165  -149.09848439894151 54.323033199994391","_id_":"ext-record-1284"},
{"footprint":"POLYGON ICRS -149.14588029894091 54.333941199710949  210.8955079 54.3845496  -149.14904859959293 54.3941643990526  -149.14909863039944 54.394103221195046  -149.1492108008087 54.394127399820512  -149.19055469935975 54.343502199626776  -149.14604200039071 54.333902400232354  -149.14599082511475 54.333965062409369  -149.14588029894091 54.333941199710949POLYGON ICRS  -149.09848540094805 54.323024400725949  210.942262 54.3745367  -149.10367750018753 54.384704800765114  -149.10372646342807 54.384642897181543  -149.10383819769874 54.384667600556604  -149.14453910160793 54.333138199664496  -149.098645602432 54.322985299844092  -149.0985954612066 54.323048776219828  -149.09848540094805 54.323024400725949","_id_":"ext-record-1285"},
{"footprint":"POLYGON ICRS -149.14587899890023 54.333942199419745  210.8955098 54.3845481  -149.1044928576479 54.384548674094638  210.8955079 54.3845496  -149.14904860144708 54.394164397819942  -149.14909862929795 54.394103220699726  -149.14921080066452 54.394127398982036  -149.19055469935975 54.343502199626776  -149.14604199961943 54.333902398844657  -149.14604111194575 54.333903487764843  -149.14604069957809 54.333903398834487  -149.14599040942187 54.333964972219015  -149.14588029894676 54.333941199431649  -149.145879409814 54.333942288131951  -149.14587899890023 54.333942199419745POLYGON ICRS  -149.09848540051624 54.323024399556388  210.942262 54.3745367  -149.10367750082932 54.384704799686645  -149.10372646155733 54.38464289679645  -149.10383819633438 54.3846675998815  -149.14453910160793 54.333138199664496  -149.09864560253527 54.322985299804508  -149.09859546103559 54.323048775517378  -149.09848540051624 54.323024399556388","_id_":"ext-record-1286"},
{"footprint":"POLYGON ICRS -149.14587579970029 54.333945499941159  210.8955134 54.3845517  -149.10448932696997 54.384552288730553  210.8955115 54.3845533  -149.1490455011708 54.3941683010914  -149.1490957491055 54.394106856226891  -149.14920730034723 54.394130900019896  -149.19055140118533 54.343505600436217  -149.14603850113923 54.333905699914517  -149.146037512287 54.333906910340311  -149.14603699973105 54.333906800462742  -149.1459869255859 54.333968110604687  -149.1458771003081 54.3339444000133  -149.14587614170506 54.333945573776866  -149.14587579970029 54.333945499941159POLYGON ICRS  -149.09848169890148 54.323027500804777  210.9422662 54.3745403  -149.10367390107592 54.384708601495063  -149.10372306439032 54.384646440752647  -149.10383460109733 54.384671098878684  -149.14453560061054 54.333141499310642  -149.0986420018736 54.322988600283836  -149.0985919802711 54.323051924578046  -149.09848169890148 54.323027500804777","_id_":"ext-record-1287"},
{"footprint":"POLYGON ICRS -149.14587709917558 54.333944400187285  210.8955115 54.3845533  -149.14904549942779 54.394168300719372  -149.14909574864063 54.394106855805028  -149.14920729935972 54.394130900751016  -149.19055140118533 54.343505600436217  -149.14603850203096 54.333905699694789  -149.14598744025051 54.333968222846018  -149.14587709917558 54.333944400187285POLYGON ICRS  -149.09848169945909 54.323027500366308  210.9422662 54.3745403  -149.10367390054856 54.384708600966178  -149.10372306707916 54.384646440248638  -149.10383459875473 54.384671098716851  -149.14453560061054 54.333141499310642  -149.09864200064152 54.322988600215652  -149.09859197949888 54.323051924507006  -149.09848169945909 54.323027500366308","_id_":"ext-record-1288"},
{"footprint":"POLYGON ICRS -149.14587899944914 54.333942200438422  210.8955098 54.3845481  -149.14904570072238 54.394162199304944  -149.14909580196036 54.394100939380756  -149.14920789866042 54.394125100908745  -149.19055229987134 54.343502499807585  -149.14604070215324 54.333903399461505  -149.14598952365628 54.333966061453154  -149.14587899944914 54.333942200438422POLYGON ICRS  -149.09848609982308 54.32302569915138  210.9422611 54.374536  -149.10367669999539 54.384703499302248  -149.10372574590554 54.384641493330705  -149.10383750193236 54.384666200725263  -149.14453810073928 54.333138900391937  -149.09864640042011 54.322986700552867  -149.09859631223057 54.32305010750926  -149.09848609982308 54.32302569915138","_id_":"ext-record-1289"},
{"footprint":"POLYGON ICRS -149.14587579973602 54.3339454993269  210.8955134 54.3845517  -149.14904249981331 54.394165900261555  -149.14909272063656 54.394104494197578  -149.14920409863802 54.394128500919741  -149.19054850076191 54.343505900510678  -149.14603699796911 54.33390680022513  -149.14598596586964 54.333969283126542  -149.14587579973602 54.3339454993269POLYGON ICRS  -149.09848239959518 54.323028899430561  210.9422651 54.3745396  -149.10367310038146 54.384707199533864  -149.10372234864161 54.384644937718647  -149.10383389918564 54.384669599686468  -149.14453440023405 54.33314229987004  -149.09864269885412 54.32299010071808  -149.09859274534196 54.323053337346  -149.09848239959518 54.323028899430561","_id_":"ext-record-1290"},
{"footprint":"POLYGON ICRS -149.14586990003531 54.333947300579943  210.8955187 54.3845561  -149.14903820017224 54.394171000575248  -149.14908832648499 54.394109706038755  -149.14920009889875 54.394133799073067  -149.1905442004192 54.343508400982444  -149.14603140140247 54.333908600176571  -149.1459803247609 54.333971141406273  -149.14586990003531 54.333947300579943POLYGON ICRS  -149.09847459912649 54.323030399617465  210.9422732 54.3745431  -149.10366669965228 54.38471139973219  -149.10371581220272 54.384649307386255  -149.10382749972322 54.384674000503963  -149.14452839846288 54.333144400140142  -149.09863480061225 54.322991500862209  -149.09858479513295 54.323054805201913  -149.09847459912649 54.323030399617465","_id_":"ext-record-1291"},
{"footprint":"POLYGON ICRS -149.14586850017008 54.333948400496013  210.8955205 54.3845545  -149.10448214146274 54.384555070666131  210.8955187 54.3845561  -149.14903820001174 54.394171001186884  -149.14908832545618 54.394109707064437  -149.14920009798672 54.394133798580881  -149.1905442004192 54.343508400982444  -149.14603139861325 54.333908600634608  -149.14603041358257 54.333909810288574  -149.14602989845389 54.333909699682643  -149.14597980805354 54.333971029321859  -149.14586989927034 54.333947300644951  -149.14586892595676 54.3339484924185  -149.14586850017008 54.333948400496013POLYGON ICRS  -149.09847459792209 54.3230303987941  210.9422732 54.3745431  -149.1036666982782 54.384711400499484  -149.10371581166277 54.384649307202913  -149.10382750016643 54.3846740009035  -149.14452839846288 54.333144400140142  -149.09863480119753 54.322991499669243  -149.09858479583454 54.323054804465237  -149.09847459792209 54.3230303987941","_id_":"ext-record-1292"},
{"footprint":"POLYGON ICRS -149.14587569946758 54.3339388993125  210.8955127 54.3845474  -149.14904400027098 54.394162300830864  -149.14909408725393 54.39410105432939  -149.14920610207588 54.39412519936171  -149.1905500996489 54.343499900101229  -149.14603729859851 54.333900100534137  -149.1459861392041 54.333962743241358  -149.14587569946758 54.3339388993125POLYGON ICRS  -149.09848060136696 54.323022000017353  210.942267 54.3745345  -149.10367270054334 54.384702700755952  -149.1037216638662 54.384640797008693  -149.1038333980504 54.384665500553396  -149.14453439941954 54.333135899971637  -149.09864079959686 54.322982998351648  -149.09859072561127 54.3230463895762  -149.09848060136696 54.323022000017353","_id_":"ext-record-1293"},
{"footprint":"POLYGON ICRS -149.14587440017812 54.333939899639589  210.8955145 54.3845459  -149.10448807144158 54.384546456210465  210.8955127 54.3845474  -149.14904399904853 54.394162300988405  -149.14909408444166 54.39410105486531  -149.14920610010392 54.394125198414038  -149.1905500996489 54.343499900101229  -149.14603730115121 54.333900100493786  -149.14603639591533 54.33390120668647  -149.14603590261783 54.333901100407  -149.14598564368569 54.333962636287922  -149.14587570022465 54.333938899651528  -149.14587481108987 54.333939988351176  -149.14587440017812 54.333939899639589POLYGON ICRS  -149.09848060043402 54.323022000965885  210.942267 54.3745345  -149.10367269936083 54.384702699566866  -149.10372166392051 54.384640795908062  -149.10383339743694 54.384665500496894  -149.14453439941954 54.333135899971637  -149.09864080045151 54.322982998930605  -149.09859072383409 54.323046389206546  -149.09848060043402 54.323022000965885","_id_":"ext-record-1294"},
{"footprint":"POLYGON ICRS -149.14586849997437 54.33394840001737  210.8955205 54.3845545  -149.14903530033627 54.39416869973526  -149.14908548075147 54.394107342933395  -149.14919709847388 54.39413140122268  -149.19054150128022 54.343508800297194  -149.14602989903327 54.333909700310343  -149.1459788364769 54.333972220348251  -149.14586849997437 54.33394840001737POLYGON ICRS  -149.09847529970435 54.323031799786165  210.9422722 54.3745424  -149.10366600111956 54.384710000245121  -149.10371516755123 54.384647841726412  -149.10382669808777 54.384672499508142  -149.14452730128613 54.3331451999968  -149.0986355989829 54.322993001085344  -149.09858564541221 54.323056237506854  -149.09847529970435 54.323031799786165","_id_":"ext-record-1295"},
{"footprint":"POLYGON ICRS -149.14587469925183 54.333937299487786  210.8955144 54.3845437  -149.10448824377571 54.384544270739724  210.8955126 54.3845453  -149.14904450120679 54.3941602990499  -149.1490947891559 54.394098806181447  -149.14920610370581 54.394122801616824  -149.19055009960763 54.343497499995806  -149.14603740266881 54.333897600127131  -149.14603632762788 54.333898914057741  -149.14603579963554 54.333898799810314  -149.14598583909768 54.333959969718144  -149.14587619885515 54.333936300381424  -149.14587528042145 54.333937424956446  -149.14587469925183 54.333937299487786POLYGON ICRS  -149.09848060108439 54.323019301020004  210.9422674 54.3745323  -149.10367280197568 54.384700599291854  -149.10372203540152 54.384638354569404  -149.10383350012398 54.384662999733031  -149.1445343992755 54.333133499464196  -149.09864089952552 54.32298050120675  -149.09859094664353 54.323043739296295  -149.09848060108439 54.323019301020004","_id_":"ext-record-1296"},
{"footprint":"POLYGON ICRS -149.14587619933536 54.333936299906291  210.8955126 54.3845453  -149.14904449983189 54.394160299528011  -149.14909478825263 54.394098806850472  -149.14920610176264 54.394122800968383  -149.19055009960763 54.343497499995806  -149.14603740135138 54.333897600028664  -149.14598637005454 54.333960085732983  -149.14587619933536 54.333936299906291POLYGON ICRS  -149.09848059943423 54.323019300703464  210.9422674 54.3745323  -149.1036728018145 54.384700599614028  -149.10372203462708 54.384638355162195  -149.10383350197694 54.38466299965183  -149.1445343992755 54.333133499464196  -149.09864089990543 54.322980500490225  -149.0985909460461 54.323043739484206  -149.09848059943423 54.323019300703464","_id_":"ext-record-1297"},
{"footprint":"POLYGON ICRS -149.14587439969131 54.333939900021221  210.8955145 54.3845459  -149.14904109853083 54.394160100336279  -149.14909124103843 54.394098789858255  -149.14920310099811 54.394122900356642  -149.1905476010169 54.34350020017677  -149.14603589901179 54.333901098938966  -149.14598475105333 54.333963723665519  -149.14587439969131 54.333939900021221POLYGON ICRS  -149.09848130060155 54.323023400590166  210.9422661 54.3745339  -149.10367190005945 54.384701399272217  -149.10372101352482 54.384639307808193  -149.10383270008322 54.384664000032373  -149.14453329943166 54.33313660055709  -149.09864150140743 54.32298439965286  -149.09859142654216 54.323047789744408  -149.09848130060155 54.323023400590166","_id_":"ext-record-1298"},
{"footprint":"POLYGON ICRS -149.14587469887621 54.333937299490394  210.8955144 54.3845437  -149.14904149819273 54.394157900337554  -149.14909182878463 54.394096359965339  -149.14920289824647 54.39412030023631  -149.19054719973414 54.343497899817784  -149.14603579745827 54.333898800188877  -149.1459849190299 54.333961094843481  -149.14587469887621 54.333937299490394POLYGON ICRS  -149.0984813013643 54.323020700265012  210.9422663 54.3745316  -149.1036720002873 54.384699099509668  -149.10372131598621 54.3846367524697  -149.10383279972444 54.384661399720542  -149.14453319990423 54.333134299829808  -149.09864159923427 54.322982101429496  -149.0985917802837 54.323045167621537  -149.0984813013643 54.323020700265012","_id_":"ext-record-1299"},
{"footprint":"POLYGON ICRS -149.14587580092103 54.333930300177521  210.8955133 54.3845365  -149.10448941319586 54.384537085561945  210.8955115 54.3845382  -149.1490456006126 54.394153201247427  -149.14909594483504 54.394091640872908  -149.149207102421 54.394115603067455  -149.19055100063747 54.343490400618258  -149.14603840066931 54.333890599278753  -149.14603732562867 54.333891913209385  -149.14603680020915 54.333891799690285  -149.14598692330151 54.33395286536652  -149.14587729976151 54.333929198902922  -149.14587631050827 54.33393041019275  -149.14587580092103 54.333930300177521POLYGON ICRS  -149.09848170148521 54.323012198792419  210.9422663 54.3745252  -149.10367390448496 54.384693499906007  -149.10372320286982 54.384631169744239  -149.10383460069104 54.3846557996735  -149.1445354007206 54.333126400028291  -149.098641997969 54.32297350041344  -149.09859211255875 54.323036653798752  -149.09848170148521 54.323012198792419","_id_":"ext-record-1300"},
{"footprint":"POLYGON ICRS -149.14587729990018 54.333929199062659  210.8955115 54.3845382  -149.14904560015162 54.394153200860195  -149.14909594342717 54.39409164110134  -149.14920710139378 54.394115601552976  -149.19055100063747 54.343490400618258  -149.14603839900835 54.333890599814453  -149.14598745312176 54.333952981117186  -149.14587729990018 54.333929199062659POLYGON ICRS  -149.09848169949203 54.323012199111936  210.9422663 54.3745252  -149.10367390141033 54.384693500134844  -149.10372320162142 54.384631170471891  -149.10383459963256 54.384655799498958  -149.1445354007206 54.333126400028291  -149.09864199800381 54.322973500332942  -149.09859211275719 54.323036652656072  -149.09848169949203 54.323012199111936","_id_":"ext-record-1301"},
{"footprint":"POLYGON ICRS -149.14587580063701 54.333930299430307  210.8955133 54.3845365  -149.14904250086127 54.394150700270664  -149.14909283155225 54.3940891598673  -149.14920390088344 54.394113100164674  -149.19054809879754 54.34349079955377  -149.14603680069624 54.333891800046672  -149.14598593683249 54.333954076767689  -149.14587580063701 54.333930299430307POLYGON ICRS  -149.09848250096832 54.323013699571533  210.9422651 54.3745244  -149.10367309906229 54.384692000143971  -149.10372241596838 54.384629651546945  -149.10383389972048 54.384654298850357  -149.14453419938476 54.333127199505718  -149.09864269777054 54.322975099885852  -149.09859289325041 54.323038147846241  -149.09848250096832 54.323013699571533","_id_":"ext-record-1302"},
{"footprint":"POLYGON ICRS -149.14586500061372 54.333939199501117  210.8955238 54.3845451  -149.10447877246094 54.384545656300887  210.895522 54.3845466  -149.14903459928942 54.39416149958641  -149.14908463098556 54.394100321700257  -149.14919680233578 54.394124499995868  -149.19054080021812 54.343499199637066  -149.14602800064651 54.333899300505614  -149.14602702803265 54.333900491430434  -149.14602660155217 54.333900400358544  -149.14597632636742 54.333961955736228  -149.14586630066049 54.333938199513177  -149.14586541152607 54.333939288212818  -149.14586500061372 54.333939199501117POLYGON ICRS  -149.09847130031341 54.32302130000069  210.9422762 54.3745337  -149.10366340281422 54.384701900465714  -149.10371237726406 54.384639977356926  -149.10382420201432 54.384664698520631  -149.14452509953622 54.333135200221442  -149.09863149908708 54.322982299478191  -149.09858142627695 54.32304568897974  -149.09847130031341 54.32302130000069","_id_":"ext-record-1303"},
{"footprint":"POLYGON ICRS -149.14586630022137 54.333938200924095  210.895522 54.3845466  -149.14903459950938 54.39416149895051  -149.14908463056278 54.394100320967127  -149.1491968007266 54.394124499737622  -149.19054080021812 54.343499199637066  -149.14602799966295 54.333899300796553  -149.14597675486883 54.333962048069878  -149.14586630022137 54.333938200924095POLYGON ICRS  -149.09847129899055 54.323021300074238  210.9422762 54.3745337  -149.10366340041932 54.384701900448171  -149.10371237936991 54.38463997691472  -149.10382420037334 54.384664699600044  -149.14452509953622 54.333135200221442  -149.09863149854783 54.322982300650629  -149.0985814257543 54.323045690241528  -149.09847129899055 54.323021300074238","_id_":"ext-record-1304"},
{"footprint":"POLYGON ICRS -149.14586500058797 54.333939200127027  210.8955238 54.3845451  -149.14903169923286 54.394159299178462  -149.14908178718767 54.39409805552625  -149.14919379978676 54.394122199226757  -149.19053830029776 54.34349950009392  -149.14602659840719 54.333900399135459  -149.14597543576582 54.333963041868266  -149.14586500058797 54.333939200127027POLYGON ICRS  -149.09847200094174 54.323022699869263  210.9422753 54.3745331  -149.10366270001231 54.384700599797959  -149.10371173160812 54.384638611804029  -149.1038233981869 54.384663299669953  -149.14452399951159 54.333135899683789  -149.09863230279132 54.322983699790335  -149.09858221345479 54.32304710819858  -149.09847200094174 54.323022699869263","_id_":"ext-record-1305"},
{"footprint":"POLYGON ICRS -149.1458677987946 54.333928900175387  210.8955208 54.3845346  -149.10448177225922 54.384535155379496  210.895519 54.3845361  -149.149037401754 54.394150900309413  -149.14908748713822 54.394089654187717  -149.14919950026569 54.394113800739738  -149.19054320019009 54.343488800575258  -149.14603079946849 54.333889099610609  -149.14602989680549 54.333890206531578  -149.14602940093539 54.333890099523792  -149.14597912577324 54.333951654904261  -149.14586910266274 54.333927899415613  -149.14586821241991 54.333928989473378  -149.1458677987946 54.333928900175387POLYGON ICRS  -149.0984743009449 54.323011100513988  210.942273 54.3745232  -149.10366640105502 54.384691298777831  -149.10371537422955 54.384629377165886  -149.1038272041055 54.384654099781635  -149.14452780056365 54.333124900777605  -149.09863460138504 54.322972000887255  -149.09858444363334 54.323035496091556  -149.0984743009449 54.323011100513988","_id_":"ext-record-1306"},
{"footprint":"POLYGON ICRS -149.14586910032926 54.333927900280656  210.895519 54.3845361  -149.14903740008626 54.394150899127553  -149.14908748597139 54.394089654010742  -149.14919950059829 54.39411379916298  -149.19054320019009 54.343488800575258  -149.14603079916125 54.333889100083958  -149.14597962359269 54.3339517624027  -149.14586910032926 54.333927900280656POLYGON ICRS  -149.09847430029734 54.323011100772469  210.942273 54.3745232  -149.10366639934821 54.384691298945029  -149.10371537697671 54.38462937714197  -149.10382720057737 54.384654100316588  -149.14452780056365 54.333124900777605  -149.09863460020239 54.322972000016435  -149.09858444447102 54.32303549466333  -149.09847430029734 54.323011100772469","_id_":"ext-record-1307"},
{"footprint":"POLYGON ICRS -149.145867800476 54.333928899704816  210.8955208 54.3845346  -149.14903449959127 54.394148699576796  -149.14908464115419 54.394087390291467  -149.14919649818916 54.3941115003735  -149.19054070084084 54.3434892007935  -149.14602940082116 54.333890099432615  -149.1459782381412 54.333952742145037  -149.145867800476 54.333928899704816POLYGON ICRS  -149.09847509860055 54.323012400236088  210.942272 54.3745226  -149.10366570004473 54.384690000745216  -149.10371473270484 54.384628011460165  -149.103826401999 54.384652699837638  -149.14452680005564 54.333125600138459  -149.09863530125989 54.322973499971582  -149.09858529376075 54.323036804878605  -149.09847509860055 54.323012400236088","_id_":"ext-record-1308"},
{"footprint":"POLYGON ICRS -149.14586370081085 54.333930700146176  210.8955252 54.3845399  -149.14903210024653 54.3941548999017  -149.14908241444382 54.394093375730073  -149.1491933990834 54.3941172990092  -149.19053740055193 54.343492000696557  -149.1460247021339 54.333892199281109  -149.14597383900846 54.333954479093094  -149.14586370081085 54.333930700146176POLYGON ICRS  -149.09846799932487 54.32301370000058  210.9422802 54.3745269  -149.10366010107691 54.384695200567251  -149.10370941713782 54.384632850933087  -149.10382090176134 54.384657499293432  -149.14452180033362 54.333128000934458  -149.09862830100116 54.322975100411583  -149.09857848189935 54.323038168840888  -149.09846799932487 54.32301370000058","_id_":"ext-record-1309"},
{"footprint":"POLYGON ICRS -149.14586220030395 54.333931800187024  210.8955271 54.3845382  -149.10447569798961 54.384538803727715  210.8955252 54.3845399  -149.14903209792908 54.394154900714462  -149.14908241473324 54.39409337707702  -149.14919339858295 54.3941172994505  -149.19053740055193 54.343492000696557  -149.14602470226956 54.333892197672277  -149.14602361356265 54.33389353183199  -149.14602300206781 54.333893399507105  -149.1459732238153 54.333954346671412  -149.14586369914491 54.33393069891261  -149.14586270989324 54.333931910202452  -149.14586220030395 54.333931800187024POLYGON ICRS  -149.09846799854242 54.3230137001064  210.9422802 54.3745269  -149.10366010262598 54.384695199264812  -149.10370941731537 54.384632849614235  -149.10382090129417 54.384657498393686  -149.14452180033362 54.333128000934458  -149.09862829944319 54.322975099417825  -149.09857848146476 54.323038168090953  -149.09846799854242 54.3230137001064","_id_":"ext-record-1310"},
{"footprint":"POLYGON ICRS -149.14586029957644 54.33393089920034  210.8955283 54.3845367  -149.104474274907 54.384537255508015  210.8955265 54.3845382  -149.14902989924084 54.394152998693251  -149.14907993092996 54.394091820808029  -149.1491921022556 54.394115999105722  -149.19053590036086 54.343490900145774  -149.14602340133166 54.3338910994525  -149.14602249741927 54.333892207873483  -149.14602199897641 54.333892100137348  -149.14597171005826 54.333953672014893  -149.14586160219574 54.333929899940728  -149.14586071318331 54.33393098849416  -149.14586029957644 54.33393089920034POLYGON ICRS  -149.09846680187155 54.323013099759969  210.9422805 54.3745253  -149.10365889968 54.384693399764934  -149.10370787795011 54.384631475886557  -149.10381970017161 54.38465620005141  -149.14452040017167 54.333126899998788  -149.09862709975673 54.322973999415787  -149.0985769419888 54.323037494616379  -149.09846680187155 54.323013099759969","_id_":"ext-record-1311"},
{"footprint":"POLYGON ICRS -149.14586160091378 54.333929899388934  210.8955265 54.3845382  -149.14902989869205 54.39415299982317  -149.1490799308296 54.394091820459423  -149.14919210112853 54.394115999113211  -149.19053590036086 54.343490900145774  -149.14602340082681 54.333891100009787  -149.14597221077548 54.3339537801543  -149.14586160091378 54.333929899388934POLYGON ICRS  -149.09846680102751 54.323013100099779  210.9422805 54.3745253  -149.10365890034248 54.384693400742961  -149.10370787922943 54.384631477344065  -149.10381970026503 54.384656199897556  -149.14452040017167 54.333126899998788  -149.09862709837765 54.322973998626267  -149.09857694236879 54.323037493602854  -149.09846680102751 54.323013100099779","_id_":"ext-record-1312"},
{"footprint":"POLYGON ICRS -149.14586219936635 54.333931799655311  210.8955271 54.3845382  -149.1490289009059 54.394152500546873  -149.14907935596278 54.394090807862419  -149.14919020084798 54.394114699779024  -149.19053430076653 54.343492399553824  -149.14602299932653 54.333893399349286  -149.14597223439202 54.333955555096452  -149.14586219936635 54.333931799655311POLYGON ICRS  -149.09846870015551 54.323015199485155  210.942279 54.3745261  -149.10365940156322 54.38469369978781  -149.10370876893404 54.384631287297374  -149.10382009921526 54.384655900733733  -149.14452040110123 54.333128800259992  -149.09862899990867 54.322976701334355  -149.09857924839531 54.323039682193183  -149.09846870015551 54.323015199485155","_id_":"ext-record-1313"},
{"footprint":"POLYGON ICRS -149.1458602986954 54.333930900516755  210.8955283 54.3845367  -149.14902709954947 54.394150799778785  -149.14907717264617 54.394089574088262  -149.14918909885867 54.394113699027031  -149.19053349918249 54.343491199344932  -149.14602200137074 54.333892099562028  -149.14597082298218 54.333954761600033  -149.1458602986954 54.333930900516755POLYGON ICRS  -149.0984674990699 54.323014499591714  210.9422796 54.3745247  -149.10365819860363 54.384692100518805  -149.10370723121366 54.384630111177721  -149.10381889799905 54.384654798893891  -149.14451940018765 54.333127600471066  -149.09862780156723 54.322975399575945  -149.09857764505921 54.32303889305139  -149.0984674990699 54.323014499591714","_id_":"ext-record-1314"},
{"footprint":"POLYGON ICRS -149.1014570011769 54.355544000352822  -149.12392499904507 54.373400000400572  -149.12404249891304 54.37334985048328  -149.1447159999019 54.389446999656926  -149.17155313122532 54.377746399901788  -149.17175400055223 54.377901000010617  -149.20201500035421 54.36455800026075  -149.201925620417 54.364489199537196  -149.1815312313328 54.348784020203077  -149.18060392012399 54.34806955921357  -149.17909900122754 54.346909999735942  -149.16368979137883 54.353704499751466  -149.15472799974341 54.346667000691674  -149.14348167068292 54.351534620506406  -149.13207500026593 54.342464999235951  -149.1014570011769 54.355544000352822","_id_":"ext-record-1315"},
{"footprint":"POLYGON ICRS -149.1014570011769 54.355544000352822  -149.12392499904507 54.373400000400572  -149.12404249891304 54.37334985048328  -149.1447159999019 54.389446999656926  -149.17155313122532 54.377746399901788  -149.17175400055223 54.377901000010617  -149.20201500035421 54.36455800026075  -149.201925620417 54.364489199537196  -149.1815312313328 54.348784020203077  -149.18060392012399 54.34806955921357  -149.17909900122754 54.346909999735942  -149.16368979137883 54.353704499751466  -149.15472799974341 54.346667000691674  -149.14348167068292 54.351534620506406  -149.13207500026593 54.342464999235951  -149.1014570011769 54.355544000352822","_id_":"ext-record-1316"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354051  -149.12392499890083 54.373400000401823  -149.12404249876883 54.37334985048453  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011931  -149.20201500021003 54.3645580002621  -149.20192562027279 54.364489199538546  -149.18153123133283 54.34878402020307  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737257  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354051","_id_":"ext-record-1317"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1318"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1319"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1320"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1321"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1322"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.171754000408 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.18153123133283 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959932 54.346667000692968  -149.14348167053876 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1323"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.373400000401823  -149.12404249876883 54.37334985048453  -149.14471599975764 54.389446999658205  -149.17155313108108 54.37774639990311  -149.17175400040796 54.377901000011938  -149.20201500021003 54.364558000262107  -149.20192562027279 54.364489199538554  -149.1815312313328 54.348784020203077  -149.18060391997983 54.3480695592149  -149.17909900108344 54.346909999737264  -149.1636897912347 54.353704499752773  -149.15472799959929 54.346667000692968  -149.14348167053873 54.351534620507692  -149.1320750001218 54.342464999237222  -149.10145700103274 54.355544000354058","_id_":"ext-record-1324"},
{"footprint":"POLYGON ICRS -149.10145700103274 54.355544000354058  -149.12392499890083 54.37340000040183  -149.12404249876883 54.373349850484537  -149.14471599975764 54.389446999658212  -149.17155313108108 54.377746399903117  -149.17175400040796 54.377901000011938  -149.20201500021003 54.364558000262114  -149.20192562027279 54.364489199538561  -149.1815312313328 54.348784020203084  -149.18060391997983 54.348069559214906  -149.17909900108344 54.346909999737271  -149.1636897912347 54.35370449975278  -149.15472799959929 54.346667000692975  -149.14348167053873 54.351534620507692  -149.1320750001218 54.34246499923723  -149.10145700103274 54.355544000354058","_id_":"ext-record-1325"},
{"footprint":"POLYGON ICRS -149.1387115006558 54.331411699836863  -149.17180399944482 54.351243799290415  -149.24640839985588 54.315884199880557  -149.21330830026142 54.296072500410581  -149.21330824473091 54.296072526470006  210.7866918 54.2960725  -149.1387115006558 54.331411699836863POLYGON ICRS  -149.10393269992827 54.310671301069874  -149.13786319951927 54.331348398959769  -149.21251390029542 54.295006199349046  -149.17857589913325 54.274350100231374  -149.10393269992827 54.310671301069874","_id_":"ext-record-1326"},
{"footprint":"POLYGON ICRS -149.1387115006558 54.331411699836863  -149.17180399944482 54.351243799290415  -149.24640839985588 54.315884199880557  -149.21330830026142 54.296072500410581  -149.21330824473091 54.296072526470006  210.7866918 54.2960725  -149.1387115006558 54.331411699836863POLYGON ICRS  -149.10393269992827 54.310671301069874  -149.13786319951927 54.331348398959769  -149.21251390029542 54.295006199349046  -149.17857589913325 54.274350100231374  -149.10393269992827 54.310671301069874","_id_":"ext-record-1327"},
{"footprint":"POLYGON ICRS -149.09994900049097 54.355567000019256  -149.12241600114737 54.373422999973506  -149.12253408844543 54.373372600203069  -149.14320699890055 54.389469999747391  -149.17004436995327 54.377770569507668  -149.17024500038883 54.377925000984035  -149.20050699961405 54.364581000218287  -149.20041950054906 54.364513649391654  -149.18002337030313 54.3488073400892  -149.17909782950062 54.34809425014231  -149.17759200093226 54.34693399955902  -149.1621829304498 54.35372771052802  -149.15322000034084 54.346690000331776  -149.14197302022086 54.351557090351506  -149.13056699951289 54.342488000053272  -149.09994900049097 54.355567000019256","_id_":"ext-record-1328"},
{"footprint":"POLYGON ICRS -149.09994900049097 54.355567000019256  -149.12241600114737 54.373422999973506  -149.12253408844543 54.373372600203069  -149.14320699890055 54.389469999747391  -149.17004436995327 54.377770569507668  -149.17024500038883 54.377925000984035  -149.20050699961405 54.364581000218287  -149.20041950054906 54.364513649391654  -149.18002337030313 54.3488073400892  -149.17909782950062 54.34809425014231  -149.17759200093226 54.34693399955902  -149.1621829304498 54.35372771052802  -149.15322000034084 54.346690000331776  -149.14197302022086 54.351557090351506  -149.13056699951289 54.342488000053272  -149.09994900049097 54.355567000019256","_id_":"ext-record-1329"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371355  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985387  -149.14320499973056 54.389470000570768  -149.17004320007754 54.377770209992811  -149.1702430003086 54.377923999188312  -149.20050500037675 54.364580999796978  -149.2004175000672 54.364513650469483  -149.18002053895708 54.348805810371566  -149.17909583881925 54.3480933304787  -149.17759000099184 54.346932999389843  -149.16217955942733 54.353727310276184  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600835  -149.13056500014406 54.342487999931258  -149.09994700046113 54.355566999371355","_id_":"ext-record-1330"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371355  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985387  -149.14320499973056 54.389470000570768  -149.17004320007754 54.377770209992811  -149.1702430003086 54.377923999188312  -149.20050500037675 54.364580999796978  -149.2004175000672 54.364513650469483  -149.18002053895708 54.348805810371566  -149.17909583881925 54.3480933304787  -149.17759000099184 54.346932999389843  -149.16217955942733 54.353727310276184  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600835  -149.13056500014406 54.342487999931258  -149.09994700046113 54.355566999371355","_id_":"ext-record-1331"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371348  -149.12241400021384 54.3734229997115  -149.12253208111369 54.37337259998538  -149.14320499973056 54.389470000570761  -149.17004320007754 54.3777702099928  -149.1702430003086 54.377923999188305  -149.20050500037675 54.364580999796964  -149.20041750006718 54.364513650469476  -149.18002053895708 54.348805810371559  -149.17909583881925 54.34809333047869  -149.17759000099184 54.346932999389836  -149.16217955942733 54.353727310276177  -149.15321800075969 54.346690000598869  -149.14197142905385 54.351556920600821  -149.13056500014406 54.342487999931244  -149.09994700046113 54.355566999371348","_id_":"ext-record-1332"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371348  -149.12241400021384 54.3734229997115  -149.12253208111369 54.37337259998538  -149.14320499973056 54.389470000570761  -149.17004320007754 54.3777702099928  -149.1702430003086 54.377923999188305  -149.20050500037675 54.364580999796964  -149.20041750006718 54.364513650469476  -149.18002053895708 54.348805810371559  -149.17909583881925 54.34809333047869  -149.17759000099184 54.346932999389836  -149.16217955942733 54.353727310276177  -149.15321800075969 54.346690000598869  -149.14197142905385 54.351556920600821  -149.13056500014406 54.342487999931244  -149.09994700046113 54.355566999371348","_id_":"ext-record-1333"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371348  -149.12241400021384 54.3734229997115  -149.12253208111369 54.37337259998538  -149.14320499973056 54.389470000570761  -149.17004320007754 54.3777702099928  -149.1702430003086 54.377923999188305  -149.20050500037675 54.364580999796964  -149.20041750006718 54.364513650469476  -149.18002053895708 54.348805810371559  -149.17909583881925 54.34809333047869  -149.17759000099184 54.346932999389836  -149.16217955942733 54.353727310276177  -149.15321800075969 54.346690000598869  -149.14197142905385 54.351556920600821  -149.13056500014406 54.342487999931244  -149.09994700046113 54.355566999371348","_id_":"ext-record-1334"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371348  -149.12241400021384 54.3734229997115  -149.12253208111369 54.37337259998538  -149.14320499973056 54.389470000570761  -149.17004320007754 54.3777702099928  -149.1702430003086 54.377923999188305  -149.20050500037675 54.364580999796964  -149.20041750006718 54.364513650469476  -149.18002053895708 54.348805810371559  -149.17909583881925 54.34809333047869  -149.17759000099184 54.346932999389836  -149.16217955942733 54.353727310276177  -149.15321800075969 54.346690000598869  -149.14197142905385 54.351556920600821  -149.13056500014406 54.342487999931244  -149.09994700046113 54.355566999371348","_id_":"ext-record-1335"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371355  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985387  -149.14320499973056 54.389470000570768  -149.17004320007754 54.377770209992811  -149.17024300030857 54.377923999188312  -149.20050500037675 54.364580999796971  -149.20041750006718 54.364513650469483  -149.18002053895708 54.348805810371566  -149.17909583881925 54.3480933304787  -149.17759000099181 54.346932999389843  -149.16217955942733 54.353727310276184  -149.15321800075969 54.346690000598876  -149.14197142905385 54.351556920600835  -149.13056500014406 54.342487999931251  -149.0999470004611 54.355566999371355","_id_":"ext-record-1336"},
{"footprint":"POLYGON ICRS -149.09994700046113 54.355566999371355  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985387  -149.14320499973056 54.389470000570761  -149.17004320007754 54.3777702099928  -149.1702430003086 54.377923999188305  -149.20050500037675 54.364580999796971  -149.20041750006718 54.364513650469476  -149.18002053895708 54.348805810371566  -149.17909583881925 54.3480933304787  -149.17759000099184 54.346932999389836  -149.16217955942733 54.353727310276184  -149.15321800075969 54.346690000598876  -149.14197142905385 54.351556920600828  -149.13056500014406 54.342487999931251  -149.09994700046113 54.355566999371355","_id_":"ext-record-1337"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1338"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1339"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1340"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1341"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1342"},
{"footprint":"POLYGON ICRS -149.0999470004611 54.355566999371362  -149.12241400021384 54.373422999711508  -149.12253208111369 54.373372599985395  -149.14320499973056 54.389470000570782  -149.17004320007754 54.377770209992818  -149.17024300030857 54.377923999188319  -149.20050500037675 54.364580999796978  -149.20041750006718 54.36451365046949  -149.18002053895708 54.348805810371573  -149.17909583881925 54.348093330478704  -149.17759000099181 54.346932999389857  -149.16217955942733 54.3537273102762  -149.15321800075969 54.346690000598883  -149.14197142905385 54.351556920600842  -149.13056500014406 54.342487999931258  -149.0999470004611 54.355566999371362","_id_":"ext-record-1343"},
{"footprint":"POLYGON ICRS -149.09994700047778 54.3555669993958  -149.12241400023052 54.373422999735944  -149.12253208113037 54.373372600009844  -149.14320499974724 54.38947000059521  -149.17004320009426 54.377770210017246  -149.17024300032529 54.377923999212747  -149.20050500039346 54.364580999821413  -149.20041750008392 54.364513650493926  -149.18002053897379 54.348805810396009  -149.17909583883593 54.348093330503133  -149.17759000100853 54.346932999414292  -149.162179559444 54.35372731030062  -149.15321800077641 54.346690000623312  -149.14197142907057 54.351556920625278  -149.13056500016074 54.342487999955694  -149.09994700047778 54.3555669993958","_id_":"ext-record-1344"},
{"footprint":"POLYGON ICRS -149.09994700047778 54.3555669993958  -149.12241400023052 54.373422999735944  -149.12253208113037 54.373372600009844  -149.14320499974724 54.38947000059521  -149.17004320009426 54.377770210017246  -149.17024300032529 54.377923999212747  -149.20050500039346 54.364580999821413  -149.20041750008392 54.364513650493926  -149.18002053897379 54.348805810396009  -149.17909583883593 54.348093330503133  -149.17759000100853 54.346932999414292  -149.162179559444 54.35372731030062  -149.15321800077641 54.346690000623312  -149.14197142907057 54.351556920625278  -149.13056500016074 54.342487999955694  -149.09994700047778 54.3555669993958","_id_":"ext-record-1345"},
{"footprint":"POLYGON ICRS -149.09994699882907 54.355567000148262  210.877586 54.373423  -149.12253144072 54.373372870489206  -149.14320400026594 54.389469999169812  -149.17004283027916 54.377769930707764  -149.17024299866182 54.377923999982471  -149.2005049999882 54.3645809990979  -149.20041714041366 54.364513370464358  -149.18001930071927 54.34880553951281  -149.17909524866917 54.348093580062418  -149.17758900134149 54.346932999781117  -149.16217920013625 54.353727030148562  -149.15321800038441 54.346689999888696  -149.14197109876264 54.351557060093484  -149.13056499977202 54.342487999215727  -149.09994699882907 54.355567000148262","_id_":"ext-record-1346"},
{"footprint":"POLYGON ICRS -149.09994699882907 54.355567000148262  210.877586 54.373423  -149.12253144072 54.373372870489206  -149.14320400026594 54.389469999169812  -149.17004283027916 54.377769930707764  -149.17024299866182 54.377923999982471  -149.2005049999882 54.3645809990979  -149.20041714041366 54.364513370464358  -149.18001930071927 54.34880553951281  -149.17909524866917 54.348093580062418  -149.17758900134149 54.346932999781117  -149.16217920013625 54.353727030148562  -149.15321800038441 54.346689999888696  -149.14197109876264 54.351557060093484  -149.13056499977202 54.342487999215727  -149.09994699882907 54.355567000148262","_id_":"ext-record-1347"},
{"footprint":"POLYGON ICRS -149.0999469988565 54.355567000198107  -149.122413999848 54.373422999043946  -149.12253144074745 54.373372870539043  -149.14320400029342 54.389469999219642  -149.17004283030667 54.377769930757594  -149.17024299868933 54.377924000032294  -149.20050500001571 54.364580999147741  -149.2004171404412 54.364513370514196  -149.18001930074681 54.34880553956264  -149.17909524869668 54.348093580112256  -149.17758900136903 54.346932999830955  -149.16217920016376 54.353727030198385  -149.15321800041187 54.34668999993854  -149.14197109879009 54.351557060143321  -149.13056499979945 54.342487999265572  -149.0999469988565 54.355567000198107","_id_":"ext-record-1348"},
{"footprint":"POLYGON ICRS -149.0999469988565 54.355567000198107  -149.122413999848 54.373422999043946  -149.12253144074745 54.373372870539043  -149.14320400029342 54.389469999219642  -149.17004283030667 54.377769930757594  -149.17024299868933 54.377924000032294  -149.20050500001571 54.364580999147741  -149.2004171404412 54.364513370514196  -149.18001930074681 54.34880553956264  -149.17909524869668 54.348093580112256  -149.17758900136903 54.346932999830955  -149.16217920016376 54.353727030198385  -149.15321800041187 54.34668999993854  -149.14197109879009 54.351557060143321  -149.13056499979945 54.342487999265572  -149.0999469988565 54.355567000198107","_id_":"ext-record-1349"},
{"footprint":"POLYGON ICRS -149.0999469988565 54.355567000198107  -149.12241399984796 54.373422999043946  -149.12253144074745 54.373372870539043  -149.14320400029342 54.389469999219642  -149.17004283030667 54.377769930757594  -149.17024299868933 54.377924000032294  -149.20050500001571 54.364580999147741  -149.2004171404412 54.364513370514196  -149.18001930074681 54.348805539562647  -149.17909524869668 54.348093580112256  -149.17758900136903 54.346932999830955  -149.16217920016376 54.353727030198392  -149.15321800041187 54.34668999993854  -149.14197109879009 54.351557060143314  -149.13056499979945 54.342487999265572  -149.0999469988565 54.355567000198107","_id_":"ext-record-1350"},
{"footprint":"POLYGON ICRS -149.0999469988565 54.355567000198107  -149.12241399984796 54.373422999043946  -149.12253144074745 54.373372870539043  -149.14320400029342 54.389469999219642  -149.17004283030667 54.377769930757594  -149.17024299868933 54.377924000032294  -149.20050500001571 54.364580999147741  -149.2004171404412 54.364513370514196  -149.18001930074681 54.348805539562647  -149.17909524869668 54.348093580112256  -149.17758900136903 54.346932999830955  -149.16217920016376 54.353727030198392  -149.15321800041187 54.34668999993854  -149.14197109879009 54.351557060143314  -149.13056499979945 54.342487999265572  -149.0999469988565 54.355567000198107","_id_":"ext-record-1351"},
{"footprint":"POLYGON ICRS -149.15619699977697 54.314315999475859  -149.15242299922323 54.3363269994516  -149.19019299981963 54.33853099953086  -149.19020757077888 54.338446160911438  -149.22453800020182 54.340251000029539  -149.22754477946344 54.3207996195734  -149.22787699945937 54.32081599910461  -149.23107600138766 54.298783000181956  -149.2309456315036 54.298776569667609  -149.19729935990139 54.297112790690072  -149.195761059051 54.297036490273179  -149.193271999523 54.296913000659679  -149.19163943892966 54.308132089571117  -149.17668400084793 54.3073110004643  -149.17537738099816 54.315437640081477  -149.15619699977697 54.314315999475859","_id_":"ext-record-1352"},
{"footprint":"POLYGON ICRS -149.15619699977697 54.314315999475859  -149.15242299922323 54.3363269994516  -149.19019299981963 54.33853099953086  -149.19020757077888 54.338446160911438  -149.22453800020182 54.340251000029539  -149.22754477946344 54.3207996195734  -149.22787699945937 54.32081599910461  -149.23107600138766 54.298783000181956  -149.2309456315036 54.298776569667609  -149.19729935990139 54.297112790690072  -149.195761059051 54.297036490273179  -149.193271999523 54.296913000659679  -149.19163943892966 54.308132089571117  -149.17668400084793 54.3073110004643  -149.17537738099816 54.315437640081477  -149.15619699977697 54.314315999475859","_id_":"ext-record-1353"},
{"footprint":"POLYGON ICRS -149.30269229896945 54.31362589981017  210.7371552 54.3646454  -149.26284758351346 54.364645970398414  210.7371533 54.3646471  -149.30766349860522 54.373807399043741  -149.30771186317776 54.373745466625429  -149.30782370028308 54.373768299040712  -149.34762459945597 54.322730300536158  -149.30285379767645 54.313584499627062  -149.30285277238994 54.313585816277644  -149.30285219933285 54.31358569945499  -149.30280410894122 54.313647359702351  -149.30269379999231 54.313624798491993  -149.30269285274713 54.313626013056229  -149.30269229896945 54.31362589981017POLYGON ICRS  -149.25500229834043 54.303191499269658  210.7841786 54.3551111  -149.26203739987022 54.364810599043565  -149.26208470729293 54.364747902405611  -149.26219680096031 54.3647714002888  -149.30132910126875 54.312835799842759  -149.25516140234649 54.30315110057132  -149.25511337517378 54.303214828379375  -149.25500229834043 54.303191499269658","_id_":"ext-record-1354"},
{"footprint":"POLYGON ICRS -149.30269379921768 54.31362479957965  210.7371533 54.3646471  -149.30766349929252 54.373807399548774  -149.30771186260174 54.373745466794553  -149.30782369917765 54.37376829990918  -149.34762459945597 54.322730300536158  -149.30285379894713 54.313584500305254  -149.302804683611 54.313647476506716  -149.30269379921768 54.31362479957965POLYGON ICRS  -149.25500229896286 54.303191500010065  210.7841786 54.3551111  -149.26203739939649 54.364810599134437  -149.26208470569128 54.36474790223064  -149.26219679869962 54.3647714007373  -149.30132910126875 54.312835799842759  -149.25516140244696 54.303151100171831  -149.25511337744371 54.30321482778843  -149.25500229896286 54.303191500010065","_id_":"ext-record-1355"},
{"footprint":"POLYGON ICRS -149.30269229987266 54.313625899463212  210.7371552 54.3646454  -149.30766040129379 54.373805099893922  -149.30770888753469 54.3737430136495  -149.30782050023714 54.373765799726151  -149.34762170006451 54.322730699855725  -149.30285219846274 54.313585699475283  -149.30280316205383 54.313648570423474  -149.30269229987266 54.313625899463212POLYGON ICRS  -149.25500309871589 54.3031928996374  210.7841775 54.3551104  -149.26203659934683 54.364809099767726  -149.26208397285424 54.364746316081046  -149.26219600011606 54.364769799929881  -149.30132790076519 54.312836600182649  -149.25516220171886 54.30315269967857  -149.25511430576103 54.303216253928632  -149.25500309871589 54.3031928996374","_id_":"ext-record-1356"},
{"footprint":"POLYGON ICRS -149.30269430070342 54.313628700379276  210.7371532 54.3646483  -149.26284943030211 54.364648837937374  210.7371514 54.3646499  -149.30766540129716 54.373810198911649  -149.30771371124413 54.373748336307678  -149.30782570279419 54.373771201000359  -149.34762669915366 54.322733099189044  -149.30285580053709 54.313587398632961  -149.30285484098371 54.313588631953706  -149.30285419850188 54.313588499298  -149.30280609444722 54.3136501760669  -149.30269569946626 54.31362759990774  -149.30269476685561 54.313628795707416  -149.30269430070342 54.313628700379276POLYGON ICRS  -149.25500429909394 54.303194301043987  210.7841765 54.3551139  -149.26203930178363 54.364813400057855  -149.26208661899355 54.364750683950788  -149.26219879887248 54.364774200540005  -149.30133110163501 54.312838599789231  -149.25516339920671 54.303153899391589  -149.25511537203261 54.303217627200127  -149.25500429909394 54.303194301043987","_id_":"ext-record-1357"},
{"footprint":"POLYGON ICRS -149.30269569866329 54.313627599787225  210.7371514 54.3646499  -149.30766540070843 54.373810199710576  -149.30771371016073 54.373748335920858  -149.30782570165434 54.373771200661466  -149.34762669915366 54.322733099189044  -149.30285580053547 54.313587399605574  -149.30280673780189 54.313650308357929  -149.30269569866329 54.313627599787225POLYGON ICRS  -149.25500429969011 54.303194300575861  210.7841765 54.3551139  -149.26203930003439 54.364813400441157  -149.26208662088044 54.364750684368857  -149.26219879915845 54.364774200505472  -149.30133110163501 54.312838599789231  -149.25516339803758 54.303153899284979  -149.25511537268375 54.303217627492785  -149.25500429969011 54.303194300575861","_id_":"ext-record-1358"},
{"footprint":"POLYGON ICRS -149.30269430118128 54.313628700433604  210.7371532 54.3646483  -149.30766230097154 54.373807899438312  -149.30771073356911 54.37374588200683  -149.30782249839717 54.373768699136647  -149.34762379882937 54.322733499914776  -149.30285419978105 54.313588500444069  -149.30280516325382 54.313651371665522  -149.30269430118128 54.313628700433604POLYGON ICRS  -149.25500499905388 54.303195799553322  210.7841755 54.3551132  -149.26203860063703 54.364811899643371  -149.26208590782508 54.364749203845463  -149.26219799994482 54.364772701245677  -149.30132990143969 54.312839400032544  -149.25516419948491 54.303155501124351  -149.25511622630822 54.303219157798772  -149.25500499905388 54.303195799553322","_id_":"ext-record-1359"},
{"footprint":"POLYGON ICRS -149.30270039932995 54.313627200676763  210.737148 54.3646477  -149.26285464982041 54.364648242625748  210.7371461 54.3646492  -149.30767139866225 54.373809800671509  -149.30772079616696 54.37374654777684  -149.30782930041704 54.373768699353896  -149.34762900091718 54.322732300687932  -149.30285949815308 54.313586898804793  -149.30285866003976 54.313587973752874  -149.30285829898256 54.313587899923689  -149.30281104795549 54.313648483723838  -149.3027015999636 54.313626101845536  -149.30270069571029 54.313627261285987  -149.30270039932995 54.313627200676763POLYGON ICRS  -149.25500939954682 54.303192698604057  210.7841721 54.3551131  -149.26204459950637 54.364812800379219  -149.26209286909142 54.364748824499749  -149.26220389890722 54.364772098869643  -149.30133490017633 54.312838099656844  -149.25516870192075 54.303153800389644  -149.25512162593083 54.303216268485528  -149.25500939954682 54.303192698604057","_id_":"ext-record-1360"},
{"footprint":"POLYGON ICRS -149.30270160094815 54.313626100565386  210.7371461 54.3646492  -149.307671399865 54.373809800310624  -149.30772079485928 54.373746546402316  -149.30782929986714 54.373768699340722  -149.34762900091718 54.322732300687932  -149.30285949991909 54.313586898602694  -149.30281141164991 54.313648557999471  -149.30270160094815 54.313626100565386POLYGON ICRS  -149.25500939944266 54.303192699980684  210.7841721 54.3551131  -149.26204459955011 54.364812799604692  -149.26209287078552 54.36474882398533  -149.26220389977124 54.364772099177372  -149.30133490017633 54.312838099656844  -149.25516870126458 54.303153800627669  -149.25512162535958 54.30321626881053  -149.25500939944266 54.303192699980684","_id_":"ext-record-1361"},
{"footprint":"POLYGON ICRS -149.30270039957728 54.313627200335759  210.737148 54.3646477  -149.30766849911498 54.373807499878495  -149.30771782743139 54.373744335523256  -149.30782639775234 54.373766500354648  -149.34762669829229 54.322732600153522  -149.30285830001324 54.313587900660735  -149.3028101440803 54.313649642821893  -149.30270039957728 54.313627200335759POLYGON ICRS  -149.25501019950795 54.303193999037049  210.7841711 54.3551125  -149.26204379934748 54.364811399547357  -149.26209200632775 54.364747511217878  -149.262203101915 54.364770799786726  -149.30133400108312 54.312838800574177  -149.25516950009074 54.303155101184387  -149.25512242437534 54.303217566865854  -149.25501019950795 54.303193999037049","_id_":"ext-record-1362"},
{"footprint":"POLYGON ICRS -149.30270130079168 54.313627700431624  210.7371462 54.3646505  -149.30767109931205 54.373810900508118  -149.3077201189117 54.373748127339852  -149.30782970018396 54.373770500007772  -149.34762969919402 54.322733699565546  -149.30285990092207 54.313588099623985  -149.30281144787827 54.313650226566168  -149.30270130079168 54.313627700431624POLYGON ICRS  -149.25500939928037 54.303194299392352  210.7841719 54.3551144  -149.26204450000972 54.364814000821283  -149.26209244677707 54.364750455271306  -149.26220380035053 54.364773798594555  -149.30133519937863 54.312839399718484  -149.25516860133166 54.303154899643204  -149.25512121362704 54.303217781698777  -149.25500939928037 54.303194299392352","_id_":"ext-record-1363"},
{"footprint":"POLYGON ICRS -149.30270019898555 54.3136286993264  210.737148 54.3646491  -149.26285449483265 54.364649610799241  210.7371462 54.3646505  -149.30767110068541 54.373810901888071  -149.30772011810726 54.373748127236688  -149.30782970074551 54.373770500310826  -149.34762969919402 54.322733699565546  -149.30285990172396 54.313588100840654  -149.30285906485256 54.313589174286818  -149.30285870122074 54.313589099733129  -149.30281108187327 54.313650151971956  -149.30270130105032 54.313627700504732  -149.30270047762369 54.313628756307637  -149.30270019898555 54.3136286993264POLYGON ICRS  -149.25500940195747 54.303194299035773  210.7841719 54.3551144  -149.26204449738722 54.364814001165378  -149.26209244805617 54.364750455910972  -149.2622037994949 54.364773798581751  -149.30133519937863 54.312839399718484  -149.25516860197757 54.30315489970036  -149.25512121475859 54.303217781937192  -149.25500940195747 54.303194299035773","_id_":"ext-record-1364"},
{"footprint":"POLYGON ICRS -149.30270020069889 54.31362870000423  210.737148 54.3646491  -149.30766820103875 54.373808799767687  -149.30771723507615 54.37374601231825  -149.30782689924936 54.373768400700349  -149.34762760106764 54.322733900085083  -149.30285869857417 54.313589100044126  -149.30281025838906 54.313651206638788  -149.30270020069889 54.31362870000423POLYGON ICRS  -149.25501009981784 54.303195599065674  210.7841711 54.3551138  -149.26204369971146 54.3648126997901  -149.26209165949743 54.364749139148316  -149.26220309994619 54.3647724997557  -149.30133440125397 54.312840000388775  -149.25516939904855 54.303156200787136  -149.25512199809813 54.303219098116635  -149.25501009981784 54.303195599065674","_id_":"ext-record-1365"},
{"footprint":"POLYGON ICRS -149.30269159937404 54.313640300244707  210.7371558 54.3646597  -149.26284684906298 54.364660242042113  210.7371539 54.3646612  -149.30766250079168 54.373821499334291  -149.307710733842 54.373759737311644  -149.30782320028592 54.373782700241811  -149.34762419991546 54.322744600669608  -149.3028532976048 54.313598900655016  -149.30285236652531 54.313600095703336  -149.30285189884705 54.313600001128329  -149.30280359269182 54.313661937620068  -149.30269289956843 54.313639299782793  -149.30269204778554 54.313640391944276  -149.30269159937404 54.313640300244707POLYGON ICRS  -149.25500179912115 54.303206100844641  210.7841788 54.3551253  -149.26203680125906 54.364824700498225  -149.26208399193823 54.364762156188185  -149.26219629794991 54.364785700869767  -149.30132860003889 54.312850099368433  -149.25516090000878 54.303165399380411  -149.25511267811689 54.303229385280083  -149.25500179912115 54.303206100844641","_id_":"ext-record-1366"},
{"footprint":"POLYGON ICRS -149.30269289940887 54.313639299697584  210.7371539 54.3646612  -149.30766250085378 54.373821500167921  -149.30771073115719 54.373759737785626  -149.30782319979289 54.373782699940321  -149.34762419991546 54.322744600669608  -149.30285329949254 54.313598900163058  -149.30280406171372 54.313662033361332  -149.30269289940887 54.313639299697584POLYGON ICRS  -149.25500180035871 54.303206100413782  210.7841788 54.3551253  -149.26203680016064 54.364824700918874  -149.26208399072587 54.364762157556491  -149.26219629888274 54.364785700872574  -149.30132860003889 54.312850099368433  -149.25516089948707 54.303165399311077  -149.25511267910059 54.303229386156524  -149.25500180035871 54.303206100413782","_id_":"ext-record-1367"},
{"footprint":"POLYGON ICRS -149.30269159920763 54.313640299436884  210.7371558 54.3646597  -149.30765959958373 54.373819199576459  -149.307707871844 54.373757387390405  -149.307820098878 54.373780298833495  -149.34762160004155 54.3227450004007  -149.30285189948395 54.313600001415004  -149.30280274172839 54.313663027875378  -149.30269159920763 54.313640299436884POLYGON ICRS  -149.25500250024953 54.303207400840485  210.7841778 54.3551246  -149.26203599943941 54.36482330070082  -149.2620831908437 54.364760758431821  -149.26219550073392 54.364784301380951  -149.30132749952469 54.312850899642307  -149.25516169988808 54.303166900312981  -149.25511359520911 54.303230731400859  -149.25500250024953 54.303207400840485","_id_":"ext-record-1368"},
{"footprint":"POLYGON ICRS -149.30270289929632 54.3136279006631  210.7371446 54.3646473  -149.26285804974557 54.36464784230278  210.7371427 54.3646488  -149.30767389922022 54.373809099930455  -149.3077221036279 54.373747372446324  -149.30783440147485 54.373770300962555  -149.34763540100329 54.322732199829559  -149.3028645003852 54.313586399678222  -149.30286348876055 54.313587699807819  -149.30286300048519 54.313587599436921  -149.30281478733519 54.313649415834959  -149.30270419961539 54.313626800260671  -149.3027032805922 54.313627978637427  -149.30270289929632 54.3136279006631POLYGON ICRS  -149.2550129995285 54.303193499141805  210.7841677 54.3551129  -149.26204799945342 54.364812400080346  -149.26209525465873 54.364749770229309  -149.26220749888202 54.364773301215116  -149.30133979945722 54.312837699753118  -149.25517210119907 54.303152999763135  -149.25512400955782 54.303216813106395  -149.2550129995285 54.303193499141805","_id_":"ext-record-1369"},
{"footprint":"POLYGON ICRS -149.30270419986829 54.313626799693346  210.7371427 54.3646488  -149.30767389969552 54.373809100282386  -149.30772210279497 54.373747372737832  -149.307834401395 54.373770300178023  -149.34763540100329 54.322732199829559  -149.30286450144342 54.313586400205374  -149.30281527719478 54.313649516177136  -149.30270419986829 54.313626799693346POLYGON ICRS  -149.25501299993735 54.303193499730256  210.7841677 54.3551129  -149.26204799876649 54.364812400018586  -149.26209525430104 54.364749770468521  -149.26220750022824 54.36477330073555  -149.30133979945722 54.312837699753118  -149.2551720998446 54.3031530007131  -149.25512401070895 54.303216813391927  -149.25501299993735 54.303193499730256","_id_":"ext-record-1370"},
{"footprint":"POLYGON ICRS -149.30270289944795 54.313627898973863  210.7371446 54.3646473  -149.307670899626 54.37380689930508  -149.30771922462455 54.373745019621445  -149.3078312991403 54.373767900174393  -149.34763259949577 54.322732599273444  -149.30286299762358 54.313587599618216  -149.30281386682859 54.313650591547322  -149.30270289944795 54.313627898973863POLYGON ICRS  -149.25501369949393 54.3031948996968  210.7841667 54.3551122  -149.26204720052687 54.36481100012589  -149.26209452231845 54.364748284974283  -149.26220669963294 54.364771800178275  -149.30133870076082 54.3128383995379  -149.25517280040648 54.303154499117547  -149.25512477441157 54.30321822599948  -149.25501369949393 54.3031948996968","_id_":"ext-record-1371"},
{"footprint":"POLYGON ICRS -149.30270110089924 54.3136321007299  210.7371471 54.3646525  -149.26285548210046 54.364653028569336  210.7371452 54.3646539  -149.3076719988066 54.373814300348982  -149.30772107219482 54.373751458109247  -149.30783050174045 54.373773801131485  -149.34763030028267 54.322737100470107  -149.30286060003215 54.313591698588382  -149.30285977558029 54.313592757015392  -149.3028594993049 54.3135926996379  -149.30281196307368 54.313653647519992  -149.302702201506 54.313631199622137  -149.30270144400544 54.313632170894785  -149.30270110089924 54.3136321007299POLYGON ICRS  -149.25501029973987 54.303197800299984  210.7841708 54.3551178  -149.26204550314216 54.364817399411344  -149.26209351455864 54.36475376939859  -149.26220480299722 54.364777099890588  -149.30133599990276 54.312842900187064  -149.2551696017679 54.303158500098611  -149.25512226277766 54.303221312594765  -149.25501029973987 54.303197800299984","_id_":"ext-record-1372"},
{"footprint":"POLYGON ICRS -149.30270220035061 54.313631199849908  210.7371452 54.3646539  -149.30767199912117 54.373814299994173  -149.30772107248418 54.373751457941054  -149.30783050153391 54.373773799626548  -149.34763030028267 54.322737100470107  -149.30286059966417 54.313591699897628  -149.30281224211777 54.313653704496971  -149.30270220035061 54.313631199849908POLYGON ICRS  -149.25501029998145 54.30319780018268  210.7841708 54.3551178  -149.26204550105103 54.364817400145924  -149.26209351185241 54.364753769686295  -149.26220480038626 54.364777099480548  -149.30133599990276 54.312842900187064  -149.25516960022208 54.303158500344146  -149.25512226407784 54.30322131393978  -149.25501029998145 54.30319780018268","_id_":"ext-record-1373"},
{"footprint":"POLYGON ICRS -149.30270109927054 54.313632099393672  210.7371471 54.3646525  -149.30766920090397 54.373812199816875  -149.30771832872165 54.373749292127734  -149.30782760101687 54.373771600362907  -149.34762809940958 54.32273739927556  -149.30285950103615 54.313592700107662  -149.30281120891712 54.313654616651554  -149.30270109927054 54.313632099393672POLYGON ICRS  -149.25501110063857 54.303199000524337  210.78417 54.3551172  -149.26204469941575 54.364816198967596  -149.26209277495607 54.3647524849178  -149.26220400030527 54.364775800589875  -149.3013352000934 54.312843499819294  -149.25517039806707 54.30315979989944  -149.25512312578684 54.303222526457922  -149.25501110063857 54.303199000524337","_id_":"ext-record-1374"},
{"footprint":"POLYGON ICRS -149.30271310051245 54.313622600050152  210.7371344 54.3646455  -149.30768290047982 54.373805999563466  -149.30773212049661 54.373742969657812  -149.30784099986178 54.373765198975249  -149.34764070095608 54.322728700592094  -149.30287119780311 54.313583300218028  -149.30282301649709 54.313645078874863  -149.30271310051245 54.313622600050152POLYGON ICRS  -149.25502110010825 54.303189200377076  210.7841603 54.3551095  -149.26205619959808 54.364809100264836  -149.26210440567502 54.364745210960159  -149.26221549956497 54.364768499759371  -149.30134659945486 54.312834498994796  -149.25518030139739 54.303150100429413  -149.25513310888675 54.303212723463204  -149.25502110010825 54.303189200377076","_id_":"ext-record-1375"},
{"footprint":"POLYGON ICRS -149.30271179881112 54.313623700075148  210.7371363 54.364644  -149.26286635047546 54.364644542225157  210.7371344 54.3646455  -149.30768290144738 54.37380599899128  -149.30773212003029 54.373742969830609  -149.3078410013444 54.373765199555955  -149.34764070095608 54.322728700592094  -149.3028711982117 54.313583299485586  -149.30287044064474 54.313584269352596  -149.30287010142666 54.313584199817491  -149.30282267309616 54.313645009582032  -149.30271309912973 54.313622599672605  -149.30271218010844 54.313623778050093  -149.30271179881112 54.313623700075148POLYGON ICRS  -149.25502109990319 54.303189201068712  210.7841603 54.3551095  -149.26205620172763 54.364809100105589  -149.26210440547607 54.364745211256562  -149.26221549963122 54.36476850001938  -149.30134659945486 54.312834498994796  -149.25518030041056 54.303150100033385  -149.25513310915645 54.303212722680655  -149.25502109990319 54.303189201068712","_id_":"ext-record-1376"},
{"footprint":"POLYGON ICRS -149.30271179887262 54.313623700360338  210.7371363 54.364644  -149.30767989918766 54.37380379961607  -149.307729080808 54.373740823035227  -149.30783820078116 54.373763100275376  -149.34763869910572 54.322729000248742  -149.3028701009429 54.313584198957081  -149.30282175405844 54.313646186031015  -149.30271179887262 54.313623700360338POLYGON ICRS  -149.25502179968785 54.303190599871066  210.7841593 54.355108799999996  -149.26205540138017 54.364807699899707  -149.262103478258 54.364743984074259  -149.26221470089024 54.364767299275812  -149.30134580173564 54.312835099689863  -149.25518109964304 54.303151399952363  -149.25513382776074 54.303214126175931  -149.25502179968785 54.303190599871066","_id_":"ext-record-1377"},
{"footprint":"POLYGON ICRS -149.30270929973332 54.313631799478983  210.7371382 54.3646513  -149.26286451792348 54.3646518557687  210.7371363 54.3646529  -149.30768040169087 54.37381320060117  -149.307728722628 54.373751317016719  -149.3078408004377 54.3737742003697  -149.34764170058978 54.322736200424444  -149.30287079977944 54.313590401500093  -149.30286978682219 54.313591699403325  -149.30286930112143 54.313591599756954  -149.30282118094087 54.31365329605692  -149.30271069982851 54.313630701233677  -149.30270976853248 54.313631895348422  -149.30270929973332 54.313631799478983POLYGON ICRS  -149.25501929917345 54.303197499377042  210.7841614 54.3551169  -149.26205440025802 54.364816400007726  -149.26210164309651 54.364753788897808  -149.26221380370782 54.364777301896694  -149.30134620052564 54.312841699344574  -149.25517840209744 54.303156998489655  -149.25513031178988 54.303220814062051  -149.25501929917345 54.303197499377042","_id_":"ext-record-1378"},
{"footprint":"POLYGON ICRS -149.30271070005296 54.31363070055486  210.7371363 54.3646529  -149.30768039955464 54.373813200117169  -149.30772872361683 54.373751317584315  -149.3078408003289 54.373774199472955  -149.34764170058978 54.322736200424444  -149.30287079823259 54.313590401189884  -149.3028216694473 54.313653394915505  -149.30271070005296 54.31363070055486POLYGON ICRS  -149.25501929955217 54.303197499852537  210.7841614 54.3551169  -149.26205439954032 54.364816399833657  -149.26210164163075 54.364753788104139  -149.26221380244618 54.36477730057878  -149.30134620052564 54.312841699344574  -149.25517840071285 54.303156999326674  -149.2551303105684 54.303220813512027  -149.25501929955217 54.303197499852537","_id_":"ext-record-1379"},
{"footprint":"POLYGON ICRS -149.30271010086156 54.31363069977921  210.7371373 54.3646501  -149.26286535073851 54.364650642043962  210.7371354 54.3646516  -149.307681099805 54.373811901094491  -149.30772931655022 54.373750154859628  -149.30784169931673 54.373773099817626  -149.34764270094624 54.322735000144476  -149.30287179778887 54.313589301659846  -149.30287085304874 54.313590513229421  -149.302870299256 54.313590399976405  -149.3028220042344 54.31365231923197  -149.30271139848043 54.313629698592543  -149.30271054656382 54.313630790925558  -149.30271010086156 54.31363069977921POLYGON ICRS  -149.2550203016624 54.303196499287438  210.7841603 54.3551157  -149.26205529904195 54.364815201539592  -149.26210249091926 54.364752655718831  -149.26221480074065 54.364776199602375  -149.3013470996147 54.312840500481741  -149.25517939867305 54.303155798568042  -149.25513117815888 54.303219786704062  -149.2550203016624 54.303196499287438","_id_":"ext-record-1380"},
{"footprint":"POLYGON ICRS -149.30271140019855 54.3136296991803  210.7371354 54.3646516  -149.30768110042047 54.373811900374889  -149.30772931726648 54.373750155224563  -149.3078416981335 54.373773099464167  -149.34764270094624 54.322735000144476  -149.3028717989796 54.313589301116352  -149.30282256240042 54.313652432777523  -149.30271140019855 54.3136296991803POLYGON ICRS  -149.25502030095868 54.303196500308282  210.7841603 54.3551157  -149.26205529849557 54.364815200408486  -149.26210249017686 54.364752655445763  -149.2622148009805 54.364776199554953  -149.3013470996147 54.312840500481741  -149.25517940002655 54.303155799174476  -149.25513117962947 54.303219786035321  -149.25502030095868 54.303196500308282","_id_":"ext-record-1381"},
{"footprint":"POLYGON ICRS -149.30270929921133 54.31363179905874  210.7371382 54.3646513  -149.30767730036368 54.373810900653581  -149.30772568038122 54.3737489506744  -149.30783760008092 54.373771799421533  -149.3476389003585 54.322736599572984  -149.30286929882615 54.313591599704651  -149.30282024882914 54.313654488198239  -149.30270929921133 54.31363179905874POLYGON ICRS  -149.25502010069798 54.303198900393589  210.7841604 54.3551162  -149.2620535993187 54.36481499909447  -149.26210090652017 54.364752303281314  -149.26221299862382 54.36477580067649  -149.30134500071819 54.312842499617446  -149.2551791978039 54.303158500582796  -149.25513117283467 54.303222226111728  -149.25502010069798 54.303198900393589","_id_":"ext-record-1382"},
{"footprint":"POLYGON ICRS -149.30271010047269 54.313630700999475  210.7371373 54.3646501  -149.30767809958053 54.373809699650344  -149.3077264392916 54.373747801251263  -149.30783859893046 54.373770698935779  -149.34763999961643 54.322735399227319  -149.30287030091245 54.313590400788563  -149.30282115539256 54.313653411717681  -149.30271010047269 54.313630700999475POLYGON ICRS  -149.25502099999349 54.30319780058592  210.7841594 54.355115  -149.26205449948617 54.364813800767365  -149.26210175723125 54.364751170456152  -149.2622139996154 54.364774699249253  -149.30134599992738 54.312841299224154  -149.2551801995717 54.3031573000273  -149.25513209488176 54.303221131131  -149.25502099999349 54.30319780058592","_id_":"ext-record-1383"},
{"footprint":"POLYGON ICRS -149.30272010020479 54.31361789936134  210.7371263 54.3646393  -149.30768969941346 54.373799499858038  -149.30773787588546 54.373737806517042  -149.30785049928221 54.373760800172839  -149.34765099915265 54.322723200390804  -149.30288050016838 54.313577499777281  -149.3028312623255 54.313640633194865  -149.30272010020479 54.31361789936134POLYGON ICRS  -149.25502950158852 54.303184799739171  210.7841507 54.3551036  -149.26206439822045 54.364802900334027  -149.26211158866272 54.364740357096672  -149.26222389939917 54.364763900963382  -149.30135590112249 54.312828800196925  -149.25518859911091 54.303144200028306  -149.25514044477214 54.303208099268815  -149.25502950158852 54.303184799739171","_id_":"ext-record-1384"},
{"footprint":"POLYGON ICRS -149.30271890093087 54.313618999364436  210.7371281 54.3646378  -149.26287446261802 54.364638323592423  210.7371263 54.3646393  -149.30768970053325 54.373799500111375  -149.30773787371749 54.373737806439884  -149.30785049962421 54.373760799334242  -149.34765099915265 54.322723200390804  -149.30288050072193 54.31357749985375  -149.30287956840206 54.313578696404264  -149.30287909939125 54.313578599603005  -149.30283078950075 54.313640536887064  -149.302720098989 54.313617899808477  -149.30271919460179 54.313619059419779  -149.30271890093087 54.313618999364436POLYGON ICRS  -149.25502950279505 54.303184799750234  210.7841507 54.3551036  -149.26206439792693 54.364802900270931  -149.26211158727864 54.364740357447658  -149.2622239008966 54.364763900541966  -149.30135590112249 54.312828800196925  -149.25518859826224 54.303144200453772  -149.25514044475395 54.303208100061795  -149.25502950279505 54.303184799750234","_id_":"ext-record-1385"},
{"footprint":"POLYGON ICRS -149.30271889950086 54.313618999958365  210.7371281 54.3646378  -149.3076867992076 54.37379730019336  -149.30773501771745 54.373735556820165  -149.30784739943189 54.373758499992846  -149.3476484014484 54.32272349967274  -149.30287910000857 54.313578599788812  -149.30282988735459 54.3136416967726  -149.30271889950086 54.313618999958365POLYGON ICRS  -149.25503019984231 54.303186200366063  210.7841498 54.3551029  -149.26206370106576 54.364801500290326  -149.26211087922397 54.364738975537904  -149.26222309851937 54.364762499505453  -149.30135480031379 54.312829499300342  -149.25518929994254 54.3031456013798  -149.25514114503457 54.303209499438758  -149.25503019984231 54.303186200366063","_id_":"ext-record-1386"},
{"footprint":"POLYGON ICRS -149.30271729920651 54.313625199381825  210.7371303 54.3646482  -149.30768710095137 54.373808699461428  -149.30773642750702 54.373745533281166  -149.30784499957295 54.373767699910893  -149.3476445006041 54.3227314003221  -149.30287519948985 54.313585999622617  -149.30282711240361 54.313647657380415  -149.30271729920651 54.313625199381825POLYGON ICRS  -149.25502520025347 54.303191800415917  210.7841562 54.3551121  -149.26206040008424 54.36481180077579  -149.26210865792763 54.364747842740478  -149.26221959914079 54.364771099635433  -149.30135060112374 54.312837200518516  -149.25518449948208 54.303152900314082  -149.25513742359706 54.303215368511665  -149.25502520025347 54.303191800415917","_id_":"ext-record-1387"},
{"footprint":"POLYGON ICRS -149.30271609886941 54.313626299515654  210.7371322 54.3646467  -149.26287045014328 54.364647241461164  210.7371303 54.3646482  -149.30768710101927 54.373808699844908  -149.30773642761039 54.373745532555112  -149.30784499757343 54.373767700724244  -149.3476445006041 54.3227314003221  -149.30287520282116 54.313585999069574  -149.3028743498048 54.313587092040756  -149.30287390263302 54.31358699953352  -149.30282666271967 54.313647566094929  -149.30271730074452 54.313625199182326  -149.30271639531045 54.313626360137434  -149.30271609886941 54.313626299515654POLYGON ICRS  -149.25502520039581 54.303191800560633  210.7841562 54.3551121  -149.26206039998178 54.364811799343784  -149.26210865975244 54.364747842925965  -149.26221959955018 54.364771099348751  -149.30135060112374 54.312837200518516  -149.25518449884669 54.303152899370488  -149.25513742545772 54.303215368198764  -149.25502520039581 54.303191800560633","_id_":"ext-record-1388"},
{"footprint":"POLYGON ICRS -149.30271610090531 54.313626299631508  210.7371322 54.3646467  -149.30768419933395 54.373806500226657  -149.30773352780187 54.373743335641343  -149.30784209794996 54.373765500679895  -149.34764219915021 54.322731700675774  -149.30287390030304 54.313586999277732  -149.30282575771821 54.313648724238881  -149.30271610090531 54.313626299631508POLYGON ICRS  -149.25502599957107 54.303193100727668  210.7841553 54.3551115  -149.26205960037422 54.364810400472749  -149.26210785902518 54.364746443622245  -149.2622188020033 54.364769700058822  -149.30134969929182 54.31283789937558  -149.25518520038545 54.303154200654674  -149.2551381369571 54.303216650230105  -149.25502599957107 54.303193100727668","_id_":"ext-record-1389"},
{"footprint":"POLYGON ICRS -149.168278799932 54.357060099937293  -149.16611489999997 54.365049399980386  -149.17822390004906 54.3654416999923  -149.18038540001902 54.357452100090029  -149.168278799932 54.357060099937293","_id_":"ext-record-1390"},
{"footprint":"POLYGON ICRS -149.22797699980373 54.340143000157177  -149.22206700006961 54.349654999928781  -149.23838000011767 54.353098000113427  -149.24429400000906 54.343584999800669  -149.22797699980373 54.340143000157177","_id_":"ext-record-1391"},
{"footprint":"POLYGON ICRS -149.22797699980373 54.340143000157177  -149.22206700006961 54.349654999928781  -149.23838000011767 54.353098000113427  -149.24429400000906 54.343584999800669  -149.22797699980373 54.340143000157177","_id_":"ext-record-1392"},
{"footprint":"POLYGON ICRS -149.08711200141209 54.353541999715652  -149.1040657299603 54.3703898995639  -149.10377899859756 54.370488999855311  -149.12315200049662 54.389502999847394  -149.12327558931423 54.389460289536871  -149.15234263043982 54.379408550916381  -149.15366476159213 54.378951049862273  -149.15581199839258 54.378208000241138  -149.14594824868163 54.368525610338594  -149.15895200118933 54.3641230002887  -149.15191158063854 54.357070009600633  -149.16865099922978 54.351476999735588  -149.1497689987674 54.332285999640035  -149.11680500037215 54.343299000211232  -149.11687772972087 54.343372969310487  -149.08711200141209 54.353541999715652","_id_":"ext-record-1393"},
{"footprint":"POLYGON ICRS -149.08711200141209 54.353541999715659  -149.1040657299603 54.370389899563904  -149.10377899859756 54.370488999855318  -149.12315200049659 54.3895029998474  -149.12327558931423 54.389460289536885  -149.15234263043982 54.379408550916381  -149.1536647615921 54.37895104986228  -149.15581199839258 54.378208000241138  -149.14594824868163 54.368525610338608  -149.15895200118933 54.364123000288707  -149.15191158063854 54.357070009600641  -149.16865099922975 54.351476999735588  -149.1497689987674 54.332285999640042  -149.11680500037215 54.343299000211239  -149.11687772972087 54.343372969310494  -149.08711200141209 54.353541999715659","_id_":"ext-record-1394"},
{"footprint":"POLYGON ICRS -149.08711200139217 54.353541999693377  -149.10406572994037 54.370389899541621  -149.10377899857764 54.370488999833043  -149.12315200047664 54.389502999825119  -149.12327558929428 54.3894602895146  -149.15234263041984 54.379408550894119  -149.15366476157214 54.378951049840012  -149.15581199837263 54.378208000218869  -149.14594824866168 54.368525610316325  -149.15895200116935 54.364123000266432  -149.15191158061856 54.357070009578372  -149.1686509992098 54.351476999713313  -149.14976899874745 54.332285999617781  -149.11680500035223 54.343299000188964  -149.11687772970095 54.343372969288211  -149.08711200139217 54.353541999693377","_id_":"ext-record-1395"},
{"footprint":"POLYGON ICRS -149.23423320174467 54.321366699600617  210.7738341 54.3778983  -149.27530630084303 54.3782961993577  -149.27531148808413 54.378259591983358  -149.2760221014652 54.37826519948694  -149.27602713099253 54.378229689217626  -149.27673860062575 54.378235299545643  -149.27677317508636 54.3779912317003  -149.32348650090881 54.378143000115152  -149.32349196149175 54.378109635580813  -149.32420130135128 54.378111800916145  -149.32420659881734 54.37807943191499  -149.32491860082288 54.378081601229894  -149.33403879901721 54.322268799994205  -149.28659679969303 54.322118500778373  -149.28659132916485 54.322151790429537  -149.28588119914139 54.322149399484552  -149.28587587464088 54.322181793596535  -149.2851666005603 54.322179400563058  -149.28162503712372 54.34371262995171  -149.28473640136139 54.321700499914591  -149.23566349976537 54.3213064002708  -149.23565829182209 54.321342944440666  -149.23494869867011 54.32133710016106  -149.2349436465488 54.321372555909804  -149.23423320174467 54.321366699600617","_id_":"ext-record-1396"},
{"footprint":"POLYGON ICRS -149.23423320232359 54.32136670112839  210.7738341 54.3778983  -149.2753063010376 54.3782961986316  -149.27531149110513 54.378259591384648  -149.27602209863292 54.378265198882666  -149.276027130932 54.378229689954232  -149.27673859866411 54.3782352982706  -149.2767731737251 54.37799123127995  -149.32348650142924 54.37814300238032  -149.3234919608054 54.378109636996307  -149.32420130392987 54.378111801045826  -149.32420660180489 54.378079432652605  -149.32491860304052 54.378081602171228  -149.33403879901721 54.322268799994205  -149.28659680189074 54.32211850104337  -149.28659653964098 54.322120102942542  -149.28659550119778 54.322120099446295  -149.28659520415815 54.3221219044892  -149.28659440099821 54.322121900573045  -149.28658948692845 54.322151785689222  -149.28588119661958 54.322149401212819  -149.28588091756652 54.32215110393134  -149.2858798970575 54.322151101774978  -149.28587975096323 54.322151999427952  -149.28587969982237 54.322151999843129  -149.28587480218286 54.322181791566081  -149.28516660338073 54.322179399174566  -149.28516632305494 54.322181103393149  -149.28516530121334 54.322181099004126  -149.2851650211806 54.322182802612367  -149.28516410008467 54.322182799505612  -149.28164149367021 54.343596274539649  -149.28473640136139 54.321700499914591  -149.23566349836511 54.321306401223488  -149.23565829287529 54.321342945516243  -149.23494869887631 54.321337098778642  -149.2349436484331 54.321372555203766  -149.23423320232359 54.32136670112839","_id_":"ext-record-1397"},
{"footprint":"POLYGON ICRS -149.23423360034704 54.321367600133023  210.7738338 54.3778973  -149.27530590023048 54.378295300594743  -149.27531107378132 54.378258791512685  -149.27602170063417 54.378264400667419  -149.27602673140697 54.378228888896551  -149.27673810118279 54.378234500773182  -149.2767727626175 54.377989838235251  -149.32348469974994 54.378141899265493  -149.32349016090757 54.378108530149461  -149.32419969994976 54.378110700636981  -149.32420499723236 54.378078324179285  -149.32491679829289 54.378080498176935  -149.33403610160096 54.322270799855481  -149.28659550256862 54.322120101091627  -149.28659001152985 54.322153496068417  -149.28587989799135 54.322151100506332  -149.28587457327626 54.3221834976613  -149.28516530062265 54.322181100074665  -149.28162797677913 54.343689477577243  -149.28473609979625 54.32170149997328  -149.23566400067932 54.321307300014517  -149.23565880590652 54.321343747874408  -149.2349491014094 54.321337899976619  -149.23494403451767 54.321373457864112  -149.23423360034704 54.321367600133023","_id_":"ext-record-1398"},
{"footprint":"POLYGON ICRS -149.23423550097061 54.321369299489547  210.7738328 54.3778959  -149.27530400121864 54.378293699655657  -149.27530906238113 54.3782579890866  -149.27602040054069 54.378263599675492  -149.27602555761064 54.378227199379005  -149.27673619849608 54.378232800691755  -149.27677085122158 54.3779882327024  -149.3234794008261 54.378139800285119  -149.32348471623524 54.37810733040692  -149.32419640036403 54.378109500008563  -149.32420184611311 54.378076241492707  -149.32491149930368 54.378078399258314  -149.33403269957444 54.322272000793483  -149.2865944014022 54.322121899999743  -149.28658906005271 54.322154385364321  -149.28587969993865 54.32215200010539  -149.28587424285479 54.322185193722888  -149.2851640998658 54.322182800582866  -149.28162921957605 54.34367085109136  -149.28473500053096 54.321702800161759  -149.23566580062436 54.321308898691832  -149.235660720837 54.321344549148954  -149.23495040122845 54.3213387000171  -149.23494520698569 54.321375145803358  -149.23423550097061 54.321369299489547","_id_":"ext-record-1399"},
{"footprint":"POLYGON ICRS -149.28587809894469 54.322153000184031  -149.27669299906481 54.377953900291224  -149.32419430062711 54.378108201055554  -149.33331500119087 54.322303599779147  -149.28587809894469 54.322153000184031POLYGON ICRS  -149.23495059977543 54.321340300858019  -149.22688490049384 54.377864500171881  -149.27602020024941 54.378261999882319  -149.28401839925033 54.321734599470169  -149.23495059977543 54.321340300858019","_id_":"ext-record-1400"},
{"footprint":"POLYGON ICRS -149.14079300027933 54.352697999814254  -149.14079300047629 54.352697999968875  -149.14079299996689 54.352698000189271  -149.15114200009177 54.360820000060059  -149.16507499996973 54.354791999808889  -149.15472800008868 54.3466669998012  -149.14079300027933 54.352697999814254","_id_":"ext-record-1401"},
{"footprint":"POLYGON ICRS -149.14079300027933 54.352697999814254  -149.14079300047629 54.352697999968875  -149.14079299996689 54.352698000189271  -149.15114200009177 54.360820000060059  -149.16507499996973 54.354791999808889  -149.15472800008868 54.3466669998012  -149.14079300027933 54.352697999814254","_id_":"ext-record-1402"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861128  -149.15114200005272 54.360820000106926  -149.16507499993071 54.35479199985577  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861128","_id_":"ext-record-1403"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861128  -149.15114200005272 54.360820000106926  -149.16507499993071 54.35479199985577  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861128","_id_":"ext-record-1404"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106933  -149.16507499993071 54.354791999855777  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861135","_id_":"ext-record-1405"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106933  -149.16507499993071 54.354791999855777  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861135","_id_":"ext-record-1406"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106933  -149.16507499993071 54.354791999855777  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861135","_id_":"ext-record-1407"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106941  -149.16507499993071 54.354791999855777  -149.15472800008868 54.346666999801208  -149.14079300024025 54.352697999861135","_id_":"ext-record-1408"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106933  -149.16507499993071 54.354791999855777  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861135","_id_":"ext-record-1409"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106933  -149.16507499993071 54.354791999855777  -149.15472800008868 54.3466669998012  -149.14079300024025 54.352697999861135","_id_":"ext-record-1410"},
{"footprint":"POLYGON ICRS -149.14079300024025 54.352697999861135  -149.15114200005272 54.360820000106941  -149.16507499993071 54.354791999855777  -149.15472800008868 54.346666999801208  -149.14079300024025 54.352697999861135","_id_":"ext-record-1411"},
{"footprint":"POLYGON ICRS -149.13928500015692 54.352719999849917  -149.13928500035388 54.352720000004553  -149.13928499984448 54.352720000224927  -149.14963299986377 54.36084200012273  -149.16356800017923 54.354815000114158  -149.15321999964405 54.346690000100772  -149.13928500015692 54.352719999849917","_id_":"ext-record-1412"},
{"footprint":"POLYGON ICRS -149.13928500015692 54.352719999849917  -149.13928500035388 54.352720000004553  -149.13928499984448 54.352720000224927  -149.14963299986377 54.36084200012273  -149.16356800017923 54.354815000114158  -149.15321999964405 54.346690000100772  -149.13928500015692 54.352719999849917","_id_":"ext-record-1413"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1414"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1415"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1416"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1417"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1418"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1419"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1420"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1421"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1422"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1423"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1424"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923136  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1425"},
{"footprint":"POLYGON ICRS -149.13928300013797 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.15321799993222 54.346689999837764  -149.13928300013797 54.352720000263247","_id_":"ext-record-1426"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1427"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1428"},
{"footprint":"POLYGON ICRS -149.13928300013797 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.15321799993222 54.346689999837764  -149.13928300013797 54.352720000263247","_id_":"ext-record-1429"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1430"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1431"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1432"},
{"footprint":"POLYGON ICRS -149.13928300013797 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.15321799993222 54.346689999837764  -149.13928300013797 54.352720000263247","_id_":"ext-record-1433"},
{"footprint":"POLYGON ICRS -149.13928300013797 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.15321799993222 54.346689999837764  -149.13928300013797 54.352720000263247","_id_":"ext-record-1434"},
{"footprint":"POLYGON ICRS -149.13928300013794 54.352720000263247  -149.14963100003075 54.360841999975925  -149.16356499989911 54.354814999923143  -149.1532179999322 54.346689999837764  -149.13928300013794 54.352720000263247","_id_":"ext-record-1435"},
{"footprint":"POLYGON ICRS -149.20376979814165 54.4022896001703  -149.22439659931939 54.427220099641758  -149.31458179931283 54.407485600281291  -149.29391009992287 54.382570499948841  -149.29391001520136 54.382570518519614  210.70609 54.3825705  -149.20885933500378 54.40117850499734  -149.20376979814165 54.4022896001703POLYGON ICRS  -149.18202579985169 54.376184400689674  -149.20299859984874 54.402072100853239  -149.24665651968684 54.39214258757071  -149.29373440122748 54.381412200122895  -149.29321785224641 54.380776835965051  -149.27271500002246 54.355540200617142  -149.18202579985169 54.376184400689674","_id_":"ext-record-1436"},
{"footprint":"POLYGON ICRS -149.20376979946386 54.402289601723837  -149.22439659931939 54.427220099641758  -149.31458179925372 54.407485601554122  -149.29391009992287 54.382570499948841  -149.29391001520136 54.382570518519614  210.70609 54.3825705  -149.29390756052206 54.382571034905062  -149.293907200761 54.382570600989467  -149.29390486100107 54.382571114063573  210.7060954 54.3825708  -149.29390451541354 54.382570818591191  210.7060955 54.3825708  -149.20377240138794 54.402286900180911  -149.20377367991745 54.40228844662694  -149.20377160125378 54.402288901751618  -149.20377181439534 54.402289161978231  -149.20376979946386 54.402289601723837POLYGON ICRS  -149.18202580120325 54.376184400461341  -149.20299859984874 54.402072100853239  -149.24665651968684 54.39214258757071  -149.29373440393081 54.381412202348308  -149.29321785224641 54.380776835965051  -149.27271500002246 54.355540200617142  -149.18202580120325 54.376184400461341","_id_":"ext-record-1437"},
{"footprint":"POLYGON ICRS -149.20377159801856 54.40228890046663  -149.20482370398528 54.403561373138885  210.7756025 54.427219  -149.31457799983124 54.407485200464478  -149.29390719970544 54.382570600466067  -149.20377159801856 54.40228890046663POLYGON ICRS  -149.18202719960914 54.376184200099267  -149.20299959940209 54.402071600386464  -149.29373300023178 54.381412499436557  210.727286 54.3555407  -149.18202719960914 54.376184200099267","_id_":"ext-record-1438"},
{"footprint":"POLYGON ICRS -149.20377239954942 54.402286900755968  210.7756018 54.4272158  -149.31457530000446 54.407484300882388  210.7060954 54.3825708  -149.29390451566408 54.382570819150033  -149.293904500331 54.382570799455138  -149.20377239954942 54.402286900755968POLYGON ICRS  -149.18202920127334 54.376184099817017  210.796999 54.4020702  -149.29373110102867 54.381412499938492  210.7272873 54.3555422  -149.27271261626456 54.355542219213  -149.27271260158921 54.355542199922084  -149.22738234362362 54.365871645510886  -149.18202920127334 54.376184099817017","_id_":"ext-record-1439"},
{"footprint":"POLYGON ICRS -149.08286200058018 54.386323999995575  -149.11678300130475 54.396292000206742  -149.12549304010926 54.386240600484982  -149.13885800046359 54.390258999645688  -149.14527857023265 54.383006949993536  -149.16237999847016 54.388222000237931  -149.17999700092855 54.368624000468394  -149.14632099945928 54.3583549999576  -149.146253200142 54.358430450177529  -149.11551899898183 54.349279000044582  -149.10025699104455 54.366668630389377  -149.09995899917871 54.366581000478213  -149.09818775960795 54.368627359900756  -149.08286200058018 54.386323999995575","_id_":"ext-record-1440"},
{"footprint":"POLYGON ICRS -149.08286200058018 54.386323999995575  -149.11678300130475 54.396292000206742  -149.12549304010926 54.386240600484982  -149.13885800046359 54.390258999645688  -149.14527857023265 54.383006949993536  -149.16237999847016 54.388222000237931  -149.17999700092855 54.368624000468394  -149.14632099945928 54.3583549999576  -149.146253200142 54.358430450177529  -149.11551899898183 54.349279000044582  -149.10025699104455 54.366668630389377  -149.09995899917871 54.366581000478213  -149.09818775960795 54.368627359900756  -149.08286200058018 54.386323999995575","_id_":"ext-record-1441"},
{"footprint":"POLYGON ICRS -149.11735980106474 54.421570299893546  -149.13794379913551 54.446515399614043  -149.22821279897676 54.426845200163356  -149.20758380052823 54.401915500123927  -149.11735980106474 54.421570299893546POLYGON ICRS  -149.09566070099618 54.3954497006618  -149.11658889881161 54.421352299767157  -149.20741069964328 54.400757000107241  -149.1864358009517 54.374870100455439  -149.09566070099618 54.3954497006618","_id_":"ext-record-1442"},
{"footprint":"POLYGON ICRS -149.11735980094525 54.421570298671973  -149.13794379913551 54.446515399614043  -149.22821279925068 54.426845199477519  -149.20758380052823 54.401915500123927  -149.20758144385204 54.401916015467172  210.7924189 54.4019156  -149.20758108656122 54.401915602599153  210.792419 54.4019155  -149.2075787422128 54.401915992664676  210.7924215 54.4019157  -149.20757841559819 54.401915719429965  210.7924216 54.4019157  -149.11736229924222 54.421567599278596  -149.1173623698615 54.421567685013351  -149.11736230115341 54.421567699947431  -149.11736357790551 54.421569248229765  -149.11736149695469 54.421569699347337  -149.11736166091453 54.421569895926915  -149.11735980094525 54.421570298671973POLYGON ICRS  -149.09566070047509 54.395449700973366  -149.11658889881161 54.421352299767157  -149.19962288790913 54.402526493453372  -149.20740950043268 54.400757300181283  -149.20740948199352 54.400757276448054  -149.20741070223977 54.400756999952939  -149.1864358009517 54.374870100455439  -149.09782548667812 54.394959955311066  -149.09566200026015 54.395449399754909  -149.09566200805531 54.395449405672444  -149.09566070047509 54.395449700973366","_id_":"ext-record-1443"},
{"footprint":"POLYGON ICRS -149.1173614978569 54.421569699460925  -149.13794459879475 54.446514400581691  -149.2282092997927 54.426844900249129  210.7924189 54.4019156  -149.20758108519891 54.401915603609169  -149.20758100095915 54.401915499382312  -149.1173614978569 54.421569699460925POLYGON ICRS  -149.09566200028087 54.395449399837545  -149.11617601013057 54.420840003466083  -149.11658979937187 54.421351799641236  -149.11658979933844 54.421351799541959  210.8834102 54.4213518  -149.20740950115311 54.400757299903496  210.8135651 54.3748706  -149.1864348154779 54.374870620116916  -149.18643480093448 54.374870599759518  -149.13977062270402 54.385460772858707  -149.09566200028087 54.395449399837545","_id_":"ext-record-1444"},
{"footprint":"POLYGON ICRS -149.1173623004824 54.421567600103749  -149.11736237026724 54.421567684729851  -149.11736229981352 54.421567700043347  210.8620547 54.4465112  -149.13794538581746 54.446511182284226  -149.13794539941023 54.446511199725052  -149.15078934802295 54.44371784424434  -149.22820660117554 54.426843999483367  210.7924215 54.4019157  -149.20757841658087 54.401915717586014  -149.20757840081782 54.401915699504  -149.1173623004824 54.421567600103749POLYGON ICRS  -149.09566399948608 54.395449299352869  210.8834088 54.4213504  -149.20740749963133 54.400757399065029  210.8135665 54.374872  -149.09566399948608 54.395449299352869","_id_":"ext-record-1445"},
{"footprint":"POLYGON ICRS -149.13521699987771 54.360463999914977  210.856062 54.369206  -149.15895200001663 54.36412299980303  -149.15022600015325 54.35538099985552  -149.13521699987771 54.360463999914977","_id_":"ext-record-1446"},
{"footprint":"POLYGON ICRS -149.13521699987771 54.360463999914977  210.856062 54.369206  -149.15895200001663 54.36412299980303  -149.15022600015325 54.35538099985552  -149.13521699987771 54.360463999914977","_id_":"ext-record-1447"},
{"footprint":"POLYGON ICRS -149.13521699984165 54.3604639999352  -149.14393800007545 54.369206000232545  -149.15895199998056 54.364122999823259  -149.15022600011716 54.355380999875742  -149.13521699984165 54.3604639999352","_id_":"ext-record-1448"},
{"footprint":"POLYGON ICRS -149.245045001086 54.353887999513908  -149.24622199989253 54.376017999643523  -149.24636936045076 54.376015370581378  -149.24777500125197 54.396104000201575  -149.28127667113208 54.395309459830322  -149.28129200045078 54.395502999214578  -149.31921499973654 54.394482999668519  -149.31920831986483 54.39439861970326  -149.31765060969229 54.374722810087505  -149.31757990058378 54.373829139846187  -149.31746499882112 54.372377000063722  -149.29815544973786 54.372895819911086  -149.29760200078263 54.364139000425411  -149.28360100910177 54.364441790007234  -149.28300500069204 54.353203001000068  -149.245045001086 54.353887999513908","_id_":"ext-record-1449"},
{"footprint":"POLYGON ICRS -149.245045001086 54.353887999513908  -149.24622199989255 54.37601799964353  -149.24636936045076 54.376015370581385  -149.24777500125197 54.396104000201575  -149.28127667113208 54.395309459830322  -149.2812920004508 54.395502999214578  -149.31921499973654 54.394482999668519  -149.31920831986483 54.39439861970326  -149.31765060969229 54.374722810087505  -149.31757990058378 54.373829139846187  -149.31746499882112 54.372377000063729  -149.29815544973786 54.372895819911086  -149.29760200078263 54.364139000425418  -149.28360100910177 54.364441790007234  -149.28300500069204 54.353203001000075  -149.245045001086 54.353887999513908","_id_":"ext-record-1450"},
{"footprint":"POLYGON ICRS -149.245045001086 54.353887999513908  -149.24622199989253 54.376017999643523  -149.24636936045076 54.376015370581378  -149.24777500125197 54.396104000201575  -149.28127667113208 54.395309459830322  -149.28129200045078 54.395502999214578  -149.31921499973654 54.394482999668519  -149.31920831986483 54.39439861970326  -149.31765060969229 54.374722810087505  -149.31757990058378 54.373829139846187  -149.31746499882112 54.372377000063722  -149.29815544973786 54.372895819911086  -149.29760200078263 54.364139000425411  -149.28360100910177 54.364441790007234  -149.28300500069204 54.353203001000068  -149.245045001086 54.353887999513908","_id_":"ext-record-1451"},
{"footprint":"POLYGON ICRS -149.24504500114583 54.353887999520794  -149.24622199995238 54.376017999650415  -149.24636936051061 54.376015370588256  -149.24777500131182 54.39610400020846  -149.28127667119196 54.395309459837193  -149.28129200051066 54.395502999221449  -149.31921499979646 54.394482999675375  -149.31920831992468 54.39439861971011  -149.31765060975218 54.374722810094362  -149.31757990064361 54.373829139853044  -149.31746499888098 54.372377000070585  -149.29815544979772 54.372895819917957  -149.29760200084249 54.364139000432282  -149.28360100916163 54.364441790014105  -149.28300500075187 54.353203001006939  -149.24504500114583 54.353887999520794","_id_":"ext-record-1452"},
{"footprint":"POLYGON ICRS -149.07863939994073 54.316666399762404  210.8997343 54.3412951  -149.1894041988852 54.320279300537422  210.8322644 54.2956666  -149.16773551665383 54.295666619029653  -149.16773550097395 54.295666600634753  -149.07863949907289 54.316666299939989  -149.07863955472112 54.316666363356589  -149.07863939994073 54.316666399762404POLYGON ICRS  -149.05585060070089 54.290879300878245  210.9221388 54.3164599  -149.07786128343082 54.316459879667093  -149.07786130114906 54.316459899663009  -149.09256471528943 54.312866281300067  -149.16751200065167 54.29451119945162  210.8545425 54.2689468  -149.05585060070089 54.290879300878245","_id_":"ext-record-1453"},
{"footprint":"POLYGON ICRS -149.07863940050865 54.316666400429128  210.8997343 54.3412951  -149.18940419870853 54.320279300279118  210.8322644 54.2956666  -149.16773551665383 54.295666619029653  -149.16773550097395 54.295666600634753  -149.1677332007242 54.295667143596759  210.8322671 54.2956668  -149.16773281679977 54.295666819250762  210.8322672 54.2956668  -149.16773033614933 54.295667382768748  210.83227 54.295667  -149.16772991766 54.295667019180819  210.8322701 54.295667  -149.12201303886465 54.3064525718043  -149.078641999113 54.316663501382024  -149.07864344537094 54.316665148538995  -149.07864110319181 54.316665700455019  -149.07864126391823 54.316665885129567  -149.07863949964144 54.316666300606919  -149.07863955710803 54.3166663651234  -149.07863940050865 54.316666400429128POLYGON ICRS  -149.05585060388304 54.290879300470166  210.9221388 54.3164599  -149.07786128343082 54.316459879667093  -149.07786130114906 54.316459899663009  -149.09256471528943 54.312866281300067  -149.16751200049956 54.294511199586488  210.8545425 54.2689468  -149.05585060388304 54.290879300470166","_id_":"ext-record-1454"},
{"footprint":"POLYGON ICRS -149.07864110031875 54.316665700652095  -149.10026650076389 54.341294000043618  -149.18940050070168 54.32027910015649  -149.16773290042215 54.29566680052222  -149.16773281754783 54.295666820208353  210.8322672 54.2956668  -149.12077474655814 54.306745669386466  -149.07864110031875 54.316665700652095POLYGON ICRS  -149.05585189874887 54.290878999847124  -149.0778621002396 54.316459499513314  -149.16751070118056 54.294511500407268  -149.14545659886949 54.268947299894833  -149.05585189874887 54.290878999847124","_id_":"ext-record-1455"},
{"footprint":"POLYGON ICRS -149.078642000481 54.316663500673151  -149.09973058808296 54.340679803338332  210.8997328 54.3412905  -149.10488623115967 54.340203700214644  -149.18939739973808 54.320277999276641  -149.16773000011437 54.295667000761775  -149.16772991734646 54.295667020304812  210.8322701 54.295667  -149.1207736935574 54.3067446398871  -149.078642000481 54.316663500673151POLYGON ICRS  -149.0558541013356 54.290878900336153  -149.07786359955179 54.316457899873164  -149.16750840000617 54.294511499343017  -149.1454551001469 54.268948900884709  -149.0558541013356 54.290878900336153","_id_":"ext-record-1456"},
{"footprint":"POLYGON ICRS -149.16519720088496 54.29588320044224  -149.18166850076329 54.32183039996449  -149.27440729979338 54.307110299968443  210.7421164 54.2811756  -149.25788341768109 54.281175628477122  -149.2578833989171 54.281175600409519  -149.16519730096726 54.295883099036232  -149.16519735029738 54.295883176799  -149.16519720088496 54.29588320044224POLYGON ICRS  -149.14780090028106 54.268705200293432  210.8355333 54.2956245  -149.25789549935925 54.280012799194637  210.7588249 54.2531064  -149.14780090028106 54.268705200293432","_id_":"ext-record-1457"},
{"footprint":"POLYGON ICRS -149.16519720085992 54.295883198547386  -149.18166850076329 54.32183039996449  -149.27440730056429 54.307110299411768  210.7421164 54.2811756  -149.25788341768109 54.281175628477122  -149.2578833989171 54.281175600409519  -149.25788103819474 54.281175975167372  -149.25788080053422 54.281175599938635  -149.25787816870357 54.281176019025757  210.7421221 54.2811756  -149.25787780771233 54.28117561445125  210.7421222 54.2811756  -149.16520040105806 54.295880499511533  -149.16520149054818 54.295882219655546  -149.16519909990058 54.295882597888514  -149.16519910004891 54.29588260067996  -149.16519910681438 54.295882611460264  -149.16519922290431 54.295882793890662  -149.16519730094359 54.295883097141846  -149.16519734989586 54.295883175842825  -149.16519720085992 54.295883198547386POLYGON ICRS  -149.14780090140636 54.268705200712873  210.8355333 54.2956245  -149.257895500641 54.280012798777065  210.7588249 54.2531064  -149.14780090140636 54.268705200712873","_id_":"ext-record-1458"},
{"footprint":"POLYGON ICRS -149.16519910076929 54.295882600951323  -149.1816695016889 54.321829399624782  -149.27440370050567 54.307109800094715  -149.2578808008316 54.281175599837177  -149.16519910076929 54.295882600951323POLYGON ICRS  -149.14780230040745 54.268704999142905  -149.14780230113254 54.268705000314952  -149.14780229916155 54.26870500064333  -149.16446769997447 54.295624099922712  -149.25789410096093 54.280013000215895  -149.24117410057193 54.253106799836921  -149.14780230040745 54.268704999142905","_id_":"ext-record-1459"},
{"footprint":"POLYGON ICRS -149.16520040167632 54.295880499149028  -149.16520040242776 54.295880500333574  -149.16520040043022 54.295880500649631  -149.16574831565134 54.296744183022717  -149.18167079837534 54.32182610034058  -149.27440090184177 54.307108499106015  -149.25787790049077 54.281175599730446  -149.25787780869879 54.281175614103539  210.7421222 54.2811756  -149.1724196363165 54.294737969943405  -149.16520040167632 54.295880499149028POLYGON ICRS  -149.14780450179967 54.268705000289529  -149.16446939938606 54.295622599053821  -149.16446948997233 54.295622583740858  210.8355305 54.2956226  -149.21632673173329 54.28696833628706  -149.25789190089873 54.280012998532165  -149.24117230149986 54.253108299780351  -149.14780450179967 54.268705000289529","_id_":"ext-record-1460"},
{"footprint":"POLYGON ICRS -149.17668400030973 54.30731099982836  -149.17668400024721 54.307311000217204  -149.17668399999812 54.307311000203512  -149.17506500025104 54.317380000000504  -149.19233199980482 54.318326999853717  -149.1939540000628 54.308258999801616  -149.17668400030973 54.30731099982836","_id_":"ext-record-1461"},
{"footprint":"POLYGON ICRS -149.17668400030973 54.30731099982836  -149.17668400024721 54.307311000217204  -149.17668399999812 54.307311000203512  -149.17506500025104 54.317380000000504  -149.19233199980482 54.318326999853717  -149.1939540000628 54.308258999801616  -149.17668400030973 54.30731099982836","_id_":"ext-record-1462"},
{"footprint":"POLYGON ICRS -149.26329799811109 54.313246001436269  -149.25729699827696 54.335109004733482  -149.25744203316796 54.335122572533642  -149.2523239967303 54.355005004944886  -149.28545253573523 54.35790349955019  -149.28540500127224 54.358094992928578  -149.322943989489 54.361257988280592  -149.32296484279777 54.361174101698367  -149.32779988814553 54.341680006561404  -149.32801912096116 54.340795681907757  -149.32837601465923 54.33935600322075  -149.30926215128622 54.337744890631939  -149.31155197313495 54.329084002938416  -149.29771641105575 54.327844828555563  -149.30076498309634 54.316742006230527  -149.26329799811109 54.313246001436269","_id_":"ext-record-1463"},
{"footprint":"POLYGON ICRS -149.26329799811109 54.313246001436262  -149.25729699827696 54.335109004733475  -149.25744203316796 54.335122572533642  -149.25232399673033 54.355005004944886  -149.28545253573523 54.357903499550183  -149.28540500127227 54.358094992928571  -149.322943989489 54.361257988280585  -149.32296484279777 54.36117410169836  -149.32779988814553 54.341680006561404  -149.32801912096116 54.340795681907757  -149.32837601465923 54.339356003220743  -149.30926215128625 54.337744890631932  -149.31155197313498 54.329084002938409  -149.29771641105575 54.327844828555556  -149.30076498309634 54.316742006230527  -149.26329799811109 54.313246001436262","_id_":"ext-record-1464"},
{"footprint":"POLYGON ICRS -149.26329799978376 54.313245999079967  -149.26329799935107 54.313246000657244  -149.26329799854039 54.313246000581479  -149.26329799815488 54.313246002101323  -149.258803173803 54.3296238796725  -149.25729700013548 54.335109000540513  -149.25729699827696 54.335109004733475  -149.25744203316796 54.335122572533642  -149.25232399673033 54.355005004944886  -149.27925983824818 54.357362405981348  -149.28545253055213 54.3579034993109  -149.28540499945842 54.358095000411431  -149.32294400076407 54.361258000227856  -149.32295185921768 54.361226330031414  -149.32296484279777 54.36117410169836  -149.32766248715464 54.34223424085085  210.67220011 54.34168  -149.32784180787806 54.341510915574339  -149.32801912096116 54.340795681907757  -149.32837601465923 54.339356003220743  -149.32837600063533 54.339356001853808  -149.32837600103883 54.339356000225969  -149.30926215535607 54.337744880893368  -149.31010093056005 54.334572771053914  -149.311551999639 54.329084000804045  -149.30367801610097 54.328378979082345  -149.29771641105575 54.327844828555556  -149.2994600651254 54.321495227780389  -149.30076500074188 54.316741999980664  -149.26329799978376 54.313245999079967","_id_":"ext-record-1465"},
{"footprint":"POLYGON ICRS -149.13620669818553 54.435297500355546  -149.16897419911902 54.456651599732531  -149.24598530084 54.421856100048373  -149.21320550065977 54.400522799632533  -149.13620669818553 54.435297500355546POLYGON ICRS  -149.10356599823413 54.413705000762342  -149.13547730064329 54.434215100790524  -149.13547780876942 54.434214878009605  210.864522 54.434215  -149.21235999935425 54.40044290067749  -149.21235971072261 54.400442715551385  -149.2123601984166 54.400442500988966  -149.18043689940882 54.379952700328474  -149.10356599823413 54.413705000762342","_id_":"ext-record-1466"},
{"footprint":"POLYGON ICRS -149.13620669818553 54.435297500355546  -149.16897419911902 54.456651599732531  -149.24598530084 54.421856100048373  -149.21320550065977 54.400522799632533  -149.13620669818553 54.435297500355546POLYGON ICRS  -149.10356599823413 54.413705000762342  -149.13547730064329 54.434215100790524  -149.13547780876942 54.434214878009605  210.864522 54.434215  -149.21235999935425 54.40044290067749  -149.21235971072261 54.400442715551385  -149.2123601984166 54.400442500988966  -149.18043689940882 54.379952700328474  -149.10356599823413 54.413705000762342","_id_":"ext-record-1467"},
{"footprint":"POLYGON ICRS -149.22914780016228 54.376059900920509  210.7789303 54.4325909  -149.270275301321 54.432988899456078  -149.27028041065756 54.432952889552332  -149.27099240023233 54.432958500076261  -149.27099752078965 54.432922393685168  -149.2717094022785 54.432928000987808  -149.27174402334472 54.432683931410978  -149.31851959895567 54.432835700150612  -149.31852498526783 54.432802833047433  -149.31923680108275 54.432805001252426  -149.31924220393324 54.432772035271896  -149.31995349838871 54.432774199312831  -149.32908569986577 54.376962000287932  -149.28158110007448 54.376811700912256  -149.28157570609295 54.3768444889485  -149.28086509875649 54.3768421009583  -149.28085968479849 54.376874995735378  -149.28014889903324 54.376872600498466  -149.27660011982556 54.398421162347255  -149.27971779818509 54.376393700295431  -149.23058010156313 54.375999601553595  -149.2305749866251 54.376035450907331  -149.22986389810399 54.37602960123737  -149.22985873975162 54.376065753829444  -149.22914780016228 54.376059900920509","_id_":"ext-record-1468"},
{"footprint":"POLYGON ICRS -149.229147801899 54.376059902074843  210.7789303 54.4325909  -149.27027529856477 54.43298890100646  -149.27028040799766 54.432952890070908  -149.2709923995547 54.432958499484009  -149.27099752026839 54.432922393707514  -149.27170940117819 54.432928001257821  -149.2717440251439 54.432683932673591  -149.31851959704386 54.432835700862007  -149.31852498272303 54.432802834116089  -149.31923680384153 54.4328050005415  -149.31924220448184 54.4327720344428  -149.31995350073817 54.4327741994701  -149.32908569986577 54.376962000287932  -149.28158110390453 54.376811700806485  -149.28158083910856 54.376813304154119  -149.28157980310789 54.376813299672271  -149.28157953831186 54.376814903019906  -149.28157880200641 54.376814901387519  -149.28157393341192 54.376844482905575  -149.2808650978335 54.376842102281138  -149.28086483550032 54.376843702624278  -149.28086379825348 54.376843699637767  -149.28086351657183 54.376845403801354  -149.28086269954187 54.376845399819587  -149.28085782935597 54.376874990267034  -149.28014889967866 54.376872599475419  -149.28014861796714 54.376874303637337  -149.28014769851822 54.376874300459868  -149.280147434664 54.376875903506686  -149.28014660000969 54.37687590069492  -149.27661560700849 54.398311799152346  -149.27971779818509 54.376393700295431  -149.23058010130404 54.375999602172378  -149.23057498619605 54.376035450919986  -149.22986389816765 54.3760296010114  -149.22985873921388 54.376065754339294  -149.229147801899 54.376059902074843","_id_":"ext-record-1469"},
{"footprint":"POLYGON ICRS -149.22914830096 54.3760607995657  210.77893 54.4325899  -149.27027490064955 54.43298799936958  -149.27027999647572 54.432952086336947  -149.2709919992825 54.432957699932494  -149.27099713541818 54.432921490498629  -149.27170899766151 54.4329270979536  -149.27174369220222 54.432682536231475  -149.31851789863245 54.432834600540247  -149.31852328580666 54.432801726704909  -149.31923509940546 54.432803899425757  -149.3192405009211 54.4327709312188  -149.31995189934119 54.432773100384182  -149.32908309907143 54.376963899572317  -149.28157979881161 54.376813301211705  -149.28157440367 54.376846094477692  -149.28086380128812 54.376843700473977  -149.280858370684 54.376876697831541  -149.28014769989082 54.376874299915713  -149.27660291016377 54.398399317839512  -149.27971749830587 54.376394700274837  -149.2305804973974 54.376000500231427  -149.23057538378902 54.376036351812651  -149.22986430032392 54.376030499869636  -149.22985914162507 54.376066653220931  -149.22914830096 54.3760607995657","_id_":"ext-record-1470"},
{"footprint":"POLYGON ICRS -149.22915010014336 54.3760622999183  210.7789291 54.4325887  -149.27027309922806 54.432986499762755  -149.27027820958278 54.432950490103273  -149.27099020192742 54.432956099617243  -149.27099532381942 54.43291999545324  -149.27170719877051 54.432925599087341  -149.27174189873705 54.432681032534639  -149.31851289828558 54.432832600308281  -149.31851826948969 54.432799832622621  -149.31923000015198 54.432801999223216  -149.31923540185156 54.432769038473126  -149.31994689878746 54.432771200145289  -149.32907999879174 54.37696509977566  -149.28157879807412 54.376814900963481  -149.28157338589605 54.376847787589952  -149.28086269911097 54.376845399469268  -149.28085728442818 54.376878291766943  -149.28014660050167 54.376875899692962  -149.276604723042 54.398377839724596  -149.27971659903309 54.37639590045832  -149.23058230045396 54.37600199978192  -149.230577169778 54.376037948444939  -149.22986610174587 54.376032100875051  -149.22986095719151 54.376068149668171  -149.22915010014336 54.3760622999183","_id_":"ext-record-1471"},
{"footprint":"POLYGON ICRS -149.28086190073526 54.376845800429  -149.27166449839436 54.432647100459327  -149.31922950065081 54.432801300302941  -149.32836229964593 54.3769963991312  -149.28086190073526 54.376845800429POLYGON ICRS  -149.22986589954439 54.376033100174631  -149.22178929993805 54.432557599588222  -149.27099040122567 54.432955099621417  -149.27899940070594 54.376427300484224  -149.22986589954439 54.376033100174631","_id_":"ext-record-1472"},
{"footprint":"POLYGON ICRS -149.19644599972483 54.415805000633327  -149.23044799958794 54.425705999897488  -149.23910666093869 54.415637730298037  -149.25250400069351 54.419629999556889  -149.25888725952709 54.412366079765683  -149.27603099928155 54.417546999394077  -149.29354699947933 54.3979150007188  -149.29354699940598 54.397915000696635  -149.2935470007252 54.397914999217249  -149.2597880004316 54.387712000041169  -149.25972047120914 54.387787709606627  -149.2289110003139 54.378695999414404  -149.21373938164808 54.39611623974529  -149.21343999914689 54.396028999243214  -149.21167838102198 54.398080010667606  -149.19644599972483 54.415805000633327","_id_":"ext-record-1473"},
{"footprint":"POLYGON ICRS -149.19644599972483 54.415805000633327  -149.23044799958794 54.425705999897488  -149.23910666093869 54.415637730298037  -149.25250400069351 54.419629999556889  -149.25888725952709 54.412366079765683  -149.27603099928155 54.417546999394077  -149.29354699947933 54.3979150007188  -149.29354699940598 54.397915000696635  -149.2935470007252 54.397914999217249  -149.2597880004316 54.387712000041169  -149.25972047120914 54.387787709606627  -149.2289110003139 54.378695999414404  -149.21373938164808 54.39611623974529  -149.21343999914689 54.396028999243214  -149.21167838102198 54.398080010667606  -149.19644599972483 54.415805000633327","_id_":"ext-record-1474"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.269738999096077  -149.13859199961496 54.291846000143288  -149.1578616891932 54.291338299800785  -149.15840200106626 54.300093000491493  -149.17237442931261 54.299795430406917  -149.17295500043309 54.311033999709451  -149.21083499831235 54.3103650003398  -149.20968800104893 54.288234999799563  -149.20954228077278 54.288237589564893  -149.20816499913931 54.2681479992364  -149.17473311017082 54.26892960949499  -149.17471799977471 54.268735000543082  -149.17077696831092 54.268840110141348  -149.13687399996786 54.269738999096077","_id_":"ext-record-1475"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.269738999096077  -149.13859199961496 54.291846000143288  -149.1578616891932 54.291338299800785  -149.15840200106626 54.300093000491493  -149.17237442931261 54.299795430406917  -149.17295500043309 54.311033999709451  -149.21083499831235 54.3103650003398  -149.20968800104893 54.288234999799563  -149.20954228077278 54.288237589564893  -149.20816499913931 54.2681479992364  -149.17473311017082 54.26892960949499  -149.17471799977471 54.268735000543082  -149.17077696831092 54.268840110141348  -149.13687399996786 54.269738999096077","_id_":"ext-record-1476"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143295  -149.1578616891932 54.291338299800785  -149.15840200106626 54.300093000491493  -149.17237442931261 54.29979543040691  -149.17295500043309 54.311033999709451  -149.21083499831235 54.3103650003398  -149.20968800104893 54.28823499979957  -149.20954228077278 54.288237589564893  -149.20816499913931 54.2681479992364  -149.17473311017082 54.26892960949499  -149.17471799977471 54.268735000543074  -149.17077696831092 54.268840110141348  -149.13687399996786 54.26973899909607","_id_":"ext-record-1477"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143281  -149.1578616891932 54.291338299800778  -149.15840200106626 54.300093000491486  -149.17237442931261 54.29979543040691  -149.17295500043309 54.311033999709444  -149.21083499831235 54.310365000339793  -149.20968800104893 54.288234999799556  -149.20954228077278 54.288237589564886  -149.20816499913931 54.2681479992364  -149.17473311017082 54.268929609494982  -149.17471799977471 54.268735000543074  -149.17077696831092 54.268840110141348  -149.13687399996786 54.26973899909607","_id_":"ext-record-1478"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143281  -149.1578616891932 54.291338299800778  -149.15840200106626 54.300093000491486  -149.17237442931261 54.29979543040691  -149.17295500043309 54.311033999709444  -149.21083499831235 54.310365000339793  -149.20968800104893 54.288234999799556  -149.20954228077278 54.288237589564886  -149.20816499913931 54.2681479992364  -149.17473311017082 54.268929609494982  -149.17471799977471 54.268735000543074  -149.17077696831092 54.268840110141348  -149.13687399996786 54.26973899909607","_id_":"ext-record-1479"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143281  -149.1578616891932 54.291338299800778  -149.15840200106626 54.300093000491486  -149.17237442931261 54.29979543040691  -149.17295500043309 54.311033999709444  -149.21083499831235 54.310365000339793  -149.20968800104893 54.288234999799556  -149.20954228077278 54.288237589564886  -149.20816499913931 54.2681479992364  -149.17473311017082 54.268929609494982  -149.17471799977471 54.268735000543074  -149.17077696831092 54.268840110141348  -149.13687399996786 54.26973899909607","_id_":"ext-record-1480"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143281  -149.1578616891932 54.291338299800778  -149.15840200106626 54.300093000491486  -149.17237442931261 54.29979543040691  -149.17295500043309 54.311033999709444  -149.21083499831235 54.310365000339793  -149.20968800104893 54.288234999799556  -149.20954228077278 54.288237589564886  -149.20816499913931 54.2681479992364  -149.17473311017082 54.268929609494982  -149.17471799977471 54.268735000543074  -149.17077696831092 54.268840110141348  -149.13687399996786 54.26973899909607","_id_":"ext-record-1481"},
{"footprint":"POLYGON ICRS -149.13687399996786 54.26973899909607  -149.13859199961496 54.291846000143273  -149.1578616891932 54.291338299800771  -149.15840200106626 54.300093000491479  -149.17237442931261 54.2997954304069  -149.17295500043309 54.311033999709437  -149.21083499831235 54.310365000339793  -149.20968800104893 54.288234999799556  -149.20954228077278 54.288237589564886  -149.20816499913931 54.2681479992364  -149.17473311017082 54.268929609494975  -149.17471799977471 54.268735000543067  -149.17077696831092 54.26884011014134  -149.13687399996786 54.26973899909607","_id_":"ext-record-1482"},
{"footprint":"POLYGON ICRS -149.13687399991673 54.269738999074178  -149.13859199956383 54.2918460001214  -149.15786168914204 54.2913382997789  -149.15840200101508 54.300093000469616  -149.17237442926142 54.299795430385039  -149.17295500038188 54.311033999687574  -149.21083499826113 54.310365000317937  -149.20968800099777 54.2882349997777  -149.20954228072162 54.28823758954303  -149.20816499908818 54.268147999214534  -149.17473311011969 54.268929609473112  -149.17471799972358 54.2687350005212  -149.17077696831092 54.268840110141348  -149.13687399991673 54.269738999074178","_id_":"ext-record-1483"},
{"footprint":"POLYGON ICRS -149.13687399991673 54.269738999074178  -149.1385919995638 54.291846000121396  -149.15786168914204 54.291338299778893  -149.15840200101508 54.300093000469609  -149.17237442926142 54.299795430385039  -149.17295500038188 54.311033999687567  -149.21083499826113 54.310365000317937  -149.20968800099774 54.288234999777693  -149.20954228072159 54.288237589543023  -149.20816499908815 54.268147999214527  -149.17473311011966 54.268929609473105  -149.17471799972355 54.2687350005212  -149.17077696831092 54.26884011014134  -149.13687399991673 54.269738999074178","_id_":"ext-record-1484"},
{"footprint":"POLYGON ICRS -149.22305599898365 54.392023999345959  -149.25256799963023 54.405985000038129  -149.25266007109315 54.405919069947849  -149.27967200055426 54.41844399974589  -149.30056828939118 54.403167039684256  -149.30082999868719 54.403287000388957  -149.32432700076987 54.385916000546956  -149.3242120990997 54.385863329944591  -149.29764039988561 54.373676490442008  -149.29643308043254 54.373122450236217  -149.29447100083419 54.3722219997675  -149.28250553961965 54.3810670290166  -149.27076999959573 54.375580999412868  -149.26199778042181 54.381947609296063  -149.24701300134592 54.37485499948135  -149.22305599898365 54.392023999345959","_id_":"ext-record-1485"},
{"footprint":"POLYGON ICRS -149.22305599897345 54.392023999425682  -149.25256799962006 54.405985000117859  -149.25266007108297 54.405919070027586  -149.27967200054414 54.41844399982562  -149.30056828938109 54.403167039764  -149.30082999867713 54.403287000468708  -149.32432700075987 54.385916000626708  -149.32421209908969 54.385863330024343  -149.29764039987555 54.373676490521746  -149.29643308042245 54.373122450315961  -149.29447100082413 54.372221999847241  -149.28250553961965 54.3810670290166  -149.27076999958564 54.375580999492605  -149.26199778041166 54.381947609375793  -149.24701300134592 54.37485499948135  -149.22305599897345 54.392023999425682","_id_":"ext-record-1486"},
{"footprint":"POLYGON ICRS -149.22305599898365 54.392023999345959  -149.25256799963023 54.405985000038129  -149.25266007109315 54.405919069947849  -149.27967200055426 54.41844399974589  -149.30056828939118 54.403167039684256  -149.30082999868719 54.403287000388957  -149.32432700076987 54.385916000546956  -149.3242120990997 54.385863329944591  -149.29764039988561 54.373676490442008  -149.29643308043254 54.373122450236217  -149.29447100083419 54.3722219997675  -149.28250553961965 54.3810670290166  -149.27076999959573 54.375580999412868  -149.26199778042181 54.381947609296063  -149.24701300134592 54.37485499948135  -149.22305599898365 54.392023999345959","_id_":"ext-record-1487"},
{"footprint":"POLYGON ICRS -149.26382469995477 54.354254199987686  -149.2616608999906 54.362243700063686  -149.2737692000108 54.362635899961788  -149.2759307000438 54.354646199986831  -149.26382469995477 54.354254199987686","_id_":"ext-record-1488"},
{"footprint":"POLYGON ICRS -149.06790400034302 54.305710000294617  -149.06699599886628 54.327823999891585  -149.1049339990013 54.328353999639  -149.10493749897111 54.328269199850084  -149.13939500040993 54.328554000152167  -149.13986946020546 54.309026330358655  -149.14020300034903 54.309028000705737  -149.14053499957848 54.286916000115056  -149.1404061505043 54.286915350385591  -149.10664759841086 54.286741450272011  -149.10510353073505 54.286733269456114  -149.10260599977209 54.286720000119836  -149.10243343074396 54.297979490553857  -149.08741799885485 54.297820000262767  -149.08717021965325 54.305981640534128  -149.06790400034302 54.305710000294617","_id_":"ext-record-1489"},
{"footprint":"POLYGON ICRS -149.06790400034305 54.30571000029461  -149.06699599886628 54.327823999891585  -149.1049339990013 54.328353999638992  -149.10493749897111 54.328269199850077  -149.13939500040993 54.328554000152167  -149.13986946020546 54.309026330358648  -149.14020300034903 54.30902800070573  -149.14053499957848 54.286916000115049  -149.1404061505043 54.286915350385584  -149.10664759841086 54.286741450272011  -149.10510353073505 54.286733269456114  -149.10260599977209 54.286720000119828  -149.102433430744 54.29797949055385  -149.08741799885485 54.297820000262767  -149.08717021965327 54.305981640534121  -149.06790400034305 54.30571000029461","_id_":"ext-record-1490"},
{"footprint":"POLYGON ICRS -149.06790400028297 54.305710000337783  -149.06699599880616 54.327823999934758  -149.10493399894128 54.328353999682193  -149.10493749891106 54.328269199893271  -149.13939500034994 54.328554000195382  -149.13986946014546 54.309026330401885  -149.14020300028903 54.309028000748953  -149.14053499951854 54.286916000158264  -149.14040615044436 54.286915350428806  -149.10664759835089 54.286741450315205  -149.10510353067508 54.286733269499308  -149.1026059997121 54.286720000163022  -149.10243343068396 54.297979490597037  -149.08741799879482 54.297820000305954  -149.08717021959322 54.3059816405773  -149.06790400028297 54.305710000337783","_id_":"ext-record-1491"},
{"footprint":"POLYGON ICRS -149.10566299871843 54.306535000460322  -149.13897500067881 54.317109000138757  -149.14819634924496 54.307219570285319  -149.16131600054641 54.311476000260726  -149.16810563137389 54.304343839715813  -149.18489000089932 54.309863999698727  -149.20350200024879 54.290593000109858  -149.17045199986663 54.279721999288519  -149.1703794597185 54.2797971298439  -149.14020400075853 54.270096000509518  -149.124057369825 54.287203180304175  -149.12376399935224 54.287110000738004  -149.12188829922428 54.289123880563409  -149.10566299871843 54.306535000460322","_id_":"ext-record-1492"},
{"footprint":"POLYGON ICRS -149.10566299871846 54.306535000460322  -149.13897500067881 54.317109000138757  -149.14819634924496 54.307219570285319  -149.16131600054641 54.311476000260733  -149.16810563137389 54.304343839715813  -149.18489000089932 54.309863999698727  -149.20350200024882 54.290593000109865  -149.17045199986666 54.279721999288519  -149.1703794597185 54.2797971298439  -149.14020400075856 54.270096000509518  -149.124057369825 54.287203180304182  -149.12376399935226 54.287110000738004  -149.12188829922428 54.289123880563409  -149.10566299871846 54.306535000460322","_id_":"ext-record-1493"},
{"footprint":"POLYGON ICRS -149.10566299871846 54.306535000460322  -149.13897500067881 54.317109000138757  -149.14819634924496 54.307219570285319  -149.16131600054641 54.311476000260733  -149.16810563137389 54.304343839715813  -149.18489000089932 54.309863999698727  -149.20350200024882 54.290593000109865  -149.17045199986666 54.279721999288519  -149.1703794597185 54.2797971298439  -149.14020400075856 54.270096000509518  -149.124057369825 54.287203180304182  -149.12376399935226 54.287110000738004  -149.12188829922428 54.289123880563409  -149.10566299871846 54.306535000460322","_id_":"ext-record-1494"},
{"footprint":"POLYGON ICRS -149.10566299871843 54.306535000460322  -149.13897500067881 54.317109000138757  -149.14819634924496 54.307219570285319  -149.16131600054641 54.311476000260726  -149.16810563137389 54.304343839715813  -149.18489000089932 54.309863999698727  -149.20350200024879 54.290593000109858  -149.17045199986663 54.279721999288519  -149.1703794597185 54.2797971298439  -149.14020400075853 54.270096000509518  -149.124057369825 54.287203180304175  -149.12376399935224 54.287110000738004  -149.12188829922428 54.289123880563409  -149.10566299871843 54.306535000460322","_id_":"ext-record-1495"},
{"footprint":"POLYGON ICRS -149.10566299871843 54.306535000460315  -149.13897500067881 54.31710900013875  -149.14819634924496 54.307219570285312  -149.16131600054641 54.311476000260718  -149.16810563137389 54.304343839715806  -149.18489000089932 54.30986399969872  -149.20350200024879 54.290593000109851  -149.17045199986663 54.279721999288505  -149.1703794597185 54.279797129843892  -149.14020400075853 54.270096000509511  -149.124057369825 54.287203180304175  -149.12376399935224 54.287110000738  -149.12188829922428 54.2891238805634  -149.10566299871843 54.306535000460315","_id_":"ext-record-1496"},
{"footprint":"POLYGON ICRS -149.10566299871843 54.306535000460315  -149.13897500067881 54.31710900013875  -149.14819634924496 54.307219570285312  -149.16131600054641 54.311476000260718  -149.16810563137389 54.304343839715806  -149.18489000089932 54.30986399969872  -149.20350200024879 54.290593000109851  -149.17045199986663 54.279721999288505  -149.1703794597185 54.279797129843892  -149.14020400075853 54.270096000509511  -149.124057369825 54.287203180304175  -149.12376399935224 54.287110000738  -149.12188829922428 54.2891238805634  -149.10566299871843 54.306535000460315","_id_":"ext-record-1497"},
{"footprint":"POLYGON ICRS -149.10566299870675 54.306535000411152  -149.13897500066707 54.317109000089573  -149.14819634923322 54.307219570236128  -149.16131600053467 54.311476000211549  -149.16810563136212 54.304343839666629  -149.18489000088752 54.309863999649551  -149.203502000237 54.290593000060682  -149.17045199985489 54.279721999239335  -149.17037945970671 54.279797129794716  -149.14020400074679 54.270096000460342  -149.1240573698133 54.287203180255  -149.12376399934055 54.28711000068882  -149.1218882992126 54.289123880514232  -149.10566299870675 54.306535000411152","_id_":"ext-record-1498"},
{"footprint":"POLYGON ICRS -149.10566299870675 54.306535000411145  -149.13897500066707 54.317109000089573  -149.14819634923325 54.307219570236128  -149.16131600053467 54.311476000211549  -149.16810563136212 54.304343839666629  -149.18489000088755 54.309863999649544  -149.203502000237 54.290593000060674  -149.17045199985489 54.279721999239335  -149.17037945970674 54.279797129794716  -149.14020400074679 54.270096000460342  -149.1240573698133 54.287203180255  -149.12376399934055 54.28711000068882  -149.1218882992126 54.289123880514225  -149.10566299870675 54.306535000411145","_id_":"ext-record-1499"},
{"footprint":"POLYGON ICRS -149.10566299870675 54.306535000411145  -149.13897500066707 54.317109000089573  -149.14819634923325 54.307219570236128  -149.16131600053467 54.311476000211549  -149.16810563136212 54.304343839666629  -149.18489000088755 54.309863999649544  -149.203502000237 54.290593000060674  -149.17045199985489 54.279721999239335  -149.17037945970674 54.279797129794716  -149.14020400074679 54.270096000460342  -149.1240573698133 54.287203180255  -149.12376399934055 54.28711000068882  -149.1218882992126 54.289123880514225  -149.10566299870675 54.306535000411145","_id_":"ext-record-1500"},
{"footprint":"POLYGON ICRS -149.14616400040728 54.306559999972613  -149.16131599977552 54.311475999834528  -149.16973199996622 54.302634999916691  -149.15458100016269 54.297720999901117  -149.14616400040728 54.306559999972613","_id_":"ext-record-1501"},
{"footprint":"POLYGON ICRS -149.14616400040728 54.306559999972613  -149.16131599977552 54.311475999834528  -149.16973199996622 54.302634999916691  -149.15458100016269 54.297720999901117  -149.14616400040728 54.306559999972613","_id_":"ext-record-1502"},
{"footprint":"POLYGON ICRS -149.14616400040728 54.306559999972613  -149.16131599977552 54.311475999834528  -149.16973199996622 54.302634999916691  -149.15458100016269 54.297720999901117  -149.14616400040728 54.306559999972613","_id_":"ext-record-1503"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925732  -149.16131599981452 54.311475999787639  -149.16973200000521 54.302634999869795  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925732","_id_":"ext-record-1504"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925732  -149.16131599981452 54.311475999787639  -149.16973200000521 54.302634999869795  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925732","_id_":"ext-record-1505"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925724  -149.16131599981452 54.311475999787632  -149.16973200000521 54.302634999869788  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925724","_id_":"ext-record-1506"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925724  -149.16131599981452 54.311475999787632  -149.16973200000521 54.302634999869788  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925724","_id_":"ext-record-1507"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925732  -149.16131599981449 54.311475999787639  -149.16973200000521 54.3026349998698  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925732","_id_":"ext-record-1508"},
{"footprint":"POLYGON ICRS -149.14616400044628 54.306559999925732  -149.16131599981449 54.311475999787639  -149.16973200000521 54.3026349998698  -149.15458100020166 54.297720999854235  -149.14616400044628 54.306559999925732","_id_":"ext-record-1509"},
{"footprint":"POLYGON ICRS -149.12342299993955 54.38561799996404  210.861142 54.390259  -149.14681600011534 54.381269999893362  -149.131383000307 54.376631999891025  -149.12342299993955 54.38561799996404","_id_":"ext-record-1510"},
{"footprint":"POLYGON ICRS -149.12342299993955 54.38561799996404  210.861142 54.390259  -149.14681600011534 54.381269999893362  -149.131383000307 54.376631999891025  -149.12342299993955 54.38561799996404","_id_":"ext-record-1511"},
{"footprint":"POLYGON ICRS -149.13048900033363 54.392257000390572  -149.128645240241 54.411757709375344  -149.12831100052477 54.411747999590204  -149.12811598097474 54.41403881036171  -149.12643000054854 54.433833000467445  -149.16441100005272 54.434930999650447  -149.16537255878566 54.423686040519769  -149.18040200123639 54.424202000420564  -149.1812225810888 54.416056340547684  -149.20050299950995 54.41678299947219  -149.20296200032416 54.394708999520027  -149.16499500031222 54.393275999438835  -149.16498537935067 54.393362280694227  -149.13048900033363 54.392257000390572","_id_":"ext-record-1512"},
{"footprint":"POLYGON ICRS -149.13048900033363 54.392257000390572  -149.128645240241 54.411757709375344  -149.12831100052477 54.411747999590204  -149.12811598097474 54.41403881036171  -149.12643000054854 54.433833000467445  -149.16441100005272 54.434930999650447  -149.16537255878566 54.423686040519769  -149.18040200123639 54.424202000420564  -149.1812225810888 54.416056340547684  -149.20050299950995 54.41678299947219  -149.20296200032416 54.394708999520027  -149.16499500031222 54.393275999438835  -149.16498537935067 54.393362280694227  -149.13048900033363 54.392257000390572","_id_":"ext-record-1513"},
{"footprint":"POLYGON ICRS -149.00466199554174 54.303208000587624  -149.01409039929479 54.312464441637793  210.975946 54.322241  -149.02417890973686 54.322197730636233  -149.02622618527431 54.32416387246721  210.957923 54.339379  -149.04869266892609 54.337035128563848  -149.05033833491677 54.336451989496737  -149.06406298288289 54.349613989380678  -149.09267623440931 54.339467392761918  -149.09285001138929 54.339632007351938  -149.12513701251058 54.328040986469496  -149.12506111376769 54.327969123800834  -149.10736075515783 54.311195336733057  -149.10655718499672 54.310433452458604  -149.10524999375539 54.30919399548106  -149.09712783746193 54.312110261984962  210.91461745 54.30096724  -149.08496119559419 54.300567354827983  -149.08458124900486 54.300206733547434  -149.08360400161865 54.299279263537592  210.916725 54.298967  -149.0832749939533 54.298967003024188  -149.08327498184943 54.29896699168895  -149.06683573900298 54.30486561986806  210.940914 54.297359  -149.04986497198161 54.30060175738479  -149.04710334994138 54.301572699248929  -149.04165046362661 54.296218875775068  -149.03725901869549 54.291905996982337  -149.03725900205967 54.29190600294185  210.962741 54.291906  -149.01009607499145 54.301324943898173  -149.00466199554174 54.303208000587624","_id_":"ext-record-1514"},
{"footprint":"POLYGON ICRS -149.00466200461662 54.303207989753858  -149.00466201000762 54.30320799589429  -149.00466199966704 54.303207999477181  -149.00792756628425 54.306414561928641  -149.01181717815589 54.31023312863018  -149.01409039581523 54.312464444007212  -149.01871500930858 54.317002916327162  -149.02405399944209 54.322240999168763  -149.02405399981856 54.322240998869837  210.975946 54.322241  -149.02412938552587 54.322214885662511  -149.02417891001966 54.3221977305537  -149.04207699944837 54.339379000238445  -149.04207701174292 54.339378996458358  -149.04207702000886 54.339379005025037  -149.04419963636747 54.338627038021  -149.04424375333477 54.338611408481491  -149.04426297616527 54.338604598321538  -149.04430417712948 54.338590002298069  -149.04561663114444 54.338125019419564  -149.04821132610027 54.337205685777093  -149.04999273727833 54.336574452949996  -149.0503383497161 54.3364519895627  -149.06406295938643 54.349613988829987  -149.09267624264083 54.339467390162781  -149.09285002345985 54.339632003358084  -149.12513700465811 54.328040977194924  -149.1250611023062 54.327969113012365  -149.10736075976928 54.311195345339371  -149.10655719183734 54.310433443471922  -149.10524997169705 54.30919399690751  -149.09712783789675 54.312110258337135  -149.08538255639618 54.300967238102906  -149.08507664478196 54.300676920823932  -149.08488949362703 54.300499303464711  -149.08458125953689 54.300206730785874  -149.08364599019026 54.299319082650129  -149.08336126861909 54.299048878692908  -149.0832750008519 54.298966999379367  -149.08327498636558 54.298967004578984  -149.08327496342287 54.298966999297086  -149.06789139089085 54.304486940960672  -149.06723975874743 54.30472069985241  -149.06718889186033 54.304738945760135  -149.06683574994557 54.304865615714604  -149.05908600638523 54.297358993557161  -149.04850286182622 54.301080658701636  -149.04794335629131 54.301277377661187  -149.04731257032256 54.301499144737228  -149.04710335802102 54.301572696687778  -149.04471882736948 54.299231689988  -149.04464312634755 54.29915737248011  -149.04357085193467 54.298104564113508  -149.04229524811871 54.296852032648495  -149.04086277854026 54.295445346101566  -149.03855633709156 54.293180232176276  -149.03725899924896 54.291906000486172  -149.03725899371935 54.291906002404666  -149.03725899114735 54.291905999630607  -149.033290559885 54.293282734938145  -149.02954324669997 54.294582553015545  -149.01459718797597 54.2997648693098  -149.00674384953723 54.3024866146462  -149.00466200461662 54.303207989753858","_id_":"ext-record-1515"},
{"footprint":"POLYGON ICRS -149.04346649736146 54.288520399515953  -149.04346682910648 54.288520869729545  -149.04346639938097 54.288521000458964  210.9199181 54.3403486  -149.08008224279578 54.340348496669506  -149.0800825989518 54.340349000624428  -149.1221437994846 54.327541900831811  -149.08548610165164 54.2757260996032  -149.04346649736146 54.288520399515953POLYGON ICRS  -148.99782030165892 54.301682901719325  -149.03602409956443 54.3538440004218  -149.0797354996478 54.34080460046858  -149.04148839993496 54.288657399856646  -148.99782030165892 54.301682901719325","_id_":"ext-record-1516"},
{"footprint":"POLYGON ICRS -149.04346649736146 54.288520399515953  -149.04346682910648 54.288520869729545  -149.04346639938097 54.288521000458964  210.9199181 54.3403486  -149.08008224279578 54.340348496669506  -149.0800825989518 54.340349000624428  -149.1221437994846 54.327541900831811  -149.08548610165164 54.2757260996032  -149.04346649736146 54.288520399515953POLYGON ICRS  -148.99782030165892 54.301682901719325  -149.03602409956443 54.3538440004218  -149.0797354996478 54.34080460046858  -149.04148839993496 54.288657399856646  -148.99782030165892 54.301682901719325","_id_":"ext-record-1517"},
{"footprint":"POLYGON ICRS -149.25990099954294 54.383469000025876  -149.27345000011758 54.389800999967626  -149.28431999952664 54.3819150001547  -149.28431999983795 54.381914999779319  -149.27077000012267 54.375581000141281  -149.25990099954294 54.383469000025876","_id_":"ext-record-1518"},
{"footprint":"POLYGON ICRS -149.25990099954294 54.383469000025876  -149.27345000011758 54.389800999967626  -149.28431999952664 54.3819150001547  -149.28431999983795 54.381914999779319  -149.27077000012267 54.375581000141281  -149.25990099954294 54.383469000025876","_id_":"ext-record-1519"},
{"footprint":"POLYGON ICRS -149.25990099982238 54.383468999985865  -149.27344999997422 54.389801000064132  -149.28431999986515 54.381915000145383  -149.27077000002689 54.375581000179942  -149.25990099982238 54.383468999985865","_id_":"ext-record-1520"},
{"footprint":"POLYGON ICRS -149.28025099997828 54.3645140002067  -149.2808930002536 54.374622999922458  -149.29824099980775 54.37424900003743  -149.29760199996028 54.364138999833543  -149.28025099997828 54.3645140002067","_id_":"ext-record-1521"},
{"footprint":"POLYGON ICRS -149.28025099997831 54.364514000206682  -149.28089300025363 54.374622999922444  -149.29824099980777 54.374249000037423  -149.29760199996028 54.364138999833543  -149.28025099997831 54.364514000206682","_id_":"ext-record-1522"},
{"footprint":"POLYGON ICRS -149.28025099997831 54.364514000206682  -149.28089300025363 54.374622999922444  -149.29824099980777 54.374249000037423  -149.29760199996028 54.364138999833536  -149.28025099997831 54.364514000206682","_id_":"ext-record-1523"},
{"footprint":"POLYGON ICRS -149.28025099997831 54.364514000206682  -149.28089300025363 54.374622999922444  -149.29824099980777 54.374249000037423  -149.29760199996028 54.364138999833543  -149.28025099997831 54.364514000206682","_id_":"ext-record-1524"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1525"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1526"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1527"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1528"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1529"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.1584020002982 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909842  -149.15777799974853 54.289982000015407","_id_":"ext-record-1530"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1531"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1532"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1533"},
{"footprint":"POLYGON ICRS -149.15777799974853 54.289982000015407  -149.15840200029822 54.300093000115666  -149.17571700002509 54.299723999959092  -149.1750899999283 54.289615999909834  -149.15777799974853 54.289982000015407","_id_":"ext-record-1534"},
{"footprint":"POLYGON ICRS -149.02736760192514 54.390913597828494  -149.04900220157447 54.415551300022095  -149.13832780032473 54.394573298267161  -149.11665059935913 54.369951599075463  -149.11665051519614 54.369951618674982  210.8833495 54.3699516  -149.11664810905353 54.369952162755624  -149.11664769989713 54.369951700761909  -149.11664540092752 54.36995224113069  210.8833549 54.3699519  -149.11664501765691 54.369951919832637  210.883355 54.3699519  -149.02783769729086 54.390801250466296  -149.02737009862807 54.390910800352572  -149.02737143501193 54.390912323251293  -149.02736940217204 54.390912799275988  -149.02736947531019 54.390912883131591  -149.02736940129159 54.390912899215017  -149.02736960369285 54.390913129251338  -149.02736760192514 54.390913597828494POLYGON ICRS  -149.00456969878576 54.365116999676651  -149.02658809863027 54.390706800112092  -149.11642790427933 54.368795999214996  -149.09436530075519 54.34322249954829  -149.09436521692911 54.343222519823783  210.9056348 54.3432225  -149.00456969878576 54.365116999676651","_id_":"ext-record-1535"},
{"footprint":"POLYGON ICRS -149.02736760000022 54.390913599633549  -149.04900220157447 54.415551300022095  -149.13832779923118 54.394573298998118  -149.11665059935913 54.369951599075463  -149.11665051519614 54.369951618674982  210.8833495 54.3699516  -149.02736760000022 54.390913599633549POLYGON ICRS  -149.00456969979658 54.365117000020028  -149.02658809863027 54.390706800112092  -149.11642790104992 54.368795999630791  -149.09436530075519 54.34322249954829  -149.09436521692911 54.343222519823783  210.9056348 54.3432225  -149.00456969979658 54.365117000020028","_id_":"ext-record-1536"},
{"footprint":"POLYGON ICRS -149.02736940115739 54.390912798770515  -149.02736947462421 54.390912882494227  -149.02736939901897 54.390912900208249  -149.04900310020759 54.41555009982158  -149.13832389885511 54.394572999503048  -149.13832382603496 54.394572916845107  -149.13832389946768 54.39457289956335  -149.11664769979674 54.369951699181719  -149.02736940115739 54.390912798770515POLYGON ICRS  -149.00457110003128 54.365116699885974  210.973411 54.3907063  -149.02658908408736 54.3907062801238  -149.0265890994913 54.390706299510605  -149.11199760312169 54.369878529911411  -149.11642640090309 54.368796300773965  -149.11642640005209 54.368796299788222  -149.11642640215484 54.368796299274344  -149.09436429969347 54.343222999314584  -149.00457110003128 54.365116699885974","_id_":"ext-record-1537"},
{"footprint":"POLYGON ICRS -149.02737010099207 54.390910800116416  210.9509963 54.415547  -149.13832129964055 54.394572100208812  210.8833549 54.3699519  -149.11664501809969 54.369951919452426  -149.11664499953881 54.369951899056453  -149.02737010099207 54.390910800116416POLYGON ICRS  -149.00457299989583 54.365116599498656  210.9734097 54.3907048  -149.02659038439634 54.390704779585555  -149.02659040113164 54.390704799736859  -149.07280724849687 54.379444847579208  -149.11642450109667 54.368796399640836  210.905637 54.3432245  -149.09436291646378 54.343224519552528  -149.09436289947888 54.343224500567175  -149.05183899543783 54.353603681055922  -149.00457299989583 54.365116599498656","_id_":"ext-record-1538"},
{"footprint":"POLYGON ICRS -149.27790429886875 54.325206200024184  210.705463 54.351121  -149.38725620026028 54.33621880036393  210.6294289 54.3103168  -149.37057101245139 54.310316814002185  -149.37057100147376 54.310316800066879  -149.32400135514015 54.317809697767025  -149.27790429886875 54.325206200024184POLYGON ICRS  -149.26033910044683 54.298062599563281  210.7228282 54.324949  -149.37057639910557 54.309153999211546  210.6463108 54.2822806  -149.35368911194823 54.28228061504781  -149.35368910074777 54.282280600398416  -149.30860225850333 54.289913620398671  -149.26033910044683 54.298062599563281","_id_":"ext-record-1539"},
{"footprint":"POLYGON ICRS -149.27790429948848 54.325206200032255  210.705463 54.351121  -149.38725619949284 54.336218801441383  210.6294289 54.3103168  -149.37057101245139 54.310316814002185  -149.37057100147376 54.310316800066879  -149.37056870316835 54.310317170586693  -149.37056839970919 54.310316699787549  -149.37056571312198 54.3103171333311  210.6294345 54.3103168  -149.37056546773229 54.31031680485205  210.6294346 54.3103167  -149.28959894009162 54.323329698496  -149.27790739831178 54.325203598657758  -149.27790850780775 54.325205331062314  -149.27790619936565 54.325205699721245  -149.27790631101729 54.325205875566304  -149.27790429948848 54.325206200032255POLYGON ICRS  -149.26033909801379 54.298062599715188  210.7228282 54.324949  -149.37057639863161 54.309153998457447  210.6463108 54.2822806  -149.35368911194823 54.28228061504781  -149.35368910074777 54.282280600398416  -149.30860225850333 54.289913620398671  -149.26033909801379 54.298062599715188","_id_":"ext-record-1540"},
{"footprint":"POLYGON ICRS -149.27790619948368 54.3252056992702  210.705462 54.35112  -149.38725259938258 54.336218299222693  -149.37056839975983 54.310316699441877  -149.27790619948368 54.3252056992702POLYGON ICRS  -149.26034039874148 54.298062400001555  210.7228273 54.3249486  -149.27717278993549 54.324948584685373  -149.27717279918198 54.324948599899621  -149.37057500012997 54.309154199899126  -149.35368819994886 54.282280999333345  -149.34687938849598 54.283434946084419  -149.26034039874148 54.298062400001555","_id_":"ext-record-1541"},
{"footprint":"POLYGON ICRS -149.27790739781472 54.3252036003033  -149.29453929980369 54.351116700246862  -149.38724969867332 54.336217099720166  -149.37056549935295 54.310316799420839  -149.37056546720697 54.310316804577795  210.6294346 54.3103167  -149.28889690702306 54.323442257198607  -149.27790739781472 54.3252036003033POLYGON ICRS  -149.26034259856831 54.298062400127321  -149.27717449913752 54.324947100564195  -149.27717459101186 54.324947085042595  210.7228254 54.3249471  -149.32369408355038 54.317091495926022  -149.370572799124 54.309154100610691  -149.35368640048961 54.282282399442245  -149.26034259856831 54.298062400127321","_id_":"ext-record-1542"},
{"footprint":"POLYGON ICRS -148.997659800457 54.34146220061281  -149.01925029843449 54.366105300200644  -149.10848360094042 54.345149598156908  -149.08685070098062 54.320522499445126  -149.0868483888899 54.320523042463918  210.913152 54.3205226  -149.0868479165043 54.320522619342519  210.9131521 54.3205226  -149.08684568308908 54.320523121306195  210.9131546 54.3205228  -148.99766209891774 54.341459599960693  -148.99766337774005 54.3414610605466  -148.99766150068058 54.341461500546536  -148.99766155545854 54.341461564377752  -148.99766140125195 54.341461600288888  -148.99766156420873 54.341461785824023  -148.997659800457 54.34146220061281POLYGON ICRS  -148.97490830055327 54.315659999453892  211.0031186 54.3412553  -148.99688148296636 54.341255279463411  -148.99688150095454 54.341255300581821  -149.08494522715287 54.319778429330761  -149.0866280000788 54.319367200614295  -149.0866279974388 54.319367196156264  -149.08662920027712 54.319366901434648  -149.06461189989926 54.293787899737474  -148.97844041135349 54.3148004764652  -148.97490950014037 54.31565969915313  -148.97490950665045 54.315659706575751  -148.97490830055327 54.315659999453892","_id_":"ext-record-1543"},
{"footprint":"POLYGON ICRS -148.99765979874647 54.341462199767463  -149.01925029843449 54.366105300200644  -149.10848359979946 54.345149599772171  -149.08685070098062 54.320522499445126  -148.99765979874647 54.341462199767463POLYGON ICRS  -148.97490829954958 54.315660000595649  211.0031186 54.3412553  -148.99688148296636 54.341255279463411  -148.99688150095454 54.341255300581821  -149.08662920021823 54.319366899670051  -149.06461189989926 54.293787899737474  -148.97490829954958 54.315660000595649","_id_":"ext-record-1544"},
{"footprint":"POLYGON ICRS -148.99766139923693 54.341461599838745  210.9807489 54.3661043  -149.10848009896549 54.345149399864951  210.913152 54.3205226  -149.08684791916798 54.320522618825436  -149.0868479018113 54.320522599641336  -148.99766149866571 54.341461500096443  -148.99766155420394 54.3414615635316  -148.99766139923693 54.341461599838745POLYGON ICRS  -148.97490950006488 54.315659699197845  -148.99688229981393 54.341254799092567  -149.08662799969682 54.319367199218675  210.9353889 54.2937883  -148.97490950006488 54.315659699197845","_id_":"ext-record-1545"},
{"footprint":"POLYGON ICRS -148.99766209942248 54.3414595997165  -149.01925159964338 54.366101099576682  -149.01925168339673 54.366101079792287  210.9807483 54.3661011  -149.10847740006389 54.345148500849035  -149.10799077782693 54.344594902829684  -149.08684539867639 54.320522800148694  -148.99766209942248 54.3414595997165POLYGON ICRS  -148.97491139944478 54.315659599723212  -148.99688359996986 54.341253399500886  -148.9968835999592 54.341253399330917  211.0031164 54.3412534  -149.0866260003701 54.319367300663728  210.9353902 54.2937898  -148.97491139944478 54.315659599723212","_id_":"ext-record-1546"},
{"footprint":"POLYGON ICRS -149.294405000072 54.327548000213575  -149.29176599986414 54.337545999975383  -149.30890799988262 54.339083999874227  -149.31155200018108 54.329083999936806  -149.294405000072 54.327548000213575","_id_":"ext-record-1547"},
{"footprint":"POLYGON ICRS -149.294405000072 54.327548000213575  -149.29176599986414 54.337545999975383  -149.30890799988262 54.339083999874227  -149.31155200018108 54.329083999936806  -149.294405000072 54.327548000213575","_id_":"ext-record-1548"},
{"footprint":"POLYGON ICRS -149.294405000072 54.327548000213575  -149.29176599986414 54.337545999975383  -149.30890799988262 54.339083999874227  -149.31155200018108 54.329083999936806  -149.294405000072 54.327548000213575","_id_":"ext-record-1549"},
{"footprint":"POLYGON ICRS -149.17865799854221 54.259848000464991  210.833087 54.280879  -149.16780188753489 54.281048451441947  -149.1676200002729 54.281373999917761  -149.20366499944265 54.288237999402376  -149.20370975094303 54.288157919464815  -149.23654499976755 54.294203999899707  -149.24660270054108 54.275577710498546  -149.24691999960191 54.275635000117241  -149.258111999836 54.254511000365106  -149.2579795491379 54.254487079535252  -149.25722746447423 54.25435127684618  210.742595 54.254016  210.74272665 54.25399223  210.77494749 54.2481692  210.7764149 54.24790376  -149.2212039999558 54.247472999577958  -149.21550319174955 54.2582293896004  -149.20121200094724 54.255561000268884  -149.1969612491817 54.263336520652388  -149.17865799854221 54.259848000464991","_id_":"ext-record-1550"},
{"footprint":"POLYGON ICRS -149.17865799854221 54.259848000464991  210.833087 54.280879  -149.16780188753489 54.281048451441947  -149.1676200002729 54.281373999917761  -149.20366499944265 54.288237999402376  -149.20370975094303 54.288157919464815  -149.23654499976755 54.294203999899707  -149.24660270054108 54.275577710498546  -149.24691999960191 54.275635000117241  -149.258111999836 54.254511000365106  -149.2579795491379 54.254487079535252  -149.25722746447423 54.25435127684618  210.742595 54.254016  210.74272665 54.25399223  210.77494749 54.2481692  210.7764149 54.24790376  -149.2212039999558 54.247472999577958  -149.21550319174955 54.2582293896004  -149.20121200094724 54.255561000268884  -149.1969612491817 54.263336520652388  -149.17865799854221 54.259848000464991","_id_":"ext-record-1551"},
{"footprint":"POLYGON ICRS -149.1115130010111 54.438731999401817  -149.11151300104714 54.438731999412404  -149.11151299975677 54.438732000901311  -149.14548499947696 54.44869200014638  -149.15419916010336 54.43863850978066  -149.16758299845915 54.442654000450688  -149.1740074899987 54.435400470776756  -149.19113499937626 54.440611000633332  -149.20875999935478 54.421010000728678  -149.1750340008675 54.410748000126524  -149.17496561138194 54.410824079736379  -149.14418599944261 54.401678999232566  -149.12891715958509 54.419072779426429  -149.12861799998353 54.418984999290629  -149.12684466893651 54.421033229635142  -149.1115130010111 54.438731999401817","_id_":"ext-record-1552"},
{"footprint":"POLYGON ICRS -149.1115130010111 54.438731999401817  -149.11151300104714 54.438731999412404  -149.11151299975677 54.438732000901311  -149.14548499947696 54.44869200014638  -149.15419916010336 54.43863850978066  -149.16758299845915 54.442654000450688  -149.1740074899987 54.435400470776756  -149.19113499937626 54.440611000633332  -149.20875999935478 54.421010000728678  -149.1750340008675 54.410748000126524  -149.17496561138194 54.410824079736379  -149.14418599944261 54.401678999232566  -149.12891715958509 54.419072779426429  -149.12861799998353 54.418984999290629  -149.12684466893651 54.421033229635142  -149.1115130010111 54.438731999401817","_id_":"ext-record-1553"},
{"footprint":"POLYGON ICRS -149.23703100024861 54.415018999893462  -149.25250400007806 54.419629999956136  -149.26041599998908 54.410625999801582  -149.24494500011326 54.406017999832834  -149.23703100024861 54.415018999893462","_id_":"ext-record-1554"},
{"footprint":"POLYGON ICRS -149.23703100024861 54.415018999893462  -149.25250400007806 54.419629999956136  -149.26041599998908 54.410625999801582  -149.24494500011326 54.406017999832834  -149.23703100024861 54.415018999893462","_id_":"ext-record-1555"},
{"footprint":"POLYGON ICRS -149.00466200061223 54.303207999788881  -149.01432906543951 54.312698690751795  -149.02405399000253 54.322241007107678  -149.02414246116518 54.322210356491269  210.97582109 54.32219773  210.957923 54.339379  -149.07068784503562 54.3292379143343  -149.07086198352729 54.329402996174672  -149.07086199221732 54.329402993512062  210.929138 54.329403  -149.1031469988333 54.317817999803218  -149.10309663827923 54.31777025151257  -149.10307068204014 54.3177456346375  -149.08538257464863 54.300967235310331  -149.08458124574165 54.300206729906613  -149.08394677345839 54.299604590018646  210.916725 54.298967  -149.08327497978624 54.298967007675827  -149.08327497701538 54.29896700544559  -149.06683573495894 54.304865615579615  -149.05908602627366 54.297358995399151  -149.05908600346913 54.297359003515723  210.940914 54.297359  210.95289665 54.3015727  210.962741 54.291906  -149.03725898193005 54.291906007122584  -149.03725897999328 54.291906005357689  -149.02607979273915 54.29578373371023  -149.00466200061223 54.303207999788881","_id_":"ext-record-1556"},
{"footprint":"POLYGON ICRS -149.0046619973055 54.303208006636694  -149.01255320881606 54.310955607598352  -149.0149186931751 54.313277390249191  -149.02405399669337 54.322241022424237  -149.02411324083144 54.322220497015536  -149.02417889088022 54.32219772648034  -149.04207697961857 54.339379002715752  -149.05096183289044 54.336231026569571  -149.07068785654297 54.329237917263043  -149.07086200433508 54.329403000808874  -149.10314700309777 54.317817993605772  -149.10308907846934 54.317763063822419  -149.10307070086785 54.317745630831467  -149.08538256363229 54.300967243215474  -149.08458123032014 54.300206722573122  -149.08397167886127 54.299628236217821  -149.08327498291928 54.298967000054425  -149.06683572277916 54.3048656125472  -149.05908603713451 54.297359003676796  -149.05908599602893 54.297358991997527  -149.04710335055586 54.301572714054949  -149.03725897345072 54.29190600048738  -149.0274237304275 54.295317662319114  -149.02221110028239 54.297125245509292  -149.0046619973055 54.303208006636694","_id_":"ext-record-1557"},
{"footprint":"POLYGON ICRS -149.16310460009419 54.411752900087748  -149.16093779996666 54.419742299933745  -149.17306299996335 54.420134500098129  -149.17522749997585 54.412144899880396  -149.16310460009419 54.411752900087748","_id_":"ext-record-1558"},
{"footprint":"POLYGON ICRS -149.03487300093224 54.296535999529105  -149.02347999915182 54.317644999736665  -149.0418927490125 54.321027950524204  -149.03724999985576 54.329358999518853  210.94943975 54.33187955  210.955508 54.342551  -149.08049400117861 54.349511000389313  -149.0924379997513 54.328495000690815  -149.09229895873582 54.328468139875532  -149.1028249998829 54.309322999704818  -149.0709376510199 54.303360030483354  -149.07103800051857 54.303174000416107  -149.06726024120758 54.302481210173063  -149.03487300093224 54.296535999529105","_id_":"ext-record-1559"},
{"footprint":"POLYGON ICRS -149.03487300093224 54.296535999529105  -149.02347999915182 54.317644999736665  -149.0418927490125 54.321027950524204  -149.03724999985576 54.329358999518853  210.94943975 54.33187955  210.955508 54.342551  -149.08049400117861 54.349511000389313  -149.0924379997513 54.328495000690815  -149.09229895873582 54.328468139875532  -149.1028249998829 54.309322999704818  -149.0709376510199 54.303360030483354  -149.07103800051857 54.303174000416107  -149.06726024120758 54.302481210173063  -149.03487300093224 54.296535999529105","_id_":"ext-record-1560"},
{"footprint":"POLYGON ICRS -149.1640660002044 54.413511999956029  -149.16304600022136 54.423606000078735  -149.18040200038408 54.424202000128766  -149.18141900015704 54.41410600010952  -149.1640660002044 54.413511999956029","_id_":"ext-record-1561"},
{"footprint":"POLYGON ICRS -149.1640660002044 54.413511999956029  -149.16304600022136 54.423606000078735  -149.18040200038408 54.424202000128766  -149.18141900015704 54.41410600010952  -149.1640660002044 54.413511999956029","_id_":"ext-record-1562"},
{"footprint":"POLYGON ICRS -149.08741800004967 54.297820000107471  -149.08711099996776 54.307932000081465  -149.10444599991519 54.308113999811169  -149.10475700006734 54.298003999999963  -149.08741800004967 54.297820000107471","_id_":"ext-record-1563"},
{"footprint":"POLYGON ICRS -149.08741800004967 54.297820000107471  -149.08711099996776 54.307932000081465  -149.10444599991519 54.308113999811169  -149.10475700006734 54.298003999999963  -149.08741800004967 54.297820000107471","_id_":"ext-record-1564"},
{"footprint":"POLYGON ICRS -149.08741800004967 54.297820000107471  -149.08711099996776 54.307932000081465  -149.10444599991519 54.308113999811169  -149.10475700006737 54.298003999999963  -149.08741800004967 54.297820000107471","_id_":"ext-record-1565"},
{"footprint":"POLYGON ICRS -149.25866770009577 54.409028400063534  -149.25650099994758 54.417017899963625  -149.26862550011086 54.417410200006124  -149.27078989984585 54.409420399966713  -149.25866770009577 54.409028400063534","_id_":"ext-record-1566"},
{"footprint":"POLYGON ICRS -149.06620499953883 54.3128139998525  -149.07516100021741 54.321476999975623  -149.09000900038279 54.316256000016985  -149.08105400017897 54.307591000207367  -149.06620499953883 54.3128139998525POLYGON ICRS  -149.04423800002482 54.30257999974706  -149.05318700022093 54.31124400051668  -149.06803400036131 54.306025999745522  -149.0590859994189 54.297358999955357  -149.04423800002482 54.30257999974706","_id_":"ext-record-1567"},
{"footprint":"POLYGON ICRS -149.06620499955955 54.3128139997763  -149.07516100021741 54.321476999975623  -149.09000900040348 54.316255999940786  -149.08105400017897 54.307591000207367  -149.06620499955955 54.3128139997763POLYGON ICRS  -149.04423800004554 54.302579999670883  -149.05318700022093 54.31124400051668  -149.06803400038203 54.30602599966933  -149.05908599943962 54.297358999879165  -149.04423800004554 54.302579999670883","_id_":"ext-record-1568"},
{"footprint":"POLYGON ICRS -149.32977490052852 54.318561100468429  210.6782918 54.3750923  -149.37084499923603 54.375490200362371  -149.37085014374111 54.375453892057088  -149.37156080081405 54.375459501781691  -149.37156583206857 54.375423989311  -149.37227729964965 54.375429599748287  -149.37231190045415 54.375185332135231  -149.41902169954972 54.375336999697268  -149.41902712747986 54.375303832678092  -149.41973679967555 54.375305999185727  -149.41974209682769 54.37527363227742  -149.42045409883062 54.375275800412773  -149.42957369958302 54.319462999700896  -149.38213499870071 54.319312700965106  -149.38212951311604 54.319346090992326  -149.38141929984297 54.319343701191862  -149.3814139597356 54.319376192677467  -149.38070459924762 54.319373800687622  -149.37716415605556 54.340901749171564  -149.38027459938914 54.318894699620444  -149.33120500012302 54.318500600154294  -149.33119979303825 54.318537142927248  -149.330490301582 54.31853129990207  -149.33048522141115 54.318566954705346  -149.32977490052852 54.318561100468429","_id_":"ext-record-1569"},
{"footprint":"POLYGON ICRS -149.32977490126072 54.318561101894609  210.6782918 54.3750923  -149.37084500154117 54.3754902021  -149.37085014329784 54.37545389200799  -149.3715608000623 54.375459499952186  -149.3715658366776 54.37542398954097  -149.37227730115237 54.375429599509651  -149.37231190025634 54.375185332671563  -149.41902169843783 54.375336999693168  -149.4190271269961 54.375303831928953  -149.41973680067639 54.375305997074214  -149.41974210082546 54.3752736314937  -149.42045409704116 54.375275802156494  -149.42957369958302 54.319462999700896  -149.38213499367927 54.319312701189538  -149.38213473175179 54.319314304096508  -149.3821336971871 54.31931430072855  -149.38213340089595 54.3193161030812  -149.38213249932477 54.319316100007953  -149.38212757161287 54.319346084181532  -149.38141929512034 54.319343700566094  -149.38141903440373 54.3193453019687  -149.38141809447978 54.319345299235081  -149.3814179469872 54.319346201523253  -149.38141779863298 54.319346200620089  -149.381412870495 54.319376188492818  -149.38070460507021 54.319373802079475  -149.38070432399351 54.319375505096041  -149.38070330221149 54.319375501600959  -149.38070302258737 54.319377202838474  -149.38070220008225 54.319377200064061  -149.37718020707678 54.340788229422543  -149.38027459938914 54.318894699620444  -149.33120500019581 54.318500599583075  -149.33119979298266 54.318537141754895  -149.33049030320819 54.3185312969552  -149.33048522392232 54.3185669534503  -149.32977490126072 54.318561101894609","_id_":"ext-record-1570"},
{"footprint":"POLYGON ICRS -149.32977540037075 54.318561999686672  210.6782914 54.3750913  -149.37084460064563 54.375489300303329  -149.37084974553721 54.375452990544758  -149.37156039704871 54.375458600279984  -149.3715654283036 54.375423087809317  -149.37227680119611 54.375428701328339  -149.37231148803033 54.375183838451917  -149.41901980015754 54.375335900447141  -149.41902522860016 54.375302728911834  -149.41973520119996 54.375304900885396  -149.41974049814945 54.375272526520689  -149.42045240164427 54.375274698563786  -149.42957109950279 54.319464999967558  -149.38213370112379 54.319314300099251  -149.38212821182057 54.319347694634743  -149.38141809811765 54.319345299457318  -149.3814127406404 54.319377898074819  -149.38070330168935 54.319375499745085  -149.37716709022308 54.340878593096669  -149.38027429912094 54.318895699611232  -149.33120550257155 54.318501500092076  -149.33120030840863 54.318537946543643  -149.33049070045459 54.318532098550079  -149.33048560544535 54.318567855971409  -149.32977540037075 54.318561999686672","_id_":"ext-record-1571"},
{"footprint":"POLYGON ICRS -149.32977720001455 54.318563700786  210.6782904 54.3750899  -149.37084270117705 54.3754876997311  -149.37084773393519 54.375452187160761  -149.37155910109669 54.375457799852882  -149.37156426017248 54.375421397967507  -149.37227500020967 54.375427000705734  -149.3723096651417 54.375182332757454  -149.41901459945717 54.375333799352092  -149.41901988206052 54.375301527282105  -149.41973190094231 54.375303699568185  -149.41973734655505 54.37527044001029  -149.42044709722788 54.375272599981542  -149.42956760073488 54.319466200364019  -149.38213250192467 54.319316100360332  -149.38212716129846 54.319348587379174  -149.38141779965679 54.319346199620391  -149.38141231006227 54.319379593114235  -149.38070220080681 54.319377199917277  -149.37716819101169 54.340861240914258  -149.3802731991934 54.318897000072582  -149.33120730098085 54.318503099001262  -149.3312022069168 54.318538848000081  -149.33049199924852 54.318532999925772  -149.33048679100531 54.318569546539848  -149.32977720001455 54.318563700786","_id_":"ext-record-1572"},
{"footprint":"POLYGON ICRS -149.38141670039386 54.319346800561021  -149.37223220075148 54.375148599383849  -149.41973089960388 54.375302800113943  -149.4288511010142 54.319497400234177  -149.38141670039386 54.319346800561021POLYGON ICRS  -149.33049190069298 54.318534100830078  -149.32242660074516 54.375059099523973  -149.3715592011001 54.375456600590319  -149.37955699911734 54.318928399677127  -149.33049190069298 54.318534100830078","_id_":"ext-record-1573"},
{"footprint":"POLYGON ICRS -149.19616299885814 54.248350999554262  -149.22205199999416 54.264518999791925  -149.22215769979681 54.264461279942807  -149.24591299901127 54.279008999878258  -149.24591299891537 54.279008999754524  -149.24591300044631 54.279009000691595  210.72989932 54.26552852  -149.27033099980738 54.265667999384661  -149.29757500071102 54.2503180010983  -149.29747337038057 54.250256450930955  -149.27407294891614 54.236078360115826  -149.27301158173577 54.2354349497656  -149.271283001081 54.234387000229489  -149.25741034063094 54.242202750178322  -149.24710100149562 54.235837999962179  -149.2471009997372 54.235837999109847  -149.23695741072635 54.241451640704852  -149.22381299965389 54.233238999894759  -149.19616299885814 54.248350999554262","_id_":"ext-record-1574"},
{"footprint":"POLYGON ICRS -149.19616299845407 54.248351000113409  -149.22205200078116 54.264518999896595  -149.22215769956605 54.264461279413908  210.754087 54.279009  -149.24591299966039 54.279008999027425  -149.24591300129842 54.27900900014923  -149.27010068078025 54.265528520199013  -149.27033099914212 54.265667999925363  -149.29757499959439 54.250317999304578  -149.29747337075423 54.25025645047161  -149.27407294939675 54.236078359254485  -149.2730115817358 54.2354349497656  -149.27128300077811 54.234387000190758  -149.25741034035386 54.242202750439787  -149.24710100149562 54.235837999962186  -149.24710100096738 54.235838000377278  210.752899 54.235838  -149.2369574107712 54.241451639881269  -149.22381300103208 54.233239000630043  -149.22381300048863 54.23323900104856  210.776187 54.233239  -149.19616299845407 54.248351000113409","_id_":"ext-record-1575"},
{"footprint":"POLYGON ICRS -149.19616299845404 54.248351000113395  -149.22205200078116 54.26451899989658  -149.22215769956605 54.264461279413894  210.754087 54.279009  -149.24591299966039 54.279008999027411  -149.24591300129842 54.27900900014923  -149.27010068078025 54.265528520199005  -149.27033099914212 54.265667999925356  -149.29757499959436 54.250317999304563  -149.29747337075423 54.250256450471596  -149.27407294939675 54.236078359254478  -149.27301158173577 54.2354349497656  -149.27128300077811 54.234387000190743  -149.25741034035386 54.24220275043978  -149.24710100149562 54.235837999962179  -149.24710100096738 54.235838000377271  210.752899 54.235838  -149.2369574107712 54.241451639881262  -149.22381300103208 54.233239000630043  -149.22381300048863 54.233239001048545  210.776187 54.233239  -149.19616299845404 54.248351000113395","_id_":"ext-record-1576"},
{"footprint":"POLYGON ICRS -149.04070109925172 54.267535900155359  210.9428389 54.2934832  -149.14983609993371 54.278762999967846  210.8666763 54.2528284  -149.13332366867095 54.252828405173823  -149.13332360041116 54.252828299340813  -149.05159416987675 54.26581042154735  -149.04070109925172 54.267535900155359POLYGON ICRS  -149.02331679963413 54.240358000680779  210.9600289 54.2672772  -149.03997119239258 54.2672771850589  -149.03997120076494 54.267277200068939  -149.07022536393086 54.262227511987305  -149.13333569889551 54.251665500265254  210.8833733 54.2247591  -149.02331679963413 54.240358000680779","_id_":"ext-record-1577"},
{"footprint":"POLYGON ICRS -149.04070110033888 54.267535899340395  210.9428389 54.2934832  -149.14983610132614 54.278762999261389  210.8666763 54.2528284  -149.13332366867095 54.252828405173823  -149.13332360041116 54.252828299340813  -149.13332114733686 54.252828689783087  -149.13332090075224 54.252828300706  -149.1333183587939 54.2528287051046  210.8666819 54.2528283  -149.13331800905146 54.252828313707106  210.866682 54.2528283  -149.04070430011458 54.267533199948119  -149.04070434857434 54.267533276389905  -149.04070420104031 54.26753329975012  -149.0407052435539 54.267534944462383  -149.04070300115296 54.267535299158205  -149.040703173402 54.267535572540439  -149.04070110033888 54.267535899340395POLYGON ICRS  -149.023316799151 54.240358000582454  210.9600289 54.2672772  -149.03997119239258 54.2672771850589  -149.03997120076494 54.267277200068939  -149.07022536393086 54.262227511987305  -149.113013769064 54.255070723724486  -149.13333429913192 54.251665799708036  -149.13333426219307 54.251665741047312  -149.13333569596418 54.251665500623957  210.8833733 54.2247591  -149.04821496782174 54.236203899926792  -149.02331810062702 54.24035769962066  -149.02331814917412 54.2403577746509  -149.023316799151 54.240358000582454","_id_":"ext-record-1578"},
{"footprint":"POLYGON ICRS -149.04070299983229 54.267535299144  -149.05716210115233 54.293482200204807  -149.14983249858952 54.278762500302648  -149.13332089976407 54.252828299951339  -149.04070299983229 54.267535299144POLYGON ICRS  -149.02331809959287 54.240357700392721  210.9600279 54.2672768  -149.03997210144482 54.267276799450038  -149.03997210078231 54.26727680065806  -149.13333430055212 54.251665799970169  210.8833743 54.2247595  -149.02331809959287 54.240357700392721","_id_":"ext-record-1579"},
{"footprint":"POLYGON ICRS -149.04070428724708 54.267533187488652  -149.04070437047525 54.267533270503748  -149.0407041872509 54.267533307429524  -149.05716338373753 54.293478808998529  -149.06041603413524 54.292963571186483  -149.14982969990422 54.2787612049479  -149.1333181083634 54.252828316283754  -149.13331800625105 54.252828316991028  -149.13331802618924 54.252828292987559  -149.04070428724708 54.267533187488652POLYGON ICRS  -149.0233202900169 54.240357803156741  -149.03997388109534 54.267275406333255  -149.13333207541456 54.251665701506347  -149.11662399694424 54.224760986752166  -149.0233202900169 54.240357803156741","_id_":"ext-record-1580"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.47236680987421  -149.20178927946364 54.472021149028976  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1581"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670914  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439104 54.479983113554908  -149.16897299978984 54.479984000357575  -149.16910208099492 54.479952699515231  -149.20036578122185 54.4723668098742  -149.20178927946364 54.472021149028976  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526717  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917502 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.4337173890963  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884236  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1582"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.47236680987421  -149.20178927946364 54.472021149028976  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1583"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515245  -149.20036578122185 54.472366809874217  -149.20178927946364 54.472021149028983  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417639  -149.21044130272861 54.457807003977528  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636075  -149.17378395338594 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884236  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1584"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515245  -149.20036578122185 54.472366809874217  -149.20178927946364 54.472021149028983  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1585"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.1541579989792 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439104 54.479983113554908  -149.16897299978982 54.479984000357582  -149.16910208099492 54.479952699515245  -149.20036578122185 54.472366809874217  -149.20178927946364 54.472021149028983  -149.20410000025504 54.47146000050649  -149.19664050458528 54.461069783143955  -149.21044199975248 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.4577690009468  -149.20531238439224 54.450212875526709  -149.22310800172082 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917502 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1586"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357575  -149.16910208099492 54.479952699515231  -149.20036578122185 54.4723668098742  -149.20178927946364 54.472021149028976  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1587"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.1541579989792 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439104 54.479983113554908  -149.16897299978982 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.47236680987421  -149.20178927946364 54.472021149028983  -149.20410000025504 54.47146000050649  -149.19664050458528 54.461069783143955  -149.21044199975248 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.4577690009468  -149.20531238439224 54.450212875526709  -149.22310800172082 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917502 54.446037000114174  -149.20914399855084 54.425496999925322  -149.17378299933935 54.433716000636082  -149.17378395338591 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1588"},
{"footprint":"POLYGON ICRS -149.14169199972443 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.472366809874217  -149.20178927946364 54.472021149028983  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.2106020004976 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925329  -149.17378299933938 54.433716000636082  -149.17378395338594 54.433717389096294  -149.17362199940086 54.433755000142668  -149.17367657075982 54.433834420686274  -149.14185300058111 54.441440999884229  -149.14185333089165 54.441441467195638  -149.14169199972443 54.441479999796591","_id_":"ext-record-1589"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.4596169993932  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.4799831135549  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.47236680987421  -149.20178927946364 54.472021149028976  -149.20410000025507 54.471460000506482  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755614  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925329  -149.17378299933935 54.433716000636075  -149.17378395338594 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195631  -149.1416919997244 54.441479999796591","_id_":"ext-record-1590"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670914  -149.15415799897923 54.459616999393205  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439104 54.479983113554908  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.472366809874217  -149.20178927946364 54.472021149028983  -149.20410000025507 54.47146000050649  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526717  -149.22310800172085 54.446075999755621  -149.22310708049881 54.446074660782493  -149.22326899917502 54.446037000114174  -149.20914399855084 54.425496999925329  -149.17378299933935 54.433716000636082  -149.17378395338591 54.4337173890963  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884236  -149.14185333089165 54.441441467195638  -149.1416919997244 54.441479999796591","_id_":"ext-record-1591"},
{"footprint":"POLYGON ICRS -149.1416919997244 54.441479999796591  -149.15446579065693 54.459542379670907  -149.15415799897923 54.4596169993932  210.831188 54.480022  210.83105839 54.47999057  -149.16897236439107 54.4799831135549  -149.16897299978984 54.479984000357582  -149.16910208099492 54.479952699515238  -149.20036578122185 54.47236680987421  -149.20178927946364 54.472021149028976  -149.20410000025507 54.471460000506482  -149.19664050458528 54.461069783143955  -149.2104419997525 54.457807999417646  -149.21044130272861 54.457807003977535  -149.21060200049757 54.45776900094679  -149.20531238439227 54.450212875526709  -149.22310800172085 54.446075999755614  -149.22310708049881 54.446074660782493  -149.22326899917505 54.446037000114174  -149.20914399855084 54.425496999925329  -149.17378299933935 54.433716000636075  -149.17378395338594 54.433717389096294  -149.17362199940084 54.433755000142668  -149.1736765707598 54.433834420686274  -149.14185300058108 54.441440999884229  -149.14185333089165 54.441441467195631  -149.1416919997244 54.441479999796591","_id_":"ext-record-1592"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.4799840003871  -149.16910208110437 54.479952699544761  -149.2003657813313 54.472366809903704  -149.20178927957309 54.47202114905847  -149.20409999975976 54.471459999741576  -149.19664043066851 54.461069679968872  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968229  -149.20914399866018 54.425496999954809  -149.17378300085309 54.433716000396991  -149.17383786095368 54.433795840174575  -149.14185300092754 54.441441000503062","_id_":"ext-record-1593"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.4799840003871  -149.16910208110437 54.479952699544761  -149.2003657813313 54.472366809903704  -149.20178927957309 54.47202114905847  -149.20409999975976 54.471459999741576  -149.19664043066851 54.461069679968872  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968229  -149.20914399866018 54.425496999954809  -149.17378300085309 54.433716000396991  -149.17383786095368 54.433795840174575  -149.14185300092754 54.441441000503062","_id_":"ext-record-1594"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.4799840003871  -149.16910208110437 54.479952699544761  -149.2003657813313 54.472366809903704  -149.20178927957309 54.47202114905847  -149.20409999975976 54.471459999741576  -149.19664043066851 54.461069679968872  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968229  -149.20914399866018 54.425496999954809  -149.17378300085309 54.433716000396991  -149.17383786095368 54.433795840174575  -149.14185300092754 54.441441000503062","_id_":"ext-record-1595"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903718  -149.20178927957309 54.472021149058477  -149.20409999975976 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954817  -149.17378300085309 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503062","_id_":"ext-record-1596"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903718  -149.20178927957309 54.472021149058477  -149.20409999975976 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954817  -149.17378300085309 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503062","_id_":"ext-record-1597"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903711  -149.20178927957309 54.472021149058477  -149.20409999975973 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954817  -149.17378300085306 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503062","_id_":"ext-record-1598"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903711  -149.20178927957309 54.472021149058477  -149.20409999975973 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954817  -149.17378300085306 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503062","_id_":"ext-record-1599"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903718  -149.20178927957309 54.472021149058477  -149.20409999975976 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954817  -149.17378300085309 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503062","_id_":"ext-record-1600"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503069  -149.15462685978724 54.459504350770786  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.20036578133133 54.472366809903711  -149.20178927957309 54.47202114905847  -149.20409999975976 54.471459999741583  -149.19664043066854 54.46106967996888  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866021 54.425496999954817  -149.17378300085309 54.433716000396991  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503069","_id_":"ext-record-1601"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503069  -149.15462685978724 54.459504350770786  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.20036578133133 54.472366809903711  -149.20178927957309 54.47202114905847  -149.20409999975976 54.471459999741583  -149.19664043066854 54.46106967996888  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361029  -149.223269000044 54.446036999968236  -149.20914399866021 54.425496999954817  -149.17378300085309 54.433716000396991  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503069","_id_":"ext-record-1602"},
{"footprint":"POLYGON ICRS -149.14185300092751 54.441441000503062  -149.15462685978721 54.459504350770779  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110434 54.479952699544768  -149.2003657813313 54.472366809903718  -149.20178927957309 54.472021149058477  -149.20409999975973 54.47145999974159  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861134  -149.20531173102958 54.450211940361037  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954824  -149.17378300085306 54.433716000397  -149.17383786095368 54.433795840174582  -149.14185300092751 54.441441000503062","_id_":"ext-record-1603"},
{"footprint":"POLYGON ICRS -149.14185300092754 54.441441000503069  -149.15462685978721 54.459504350770786  -149.15431899999547 54.459578999733111  -149.1689729998993 54.479984000387105  -149.16910208110437 54.479952699544768  -149.2003657813313 54.472366809903711  -149.20178927957309 54.47202114905847  -149.20409999975973 54.471459999741583  -149.19664043066851 54.46106967996888  -149.21060200033611 54.457769000861127  -149.20531173102958 54.450211940361037  -149.223269000044 54.446036999968236  -149.20914399866018 54.425496999954824  -149.17378300085306 54.433716000396991  -149.17383786095368 54.433795840174582  -149.14185300092754 54.441441000503069","_id_":"ext-record-1604"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.4414799997009  -149.15446578949511 54.45954237942599  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101036  210.831188 54.480022  -149.16881200109574 54.480021999839714  -149.16881200091154 54.480022000436144  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401856  -149.16910208115911 54.479952699559512  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073214  -149.20410000079718 54.47145999927757  -149.19664050627253 54.461069782463937  -149.21044199940823 54.45780799942748  -149.21044200085049 54.457807998807723  -149.21044130370433 54.45780700274392  -149.2106019999928 54.457769000733649  -149.20531238546374 54.45021287626686  -149.2231079983579 54.446076000412013  -149.22310707850249 54.446074660851224  -149.22326899823267 54.446037001273226  -149.20914399871486 54.425496999969553  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125455  -149.17362200012283 54.433755000011836  -149.17367656977956 54.433834420388251  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.4414799997009","_id_":"ext-record-1605"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.441479999700896  -149.15446578949513 54.45954237942599  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101036  210.831188 54.480022  -149.16881200109574 54.480021999839707  -149.16881200091157 54.480022000436144  -149.16894160954027 54.479990570579979  -149.16897236447315 54.479983113577042  -149.168972999954 54.479984000401856  -149.16910208115911 54.479952699559512  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073207  -149.20410000079718 54.47145999927757  -149.19664050627253 54.461069782463937  -149.21044199940826 54.457807999427473  -149.21044200085049 54.457807998807716  -149.21044130370433 54.45780700274392  -149.21060199999283 54.457769000733649  -149.20531238546377 54.45021287626686  -149.22310799835793 54.446076000412013  -149.22310707850249 54.446074660851224  -149.2232689982327 54.446037001273226  -149.20914399871486 54.425496999969553  -149.17378299990094 54.433716000280455  -149.17378395341709 54.433717389125448  -149.17362200012286 54.433755000011836  -149.17367656977959 54.433834420388251  -149.1418530000482 54.441440999563916  -149.1418533296609 54.441441467264532  -149.14169199935253 54.441479999700896","_id_":"ext-record-1606"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.441479999700896  -149.15446578949511 54.45954237942599  -149.1541579981261 54.45961699957703  -149.16148317312295 54.469819722101036  210.831188 54.480022  -149.16881200109572 54.480021999839707  -149.16881200091154 54.480022000436144  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401856  -149.16910208115911 54.479952699559512  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073214  -149.20410000079718 54.47145999927757  -149.19664050627253 54.461069782463937  -149.21044199940823 54.457807999427473  -149.21044200085049 54.457807998807723  -149.21044130370433 54.45780700274392  -149.2106019999928 54.457769000733649  -149.20531238546374 54.45021287626686  -149.2231079983579 54.446076000412006  -149.22310707850249 54.446074660851224  -149.22326899823267 54.446037001273226  -149.20914399871486 54.425496999969553  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125448  -149.17362200012283 54.433755000011836  -149.17367656977956 54.433834420388244  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.441479999700896","_id_":"ext-record-1607"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.44147999970091  -149.15446578949513 54.459542379426004  -149.1541579981261 54.459616999577044  -149.16148317312297 54.46981972210105  210.831188 54.480022  -149.16881200109574 54.480021999839721  -149.16881200091154 54.480022000436144  -149.16894160954027 54.479990570579993  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401856  -149.16910208115911 54.479952699559512  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463952  -149.21044199940826 54.457807999427487  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733663  -149.20531238546374 54.450212876266875  -149.22310799835793 54.446076000412027  -149.22310707850249 54.446074660851231  -149.2232689982327 54.44603700127324  -149.20914399871486 54.425496999969553  -149.17378299990094 54.433716000280469  -149.17378395341706 54.433717389125462  -149.17362200012286 54.43375500001185  -149.17367656977956 54.433834420388258  -149.14185300004817 54.44144099956393  -149.1418533296609 54.441441467264546  -149.14169199935253 54.44147999970091","_id_":"ext-record-1608"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.4414799997009  -149.15446578949511 54.459542379426  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101043  210.831188 54.480022  -149.16881200109574 54.480021999839714  -149.16881200091154 54.480022000436144  -149.16894160954027 54.479990570579993  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401856  -149.16910208115911 54.479952699559512  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463944  -149.21044199940826 54.45780799942748  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733656  -149.20531238546374 54.450212876266868  -149.22310799835793 54.44607600041202  -149.22310707850249 54.446074660851231  -149.2232689982327 54.446037001273233  -149.20914399871486 54.425496999969553  -149.17378299990094 54.433716000280462  -149.17378395341706 54.433717389125455  -149.17362200012283 54.433755000011843  -149.17367656977956 54.433834420388258  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.4414799997009","_id_":"ext-record-1609"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.44147999970091  -149.15446578949511 54.459542379426004  -149.1541579981261 54.459616999577044  -149.16148317312297 54.469819722101043  210.831188 54.480022  -149.16881200109574 54.480021999839721  -149.16881200091154 54.480022000436151  -149.16894160954027 54.479990570579993  -149.16897236447315 54.479983113577049  -149.16897299995404 54.479984000401856  -149.16910208115911 54.479952699559519  -149.20036578138604 54.472366809918448  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463952  -149.21044199940823 54.45780799942748  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733656  -149.20531238546374 54.450212876266875  -149.2231079983579 54.446076000412027  -149.22310707850249 54.446074660851238  -149.22326899823267 54.44603700127324  -149.20914399871486 54.42549699996956  -149.17378299990094 54.433716000280469  -149.17378395341706 54.433717389125462  -149.17362200012283 54.43375500001185  -149.17367656977956 54.433834420388258  -149.14185300004817 54.44144099956393  -149.1418533296609 54.441441467264539  -149.14169199935253 54.44147999970091","_id_":"ext-record-1610"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.4414799997009  -149.15446578949513 54.45954237942599  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101043  210.831188 54.480022  -149.16881200109574 54.480021999839714  -149.16881200091157 54.480022000436151  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.16897299995404 54.479984000401863  -149.16910208115911 54.479952699559519  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463944  -149.21044199940826 54.45780799942748  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733649  -149.20531238546377 54.450212876266868  -149.22310799835793 54.446076000412013  -149.22310707850249 54.446074660851231  -149.2232689982327 54.446037001273233  -149.20914399871486 54.42549699996956  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125455  -149.17362200012286 54.433755000011836  -149.17367656977959 54.433834420388251  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.4414799997009","_id_":"ext-record-1611"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.441479999700896  -149.15446578949511 54.45954237942599  -149.1541579981261 54.45961699957703  -149.16148317312297 54.469819722101029  210.831188 54.480022  -149.16881200109574 54.480021999839707  -149.16881200091154 54.480022000436151  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401863  -149.16910208115911 54.479952699559526  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.47145999927757  -149.19664050627253 54.461069782463937  -149.21044199940826 54.457807999427473  -149.21044200085049 54.457807998807723  -149.21044130370433 54.45780700274392  -149.2106019999928 54.457769000733649  -149.20531238546374 54.45021287626686  -149.22310799835793 54.446076000412006  -149.22310707850249 54.446074660851224  -149.2232689982327 54.446037001273226  -149.20914399871486 54.42549699996956  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125448  -149.17362200012283 54.433755000011828  -149.17367656977956 54.433834420388244  -149.14185300004817 54.441440999563916  -149.1418533296609 54.441441467264532  -149.14169199935253 54.441479999700896","_id_":"ext-record-1612"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.4414799997009  -149.15446578949511 54.45954237942599  -149.15415799812607 54.459616999577037  -149.16148317312295 54.469819722101043  210.831188 54.480022  -149.16881200109572 54.480021999839714  -149.16881200091154 54.480022000436158  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.168972999954 54.479984000401863  -149.16910208115911 54.479952699559526  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.1966405062725 54.461069782463944  -149.21044199940823 54.45780799942748  -149.21044200085049 54.457807998807731  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733656  -149.20531238546374 54.450212876266868  -149.2231079983579 54.446076000412013  -149.22310707850249 54.446074660851231  -149.22326899823267 54.446037001273233  -149.20914399871486 54.42549699996956  -149.17378299990091 54.433716000280455  -149.17378395341706 54.433717389125455  -149.17362200012283 54.433755000011836  -149.17367656977956 54.433834420388251  -149.14185300004817 54.44144099956393  -149.1418533296609 54.441441467264532  -149.14169199935253 54.4414799997009","_id_":"ext-record-1613"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.4414799997009  -149.15446578949513 54.45954237942599  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101043  210.831188 54.480022  -149.16881200109574 54.480021999839714  -149.16881200091157 54.480022000436158  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.16897299995404 54.479984000401863  -149.16910208115911 54.479952699559519  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463944  -149.21044199940826 54.45780799942748  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733649  -149.20531238546377 54.450212876266868  -149.22310799835793 54.446076000412013  -149.22310707850249 54.446074660851231  -149.2232689982327 54.446037001273233  -149.20914399871489 54.42549699996956  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125455  -149.17362200012286 54.433755000011836  -149.17367656977959 54.433834420388251  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.4414799997009","_id_":"ext-record-1614"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.44147999970091  -149.15446578949513 54.459542379426004  -149.1541579981261 54.459616999577044  -149.16148317312297 54.46981972210105  210.831188 54.480022  -149.16881200109574 54.480021999839721  -149.16881200091157 54.480022000436144  -149.16894160954027 54.47999057058  -149.16897236447315 54.479983113577049  -149.16897299995404 54.479984000401863  -149.16910208115911 54.479952699559519  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.471459999277577  -149.19664050627253 54.461069782463952  -149.21044199940826 54.457807999427487  -149.21044200085049 54.457807998807723  -149.21044130370433 54.457807002743927  -149.2106019999928 54.457769000733663  -149.20531238546374 54.450212876266875  -149.22310799835793 54.446076000412027  -149.22310707850249 54.446074660851238  -149.2232689982327 54.44603700127324  -149.20914399871489 54.42549699996956  -149.17378299990094 54.433716000280469  -149.17378395341706 54.433717389125462  -149.17362200012286 54.43375500001185  -149.17367656977959 54.433834420388258  -149.14185300004817 54.441440999563937  -149.1418533296609 54.441441467264539  -149.14169199935253 54.44147999970091","_id_":"ext-record-1615"},
{"footprint":"POLYGON ICRS -149.14169199935253 54.441479999700896  -149.15446578949513 54.45954237942599  -149.1541579981261 54.459616999577037  -149.16148317312297 54.469819722101036  210.831188 54.480022  -149.16881200109574 54.480021999839707  -149.16881200091157 54.480022000436144  -149.16894160954027 54.479990570579986  -149.16897236447315 54.479983113577049  -149.16897299995404 54.479984000401863  -149.16910208115911 54.479952699559519  -149.20036578138604 54.472366809918455  -149.20178927962783 54.472021149073214  -149.20410000079718 54.47145999927757  -149.19664050627253 54.461069782463937  -149.21044199940826 54.457807999427473  -149.21044200085049 54.457807998807723  -149.21044130370433 54.45780700274392  -149.2106019999928 54.457769000733649  -149.20531238546374 54.45021287626686  -149.22310799835793 54.446076000412006  -149.22310707850249 54.446074660851224  -149.2232689982327 54.446037001273226  -149.20914399871489 54.42549699996956  -149.17378299990094 54.433716000280455  -149.17378395341706 54.433717389125448  -149.17362200012286 54.433755000011836  -149.17367656977956 54.433834420388244  -149.14185300004817 54.441440999563923  -149.1418533296609 54.441441467264532  -149.14169199935253 54.441479999700896","_id_":"ext-record-1616"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079107  -149.15446579022228 54.45954238044019  -149.15415799851806 54.4596170001258  -149.16881200030136 54.480022000819872  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600011  -149.20162894990639 54.472058979661952  -149.20393900063306 54.4714979997741  -149.19647921052285 54.461107890891007  -149.21044199978343 54.457807999404864  -149.2051510698665 54.450250359754449  -149.22310800072137 54.446075999802986  -149.20898399944434 54.425535999494414  -149.17362200158792 54.433754999511379  -149.17362200230446 54.433755000554243  -149.17362200033574 54.433755001011441  -149.17367698954931 54.4338350294296  -149.14169199924754 54.441479999079107","_id_":"ext-record-1617"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079107  -149.15446579022228 54.45954238044019  -149.15415799851806 54.4596170001258  -149.16881200030136 54.480022000819872  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600011  -149.20162894990639 54.472058979661952  -149.20393900063306 54.4714979997741  -149.19647921052285 54.461107890891007  -149.21044199978343 54.457807999404864  -149.2051510698665 54.450250359754449  -149.22310800072137 54.446075999802986  -149.20898399944434 54.425535999494414  -149.17362200158792 54.433754999511379  -149.17362200230446 54.433755000554243  -149.17362200033574 54.433755001011441  -149.17367698954931 54.4338350294296  -149.14169199924754 54.441479999079107","_id_":"ext-record-1618"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079107  -149.1544657902223 54.45954238044019  -149.15415799851809 54.4596170001258  -149.16881200030136 54.480022000819872  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600011  -149.20162894990639 54.472058979661952  -149.20393900063306 54.4714979997741  -149.19647921052285 54.461107890891007  -149.21044199978343 54.457807999404864  -149.2051510698665 54.450250359754449  -149.22310800072137 54.446075999802993  -149.20898399944434 54.425535999494414  -149.17362200158794 54.433754999511379  -149.17362200230446 54.433755000554243  -149.17362200033574 54.433755001011441  -149.17367698954931 54.4338350294296  -149.14169199924754 54.441479999079107","_id_":"ext-record-1619"},
{"footprint":"POLYGON ICRS -149.14169199924757 54.441479999079107  -149.1544657902223 54.45954238044019  -149.15415799851809 54.4596170001258  -149.16881200030136 54.480022000819879  -149.16894160931227 54.479990569102263  -149.20020529944316 54.472404679600018  -149.20162894990639 54.472058979661959  -149.20393900063306 54.471497999774108  -149.19647921052288 54.461107890891007  -149.21044199978346 54.457807999404864  -149.20515106986653 54.450250359754456  -149.22310800072137 54.446075999802993  -149.20898399944437 54.425535999494414  -149.17362200158794 54.433754999511386  -149.17362200230448 54.43375500055425  -149.17362200033574 54.433755001011448  -149.17367698954931 54.433835029429609  -149.14169199924757 54.441479999079107","_id_":"ext-record-1620"},
{"footprint":"POLYGON ICRS -149.14169199924757 54.441479999079107  -149.1544657902223 54.45954238044019  -149.15415799851809 54.4596170001258  -149.16881200030136 54.480022000819879  -149.16894160931227 54.479990569102263  -149.20020529944316 54.472404679600018  -149.20162894990639 54.472058979661959  -149.20393900063306 54.471497999774108  -149.19647921052288 54.461107890891007  -149.21044199978346 54.457807999404864  -149.20515106986653 54.450250359754456  -149.22310800072137 54.446075999802993  -149.20898399944437 54.425535999494414  -149.17362200158794 54.433754999511386  -149.17362200230448 54.43375500055425  -149.17362200033574 54.433755001011448  -149.17367698954931 54.433835029429609  -149.14169199924757 54.441479999079107","_id_":"ext-record-1621"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079107  -149.15446579022228 54.459542380440183  -149.15415799851806 54.4596170001258  -149.16881200030133 54.480022000819872  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600011  -149.20162894990636 54.472058979661952  -149.20393900063306 54.4714979997741  -149.19647921052285 54.461107890891007  -149.21044199978343 54.457807999404864  -149.2051510698665 54.450250359754449  -149.22310800072137 54.446075999802993  -149.20898399944434 54.425535999494421  -149.17362200158792 54.433754999511386  -149.17362200230446 54.433755000554243  -149.17362200033571 54.433755001011441  -149.17367698954928 54.433835029429609  -149.14169199924754 54.441479999079107","_id_":"ext-record-1622"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079114  -149.15446579022228 54.4595423804402  -149.15415799851806 54.45961700012581  -149.16881200030136 54.480022000819879  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600025  -149.20162894990639 54.472058979661959  -149.20393900063306 54.471497999774115  -149.19647921052285 54.461107890891014  -149.21044199978343 54.457807999404871  -149.2051510698665 54.450250359754463  -149.22310800072137 54.446075999803  -149.20898399944434 54.425535999494421  -149.17362200158792 54.433754999511386  -149.17362200230446 54.43375500055425  -149.17362200033574 54.433755001011455  -149.17367698954931 54.433835029429609  -149.14169199924754 54.441479999079114","_id_":"ext-record-1623"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079114  -149.15446579022228 54.45954238044019  -149.15415799851806 54.4596170001258  -149.16881200030133 54.480022000819879  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600018  -149.20162894990636 54.472058979661959  -149.20393900063306 54.471497999774108  -149.19647921052285 54.461107890891007  -149.21044199978343 54.457807999404864  -149.2051510698665 54.450250359754456  -149.22310800072137 54.446075999802993  -149.20898399944434 54.425535999494421  -149.17362200158792 54.433754999511386  -149.17362200230446 54.43375500055425  -149.17362200033571 54.433755001011448  -149.17367698954928 54.433835029429609  -149.14169199924754 54.441479999079114","_id_":"ext-record-1624"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079121  -149.15446579022228 54.4595423804402  -149.15415799851806 54.45961700012581  -149.16881200030133 54.480022000819879  -149.16894160931224 54.479990569102263  -149.20020529944313 54.472404679600025  -149.20162894990636 54.472058979661959  -149.20393900063303 54.471497999774115  -149.19647921052285 54.461107890891014  -149.21044199978343 54.457807999404871  -149.2051510698665 54.450250359754463  -149.22310800072134 54.446075999803  -149.20898399944434 54.425535999494429  -149.17362200158792 54.433754999511386  -149.17362200230446 54.433755000554257  -149.17362200033571 54.433755001011455  -149.17367698954928 54.433835029429609  -149.14169199924754 54.441479999079121","_id_":"ext-record-1625"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079121  -149.15446579022228 54.4595423804402  -149.15415799851806 54.45961700012581  -149.16881200030136 54.480022000819886  -149.16894160931224 54.47999056910227  -149.20020529944313 54.472404679600018  -149.20162894990639 54.472058979661959  -149.20393900063306 54.471497999774108  -149.19647921052285 54.461107890891014  -149.21044199978343 54.457807999404871  -149.2051510698665 54.450250359754463  -149.22310800072137 54.446075999803  -149.20898399944434 54.425535999494429  -149.17362200158792 54.433754999511386  -149.17362200230446 54.43375500055425  -149.17362200033574 54.433755001011448  -149.17367698954931 54.433835029429609  -149.14169199924754 54.441479999079121","_id_":"ext-record-1626"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079121  -149.15446579022228 54.4595423804402  -149.15415799851806 54.45961700012581  -149.16881200030136 54.480022000819886  -149.16894160931224 54.47999056910227  -149.20020529944313 54.472404679600018  -149.20162894990639 54.472058979661959  -149.20393900063306 54.471497999774108  -149.19647921052285 54.461107890891014  -149.21044199978343 54.457807999404871  -149.2051510698665 54.450250359754463  -149.22310800072137 54.446075999803  -149.20898399944434 54.425535999494429  -149.17362200158792 54.433754999511386  -149.17362200230446 54.43375500055425  -149.17362200033574 54.433755001011448  -149.17367698954931 54.433835029429609  -149.14169199924754 54.441479999079121","_id_":"ext-record-1627"},
{"footprint":"POLYGON ICRS -149.14169199924754 54.441479999079121  -149.15446579022228 54.4595423804402  -149.15415799851806 54.459617000125817  -149.16881200030133 54.480022000819886  -149.16894160931224 54.479990569102277  -149.20020529944313 54.472404679600025  -149.20162894990636 54.472058979661966  -149.20393900063306 54.471497999774115  -149.19647921052285 54.461107890891022  -149.21044199978343 54.457807999404878  -149.2051510698665 54.450250359754463  -149.22310800072137 54.446075999803007  -149.20898399944434 54.425535999494429  -149.17362200158792 54.433754999511393  -149.17362200230446 54.433755000554257  -149.17362200033571 54.433755001011455  -149.17367698954928 54.433835029429616  -149.14169199924754 54.441479999079121","_id_":"ext-record-1628"},
{"footprint":"POLYGON ICRS -149.0927383993768 54.283794799575254  210.9082487 54.293364  -149.09179203671496 54.293363125127691  210.9082102 54.2933848  -149.10643809969417 54.29306929998873  -149.10644032828756 54.293047627079048  -149.10647399977 54.293046899881347  -149.10648142051227 54.292974730483287  -149.10658929984268 54.292972400049841  -149.10659152862698 54.292950724898176  -149.10662509976069 54.292949999817047  -149.10663253075532 54.292877731895445  -149.10674049988302 54.292875399982108  -149.10674273873184 54.2928536229599  -149.10677619993476 54.292852900059351  -149.10766891948617 54.284167860386667  -149.10775980003712 54.283283600137885  -149.10775978981775 54.28328360045662  210.8922402 54.2832835  -149.10771913099765 54.283284379120779  210.8922786 54.2832623  -149.09307630021354 54.283578000154911  -149.09307403356419 54.283599981191031  -149.09304060015853 54.283600699822081  -149.09303316578573 54.283672781269487  -149.09292530015225 54.283675100044775  -149.09292304400432 54.283696979052245  -149.09288949964369 54.283697699828494  -149.09288207735568 54.283769676775471  -149.09277399948041 54.283772000158365  -149.09277172257131 54.283794083271076  -149.0927383993768 54.283794799575254","_id_":"ext-record-1629"},
{"footprint":"POLYGON ICRS -149.0927383993768 54.283794799575254  210.9082487 54.293364  -149.09179203671496 54.293363125127691  210.9082102 54.2933848  -149.10643809969417 54.29306929998873  -149.10644032828756 54.293047627079048  -149.10647399977 54.293046899881347  -149.10648142051227 54.292974730483287  -149.10658929984268 54.292972400049841  -149.10659152862698 54.292950724898176  -149.10662509976069 54.292949999817047  -149.10663253075532 54.292877731895445  -149.10674049988302 54.292875399982108  -149.10674273873184 54.2928536229599  -149.10677619993476 54.292852900059351  -149.10766891948617 54.284167860386667  -149.10775980003712 54.283283600137885  -149.10775978981775 54.28328360045662  210.8922402 54.2832835  -149.10771913099765 54.283284379120779  210.8922786 54.2832623  -149.09307630021354 54.283578000154911  -149.09307403356419 54.283599981191031  -149.09304060015853 54.283600699822081  -149.09303316578573 54.283672781269487  -149.09292530015225 54.283675100044775  -149.09292304400432 54.283696979052245  -149.09288949964369 54.283697699828494  -149.09288207735568 54.283769676775471  -149.09277399948041 54.283772000158365  -149.09277172257131 54.283794083271076  -149.0927383993768 54.283794799575254","_id_":"ext-record-1630"},
{"footprint":"POLYGON ICRS -149.32470660138787 54.373335499588123  210.683371 54.4298665  -149.36583129992138 54.430264400104214  -149.36583636691628 54.430228686451507  -149.36654850203021 54.430234299909209  -149.36655366648185 54.430197896282053  -149.36726529807612 54.430203500887522  -149.36729991866352 54.429959432223065  -149.41407230003668 54.430111200668691  -149.41407763763905 54.430078628704905  -149.41479000056518 54.43008080020477  -149.41479543593587 54.430047636798385  -149.41550619802814 54.430049799765264  -149.42463770047704 54.374237599614915  -149.37713629961974 54.374087300320575  -149.37713093654835 54.374119889150982  -149.37642060207821 54.374117500450318  -149.37641515542094 54.374150596222776  -149.37570429933342 54.374148200591605  -149.37215571504777 54.395696709087574  -149.37527319940028 54.373669299973635  -149.3261387993596 54.373275200809424  -149.32613371447744 54.373310850016708  -149.32542250017909 54.373304999728092  -149.32541731382094 54.373341351035464  -149.32470660138787 54.373335499588123","_id_":"ext-record-1631"},
{"footprint":"POLYGON ICRS -149.3247066012722 54.37333549982047  210.683371 54.4298665  -149.36583130372568 54.430264399485743  -149.36583636995093 54.430228685009595  -149.36654850424361 54.430234301687555  -149.36655366625649 54.430197897948673  -149.36726530372238 54.430203502790405  -149.367299915741 54.429959431687912  -149.41407229854 54.430111201172927  -149.41407764046869 54.430078629957762  -149.41478999873462 54.430080801591124  -149.41479543447113 54.430047638788508  -149.41550619830781 54.430049799061941  -149.42463770047704 54.374237599614915  -149.37713630062862 54.374087299264218  -149.37713602080643 54.374089002950534  -149.37713500148627 54.374088998482264  -149.37713473835541 54.37409060506463  -149.37713389958958 54.374090601247332  -149.37712908174527 54.374119883662722  -149.37642059951932 54.374117499280828  -149.37642035286041 54.374119001301224  -149.37641949747183 54.374118998370747  -149.37641921761966 54.374120702055421  -149.37641840063458 54.374120698787721  -149.37641347897195 54.374150590675129  -149.37570430222087 54.374148199989733  -149.37570402109725 54.3741499051753  -149.3757030017758 54.37414990069496  -149.37570273813506 54.374151502543327  -149.3757018997251 54.3741514997187  -149.3721715559636 54.395584818086895  -149.37527319940028 54.373669299973635  -149.32613880498442 54.373275199027574  -149.32613371608937 54.373310848407804  -149.32542250233416 54.373305001573065  -149.3254173148722 54.373341352809966  -149.3247066012722 54.37333549982047","_id_":"ext-record-1632"},
{"footprint":"POLYGON ICRS -149.32470709995602 54.373336399635939  210.6833706 54.4298654  -149.36583079860895 54.430263500082383  -149.36583583797437 54.4302279828151  -149.366548198139 54.430233599552757  -149.36655337790211 54.430197092869356  -149.36726480055628 54.430202700034734  -149.36729950430396 54.429958035916805  -149.41407059985326 54.430110099193442  -149.41407592158563 54.430077622671163  -149.41478860181209 54.430079799678879  -149.41479405237374 54.430046533210245  -149.41550440042803 54.43004870068674  -149.42463510031459 54.374239599901706  -149.37713499798059 54.374088999866331  -149.37712966954527 54.374121392987  -149.37641950012664 54.374119000594767  -149.37641401990231 54.374152301568877  -149.37570300021233 54.374149900843442  -149.37215866152602 54.395673544401994  -149.37527290012505 54.373670299723088  -149.32613919679369 54.37327610020548  -149.32613412490787 54.373311653093978  -149.32542279911641 54.373305799261388  -149.32541759823798 54.373342250860496  -149.32470709995602 54.373336399635939","_id_":"ext-record-1633"},
{"footprint":"POLYGON ICRS -149.32470890116184 54.373338000355311  210.6833697 54.4298642  -149.36582899829543 54.430262000229931  -149.36583405197362 54.430226387846467  -149.36654640038796 54.430232000490172  -149.3665515802474 54.430195497535109  -149.36726309891858 54.430201100145311  -149.36729779629667 54.429956533223134  -149.41406549947263 54.430108099385016  -149.41407082139344 54.430075630273528  -149.41478350139977 54.430077799896807  -149.41478895092254 54.430044542387783  -149.41549939870686 54.430046700781453  -149.42463190172631 54.374240699748874  -149.37713389865493 54.37409060183915  -149.37712855208696 54.374123086109343  -149.37641839945377 54.374120700279562  -149.37641293543962 54.374153893989842  -149.37570189949227 54.374151500585256  -149.37216077712296 54.395650226394395  -149.37527199865892 54.37367160035744  -149.32614100197728 54.373277600177758  -149.32613591585374 54.373313250887179  -149.32542460034833 54.37330739996623  -149.32541939919665 54.373343847540916  -149.32470890116184 54.373338000355311","_id_":"ext-record-1634"},
{"footprint":"POLYGON ICRS -148.967337999045 54.35765399935957  -148.98524671103897 54.374163470513771  -148.98496599782447 54.374267999911034  -149.00541699965635 54.392894999459273  -149.00553785030263 54.392849999731411  -149.03401810093044 54.382238470023715  -149.03531193128879 54.381756109643916  -149.0374150000996 54.380972001128072  -149.0374149992964 54.380972000396646  -149.03741500135456 54.38097199962926  -149.03684348053972 54.380451520444772  -149.03849455841518 54.379835897656939  -149.04940179854495 54.3757679968055  -149.05069478160161 54.375285652566312  -149.05100601987394 54.375169540006631  -149.05279799939169 54.374501000377286  -149.05279798896802 54.374500989998033  -149.05279799530408 54.374500986231666  -149.04238409474056 54.365017496167638  -149.04580100575751 54.363769467771483  -149.04630922724684 54.36358382542916  -149.05512500768037 54.36036300267854  -149.05401611900629 54.359333216910606  -149.05036632219975 54.355943325625752  -149.04840463870593 54.354121046334889  -149.04872099954903 54.3540070006793  -149.04800725242904 54.353334992445362  -149.05265456867994 54.351659502126587  -149.06409102040294 54.347534996128928  -149.04411700109137 54.328723992632149  -149.01487602002913 54.339268059685871  -149.01180700127009 54.340373999370556  -149.01183948508879 54.340404617845216  -149.01188441760797 54.340446994067953  -149.00602692440324 54.342606302280494  -148.98272300130219 54.351192000672135  -148.98334181284426 54.351762526217584  -148.967337999045 54.35765399935957","_id_":"ext-record-1635"},
{"footprint":"POLYGON ICRS -148.98272299951063 54.35119200060128  -149.00063505955924 54.367698650536838  -149.00050439812011 54.36774733527897  -149.00035499816835 54.367803000028168  -149.0208089985245 54.386428000229394  -149.020930720654 54.386382650692013  -149.04940179926126 54.37576798971326  -149.05069477885016 54.375285650347934  -149.05279800048925 54.374501001053311  -149.04238408058447 54.365017499815096  -149.05512500105507 54.360363001530445  -149.05512499848248 54.360363000799971  -149.0551250023087 54.360363000031434  -149.04768395016552 54.35345153933018  -149.06409100139129 54.347535000387651  -149.04411700095275 54.328723998979378  -149.0441169996997 54.328724000478275  -149.01180700089347 54.340374000419516  -149.01188443981312 54.340446990038338  -148.98272299951063 54.35119200060128","_id_":"ext-record-1636"},
{"footprint":"POLYGON ICRS -148.96733799822715 54.357653999578446  -148.97416766475649 54.363951929167939  -148.98524670869091 54.374163469705145  -148.98496599806691 54.374267999866063  -149.00541699999269 54.392895000798326  -149.00553784965584 54.392849999142094  -149.03401810082323 54.382238470211796  -149.03531193085 54.381756110109357  -149.03741499766249 54.380972000936339  -149.03741499685935 54.380972000204913  -149.03741499891748 54.380971999437527  -149.03684347564146 54.3804515177945  -149.04940179926126 54.37576798971326  -149.05069477885016 54.375285650347934  -149.05279799918367 54.374501001699691  -149.04238407943291 54.365017501017391  -149.05512500161942 54.36036300088201  -149.05512499904685 54.360363000151494  -149.05512500287313 54.360362999383014  -149.04840462945177 54.354121049047222  -149.04872100112345 54.354006999495425  -149.04800725257618 54.353334989511637  -149.06409100113606 54.347535001205962  -149.044117002028 54.32872399971464  -149.04411700077483 54.328724001213516  -149.01180700162553 54.340374000777992  -149.01188443780558 54.340446990335913  -148.982723001895 54.351192000175971  -148.98334181312438 54.351762525555166  -148.96733799822715 54.357653999578446","_id_":"ext-record-1637"},
{"footprint":"POLYGON ICRS -148.98272299995617 54.351192000390689  -149.0006350610187 54.367698650647895  -149.00035499908842 54.3678029996481  -149.02080899984495 54.3864280002589  -149.020930720654 54.386382650692006  -149.04940180009152 54.375767990350141  -149.0506947790187 54.375285649931115  -149.05279799988841 54.374501000034243  -149.04238408060709 54.365017500154657  -149.05512500040592 54.360363000491049  -149.05512499861402 54.360362998827028  -149.05512499957339 54.360362998973216  -149.04768395087405 54.35345153961736  -149.06409100012178 54.347534999834117  -149.04411700166085 54.328723999743744  -149.0441170014457 54.328724000399376  210.955883 54.328724  -149.01180700123518 54.340374000162711  -149.01188443977705 54.340446989916551  -148.98272299995617 54.351192000390689","_id_":"ext-record-1638"},
{"footprint":"POLYGON ICRS -149.3764175984773 54.374121000438194  -149.36722069923928 54.429922999729982  -149.41478310109355 54.430077200211009  -149.42391539977228 54.374271600077869  -149.3764175984773 54.374121000438194POLYGON ICRS  -149.32542440072211 54.3733083006107  -149.31734820006758 54.429833499757692  -149.36654660144188 54.430231099734236  -149.37455520002914 54.373702599631734  -149.32542440072211 54.3733083006107","_id_":"ext-record-1639"},
{"footprint":"POLYGON ICRS -149.201211999684 54.255561000022418  210.804055 54.265195  -149.19683124609591 54.2653604478811  -149.19665100000077 54.265689999842884  -149.21315200075003 54.268769000003026  -149.21842099992728 54.2591370001527  -149.21753383360451 54.258971431241534  210.782286 54.258642  -149.201211999684 54.255561000022418","_id_":"ext-record-1640"},
{"footprint":"POLYGON ICRS -149.201211999684 54.255561000022418  210.804055 54.265195  -149.19683124609591 54.2653604478811  -149.19665100000077 54.265689999842884  -149.21315200075003 54.268769000003026  -149.21842099992728 54.2591370001527  -149.21753383360451 54.258971431241534  210.782286 54.258642  -149.201211999684 54.255561000022418","_id_":"ext-record-1641"},
{"footprint":"POLYGON ICRS -149.15212499994971 54.438016000164644  -149.16758300037336 54.442653999926179  -149.17554599988807 54.433662999852189  -149.16008899978888 54.429029000056943  -149.15212499994971 54.438016000164644","_id_":"ext-record-1642"},
{"footprint":"POLYGON ICRS -149.15212499994971 54.438016000164644  -149.16758300037336 54.442653999926179  -149.17554599988807 54.433662999852189  -149.16008899978888 54.429029000056943  -149.15212499994971 54.438016000164644","_id_":"ext-record-1643"},
{"footprint":"POLYGON ICRS -148.95870199873809 54.357879000068607  -148.9925999990337 54.367847000644964  -149.00130366919603 54.357795889560222  -149.01465899957628 54.361814000638006  -149.02107568899476 54.3545625902194  -149.03816500112129 54.359776999617935  -149.05576900068382 54.340180001080782  -149.02211699850966 54.3299100003509  -149.02204842923499 54.329986359820275  211.008664 54.320834  -148.97608434948242 54.338224789484521  -148.97578599983453 54.338137000245709  -148.97401551076925 54.340183940581078  -148.95870199873809 54.357879000068607","_id_":"ext-record-1644"},
{"footprint":"POLYGON ICRS -148.95870199873809 54.357879000068607  -148.9925999990337 54.367847000644964  -149.00130366919603 54.357795889560222  -149.01465899957628 54.361814000638006  -149.02107568899476 54.3545625902194  -149.03816500112129 54.359776999617935  -149.05576900068382 54.340180001080782  -149.02211699850966 54.3299100003509  -149.02204842923499 54.329986359820275  211.008664 54.320834  -148.97608434948242 54.338224789484521  -148.97578599983453 54.338137000245709  -148.97401551076925 54.340183940581078  -148.95870199873809 54.357879000068607","_id_":"ext-record-1645"},
{"footprint":"POLYGON ICRS -149.22445680131864 54.431274997906314  210.7836325 54.4878065  -149.26563999884078 54.488204496684276  -149.26564522796446 54.488167691809039  -149.26635749740336 54.488173300461348  -149.26636250116488 54.488138090300964  -149.26707610175833 54.488143700269489  -149.26711078647114 54.48789953305036  -149.31394950172887 54.488051198321031  -149.31395501078927 54.488017637539635  -149.3146657966366 54.48801980070256  -149.31467106188285 54.487987732710643  -149.31538570057589 54.487989900351678  -149.32453010041345 54.432176999379095  -149.27696090171494 54.432026698649821  -149.27696062124713 54.4320284034839  -149.27695950052615 54.432028399434024  -149.2769592056157 54.432030202079851  -149.27695830033173 54.432030199354472  -149.27695334204986 54.432060283808283  -149.27624299972646 54.432057900508781  -149.27624271798047 54.432059606842415  -149.27624169981556 54.4320596019969  -149.27623667582313 54.432090090403726  -149.27552669713504 54.432087701592813  -149.27552639958026 54.432089503509189  -149.27552530066802 54.43208950002419  -149.27552500357493 54.432091302597371  -149.27552399909993 54.432091299217618  -149.27199386000686 54.453493185994333  -149.27509550079748 54.431608699678129  -149.22589090156498 54.431214601676125  -149.22588565552945 54.431251345086544  -149.22517439845529 54.431245501601381  -149.22516935021602 54.431280859335317  -149.22445680131864 54.431274997906314","_id_":"ext-record-1646"},
{"footprint":"POLYGON ICRS -149.22445679982746 54.431274999602458  210.7836325 54.4878065  -149.26563999950022 54.48820450041535  -149.26564522884289 54.488167694594104  -149.26635749949523 54.488173299562916  -149.2663625027121 54.488138088769773  -149.26707610018889 54.48814370076775  -149.26711078314369 54.487899532417316  -149.31394950114606 54.488051199426963  -149.31395500870164 54.488017637480304  -149.31466579880083 54.488019799382684  -149.3146710607314 54.487987730039556  -149.31538570165498 54.487989901181471  -149.32453010041345 54.432176999379095  -149.27696090079615 54.432026699643856  -149.27695536652845 54.432060293693091  -149.27624299987627 54.432057899362512  -149.27623769435903 54.432090092068549  -149.27552669976663 54.4320877005649  -149.2719762221578 54.453617556656724  -149.27509550079748 54.431608699678129  -149.22589090029234 54.431214600887763  -149.22588565212897 54.43125134419185  -149.22517439957286 54.431245500928782  -149.22516934781646 54.431280858131359  -149.22445679982746 54.431274999602458","_id_":"ext-record-1647"},
{"footprint":"POLYGON ICRS -149.22445729915236 54.431276000622688  210.783632 54.4878054  -149.26563950148903 54.488203499669218  -149.26564470324644 54.488166889808262  -149.26635709843572 54.488172500197962  -149.26636211731272 54.488137186365158  -149.26707560057284 54.488142801469877  -149.26711036926719 54.487898036651515  -149.31394750016602 54.488049999828043  -149.3139529917957 54.488016533618477  -149.31466409935618 54.488018701057264  -149.31466937798629 54.487986523438387  -149.3153837981622 54.487988699336846  -149.32452729979673 54.432178999026632  -149.27695949912092 54.432028399000195  -149.27695396235552 54.432061996051743  -149.27624170205968 54.432059600150168  -149.27623637961833 54.432091896895756  -149.27552530031113 54.432089500569745  -149.27197932575115 54.453592892896907  -149.27509510074682 54.431609799656073  -149.22589140007216 54.431215501963152  -149.22588615065911 54.431252246767912  -149.2251748018613 54.431246400476091  -149.22516973527681 54.431281859759622  -149.22445729915236 54.431276000622688","_id_":"ext-record-1648"},
{"footprint":"POLYGON ICRS -149.22445929895648 54.431277700034983  210.7836309 54.4878039  -149.26563749954494 54.488201699633976  -149.26564251770915 54.488166387419696  -149.26635600004914 54.488172000718542  -149.26636119925314 54.488135398260994  -149.26707369868208 54.488140999953842  -149.26710845882067 54.487896334232559  -149.3139420005021 54.4880478995955  -149.31394729733285 54.488015628862819  -149.31466160036089 54.488017800040211  -149.31466707577565 54.487984442470065  -149.31537830101797 54.487986599895571  -149.32452360031837 54.432180299364305  -149.27695829801641 54.4320302004877  -149.27695296066949 54.432062584819732  -149.27624179944385 54.432060199868587  -149.27623627813642 54.43209369433162  -149.27552399908095 54.432091299826141  -149.27198074126255 54.453572678484477  -149.27509390100545 54.43161120006809  -149.22589329820678 54.431217300536417  -149.22588823514386 54.431252750774348  -149.22517580027196 54.431246900437941  -149.22517056348124 54.431283545565663  -149.22445929895648 54.431277700034983","_id_":"ext-record-1649"},
{"footprint":"POLYGON ICRS -149.27623990008939 54.432061600244232  -149.26703019922968 54.4878619006521  -149.31465860021274 54.488016099247041  -149.32380340033347 54.43221219935036  -149.27623990008939 54.432061600244232POLYGON ICRS  -149.22517630169926 54.431248899657369  -149.21708880125712 54.487772399504642  -149.26635550120756 54.488169999788241  -149.27437520064368 54.431643100973282  -149.22517630169926 54.431248899657369","_id_":"ext-record-1650"},
{"footprint":"POLYGON ICRS -149.03119500010348 54.357169999835676  -149.04041400000526 54.365737000212185  -149.05512500016937 54.360362999986926  -149.04590199972193 54.351795999965233  -149.03119500010348 54.357169999835676","_id_":"ext-record-1651"},
{"footprint":"POLYGON ICRS -149.03119500010348 54.357169999835683  -149.04041400000526 54.365737000212192  -149.05512500016937 54.360362999986926  -149.04590199972193 54.351795999965233  -149.03119500010348 54.357169999835683","_id_":"ext-record-1652"},
{"footprint":"POLYGON ICRS -149.03119499951475 54.357169999931131  -149.04041400000526 54.365737000212192  -149.05512499998591 54.360363000058065  -149.04590199972193 54.351795999965233  -149.03119499951475 54.357169999931131POLYGON ICRS  -149.01581600008259 54.363638000056149  -149.02503299998881 54.372206000042134  -149.03974799977524 54.366834000091046  -149.03974799957416 54.366833999904273  -149.03974800008885 54.366833999716341  -149.0305270002338 54.358266000000036  -149.01581600008259 54.363638000056149","_id_":"ext-record-1653"},
{"footprint":"POLYGON ICRS -149.03119499951475 54.357169999931131  -149.04041400000526 54.365737000212192  -149.05512499998591 54.360363000058065  -149.04590199972193 54.351795999965233  -149.03119499951475 54.357169999931131POLYGON ICRS  -149.01581600008259 54.363638000056149  -149.02503299998881 54.372206000042134  -149.03974799977524 54.366834000091046  -149.03974799957416 54.366833999904273  -149.03974800008885 54.366833999716341  -149.0305270002338 54.358266000000036  -149.01581600008259 54.363638000056149","_id_":"ext-record-1654"},
{"footprint":"POLYGON ICRS -149.04261200008122 54.319737000102279  -149.0372499997091 54.329358999954863  -149.05374300020247 54.3324820001239  -149.05910200000721 54.322860999818936  -149.04261200008122 54.319737000102279","_id_":"ext-record-1655"},
{"footprint":"POLYGON ICRS -149.04261200008122 54.319737000102279  -149.0372499997091 54.329358999954863  -149.05374300020247 54.3324820001239  -149.05910200000721 54.322860999818936  -149.04261200008122 54.319737000102279","_id_":"ext-record-1656"},
{"footprint":"POLYGON ICRS -149.04423799994379 54.302580000088753  -149.05318700008917 54.311244000094128  -149.06803400000655 54.306025999779919  -149.05908599996047 54.297359000037247  -149.04423799994379 54.302580000088753","_id_":"ext-record-1657"},
{"footprint":"POLYGON ICRS -149.04423799994379 54.302580000088746  -149.05318700008917 54.311244000094128  -149.06803400000655 54.306025999779919  -149.05908599996047 54.297359000037247  -149.04423799994379 54.302580000088746","_id_":"ext-record-1658"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019575  -149.35501199901566 54.297936000550884  -149.3551496502615 54.29796567085026  -149.34353799897551 54.316895000138331  -149.37505195995627 54.323476769798667  -149.37494199986565 54.323658999793139  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507521  -149.41084399863757 54.330954999603605  -149.41089179062692 54.33087580982891  -149.42209829999996 54.312295740137934  -149.42260680977887 54.311452230704766  -149.42343399968578 54.310079999493389  -149.40523096936337 54.306343579719957  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100487  -149.40386699811481 54.284779000382947  -149.36829799758968 54.277121998442261  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019575","_id_":"ext-record-1659"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019575  -149.35501199901566 54.297936000550877  -149.3551496502615 54.297965670850253  -149.34353799897551 54.316895000138324  -149.37505195995627 54.32347676979866  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.3309549996036  -149.41089179062689 54.330875809828896  -149.42209829999993 54.31229574013792  -149.42260680977884 54.311452230704752  -149.42343399968578 54.310079999493382  -149.40523096936334 54.306343579719943  -149.41034699974489 54.2981050003895  -149.39719642908898 54.295328100486984  -149.40386699811478 54.284779000382933  -149.36829799758968 54.277121998442261  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019575","_id_":"ext-record-1660"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.297936000550877  -149.3551496502615 54.297965670850253  -149.34353799897551 54.316895000138324  -149.37505195995627 54.32347676979866  -149.37494199986565 54.323658999793139  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.3309549996036  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442261  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1661"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019575  -149.35501199901569 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138324  -149.37505195995627 54.32347676979866  -149.37494199986565 54.323658999793132  -149.41069200141189 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.3309549996036  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019575","_id_":"ext-record-1662"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.297936000550877  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138324  -149.37505195995627 54.32347676979866  -149.37494199986565 54.323658999793139  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.3309549996036  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442261  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1663"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.330875809828896  -149.42209829999993 54.31229574013792  -149.42260680977884 54.311452230704752  -149.42343399968578 54.310079999493382  -149.40523096936334 54.306343579719943  -149.41034699974489 54.2981050003895  -149.39719642908898 54.295328100486984  -149.40386699811478 54.284779000382933  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1664"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062692 54.33087580982891  -149.42209829999996 54.312295740137934  -149.42260680977887 54.311452230704766  -149.42343399968578 54.310079999493389  -149.40523096936337 54.306343579719957  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100487  -149.40386699811481 54.284779000382947  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1665"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1666"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285585  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1667"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1668"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.277164000019567  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285592  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.36829799758968 54.277121998442254  -149.36825641718571 54.277187790325087  -149.36814599997686 54.277164000019567","_id_":"ext-record-1669"},
{"footprint":"POLYGON ICRS -149.36814599997686 54.27716400001956  -149.35501199901566 54.29793600055087  -149.35514965026152 54.297965670850253  -149.34353799897553 54.316895000138317  -149.37505195995627 54.323476769798653  -149.37494199986565 54.323658999793132  -149.41069200141186 54.330997000285585  -149.41073129431931 54.330931886507514  -149.41084399863757 54.330954999603591  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974489 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811478 54.28477900038294  -149.36829799758968 54.277121998442247  -149.36825641718571 54.27718779032508  -149.36814599997686 54.27716400001956","_id_":"ext-record-1670"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1671"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.33093188719193  -149.41084399890852 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103345  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354951  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1672"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1673"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458676  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103359  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911193 54.277187791337013  -149.36814599923346 54.277164000458676","_id_":"ext-record-1674"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.33093188719193  -149.41084399890852 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103359  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1675"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.33087580979435  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992731 54.310079999458793  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1676"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.330931887191937  -149.41084399890852 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103359  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1677"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901714  -149.41073129423657 54.330931887191937  -149.41084399890849 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458669","_id_":"ext-record-1678"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1679"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.33093188719193  -149.41084399890852 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1680"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1681"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458676  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901714  -149.4107312942366 54.330931887191944  -149.41084399890852 54.330955000092807  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103359  -149.4226068100204 54.3114522306702  -149.42343399992734 54.3100799994588  -149.4052309696049 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.29532810045248  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458676","_id_":"ext-record-1682"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103345  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354951  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1683"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1684"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.33093188719193  -149.41084399890852 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1685"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.33093188719193  -149.41084399890852 54.330955000092793  -149.41089179086859 54.33087580979435  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992734 54.310079999458793  -149.4052309696049 54.306343579685404  -149.41034699998639 54.298105000354951  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1686"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1687"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458662  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.330931887191937  -149.41084399890852 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103345  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354951  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755941  -149.36825641911196 54.277187791337006  -149.36814599923346 54.277164000458662","_id_":"ext-record-1688"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670184  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.2847790003484  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1689"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.4107312942366 54.330931887191937  -149.41084399890852 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1690"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1691"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.330955000092793  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911196 54.277187791337013  -149.36814599923346 54.277164000458669","_id_":"ext-record-1692"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458676  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911193 54.277187791337013  -149.36814599923346 54.277164000458676","_id_":"ext-record-1693"},
{"footprint":"POLYGON ICRS -149.36814599923346 54.277164000458669  210.644988 54.297936  210.64485035 54.29796567  210.656462 54.316895  210.62494804 54.32347677  210.625058 54.323659  -149.41069199898112 54.330996999901707  -149.41073129423657 54.330931887191937  -149.41084399890849 54.3309550000928  -149.41089179086859 54.330875809794357  -149.42209830024152 54.312295740103352  -149.4226068100204 54.311452230670191  -149.42343399992734 54.3100799994588  -149.40523096960487 54.306343579685411  -149.41034699998639 54.298105000354958  -149.39719642933045 54.295328100452473  -149.40386699835619 54.284779000348408  -149.36829799954239 54.277121999755948  -149.36825641911193 54.277187791337006  -149.36814599923346 54.277164000458669","_id_":"ext-record-1694"},
{"footprint":"POLYGON ICRS -149.3682979980878 54.277122000275334  -149.35516299858924 54.297893999935305  -149.35516300101622 54.297894000544822  210.644837 54.297894  -149.3553012290171 54.297923790365623  -149.34368999873172 54.316852999746253  -149.34369000116646 54.31685300034006  210.65631 54.316853  -149.37520395925878 54.323434769937251  -149.37509399948365 54.323616999974249  -149.41084400004456 54.330955000104169  -149.41089179062689 54.3308758098289  -149.42209829999993 54.312295740137927  -149.42260680977884 54.311452230704759  -149.42343399968578 54.310079999493382  -149.40523096936334 54.30634357971995  -149.41034699974492 54.298105000389505  -149.39719642908898 54.295328100486991  -149.40386699811481 54.28477900038294  -149.3682979980878 54.277122000275334","_id_":"ext-record-1695"},
{"footprint":"POLYGON ICRS -149.3682979980878 54.277122000275327  -149.35516299858926 54.2978939999353  -149.35516300101619 54.297894000544815  210.644837 54.297894  -149.3553012290171 54.297923790365616  -149.34368999873172 54.316852999746246  -149.34369000116646 54.316853000340053  210.65631 54.316853  -149.37520395925878 54.323434769937251  -149.37509399948365 54.323616999974242  -149.41084400004453 54.330955000104169  -149.41089179062689 54.330875809828896  -149.42209829999993 54.31229574013792  -149.42260680977884 54.311452230704752  -149.42343399968578 54.310079999493382  -149.40523096936334 54.306343579719943  -149.41034699974489 54.2981050003895  -149.39719642908898 54.295328100486984  -149.40386699811478 54.284779000382933  -149.3682979980878 54.277122000275327","_id_":"ext-record-1696"},
{"footprint":"POLYGON ICRS -149.301637000521 54.4210699991331  -149.30163700126462 54.4210700001354  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757368  -149.31463999987102 54.439078998306591  -149.31464000048743 54.439078999334008  -149.31463999862467 54.439078999808338  -149.32989800040349 54.459331000909671  -149.33002733086286 54.459298069829131  -149.33005690112506 54.459290540812447  -149.33005799986529 54.45929199953877  -149.3301869205556 54.459259169923406  -149.36120175949492 54.451355449144145  -149.36261185026939 54.450995849636065  -149.36490499953607 54.450411000841726  -149.35713828484819 54.440099201708364  -149.37083199978926 54.436696999437871  -149.37083185196542 54.4366967983739  -149.3709920036091 54.436656999508394  -149.36547769317787 54.429155165976951  -149.38313800013259 54.4248380010451  -149.38313799939735 54.424838000026547  -149.383138001376 54.424837999542653  -149.38313731809134 54.424837054271471  -149.38329700241914 54.424797998285676  -149.36856399823532 54.404405000296542  -149.3334729992045 54.412982000364984  -149.33347371530698 54.412982993465612  -149.33331399952374 54.413022000292585  -149.33337126911954 54.413101340303356  -149.31636692878308 54.417372649008492  -149.301637000521 54.4210699991331","_id_":"ext-record-1698"},
{"footprint":"POLYGON ICRS -149.301637000521 54.4210699991331  -149.3016370012646 54.4210700001354  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.314639999871 54.4390789983066  -149.3146400004874 54.439078999334015  -149.31463999862464 54.439078999808345  -149.32989800040346 54.459331000909678  -149.33002733086283 54.459298069829138  -149.33005690112506 54.459290540812439  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923406  -149.36120175949489 54.451355449144138  -149.36261185026936 54.450995849636065  -149.36490499953604 54.450411000841726  -149.35713828484816 54.440099201708371  -149.37083199978926 54.436696999437871  -149.37083185196542 54.4366967983739  -149.3709920036091 54.436656999508394  -149.36547769317784 54.429155165976951  -149.38313800013259 54.4248380010451  -149.38313799939732 54.424838000026554  -149.38313800137598 54.424837999542653  -149.38313731809134 54.424837054271471  -149.38329700241911 54.424797998285676  -149.36856399823532 54.404405000296542  -149.33347299920447 54.412982000364984  -149.33347371530698 54.412982993465612  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303356  -149.31636692878305 54.417372649008492  -149.301637000521 54.4210699991331","_id_":"ext-record-1699"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.30163700126462 54.421070000135416  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.31463999987102 54.4390789983066  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808345  -149.32989800040349 54.459331000909685  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812439  -149.33005799986529 54.459291999538777  -149.3301869205556 54.459259169923421  -149.36120175949489 54.451355449144152  -149.36261185026939 54.450995849636072  -149.36490499953607 54.450411000841733  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437878  -149.37083185196542 54.436696798373909  -149.3709920036091 54.4366569995084  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939735 54.424838000026554  -149.383138001376 54.42483799954266  -149.38313731809134 54.424837054271471  -149.38329700241914 54.424797998285683  -149.36856399823532 54.404405000296556  -149.3334729992045 54.412982000365  -149.33347371530698 54.412982993465612  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878308 54.417372649008492  -149.301637000521 54.42106999913311","_id_":"ext-record-1700"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.3016370012646 54.421070000135408  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.31463999987102 54.4390789983066  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808345  -149.32989800040346 54.459331000909685  -149.33002733086283 54.459298069829146  -149.33005690112503 54.459290540812439  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923413  -149.36120175949489 54.451355449144138  -149.36261185026936 54.450995849636065  -149.36490499953604 54.450411000841726  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437878  -149.37083185196542 54.436696798373909  -149.3709920036091 54.436656999508394  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939732 54.424838000026554  -149.38313800137598 54.42483799954266  -149.38313731809134 54.424837054271471  -149.38329700241911 54.424797998285683  -149.36856399823529 54.404405000296542  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465612  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878308 54.417372649008492  -149.301637000521 54.42106999913311","_id_":"ext-record-1701"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.3016370012646 54.421070000135408  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.31463999987102 54.4390789983066  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808345  -149.32989800040346 54.459331000909685  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812447  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923413  -149.36120175949486 54.451355449144145  -149.36261185026936 54.450995849636072  -149.36490499953604 54.450411000841726  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437878  -149.37083185196542 54.436696798373909  -149.3709920036091 54.436656999508394  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939732 54.424838000026554  -149.38313800137598 54.42483799954266  -149.38313731809134 54.424837054271471  -149.38329700241911 54.424797998285683  -149.36856399823529 54.404405000296549  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465612  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878308 54.417372649008492  -149.301637000521 54.42106999913311","_id_":"ext-record-1702"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.3016370012646 54.421070000135416  -149.30163699927471 54.421070000634721  -149.31494635142371 54.439000989757375  -149.314639999871 54.439078998306606  -149.3146400004874 54.439078999334015  -149.31463999862464 54.439078999808352  -149.32989800040346 54.459331000909685  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812439  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923413  -149.36120175949489 54.451355449144138  -149.36261185026936 54.450995849636065  -149.36490499953604 54.450411000841733  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437885  -149.37083185196539 54.436696798373909  -149.3709920036091 54.4366569995084  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939732 54.424838000026561  -149.38313800137598 54.42483799954266  -149.38313731809131 54.424837054271478  -149.38329700241911 54.424797998285683  -149.36856399823529 54.404405000296542  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465619  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878305 54.4173726490085  -149.301637000521 54.42106999913311","_id_":"ext-record-1703"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.30163700126462 54.421070000135416  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.31463999987102 54.439078998306606  -149.31464000048743 54.439078999334015  -149.31463999862467 54.439078999808352  -149.32989800040349 54.459331000909685  -149.33002733086286 54.459298069829146  -149.33005690112506 54.459290540812447  -149.33005799986529 54.45929199953877  -149.3301869205556 54.459259169923406  -149.36120175949492 54.451355449144145  -149.36261185026939 54.450995849636058  -149.36490499953607 54.450411000841733  -149.35713828484819 54.440099201708378  -149.37083199978926 54.436696999437885  -149.37083185196542 54.436696798373909  -149.3709920036091 54.4366569995084  -149.36547769317787 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939735 54.424838000026561  -149.383138001376 54.42483799954266  -149.38313731809134 54.424837054271478  -149.38329700241914 54.424797998285683  -149.36856399823532 54.404405000296542  -149.3334729992045 54.412982000365  -149.33347371530698 54.412982993465619  -149.33331399952374 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878308 54.417372649008492  -149.301637000521 54.42106999913311","_id_":"ext-record-1704"},
{"footprint":"POLYGON ICRS -149.301637000521 54.42106999913311  -149.3016370012646 54.421070000135408  -149.30163699927471 54.421070000634721  -149.31494635142374 54.439000989757375  -149.31463999987102 54.4390789983066  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808345  -149.32989800040346 54.459331000909685  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812439  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923406  -149.36120175949489 54.451355449144145  -149.36261185026936 54.450995849636058  -149.36490499953604 54.450411000841726  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437878  -149.37083185196542 54.436696798373909  -149.3709920036091 54.436656999508394  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939732 54.424838000026554  -149.38313800137598 54.42483799954266  -149.38313731809134 54.424837054271471  -149.38329700241911 54.424797998285683  -149.36856399823529 54.404405000296542  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465612  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878308 54.417372649008492  -149.301637000521 54.42106999913311","_id_":"ext-record-1705"},
{"footprint":"POLYGON ICRS -149.301637000521 54.421069999133117  -149.3016370012646 54.421070000135423  -149.30163699927471 54.421070000634728  -149.31494635142374 54.439000989757375  -149.31463999987102 54.439078998306606  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808352  -149.32989800040346 54.459331000909692  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812439  -149.33005799986529 54.459291999538777  -149.3301869205556 54.459259169923413  -149.36120175949492 54.451355449144152  -149.36261185026939 54.450995849636072  -149.36490499953604 54.45041100084174  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437885  -149.37083185196542 54.436696798373916  -149.3709920036091 54.436656999508408  -149.36547769317784 54.429155165976958  -149.38313800013259 54.424838001045117  -149.38313799939732 54.424838000026561  -149.38313800137598 54.424837999542667  -149.38313731809134 54.424837054271478  -149.38329700241911 54.424797998285683  -149.36856399823532 54.404405000296556  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465619  -149.33331399952371 54.413022000292592  -149.33337126911954 54.413101340303363  -149.31636692878305 54.4173726490085  -149.301637000521 54.421069999133117","_id_":"ext-record-1706"},
{"footprint":"POLYGON ICRS -149.301637000521 54.421069999133117  -149.3016370012646 54.421070000135423  -149.30163699927471 54.421070000634728  -149.31494635142374 54.439000989757375  -149.31463999987102 54.439078998306606  -149.31464000048743 54.439078999334015  -149.31463999862464 54.439078999808352  -149.32989800040346 54.459331000909692  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812447  -149.33005799986529 54.45929199953877  -149.3301869205556 54.459259169923406  -149.36120175949489 54.451355449144138  -149.36261185026939 54.450995849636058  -149.36490499953604 54.45041100084174  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437885  -149.37083185196542 54.436696798373916  -149.3709920036091 54.436656999508408  -149.36547769317784 54.429155165976958  -149.38313800013259 54.424838001045117  -149.38313799939732 54.424838000026561  -149.38313800137598 54.424837999542667  -149.38313731809134 54.424837054271478  -149.38329700241911 54.424797998285683  -149.36856399823532 54.404405000296535  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465619  -149.33331399952371 54.413022000292592  -149.33337126911954 54.413101340303363  -149.31636692878305 54.4173726490085  -149.301637000521 54.421069999133117","_id_":"ext-record-1707"},
{"footprint":"POLYGON ICRS -149.301637000521 54.421069999133117  -149.3016370012646 54.421070000135423  -149.30163699927471 54.421070000634728  -149.31494635142374 54.439000989757382  -149.314639999871 54.439078998306613  -149.3146400004874 54.439078999334022  -149.31463999862464 54.439078999808359  -149.32989800040346 54.459331000909692  -149.33002733086283 54.459298069829146  -149.33005690112503 54.459290540812439  -149.33005799986526 54.45929199953877  -149.33018692055558 54.459259169923413  -149.36120175949489 54.451355449144138  -149.36261185026936 54.450995849636065  -149.36490499953604 54.450411000841747  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437892  -149.37083185196539 54.436696798373923  -149.3709920036091 54.436656999508415  -149.36547769317784 54.429155165976965  -149.38313800013259 54.424838001045117  -149.38313799939732 54.424838000026561  -149.38313800137598 54.424837999542667  -149.38313731809131 54.424837054271485  -149.38329700241911 54.42479799828569  -149.36856399823529 54.404405000296542  -149.33347299920447 54.412982000365005  -149.33347371530698 54.412982993465626  -149.33331399952371 54.4130220002926  -149.33337126911954 54.413101340303371  -149.31636692878305 54.417372649008506  -149.301637000521 54.421069999133117","_id_":"ext-record-1708"},
{"footprint":"POLYGON ICRS -149.301637000521 54.421069999133117  -149.3016370012646 54.421070000135416  -149.30163699927471 54.421070000634728  -149.31494635142374 54.439000989757375  -149.314639999871 54.439078998306606  -149.3146400004874 54.439078999334015  -149.31463999862464 54.439078999808352  -149.32989800040346 54.459331000909692  -149.33002733086283 54.459298069829146  -149.33005690112506 54.459290540812439  -149.33005799986529 54.45929199953877  -149.3301869205556 54.459259169923406  -149.36120175949492 54.451355449144145  -149.36261185026939 54.450995849636058  -149.36490499953604 54.450411000841733  -149.35713828484816 54.440099201708378  -149.37083199978926 54.436696999437885  -149.37083185196542 54.436696798373916  -149.3709920036091 54.4366569995084  -149.36547769317784 54.429155165976958  -149.38313800013259 54.42483800104511  -149.38313799939732 54.424838000026561  -149.38313800137598 54.424837999542667  -149.38313731809134 54.424837054271478  -149.38329700241911 54.424797998285683  -149.36856399823532 54.404405000296542  -149.33347299920447 54.412982000365  -149.33347371530698 54.412982993465619  -149.33331399952371 54.413022000292585  -149.33337126911954 54.413101340303363  -149.31636692878305 54.4173726490085  -149.301637000521 54.421069999133117","_id_":"ext-record-1709"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597318  -149.31494634907889 54.439000989766861  -149.31464000038929 54.439078999412715  -149.3298979996402 54.459330999697265  -149.33002732873439 54.459298069961953  -149.36104223953092 54.45139545050548  -149.36245373010138 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424275  -149.37083199980216 54.436696999382015  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823922  -149.36840499831658 54.404445000128135  -149.33331400056605 54.413021999533505  -149.33337127044669 54.413101339475034  -149.30163700009291 54.421070000597318","_id_":"ext-record-1710"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597318  -149.31494634907889 54.439000989766861  -149.31464000038929 54.439078999412715  -149.3298979996402 54.459330999697265  -149.33002732873439 54.459298069961953  -149.36104223953092 54.45139545050548  -149.36245373010138 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424275  -149.37083199980216 54.436696999382015  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823922  -149.36840499831658 54.404445000128135  -149.33331400056605 54.413021999533505  -149.33337127044669 54.413101339475034  -149.30163700009291 54.421070000597318","_id_":"ext-record-1711"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597318  -149.31494634907889 54.439000989766868  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697272  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424282  -149.37083199980216 54.436696999382022  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128142  -149.33331400056605 54.413021999533512  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597318","_id_":"ext-record-1712"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597318  -149.31494634907889 54.439000989766868  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697272  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424282  -149.37083199980216 54.436696999382022  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128142  -149.33331400056605 54.413021999533512  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597318","_id_":"ext-record-1713"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597318  -149.31494634907889 54.439000989766868  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697265  -149.33002732873442 54.459298069961953  -149.36104223953092 54.45139545050548  -149.36245373010141 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424282  -149.37083199980216 54.436696999382015  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823922  -149.36840499831658 54.404445000128142  -149.33331400056605 54.413021999533505  -149.33337127044669 54.413101339475034  -149.30163700009291 54.421070000597318","_id_":"ext-record-1714"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597325  -149.31494634907889 54.439000989766868  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697272  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424282  -149.37083199980216 54.436696999382029  -149.36531712984743 54.429194400315957  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128142  -149.33331400056605 54.413021999533512  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597325","_id_":"ext-record-1715"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597325  -149.31494634907889 54.439000989766868  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697265  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241808  -149.36474600134471 54.450450999583261  -149.35697853101541 54.440138879424282  -149.37083199980216 54.436696999382022  -149.36531712984743 54.42919440031595  -149.38313800031636 54.424837999823922  -149.36840499831658 54.404445000128142  -149.33331400056605 54.413021999533505  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597325","_id_":"ext-record-1716"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597332  -149.31494634907889 54.439000989766875  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697279  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241815  -149.36474600134471 54.450450999583268  -149.35697853101541 54.440138879424289  -149.37083199980216 54.436696999382036  -149.36531712984743 54.429194400315964  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128149  -149.33331400056605 54.413021999533512  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597332","_id_":"ext-record-1717"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597332  -149.31494634907889 54.439000989766875  -149.31464000038929 54.439078999412722  -149.32989799964022 54.459330999697279  -149.33002732873442 54.45929806996196  -149.36104223953092 54.451395450505494  -149.36245373010141 54.451035539241822  -149.36474600134471 54.450450999583275  -149.35697853101541 54.440138879424289  -149.37083199980216 54.436696999382036  -149.36531712984743 54.429194400315964  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128149  -149.33331400056605 54.41302199953352  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597332","_id_":"ext-record-1718"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597339  -149.31494634907889 54.439000989766882  -149.31464000038929 54.439078999412729  -149.32989799964022 54.459330999697279  -149.33002732873442 54.459298069961967  -149.36104223953092 54.4513954505055  -149.36245373010141 54.451035539241822  -149.36474600134471 54.450450999583275  -149.35697853101541 54.440138879424296  -149.37083199980216 54.436696999382036  -149.36531712984743 54.429194400315964  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128156  -149.33331400056605 54.41302199953352  -149.33337127044669 54.413101339475048  -149.30163700009291 54.421070000597339","_id_":"ext-record-1719"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597332  -149.31494634907889 54.439000989766882  -149.31464000038929 54.439078999412722  -149.32989799964022 54.459330999697279  -149.33002732873442 54.45929806996196  -149.36104223953092 54.451395450505494  -149.36245373010141 54.451035539241822  -149.36474600134474 54.450450999583268  -149.35697853101544 54.440138879424296  -149.37083199980219 54.436696999382029  -149.36531712984743 54.429194400315964  -149.38313800031636 54.424837999823929  -149.36840499831661 54.404445000128156  -149.33331400056605 54.41302199953352  -149.33337127044672 54.413101339475041  -149.30163700009291 54.421070000597332","_id_":"ext-record-1720"},
{"footprint":"POLYGON ICRS -149.30163700009291 54.421070000597332  -149.31494634907889 54.439000989766875  -149.31464000038929 54.439078999412715  -149.32989799964022 54.459330999697279  -149.33002732873442 54.459298069961953  -149.36104223953092 54.451395450505487  -149.36245373010141 54.451035539241815  -149.36474600134471 54.450450999583268  -149.35697853101541 54.440138879424289  -149.37083199980216 54.436696999382036  -149.36531712984743 54.429194400315964  -149.38313800031636 54.424837999823929  -149.36840499831658 54.404445000128149  -149.33331400056605 54.413021999533512  -149.33337127044669 54.413101339475041  -149.30163700009291 54.421070000597332","_id_":"ext-record-1721"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482977  -149.31494635099313 54.439000990330427  -149.31463999974912 54.439078999288114  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939515 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051459 54.459259169917104  -149.36120175945391 54.451355449137857  -149.36261185022835 54.450995849629763  -149.36490499948005 54.450411000031139  -149.35713828369339 54.440099201049819  -149.3708319984986 54.436696999981415  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838462  -149.36547769250524 54.429155166265232  -149.3831379999865 54.424838000391183  -149.38313731720984 54.424837055822607  -149.38329700087269 54.424797998921825  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929038  -149.33347371741615 54.412982993084555  -149.33331400101332 54.413022000069162  -149.33337127092207 54.413101340003827  -149.30163700073234 54.421070000482977","_id_":"ext-record-1722"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482977  -149.31494635099313 54.43900099033042  -149.31463999974912 54.439078999288114  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244549  -149.33005799982428 54.459291999532461  -149.33018692051459 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.450995849629763  -149.36490499948005 54.450411000031139  -149.35713828369339 54.440099201049819  -149.3708319984986 54.436696999981407  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838462  -149.36547769250524 54.429155166265232  -149.3831379999865 54.424838000391176  -149.38313731720984 54.4248370558226  -149.38329700087269 54.424797998921825  -149.36856399819436 54.404405000290254  -149.33347300053666 54.412981999929031  -149.33347371741615 54.412982993084555  -149.33331400101332 54.413022000069162  -149.33337127092207 54.413101340003827  -149.30163700073234 54.421070000482977","_id_":"ext-record-1723"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939515 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051459 54.459259169917111  -149.36120175945391 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.37083199849863 54.436696999981422  -149.37083185082969 54.436696799128157  -149.3709920021611 54.436656999838469  -149.36547769250524 54.429155166265232  -149.38313799998653 54.42483800039119  -149.38313731720984 54.424837055822607  -149.38329700087272 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741618 54.412982993084562  -149.33331400101335 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482985","_id_":"ext-record-1724"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939515 54.459290541244556  -149.33005799982428 54.459291999532461  -149.33018692051459 54.459259169917104  -149.36120175945391 54.451355449137857  -149.36261185022835 54.450995849629763  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981422  -149.37083185082969 54.436696799128157  -149.37099200216107 54.436656999838469  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822607  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290254  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101335 54.413022000069169  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482985","_id_":"ext-record-1725"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939515 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051459 54.459259169917111  -149.36120175945391 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981422  -149.37083185082969 54.436696799128157  -149.37099200216107 54.436656999838469  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822607  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101335 54.413022000069169  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482985","_id_":"ext-record-1726"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939515 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051459 54.459259169917111  -149.36120175945391 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.37083199849863 54.436696999981422  -149.37083185082969 54.436696799128157  -149.3709920021611 54.436656999838469  -149.36547769250524 54.429155166265232  -149.38313799998653 54.42483800039119  -149.38313731720984 54.424837055822607  -149.38329700087272 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741618 54.412982993084562  -149.33331400101335 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482985","_id_":"ext-record-1727"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288114  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051459 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.450995849629763  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981422  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838469  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822614  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069169  -149.33337127092207 54.413101340003827  -149.30163700073234 54.421070000482985","_id_":"ext-record-1728"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482985  -149.31494635099313 54.439000990330427  -149.31463999974912 54.439078999288114  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051456 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031146  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981415  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838462  -149.36547769250524 54.429155166265232  -149.3831379999865 54.424838000391183  -149.38313731720984 54.424837055822607  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929038  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069169  -149.33337127092207 54.413101340003827  -149.30163700073234 54.421070000482985","_id_":"ext-record-1729"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482992  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051456 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031153  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981429  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838476  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822614  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482992","_id_":"ext-record-1730"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482992  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051456 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031153  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981429  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838476  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822614  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482992","_id_":"ext-record-1731"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482992  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532475  -149.33018692051459 54.459259169917111  -149.36120175945388 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031153  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981429  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838476  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822614  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290268  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482992","_id_":"ext-record-1732"},
{"footprint":"POLYGON ICRS -149.30163700073234 54.421070000482992  -149.31494635099313 54.439000990330435  -149.31463999974912 54.439078999288121  210.670102 54.459331  210.66997267 54.45929807  -149.33005689939512 54.459290541244556  -149.33005799982428 54.459291999532468  -149.33018692051456 54.459259169917104  -149.36120175945388 54.451355449137857  -149.36261185022835 54.45099584962977  -149.36490499948005 54.450411000031153  -149.35713828369339 54.440099201049826  -149.3708319984986 54.436696999981429  -149.37083185082966 54.436696799128157  -149.37099200216107 54.436656999838476  -149.36547769250524 54.429155166265232  -149.3831379999865 54.42483800039119  -149.38313731720984 54.424837055822614  -149.38329700087269 54.424797998921832  -149.36856399819436 54.404405000290261  -149.33347300053666 54.412981999929045  -149.33347371741615 54.412982993084562  -149.33331400101332 54.413022000069176  -149.33337127092207 54.413101340003834  -149.30163700073234 54.421070000482992","_id_":"ext-record-1733"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333851  -149.31510553972996 54.438961200708157  -149.31479999970023 54.439039000659143  -149.33005799985162 54.45929199953666  -149.33018692054193 54.4592591699213  -149.36120175948122 54.451355449142042  -149.36261185025572 54.450995849633955  -149.36490500041847 54.45041100056266  -149.35713798049545 54.440098800299346  -149.37099200081281 54.436657000363596  -149.36547737125917 54.429154729796117  -149.38329700049817 54.42479799941492  -149.36856399822165 54.404405000294446  -149.33347300093931 54.412982000390507  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333851","_id_":"ext-record-1734"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333851  -149.31510553972996 54.438961200708157  -149.31479999970023 54.439039000659143  -149.33005799985162 54.45929199953666  -149.33018692054193 54.4592591699213  -149.36120175948122 54.451355449142042  -149.36261185025572 54.450995849633955  -149.36490500041847 54.45041100056266  -149.35713798049545 54.440098800299346  -149.37099200081281 54.436657000363596  -149.36547737125917 54.429154729796117  -149.38329700049817 54.42479799941492  -149.36856399822165 54.404405000294446  -149.33347300093931 54.412982000390507  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333851","_id_":"ext-record-1735"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333858  -149.31510553972998 54.438961200708157  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054193 54.4592591699213  -149.36120175948122 54.451355449142049  -149.36261185025572 54.450995849633955  -149.36490500041847 54.450411000562667  -149.35713798049548 54.440098800299353  -149.37099200081283 54.436657000363596  -149.36547737125917 54.429154729796124  -149.3832970004982 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093931 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333858","_id_":"ext-record-1736"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333858  -149.31510553972996 54.438961200708157  -149.3147999997002 54.439039000659143  -149.33005799985162 54.459291999536667  -149.33018692054191 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025569 54.450995849633962  -149.36490500041845 54.45041100056266  -149.35713798049545 54.440098800299346  -149.37099200081281 54.436657000363596  -149.36547737125915 54.429154729796117  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093928 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333858","_id_":"ext-record-1737"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333858  -149.31510553972996 54.438961200708157  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536667  -149.33018692054191 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025569 54.450995849633962  -149.36490500041845 54.450411000562667  -149.35713798049545 54.440098800299346  -149.37099200081281 54.436657000363596  -149.36547737125915 54.429154729796117  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093931 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333858","_id_":"ext-record-1738"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333858  -149.31510553972996 54.438961200708157  -149.3147999997002 54.439039000659143  -149.33005799985162 54.459291999536667  -149.33018692054191 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025569 54.450995849633962  -149.36490500041845 54.45041100056266  -149.35713798049545 54.440098800299346  -149.37099200081281 54.436657000363596  -149.36547737125915 54.429154729796117  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093928 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333858","_id_":"ext-record-1739"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333865  -149.31510553972996 54.438961200708164  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054193 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025572 54.450995849633962  -149.36490500041847 54.450411000562667  -149.35713798049548 54.440098800299353  -149.37099200081281 54.436657000363596  -149.36547737125917 54.429154729796124  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093931 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333865","_id_":"ext-record-1740"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333865  -149.31510553972996 54.438961200708164  -149.3147999997002 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054191 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025569 54.450995849633962  -149.36490500041845 54.450411000562667  -149.35713798049545 54.440098800299353  -149.37099200081281 54.436657000363596  -149.36547737125915 54.429154729796124  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093928 54.412982000390521  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333865","_id_":"ext-record-1741"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333865  -149.31510553972996 54.438961200708164  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054191 54.459259169921317  -149.36120175948122 54.451355449142056  -149.36261185025569 54.450995849633969  -149.36490500041845 54.450411000562674  -149.35713798049545 54.44009880029936  -149.37099200081281 54.4366570003636  -149.36547737125915 54.429154729796124  -149.38329700049817 54.424797999414935  -149.36856399822165 54.40440500029446  -149.33347300093931 54.412982000390521  -149.3335304794314 54.413061630131622  -149.30179699959334 54.421030999333865","_id_":"ext-record-1742"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333865  -149.31510553972996 54.438961200708164  -149.3147999997002 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054191 54.45925916992131  -149.36120175948122 54.451355449142049  -149.36261185025569 54.450995849633962  -149.36490500041845 54.450411000562667  -149.35713798049545 54.440098800299353  -149.37099200081281 54.436657000363596  -149.36547737125915 54.429154729796124  -149.38329700049817 54.424797999414928  -149.36856399822165 54.404405000294453  -149.33347300093928 54.412982000390521  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333865","_id_":"ext-record-1743"},
{"footprint":"POLYGON ICRS -149.30179699959334 54.421030999333865  -149.31510553972998 54.438961200708157  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536674  -149.33018692054193 54.4592591699213  -149.36120175948122 54.451355449142042  -149.36261185025572 54.450995849633955  -149.36490500041847 54.450411000562667  -149.35713798049548 54.440098800299353  -149.37099200081283 54.436657000363596  -149.36547737125917 54.429154729796117  -149.3832970004982 54.424797999414928  -149.36856399822167 54.404405000294453  -149.33347300093931 54.412982000390514  -149.3335304794314 54.413061630131615  -149.30179699959334 54.421030999333865","_id_":"ext-record-1744"},
{"footprint":"POLYGON ICRS -149.30179699959336 54.421030999333858  -149.31510553972998 54.43896120070815  -149.31479999970023 54.439039000659143  -149.33005799985162 54.459291999536667  -149.33018692054193 54.4592591699213  -149.36120175948125 54.451355449142049  -149.36261185025572 54.450995849633955  -149.36490500041847 54.45041100056266  -149.35713798049548 54.440098800299346  -149.37099200081283 54.436657000363596  -149.36547737125917 54.429154729796117  -149.3832970004982 54.42479799941492  -149.36856399822167 54.404405000294453  -149.33347300093931 54.412982000390507  -149.33353047943143 54.413061630131615  -149.30179699959336 54.421030999333858","_id_":"ext-record-1745"},
{"footprint":"POLYGON ICRS -149.33046639988763 54.288251599936679  -149.33049866853534 54.288266107118247  -149.33047329965862 54.288281899748988  210.6579137 54.2935016  -149.3421967821713 54.293432809249886  210.6576913 54.2934831  -149.34241918155354 54.293414309008853  -149.34253110008154 54.293464600004782  -149.34255666999667 54.293448678763454  210.6574168 54.2934606  -149.35381419982306 54.286465800036027  210.6577984 54.2812473  -149.34220152978293 54.281247343683646  210.6578573 54.2812209  -149.34203226465814 54.28128968267562  210.6580796 54.2812394  -149.34180996355443 54.281308182605812  210.6583019 54.2812579  -149.33046639988763 54.288251599936679","_id_":"ext-record-1746"},
{"footprint":"POLYGON ICRS -149.33046639988763 54.288251599936679  -149.33049866853534 54.288266107118247  -149.33047329965862 54.288281899748988  210.6579137 54.2935016  -149.3421967821713 54.293432809249886  210.6576913 54.2934831  -149.34241918155354 54.293414309008853  -149.34253110008154 54.293464600004782  -149.34255666999667 54.293448678763454  210.6574168 54.2934606  -149.35381419982306 54.286465800036027  210.6577984 54.2812473  -149.34220152978293 54.281247343683646  210.6578573 54.2812209  -149.34203226465814 54.28128968267562  210.6580796 54.2812394  -149.34180996355443 54.281308182605812  210.6583019 54.2812579  -149.33046639988763 54.288251599936679","_id_":"ext-record-1747"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.45225200001137  -149.18776700012293 54.452252000183762  -149.19372373949906 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.46157997110911  -149.19448100025596 54.461579999962794  -149.19493205568469 54.4614734095797  -149.21044200016297 54.457808000061043  -149.21044130183995 54.457807002658896  -149.21060199960598 54.45776899987051  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101428  -149.20388400000181 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095829 54.452212999930332  -149.18792856151967 54.452213801660051  -149.18776700038225 54.45225200001137","_id_":"ext-record-1748"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011363  -149.18776700012293 54.452252000183762  -149.19372373949906 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579689  -149.21044200016297 54.457808000061036  -149.21044130183998 54.457807002658889  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101428  -149.20388400000181 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095829 54.452212999930332  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011363","_id_":"ext-record-1749"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011356  -149.18776700012293 54.452252000183762  -149.19372373949906 54.46076597520851  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579682  -149.21044200016297 54.457808000061036  -149.21044130183995 54.457807002658882  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095827 54.452212999930325  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011356","_id_":"ext-record-1750"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.45225200001137  -149.18776700012293 54.452252000183762  -149.19372373949903 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.46157997110911  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579696  -149.21044200016294 54.457808000061043  -149.21044130183995 54.457807002658889  -149.21060199960598 54.4577689998705  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095827 54.452212999930332  -149.18792856151967 54.452213801660051  -149.18776700038225 54.45225200001137","_id_":"ext-record-1751"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011363  -149.18776700012293 54.452252000183762  -149.19372373949906 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579689  -149.21044200016297 54.457808000061036  -149.21044130183998 54.457807002658889  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101428  -149.20388400000181 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095829 54.452212999930332  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011363","_id_":"ext-record-1752"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011363  -149.18776700012293 54.452252000183762  -149.19372373949906 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579689  -149.21044200016297 54.457808000061036  -149.21044130183998 54.457807002658889  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101428  -149.20388400000181 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095829 54.452212999930332  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011363","_id_":"ext-record-1753"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.45225200001137  -149.18776700012293 54.452252000183762  -149.19372373949903 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.46157997110911  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579696  -149.21044200016294 54.457808000061043  -149.21044130183995 54.457807002658889  -149.21060199960598 54.4577689998705  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095827 54.452212999930332  -149.18792856151967 54.452213801660051  -149.18776700038225 54.45225200001137","_id_":"ext-record-1754"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.45225200001137  -149.18776700012293 54.452252000183762  -149.19372373949903 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.46157997110911  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579696  -149.21044200016294 54.457808000061043  -149.21044130183995 54.457807002658889  -149.21060199960598 54.4577689998705  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095827 54.452212999930332  -149.18792856151967 54.452213801660051  -149.18776700038225 54.45225200001137","_id_":"ext-record-1755"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011356  -149.18776700012293 54.452252000183762  -149.19372373949906 54.46076597520851  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579682  -149.21044200016297 54.457808000061036  -149.21044130183995 54.457807002658882  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095827 54.452212999930325  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011356","_id_":"ext-record-1756"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011356  -149.18776700012293 54.452252000183762  -149.19372373949906 54.46076597520851  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579682  -149.21044200016297 54.457808000061036  -149.21044130183995 54.457807002658882  -149.21060199960598 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358712  -149.18792800095827 54.452212999930325  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011356","_id_":"ext-record-1757"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011363  -149.18776700012293 54.452252000183755  -149.19372373949903 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579689  -149.21044200016294 54.457808000061043  -149.21044130183995 54.457807002658889  -149.21060199960596 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095827 54.452212999930332  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011363","_id_":"ext-record-1758"},
{"footprint":"POLYGON ICRS -149.18776700038225 54.452252000011363  -149.18776700012293 54.452252000183755  -149.19372373949903 54.460765975208517  210.80568 54.461618  -149.19448098012489 54.4615799711091  -149.19448100025596 54.461579999962794  -149.19493205568469 54.461473409579689  -149.21044200016294 54.457808000061043  -149.21044130183995 54.457807002658889  -149.21060199960596 54.457768999870495  -149.204044999789 54.448401999947961  -149.20388405065921 54.4484400719025  -149.20388400029887 54.448440000101421  -149.20388400000178 54.448440000261094  210.796116 54.44844  -149.20256827267221 54.448751303358719  -149.18792800095827 54.452212999930332  -149.18792856151967 54.452213801660037  -149.18776700038225 54.452252000011363","_id_":"ext-record-1759"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1760"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1761"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1762"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1763"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1764"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1765"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1766"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1767"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1768"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1769"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1770"},
{"footprint":"POLYGON ICRS -149.18792800017258 54.452213000253337  -149.19448100025596 54.461579999962794  -149.2106019997824 54.457768999835928  -149.204044999789 54.448401999947961  -149.18792800017258 54.452213000253337","_id_":"ext-record-1771"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1772"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1773"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1774"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1775"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955087  -149.2104413019949 54.4578070027101  -149.21060199969565 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033228  -149.18792800050181 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1776"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955087  -149.2104413019949 54.4578070027101  -149.21060199969565 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033228  -149.18792800050181 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1777"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955087  -149.2104413019949 54.4578070027101  -149.21060199969565 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033228  -149.18792800050181 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1778"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1779"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1780"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1781"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955094  -149.2104413019949 54.457807002710105  -149.21060199969568 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033235  -149.18792800050184 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1782"},
{"footprint":"POLYGON ICRS -149.18776700026945 54.452251999990438  210.80568 54.461618  -149.19448097967626 54.461579970892032  -149.19448100025596 54.461579999962794  -149.19493598438186 54.461472481143268  -149.21044200028197 54.457807999955087  -149.2104413019949 54.4578070027101  -149.21060199969565 54.45776899972293  -149.204044999789 54.448401999947968  -149.20388405065921 54.4484400719025  210.796116 54.44844  -149.20257074306724 54.448750719033228  -149.18792800050181 54.452213000242622  -149.18792856106492 54.452213801722181  -149.18776700026945 54.452251999990438","_id_":"ext-record-1783"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.452251999986366  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1784"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.452251999986366  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1785"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009258 54.452251999986373  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989784 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1786"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009258 54.452251999986373  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989784 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1787"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.452251999986366  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1788"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009258 54.452251999986373  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989784 54.448440000114381  -149.20388399973851 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1789"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.452251999986373  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973854 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1790"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.452251999986373  -149.18776699959886 54.4522520001031  -149.19431999973924 54.461617999836491  -149.21044199984064 54.457808000147679  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990931  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973854 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1791"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.45225199998638  -149.18776699959886 54.452252000103108  -149.19431999973924 54.4616179998365  -149.21044199984064 54.457808000147686  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990938  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973854 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1792"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.45225199998638  -149.18776699959886 54.452252000103108  -149.19431999973924 54.4616179998365  -149.21044199984064 54.457808000147693  -149.21044199965974 54.457807999889312  -149.21044200015353 54.457807999772569  -149.20698992591403 54.452877344990938  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973854 54.448439999896976  -149.1877669999119 54.452251999728055","_id_":"ext-record-1793"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.45225199998638  -149.18776699959886 54.452252000103108  -149.19431999973924 54.4616179998365  -149.21044199984064 54.457808000147686  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990938  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973851 54.448439999896983  -149.1877669999119 54.452251999728055","_id_":"ext-record-1794"},
{"footprint":"POLYGON ICRS -149.1877669999119 54.452251999728055  -149.18776700009261 54.45225199998638  -149.18776699959886 54.452252000103108  -149.19431999973924 54.4616179998365  -149.21044199984064 54.457808000147686  -149.21044199965974 54.457807999889305  -149.21044200015353 54.457807999772562  -149.20698992591403 54.452877344990938  210.796116 54.44844  -149.20388399989787 54.448440000114381  -149.20388399973851 54.448439999896983  -149.1877669999119 54.452251999728055","_id_":"ext-record-1795"},
{"footprint":"POLYGON ICRS -148.999233996973 54.357172997665835  -149.01431914014745 54.361711779746528  -149.01465899908561 54.361814001046817  -149.02261199689636 54.352825997530438  -149.00718899517054 54.348187001226478  -148.999233996973 54.357172997665835","_id_":"ext-record-1796"},
{"footprint":"POLYGON ICRS -148.999233996973 54.357172997665835  -149.01431914014745 54.361711779746528  -149.01465899908561 54.361814001046817  -149.02261199689636 54.352825997530438  -149.00718899517054 54.348187001226478  -148.999233996973 54.357172997665835","_id_":"ext-record-1797"},
{"footprint":"POLYGON ICRS -149.2345329998009 54.242792999869138  -149.24643700007348 54.25013900007729  -149.25900500032529 54.243187000059812  -149.24710099980038 54.235837999993784  -149.2345329998009 54.242792999869138","_id_":"ext-record-1798"},
{"footprint":"POLYGON ICRS -149.2345329998009 54.242792999869131  -149.24643700007351 54.25013900007729  -149.25900500032529 54.2431870000598  -149.24710099980038 54.23583799999377  -149.2345329998009 54.242792999869131","_id_":"ext-record-1799"},
{"footprint":"POLYGON ICRS -149.2345329998009 54.242792999869124  -149.24643700007351 54.250139000077283  -149.25900500032529 54.2431870000598  -149.24710099980038 54.23583799999377  -149.2345329998009 54.242792999869124","_id_":"ext-record-1800"},
{"footprint":"POLYGON ICRS -149.36850399665119 54.279734000446219  -149.37034098903456 54.30184898662889  -149.37048671430824 54.301844900154833  -149.37249097395275 54.321916002223389  -149.40590288327235 54.320780448720285  -149.40592398918969 54.320974002017145  -149.44374199243472 54.319567984073  -149.44373280451583 54.319483512071749  -149.44159038480933 54.2998274006927  -149.44149315514065 54.29893493775679  -149.44133499522013 54.297482987936149  -149.42207827984589 54.29819837359593  -149.4212650031875 54.289448007290318  -149.40729952191751 54.289893637676144  -149.40636900558417 54.278662992242126  -149.36850399665119 54.279734000446219","_id_":"ext-record-1801"},
{"footprint":"POLYGON ICRS -149.36850399665119 54.279734000446219  -149.37034098903456 54.30184898662889  -149.37048671430824 54.301844900154833  -149.37249097395275 54.321916002223389  -149.40590288327235 54.320780448720285  -149.40592398918969 54.320974002017145  -149.44374199243472 54.319567984073  -149.44373280451583 54.319483512071749  -149.44159038480933 54.2998274006927  -149.44149315514065 54.29893493775679  -149.44133499522013 54.297482987936149  -149.42207827984589 54.29819837359593  -149.4212650031875 54.289448007290318  -149.40729952191751 54.289893637676144  -149.40636900558417 54.278662992242126  -149.36850399665119 54.279734000446219","_id_":"ext-record-1802"},
{"footprint":"POLYGON ICRS -149.36850399665121 54.279734000446233  -149.37034098903456 54.30184898662889  -149.37048671430827 54.30184490015484  -149.37249097395278 54.3219160022234  -149.40590288327235 54.3207804487203  -149.40592398918972 54.320974002017152  -149.44374199243472 54.319567984073011  -149.44373280451586 54.319483512071756  -149.44159038480936 54.299827400692713  -149.44149315514068 54.2989349377568  -149.44133499522016 54.297482987936156  -149.42207827984589 54.298198373595937  -149.42126500318753 54.289448007290318  -149.40729952191751 54.289893637676151  -149.4063690055842 54.278662992242133  -149.36850399665121 54.279734000446233","_id_":"ext-record-1803"},
{"footprint":"POLYGON ICRS -149.36850399665121 54.279734000446233  -149.37034098903456 54.30184898662889  -149.37048671430827 54.30184490015484  -149.37249097395278 54.3219160022234  -149.40590288327235 54.3207804487203  -149.40592398918972 54.320974002017152  -149.44374199243472 54.319567984073011  -149.44373280451586 54.319483512071756  -149.44159038480936 54.299827400692713  -149.44149315514068 54.2989349377568  -149.44133499522016 54.297482987936156  -149.42207827984589 54.298198373595937  -149.42126500318753 54.289448007290318  -149.40729952191751 54.289893637676151  -149.4063690055842 54.278662992242133  -149.36850399665121 54.279734000446233","_id_":"ext-record-1804"},
{"footprint":"POLYGON ICRS -149.3685059984083 54.279735000079391  -149.37034299949863 54.301850000366677  -149.37048869913542 54.301845910631009  -149.37249299956204 54.32191700046922  -149.4059048992045 54.320781439368474  -149.40592600059293 54.320975000940493  -149.44374400067153 54.319569000380064  -149.44373478963362 54.319484510688255  210.55840761 54.2998284  -149.44149516059557 54.298935929401033  -149.4413370004404 54.297483999520459  -149.42208012094338 54.298199390658056  -149.42126599833432 54.289449000141076  -149.40730152034052 54.289894610418109  -149.40637099948907 54.278663999263877  -149.3685059984083 54.279735000079391","_id_":"ext-record-1805"},
{"footprint":"POLYGON ICRS -149.3685059984083 54.279735000079391  -149.37034299949863 54.301850000366677  -149.37048869913542 54.301845910631009  -149.37249299956204 54.32191700046922  -149.4059048992045 54.320781439368474  -149.40592600059293 54.320975000940493  -149.44374400067153 54.319569000380064  -149.44373478963362 54.319484510688255  210.55840761 54.2998284  -149.44149516059557 54.298935929401033  -149.4413370004404 54.297483999520459  -149.42208012094338 54.298199390658056  -149.42126599833432 54.289449000141076  -149.40730152034052 54.289894610418109  -149.40637099948907 54.278663999263877  -149.3685059984083 54.279735000079391","_id_":"ext-record-1806"},
{"footprint":"POLYGON ICRS -149.36978300028542 54.280201000989436  -149.37161999908687 54.302314999464706  210.62823339 54.30231088  -149.37377100074093 54.322382000125415  -149.40718387929206 54.321246410459729  -149.40720500038842 54.321440000696285  -149.44502200074552 54.320034000980307  -149.44501279127579 54.319949509981356  -149.44363937076923 54.30735083525397  210.55712961 54.30029342  -149.4427731609091 54.2994009297088  -149.44261499959219 54.297948999871537  -149.42335810998861 54.298664389869309  210.577456 54.289915  210.59142093 54.29036063  -149.40764799864388 54.279130000493709  -149.36978300028542 54.280201000989436","_id_":"ext-record-1807"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.280201000027667  -149.37161999942211 54.302315000215984  -149.3717666098533 54.302310879125464  -149.37377100108242 54.322381999503961  -149.40718387924531 54.321246410398594  -149.40720500155538 54.321439999957072  -149.44502200110534 54.320034000300183  -149.44501279122898 54.319949509920235  -149.44287038967207 54.300293420431913  -149.44277315969404 54.29940092976134  -149.44261499993942 54.297948999569918  -149.42335810940816 54.298664390468083  -149.42254400089629 54.289915000538805  -149.40857906822657 54.290360630289811  -149.40764799985564 54.279130000713067  -149.36978299975371 54.280201000027667","_id_":"ext-record-1808"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.280201000027667  -149.37161999942211 54.302315000215984  -149.3717666098533 54.302310879125471  -149.37377100108242 54.322381999503961  -149.40718387924531 54.321246410398594  -149.40720500155541 54.321439999957072  -149.44502200110534 54.320034000300183  -149.44501279122898 54.319949509920235  -149.44287038967207 54.30029342043192  -149.44277315969404 54.29940092976134  -149.44261499993945 54.297948999569918  -149.42335810940816 54.298664390468083  -149.42254400089629 54.289915000538805  -149.40857906822657 54.290360630289811  -149.40764799985564 54.279130000713067  -149.36978299975371 54.280201000027667","_id_":"ext-record-1809"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.280201000027667  -149.37161999942211 54.302315000215984  -149.3717666098533 54.302310879125464  -149.37377100108242 54.322381999503961  -149.40718387924531 54.321246410398594  -149.40720500155538 54.321439999957072  -149.44502200110534 54.320034000300183  -149.44501279122898 54.319949509920235  -149.44287038967207 54.300293420431913  -149.44277315969404 54.29940092976134  -149.44261499993942 54.297948999569918  -149.42335810940816 54.298664390468083  -149.42254400089629 54.289915000538805  -149.40857906822657 54.290360630289811  -149.40764799985564 54.279130000713067  -149.36978299975371 54.280201000027667","_id_":"ext-record-1810"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.280201000027667  -149.37161999942211 54.302315000215984  -149.3717666098533 54.302310879125471  -149.37377100108242 54.322381999503961  -149.40718387924531 54.321246410398594  -149.40720500155541 54.321439999957072  -149.44502200110534 54.320034000300183  -149.44501279122898 54.319949509920235  -149.44287038967207 54.30029342043192  -149.44277315969404 54.29940092976134  -149.44261499993945 54.297948999569918  -149.42335810940816 54.298664390468083  -149.42254400089629 54.289915000538805  -149.40857906822657 54.290360630289811  -149.40764799985564 54.279130000713067  -149.36978299975371 54.280201000027667","_id_":"ext-record-1811"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.28020100002766  -149.37161999942211 54.302315000215977  -149.37176660985332 54.302310879125457  -149.37377100108245 54.322381999503961  -149.40718387924531 54.321246410398587  -149.40720500155541 54.321439999957065  -149.44502200110534 54.320034000300176  -149.44501279122898 54.319949509920235  -149.44287038967209 54.300293420431913  -149.44277315969404 54.299400929761333  -149.44261499993945 54.297948999569911  -149.42335810940816 54.298664390468076  -149.42254400089629 54.2899150005388  -149.4085790682266 54.290360630289811  -149.40764799985564 54.27913000071306  -149.36978299975371 54.28020100002766","_id_":"ext-record-1812"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.28020100002766  -149.37161999942211 54.302315000215977  -149.37176660985332 54.302310879125457  -149.37377100108245 54.322381999503961  -149.40718387924531 54.321246410398587  -149.40720500155541 54.321439999957065  -149.44502200110534 54.320034000300176  -149.44501279122898 54.319949509920235  -149.44287038967209 54.300293420431913  -149.44277315969404 54.299400929761333  -149.44261499993945 54.297948999569911  -149.42335810940816 54.298664390468076  -149.42254400089629 54.2899150005388  -149.4085790682266 54.290360630289811  -149.40764799985564 54.27913000071306  -149.36978299975371 54.28020100002766","_id_":"ext-record-1813"},
{"footprint":"POLYGON ICRS -149.36978299975371 54.28020100002766  -149.37161999942211 54.302315000215977  -149.37176660985332 54.302310879125457  -149.37377100108245 54.322381999503961  -149.40718387924531 54.321246410398587  -149.40720500155541 54.321439999957065  -149.44502200110534 54.320034000300176  -149.44501279122898 54.319949509920235  -149.44287038967209 54.300293420431913  -149.44277315969404 54.299400929761333  -149.44261499993945 54.297948999569911  -149.42335810940816 54.298664390468076  -149.42254400089629 54.2899150005388  -149.4085790682266 54.290360630289811  -149.40764799985564 54.27913000071306  -149.36978299975371 54.28020100002766","_id_":"ext-record-1814"},
{"footprint":"POLYGON ICRS -149.36978299978483 54.280201000068423  -149.37161999945323 54.302315000256741  -149.37176660988445 54.302310879166221  -149.37377100111357 54.322381999544717  -149.40718387927649 54.321246410439343  -149.40720500158656 54.321439999997814  -149.44502200113655 54.320034000340932  -149.44501279126015 54.319949509960971  -149.44287038970327 54.300293420472663  -149.44277315972522 54.299400929802083  -149.44261499997063 54.297948999610654  -149.42335810943936 54.298664390508826  -149.42254400092747 54.289915000579548  -149.40857906825775 54.290360630330561  -149.40764799988679 54.279130000753817  -149.36978299978483 54.280201000068423","_id_":"ext-record-1815"},
{"footprint":"POLYGON ICRS -149.3697829997848 54.280201000068431  -149.37161999945323 54.302315000256748  -149.37176660988442 54.302310879166221  -149.37377100111357 54.322381999544717  -149.40718387927649 54.32124641043935  -149.40720500158656 54.321439999997828  -149.44502200113652 54.320034000340932  -149.44501279126015 54.319949509960978  -149.44287038970324 54.300293420472663  -149.44277315972522 54.29940092980209  -149.44261499997063 54.297948999610661  -149.42335810943933 54.298664390508833  -149.42254400092747 54.289915000579555  -149.40857906825775 54.290360630330561  -149.40764799988679 54.279130000753817  -149.3697829997848 54.280201000068431","_id_":"ext-record-1816"},
{"footprint":"POLYGON ICRS -149.31972599584469 54.428422099514258  210.6883627 54.484953  -149.36090580157207 54.48535089984864  -149.36091090115562 54.485314987960166  -149.36162380247009 54.485320598721515  -149.36162893122668 54.485284493382864  -149.36234180385071 54.485290099413639  -149.36237647018947 54.485046031973056  -149.40921159918594 54.4851976995419  -149.40921697649318 54.485164930183451  -149.40992970221345 54.4851670997555  -149.40993510852 54.485134136933546  -149.41064750115117 54.485136300014162  -149.41979119957378 54.429324100614949  -149.37222589906258 54.429173799326406  -149.37222561785322 54.429175502956369  -149.37222459850449 54.429175500509096  -149.37222433521606 54.429177101807845  -149.37222359921134 54.4291770991686  -149.37221872412812 54.429206682211543  -149.37150899749352 54.42920430255981  -149.37150873417698 54.42920590385701  -149.3715076994265 54.429205900789846  -149.37150741943114 54.429207602915838  -149.37150660010602 54.429207600986132  -149.37150172335168 54.429237189230825  -149.37079189990271 54.429234798743288  -149.37079162121589 54.42923650309362  -149.3707906980041 54.429236499811111  -149.37079043380976 54.429238102868887  -149.37078959868845 54.429238100060537  -149.3672557224308 54.450664026278417  -149.3703605015578 54.428755799890389  -149.32115999953751 54.428361702212463  -149.32115486286008 54.428397650540482  -149.32044289636207 54.428391799402092  -149.32043773318824 54.428427951517378  -149.31972599584469 54.428422099514258","_id_":"ext-record-1817"},
{"footprint":"POLYGON ICRS -149.31972599963481 54.428422099753064  210.6883627 54.484953  -149.36090579876202 54.485350899621395  -149.36091090072415 54.485314988092306  -149.36162380136813 54.485320599797362  -149.36162892737127 54.485284493121476  -149.36234180312991 54.485290099566463  -149.36237646956604 54.485046031070766  -149.40921159982696 54.485197699541786  -149.40921697695913 54.485164930215696  -149.40992969937798 54.485167099615353  -149.40993510788869 54.48513413691105  -149.41064750127 54.485136299670316  -149.41979119957378 54.429324100614949  -149.37222590251594 54.429173799061473  -149.3722204820763 54.42920668913272  -149.37150899932752 54.429204300980714  -149.37150357883675 54.429237193571524  -149.3707919002114 54.429234799573912  -149.36724025196159 54.450773148496438  -149.3703605015578 54.428755799890389  -149.32116000008992 54.428361701408093  -149.32115486358688 54.428397650340614  -149.32044289920819 54.428391800230294  -149.32043773431997 54.428427952052296  -149.31972599963481 54.428422099753064","_id_":"ext-record-1818"},
{"footprint":"POLYGON ICRS -149.319726399572 54.4284228992231  210.6883623 54.4849519  -149.36090540130158 54.48535000002088  -149.36091050360335 54.485314087745472  -149.36162340006007 54.485319700977549  -149.36162852989563 54.485283593523469  -149.36234130036425 54.485289200115538  -149.36237603943616 54.485044636008936  -149.40920989931072 54.485196599064537  -149.4092152767119 54.4851638266927  -149.40992799759925 54.485166000650985  -149.40993340850233 54.485133031213145  -149.41064580071773 54.485135199213474  -149.41978860032907 54.429326099683465  -149.37222459924686 54.429175499722746  -149.37221919692229 54.429208294919746  -149.37150769989469 54.429205900915285  -149.37150226244893 54.429238897482932  -149.37079069942848 54.429236499343773  -149.36724276224962 54.450753029256461  -149.37036019922868 54.428756800440269  -149.32116039989143 54.428362600813941  -149.32115526472415 54.428398551973061  -149.32044340029475 54.42839270030705  -149.32043824891932 54.428428755433728  -149.319726399572 54.4284228992231","_id_":"ext-record-1819"},
{"footprint":"POLYGON ICRS -149.31972820106728 54.428424500428122  210.6883614 54.4849507  -149.36090360078092 54.485348500672913  -149.36090871772694 54.485312491083107  -149.36162160082478 54.485318100194192  -149.36162671526807 54.485282095793345  -149.362339598649 54.4852877006915  -149.3623743430949 54.485043132164137  -149.4092048986021 54.48519460044178  -149.4092102764547 54.485161833591341  -149.40992290307867 54.485163999901083  -149.40992829498038 54.485131138030731  -149.41064080000078 54.48513330058325  -149.41978550071158 54.429327199706485  -149.3722235988626 54.429177100745548  -149.37221817832693 54.429209987088406  -149.37150660196505 54.429207600471571  -149.37150118057954 54.42924049125638  -149.37078960026656 54.429238100459017  -149.36724448359465 54.450732139189206  -149.37035920091822 54.4287580997194  -149.32116220026688 54.4283640998237  -149.32115705069489 54.4284001450725  -149.3204452004808 54.428394299271289  -149.32044004879918 54.428430351121023  -149.31972820106728 54.428424500428122","_id_":"ext-record-1820"},
{"footprint":"POLYGON ICRS -149.37150629937975 54.429207499681475  -149.36229699908304 54.485009499754085  -149.4099233997697 54.485163699135725  -149.41906789921353 54.429358099715166  -149.37150629937975 54.429207499681475POLYGON ICRS  -149.32044459899876 54.428394799573347  -149.31235740082312 54.4849200003922  -149.36162220141708 54.485317599685295  -149.36964150081886 54.428789100028361  -149.32044459899876 54.428394799573347","_id_":"ext-record-1821"},
{"footprint":"POLYGON ICRS -149.16311299869702 54.458555999808382  -149.15069435031694 54.476704089945478  -149.15038199944982 54.476632999333  -149.14894404975442 54.478768879114725  -149.1365119992814 54.497224999257654  -149.17198599966494 54.505290999249169  -149.17905284106715 54.4948076105513  -149.19304399893861 54.498075999955049  -149.19827958889255 54.490504160389861  -149.216195001089 54.494758999571431  -149.2306070001064 54.474283999971668  -149.19532999957954 54.465904000776355  -149.19527375054264 54.465983940528879  -149.16311299869702 54.458555999808382","_id_":"ext-record-1822"},
{"footprint":"POLYGON ICRS -149.16311299869702 54.458555999808382  -149.15069435031694 54.476704089945478  -149.15038199944982 54.476632999333  -149.14894404975442 54.478768879114725  -149.1365119992814 54.497224999257654  -149.17198599966494 54.505290999249169  -149.17905284106715 54.4948076105513  -149.19304399893861 54.498075999955049  -149.19827958889255 54.490504160389861  -149.216195001089 54.494758999571431  -149.2306070001064 54.474283999971668  -149.19532999957954 54.465904000776355  -149.19527375054264 54.465983940528879  -149.16311299869702 54.458555999808382","_id_":"ext-record-1823"},
{"footprint":"POLYGON ICRS -148.93155599803606 54.313351998835067  -148.93208372877925 54.313513070886785  -148.93141500157773 54.314258999547228  211.034956 54.324517  211.03488821 54.32444141  211.004195 54.333581  210.98897679 54.31618683  210.98868 54.316274  -149.02836700016914 54.296525000853705  210.97175962 54.29648781  -149.0278529311328 54.296374017970777  -149.02850599816512 54.295617000004533  -149.02837955886272 54.29557986995836  -148.9982307807785 54.286719410937167  -148.99686019126335 54.286316330115909  -148.99463200107306 54.285660999818774  -148.98595104888315 54.29571710983  -148.97260200020935 54.291701999243159  -148.96619724010156 54.298959349688538  -148.94912099957762 54.293747999864507  -148.93155599803606 54.313351998835067","_id_":"ext-record-1824"},
{"footprint":"POLYGON ICRS -148.93155599803606 54.313351998835067  -148.93208372877925 54.313513070886785  -148.93141500157773 54.314258999547228  211.034956 54.324517  211.03488821 54.32444141  211.004195 54.333581  210.98897679 54.31618683  210.98868 54.316274  -149.02836700016914 54.296525000853705  210.97175962 54.29648781  -149.0278529311328 54.296374017970777  -149.02850599816512 54.295617000004533  -149.02837955886272 54.29557986995836  -148.9982307807785 54.286719410937167  -148.99686019126335 54.286316330115909  -148.99463200107306 54.285660999818774  -148.98595104888315 54.29571710983  -148.97260200020935 54.291701999243159  -148.96619724010156 54.298959349688538  -148.94912099957762 54.293747999864507  -148.93155599803606 54.313351998835067","_id_":"ext-record-1825"},
{"footprint":"POLYGON ICRS -149.34575700000411 54.423647000677526  -149.36838982930888 54.4380706597355  -149.36814500073109 54.438201999706656  -149.39392299892828 54.454451999597964  -149.394030939201 54.454394089691391  -149.41891178034555 54.441038650428766  -149.42004127949482 54.440432039885707  -149.42187899978688 54.43944500056395  -149.40875392959484 54.431170860479455  -149.41991900121724 54.425288000316044  -149.41049933997971 54.419242769587584  -149.42489899853345 54.411743000199095  -149.3995389993398 54.395261000414571  -149.37118299817445 54.410028000412744  -149.37128089965469 54.410091670577984  -149.34575700000411 54.423647000677526","_id_":"ext-record-1826"},
{"footprint":"POLYGON ICRS -149.34575700000411 54.423647000677526  -149.36838982930888 54.4380706597355  -149.36814500073109 54.438201999706656  -149.39392299892828 54.454451999597964  -149.394030939201 54.454394089691391  -149.41891178034555 54.441038650428766  -149.42004127949482 54.440432039885707  -149.42187899978688 54.43944500056395  -149.40875392959484 54.431170860479455  -149.41991900121724 54.425288000316044  -149.41049933997971 54.419242769587584  -149.42489899853345 54.411743000199095  -149.3995389993398 54.395261000414571  -149.37118299817445 54.410028000412744  -149.37128089965469 54.410091670577984  -149.34575700000411 54.423647000677526","_id_":"ext-record-1827"},
{"footprint":"POLYGON ICRS -149.34575700000411 54.423647000677526  -149.36838982930888 54.4380706597355  -149.36814500073109 54.438201999706656  -149.39392299892828 54.454451999597964  -149.394030939201 54.454394089691391  -149.41891178034555 54.441038650428766  -149.42004127949482 54.440432039885707  -149.42187899978688 54.43944500056395  -149.40875392959484 54.431170860479455  -149.41991900121724 54.425288000316044  -149.41049933997971 54.419242769587584  -149.42489899853345 54.411743000199095  -149.3995389993398 54.395261000414571  -149.37118299817445 54.410028000412744  -149.37128089965469 54.410091670577984  -149.34575700000411 54.423647000677526","_id_":"ext-record-1828"},
{"footprint":"POLYGON ICRS -149.02583899829847 54.471024000195364  -149.05540800059754 54.484985000554815  -149.05550025091105 54.484919060085033  -149.08256399956082 54.497443999528329  -149.10350034021118 54.482166829935821  -149.1037629992444 54.482286999318738  -149.12730499971272 54.464915000020468  -149.12719005102849 54.464862409583382  -149.10056600098713 54.452675230266827  -149.09935593836352 54.452121020115243  -149.09739099924579 54.451221000138446  -149.08540247118515 54.460066150163193  -149.07364500051366 54.454580000297909  -149.0648555613806 54.460947030117531  -149.04984200027789 54.453855000228941  -149.02583899829847 54.471024000195364","_id_":"ext-record-1829"},
{"footprint":"POLYGON ICRS -149.02583899837447 54.471024000222918  -149.05540800067357 54.484985000582355  -149.05550025098708 54.484919060112567  -149.08256399963687 54.497443999555848  -149.10350034028724 54.482166829963326  -149.10376299932045 54.482286999346243  -149.12730499978875 54.464915000047952  -149.12719005110452 54.464862409610866  -149.10056600106313 54.452675230294339  -149.09935593843952 54.452121020142748  -149.09739099932179 54.451221000165958  -149.08540247126115 54.460066150190706  -149.07364500058964 54.454580000325436  -149.0648555614566 54.460947030145064  -149.04984200035386 54.453855000256475  -149.02583899837447 54.471024000222918","_id_":"ext-record-1830"},
{"footprint":"POLYGON ICRS -149.02583899837447 54.471024000222918  -149.05540800067357 54.484985000582355  -149.05550025098708 54.484919060112567  -149.08256399963687 54.497443999555848  -149.10350034028724 54.482166829963326  -149.10376299932045 54.482286999346243  -149.12730499978875 54.464915000047952  -149.12719005110452 54.464862409610866  -149.10056600106313 54.452675230294339  -149.09935593843952 54.452121020142748  -149.09739099932179 54.451221000165958  -149.08540247126115 54.460066150190706  -149.07364500058964 54.454580000325436  -149.0648555614566 54.460947030145064  -149.04984200035386 54.453855000256475  -149.02583899837447 54.471024000222918","_id_":"ext-record-1831"},
{"footprint":"POLYGON ICRS -148.95892600025127 54.397839000209778  -148.94725900134884 54.416151669527132  -148.94694499990709 54.416085000178029  -148.94559606049867 54.418238940896046  -148.93392799964963 54.436860000482547  -148.96966299879367 54.444441000675148  -148.96966299977086 54.444440999117305  -148.96966300005394 54.4444409991773  -148.97629595895455 54.433863870017255  -148.99039399943564 54.436941000455413  -148.99531639078276 54.429300000127377  -149.01337100026123 54.4333100006184  -149.02693400056845 54.41264500039609  -148.99138200032002 54.404746999281627  -148.99138199930189 54.404747000833112  -148.99138199906173 54.404747000779714  -148.99132925949289 54.404827369064364  -148.95892600025127 54.397839000209778","_id_":"ext-record-1832"},
{"footprint":"POLYGON ICRS -148.95892600025127 54.397839000209778  -148.94725900134884 54.416151669527132  -148.94694499990709 54.416085000178029  -148.94559606049867 54.418238940896046  -148.93392799964963 54.436860000482547  -148.96966299879367 54.444441000675148  -148.96966299977086 54.444440999117305  -148.96966300005394 54.4444409991773  -148.97629595895455 54.433863870017255  -148.99039399943564 54.436941000455413  -148.99531639078276 54.429300000127377  -149.01337100026123 54.4333100006184  -149.02693400056845 54.41264500039609  -148.99138200032002 54.404746999281627  -148.99138199930189 54.404747000833112  -148.99138199906173 54.404747000779714  -148.99132925949289 54.404827369064364  -148.95892600025127 54.397839000209778","_id_":"ext-record-1833"},
{"footprint":"POLYGON ICRS -149.15832510008093 54.466968200078945  -149.15615529993795 54.474957499948964  -149.16829670008292 54.475349699924394  -149.17046409989823 54.467360200047715  -149.15832510008093 54.466968200078945","_id_":"ext-record-1834"},
{"footprint":"POLYGON ICRS -149.25359830007997 54.464114800011686  -149.25142860013051 54.4721043000204  -149.26356949991478 54.472496599963961  -149.26573679987482 54.464506900003975  -149.25359830007997 54.464114800011686","_id_":"ext-record-1835"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1836"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1837"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1838"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1839"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1840"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1841"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1842"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1843"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1844"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1845"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1846"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1847"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1848"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1849"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1850"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1851"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1852"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1853"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1854"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1855"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1856"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137461  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1857"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1858"},
{"footprint":"POLYGON ICRS -149.39389700006305 54.294704999989776  210.612007 54.304216  -149.40428800003482 54.307659000084286  -149.40432869764885 54.307593492119807  -149.40444000021637 54.307617000040139  -149.41034700019515 54.298105000137468  -149.39404799973585 54.294663000086757  -149.39400745900005 54.294728335885047  -149.39389700006305 54.294704999989776","_id_":"ext-record-1859"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1860"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839979  -149.39389699973708 54.29470500022996  -149.39389699966981 54.29470500021575  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.307593489573087  -149.40443999977322 54.30761699695271  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200515  -149.39400746394321 54.294728336799373  -149.39389699997912 54.294704999839979","_id_":"ext-record-1861"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1862"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1863"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1864"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1865"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1866"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1867"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839979  -149.39389699973708 54.29470500022996  -149.39389699966981 54.29470500021575  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.307593489573087  -149.40443999977322 54.30761699695271  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200515  -149.39400746394321 54.294728336799373  -149.39389699997912 54.294704999839979","_id_":"ext-record-1868"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839965  -149.39389699973708 54.294705000229953  -149.39389699966981 54.294705000215735  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.307593489573073  -149.40443999977322 54.307616996952696  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839965","_id_":"ext-record-1869"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839979  -149.39389699973708 54.29470500022996  -149.39389699966981 54.29470500021575  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.307593489573087  -149.40443999977322 54.30761699695271  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200515  -149.39400746394321 54.294728336799373  -149.39389699997912 54.294704999839979","_id_":"ext-record-1870"},
{"footprint":"POLYGON ICRS -149.39389699997912 54.294704999839972  -149.39389699973708 54.29470500022996  -149.39389699966981 54.294705000215743  -149.38799300039793 54.304215999986553  -149.4042880003073 54.307658999967281  -149.4043286994287 54.30759348957308  -149.40443999977322 54.3076169969527  -149.4103469969717 54.298105002023284  -149.39404800663849 54.294662998200508  -149.39400746394321 54.294728336799366  -149.39389699997912 54.294704999839972","_id_":"ext-record-1871"},
{"footprint":"POLYGON ICRS -149.39404800007929 54.294663000041076  -149.38814500024716 54.304173999987768  -149.40444000012275 54.307617000141434  -149.41034700023386 54.29810500015742  -149.40244694751357 54.296437024311764  -149.39404800706075 54.294662998298456  -149.39404800480472 54.294663001039396  -149.39404800007929 54.294663000041076","_id_":"ext-record-1872"},
{"footprint":"POLYGON ICRS -149.39404800657496 54.294662998214584  -149.38814500313103 54.304173998735649  -149.40444000006556 54.307616997195865  -149.4103469969717 54.298105002023284  -149.39404800657496 54.294662998214584","_id_":"ext-record-1873"},
{"footprint":"POLYGON ICRS -149.39404800657496 54.294662998214584  -149.38814500313103 54.304173998735649  -149.40444000006556 54.307616997195865  -149.4103469969717 54.298105002023284  -149.39404800657496 54.294662998214584","_id_":"ext-record-1874"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.4313719998494  -149.34800500022018 54.431372000030414  -149.34800499926337 54.431372000224933  -149.35440328757264 54.4400820878087  210.645164 54.440671  -149.37083199996692 54.436696999948836  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745438  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173967  -149.34816515801617 54.431332214940582  -149.34800499957458 54.4313719998494","_id_":"ext-record-1875"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.4313719998494  -149.34800500022018 54.431372000030414  -149.34800499926337 54.431372000224933  -149.35440328757264 54.4400820878087  210.645164 54.440671  -149.37083199996692 54.436696999948836  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745438  -149.36415700004011 54.427358000195696  -149.34816499945364 54.431332000173967  -149.34816515801617 54.431332214940582  -149.34800499957458 54.4313719998494","_id_":"ext-record-1876"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1877"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195696  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1878"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1879"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1880"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195696  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1881"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195696  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1882"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1883"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1884"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022018 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757264 54.440082087808705  210.645164 54.440671  -149.37083199996692 54.436696999948843  -149.370831851858 54.436696798359144  -149.3709920000608 54.436656999745445  -149.36415700004011 54.427358000195689  -149.34816499945364 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1885"},
{"footprint":"POLYGON ICRS -149.34800499957458 54.431371999849404  -149.34800500022016 54.431372000030422  -149.34800499926337 54.431372000224933  -149.35440328757261 54.440082087808705  210.645164 54.440671  -149.3708319999669 54.436696999948843  -149.370831851858 54.436696798359144  -149.37099200006077 54.436656999745438  -149.36415700004008 54.427358000195689  -149.34816499945362 54.431332000173974  -149.34816515801617 54.431332214940589  -149.34800499957458 54.431371999849404","_id_":"ext-record-1886"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1887"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1888"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1889"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087235  -149.37083199972514 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1890"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1891"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.43137199978716  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870605  -149.36399699991574 54.427397999983548  -149.34800499978314 54.43137199978716","_id_":"ext-record-1892"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983555  -149.34800499978314 54.431371999787167","_id_":"ext-record-1893"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983555  -149.34800499978314 54.431371999787167","_id_":"ext-record-1894"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983555  -149.34800499978314 54.431371999787167","_id_":"ext-record-1895"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983555  -149.34800499978314 54.431371999787167","_id_":"ext-record-1896"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087242  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983555  -149.34800499978314 54.431371999787167","_id_":"ext-record-1897"},
{"footprint":"POLYGON ICRS -149.34800499978314 54.431371999787167  -149.35483599960747 54.440671000087249  -149.37083199972511 54.436696999870613  -149.36399699991574 54.427397999983548  -149.34800499978314 54.431371999787167","_id_":"ext-record-1898"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1899"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1900"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922079  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1901"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1902"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1903"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.43669679851012  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1904"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1905"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1906"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.43669679851012  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1907"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.43669679851012  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1908"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922079  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1909"},
{"footprint":"POLYGON ICRS -149.348004999633 54.431372000016076  210.645164 54.440671  -149.37083199993668 54.43669700016298  -149.37083185168004 54.436696798510113  -149.37099199999938 54.436656999916487  -149.36415700009354 54.427358000131136  -149.34816499979084 54.431331999922087  -149.34816515781253 54.431332215120882  -149.348004999633 54.431372000016076","_id_":"ext-record-1910"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.4406310001176  -149.37099200016914 54.436657000139263  -149.36415700005466 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1911"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1912"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.4406310001176  -149.37099200016914 54.436657000139263  -149.36415700005466 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1913"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.4406310001176  -149.37099200016914 54.436657000139263  -149.36415700005466 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1914"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.4406310001176  -149.37099200016914 54.43665700013927  -149.36415700005469 54.427358000178089  -149.34816499959885 54.4313320000814","_id_":"ext-record-1915"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.4406310001176  -149.37099200016914 54.436657000139263  -149.36415700005466 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1916"},
{"footprint":"POLYGON ICRS -149.34816499959885 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959885 54.4313320000814","_id_":"ext-record-1917"},
{"footprint":"POLYGON ICRS -149.34816499959888 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959888 54.4313320000814","_id_":"ext-record-1918"},
{"footprint":"POLYGON ICRS -149.34816499959888 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959888 54.4313320000814","_id_":"ext-record-1919"},
{"footprint":"POLYGON ICRS -149.34816499959888 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959888 54.4313320000814","_id_":"ext-record-1920"},
{"footprint":"POLYGON ICRS -149.34816499959888 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959888 54.4313320000814","_id_":"ext-record-1921"},
{"footprint":"POLYGON ICRS -149.34816499959888 54.4313320000814  -149.35499499974958 54.440631000117591  -149.37099200016914 54.436657000139263  -149.36415700005469 54.427358000178081  -149.34816499959888 54.4313320000814","_id_":"ext-record-1922"},
{"footprint":"POLYGON ICRS -149.06275400017904 54.462468999995018  -149.07632999985441 54.468800000008542  -149.08721999983004 54.460913999973407  -149.07364500013657 54.454580000023121  -149.06275400017904 54.462468999995018","_id_":"ext-record-1923"},
{"footprint":"POLYGON ICRS -149.06275400017907 54.462468999995025  -149.07632999985441 54.468800000008549  -149.08721999983004 54.460913999973407  -149.07364500013657 54.454580000023128  -149.06275400017907 54.462468999995025","_id_":"ext-record-1924"},
{"footprint":"POLYGON ICRS -149.06275400017907 54.462468999995025  -149.07632999985441 54.468800000008549  -149.08721999983004 54.460913999973407  -149.07364500013657 54.454580000023128  -149.06275400017907 54.462468999995025","_id_":"ext-record-1925"},
{"footprint":"POLYGON ICRS -149.40395900037146 54.29000000002975  -149.4049009999444 54.300100999770194  -149.42220399971404 54.299551000089444  -149.42126499997005 54.28944800011071  -149.40395900037146 54.29000000002975","_id_":"ext-record-1926"},
{"footprint":"POLYGON ICRS -149.40395900037149 54.29000000002975  -149.4049009999444 54.300100999770194  -149.42220399971404 54.299551000089444  -149.42126499997005 54.28944800011071  -149.40395900037149 54.29000000002975","_id_":"ext-record-1927"},
{"footprint":"POLYGON ICRS -149.40395900037146 54.29000000002975  -149.4049009999444 54.300100999770194  -149.42220399971404 54.299551000089444  -149.42126499997005 54.28944800011071  -149.40395900037146 54.29000000002975","_id_":"ext-record-1928"},
{"footprint":"POLYGON ICRS -149.40395900037149 54.29000000002975  -149.4049009999444 54.300100999770194  -149.42220399971404 54.299551000089444  -149.42126499997005 54.28944800011071  -149.40395900037149 54.29000000002975","_id_":"ext-record-1929"},
{"footprint":"POLYGON ICRS -149.40395999999487 54.290001000141118  -149.40490299991816 54.30010199984482  -149.422206000084 54.299551999906484  -149.42126600000293 54.2894490001076  -149.40395999999487 54.290001000141118","_id_":"ext-record-1930"},
{"footprint":"POLYGON ICRS -149.40395999999487 54.290001000141118  -149.40490299991816 54.30010199984482  -149.422206000084 54.299551999906484  -149.42126600000293 54.2894490001076  -149.40395999999487 54.290001000141118","_id_":"ext-record-1931"},
{"footprint":"POLYGON ICRS -149.405237999626 54.290467000072489  -149.40618099983391 54.300567000147481  -149.42348400035422 54.300017000151627  -149.42287687139057 54.293492871341982  210.577456 54.289915  -149.405237999626 54.290467000072489","_id_":"ext-record-1932"},
{"footprint":"POLYGON ICRS -149.405237999626 54.290467000072482  -149.40618099983391 54.300567000147474  -149.42348400035422 54.30001700015162  -149.42287687139057 54.293492871341982  210.577456 54.289915  -149.405237999626 54.290467000072482","_id_":"ext-record-1933"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183391  -149.40618099979204 54.30056700007794  -149.42348400031233 54.3000170000821  -149.42254400000249 54.289915000212829  -149.405238000228 54.290467000183391","_id_":"ext-record-1934"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183391  -149.40618099979204 54.30056700007794  -149.42348400031233 54.3000170000821  -149.42254400000249 54.289915000212829  -149.405238000228 54.290467000183391","_id_":"ext-record-1935"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183391  -149.40618099979204 54.30056700007794  -149.42348400031233 54.3000170000821  -149.42254400000249 54.289915000212829  -149.405238000228 54.290467000183391","_id_":"ext-record-1936"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183391  -149.40618099979204 54.30056700007794  -149.42348400031233 54.3000170000821  -149.42254400000249 54.289915000212829  -149.405238000228 54.290467000183391","_id_":"ext-record-1937"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183384  -149.40618099979204 54.300567000077933  -149.42348400031233 54.300017000082093  -149.42254400000249 54.289915000212822  -149.405238000228 54.290467000183384","_id_":"ext-record-1938"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183384  -149.40618099979204 54.300567000077933  -149.42348400031233 54.300017000082093  -149.42254400000249 54.289915000212822  -149.405238000228 54.290467000183384","_id_":"ext-record-1939"},
{"footprint":"POLYGON ICRS -149.405238000228 54.290467000183384  -149.40618099979204 54.300567000077933  -149.42348400031233 54.300017000082093  -149.42254400000249 54.289915000212822  -149.405238000228 54.290467000183384","_id_":"ext-record-1940"},
{"footprint":"POLYGON ICRS -149.40523799967127 54.290466999935681  -149.40618099983163 54.3005669998747  -149.42348400078021 54.30001700010493  -149.42295994230611 54.294385664756987  -149.42280221591693 54.292690492211015  -149.42254400013172 54.289914999839567  -149.40523799967127 54.290466999935681","_id_":"ext-record-1941"},
{"footprint":"POLYGON ICRS -149.11962899944896 54.1772869996957  -149.11821988863312 54.196800239423162  -149.11788699909278 54.1967929994576  -149.11774280957263 54.199084600002443  -149.11649599947086 54.218890000319824  -149.11677957034894 54.218896172860113  -149.11676899888275 54.219063999536687  -149.11705150974615 54.219070141596092  -149.11704099947139 54.219237000687841  -149.15484499973294 54.22005399824058  -149.15555701110114 54.20880274992664  -149.17051800014704 54.209207000637456  -149.17115700849851 54.201055997593237  -149.19035199947274 54.20164000102384  -149.192317000287 54.179549000135331  -149.19202930585229 54.179540283664011  -149.19204400009255 54.179374999602373  -149.1917573838702 54.179366324409109  -149.19177200029361 54.179201999405883  -149.15397499961671 54.178050998835893  -149.15397499948006 54.178051000370466  -149.15397499837468 54.17805100033663  -149.1539673402381 54.17813699891709  -149.11962899944896 54.1772869996957","_id_":"ext-record-1942"},
{"footprint":"POLYGON ICRS -149.11962899944896 54.1772869996957  -149.11821988863312 54.196800239423162  -149.11788699909278 54.1967929994576  -149.11774280957263 54.199084600002443  -149.11649599947086 54.218890000319824  -149.11677957034894 54.218896172860113  -149.11676899888275 54.219063999536687  -149.11705150974615 54.219070141596092  -149.11704099947139 54.219237000687841  -149.15484499973294 54.22005399824058  -149.15555701110114 54.20880274992664  -149.17051800014704 54.209207000637456  -149.17115700849851 54.201055997593237  -149.19035199947274 54.20164000102384  -149.192317000287 54.179549000135331  -149.19202930585229 54.179540283664011  -149.19204400009255 54.179374999602373  -149.1917573838702 54.179366324409109  -149.19177200029361 54.179201999405883  -149.15397499961671 54.178050998835893  -149.15397499948006 54.178051000370466  -149.15397499837468 54.17805100033663  -149.1539673402381 54.17813699891709  -149.11962899944896 54.1772869996957","_id_":"ext-record-1943"},
{"footprint":"POLYGON ICRS -149.11962900018202 54.177287000275626  -149.11821988976041 54.196800239622142  -149.11788700021512 54.196792999710809  -149.11774280922654 54.199084599556478  -149.11649599966393 54.218889999406414  -149.11677956840768 54.218896173205074  -149.11676899961387 54.219064000764511  -149.15457300167884 54.219879999514824  -149.15528451081337 54.208628730781633  -149.17024600157379 54.2090330010956  -149.17088420001613 54.200881990163033  -149.19007899866952 54.201465999610988  -149.19204399913141 54.179375000576435  -149.19175738329162 54.179366325040142  -149.1917719984167 54.179202000283077  -149.15397499966363 54.178050999094552  -149.15396734018987 54.178137000705192  -149.11962900018202 54.177287000275626","_id_":"ext-record-1944"},
{"footprint":"POLYGON ICRS -149.11962900018202 54.177287000275626  -149.11821988976041 54.196800239622142  -149.11788700021512 54.196792999710809  -149.11774280922654 54.199084599556478  -149.11649599966393 54.218889999406414  -149.11677956840768 54.218896173205074  -149.11676899961387 54.219064000764511  -149.15457300167884 54.219879999514824  -149.15528451081337 54.208628730781633  -149.17024600157379 54.2090330010956  -149.17088420001613 54.200881990163033  -149.19007899866952 54.201465999610988  -149.19204399913141 54.179375000576435  -149.19175738329162 54.179366325040142  -149.1917719984167 54.179202000283077  -149.15397499966363 54.178050999094552  -149.15396734018987 54.178137000705192  -149.11962900018202 54.177287000275626","_id_":"ext-record-1945"},
{"footprint":"POLYGON ICRS -148.88490999975824 54.316728999957114  -148.88581350203737 54.3170051847262  -148.88561700162703 54.317224000910187  211.080762 54.327494  211.08069422 54.32741854  211.050009 54.33657  -148.95619575283294 54.329491689199159  -148.96042500028378 54.330752000136272  -148.975657068616 54.313361680147807  -148.97595400029431 54.313449000028825  -148.99301700157346 54.293705000725744  -148.99288949977046 54.2936675091506  -148.96275091864425 54.284798530146269  -148.96137978993934 54.284394770704822  -148.95915300038641 54.283739000027772  -148.95322733305176 54.290595797189731  211.04837917 54.29012268  211.04975073 54.28971873  211.051977 54.289063  -148.94528221571252 54.292232808356275  -148.93711799910955 54.289773999958769  -148.93106396386267 54.296625686416917  211.074016 54.295095  -148.92564204823637 54.295481878481858  -148.91363699938975 54.291813000174905  -148.90749686082248 54.298659983559553  211.097501 54.297132  -148.88490999975824 54.316728999957114","_id_":"ext-record-1946"},
{"footprint":"POLYGON ICRS -148.88490999967851 54.316729000318752  -148.88581349914239 54.31700518422609  -148.88561699797978 54.317224001232013  211.080762 54.327494  211.08069422 54.32741854  211.050009 54.33657  -148.95619575357551 54.329491688724836  211.039575 54.330752  211.02434293 54.31336168  211.024046 54.313449  -148.99301699916703 54.293705001280756  211.0071105 54.29366751  -148.99149219427554 54.293256570081837  -148.99302400082888 54.291483002059984  211.00710261 54.29144576  -148.99251028966381 54.291331907067324  -148.99316399798354 54.290574999106141  -148.99303589848358 54.290537320371939  -148.96290032937071 54.281668448936593  -148.961528859787 54.281264559876192  -148.95930300026214 54.280608999888827  -148.95119064391571 54.289995984502561  211.04975073 54.28971873  211.051977 54.289063  -148.94746068556188 54.289713379651481  -148.93726999853138 54.286644000387753  -148.93085925016553 54.293899310909069  -148.91378999877045 54.2886830000155  -148.90541391864619 54.298023209240505  211.097501 54.297132  -148.88490999967851 54.316729000318752","_id_":"ext-record-1947"},
{"footprint":"POLYGON ICRS -148.88575900156724 54.316315999906116  211.080621 54.326586  211.08055395 54.32651135  211.049869 54.335663  -148.9558736148181 54.329111650629692  -148.95971699900372 54.330257000364291  -148.97494967994606 54.31286686038613  -148.97524599918412 54.312953999338305  -148.99230900160845 54.293210000043672  -148.99218281002976 54.293172890065705  -148.9620426410913 54.2843040801435  -148.96067221950508 54.283900559294764  -148.95844599862266 54.283244999883429  -148.95277935722009 54.289801399371029  211.04753205 54.28970969  211.04890293 54.28930595  211.05113 54.28865  -148.94586469957335 54.292126076939866  -148.93641100003973 54.289278999272035  -148.9306267183857 54.295825446849932  211.073168 54.294682  -148.92621653894841 54.295378406589229  -148.91293000133442 54.291317999642  -148.90706676243957 54.2978562634126  211.096653 54.296719  -148.88575900156724 54.316315999906116","_id_":"ext-record-1948"},
{"footprint":"POLYGON ICRS -148.88575900156724 54.316315999906116  211.080621 54.326586  211.08055395 54.32651135  211.049869 54.335663  -148.9558736148181 54.329111650629692  -148.95971699900372 54.330257000364291  -148.97494967994606 54.31286686038613  -148.97524599918412 54.312953999338305  -148.99230900160845 54.293210000043672  -148.99218281002976 54.293172890065705  -148.9620426410913 54.2843040801435  -148.96067221950508 54.283900559294764  -148.95844599862266 54.283244999883429  -148.95277935722009 54.289801399371029  211.04753205 54.28970969  211.04890293 54.28930595  211.05113 54.28865  -148.94586469957335 54.292126076939866  -148.93641100003973 54.289278999272035  -148.9306267183857 54.295825446849932  211.073168 54.294682  -148.92621653894841 54.295378406589229  -148.91293000133442 54.291317999642  -148.90706676243957 54.2978562634126  211.096653 54.296719  -148.88575900156724 54.316315999906116","_id_":"ext-record-1949"},
{"footprint":"POLYGON ICRS -148.89620999782144 54.30828099988873  -148.89673882120505 54.308442640903614  -148.89606899951977 54.309189000489134  211.070314 54.319456  211.07024685 54.31938119  211.039565 54.32853  211.02433383 54.3111397  211.024037 54.311227  -148.99302399954226 54.291482999821717  211.00710261 54.29144576  -148.99251028697637 54.291331907377518  -148.99316399842888 54.290575000368044  -148.99303589848358 54.290537320371939  -148.96290032937071 54.281668448936593  -148.961528859787 54.281264559876192  -148.95930300026214 54.280608999888827  -148.95061412110368 54.290662910202464  -148.93726999853138 54.286644000387753  -148.93085925016553 54.293899310909069  -148.91378999877045 54.2886830000155  -148.89620999782144 54.30828099988873","_id_":"ext-record-1950"},
{"footprint":"POLYGON ICRS -148.96466600051397 54.300694000132616  -148.96518871820587 54.30085117333045  -148.96452600021826 54.301602000005765  211.020062 54.306234  -148.987874999988 54.29724499992556  -148.98735367222426 54.297088285472711  -148.98801600030083 54.296337999916773  -148.97260199999221 54.2917019998835  -148.96466600051397 54.300694000132616","_id_":"ext-record-1951"},
{"footprint":"POLYGON ICRS -148.96466600051397 54.300694000132616  -148.96518871820587 54.30085117333045  -148.96452600021826 54.301602000005765  211.020062 54.306234  -148.987874999988 54.29724499992556  -148.98735367222426 54.297088285472711  -148.98801600030083 54.296337999916773  -148.97260199999221 54.2917019998835  -148.96466600051397 54.300694000132616","_id_":"ext-record-1952"},
{"footprint":"POLYGON ICRS -149.183377000118 54.484919000235834  -149.17688499988262 54.494301000066535  -149.1930439999536 54.498076000153183  -149.19953299957552 54.488691000107011  -149.183377000118 54.484919000235834","_id_":"ext-record-1953"},
{"footprint":"POLYGON ICRS -149.183377000118 54.484919000235834  -149.17688499988262 54.494301000066535  -149.1930439999536 54.498076000153183  -149.19953299957552 54.488691000107011  -149.183377000118 54.484919000235834","_id_":"ext-record-1954"},
{"footprint":"POLYGON ICRS -149.3953579998886 54.424586000128961  -149.4070279999479 54.432079999818455  -149.41991900028222 54.4252880000099  -149.40824399988125 54.417795000042624  -149.3953579998886 54.424586000128961","_id_":"ext-record-1955"},
{"footprint":"POLYGON ICRS -149.3953579998886 54.424586000128961  -149.4070279999479 54.432079999818455  -149.41991900028222 54.4252880000099  -149.40824399988125 54.417795000042624  -149.3953579998886 54.424586000128961","_id_":"ext-record-1956"},
{"footprint":"POLYGON ICRS -149.3953579998886 54.424586000128961  -149.4070279999479 54.432079999818455  -149.41991900028222 54.4252880000099  -149.40824399988125 54.417795000042624  -149.3953579998886 54.424586000128961","_id_":"ext-record-1957"},
{"footprint":"POLYGON ICRS -149.15349100035209 54.19829200004628  -149.15269600000912 54.20839200012054  -149.15298208689958 54.208399769863405  -149.15296900038288 54.208565999926165  -149.15325408973555 54.208573726080047  -149.15324099975356 54.208739999965751  -149.17051800061816 54.209206999879648  -149.171310000312 54.199103999996424  -149.1710239808346 54.199096321037025  -149.17103699955635 54.198930000084367  -149.17075204134116 54.198922348979011  -149.1707650002524 54.198756999916107  -149.15349100035209 54.19829200004628","_id_":"ext-record-1958"},
{"footprint":"POLYGON ICRS -149.15349100035209 54.19829200004628  -149.15269600000912 54.20839200012054  -149.15298208689958 54.208399769863405  -149.15296900038288 54.208565999926165  -149.15325408973555 54.208573726080047  -149.15324099975356 54.208739999965751  -149.17051800061816 54.209206999879648  -149.171310000312 54.199103999996424  -149.1710239808346 54.199096321037025  -149.17103699955635 54.198930000084367  -149.17075204134116 54.198922348979011  -149.1707650002524 54.198756999916107  -149.15349100035209 54.19829200004628","_id_":"ext-record-1959"},
{"footprint":"POLYGON ICRS -149.15349100043451 54.198291999901066  -149.15349100040439 54.198292000283821  -149.15349100012384 54.198292000276254  -149.15269599968912 54.208391999910418  -149.15298208629889 54.2083997698584  -149.1529690001486 54.208566000179651  -149.17024600025323 54.209033000617509  -149.17103700024634 54.1989300001263  -149.17075204138527 54.198922348960416  -149.17076500022412 54.198757000007518  -149.15349100043451 54.198291999901066","_id_":"ext-record-1960"},
{"footprint":"POLYGON ICRS -149.15349100043451 54.198291999901066  -149.15349100040439 54.198292000283821  -149.15349100012384 54.198292000276254  -149.15269599968912 54.208391999910418  -149.15298208629889 54.2083997698584  -149.1529690001486 54.208566000179651  -149.17024600025323 54.209033000617509  -149.17103700024634 54.1989300001263  -149.17075204138527 54.198922348960416  -149.17076500022412 54.198757000007518  -149.15349100043451 54.198291999901066","_id_":"ext-record-1961"},
{"footprint":"POLYGON ICRS -148.98021499994326 54.423919000186828  -148.97411199959194 54.433387000056683  -148.99039400014883 54.436941000187645  211.003505 54.42747  -148.99649499949774 54.427470000076475  -148.99649499964167 54.427470000015092  -148.98021499994326 54.423919000186828","_id_":"ext-record-1962"},
{"footprint":"POLYGON ICRS -148.98021499994326 54.423919000186828  -148.97411199959194 54.433387000056683  -148.99039400014883 54.436941000187645  211.003505 54.42747  -148.99649499949774 54.427470000076475  -148.99649499964167 54.427470000015092  -148.98021499994326 54.423919000186828","_id_":"ext-record-1963"},
{"footprint":"POLYGON ICRS -149.16857600069275 54.15645099990067  -149.18085338024449 54.165118180878565  -149.16924700012785 54.170648000892719  -149.17805475904743 54.176976689500094  -149.16308500030755 54.184022000423  -149.18677200136949 54.201258000244806  -149.21624299860346 54.187380999783741  -149.21615226896171 54.187314999886325  -149.24270299833159 54.174560000268926  -149.2215372402477 54.159463270886107  -149.22179299930454 54.159339000498107  -149.21926776096723 54.157558360717552  -149.1976720006042 54.142321999922871  -149.16857600069275 54.15645099990067","_id_":"ext-record-1964"},
{"footprint":"POLYGON ICRS -149.16857600082358 54.156450995647539  -149.16857600314484 54.156450998396963  -149.16857600090631 54.156450999375934  -149.18085337997385 54.165118179350429  -149.16924700130787 54.170648000247581  -149.16924700698002 54.170648004324072  -149.16924699138207 54.170648012255974  -149.1766122595761 54.175940378177984  -149.17805476004702 54.176976689719744  -149.16308500034043 54.184021999675615  -149.18677199907324 54.201257999254388  -149.18677201954861 54.201257991385219  -149.18677202322729 54.20125799278027  -149.19188721942433 54.19885058296353  -149.21624300227242 54.187381000117206  -149.21619822202223 54.187348427083265  -149.21615228531056 54.187315000147912  -149.24270301176475 54.174560011077638  -149.24270299751873 54.174560001499138  -149.2427030013227 54.17456000072206  -149.22799970640017 54.1640742624551  -149.22153724248497 54.159463269267363  -149.22156459388867 54.159449978720161  -149.221602628814 54.159431498319776  -149.22179301052921 54.159339006925322  -149.21926776262649 54.157558357328391  -149.21623737170219 54.155421252228855  -149.197672000191 54.142322000843876  -149.19767198238497 54.142322008461576  -149.19767198250452 54.142322007273556  -149.16857600082358 54.156450995647539","_id_":"ext-record-1965"},
{"footprint":"POLYGON ICRS -149.16857600024809 54.156450995822169  -149.18085339352004 54.165118180016108  -149.16924699102304 54.170648010790615  -149.17805477222046 54.176976695257352  -149.16308501275546 54.184021997367168  -149.18677202322729 54.20125799278027  -149.21624298100824 54.187380994223254  -149.21615228232145 54.18731500101925  -149.24270301056322 54.17456001156274  -149.22153724010448 54.159463267641136  -149.22179300826016 54.159339007563617  -149.21926776262649 54.157558357328391  -149.19767198250452 54.142322007273556  -149.16857600024809 54.156450995822169","_id_":"ext-record-1966"},
{"footprint":"POLYGON ICRS -149.16857600024809 54.156450995822169  -149.18085339352004 54.165118180016108  -149.16924699102304 54.170648010790615  -149.17805477222046 54.176976695257352  -149.16308501275546 54.184021997367168  -149.18677202322729 54.20125799278027  -149.21624298100824 54.187380994223254  -149.21615228232145 54.18731500101925  -149.24270301056322 54.17456001156274  -149.22153724010448 54.159463267641136  -149.22179300826016 54.159339007563617  -149.21926776262649 54.157558357328391  -149.19767198250452 54.142322007273556  -149.16857600024809 54.156450995822169","_id_":"ext-record-1967"},
{"footprint":"POLYGON ICRS -149.33977699972422 54.478681000440368  -149.3624399993266 54.49310473950964  -149.36219500045971 54.493235999277061  210.611993 54.509486  210.61188492 54.5094281  -149.38842140385051 54.509263990950146  -149.3887249985921 54.5094550002883  -149.38883146928481 54.509397959583239  -149.41374779925735 54.496041629118814  -149.41487931119224 54.495434759905741  -149.41671899880993 54.494447998985549  -149.40357653896407 54.486173810580915  -149.41475599877327 54.480292001304072  -149.4053242109313 54.474245949632959  -149.41974399889145 54.466746000893082  -149.39434900092891 54.450264000304621  -149.39392444570629 54.450484928874126  210.606368 54.450295  -149.36523699969044 54.465062000556649  -149.36533598095397 54.465126290017245  -149.33977699972422 54.478681000440368","_id_":"ext-record-1968"},
{"footprint":"POLYGON ICRS -149.33977699972422 54.478681000440368  -149.3624399993266 54.49310473950964  -149.36219500045971 54.493235999277061  210.611993 54.509486  210.61188492 54.5094281  -149.38842140385051 54.509263990950146  -149.3887249985921 54.5094550002883  -149.38883146928481 54.509397959583239  -149.41374779925735 54.496041629118814  -149.41487931119224 54.495434759905741  -149.41671899880993 54.494447998985549  -149.40357653896407 54.486173810580915  -149.41475599877327 54.480292001304072  -149.4053242109313 54.474245949632959  -149.41974399889145 54.466746000893082  -149.39434900092891 54.450264000304621  -149.39392444570629 54.450484928874126  210.606368 54.450295  -149.36523699969044 54.465062000556649  -149.36533598095397 54.465126290017245  -149.33977699972422 54.478681000440368","_id_":"ext-record-1969"},
{"footprint":"POLYGON ICRS -149.34121299873473 54.478619000365889  -149.36387609004527 54.493043679714773  -149.36363100039657 54.49317500086341  -149.38944300020847 54.509423999796319  210.61045053 54.50936696  -149.41446628053836 54.496010369326655  -149.41559636929026 54.495404260659988  -149.41743699913297 54.494417000075245  -149.40429441924493 54.486142739506853  -149.4154740012554 54.480259999792786  -149.40604214874568 54.474214719879711  -149.42046100062964 54.466714999794384  -149.39506599854855 54.450233000275375  -149.3666720001097 54.464999999595214  -149.366771429825 54.46506457936178  -149.34121299873473 54.478619000365889","_id_":"ext-record-1970"},
{"footprint":"POLYGON ICRS -149.34121299873473 54.478619000365889  -149.36387609004527 54.493043679714773  -149.36363100039657 54.49317500086341  -149.38944300020847 54.509423999796319  210.61045053 54.50936696  -149.41446628053836 54.496010369326655  -149.41559636929026 54.495404260659988  -149.41743699913297 54.494417000075245  -149.40429441924493 54.486142739506853  -149.4154740012554 54.480259999792786  -149.40604214874568 54.474214719879711  -149.42046100062964 54.466714999794384  -149.39506599854855 54.450233000275375  -149.3666720001097 54.464999999595214  -149.366771429825 54.46506457936178  -149.34121299873473 54.478619000365889","_id_":"ext-record-1971"},
{"footprint":"POLYGON ICRS -148.91803699983933 54.304083999837566  -148.91894180014538 54.30435642149051  -148.91874500031062 54.304578999761667  211.065847 54.309217  -148.94040914238724 54.302144184991626  -148.94458300023356 54.303400000031694  -148.95252700013748 54.294414000087293  -148.93711799999002 54.289773999785318  -148.93106396375 54.296625684544736  211.074016 54.295095  -148.91803699983933 54.304083999837566","_id_":"ext-record-1972"},
{"footprint":"POLYGON ICRS -148.91803700003362 54.304083999846256  -148.91894180088886 54.304356421666775  -148.91874500088 54.304579000158647  211.065847 54.309217  -148.94040914235407 54.302144184910219  211.055417 54.3034  -148.9525269995203 54.29441399991493  -148.95098368597988 54.29394940873896  -148.95253699982371 54.292192000302663  -148.9520137745599 54.292034484296337  -148.95267699929983 54.291284000430196  -148.93726999988 54.286644000142516  -148.92932700015237 54.29563299969066  -148.9298502165048 54.29579052049678  -148.92952257837655 54.296161272102687  211.074016 54.295095  -148.91803700003362 54.304083999846256","_id_":"ext-record-1973"},
{"footprint":"POLYGON ICRS -148.91888599997625 54.303670999961611  211.065706 54.308309  -148.94008267999627 54.301764669329685  -148.94387500032525 54.302906000061306  -148.95181999999139 54.293919000341205  -148.93641100040756 54.289278999937544  -148.93062671544297 54.295825446964137  211.073168 54.294682  -148.91888599997625 54.303670999961611","_id_":"ext-record-1974"},
{"footprint":"POLYGON ICRS -148.91888599997625 54.303670999961611  211.065706 54.308309  -148.94008267999627 54.301764669329685  -148.94387500032525 54.302906000061306  -148.95181999999139 54.293919000341205  -148.93641100040756 54.289278999937544  -148.93062671544297 54.295825446964137  211.073168 54.294682  -148.91888599997625 54.303670999961611","_id_":"ext-record-1975"},
{"footprint":"POLYGON ICRS -148.92932699981989 54.295632999957377  -148.92985021717649 54.295790520501079  -148.92918700007024 54.296540999892855  211.055407 54.301178  -148.95253699993717 54.292191999868606  -148.95201377456485 54.292034483948122  -148.95267699979772 54.291284000191254  -148.93726999988 54.286644000142516  -148.92932699981989 54.295632999957377","_id_":"ext-record-1976"},
{"footprint":"POLYGON ICRS -149.44015300066678 54.420974999909554  -149.46278405058459 54.435399530411146  -149.46253900138461 54.435530999504905  210.511686 54.451781  210.51157815 54.45172313  -149.48914218149116 54.451336653501968  -149.48974900077815 54.451719000639727  -149.48985706039826 54.451661019837559  -149.51473648926506 54.43830538949851  -149.515866419254 54.437698490109483  -149.51770299956348 54.43671199999195  -149.50457970825931 54.428437829174833  -149.51574300038493 54.422554999218193  -149.50632492906513 54.416509471449963  -149.52072299996578 54.409010000950289  -149.49536399809656 54.392528000388289  -149.49451608796633 54.392969849137224  210.506068 54.39259  -149.46557700117708 54.407357000172709  -149.465675309253 54.407420940662277  -149.44015300066678 54.420974999909554","_id_":"ext-record-1977"},
{"footprint":"POLYGON ICRS -149.44015300066678 54.420974999909554  -149.46278405058459 54.435399530411146  -149.46253900138461 54.435530999504905  210.511686 54.451781  210.51157815 54.45172313  -149.48914218149116 54.451336653501968  -149.48974900077815 54.451719000639727  -149.48985706039826 54.451661019837559  -149.51473648926506 54.43830538949851  -149.515866419254 54.437698490109483  -149.51770299956348 54.43671199999195  -149.50457970825931 54.428437829174833  -149.51574300038493 54.422554999218193  -149.50632492906513 54.416509471449963  -149.52072299996578 54.409010000950289  -149.49536399809656 54.392528000388289  -149.49451608796633 54.392969849137224  210.506068 54.39259  -149.46557700117708 54.407357000172709  -149.465675309253 54.407420940662277  -149.44015300066678 54.420974999909554","_id_":"ext-record-1978"},
{"footprint":"POLYGON ICRS -148.8271129993164 54.3928049992651  -148.84750536022565 54.402381576024148  -148.83108398915482 54.414234987878281  -148.83108400462592 54.414234996022039  -148.83108399952539 54.414234998278943  -148.83108400206766 54.414234999520822  -148.83108400070864 54.414235000501414  -148.85609625249131 54.425972863246258  -148.86068500009262 54.428125001946533  -148.87059473951928 54.420975588406705  -148.87203609892387 54.419935427734615  -148.87253165537916 54.419577795648237  -148.8728316008243 54.419361327907993  -148.87851783395951 54.4220810477295  -148.88446100059286 54.4249229994905  -148.88446101182583 54.424922992650117  -148.88446101651962 54.424922995184559  -148.89335783149045 54.418616950738411  -148.89960589807308 54.421640561380947  -148.90821399916345 54.425805010449594  -148.93251501213842 54.408795994342377  -148.91716487268283 54.401369925209671  -148.91204048683983 54.398889820772062  -148.92849100108478 54.387368999589434  -148.89925600010494 54.373213000358881  -148.89916152985526 54.373279170703604  -148.87239899970078 54.360575000049963  -148.85120792079132 54.375712590130362  -148.85094700112083 54.375589999501  -148.84847668827996 54.377375160170779  -148.8271129993164 54.3928049992651","_id_":"ext-record-1979"},
{"footprint":"POLYGON ICRS -148.82711299983725 54.392805000589377  -148.84750535268424 54.402381572167151  -148.83108399743568 54.414234993614471  -148.8310839780317 54.4142350166432  -148.83962865676389 54.418246206646948  -148.844558217744 54.420559709234475  -148.84476204925016 54.420655379170029  -148.85199447907368 54.424048765685832  -148.85246616688497 54.424270048776776  -148.85851206276726 54.427105960593266  -148.85977169769345 54.427696702388431  -148.86068498515297 54.428124998581282  -148.86696270555777 54.423596318646268  -148.87059472735845 54.420975575432237  -148.87198087464378 54.4199753005423  -148.87199742079713 54.419963323474086  -148.87200732047856 54.41995619462282  -148.87280989989435 54.4193769979711  -148.8728316089626 54.4193613241866  -148.87369466065562 54.419774171700219  -148.87641929624451 54.421077390574318  -148.88446098649419 54.424923004069733  -148.88508367311857 54.42448175951683  -148.8933578394261 54.418616952037482  -148.89498179080891 54.419402904766486  -148.90060380369366 54.422123420325626  -148.90610110605726 54.424782971650231  -148.90821401686253 54.425805007201269  -148.93251501332136 54.408796006145984  -148.92638017735175 54.405828662936628  -148.92245899865162 54.40393162921103  -148.91786395472647 54.401708212611439  -148.91763431780066 54.401597091626215  -148.91204048516906 54.398889820611089  -148.9284910006827 54.387368999824609  -148.89925600146191 54.373212999937486  -148.89916152960396 54.373279169498353  -148.87239899879253 54.360575000224692  -148.85120792020186 54.3757125900985  -148.85094700100763 54.375590000316393  -148.84847668850676 54.377375160431036  -148.82711299983725 54.392805000589377","_id_":"ext-record-1980"},
{"footprint":"POLYGON ICRS -148.83108398903187 54.414234987868284  -148.83108400450249 54.414234996012347  -148.83108399940184 54.414234998269151  -148.83108400206766 54.414234999520815  -148.83108400070864 54.414235000501414  -148.85609625249131 54.425972863246258  -148.86068500009262 54.42812500194654  -148.87059473951928 54.420975588406712  -148.87203609892387 54.419935427734615  -148.87253165537916 54.419577795648237  -148.8728316008243 54.419361327907986  -148.87851783395951 54.4220810477295  -148.88446100059286 54.424922999490491  -148.88446101182581 54.424922992650117  -148.88446101651962 54.424922995184559  -148.89335783149045 54.418616950738418  -148.89960589807308 54.421640561380954  -148.90821399916345 54.425805010449594  -148.93251501313409 54.40879599385795  -148.9171648726828 54.401369925209664  -148.90326199846504 54.394640000118628  -148.90326198974847 54.394640006222474  -148.90326198621281 54.394640003619756  -148.90316801399084 54.394705795681269  -148.876391006436 54.382003000329391  -148.87639100399463 54.382003001800605  -148.87639100060198 54.3820030010129  -148.85518944204892 54.397142268716671  -148.85492898946387 54.397019990186521  -148.85291199261266 54.398476923323322  -148.85245753939674 54.398805170486561  -148.83108398903187 54.414234987868284","_id_":"ext-record-1981"},
{"footprint":"POLYGON ICRS -148.83108398896204 54.414234988589271  -148.83108400397651 54.414234995638907  -148.83108399933204 54.414234998990125  -148.85398241521403 54.424981331271546  -148.86068500009262 54.428125001946533  -148.87059473951928 54.420975588406705  -148.87209826829874 54.419890562105124  -148.8728316008243 54.419361327907993  -148.8786459118256 54.422142299906739  211.115539 54.424923  -148.88446101182583 54.424922992650124  -148.88446101651962 54.424922995184559  -148.89335783099239 54.418616950046037  -148.89901359756993 54.421353964014159  -148.90821399916345 54.425805010449594  -148.93251501193933 54.408795994173069  -148.91716487268283 54.401369925209671  211.096738 54.39464  -148.90326199146162 54.3946400068933  -148.90326198621281 54.394640003619756  -148.90316801399084 54.394705795681261  -148.87639100643602 54.382003000329384  -148.87639100399466 54.382003001800612  211.123609 54.382003  -148.8551894431078 54.397142268862616  -148.85492898946387 54.397019990186514  -148.85291199261269 54.398476923323315  -148.85245753939674 54.398805170486561  -148.83108398896204 54.414234988589271","_id_":"ext-record-1982"},
{"footprint":"POLYGON ICRS -148.83108398897946 54.41423498894671  -148.86068500009262 54.428125001946533  -148.87059473951928 54.420975588406712  -148.8728315971864 54.41936132595503  -148.88446101651962 54.424922995184566  -148.89335783797642 54.418616946118931  -148.90821399916345 54.425805010449594  -148.9325150112015 54.408795994226359  -148.90326198621281 54.394640003619756  -148.90316801399084 54.394705795681261  -148.87639100643602 54.382003000329391  -148.85518945138941 54.397142272554227  -148.85492898946387 54.397019990186514  -148.85291199261269 54.398476923323315  -148.85245753939674 54.398805170486561  -148.83108398897946 54.41423498894671","_id_":"ext-record-1983"},
{"footprint":"POLYGON ICRS -149.16924699975712 54.170647999945793  -149.18016000012159 54.178489000110922  -149.19355999989543 54.17210200000099  -149.18265200022597 54.164260999942336  -149.16924699975712 54.170647999945793","_id_":"ext-record-1984"},
{"footprint":"POLYGON ICRS -149.16924699975709 54.170647999945786  -149.18016000012156 54.178489000110922  -149.1935599998954 54.17210200000099  -149.18265200022597 54.164260999942336  -149.16924699975709 54.170647999945786","_id_":"ext-record-1985"},
{"footprint":"POLYGON ICRS -149.16924699975709 54.170647999945786  -149.18016000012156 54.178489000110922  -149.1935599998954 54.17210200000099  -149.18265200022597 54.164260999942336  -149.16924699975709 54.170647999945786","_id_":"ext-record-1986"},
{"footprint":"POLYGON ICRS -149.16924699975709 54.170647999945786  -149.18016000012156 54.178489000110915  -149.1935599998954 54.17210200000099  -149.18265200022597 54.164260999942329  -149.16924699975709 54.170647999945786","_id_":"ext-record-1987"},
{"footprint":"POLYGON ICRS -149.38944500031442 54.479620999928628  210.59887 54.487114  -149.40155097401507 54.486892579777994  -149.40184800020097 54.487082999853584  -149.41475600011341 54.480291999758506  -149.40306600008159 54.472797999818333  -149.40264519164242 54.473019566134234  210.597652 54.472829  -149.38944500031442 54.479620999928628","_id_":"ext-record-1988"},
{"footprint":"POLYGON ICRS -149.38944500031442 54.479620999928628  210.59887 54.487114  -149.40155097401507 54.486892579777994  -149.40184800020097 54.487082999853584  -149.41475600011341 54.480291999758506  -149.40306600008159 54.472797999818333  -149.40264519164242 54.473019566134234  210.597652 54.472829  -149.38944500031442 54.479620999928628","_id_":"ext-record-1989"},
{"footprint":"POLYGON ICRS -149.39088099962672 54.479558000062184  -149.40256599966011 54.487051999931  -149.41547400029728 54.480260000050407  -149.40378400009257 54.472766999865961  -149.39088099962672 54.479558000062184","_id_":"ext-record-1990"},
{"footprint":"POLYGON ICRS -149.39088099962672 54.479558000062184  -149.40256599966011 54.487051999931  -149.41547400029728 54.480260000050407  -149.40378400009257 54.472766999865961  -149.39088099962672 54.479558000062184","_id_":"ext-record-1991"},
{"footprint":"POLYGON ICRS -148.87102899378158 54.418498999206804  -148.87102900204988 54.418499001240157  -148.87180617813553 54.418870789648118  -148.87873610363144 54.422185440102396  -148.879946669334 54.422764363367243  -148.88446099960038 54.424923000007979  -148.88446100320837 54.42492299777826  -148.88446100617656 54.424922999063362  -148.8884372095558 54.422104994766357  -148.89548900082704 54.417106000382141  -148.89548899825431 54.417105999647589  -148.89548900206546 54.417105997020819  -148.89379789801734 54.416297907760615  -148.89342545149054 54.416119927418158  -148.88969637070485 54.414337766437  -148.882056999968 54.410686000218092  -148.88205699732211 54.410686001743208  -148.87492201579357 54.415741371987423  -148.87102899378158 54.418498999206804POLYGON ICRS  -148.86703699973293 54.397070999790039  -148.88046100034694 54.403494999898413  -148.8914840000061 54.395677999551516  -148.87806000015573 54.389257000437361  -148.86703699973293 54.397070999790039","_id_":"ext-record-1992"},
{"footprint":"POLYGON ICRS -148.8710289991331 54.418498999412719  -148.87102900049618 54.418498999728676  -148.87514601939907 54.420468381475693  -148.88446099914538 54.424923000047578  -148.88446099977685 54.424922999299127  -148.89548900055325 54.417106000580851  -148.89548899913314 54.4171059994717  -148.89548900049164 54.417105998669349  -148.89240712371409 54.415633286539965  -148.88205699995456 54.410685999991294  -148.88205699700873 54.410685998720112  -148.8710289991331 54.418498999412719POLYGON ICRS  -148.867037000119 54.397070999746219  -148.88046100015612 54.4034949998304  -148.89148399964731 54.395677999867026  -148.8780599997574 54.389256999588362  -148.867037000119 54.397070999746219","_id_":"ext-record-1993"},
{"footprint":"POLYGON ICRS -149.4897510000479 54.421914999947731  210.49858 54.429409  -149.50226072432991 54.4289661159666  -149.50285399969309 54.429346999937934  -149.51574299954311 54.422554999868865  -149.50406899986882 54.415061000004705  -149.50322830068595 54.415504263564848  210.497364 54.415124  -149.4897510000479 54.421914999947731","_id_":"ext-record-1994"},
{"footprint":"POLYGON ICRS -149.4897510000479 54.421914999947731  210.49858 54.429409  -149.50226072432991 54.4289661159666  -149.50285399969309 54.429346999937934  -149.51574299954311 54.422554999868865  -149.50406899986882 54.415061000004705  -149.50322830068595 54.415504263564848  210.497364 54.415124  -149.4897510000479 54.421914999947731","_id_":"ext-record-1995"},
{"footprint":"POLYGON ICRS -149.43531500060092 54.4759499996718  -149.45797621858748 54.490374619870792  -149.45773099873588 54.490506000526956  -149.48354100066754 54.506756000736182  -149.48364844119047 54.506698430044764  -149.50856345974262 54.493342330800118  -149.50969394069057 54.492735989896211  -149.51153399983448 54.491748999269973  -149.4983923692082 54.483474699697609  -149.5095710005028 54.477592000830917  -149.50014074894608 54.471546420410007  -149.5145579998285 54.464046999256347  -149.48916599893539 54.447565000270892  -149.46077300035603 54.462332000663366  -149.46087120934436 54.462395790304669  -149.43531500060092 54.4759499996718","_id_":"ext-record-1996"},
{"footprint":"POLYGON ICRS -149.43531500060092 54.4759499996718  -149.45797621858748 54.490374619870792  -149.45773099873588 54.490506000526956  -149.48354100066754 54.506756000736182  -149.48364844119047 54.506698430044764  -149.50856345974262 54.493342330800118  -149.50969394069057 54.492735989896211  -149.51153399983448 54.491748999269973  -149.4983923692082 54.483474699697609  -149.5095710005028 54.477592000830917  -149.50014074894608 54.471546420410007  -149.5145579998285 54.464046999256347  -149.48916599893539 54.447565000270892  -149.46077300035603 54.462332000663366  -149.46087120934436 54.462395790304669  -149.43531500060092 54.4759499996718","_id_":"ext-record-1997"},
{"footprint":"POLYGON ICRS -149.43603300070262 54.47591900078708  -149.45869421035397 54.490343610339849  -149.45844900000085 54.490475000085368  210.51574 54.506725  210.5156331 54.50666772  -149.48467439249572 54.506502963283886  -149.48497799950854 54.5066940007632  -149.48508613117619 54.50663605959339  -149.51000053890871 54.493279810429428  -149.51113177127382 54.492673049565056  -149.51296999907322 54.491686999981084  -149.49982778086786 54.483412140130753  -149.51100699915872 54.47752999991355  -149.50157499034947 54.471483809518233  -149.51599300010105 54.463983999253294  -149.4905999995373 54.447501999786866  -149.49017461941438 54.44772338853695  210.510117 54.447534  -149.46149100124629 54.462300999972776  -149.46158921022257 54.462364790438791  -149.43603300070262 54.47591900078708","_id_":"ext-record-1998"},
{"footprint":"POLYGON ICRS -149.43603300070262 54.47591900078708  -149.45869421035397 54.490343610339849  -149.45844900000085 54.490475000085368  210.51574 54.506725  210.5156331 54.50666772  -149.48467439249572 54.506502963283886  -149.48497799950854 54.5066940007632  -149.48508613117619 54.50663605959339  -149.51000053890871 54.493279810429428  -149.51113177127382 54.492673049565056  -149.51296999907322 54.491686999981084  -149.49982778086786 54.483412140130753  -149.51100699915872 54.47752999991355  -149.50157499034947 54.471483809518233  -149.51599300010105 54.463983999253294  -149.4905999995373 54.447501999786866  -149.49017461941438 54.44772338853695  210.510117 54.447534  -149.46149100124629 54.462300999972776  -149.46158921022257 54.462364790438791  -149.43603300070262 54.47591900078708","_id_":"ext-record-1999"},
{"footprint":"POLYGON ICRS -148.87102899892608 54.418498999153485  -148.88446099880352 54.424923000158834  -148.89548900055985 54.4171059985831  -148.88877247558887 54.413896189397811  -148.88205699700873 54.410685998720112  -148.87102899892608 54.418498999153485","_id_":"ext-record-2000"},
{"footprint":"POLYGON ICRS -148.87102899929198 54.41849899915259  -148.87102900049618 54.418498999728676  -148.8710290002623 54.418498999894332  -148.87505142406528 54.420423135409187  -148.88446099880352 54.424923000158834  -148.88446099966481 54.42492299985954  211.115539 54.424923  -148.8954889997072 54.41710600009575  -148.89548899866108 54.417105999595876  -148.89548900032648 54.417105998415089  -148.89240712371409 54.415633286539965  211.117943 54.410686  -148.88205700015405 54.410686000130546  -148.88205699700873 54.410685998720112  -148.87102899929198 54.41849899915259","_id_":"ext-record-2001"},
{"footprint":"POLYGON ICRS -148.8710289994051 54.418498999283983  -148.87102900049618 54.418498999728676  -148.87102900037544 54.418499000025733  -148.8751460193991 54.420468381475686  -148.88446099880352 54.424923000158834  -148.88446099966484 54.424922999859547  -148.88446100041762 54.424922999904517  -148.89548899987238 54.417106000349996  -148.89548899977021 54.41710600027195  -148.89548900018798 54.417105999975739  -148.89548899913316 54.4171059994717  -148.89548900049164 54.417105998669342  -148.89240712371409 54.415633286539965  -148.88205699995456 54.410685999991287  -148.88205699700873 54.410685998720112  -148.8710289994051 54.418498999283983","_id_":"ext-record-2002"},
{"footprint":"POLYGON ICRS -149.33498099912691 54.533652999853963  -149.35767473984365 54.5480775095642  -149.3574289997571 54.548208999439076  210.616724 54.564458  210.61661692 54.56440071  -149.384105957883 54.564013960481304  -149.38471400103961 54.564396000547092  -149.38482107046849 54.564338709669286  -149.40977177875425 54.550982089593063  -149.41090462070423 54.550375330899449  -149.41274600122571 54.549388999895584  -149.39958564041819 54.541114769899046  -149.41078000188324 54.535232000188472  -149.40133551928412 54.52918690032466  -149.41577400158516 54.521687000848551  -149.39034499994034 54.505204999182311  -149.38949414847585 54.505647150469279  210.611092 54.505267  -149.36047499926113 54.520034000206472  -149.360574290162 54.520098400760553  -149.33498099912691 54.533652999853963","_id_":"ext-record-2003"},
{"footprint":"POLYGON ICRS -149.33498099912691 54.533652999853963  -149.35767473984365 54.5480775095642  -149.3574289997571 54.548208999439076  210.616724 54.564458  210.61661692 54.56440071  -149.384105957883 54.564013960481304  -149.38471400103961 54.564396000547092  -149.38482107046849 54.564338709669286  -149.40977177875425 54.550982089593063  -149.41090462070423 54.550375330899449  -149.41274600122571 54.549388999895584  -149.39958564041819 54.541114769899046  -149.41078000188324 54.535232000188472  -149.40133551928412 54.52918690032466  -149.41577400158516 54.521687000848551  -149.39034499994034 54.505204999182311  -149.38949414847585 54.505647150469279  210.611092 54.505267  -149.36047499926113 54.520034000206472  -149.360574290162 54.520098400760553  -149.33498099912691 54.533652999853963","_id_":"ext-record-2004"},
{"footprint":"CIRCLE ICRS 209.93219466  53.98916758 0.625","_id_":"ext-record-2005"},
{"footprint":"CIRCLE ICRS 210.90789448  54.57587474 0.625","_id_":"ext-record-2006"},
{"footprint":"CIRCLE ICRS 210.80964545  54.35892400 0.625","_id_":"ext-record-2007"},
{"footprint":"CIRCLE ICRS 210.80920049  54.36298741 0.625","_id_":"ext-record-2008"},
{"footprint":"CIRCLE ICRS 210.98052835  53.66914911 0.625","_id_":"ext-record-2009"},
{"footprint":"CIRCLE ICRS 211.25948613  53.67449567 0.625","_id_":"ext-record-2010"},
{"footprint":"POLYGON ICRS 210.84249967 54.378333  210.84247684 54.37876854  210.84240862 54.3791993  210.84229574 54.37962057  210.84213944 54.38002774  210.84194144 54.38041633  210.8417039 54.38078211  210.84142944 54.38112104  210.84112104 54.38142944  210.84078211 54.3817039  210.84041633 54.38194144  210.84002774 54.38213944  210.83962057 54.38229574  210.8391993 54.38240862  210.83876854 54.38247684  210.838333 54.38249967  210.83789746 54.38247684  210.8374667 54.38240862  210.83704543 54.38229574  210.83663826 54.38213944  210.83624967 54.38194144  210.83588389 54.3817039  210.83554496 54.38142944  210.83523656 54.38112104  210.8349621 54.38078211  210.83472456 54.38041633  210.83452656 54.38002774  210.83437026 54.37962057  210.83425738 54.3791993  210.83418916 54.37876854  210.83416633 54.378333  210.83418916 54.37789746  210.83425738 54.3774667  210.83437026 54.37704543  210.83452656 54.37663826  210.83472456 54.37624967  210.8349621 54.37588389  210.83523656 54.37554496  210.83554496 54.37523656  210.83588389 54.3749621  210.83624967 54.37472456  210.83663826 54.37452656  210.83704543 54.37437026  210.8374667 54.37425738  210.83789746 54.37418916  210.838333 54.37416633  210.83876854 54.37418916  210.8391993 54.37425738  210.83962057 54.37437026  210.84002774 54.37452656  210.84041633 54.37472456  210.84078211 54.3749621  210.84112104 54.37523656  210.84142944 54.37554496  210.8417039 54.37588389  210.84194144 54.37624967  210.84213944 54.37663826  210.84229574 54.37704543  210.84240862 54.3774667  210.84247684 54.37789746  210.84249967 54.378333","_id_":"ext-record-2011"},
{"footprint":"POLYGON ICRS 210.80666667 54.298611  210.80664384 54.29904654  210.80657562 54.2994773  210.80646274 54.29989857  210.80630644 54.30030574  210.80610844 54.30069433  210.8058709 54.30106011  210.80559644 54.30139904  210.80528804 54.30170744  210.80494911 54.3019819  210.80458333 54.30221944  210.80419474 54.30241744  210.80378757 54.30257374  210.8033663 54.30268662  210.80293554 54.30275484  210.8025 54.30277767  210.80206446 54.30275484  210.8016337 54.30268662  210.80121243 54.30257374  210.80080526 54.30241744  210.80041667 54.30221944  210.80005089 54.3019819  210.79971196 54.30170744  210.79940356 54.30139904  210.7991291 54.30106011  210.79889156 54.30069433  210.79869356 54.30030574  210.79853726 54.29989857  210.79842438 54.2994773  210.79835616 54.29904654  210.79833333 54.298611  210.79835616 54.29817546  210.79842438 54.2977447  210.79853726 54.29732343  210.79869356 54.29691626  210.79889156 54.29652767  210.7991291 54.29616189  210.79940356 54.29582296  210.79971196 54.29551456  210.80005089 54.2952401  210.80041667 54.29500256  210.80080526 54.29480456  210.80121243 54.29464826  210.8016337 54.29453538  210.80206446 54.29446716  210.8025 54.29444433  210.80293554 54.29446716  210.8033663 54.29453538  210.80378757 54.29464826  210.80419474 54.29480456  210.80458333 54.29500256  210.80494911 54.2952401  210.80528804 54.29551456  210.80559644 54.29582296  210.8058709 54.29616189  210.80610844 54.29652767  210.80630644 54.29691626  210.80646274 54.29732343  210.80657562 54.2977447  210.80664384 54.29817546  210.80666667 54.298611","_id_":"ext-record-2012"},
{"footprint":"POLYGON ICRS 210.83979167 54.405  210.83976884 54.40543554  210.83970062 54.4058663  210.83958774 54.40628757  210.83943144 54.40669474  210.83923344 54.40708333  210.8389959 54.40744911  210.83872144 54.40778804  210.83841304 54.40809644  210.83807411 54.4083709  210.83770833 54.40860844  210.83731974 54.40880644  210.83691257 54.40896274  210.8364913 54.40907562  210.83606054 54.40914384  210.835625 54.40916667  210.83518946 54.40914384  210.8347587 54.40907562  210.83433743 54.40896274  210.83393026 54.40880644  210.83354167 54.40860844  210.83317589 54.4083709  210.83283696 54.40809644  210.83252856 54.40778804  210.8322541 54.40744911  210.83201656 54.40708333  210.83181856 54.40669474  210.83166226 54.40628757  210.83154938 54.4058663  210.83148116 54.40543554  210.83145833 54.405  210.83148116 54.40456446  210.83154938 54.4041337  210.83166226 54.40371243  210.83181856 54.40330526  210.83201656 54.40291667  210.8322541 54.40255089  210.83252856 54.40221196  210.83283696 54.40190356  210.83317589 54.4016291  210.83354167 54.40139156  210.83393026 54.40119356  210.83433743 54.40103726  210.8347587 54.40092438  210.83518946 54.40085616  210.835625 54.40083333  210.83606054 54.40085616  210.8364913 54.40092438  210.83691257 54.40103726  210.83731974 54.40119356  210.83770833 54.40139156  210.83807411 54.4016291  210.83841304 54.40190356  210.83872144 54.40221196  210.8389959 54.40255089  210.83923344 54.40291667  210.83943144 54.40330526  210.83958774 54.40371243  210.83970062 54.4041337  210.83976884 54.40456446  210.83979167 54.405","_id_":"ext-record-2013"},
{"footprint":"POLYGON ICRS 210.92625 54.31805556  210.92622717 54.31849109  210.92615895 54.31892185  210.92604607 54.31934313  210.92588977 54.31975029  210.92569177 54.32013889  210.92545424 54.32050466  210.92517977 54.3208436  210.92487138 54.32115199  210.92453244 54.32142646  210.92416667 54.32166399  210.92377807 54.321862  210.9233709 54.32201829  210.92294963 54.32213117  210.92251887 54.3221994  210.92208333 54.32222222  210.9216478 54.3221994  210.92121703 54.32213117  210.92079576 54.32201829  210.9203886 54.321862  210.92 54.32166399  210.91963423 54.32142646  210.91929529 54.32115199  210.9189869 54.3208436  210.91871243 54.32050466  210.91847489 54.32013889  210.91827689 54.31975029  210.9181206 54.31934313  210.91800772 54.31892185  210.91793949 54.31849109  210.91791667 54.31805556  210.91793949 54.31762002  210.91800772 54.31718926  210.9181206 54.31676798  210.91827689 54.31636082  210.91847489 54.31597222  210.91871243 54.31560645  210.9189869 54.31526751  210.91929529 54.31495912  210.91963423 54.31468465  210.92 54.31444712  210.9203886 54.31424912  210.92079576 54.31409282  210.92121703 54.31397994  210.9216478 54.31391171  210.92208333 54.31388889  210.92251887 54.31391171  210.92294963 54.31397994  210.9233709 54.31409282  210.92377807 54.31424912  210.92416667 54.31444712  210.92453244 54.31468465  210.92487138 54.31495912  210.92517977 54.31526751  210.92545424 54.31560645  210.92569177 54.31597222  210.92588977 54.31636082  210.92604607 54.31676798  210.92615895 54.31718926  210.92622717 54.31762002  210.92625 54.31805556","_id_":"ext-record-2014"},
{"footprint":"POLYGON ICRS 210.62333367 54.269722  210.62331084 54.27015754  210.62324262 54.2705883  210.62312974 54.27100957  210.62297344 54.27141674  210.62277544 54.27180533  210.6225379 54.27217111  210.62226344 54.27251004  210.62195504 54.27281844  210.62161611 54.2730929  210.62125033 54.27333044  210.62086174 54.27352844  210.62045457 54.27368474  210.6200333 54.27379762  210.61960254 54.27386584  210.619167 54.27388867  210.61873146 54.27386584  210.6183007 54.27379762  210.61787943 54.27368474  210.61747226 54.27352844  210.61708367 54.27333044  210.61671789 54.2730929  210.61637896 54.27281844  210.61607056 54.27251004  210.6157961 54.27217111  210.61555856 54.27180533  210.61536056 54.27141674  210.61520426 54.27100957  210.61509138 54.2705883  210.61502316 54.27015754  210.61500033 54.269722  210.61502316 54.26928646  210.61509138 54.2688557  210.61520426 54.26843443  210.61536056 54.26802726  210.61555856 54.26763867  210.6157961 54.26727289  210.61607056 54.26693396  210.61637896 54.26662556  210.61671789 54.2663511  210.61708367 54.26611356  210.61747226 54.26591556  210.61787943 54.26575926  210.6183007 54.26564638  210.61873146 54.26557816  210.619167 54.26555533  210.61960254 54.26557816  210.6200333 54.26564638  210.62045457 54.26575926  210.62086174 54.26591556  210.62125033 54.26611356  210.62161611 54.2663511  210.62195504 54.26662556  210.62226344 54.26693396  210.6225379 54.26727289  210.62277544 54.26763867  210.62297344 54.26802726  210.62312974 54.26843443  210.62324262 54.2688557  210.62331084 54.26928646  210.62333367 54.269722","_id_":"ext-record-2015"},
{"footprint":"POLYGON ICRS 211.12236078 54.396944  211.12234556 54.39723436  211.12230008 54.39752153  211.12222482 54.39780238  211.12212063 54.39807382  211.12198863 54.39833289  211.12183027 54.39857674  211.12164729 54.3988027  211.1214417 54.39900829  211.12121574 54.39919127  211.12097189 54.39934963  211.12071282 54.39948163  211.12044138 54.39958582  211.12016053 54.39966108  211.11987336 54.39970656  211.119583 54.39972178  211.11929264 54.39970656  211.11900547 54.39966108  211.11872462 54.39958582  211.11845318 54.39948163  211.11819411 54.39934963  211.11795026 54.39919127  211.1177243 54.39900829  211.11751871 54.3988027  211.11733573 54.39857674  211.11717737 54.39833289  211.11704537 54.39807382  211.11694118 54.39780238  211.11686592 54.39752153  211.11682044 54.39723436  211.11680522 54.396944  211.11682044 54.39665364  211.11686592 54.39636647  211.11694118 54.39608562  211.11704537 54.39581418  211.11717737 54.39555511  211.11733573 54.39531126  211.11751871 54.3950853  211.1177243 54.39487971  211.11795026 54.39469673  211.11819411 54.39453837  211.11845318 54.39440637  211.11872462 54.39430218  211.11900547 54.39422692  211.11929264 54.39418144  211.119583 54.39416622  211.11987336 54.39418144  211.12016053 54.39422692  211.12044138 54.39430218  211.12071282 54.39440637  211.12097189 54.39453837  211.12121574 54.39469673  211.1214417 54.39487971  211.12164729 54.3950853  211.12183027 54.39531126  211.12198863 54.39555511  211.12212063 54.39581418  211.12222482 54.39608562  211.12230008 54.39636647  211.12234556 54.39665364  211.12236078 54.396944","_id_":"ext-record-2016"},
{"footprint":"POLYGON ICRS 211.12236078 54.396944  211.12234556 54.39723436  211.12230008 54.39752153  211.12222482 54.39780238  211.12212063 54.39807382  211.12198863 54.39833289  211.12183027 54.39857674  211.12164729 54.3988027  211.1214417 54.39900829  211.12121574 54.39919127  211.12097189 54.39934963  211.12071282 54.39948163  211.12044138 54.39958582  211.12016053 54.39966108  211.11987336 54.39970656  211.119583 54.39972178  211.11929264 54.39970656  211.11900547 54.39966108  211.11872462 54.39958582  211.11845318 54.39948163  211.11819411 54.39934963  211.11795026 54.39919127  211.1177243 54.39900829  211.11751871 54.3988027  211.11733573 54.39857674  211.11717737 54.39833289  211.11704537 54.39807382  211.11694118 54.39780238  211.11686592 54.39752153  211.11682044 54.39723436  211.11680522 54.396944  211.11682044 54.39665364  211.11686592 54.39636647  211.11694118 54.39608562  211.11704537 54.39581418  211.11717737 54.39555511  211.11733573 54.39531126  211.11751871 54.3950853  211.1177243 54.39487971  211.11795026 54.39469673  211.11819411 54.39453837  211.11845318 54.39440637  211.11872462 54.39430218  211.11900547 54.39422692  211.11929264 54.39418144  211.119583 54.39416622  211.11987336 54.39418144  211.12016053 54.39422692  211.12044138 54.39430218  211.12071282 54.39440637  211.12097189 54.39453837  211.12121574 54.39469673  211.1214417 54.39487971  211.12164729 54.3950853  211.12183027 54.39531126  211.12198863 54.39555511  211.12212063 54.39581418  211.12222482 54.39608562  211.12230008 54.39636647  211.12234556 54.39665364  211.12236078 54.396944","_id_":"ext-record-2017"}
]};
