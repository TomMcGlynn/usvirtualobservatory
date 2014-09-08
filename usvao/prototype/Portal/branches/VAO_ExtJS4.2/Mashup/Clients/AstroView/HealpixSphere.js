/////////////////////////////
// HealpixSphere()
/////////////////////////////
ASTROVIEW.HealpixSphere = function ( radius )
{ 
    THREE.Object3D.call( this );
    this.name = "HealpixSphere";
    this.matrixAutoUpdate = false;
    this.createSphere( radius );
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
        console.log("*** level: " + level + " step:" + step + " corners:" + corners.length);
        this.dump(corners);
    }
}

ASTROVIEW.HealpixSphere.prototype.dump = function(corners)
{
    for (var i=0; i<corners.length; i++)
    {
        var c = corners[i];
        console.log("[x:" + c.x.toFixed(3) + " y:" + c.y.toFixed(3) + " z:" + c.z.toFixed(3) + "]");
    }
}
 
ASTROVIEW.HealpixSphere.prototype.createSphere = function( radius )
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
        var dg = new ASTROVIEW.HealpixGeometry(uv, 0, 1, pixel, zlevel, pixel, radius );
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

