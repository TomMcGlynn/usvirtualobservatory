//////////////////////////
// HealpixGeometry()
//////////////////////////
ASTROVIEW.HealpixGeometry = function(uv, tx, ty, pixel12, zlevel, pixel, radius)
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
    this.dynamic = true;
    
    // Texture Attributes (used by Diamond.js)
    this.side = THREE.FrontSide;                               // THREE.FrontSide, THREE.BackSide and THREE.DoubleSide; 
    this.overdraw = true;                                      // Overdraw image over edge of Diamond
    
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
        var fuv = this.getFaceUV();             // get Face UV[] array based on this.vertices and pixel12
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

ASTROVIEW.HealpixGeometry.faceUV = ASTROVIEW.HealpixGeometry.faceUV || 
    [uv11, uv10, uv00, uv01];       //  Works with Aladin Healpix Jpg Images INSIDE Sphere
    // [uv00, uv10, uv11, uv01];    //  Works with Dummy Images INSIDE Sphere
    // [uv00, uv01, uv11, uv10];    //  Works with Dummy Images OUTSIDE Sphere

////////////////////////////////
// getFaceUV() 
////////////////////////////////
ASTROVIEW.HealpixGeometry.prototype.getFaceUV = function()
{   
    return ASTROVIEW.HealpixGeometry.faceUV;
}

//////////////////////////
// clean()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.clean = function()
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

ASTROVIEW.HealpixGeometry.clear = ["FF0000",
                                  "FA4400",
                                  "FD7E00",
                                  "FEB300",
                                  "FFFF00",
                                  "00E000",
                                  "007C00",
                                  "00AEAE",
                                  "0000FF",
                                  "7308A5",
                                  "BA00FF",
                                  "CC00AF"];

//////////////////////////
// getImageUrl()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.getImageUrl = function(baseurl)
{    
    // Healpix Url
    var dir = new String(parseInt(this.pixel/10000) * 10000);
    var url = new String(baseurl).replace("[LEVEL]", this.zlevel).replace("[DIR]", dir).replace("[PIXEL]", this.pixel).replace("[PIXEL12]", this.pixel12);
    
    // Remote Dummy Images
    var proxyurl = "http://masttest.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";
    var dummyurl = "http://dummyimage.com/256x256/" + "00ff00" + "/000000%26text=%5B" + this.zlevel + "," + this.pixel12 + "," + this.pixel + "%5D";  
        
    return url;
}
     
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
    var hpi = new HEALPIX.HealpixIndex(null, zlevel);
    
    // Create the child HealpixGeometry Objects
    var dgs=[];
    for (var pixel=pixel4; pixel < pixel4+4; pixel++)
    {
        var corners = hpi.corners_nest(pixel); 
        var uv = this.cornersToVector3(corners);
        var dg = new ASTROVIEW.HealpixGeometry(uv, 0, 1, this.pixel12, zlevel, pixel, radius);
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
        //m90.multiplyVector3(v);
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
    //var sphere = this.sphereInFrustum(frustum);
    //var vertices = this.verticesInFrustum(frustum);
    return (faces); 
};

//////////////////////////
// facesInFrustrum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.facesInFrustum = function(frustum)
{
    var inFrustum = this.faceInFrustum(this.faces[0], frustum) ||
                    this.faceInFrustum(this.faces[1], frustum);
                      
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
}

//////////////////////////
// computeDiamondCentroid()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.computeDiamondCentroid = function()    
{
    if (!this.__centroid)
    {
        if (!this.vertices || this.vertices.length !== 4) return null;
     
        var v0 = this.vertices[0];
        var v1 = this.vertices[1];
        var v2 = this.vertices[2];
        var v3 = this.vertices[3];
     
        var x = (v0.x + v1.x + v2.x + v3.x)/4.0;
        var y = (v0.y + v1.y + v2.y + v3.y)/4.0;
        var z = (v0.z + v1.z + v2.z + v3.z)/4.0;
        this.__centroid = new THREE.Vector3(x, y, z);
    }
    return this.__centroid;
};

//////////////////////////
// computeDiamondRadius()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.computeDiamondRadius = function()    
{
    if (!this.__radius)
    {
        if (!this.vertices || this.vertices.length !== 4) return null;
        if (!this.__centroid) this.computeDiamondCentroid();
     
        var v0 = this.vertices[0];
        var v1 = this.vertices[1];
        var v2 = this.vertices[2];
        var v3 = this.vertices[3];
     
        var dv0 = this.__centroid.distanceTo(v0);
        var dv1 = this.__centroid.distanceTo(v1);
        var dv2 = this.__centroid.distanceTo(v2);
        var dv3 = this.__centroid.distanceTo(v3);
     
        this.__radius = Math.max(dv0, dv1, dv2, dv3);
    }

    return this.__radius;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.sphereInFrustum = function(frustum)
{
    if (!this.__centroid) this.__centroid = this.computeDiamondCentroid();
    if (!this.__radius) this.__radius = this.computeDiamondRadius();
    
    var distance;
    var planes = frustum.planes;
    for ( var i=0; i<6; i++ )
    {
        distance = planes[i].x * this.__centroid.x +
                   planes[i].y * this.__centroid.y +
                   planes[i].z * this.__centroid.z +
                   planes[i].w;
        
        if (distance < -this.__radius)
        {
            return false;
        }
    }

    return true;
};

//////////////////////////
// verticesInFrustum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.verticesInFrustum = function(frustum)
{
    for (var i=0; i<this.vertices.length; i++)
    {
        var v=this.vertices[i];
        if (this.pointInFrustum(v, frustum))
        {
            return true;
        }
    }
    return false;
};

//////////////////////////
// pointInFrustrum()
//////////////////////////
ASTROVIEW.HealpixGeometry.prototype.pointInFrustum = function(v, frustum)  // THREE.Vector3, THREE.Frustum
{
    var planes = frustum.planes;
    
    for (var p=0; p<6; p++ )
    {
        //
        // Note that for an arbitary point (x,y,z), the distance to the plane is:
        // d = A*x + B*y + C*z + D
        //
        if ((planes[p].x * v.x) + (planes[p].y * v.y) + (planes[p].z * v.z) + planes[p].w <= 0)
        {
            return false;
        }
    }
    return true;
};
