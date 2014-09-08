/////////////////////////
// Diamond
/////////////////////////
ASTROVIEW.Diamond = function( geometry, material )
{
    if (!material)
    {
        material = ASTROVIEW.DefaultMaterial;
    }

    THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = geometry.tid;
    this.flipSided = true;
    this.doubleSided = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image
    this.defaulturl = ASTROVIEW.DefaultUrl;
 
    // Image Texture Properties
    this.texture = null;
};

ASTROVIEW.Diamond.prototype = new THREE.Mesh();
ASTROVIEW.Diamond.prototype.constructor = ASTROVIEW.Diamond;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.DefaultUrl = "../AstroView/Diamond.png";
ASTROVIEW.DefaultMaterial = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.DefaultUrl) } );

/////////////////////////
// renderScene()
/////////////////////////
ASTROVIEW.Diamond.prototype.renderScene = function( av )
{   
    this.av = av;
    var frustum = av.controller.frustum;
    var vizlevel = av.controller.getVisibleLevel();
 
    // Update Visibility if we are in the Viewing Frustum
    this.visible = this.inFrustum(frustum);
    if (this.visible)
    {    
        if (vizlevel == this.geometry.zlevel)
        {    
            //
            // CASE I: We are in the Frustum, my level is active, Load my image, Remove any Children (for Zoom Out)
            //
                 
            // If survey url has changed, load new image
            if (av.baseurl !== this.baseurl)
            {
                var url = new String(av.baseurl);
                var dg = this.geometry; // DiamondGeometry  
                this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
                this.cleanTexture(av);
                this.loadTexture(this.imageurl, av );
                this.baseurl = av.baseurl;
            }
     
            // Remove all Children below current vizible Level
            if (this.children.length > 0)
            {
                this.removeDiamondChildren(av);
            }
        }
        else if (vizlevel > this.geometry.zlevel)
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
     
        // Update Material Opacity based on active Zoom Level
        if (ASTROVIEW.DiamondTimerCount == 0)
        {
            this.setOpacity(vizlevel == this.geometry.zlevel);
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

ASTROVIEW.Diamond.prototype.loadTexture = function(url)
{    
    // Create Texture from Image
    this.texture = new THREE.Texture( new Image() );

    this.texture.image.onload = bind(this, this.onLoad);
    this.texture.image.onerror = bind(this, this.onError);
    
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
}
    
ASTROVIEW.Diamond.prototype.onLoad = function(event)
{    
    // Create Material from Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture, overdraw: true } );
    this.texture.needsUpdate = true; 
    
    // Request full render
    this.av.render();
}

ASTROVIEW.Diamond.prototype.onError = function(event)
{
    console.debug(this.name + " *** onError() url: " + this.texture.image.src);
  
    // Create Blank Texture from Default Image Url
    this.loadTexture(this.defaulturl);
    
    // Request full render
    this.av.render();
}

ASTROVIEW.Diamond.prototype.setOpacity = function(visible)
{   
    if (this.material) 
    {
        this.material.opacity = (visible ? 1.0 : 0.0);
    }
}

ASTROVIEW.Diamond.prototype.expandDiamondChildren = function()
{
    // expandGeometry (depth, zlevel, radius)
    var geoms = this.geometry.expandDiamond();
    for (i=0; i<geoms.length; i++)
    {
        var ddd = new ASTROVIEW.Diamond( geoms[i] );
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
    //console.debug(this.name + " Removed Children Diamonds : " + names);
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
            if (this.texture.image.onload) this.texture.image.onload = undefined;
            if (this.texture.image.onerror) this.texture.image.onerror = undefined;
            this.texture.image = undefined;
        }
        this.texture = undefined;
    }
}

ASTROVIEW.Diamond.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
}

