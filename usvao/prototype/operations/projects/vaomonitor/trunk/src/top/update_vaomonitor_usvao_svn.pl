#!/usr/bin/perl -w
#
#   author: michael preciado
#   name: update_vaomonitor_usvao_svn.pl
#   description:
#     script updates the remote usvao svn repository with the latest
#     version of the local vaomonitor code. The script uses
#     a file called "mapping" that contains the paths to scripts
#     and directories that should be copied. The mapping file should
#     be in the same directory as this script.
#
#
#    
{
    use strict;
    use File::Copy;
    my $homedir = '/www/htdocs/vo/vaomonitor';
    my $svndump = '/www/htdocs/vo/usvao_svn/vaomonitor';
    my $svndir  = "/www/htdocs/vo/usvao_svn/vaomonitor/trunk/src";
    my $svntop = "/www/htdocs/vo/usvao_svn/vaomonitor/trunk/src/top";
    my $srctargs = "/www/htdocs/vo/vaomonitor/data/mapping";
    my @svndirs = ($svndir,$svntop);

    #get a copy of the project from the svn repository 
    #`svn checkout 'svn+ssh://usvao-svn\@svn.usvao.org/usvao/prototype/operations/projects/vaomonitor'  $svndump`;

    #clean local extracted svn, delete all but .svn files
    clean_up(\@svndirs);
    
    #get src and target directories
    my $mapping  = get_mapping($srctargs);
    
    #copy new code to checked out svn area
    copy_src_to_svn($homedir, $svndir,$svntop,$mapping);
    

    #add data to svn repository 
    chdir "$svndir" || die "cannot change dirs";
    my @array = `svn status 2>&1`;
    print "@array";
    svn_add_delete(\@array);

    my @newarray = `svn status 2>&1`;
    print "\n@newarray";


    #commit changes
    #`svn commit -m "updating vaomonitor with new sw"`;
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
	    next if ($dir eq 'GUITesting');
	    print "$dir\n";
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

	print "JJ: $n\n";
        $n =~ s/^\s+//g;
	$n =~ s/\s+$//g;
	my ($status, $path) = (split /\s+/, $n);
	if ($status =~ /\?/)
	{
	    `svn add $path`;
	}
	elsif ($status =~ /\!/)
	{
	    `svn delete $path`;
	}
    }
}
sub copy_src_to_svn
{
    my ($homedir,$svndir,$svntop,$mapping) = @_;
    foreach my $src (keys %$mapping)
    {	 	
	opendir(DIR, "$homedir/$src");
	my @files = readdir(DIR);
	closedir(DIR);
	if (! -e "$svndir/$mapping->{$src}"){mkdir "$svndir/$mapping->{$src}";}
	foreach my $file (@files)
	{	
	    next if ($file =~ /^\.\.?$/);
	    next if ($file =~ /^\.svn$/);
	    
	    copy ("$homedir/$src/$file", "$svndir/$mapping->{$src}/$file");    
	}
	closedir DIR;
    }
    

    opendir (my $dh, $homedir);
    my @scripts = grep { -f "$homedir/$_" &&  !/^\.\.?$/ } readdir($dh);
    closedir $dh;
    foreach my $file (@scripts) 
    {
	copy ("$homedir/$file", "$svntop/$file");
	#chmod "0766", "$svntop/$file";
    }

}
sub get_mapping
{

   my ($srctargs)  = @_;
   my $hash = {};
   open (File,"$srctargs");
   while(<File>) 
   {
	my $line = $_;	
	chomp $line;
	next if ($line =~ /^\#.*$/);
  	my ($src, $target) = (split /\,/,$line);
	$hash->{$src} = $target;	
    }
    close File;
    return $hash;
}
