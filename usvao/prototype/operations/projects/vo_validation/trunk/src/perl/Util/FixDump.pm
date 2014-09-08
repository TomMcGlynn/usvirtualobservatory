#
#
#
#
#
#
package Util::FixDump;


use Exporter ();
@ISA = qw(Exporter);

@EXPORT =  qw (fix_file);

use strict;
use warnings;
use File::Copy;

#############################################
#
##############################################
sub  fix_file
{
    my ($dir,$input) = @_;
    print "$dir/$input\n";
    open (I, "$dir/$input")
	|| die "cannot open registry download file\n";
    open (O, ">$dir/$input.o")
	|| die " cannot open fixed registry download file.";
    while (<I>)
    {
        next if ($_ =~ /(.*?)\<(\/|)xs\:(.*?)/ );
        next if ($_ =~ /(.*?)\<(\/|)diffgr:diffgram(.*?)/);
        next if ($_ =~ /(.*?)\<(\/|)DataSet(.*?)/);
        if ($_ =~ s/&lt;\?xml(.*?)&gt;//)
        {
            #print "$_\n";
           
        }
        
        if ($_ =~ /(.*?)(\<Table)(.*?)/)
        {
            print O "        $2\>";
            next;
        }
        print O $_;
        
    }
    close O;
    close I;
    unlink "$dir/$input";
    move ("$dir/$input.o", "$dir/$input");
    my $contents = do { local $/; local @ARGV = "$dir/$input"; <>}; 
    chmod 0664,  "$dir/$input";
    unlink "$dir/$input" if ($contents !~ /NewDataSet/);
}
1;

