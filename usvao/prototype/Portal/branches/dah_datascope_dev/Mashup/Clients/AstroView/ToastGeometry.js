//////////////////////////
// ToastGeometry()
//////////////////////////
ASTROVIEW.ToastGeometry = function(uv, tx, ty, quadrant, zlevel, did, radius)
{
    THREE.Geometry.call( this );

    this.uv = uv;                                            // Diamond Vertices Unit Vectors 
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.zlevel = zlevel;
    this.did = did;                                          // Diamond ID: 0 - 3
    this.name = "[" + zlevel + ":" + tx + "," + ty + "]";    // Toast ID: [zoom: tx, ty]
    this.radius = radius;
    this.dynamic = true;
    
    // Texture Attributes (used by Diamond.js)
    this.side = THREE.BackSide;                             // THREE.FrontSide, THREE.BackSide and THREE.DoubleSide; 
    this.overdraw = true;                                   // Overdraw image over edge of Diamond
    
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
        var fuv = this.getFaceUV(quadrant);
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

ASTROVIEW.ToastGeometry.faceUV = ASTROVIEW.ToastGeometry.faceUV || 
{
    "I"  :[uv10, uv11, uv01, uv00],
    "II" :[uv00, uv10, uv11, uv01],
    "III":[uv01, uv00, uv10, uv11],
    "IV" :[uv11, uv01, uv00, uv10]
}

////////////////////////////////
// getFaceUV() 
////////////////////////////////
ASTROVIEW.ToastGeometry.prototype.getFaceUV = function(quadrant)
{   
    return ASTROVIEW.ToastGeometry.faceUV[quadrant];
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

//////////////////////////
// getImageUrl()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.getImageUrl = function(baseurl)
{
    var url = new String(baseurl).replace("[LEVEL]", this.zlevel).replace("[TX]", this.tx).replace("[TY]", this.ty).replace("[LEVEL]", this.zlevel).replace("[TX]", this.tx).replace("[TY]", this.ty);   
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
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 0, radius); // (x, 0)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 1, radius); // (x, y)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 2, radius); // (0, y)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 3, radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 0, radius); // (0, 0)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 1, radius); // (x, 0)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 2, radius); // (x, y)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 3, radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 0, radius); // (0, y)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 1, radius); // (0, 0)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 2, radius); // (x, 0)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 3, radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.ToastGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, zl, 0, radius); // (x, y)
                    dg1 = new ASTROVIEW.ToastGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2+y, dg.quadrant, zl, 1, radius); // (0, y)
                    dg2 = new ASTROVIEW.ToastGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2,   dg.quadrant, zl, 2, radius); // (0, 0)
                    dg3 = new ASTROVIEW.ToastGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2,   dg.quadrant, zl, 3, radius); // (x, 0)
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
    //var sphere = this.sphereInFrustum(frustum);
    //var vertices = this.verticesInFrustum(frustum);
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
}

//////////////////////////
// computeDiamondCentroid()
//////////////////////////
ASTROVIEW.ToastGeometry.prototype.computeDiamondCentroid = function()    
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
ASTROVIEW.ToastGeometry.prototype.computeDiamondRadius = function()    
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
ASTROVIEW.ToastGeometry.prototype.sphereInFrustum = function(frustum)
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
ASTROVIEW.ToastGeometry.prototype.verticesInFrustum = function(frustum)
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
ASTROVIEW.ToastGeometry.prototype.pointInFrustum = function(v, frustum)  // THREE.Vector3, THREE.Frustum
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
