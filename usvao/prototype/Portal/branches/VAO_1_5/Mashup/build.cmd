call sencha create jsb -a Clients\Mast\Portal.html?isBuild -p Clients\Mast\portal.jsb3
call sencha build jsb -p Clients\Mast\portal.jsb3 -d Clients\Mast

call sencha create jsb -a Clients\Portal\DataDiscovery.html?isBuild -p Clients\Portal\portal.jsb3
call sencha build jsb -p Clients\Portal\portal.jsb3 -d Clients\Portal 

call sencha create jsb -a Clients\dt\desktop.html?isBuild -p Clients\dt\portal.jsb3
call sencha build jsb -p Clients\dt\portal.jsb3 -d Clients\dt