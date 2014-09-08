    // Do initialization for our local setup
    
    // reference local blank image
    Ext.BLANK_IMAGE_URL = AppConfig.extPath + 'examples/desktop/images/s.gif';
 
    // create namespace
    Ext.namespace('Mvp');
    (AppConfig.isMast) ? Ext.namespace('Mast') : Ext.namespace('Vao');
    // Set up dynamic loading of our Mvp JavaScript files.  The files will be autoloaded when the
    // class is referred to in an Ext.create or Ext.extend.  Using new <classname> will bypass that
    // process, so use Ext.create instead.
    Ext.Loader.setConfig({enabled: true});

    Ext.Loader.setPath('Ext', AppConfig.extPath + '/src');
    Ext.Loader.setPath('Mvp', AppConfig.initializationPath + 'Shared');
    Ext.Loader.setPath('Mvpd', AppConfig.initializationPath + 'Designer/Exports/app');
    Ext.Loader.setPath('Mvpc', AppConfig.initializationPath + 'Designer/Containers/app');
    Ext.Loader.setPath('Ext.ux', AppConfig.extPath + 'examples/ux');
    (AppConfig.isMast) ? Ext.Loader.setPath('Mast', '.') : Ext.Loader.setPath('Vao', '.');
    Ext.Loader.setPath({
        'Ext.ux.desktop': AppConfig.extPath + 'examples/desktop/js',
        MyDesktop: ''
    });
