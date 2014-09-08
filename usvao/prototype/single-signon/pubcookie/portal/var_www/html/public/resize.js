/**
        * resize.js 0.3 970811
        * by gary smith
        * js component for "reloading page onResize"
        */

       if(!window.saveInnerWidth) {
         window.onresize = resizeIt;
         window.saveInnerWidth = window.innerWidth;
         window.saveInnerHeight = window.innerHeight;
       }

       function resizeIt() {
           if (saveInnerWidth < window.innerWidth || 
               saveInnerWidth > window.innerWidth || 
               saveInnerHeight > window.innerHeight || 
               saveInnerHeight < window.innerHeight ) 
           {
               window.history.go(0);
           }
       }
