//////////////////////////////////////////////////////////
// ASTROVIEW URLS 
//
// Syntax: ASTROVIEW.urls.[type].[server].[name]
//  
//         [type]:=   [toast, healpix, proxy, name]
//         [server]:= [mastimg, dummy, wwt, aladin]
//         [name]:=   [DSS, GALEX, SDSS]
//
//////////////////////////////////////////////////////////
ASTROVIEW.urls = {
    "name" : {},
    "proxy": {},
    "toast": {
        "dummy":{},
        "mastimg":{},
        "wwt":{}
    },
    "healpix": {
        "dummy":{},
        "mastimg":{},
        "aladin":{}
    },
};

// Name
ASTROVIEW.urls.name.mastresolver    = "http://mastresolver.stsci.edu/Santa-war/query?outputFormat=xml&name=";

// Proxy
ASTROVIEW.urls.proxy.mastdev        = "http://mastdev.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";
ASTROVIEW.urls.proxy.masttest       = "http://masttest.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";
ASTROVIEW.urls.proxy.mast           = "http://mast.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";

// TOAST 
ASTROVIEW.urls.toast.dummy.image    = ASTROVIEW.urls.proxy.masttest + "http://dummyimage.com/256x256/[COLOR]/000000%26text=%5B" + "[LEVEL]" + ":" + "[TX]" + "," + "[TY]" + "%5D";
ASTROVIEW.urls.toast.mastimg.dss    = "http://mastimg.stsci.edu/surveys/toast/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
ASTROVIEW.urls.toast.mastimg.galex  = "http://mastimg.stsci.edu/surveys/toast/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
ASTROVIEW.urls.toast.mastimg.sdss   = "http://mastimg.stsci.edu/surveys/toast/sdss/[LEVEL]/[TX]/sdss_[LEVEL]_[TX]_[TY].jpg";  

// HEALPIX 
ASTROVIEW.urls.healpix.dummy.image  = ASTROVIEW.urls.proxy.masttest + "http://dummyimage.com/256x256/[COLOR]/000000%26text=%5B" + "[LEVEL]" + ":" + "[PIXEL]" + "%5D"; 
ASTROVIEW.urls.healpix.mastimg.dss  = "http://mastimg.stsci.edu/surveys/healpix/dss/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
ASTROVIEW.urls.healpix.aladin.dss   = ASTROVIEW.urls.proxy.masttest + "http://alasky.u-strasbg.fr/DssColor/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
ASTROVIEW.urls.healpix.aladin.iras  = ASTROVIEW.urls.proxy.masttest + "http://alasky.u-strasbg.fr/IRISColor/Norder3/Dir0/Npix[PIXEL].jpg";

//////////////////////////////////////////////////////////
//
// ASTROVIEW Surveys
//
//////////////////////////////////////////////////////////

ASTROVIEW.surveys = [
{
    "name"      : "DSS",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.dss,

    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":11, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":12, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":12, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":12, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":12, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":12, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":12, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":12, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":12, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":12, "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "GALEX",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.galex,
    
    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":10, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":10, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":10, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":10, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":10, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":10, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":10, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":10, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":10, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":10, "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "SDSS",
    "visible"   : true,
    "type"      : "toast",
    "baseurl"   : ASTROVIEW.urls.toast.mastimg.sdss,
    
    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":9,  "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":9,  "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":9,  "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":9,  "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":9,  "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":9,  "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":9,  "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":9,  "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":9,  "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":9,  "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":9,  "rax":   0.0001,"decx":    0.0001}]
},
{
    "name"      : "HEALPIX",
    "visible"   : false,
    "type"      : "healpix",
    "baseurl"   : ASTROVIEW.urls.healpix.mastimg.dss,
    
    "zoomTable": [{"fov":     30, "level":2, "rax":   10, "decx":    10},
                  {"fov":     20, "level":3, "rax":    5, "decx":     5},
                  {"fov":     10, "level":4, "rax":    5, "decx":     5},
                  {"fov":      5, "level":5, "rax":    2, "decx":     2},
                  {"fov":    2.5, "level":6, "rax":    1, "decx":     1},
                  {"fov":   1.25, "level":7, "rax":  0.5, "decx":   0.5},
                  {"fov":   0.75, "level":7, "rax":  0.2, "decx":   0.2},
                  {"fov":  0.355, "level":7, "rax":  0.1, "decx":   0.1},
                  {"fov":  0.125, "level":7, "rax":  0.05,"decx":  0.05},
                  {"fov": 0.0625, "level":7, "rax":  0.01,"decx":  0.01},
                  {"fov":0.00001, "level":7, "rax": 0.005,"decx": 0.005}]
},
{
    "name"      : "healpix",
    "visible"   : false,
    "type"      : "healpix",
    "dummy"     : true,
    "baseurl"   : ASTROVIEW.urls.healpix.dummy.image,
    
    "zoomTable": [{"fov":     30, "level":2, "rax":   10, "decx":    10},
                  {"fov":     20, "level":3, "rax":    5, "decx":     5},
                  {"fov":     10, "level":4, "rax":    5, "decx":     5},
                  {"fov":      5, "level":5, "rax":    2, "decx":     2},
                  {"fov":    2.5, "level":6, "rax":    1, "decx":     1},
                  {"fov":   1.25, "level":7, "rax":  0.5, "decx":   0.5},
                  {"fov":   0.75, "level":7, "rax":  0.2, "decx":   0.2},
                  {"fov":  0.355, "level":7, "rax":  0.1, "decx":   0.1},
                  {"fov":  0.125, "level":7, "rax":  0.05,"decx":  0.05},
                  {"fov": 0.0625, "level":7, "rax":  0.01,"decx":  0.01},
                  {"fov":0.00001, "level":7, "rax": 0.005,"decx": 0.005}]
},
{
    "name"      : "toast",
    "visible"   : false,
    "type"      : "toast",
    "dummy"     : true,
    "baseurl"   : ASTROVIEW.urls.toast.dummy.image,        

    "zoomTable": [{"fov":     30, "level":4,  "rax":    10,   "decx":     10},
                  {"fov":     20, "level":5,  "rax":     5,   "decx":      5},
                  {"fov":     10, "level":6,  "rax":     5,   "decx":      5},
                  {"fov":      5, "level":7,  "rax":     2,   "decx":      2},
                  {"fov":    2.5, "level":8,  "rax":     1,   "decx":      1},
                  {"fov":   1.25, "level":9,  "rax":   0.5,   "decx":    0.5},
                  {"fov":   0.75, "level":10, "rax":   0.2,   "decx":    0.2},
                  {"fov":  0.355, "level":11, "rax":   0.1,   "decx":    0.1},
                  {"fov":  0.125, "level":12, "rax":   0.05,  "decx":    0.05},
                  {"fov": 0.0625, "level":12, "rax":   0.02,  "decx":    0.02},
                  {"fov": 0.0325, "level":12, "rax":   0.01,  "decx":    0.01},
                  {"fov": 0.0163, "level":12, "rax":   0.005, "decx":    0.005},
                  {"fov": 0.0081, "level":12, "rax":   0.002, "decx":    0.002},
                  {"fov": 0.0040, "level":12, "rax":   0.001, "decx":    0.001},
                  {"fov": 0.0020, "level":12, "rax":   0.0005,"decx":    0.0005},
                  {"fov": 0.0010, "level":12, "rax":   0.0002,"decx":    0.0002},
                  {"fov": 0.0005, "level":12, "rax":   0.0001,"decx":    0.0001}]
}];
