
//////////////////////////
// DiamondGeometry()
//////////////////////////
ASTROVIEW.DiamondGeometry = function(u, tx, ty, quadrant, color, zlevel, did, radius)
{
    THREE.Geometry.call( this );

    this.u = u;	
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.color = color;
    this.zlevel = zlevel;
    this.did = did;                                                 // Diamond ID: 0 - 3
    this.tid = "[z,x,y]:[" + zlevel + "," + tx + "," + ty + "]";    // Toast ID: [zoom, tx, ty]
    this.radius = radius;
    this.diamonds=[];
    this.dynamic = true;
    
    // Create this.vertices[] array from u[] unit vector array
    if (u && u.length == 4)
    {
        for (var i=0; i<4; i++)
        {
            var v = this.scaleV(u[i], radius);
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length == 4)
    {
        var uv = this.createUV(quadrant);		// create uv[] array based on quadrant
        this.createFaces(this.vertices, uv);	// create this.faces[] based on this.vertices[] and uv[]
        this.computeCentroids();                // inherited call
    }
};

ASTROVIEW.DiamondGeometry.prototype = new THREE.Geometry();
ASTROVIEW.DiamondGeometry.prototype.constructor = ASTROVIEW.DiamondGeometry;

//////////////////////////
// createFaces()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.createFaces = function(v, uv)
{
    if ((v && v.length == 4) && (uv && uv.length == 4))
    {
        var normal = new THREE.Vector3( 0, 0, 1 );
        
        //
        // Create 'Upper' Face 0 (v0 -> v3 -> v1)
        //  
        var face0 = new THREE.Face3( 0, 3, 1);
        face0.normal.copy( normal );
        face0.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face0 );
        this.faceVertexUvs[ 0 ].push( [ uv[0], uv[3], uv[1] ]);
        
        //
        // Create 'Lower' Face 1 (v2 -> v1 -> v3)
        //
        var face1 = new THREE.Face3( 2, 1, 3 );
        face1.normal.copy( normal );
        face1.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face1 );
        this.faceVertexUvs[ 0 ].push( [ uv[2], uv[1], uv[3] ]);
    }
};

//////////////////////////
// createUV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.createUV = function(quadrant)
{	
    var uv = new Array(4);
    switch (quadrant)
    {
        case "I":
        {
            uv[0] =  new THREE.UV( 1, 0 ); 
            uv[1] =  new THREE.UV( 1, 1 ); 
            uv[2] =  new THREE.UV( 0, 1 ); 
            uv[3] =  new THREE.UV( 0, 0 ); 
            break;
        }
        case "II":
        {
            uv[0] =  new THREE.UV( 0, 0 ); 
            uv[1] =  new THREE.UV( 1, 0 ); 
            uv[2] =  new THREE.UV( 1, 1 ); 
            uv[3] =  new THREE.UV( 0, 1 ); 	
            break;
        }
        case "III":
        {
            uv[0] =  new THREE.UV( 0, 1 ); 
            uv[1] =  new THREE.UV( 0, 0 ); 
            uv[2] =  new THREE.UV( 1, 0 ); 
            uv[3] =  new THREE.UV( 1, 1 ); 
            break;
        }
        case "IV":
        {
            uv[0] =  new THREE.UV( 1, 1 ); 
            uv[1] =  new THREE.UV( 0, 1 ); 
            uv[2] =  new THREE.UV( 0, 0 ); 
            uv[3] =  new THREE.UV( 1, 0 ); 
            break;
        }
	}
    return uv;
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.inFrustum = function(frustum)
{  
    var gcenter = this.getGeometryCenter();
    var gradius = this.getGeometryRadius();
    
    if (this.sphereInFrustum(gcenter, gradius, frustum))
    {
        return true;
    }
    else if (this.verticesInFrustum(frustum))
    {
        return true;
    }
    else
    {
        return false;
    }
};

//////////////////////////
// getCenter()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getGeometryCenter = function()	
{
    return this.midpointV(this.vertices[0], this.vertices[2]);
};

//////////////////////////
// getRadius()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getGeometryRadius = function()	
{
    if (!this.gradius)
    {
        var v0 = this.vertices[0].position;
        var v1 = this.vertices[1].position;
        var v2 = this.vertices[2].position;
        var v3 = this.vertices[3].position;
        
        var dv0v1 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v1);
        var dv0v2 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v2);
        var dv0v3 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v3);
        var dv1v2 = new THREE.Vector3(v1.x, v1.y, v1.z).distanceTo(v2);
        var dv1v3 = new THREE.Vector3(v1.x, v1.y, v1.z).distanceTo(v3);
        var dv2v3 = new THREE.Vector3(v2.x, v2.y, v2.z).distanceTo(v3);
        
        this.gradius = Math.max(dv0v1, dv0v2, dv0v3, dv1v2, dv1v3, dv2v3)/2.5;
    }
    return this.gradius;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.sphereInFrustum = function(center, radius, frustum)
{
    var distance;
    var planes = frustum.planes;
    for ( var i=0; i<6; i++ )
    {
        distance = planes[i].x * center.position.x +
                   planes[i].y * center.position.y +
                   planes[i].z * center.position.z +
                   planes[i].w;
        
        if (distance < -radius)
        {
            return false;
        }
    }

    return true;
};

//////////////////////////
// pointsInFrustrum()
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
    //      NOTE: Diamond.u[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //	 	Make new points on the Diamond (a, b, c, d, e):
    //
    //      a = (u0+u1)/2
    //      b = (u0+u2)/2
    //      c = (u2+u3)/2
    //      d = (u3+u0)/2
    //      e = (u3+u1)/2
    //
    //         u0
    //         /\		    
    //        /  \
    //     d / D0 \ a         Construct 4 new diamonds    
    //      /\    /\            D0 = [u0, a,  e,  d]       
    //     /  \ e/  \           D1 = [ a,u1,  b,  e]       
    // u3 / D3 \/ D1 \ u1       D2 = [ e, b, u2,  c]       
    //    \    /\    /          D3 = [ d, e,  c, u3]      
    //     \  /  \  /
    //    c \/ D2 \/ b
    //       \    /
    //        \  /
    //         \/
    //         u2
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
            var dg = loopArray[i];
            var u = dg.u;
            
            //
            //	 Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = this.midpointV(u[0], u[1]); a = this.normalizeV(a);   // THREE.Vertex
            var b = this.midpointV(u[1], u[2]); b = this.normalizeV(b);   // THREE.Vertex
            var c = this.midpointV(u[2], u[3]); c = this.normalizeV(c);   // THREE.Vertex
            var d = this.midpointV(u[3], u[0]); d = this.normalizeV(d);   // THREE.Vertex
            var e = this.midpointV(u[3], u[1]); e = this.normalizeV(e);   // THREE.Vertex
            
            //
            //   Construct new diamonds
            //
            //	 [u0,  a,  e,  d]
            //	 [ a, u1,  b,  e]
            //	 [ e,  b, u2,  c]
            //   [ d,  e,  c, u3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.DiamondGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, 0)
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
// scaleV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.scaleV = function(v, radius)
{
    var v3 = new THREE.Vector3(v.position.x, v.position.y, v.position.z);
    v3.x *= radius;
    v3.y *= radius;
    v3.z *= radius;
    return new THREE.Vertex(v3); 
};


//////////////////////////
// midpointV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.midpointV = function(va, vb)
{
    var v3 = new THREE.Vector3((va.position.x + vb.position.x) * 0.5,
                               (va.position.y + vb.position.y) * 0.5,
                               (va.position.z + vb.position.z) * 0.5);			
    return new THREE.Vertex(v3);  
};

//////////////////////////
// normalizeV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.normalizeV = function(v) 
{
    var v3 = new THREE.Vector3(v.position.x, v.position.y, v.position.z);
    var mag = v.position.x * v.position.x +
              v.position.y * v.position.y +
              v.position.z * v.position.z;
    
    if (mag != 0.0)
    {
        mag = 1.0/Math.sqrt(mag);
        v3.x *= mag;
        v3.y *= mag;
        v3.z *= mag;
    }
    return new THREE.Vertex(v3);
};