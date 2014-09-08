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
    open (O, "$dir/$input")
	|| die "cannot open registry download file\n";
    open (C, ">$dir/$input.o")
	|| die " cannot open fixed registry download file.";
    while (<O>)
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
            print C "        $2\>";
            next;
        }
        print C $_;
        
    }
    close C;
    close O;
    unlink "$dir/$input";
    move ("$dir/$input.o", "$dir/$input");
    chmod 0664,  "$dir/$input";
}
1;

