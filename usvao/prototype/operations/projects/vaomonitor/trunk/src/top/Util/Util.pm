#
#
#
#
package Util::Util;
use LWP::UserAgent;
use XML::SimpleObject::LibXML;


use Exporter();
@ISA  = qw (Exporter);
@EXPORT  = qw (parse_xml_status getSeleniumStatus dump_selenium_lines dump_monitor_results
	       get_non_science_services load_selenium_testnames);

use strict;

sub parse_xml_status
{
    my ($type) = @_;
    my $machine = $ENV{SERVER_NAME};
    
    my $url = "http://$machine/vo/external_monitor/vodb.pl?format=xml";
    my $ua =  LWP::UserAgent->new();
    my $res = $ua->get($url); 
    my $xml = $res->content();
    my $xmlobj  = new XML::SimpleObject::LibXML(XML => $xml);
    
    my $t = $xmlobj->child();
    my @tables =  $t->children("Service");
    my @array;
    my $count = 0;
    foreach my $n (@tables)
    {
	my $element = $n->child("name");
        my $authid = $n->child("authid");	
	my $service =  $element->value;
	if ($n->child("status")->value eq 'Fail')
	{
	    push @array, $service;
	}
	$count++; 
    }
    my $down = scalar(@array);
    my $up = $count-$down;
    return \@array,$up;
    
}
sub load_selenium_testnames
{
    my $a    = 'greenlnextra';
    my $r    = 'notered';
    my %hash = (
                'Datascope'       => "Crash",
                'Directory'       => "Crash",           
                'DDT'             => "Crash",
                );    
    return \%hash;
}
sub dump_selenium_lines
{
    my ($servicenames,$names) = @_;
    my $hash = {};
    my @array = @$servicenames;
    
    foreach my $n (sort @array)
    { 
        my $class = 'greenln';
        my $service = shift @$n; 
        my $status = shift @$n;
        $status =~ s/\s+//g;
        $names->{$service} =  $status;  
    }
    foreach my $n (sort keys %$names)
    {
        my $class = 'notered';
        $class = 'greenln' if ($names->{$n} eq 'OK');
        print "<tr><td class = greenlnextra>$n</td><td class = $class>$names->{$n}</td></tr>\n";        
   
    }
}
sub dump_monitor_results
{
    my ($services) = @_;
    my @array = sort(@$services);
    foreach my $n (@array)
    {
        print "<tr><td class=greenlnextra >$n</td></tr>";
    }
}
sub get_non_science_services
{
    my ($results) = @_;
    my %hash;
    open (File, "./usvaohome/non_science_services")
	|| die "cannot open file";
    while(<File>)
    {
	my $line = $_;
	chomp $line;
	$hash{$line} = 1;
    }
    close File || die "cannot close file";
    return \%hash;
}
sub getSeleniumStatus
{
    open (Selenium, "./selenium_results");
    
    my $servicenames = [];
    while(<Selenium>)
    {
        my $line = $_;
        chomp $line;
        my @array = (split /\//,$line);
        my @b  =  (split /\:/, $array[1]);
        $b[0]  =~ s/\.pl//g;
        push @$servicenames, [$b[0], $b[1]];
    }  
    close Selenium;
    return $servicenames;
}
1;
