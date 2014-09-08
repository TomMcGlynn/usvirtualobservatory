#! /bin/csh -f
#
set out=$argv[1]

zip $out -Z store -X mimetype
zip -rg $out -Z store Pictures media --exclude \*.svn\*
zip -rg $out content.xml manifest.rdf styles.xml
zip -rg $out -Z store meta.xml
zip -rg $out Thumbnails Configurations2/accelerator --exclude \*.svn\*
zip -rg $out settings.xml META-INF --exclude \*.svn\*
