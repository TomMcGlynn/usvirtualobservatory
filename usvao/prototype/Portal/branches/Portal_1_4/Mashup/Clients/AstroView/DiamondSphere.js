/////////////////////////////
// DiamondSphere()
/////////////////////////////
ASTROVIEW.DiamondSphere = function (  )
{ 
    THREE.Object3D.call( this );
    this.name = "DiamondSphere"
    this.createDiamondSphere();
};

ASTROVIEW.DiamondSphere.prototype = new THREE.Object3D();
ASTROVIEW.DiamondSphere.prototype.constructor = ASTROVIEW.DiamondSphere;

/////////////////////////////
// createDiamondSphere3D()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondSphere = function()
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
    // STEP 2: Create the Top Level DiamondGeometry located in each 3D Qudrant (I, II, III, IV),
    //         mapped to a TOAST Image Coordinate [tx, ty] as shown in the Mapping above.
    //
    // Quadrant I: [+X,+Z] ===> TOAST: [0,1] 
    var dgI = new ASTROVIEW.DiamondGeometry([YY, XX, _Y, ZZ], 0, 1, "I", 0x0000ff, zlevel, 1, ASTROVIEW.RADIUS);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.DiamondGeometry([YY, _Z, _Y, XX], 1, 1, "II", 0x00ff00, zlevel, 0, ASTROVIEW.RADIUS);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.DiamondGeometry([YY, _X, _Y, _Z], 1, 0, "III", 0xff0000, zlevel, 2, ASTROVIEW.RADIUS);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.DiamondGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", 0xffff00, zlevel, 3, ASTROVIEW.RADIUS);
    
    //
    // STEP 3: Expand Each Top Level DiamondGeometry Object to Level 4 Array of DiamondGeometry[] objects
    //
    var depth = ASTROVIEW.ZOOM_LEVEL_MIN-1;      // expand 3 more levels...
    var zlevel = ASTROVIEW.ZOOM_LEVEL_MIN;       // ...to zlevel '4'

    var daI = dgI.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);            // Quadrant I
    var daII = dgII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);          // Quadrant II
    var daIII = dgIII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);        // Quadrant III
    var daIV = dgIV.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);          // Quadrant IV      
    
    //
    // STEP 4: Create Diamond objects from the DiamondGeometry[] array and
    //         add them as children to the DiamondSphere
    //
    this.createDiamondMeshMaterial(daI, null, zlevel); 
    this.createDiamondMeshMaterial(daII, null, zlevel);
    this.createDiamondMeshMaterial(daIII, null, zlevel);
    this.createDiamondMeshMaterial(daIV, null, zlevel);
}

/////////////////////////////
// createDiamondMeshMaterial()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshMaterial = function(diamonds, material, zlevel)
{
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
            this.createDiamond(dg, material);
        }
    }
}

/////////////////////////////
// createDiamondMeshColor()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshColor = function(diamonds, color, wireframe, zlevel)
{
    var material = new THREE.MeshBasicMaterial( { color: color, wireframe: wireframe } );
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
             this.addDiamond(dg, material);
        }
    }
}

/////////////////////////////
// createDiamondMeshUrl()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshUrl = function(diamonds, baseurl, zlevel)
{
    var url = new String(baseurl);

    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
            var imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);
            var material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(imageurl) } );
            this.addDiamond(dg, material);
        }
    }
}

/////////////////////////////
// createDiamond()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamond = function(dg, material)
{
    var ddd = new ASTROVIEW.Diamond( dg, material );
    this.add(ddd);
}

/////////////////////////////
// renderScene()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.renderScene = function( av )
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

