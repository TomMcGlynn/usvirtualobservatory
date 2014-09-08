call sencha create jsb -a Mast\Portal.html?isBuild -p Mast\portal.jsb3
call sencha build jsb -p Mast\portal.jsb3 -d Mast

call sencha create jsb -a Portal\DataDiscovery.html?isBuild -p Portal\portal.jsb3
call sencha build jsb -p Portal\portal.jsb3 -d Portal 

call sencha create jsb -a dt\desktop.html?isBuild -p dt\portal.jsb3
call sencha build jsb -p dt\portal.jsb3 -d dt