#
#
#
#
package SQL::Queries;
use Exporter;

@ISA = qw(Exporter);
@EXPORT= qw(store_user_input load_notices update_note_status get_hostname_for_note);
 
sub store_user_input
{
    my ($dbh,$c) = @_;
    my $affected      = (join ";",@{$c->{affectedservices}});
    my $otheraffected = (join ";",@{$c->{otheraffected}});
		   
    my $sql =  qq(insert into $::noticestable values (null,?,?,?,?,?,?,?,?,?,null,null));
    my $sth = $dbh->prepare($sql);
    $sth->execute($c->{ipaddress},$c->{identity},$c->{text},$c->{niceeff},$c->{niceexp},$c->{priority},$c->{host},$affected,$otheraffected);
    

}
sub update_note_status
{
    my ($c,$condition) = @_;
    my $sql = qq(update $::noticestable set deleted = 'T' where id = ?); 
    my $sth = $c->{dbh}->prepare($sql);
    $sth->execute($c->{id});
    $sql = qq(update $::noticestable set reason_deleted = ?  where id = ?);   
    $sth  = $c->{dbh}->prepare($sql);
    $sth->execute($condition, $c->{id}); 
    
}
sub get_hostname_for_note
{
     	my ($dbh,$id) = @_;
	my $sql  = qq(select identity  from $::noticestable where id = ? );
        my $sth  = $dbh->prepare($sql);
        $sth->execute($id);
	my $arrayref = $sth->fetchall_arrayref();
	return $arrayref;
}
sub load_notices
{
    my ($dbh,$type) = @_;
    my $sql = qq(select * from $::noticestable where end_date >= utc_timestamp() and deleted is null);
    if ($type && $type eq 'deleted'){
 	$sql   = qq(select * from $::noticestable where utc_timestamp() > end_date  or deleted = 'T');
    }
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}
 
