#
#
#
#
package SQL::Queries;
use Exporter;

@ISA = qw(Exporter);
@EXPORT= qw(store_user_input load_notices update_notes_to_expired update_note_status get_hostname_for_note);
 
sub store_user_input
{
    my ($dbh,$c) = @_;
    
    my $affected      = (join ";",@{$c->{affectedservices}});
    my $otheraffected = (join ";",@{$c->{otheraffected}});
		   
    my $sql =  qq(insert into vao_notices values (null,'$c->{ipaddress}','$c->{text}',
			'$c->{niceeff}','$c->{niceexp}','$c->{priority}', '$c->{host}',
			'$affected','$otheraffected',null,null));
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    

}
sub update_note_status
{
    my ($c,$condition) = @_;
    my $sql = qq(update vao_notices set deleted = 'T' where id = '$c->{id}'); 
    my $sth = $c->{dbh}->prepare($sql);
    $sth->execute();
    
    $sql = qq(update vao_notices set reason_deleted = '$condition' where id = '$c->{id}'); 
    $sth  = $c->{dbh}->prepare($sql);
    $sth->execute();	
    
    
}
sub update_notes_to_expired
{
    my ($c) = @_;
    my $sql =  qq( update vao_notices set deleted = 'T'  where now() > end_date);
    $sth  = $c->{dbh}->prepare($sql);
    $sth->execute();

    $sql = qq(update vao_notices set reason_deleted = 'expired' where now()  > end_date and reason_deleted is null);
    $sth  = $c->{dbh}->prepare($sql);
    $sth->execute();


}
sub get_hostname_for_note
{
     	my ($dbh,$id) = @_;	
	my $sql  = qq(select ipaddress from vao_notices where id = '$id');
	my $sth  = $dbh->prepare($sql);
	$sth->execute();
	my $arrayref = $sth->fetchall_arrayref();
	return $arrayref;
}
sub load_notices
{
    my ($dbh,$tablename,$condition) = @_;
    my $sql;
    if ($condition eq 'currentnotes')
    { 
	$sql = qq(select * from $tablename where end_date > now() and deleted is null);
    }
    elsif ($condition eq 'deletednotes')
    {
 	$sql   = qq(select * from $tablename where now() > end_date  or deleted = 'T');
    }
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}
 
