#!/usr1/local/bin/perl5 -wT
#
#  name:   get_publishing_registries.pl
#
#  author: Michael Preciado
#
#  description: this script uses the registry of registries interface to
#               return a list of publishing registries. This list is then
#               stored locally. The script is meant to be run only under a
#               cron job ( See the readme file for more information).
#
#
#
{
    use strict;

    use LWP::UserAgent;
    use XML::SimpleObject::LibXML;

    #local file
    open (my $fh,">","./data/publishing_registries") || die "cannot open file\n";    

    #fetch xml 
    my $ua = LWP::UserAgent->new();
    my $res = $ua->get("http://rofr.ivoa.net/cgi-bin/oai.pl?verb=ListRecords&metadataPrefix=ivo_vor&set=ivo_publishers");
    my $xml = $res->content;

    #parse xml,get list
    my $xmlobject = new XML::SimpleObject::LibXML(XML => $xml);
    my @records  = $xmlobject->child("ListRecords")->children("record");
    foreach my $record (@records)
    {
	my $identifier = $record->child("header")->child("identifier");
	print $fh  $identifier->value, " \n";
    }
    close $fh;

    
}
