#
#
#
package SQL::Queries;
use  Exporter();
@ISA = qw(Exporter);
@EXPORT =  qw( add_entry update_entry update_deprecated);

sub update_entry
{
    my ($updatedservices,$dbh) = @_;
    foreach my $data  ( @$updatedservices)
    {
        
        my ($id,$b,$serviceId) = @$data;
        print "\nUpdating url: $serviceId\n";
        $dbh->do("Update Services set serviceURL =  \'$b\' where serviceId = $serviceId"); 

    }
}
sub add_entry
{ 
    my ($newservices,$dbh) = @_;
    foreach my $data (@$newservices)
    {   
        my ($ivoid, $service) = @$data; 
        my $xsitype   = $service->get_xsitype;
        my $serviceURL = $service->get_baseurl;
        my $shortname  = $service->get_shortname;
        $shortname     =~ s/\'/\\\'/g;
        my $radius     = $service->get_sr;
        my $dec        = $service->get_dec;
        my $ra         = $service->get_ra;
        my $role       = $service->get_role;
        print "Adding  $ivoid, $xsitype\n";
        
        my $sql = qq(Insert into Services 
                     (serviceId,shortname,serviceURL,xsitype,ivoid,test_ra,test_dec,radius,role) values 
                     (NULL,'$shortname','$serviceURL','$xsitype','$ivoid','$ra','$dec','$radius', '$role'));
        
        my $sth = $dbh->do($sql);
        my $insertedid = $dbh->prepare("select last_insert_id() from Services");                
        $insertedid->execute();
    }
}
sub update_deprecated
{
          my ($todeprecate,$dbh) = @_;
          foreach my $entry (@$todeprecate)
	  {     
               	my ($id, $type) = (split /\\/,$entry);
                print "\nUpdating Deprecated Services:  $id,$type\n";
                my $query = qq{update Services set deprecated='yes'  where ivoid='$id' and xsitype='$type'};
                my $sth =  $dbh->prepare($query);
                $sth->execute();
          }
} 
1;
