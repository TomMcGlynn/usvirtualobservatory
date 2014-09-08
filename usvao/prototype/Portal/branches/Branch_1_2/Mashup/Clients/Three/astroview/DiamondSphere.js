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
    var YY = new THREE.Vertex( new THREE.Vector3(0.0,  1.0,  0.0)); // +Y
    var _Y = new THREE.Vertex( new THREE.Vector3(0.0, -1.0,  0.0)); // -Y
                    
    var XX = new THREE.Vertex( new THREE.Vector3( 1.0,  0.0,  0.0)); // +X
    var _X = new THREE.Vertex( new THREE.Vector3(-1.0,  0.0,  0.0)); // -X	

    var ZZ = new THREE.Vertex( new THREE.Vector3(0.0,  0.0,  1.0)); // +Z
    var _Z = new THREE.Vertex( new THREE.Vector3(0.0,  0.0, -1.0)); // +Z
    
    //
    // STEP 2: Create the Top Level DiamondGeometry located in each 3D Qudrant (I, II, III, IV),
    //         mapped to a TOAST Image Coordinate [tx, ty] as shown in the Mapping above.
    //
    // Quadrant I: [+X,+Z] ===> TOAST: [0,1] 
    var dgI = new ASTROVIEW.DiamondGeometry([YY, XX, _Y, ZZ], 0, 1, "I", 0x0000ff, zlevel, 1, RADIUS);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.DiamondGeometry([YY, _Z, _Y, XX], 1, 1, "II", 0x00ff00, zlevel, 0, RADIUS);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.DiamondGeometry([YY, _X, _Y, _Z], 1, 0, "III", 0xff0000, zlevel, 2, RADIUS);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.DiamondGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", 0xffff00, zlevel, 3, RADIUS);
    
    //
    // STEP 3: Expand Each Top Level DiamondGeometry Object to Level 4 Array of DiamondGeometry[] objects
    //
    var depth = ASTROVIEW.CameraController.ZOOM_LEVEL_MIN-1;	// expand 3 more levels...
    var zlevel = ASTROVIEW.CameraController.ZOOM_LEVEL_MIN;	    // ...to zlevel '4'

    var daI = dgI.expandDiamond(depth, zlevel, RADIUS);			// Quadrant I
    var daII = dgII.expandDiamond(depth, zlevel, RADIUS);		// Quadrant II
    var daIII = dgIII.expandDiamond(depth, zlevel, RADIUS);		// Quadrant III
    var daIV = dgIV.expandDiamond(depth, zlevel, RADIUS);		// Quadrant IV		
    
    //
    // STEP 4: Create DiamondMesh objects from the DiamondGeometry[] array and
    //         add them as children to the DiamondSphere
    //
    this.createDiamondsMaterial(daI, null); 
    this.createDiamondsMaterial(daII, null);
    this.createDiamondsMaterial(daIII, null);
    this.createDiamondsMaterial(daIV, null);
    
    /*
     //
     // Create DiamondMesh(s) with Test Image Material
     //
    var matI = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_1.PNG' ) } );
    var matII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_1.PNG' ) } );
    var matIII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_0.PNG' ) } );
    var matIV = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_0.PNG' ) } );
    
    this.createDiamondsMaterial(daI, matI); 
    this.createDiamondsMaterial(daII, matII);
    this.createDiamondsMaterial(daIII, matIII);
    this.createDiamondsMaterial(daIV, matIV);
    */
     
    /*
     //
     // Create DiamondMesh(s) with Color Material
     //
    this.createDiamondsColor(daI, dgI.color, true); 
    this.createDiamondsColor(daII, dgII.color, true);
    this.createDiamondsColor(daIII, dgIII.color, true);
    cthis.reateDiamondsColor(daIV, dgIV.color, true);
    */
    
    /*
    //
    // Create DiamondMesh(s) with URL Material
    //
    url = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
    this.createDiamondsUrl(daI, url); 
    this.createDiamondsUrl(daII, url);
    this.createDiamondsUrl(daIII, url);
    cthis.reateDiamondsUrl(daIV, url);
    */
}

/////////////////////////////
// createDiamondsColor()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsColor = function(diamonds, color, wireframe)
{	
    for (i=0; i<diamonds.length; i++)
    {
        var material = new THREE.MeshBasicMaterial( { color: color, wireframe: wireframe } );
        var dg = diamonds[i];
        this.addDiamond(dg, material);
    }
}

/////////////////////////////
// createDiamondsUrl()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsUrl = function(diamonds, baseurl)
{
    var url = new String(baseurl);

    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        var imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);
        var material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(imageurl) } );
        this.addDiamond(dg, material);
    }
}

/////////////////////////////
// createDiamondsMaterial()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsMaterial = function(diamonds, material)
{
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        this.createDiamond(dg, material);
    }
}

/////////////////////////////
// createDiamond()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamond = function(dg, material)
{
    var dm = new ASTROVIEW.DiamondMesh( dg, material );
    this.add(dm);
}
