#!/usr/bin/perl -w
#
#   author: michael preciado
#   name: run_svnupdate.pl
#   description:
#     script updates the remote usvao svn repository with the latest
#     version of the local external monitor code. The script uses
#     a file called "mapping" that contains the paths to scripts
#     and directories that should be copied. The mapping file should
#     be in the same directory as this script.
#
#
#    
{
    use strict;
    use File::Copy;
    my $homedir = '/www/htdocs/vo/external_monitor';
    my $svndump = '/www/htdocs/vo/usvao_svn/external_monitor';
    my $svndir  = "/www/htdocs/vo/usvao_svn/external_monitor/trunk/src";
    my $svntop = "/www/htdocs/vo/usvao_svn/external_monitor/trunk/src/topleveldir";
    my $svntargs = "/www/htdocs/vo/external_monitor/data/mapping";
    my @svndirs = ($svndir,$svntop);

    #get a copy of the project from the svn repository 
    #`svn checkout 'svn+ssh://usvao-svn\@svn.usvao.org/usvao/prototype/operations/projects/externalmonitor'  $svndump`;

    #clean local extracted svn, delete all but .svn files
    clean_up(\@svndirs);
    
    #get src and target directories
    my $mapping  = get_mapping($svntargs);
    
    #copy new code to checked out svn area
    copy_src_to_svn($homedir, $svndir,$svntop,$mapping);
    

    #add data to svn repository 
    chdir "$svndir" || die "cannot change dirs";
    my @array = `svn status 2>&1`;
    svn_add_delete(\@array);

    my @newarray = `svn status 2>&1`;
    print "@newarray";


    #commit changes
    #`svn commit -m "updating external monitor with new sw"`;
}
sub clean_up
{
    my ($svndirs) = @_;
    foreach my $subdir (@$svndirs)
    {
	my $dh;
	opendir ($dh, "$subdir");
	close $dh;  
	my @a = grep { -d "$subdir/$_" && !/^\.\.?$/  && !/^\.svn$/} readdir($dh); 	
	foreach  my $dir (@a) 
	{
	    clean("$subdir/$dir");
	}
	closedir $dh;
    }
}
sub clean
{
    my ($dir) = @_;
    opendir (DIR, $dir);
    while (defined(my $file = readdir(DIR)))
    {
	next if ($file =~ /^\.svn$/);
	next if ($file =~ /^\.\.?$/);
	#print "$file\n";
	unlink "$dir/$file" unless (-d $file);	 
    }
    closedir DIR;    
}
sub svn_add_delete
{    
    my ($array) = @_;    
    foreach my $n (@$array) 
    {
	$n =~ s/^\s+//g;
	$n =~ s/\s+$//g;
	my ($status, $path) = (split /\s+/, $n);
	if ($status =~ /^(\?|\s+?)/)
	{
	    `svn add $path`;
	}
	elsif ($status =~ /^\!/)
	{
	    `svn delete $path`;
	}
    }
}
sub copy_src_to_svn
{
    my ($homedir,$svndir,$snvtop,$mapping) = @_;
    foreach my $src (keys %$mapping)
    {	 	
       if (! -e "$svndir/$mapping->{$src}"){
            mkdir "$svndir/$mapping->{$src}";
        }
        opendir (my $dh, "$homedir/$src");
        my @a = grep { -f "$homedir/$src/$_" && !/^\.\.?$/  && !/^\.svn$/} readdir($dh);
        
        foreach my $file (@a)
        {    #print "C: $file\n";           
            copy ("$homedir/$src/$file", "$svndir/$mapping->{$src}/$file");     
        }       
    }    
}
sub get_mapping
{

   my ($svntargs) = @_;
   my $hash = {};
   open (File,"$svntargs");
   while(<File>) 
   {
	my $line = $_;	
	chomp $line;
  	my ($src, $target) = (split /\,/,$line);
	$hash->{$src} = $target;	
    }
    close File;
    return $hash;
}
