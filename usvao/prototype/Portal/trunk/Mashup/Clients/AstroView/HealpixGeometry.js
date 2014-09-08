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
}