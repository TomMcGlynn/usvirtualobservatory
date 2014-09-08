
//////////////////////////
// DiamondGeometry()
//////////////////////////
ASTROVIEW.DiamondGeometry = function(uv, tx, ty, quadrant, color, zlevel, did, radius)
{
    THREE.Geometry.call( this );

    this.uv = uv;	
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.color = color;
    this.zlevel = zlevel;
    this.did = did;                                         // Diamond ID: 0 - 3
    this.tid = "[" + zlevel + "," + tx + "," + ty + "]";    // Toast ID: [zoom, tx, ty]
    this.radius = radius;
    this.dynamic = true;
	
	// Geometry Center and Radius: must be initialized to null to be computed once only
	this.gcenter = null;
	this.gradius = null;
    
    // Create this.vertices[] array from uv[] unit vector3 array
    if (uv && uv.length == 4)
    {
        for (var i=0; i<4; i++)
        {
			// Create new Vertex from the cloned Unit Vector.  Then scale it to the Sphere Radius
			var v = new THREE.Vertex(uv[i].clone());
			v.position.multiplyScalar(radius);   
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length == 4)
    {
        var fuv = this.getFaceUV(quadrant);		// get Face uv[] array based on quadrant
        this.createFaces(this.vertices, fuv);	// create this.faces[] based on this.vertices[] and Face UV[]
        this.computeCentroids();                // inherited call
    }
};

ASTROVIEW.DiamondGeometry.prototype = new THREE.Geometry();
ASTROVIEW.DiamondGeometry.prototype.constructor = ASTROVIEW.DiamondGeometry;

//////////////////////////
// clean()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.clean = function()
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

ASTROVIEW.DiamondGeometry.prototype.cleanFace = function(face)
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
ASTROVIEW.DiamondGeometry.prototype.createFaces = function(v, fuv)
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

const UV_00 = new THREE.UV( 0, 0 );
const UV_01 = new THREE.UV( 0, 1 );
const UV_10 = new THREE.UV( 1, 0 );
const UV_11 = new THREE.UV( 1, 1 );

const FUV_QUADRANT_I   = [UV_10, UV_11, UV_01, UV_00];
const FUV_QUADRANT_II  = [UV_00, UV_10, UV_11, UV_01];
const FUV_QUADRANT_III = [UV_01, UV_00, UV_10, UV_11];
const FUV_QUADRANT_IV  = [UV_11, UV_01, UV_00, UV_10];

//////////////////////////
// createFaceUV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getFaceUV = function(quadrant)
{	
    switch (quadrant)
    {
        case "I"  : return FUV_QUADRANT_I;
        case "II" : return FUV_QUADRANT_II;
        case "III": return FUV_QUADRANT_III;
        case "IV" : return FUV_QUADRANT_IV;
		default   : return null;
	}
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.inFrustum = function(frustum)
{
	// Calculate geometric center and radius of the diamond 'sphere' and see if it overlaps the frustum
	var center = this.getDiamondCenter();
	var radius = this.getDiamondRadius();
	return (this.sphereInFrustum(center, radius, frustum) || this.verticesInFrustum(frustum));
	
	/*
	if (this.faces && this.faces.length == 2)
	{
		return (this.faceInFrustum(this.faces[0], frustum) ||
				this.faceInFrustum(this.faces[1], frustum));
	}
	return false;
	*/
};

//////////////////////////
// getDiamondCenter()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getDiamondCenter = function()	
{
	if (!this.gcenter)
	{
		if (!this.vertices || this.vertices.length !== 4) return null;
		
		var v0 = this.vertices[0].position;
		var v1 = this.vertices[1].position;
		var v2 = this.vertices[2].position;
		var v3 = this.vertices[3].position;
		
		var x = (v0.x + v1.x + v2.x + v3.x)/4.0;
		var y = (v0.y + v1.y + v2.y + v3.y)/4.0;
		var z = (v0.z + v1.z + v2.z + v3.z)/4.0;
		this.gcenter = new THREE.Vector3(x, y, z);
	}
	return this.gcenter;
};

//////////////////////////
// getDiamondRadius()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getDiamondRadius = function()	
{
	if (!this.gradius)
	{
		if (!this.vertices || this.vertices.length !== 4) return null;
		if (!this.gcenter) this.getDiamondCenter();
		
		var v0 = this.vertices[0].position;
		var v1 = this.vertices[1].position;
		var v2 = this.vertices[2].position;
		var v3 = this.vertices[3].position;
		
		var dv0 = this.gcenter.distanceTo(v0);
		var dv1 = this.gcenter.distanceTo(v1);
		var dv2 = this.gcenter.distanceTo(v2);
		var dv3 = this.gcenter.distanceTo(v3);
		
		this.gradius = Math.max(dv0, dv1, dv2, dv3);
	}

    return this.gradius;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.sphereInFrustum = function(centerv, radius, frustum)
{
    var distance;
    var planes = frustum.planes;
    for ( var i=0; i<6; i++ )
    {
        distance = planes[i].x * centerv.x +
                   planes[i].y * centerv.y +
                   planes[i].z * centerv.z +
                   planes[i].w;
        
        if (distance < -radius)
        {
            return false;
        }
    }

    return true;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.faceInFrustum = function(face, frustum)
{
	THREE.Face3
	
	if (!face.centroid) this.computeCentroids();
	var radius = this.getFaceRadius(face);
	
	var distance;
	var planes = frustum.planes;
	for ( var i=0; i<6; i++ )
	{
		distance = planes[i].x * face.centroid.x +
				   planes[i].y * face.centroid.y +
				   planes[i].z * face.centroid.z +
				   planes[i].w;
		
		if (distance < -radius)
		{
			return false;
		}
	}

    return true;
};

ASTROVIEW.DiamondGeometry.prototype.getFaceRadius = function(face)
{
	if (!face.__radius)
	{
		var v0 = this.vertices[face.a].position;
		var v1 = this.vertices[face.b].position;
		var v2 = this.vertices[face.c].position;
		
		var dv0 = face.centroid.distanceTo(v0);
		var dv1 = face.centroid.distanceTo(v1);
		var dv2 = face.centroid.distanceTo(v2);
		
		face.__radius = Math.max(dv0, dv1, dv2);			
	}
	
	return face.__radius;
}

//////////////////////////
// verticesInFrustum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.verticesInFrustum = function(frustum)
{
    for (var i=0; i<this.vertices.length; i++)
    {
        var v3=this.vertices[i].position;
        if (this.pointInFrustum(v3, frustum))
        {
            return true;
        }
    }
    return false;
};

//////////////////////////
// pointInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.pointInFrustum = function(v3, frustum)  // THREE.Vector3, THREE.Frustum
{
    var planes = frustum.planes;
    
    for (var p=0; p<6; p++ )
    {
        //
        // Note that for an arbitary point (x,y,z), the distance to the plane is:
        // d = A*x + B*y + C*z + D
        //
        if ((planes[p].x * v3.x) + (planes[p].y * v3.y) + (planes[p].z * v3.z) + planes[p].w <= 0)
        {
            return false;
        }
    }
    return true;
};
		
//////////////////////////
// expandDiamond()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.expandDiamond = function(depth, zlevel, radius) 
{
    // Store default args
    if (!depth) depth = 1;
    if (!zlevel) zlevel = this.zlevel + 1;
    if (!radius) radius = this.radius;
    
    //
    //      Expand each Diamond by number of 'depth' passed in, starting at 'zlevel'
    //
    //      NOTE: Diamond.uv[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //	 	Make new points on the Diamond (a, b, c, d, e):
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
    for (var l=1; l<depth+1; l++)
    { 	
        var loopArray = outArray;
        outArray = [];	
        
        // 
        // Loop through all diamands to expand
        //	
        for (var i=0; i<loopArray.length; i++)
        {			
            var dg = loopArray[i];	// ASTROVIEW.DiamondGeometry
            var uv0 = dg.uv[0];
            var uv1 = dg.uv[1];
            var uv2 = dg.uv[2];
            var uv3 = dg.uv[3];
            
            //
            //	 Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = new THREE.Vector3().add(uv0, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var b = new THREE.Vector3().add(uv1, uv2).multiplyScalar(0.5).normalize();   // THREE.Vector3 
            var c = new THREE.Vector3().add(uv2, uv3).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var d = new THREE.Vector3().add(uv3, uv0).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var e = new THREE.Vector3().add(uv3, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            
            //
            //   Construct new diamonds
            //
            //	 [uv0,  a,   e,   d]
            //	 [ a, uv1,   b,   e]
            //	 [ e,   b, uv2,   c]
            //   [ d,   e,   c, uv3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.DiamondGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, 0)
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