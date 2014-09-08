#!/usr1/local/bin/tcsh
#
# Check the links on various vo pages 
#
#
#
set dir = '/usr/contrib/linux/bin';
set prod = '/web_chroot.prod/.www_mountpnt/www/htdocs/vo/vaomonitor/chbot';

#vao sandbox
$dir/checkbot --verbose --url http://www.usvao.org --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/vao.html

#vao twitter
#$dir/checkbot --verbose --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/twitter.vao.html --matches 'http://twitter.com/usvao/
#$dir/checkbot --verbose --url http://twitter.com/usvao --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/twitter.vao.html

#vao facebook
$dir/checkbot --verbose --url http://www.facebook.com/pages/US-VAO/111119192247587 --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/facebook.vao.html

#vao help
$dir/checkbot --verbose --url http://vaohelp.tuc.noao.edu:8080/secure/Dashboard.jspa  --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/help.vao.html


#iris
$dir/checkbot --verbose --url  http://cxc.cfa.harvard.edu/csc1/temp/sed/   --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/iris.html


#vao help
$dir/checkbot --verbose --url http://www.usvao.org/science-tools-services/vao-tools-services-data-discovery-tool/   --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/discovery.html

#cross match
$dir/checkbot --verbose --url  http://vao-web.ipac.caltech.edu/applications/VAOSCC  --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/crossmatch.html

#timeseries
$dir/checkbot --verbose --url  http://vao-web.ipac.caltech.edu/applications/VAOTimeSeries/  --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/timeseries.html

#sso usvao
$dir/checkbot --verbose --url  https://sso.usvao.org/   --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/ssovao.html

#vao registry
$dir/checkbot --verbose --url  http://vao.stsci.edu/directory/index.aspx   --ignore 'FTP|/listserv/|/mail_archive/' --style checkbot.css --dontwarn '301|302|307' --file $prod/vaoregistry.html






exit
