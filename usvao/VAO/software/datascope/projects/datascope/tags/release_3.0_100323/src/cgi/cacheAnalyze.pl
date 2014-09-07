#!/www/server/vo/inst/bin/perl
use strict;

# Handle the old cache from the previous generation
# of DataScope (curiously this handles the directory newcache

`./oldCacheAnalyze.pl`;
my $cache = "/www/htdocs/vo/cache";

open(OUT, ">>$cache/deletionLog");

print OUT "\n\nRuning cacheAnalyze at ".scalar(localtime)." for directory $cache \n";
my $outCount = 0;


opendir(DIR, $cache);
my @scans = readdir(DIR);
closedir(DIR);

my $dcount = 0;
my $xcount = 0;
foreach my $scan(@scans) {

    if ($scan eq "." || $scan eq "..") {
        next;
    }

    my $cacheDir = "$cache/$scan";
    if (!-d $cacheDir) {
        next;
    }

    my $age = -M "$cacheDir/date.created";
    $age = sprintf("%.1f", $age);
    if ($age < 1.5) {
        $dcount += 1;
	next;
    }
    
    opendir(DIR, "$cacheDir");
    my @files = readdir(DIR);
    closedir(DIR);

    my $fits  = 0;
    my $ql    = 0;
    my $other = 0;
    my $xml   = 0;
    my $ascii = 0;
    my @fitsArr;
    
    foreach my $file (@files) {

        next if ($file eq "."  || $file eq "..");

        if ($file =~ /\.fits(\.(Z|gz))?$/) {
            $fits += 1;
	    push(@fitsArr, $file);
        } elsif ($file =~ /\.(gif|jpg)$/) {
            $ql += 1;
        } elsif ($file =~ /\.xml$/) {
            $xml += 1;
        } elsif ($file =~ /\.tab/) {
            $ascii += 1;
        } else {
            $other += 1;
        }
    }

    my $fString = join(",", @fitsArr);
    $outCount += 1;
    print OUT <<EOT;

Summary for entry: $cacheDir

   Age         = $age
   
   FITS file count:      $fits
   Quicklook file count: $ql
   XML file count:       $xml
   ASCII Table count:    $ascii
   Other file count:     $other
   
   FITS files:           $fString
EOT

    `rm -rf $cacheDir`;
#    print "rm -rf $cacheDir\n";
    $xcount += 1;

}

print OUT "\nTotal of $outCount sources deleted\n\n";

print OUT "Daily summary: Scans retained: $dcount Scans deleted:  $xcount\n\n";
print OUT "**********************************\n\n";
close OUT;

print "Content-type: text/html\n\nDone!";
