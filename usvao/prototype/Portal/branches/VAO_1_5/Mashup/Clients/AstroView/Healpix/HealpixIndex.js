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

