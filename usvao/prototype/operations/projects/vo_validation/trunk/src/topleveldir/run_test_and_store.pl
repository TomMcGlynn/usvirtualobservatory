#!/usr/bin/perl -wT


{
    use strict; 
    use lib './';   
    use lib './java';
    use lib '/www/server/vo/validation';
    use CGI;
    use data::startup;
    use HTML::ErrorMessage;
    use HTML::Layout;
    use Switch;
    use HTML::VOheader;
    my $cgi = CGI->new(); 

    print "Content-type: text/html\n\n";
     
    $ENV{"PATH"} = "/usr/contrib/linux/bin/:/bin:/usr/bin:/bin/bash:/usr/ucb:/usr/bin/X11:/usr/etc:"
                  ."/usr/bin:/usr/local/bin:/usr1/local/bin:/bin:/software/jira/ant/ant/bin";
    BEGIN
    {
        $ENV{'CLASSPATH'} =  "/www/htdocs/vo/validation/java:"
                           ."/software/jira/software/class"
	                   .":/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar"
    }
    
    my $id  = detaint("id",$cgi->param("id")) if ($cgi->param("id"));
   
    #print header
    my @linknames = ('VAO Validation','Validation Docs','VAO Monitor','VAO Home', 'NVO Feedback');
    my $voheader = new HTML::VOheader("Validation Results", \@linknames);
    $voheader->printheader;

    print "Testing ID: $id<br>";
       
    #open (Log, ") || die "cannot open log file";
    #`/usr1/local/jdk1.6.0_35/bin/javac /www/htdocs/vovaltest/java/*java`; 
    #`date >> /www/htdocs/vovaltest/log`;
   
    eval
    { 
         
       print "no";
      # `/usr1/local/jdk1.6.0_31/bin/javac /www/htdocs/vovaltest/java/*java`;
        #`/usr1/local/java/bin/java -version`; 
      #system("javac /www/htdocs/vovaltest/java/*java");
       #system("/usr1/local/java.old/bin/java /www/htdocs/vovaltest/java/*java");
       system("/usr1/local/java/bin/java  RunValidation $id"); 
    };
    
    if ($@) 
    { 
      print "there was an error $@<br>";
     #`/usr1/local/java/bin  -version`;  
    }

    #print footer
    gen_footer_bas();   
    exit();
}
sub detaint
{
    my ($parname, $value) = @_;
    my $status; 
    
    
    switch($parname)
    {
        case "id"
         { 
            if  ($value =~ m/(ivo:\/\/.*[^\<\>\;])/){$value = $1;}
            else {$status = 1;}
            
        }

    }

    if ($status)
    {
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    #return;
    return $value;
 
}
