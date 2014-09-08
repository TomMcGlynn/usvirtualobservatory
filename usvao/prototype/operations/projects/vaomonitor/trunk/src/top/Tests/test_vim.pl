#!/usr1/local/bin/perl5 -w  
################################################################################
#
# PROGRAM: wesix_test
#
# AUTHORS:    Michael Preciado <preciado@milkyway.gsfc.nasa.gov>
#
# CREATED: 2007/07/24
#
# DESCRIPTION: Program tests the WESIX VO service.
#              
# HISTORY:
#  Fri Aug  3 15:58:08 EDT 2007
#
###############################################################################
{
    use strict;
    use Getopt::Long;
    use File::Basename;
    use Time::HiRes qw [time];
    use HTML::Form;
    use LWP::UserAgent;
    use URI::URL;
    use HTTP::Request;
    use HTTP::Response;
    use lib '/www/htdocs/cgi-bin/lib/nagios/libexec'; 
    use CGI;
    my $cgi = CGI->new();
    my $url = $cgi->param("url");
    print "Content-type: text/html\n\n";

    if  ($url)
    {
       	
	run_test($url);
    }
    exit(0);

}
#################################################
# run test
#################################################
sub run_test
{
    my ($url) = @_;

    #file to upload
    

    #get start time
    my $start_time        = time();  


    #URI->new("$url);
    print $url;
    my $ua                = LWP::UserAgent->new;   
    my $response          = $ua->get($url);
    my $content =  $response->content;
    my @home_forms  = HTML::Form->parse($response);
    print scalar(@home_forms);
    my $form_a       = shift @home_forms;
    print $form_a->dump;

}
#####
sub foo
{         
    #get wesix home page form inputs
    my @inputs  = $wesix_form->inputs;
    foreach my $input (@inputs)
    {
        my $name  = $input->name;
        my $value = $input->value;
        if ($name and  ($name eq "uplFile"))
        {
           
        }
        elsif ($name and ($name eq "xmatchCat"))
        {
            my @combos  =  $input->possible_values;
            foreach my $mem (@combos)
            {
                if ($mem and ($mem eq "IRAS"))
                {                   
                    $input->check;                  
                }
            }
        }               
    }
  
    #get wesix sextractor page 
    my $req_a             = $wesix_form->make_request('Submit'); 
    my $wesix_sextractor  = $ua->request($req_a);
    my $content           = $wesix_sextractor->content;
    #print "DEBUG\n", $content;

    #get wesix sextractor page form inputs
    my @wesix_sextractor_forms  = HTML::Form->parse($wesix_sextractor);
    my $wesix_sextractor_f      = shift @wesix_sextractor_forms;   
    my @sextractor_inputs       = $wesix_sextractor_f->inputs;
    foreach my $input (@sextractor_inputs)
    {
        my $name  =  $input->name;
        my $value =  $input->value;
        if ($name)
        {
            if (($name eq "outFields") || ($name eq "matchcols"))
            {
                my @combos = $input->possible_values;
                foreach my $mem (@combos)
                {                                   
                    if ($mem and (($mem eq "FLUX_ISO") or ($mem eq "ra") or ($mem eq "dec") or ($mem eq "name")))
                    {
                        $input->check;
                    }                     
                }
            }       
        }
    }

    #get wesix session page
    my $req_b                = $wesix_sextractor_f->make_request('submit');
    my $session_page         = $ua->request($req_b);
    my $session_page_content = $session_page->content;

    #parse up session page to extract a URL to a file.
    my @lines = (split /\n/,$session_page_content);
    my $xml_file_url;
    foreach my $line (@lines)
    {
        if ($line =~ /(.*?)href=(.*?)\> Here is the matched file(.*)/)
        {
            $xml_file_url = $2;
        }    
    }
    
    #get contents of xml file
    my $xml_response      = $ua->get($xml_file_url);
    my @xml_content       = $xml_response->content;
    print $xml_content[0];
    
}
