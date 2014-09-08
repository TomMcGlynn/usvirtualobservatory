    // Do initialization for our local setup
    
    // reference local blank image
    Ext.BLANK_IMAGE_URL = extPath + 'examples/desktop/images/s.gif';
 
    // create namespace
    Ext.namespace('Mvp');

    // Set up dynamic loading of our Mvp JavaScript files.  The files will be autoloaded when the
    // class is referred to in an Ext.create or Ext.extend.  Using new <classname> will bypass that
    // process, so use Ext.create instead.
    Ext.Loader.setConfig({enabled: true});
    
    // Trim the trailing '/' off the initialization path because it causes a '//' in the class loading,
    // which fails on Windows.
    Ext.Loader.setPath('Mvp', initializationPath.substring(0, initializationPath.length - 1));
    Ext.Loader.setPath('Ext.ux', extPath + 'examples/ux');
    
    // Initialize quick tips.
    Ext.tip.QuickTipManager.init();

