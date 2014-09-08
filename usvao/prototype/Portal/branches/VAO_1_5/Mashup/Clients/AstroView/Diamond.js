/////////////////////////
// Diamond
/////////////////////////
ASTROVIEW.Diamond = function( geometry, material )
{
    if (!material)
    {
        material = ASTROVIEW.Diamond.DefaultMaterial;
    }

    THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = geometry.name;
    this.matrixAutoUpdate = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image

    // Image Texture Properties
    this.texture = null;
    this.flipY = false;
    
    // Count of children diamonds that are actively loading images
    this.childLoadingCount = 0;
};

ASTROVIEW.Diamond.prototype = new THREE.Mesh();
ASTROVIEW.Diamond.prototype.constructor = ASTROVIEW.Diamond;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.Diamond.DefaultFile = ASTROVIEW.Diamond.DefaultFile || "DiamondDefault.png";
ASTROVIEW.Diamond.DefaultUrl = ASTROVIEW.Diamond.DefaultUrl || "../AstroView/" + ASTROVIEW.Diamond.DefaultFile;
ASTROVIEW.Diamond.DefaultMaterial = ASTROVIEW.Diamond.DefaultMaterial || new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.Diamond.DefaultUrl) } );

ASTROVIEW.Diamond.ErrorFile = ASTROVIEW.Diamond.ErrorFile || "DiamondError.png";
ASTROVIEW.Diamond.ErrorUrl = ASTROVIEW.Diamond.ErrorUrl || "../AstroView/" + ASTROVIEW.Diamond.ErrorFile;

/////////////////////////
// renderScene()
/////////////////////////
ASTROVIEW.Diamond.prototype.renderScene = function( av )
{   
    this.av = av;
    this.flipY = (av.renderType == 'webgl');
    var frustum = av.controller.frustum;
    var zlevel = av.controller.getZoomLevel();
 
    // Update Visibility if we are in the Viewing Frustum
    // (this.geometry.pixel == 15 || this.geometry.pixel == 219) && 
    this.viewable = this.inFrustum(frustum); 
        
    if (this.viewable)
    {         
        if (zlevel == this.geometry.zlevel)
        {    
            //
            // CASE I: We are in the Frustum, my level is active, Load my image, Remove any Children (for Zoom Out)
            //
                 
            // If survey url has changed, load new image
            if (av.survey.baseurl !== this.baseurl)
            {
                this.imageurl = this.geometry.getImageUrl(av.survey.baseurl);
                this.cleanTexture(av);
                this.loadTexture(this.imageurl);
                this.baseurl = av.survey.baseurl;
            }
     
            // Remove all Children below current vizible Level
            if (this.children.length > 0)
            {
                this.removeDiamondChildren(av);
            }
        }
        else if (zlevel > this.geometry.zlevel)
        {
            //
            // CASE II: We are in the Frustum, Zoom In Occured, Expand my Children, renderScene Children
            //
            if (this.children.length == 0)
            {
                this.expandDiamondChildren();
            }
                     
            // renderScene Children
            if (this.children.length > 0)
            {
                this.renderDiamondChildren(av);
            }
        }
        
        //
        // Update Material Opacity Flag:
        // Set to TRUE if our zoom level is currently active 
        // If our level is NOT active, then wait until all children are finished loading to clear out our image. 
        //
        // NOTE:
        // The opacity flag is used in THREE.CanvasMobileRenderer.js and substantially improves rendering performance
        // by reducing the number of calls to clipImage() which is fairly expensive.
        //
        if (this.material)
        {
            if (zlevel == this.geometry.zlevel)
            {   
                this.material.opacity = 1.0;
            }
            else if (this.material.opacity > 0.0 && this.childLoadingCount <= 0)
            {
                this.material.opacity = 0.0;
                this.childLoadingCount = 0;
            }
        }
    }
    else    
    {
        //
        // CASE III: We are Not in the Viewing Frustum, Remove all Children,
        //
        if (this.children.length > 0)
        {
            this.removeDiamondChildren(av);
        }
    }
};

ASTROVIEW.Diamond.prototype.clearScene = function( av )
{ 
    this.cleanTexture(av);
    this.baseurl = ASTROVIEW.Diamond.DefaultUrl;
    this.loadTexture(ASTROVIEW.Diamond.DefaultUrl);
    this.clearDiamondChildren();
};

ASTROVIEW.Diamond.prototype.loadTexture = function(url)
{  
    // Increment the number of active children loading images
    this.parent.childLoadingCount++;
      
    // Create Texture from Image
    this.texture = new THREE.Texture( new Image() );
    this.texture.image.onload = ASTROVIEW.bind(this, this.onLoad);
    this.texture.image.onerror = ASTROVIEW.bind(this, this.onError);
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
    
    // NOTE: We have to Flip the UV Mapping for WebGL
    this.texture.flipY = this.flipY;
}
             
ASTROVIEW.Diamond.prototype.onLoad = function(event)
{    
    //ASTROVIEW.log(this.name + " *** onLoad() url: " + this.texture.image.src);
    
    // Decrement the number of active children loading images
    this.parent.childLoadingCount--;
    
    // Clear out image callbacks
    this.texture.image.onload = undefined;
    this.texture.image.onerror = undefined;
    
    // Create Material from Image Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture, overdraw: this.geometry.overdraw, side: this.geometry.side } );
    
    // Wireframe 
    //this.material = new THREE.MeshBasicMaterial( { color: ASTROVIEW.Diamond.color[this.geometry.zlevel], wireframe: true, transparent: true, opacity: 1.0, side: THREE.DoubleSide } );
    
    // Solid Color 
    //this.material = new THREE.MeshBasicMaterial( { color: ASTROVIEW.Diamond.color[this.geometry.zlevel], transparent: true, opacity: 1.0, side: THREE.FrontSide } );
    
    // Update the Material
    this.texture.needsUpdate = true; 
          
    // Request full render
    this.av.render("DIAMOND");
}

ASTROVIEW.Diamond.prototype.onError = function(event)
{
    ASTROVIEW.log(this.name + " *** onError() url: " + this.texture.image.src);
    
    // Decrement the number of active children loading images
    this.parent.childLoadingCount--;
  
    // Clear out image callbacks
    this.texture.image.onload = undefined;
    this.texture.image.onerror = undefined;
    
    // If failed file is not our default file, load the default file (blank diamond)   
    var src = this.texture.image.src;
    var srcfile = src.substring(src.lastIndexOf('/')+1);
  
    if (srcfile != ASTROVIEW.Diamond.DefaultFile && 
        srcfile != ASTROVIEW.Diamond.ErrorFile)
    {
        this.loadTexture(ASTROVIEW.Diamond.ErrorUrl);
    }
}

ASTROVIEW.Diamond.prototype.setMaterialOpacity = function(visible)
{   
    if (this.material) 
    {
        this.material.opacity = (visible ? 1.0 : 0.0);
    }
}

ASTROVIEW.Diamond.prototype.expandDiamondChildren = function()
{
    // geometry.expandDiamond() returns array of THREE.DiamondGeometry Objects (dgs)
    var dgs = this.geometry.expandDiamond();
    for (i=0; i<dgs.length; i++)
    {
        var ddd = new ASTROVIEW.Diamond( dgs[i] );
        this.add(ddd);
    }
};

ASTROVIEW.Diamond.prototype.renderDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
    {
        var ddd = this.children[i];
        ddd.renderScene(av);
    }
};

ASTROVIEW.Diamond.prototype.clearDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
    {
        var ddd = this.children[i];
        ddd.clearScene(av);
    }
};

ASTROVIEW.Diamond.prototype.removeDiamondChildren = function( av )
{
    var names = "";
    while (this.children.length > 0)
    {
        var ddd = this.children[0];
        if (ddd.children.length > 0) ddd.removeDiamondChildren(av);
        names += ddd.name + " ";
        ddd.clean(av);
        this.remove(ddd);   // NOTE: Decrements this.children.length
    }
    this.children = [];
};

ASTROVIEW.Diamond.prototype.clean = function( av )
{
    // Remove the Texture, Image and Material
    this.cleanTexture(av);
 
    // Remove geometry and mesh from WebGL Graphics Memory 
    if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
    {
        if (this.geometry) av.renderer.deallocateObject(this.geometry);     
        av.renderer.deallocateObject(this);
    }
 
    // Remove all Object References
    if (this.geometry)
    {
        this.geometry.clean();
        this.geometry = undefined;
    }
}

ASTROVIEW.Diamond.prototype.cleanTexture = function( av )
{        
    // Texture is created from Image
    if (this.texture)
    {
        // Remove texture from WebGL Graphics Memory 
        if (av && av.renderer && av.renderer instanceof THREE.WebGLRenderer)
        {
            av.renderer.deallocateTexture(this.texture);
        }
     
        if (this.texture.image)
        {
            // Check if image loading is still underway 
            if (this.texture.image.onload || this.texture.image.onerror)
            {
                this.parent.childLoadingCount--;
                this.texture.image.onload = undefined;
                this.texture.image.onerror = undefined;
            }
        }
        this.texture = undefined;
    }
}

ASTROVIEW.Diamond.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
}

