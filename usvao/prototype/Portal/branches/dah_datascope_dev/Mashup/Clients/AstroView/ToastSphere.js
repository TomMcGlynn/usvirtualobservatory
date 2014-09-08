/////////////////////////////
// ToastSphere()
/////////////////////////////
ASTROVIEW.ToastSphere = function ( radius )
{ 
    THREE.Object3D.call( this );
    this.name = "ToastSphere";
    this.matrixAutoUpdate = false;
    this.createSphere( radius );
};

ASTROVIEW.ToastSphere.prototype = new THREE.Object3D();
ASTROVIEW.ToastSphere.prototype.constructor = ASTROVIEW.ToastSphere;

/////////////////////////////
// createToastSphere3D()
/////////////////////////////
ASTROVIEW.ToastSphere.prototype.createSphere = function( radius )
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
    var dgI = new ASTROVIEW.ToastGeometry([YY, XX, _Y, ZZ], 0, 1, "I", zlevel, 1, radius);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.ToastGeometry([YY, _Z, _Y, XX], 1, 1, "II", zlevel, 0, radius);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.ToastGeometry([YY, _X, _Y, _Z], 1, 0, "III", zlevel, 2, radius);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.ToastGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", zlevel, 3, radius);
    
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

