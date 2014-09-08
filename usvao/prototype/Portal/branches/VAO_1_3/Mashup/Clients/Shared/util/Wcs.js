Ext.define('Mvp.util.Wcs', {
    //CTYPE1  = 'RA---TAN'
    //CTYPE2  = 'DEC--TAN'
    //CUNIT1  = 'deg     '
    //CUNIT2  = 'deg     '
    //CRPIX1  = 1.02450000000000E+03 / Column Pixel Coordinate of Ref. Pixel
    //CRPIX2  = 7.44500000000000E+02 / Row Pixel Coordinate of Ref. Pixel
    //CRVAL1  = 5.86305204900000E+01 / RA at Reference Pixel
    //CRVAL2  = -3.1363144000000E-01 / DEC at Reference Pixel
    //CD1_1   = 1.92282275464897E-08 / RA  degrees per column pixel
    //CD1_2   = 1.09997827820690E-04 / RA  degrees per row pixel
    //CD2_1   = 1.09973486328125E-04 / DEC degrees per column pixel
    //CD2_2   = -9.5161290322482E-09 / DEC degrees per row pixel

    // Must specify at a minimum: crpix1, crpix2, crval1, crval2,
    // cd1_1, cd1_2, cd2_1, cd2_2
    constructor: function (config) {
        Ext.Apply(this, config);
        
        this.valid = true;
        this.valid = this.valid && Ext.isNumber(this.crpix1);
        this.valid = this.valid && Ext.isNumber(this.crpix2);
        this.valid = this.valid && Ext.isNumber(this.crval1);
        this.valid = this.valid && Ext.isNumber(this.crval2);
        this.valid = this.valid && Ext.isNumber(this.cd1_1);
        this.valid = this.valid && Ext.isNumber(this.cd1_2);
        this.valid = this.valid && Ext.isNumber(this.cd2_1);
        this.valid = this.valid && Ext.isNumber(this.cd2_2);
        
        if (this.valid) {
            this.computeCorners();
        }
    },
    
    pixel2RaDec: function(px, py) {
        var ret = null;
        
        if (this.valid) {
            var xdif = px - this.crpix1;
            var ydif = py - this.crpix2;
            var xsi = this.cd1_1 * xdif + this.cd1_2 * ydif;
            var eta = this.cd2_1 * xdif + this.cd2_2 * ydif;  
            var a = this.crval1 + xsi
            var d = this.crval2 + eta
            ret = {ra: a, dec: d};
        }
        return ret;
    },
    
    computeCorners: function() {
        
    }
    
    
})