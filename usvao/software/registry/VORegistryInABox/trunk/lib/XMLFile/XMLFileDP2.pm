#  ---------------------------------------------------------------------
#   OAI-PMH2 XMLFile data provider
#    v2.0
#    July 2002
#  ------------------+--------------------+-----------------------------
#   Hussein Suleman  |   hussein@vt.edu   |    www.husseinsspace.com    
#  ------------------+--------------------+-+---------------------------
#   Department of Computer Science          |        www.cs.vt.edu       
#     Digital Library Research Laboratory   |       www.dlib.vt.edu      
#  -----------------------------------------+-------------+-------------
#   Virginia Polytechnic Institute and State University   |  www.vt.edu  
#  -------------------------------------------------------+-------------


package XMLFile::XMLFileDP2;


use Pure::EZXML;
use Pure::X2D;

use OAI::OAI2DP;
use vars ('@ISA');
@ISA = ("OAI::OAI2DP");

use Data::Dumper;


# constructor
sub new
{
   my ($classname, $configfile) = @_;
   my $self = $classname->SUPER::new ($configfile);

   # get configuration from file
   my $con = new Pure::X2D ($configfile);
   $self->{'repositoryName'} = $con->param ('repositoryName', 'XML-File Archive');
   $self->{'adminEmail'} = $con->param ('adminEmail', "someone\@somewhere");
   $self->{'archiveId'} = $con->param ('archiveId', 'XMLFileArchive');
   $self->{'recordlimit'} = $con->param ('recordlimit', 500);
   $self->{'datadir'} = $con->param ('datadir', 'data');
   $self->{'longids'} = $con->param ('longids', 'no');
   $self->{'filematch'} = $con->{'filematch'};
   $self->{'metadata'} = $con->{'metadata'};

   $self->{'resumptionseparator'} = '!';
   
   # remove default metadata information
   $self->{'metadatanamespace'} = {};
   $self->{'metadataschema'} = {};
   $self->{'metadatatransform'} = {};

   # add in seconds support
   $self->{'granularity'} = 'YYYY-MM-DDThh:mm:ssZ';
   
   # add in metadata formats from list in configuration
   foreach my $metadata (@{$con->{'metadata'}})
   {
      my $metadataPrefix = $metadata->{'prefix'}->[0];
      $self->{'metadatanamespace'}->{$metadataPrefix} = $metadata->{'namespace'}->[0];
      $self->{'metadataschema'}->{$metadataPrefix} = $metadata->{'schema'}->[0];
      if (defined $metadata->{'transform'}->[0])
      {
         $self->{'metadatatransform'}->{$metadataPrefix} = $metadata->{'transform'}->[0];
      }
      else
      {
         $self->{'metadatatransform'}->{$metadataPrefix} = '';
      }
   }

   # load in set mappings
   $self->{'setnames'} = {};
   if (-e 'setnames.xml')
   {
      my $parser = new Pure::EZXML;
      my $setnamedoc = $parser->parsefile ('setnames.xml')->getDocumentElement;
      
      foreach my $set ($setnamedoc->getElementsByTagName ('set'))
      {
         my $spec = $set->getElementsByTagName ('spec', 0)->item(0)->getChildNodes->toString;
         my $name = $set->getElementsByTagName ('name', 0)->item(0)->getChildNodes->toString;
         if ((defined $spec) && (defined $name))
         {
            $self->{'setnames'}->{$spec} = $name;
         }
      }
   }

   # load in complete database
   $self->read_database ('');
   
   bless $self, $classname;
   return $self;
}


# destructor
sub dispose
{
   my ($self) = @_;
   $self->SUPER::dispose ();
}


# create database of files, directories and other information
sub read_database
{
   my ($self, $directory) = @_;
   
   # clear database if top-level
   if ($directory eq '')
   {
      $self->{'database'} = { set2id => {}, id2set => {}, id2rec => {}, setname => {} };
   }
   
   # get contents of the current directory
   opendir (DIR, "$self->{'datadir'}$directory");
   my @files = readdir (DIR);
   closedir (DIR);
   
   # go through each entry in the directory
   foreach my $afile (@files)
   {
      # skip the directory markers
      if (($afile eq '.') || ($afile eq '..'))
      {
         next;
      }
   
      # if its a directory ...
      if (-d "$self->{'datadir'}$directory/$afile")
      {
         # create empty set container
         my $mainset = $directory;
         if ($mainset ne '')
         {
            $mainset = substr ($mainset, 1);
            $mainset =~ s/\//:/go;
            $mainset .= ':';
         }
         $self->{'database'}->{'set2id'}->{$mainset.$afile} = [];
         
         # add in set name if it exists
         $self->{'database'}->{'setname'}->{$mainset.$afile} = $mainset.$afile;
         if (-e "$self->{'datadir'}$directory/$afile/_name_")
         {
            open (FILE, "$self->{'datadir'}$directory/$afile/_name_");
            my $setname = <FILE>;
            close (FILE);
            
            if (defined $setname)
            {
               chomp $setname;
               $self->{'database'}->{'setname'}->{$mainset.$afile} = $setname;
            }
         }
         if (exists $self->{'setnames'}->{$mainset.$afile})
         {
            $self->{'database'}->{'setname'}->{$mainset.$afile} = 
              $self->{'setnames'}->{$mainset.$afile};
         }
      
         $self->read_database ("$directory/$afile");
      }

      # if its a file ...
      elsif (-f "$self->{'datadir'}$directory/$afile")
      {
         # screen out for files that do not match
         my $good = 0;
         foreach my $filematch (@{$self->{'filematch'}})
         {
            if ($afile =~ /$filematch/)
            {
               $good = 1;
            }
         }
         if (($good == 0) || ($afile eq '_name_'))
         {
            next;
         }
      
         # create full datestamp for file
         my $date = (stat("$self->{'datadir'}$directory/$afile"))[9];
         my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = gmtime ($date);
         my $datestamp = sprintf ("%04d-%02d-%02dT%02d:%02d:%02dZ", $year+1900, $mon+1, $mday, $hour, $min, $sec);
         
         # create identifier
        my $in = new Pure::X2D ("$self->{'datadir'}$directory/$afile");

         my $res = substr ($directory, 1);
            $res =~ s/\//:/g;
            $res = "SkyService" if ($res =~ /BrowserBasedService/);
            $res = "SkyService" if ($res =~ /GLUService/);
            $res = "TabularSkyService" if ($res =~ /ConeSearch/);
            $res = "TabularSkyService" if ($res =~ /SIAService/);
         my $identifier = $in->param ("$res/identifier", "");

         my $datestamp = $in->param ("reserved/datestamp", "");
         my $status = $in->param ("reserved/status", "");

         
         # create list of sets
         my $mainset = $directory;
         if ($mainset ne '')
         {
            $mainset = substr ($mainset, 1);
            $mainset =~ s/\//:/go;
         }
         my @splitsets = ();
         my $splitsettemp = '';
         foreach my $setpart (split (':', $mainset))
         {
            if ($splitsettemp ne '')
            {
               $splitsettemp .= ':';
            }
            $splitsettemp .= $setpart;
            push (@splitsets, $splitsettemp);
         }
         
         # add to identifier_to_set hash
         if (! exists $self->{'database'}->{'id2set'}->{$identifier})
         {
            $self->{'database'}->{'id2set'}->{$identifier} = [];
         }
         if ($mainset ne '')
         {
            push (@{$self->{'database'}->{'id2set'}->{$identifier}}, $mainset);
         }
         
         # add to set_to_identifier hash
         foreach my $aset (@splitsets, '')
         {
            # check if it isnt there already
            my $found = 0;
            foreach my $id (@{$self->{'database'}->{'set2id'}->{$aset}})
            {
               if ($id eq $identifier) { $found = 1; last; }
            }
            if ($found == 0)
            {
               push (@{$self->{'database'}->{'set2id'}->{$aset}}, $identifier);
            }
         }
         
         # add to identifier_to_record hash
         $self->{'database'}->{'id2rec'}->{$identifier} = [ $datestamp, "$directory/$afile" ];
      }
   }
}



# format header for ListIdentifiers
sub Archive_FormatHeader
{
   my ($self, $hashref, $metadataFormat) = @_;
   
   my ($datestamp, $pathname) = @{$self->{'database'}->{'id2rec'}->{$hashref}};
   
   # get status from datafile
        my $in = new Pure::X2D ("$self->{'datadir'}$pathname");
        my $status = $in->param ("reserved/status", "");

   $self->FormatHeader ($hashref,
                        $datestamp,
                        $status,
                        $self->{'database'}->{'id2set'}->{$hashref}
                       );
}


# retrieve records from the source archive as required
sub Archive_FormatRecord
{
   my ($self, $hashref, $metadataFormat) = @_;
   
   if ($self->MetadataFormatisValid ($metadataFormat) == 0)
   {
      $self->AddError ('cannotDisseminateFormat', 'The value of metadataPrefix is not supported by the repository');
      return '';
   }

   # get data file and tranform accordingly
   my ($datestamp, $pathname) = @{$self->{'database'}->{'id2rec'}->{$hashref}};
   my $metadataTransform = $self->{'metadatatransform'}->{$metadataFormat};
   open (FILE, "cat $self->{'datadir'}$pathname | $metadataTransform");
   my @data = <FILE>;
   close (FILE);
   my $fstr = join ('', @data);

   # get rid of XML declaration
   $fstr =~ s/^<\?[^\?]+\?>//o;

   # get status from datafile
        my $in = new Pure::X2D ("$self->{'datadir'}$pathname");
        my $status = $in->param ("reserved/status", "");

   $self->FormatRecord ($hashref,
                        $datestamp,
                        $status,
                        $self->{'database'}->{'id2set'}->{$hashref},
                        $fstr,
                        '',
                       );
}


# add additional information into the identification
sub Archive_Identify
{
   my ($self) = @_;
   
   my $identity = {};
   
   # add in description for toolkit
   if (! exists $identity->{'description'})
   {
      $identity->{'description'} = [];
   }
   my $desc = {
      'toolkit' => [[
         {
            'xmlns' => 'http://oai.dlib.vt.edu/OAI/metadata/toolkit',
            'xsi:schemaLocation' =>
                       'http://oai.dlib.vt.edu/OAI/metadata/toolkit '.
                       'http://oai.dlib.vt.edu/OAI/metadata/toolkit.xsd'
         },
         {
            'title'    => 'VTOAI XML-File Data Provider',
            'author'   => {
               'name' => 'Hussein Suleman',
               'email' => 'hussein@vt.edu',
               'institution' => 'Virginia Tech',
               'mdorder' => [ qw ( name email institution ) ],
            },
            'version'  => '2.0',
            'URL'      => 'http://www.dlib.vt.edu/projects/OAI/',
            'mdorder'  => [ qw ( title author version URL ) ]
         }
      ]]
   };
   push (@{$identity->{'description'}}, $desc);
   
   # add in external description containers
   opendir (DIR, ".");
   my @files = readdir (DIR);
   closedir (DIR);

   foreach my $identityfile (grep { /^identity[^\.]*\.xml$/ } @files)
   {
      open (FILE, "$identityfile");
      my @data = <FILE>;
      close (FILE);
      
      my $joineddata = join ('', @data);

      # get rid of XML declaration
      $joineddata =~ s/^<\?[^\?]+\?>//o;
      
      push (@{$identity->{'description'}}, $joineddata );
   }
   
   $identity;
}


# get full list of mdps or list for specific identifier
sub Archive_ListMetadataFormats
{
   my ($self, $identifier) = @_;
   
   if ((defined $identifier) && ($identifier ne '') && (! exists $self->{'database'}->{'id2rec'}->{$identifier}))
   {
      $self->AddError ('idDoesNotExist', 'The value of the identifier argument is unknown or illegal in this repository');
   }
   return [];
}


# get full list of sets from the archive
sub Archive_ListSets
{
   my ($self) = @_;

   delete $self->{'database'}->{'set2id'}->{''};

   [
      map {
         [ $_, $self->{'database'}->{'setname'}->{$_} ]
      } keys %{$self->{'database'}->{'setname'}}
   ];
}
                              

# get a single record from the archive
sub Archive_GetRecord
{
   my ($self, $identifier, $metadataFormat) = @_;
   
   if (! exists $self->{'database'}->{'id2rec'}->{$identifier})
   {
      $self->AddError ('idDoesNotExist', 'The value of the identifier argument is unknown or illegal in this repository');
      return undef;
   }

   return $identifier;
}


# list all records in the archive
sub Archive_ListRecords
{
   my ($self, $set, $from, $until, $metadataPrefix, $resumptionToken) = @_;

   # handle resumptionTokens
   my ($offset);
   if ($resumptionToken eq '')
   {
      $offset = 0;
   }
   else
   {
      my @rdata = split ($self->{'resumptionseparator'}, $resumptionToken);
      ($set, $from, $until, $metadataPrefix, $offset) = @rdata;
      if ((! defined $set) || (! defined $from) || (! defined $until) ||
          (! defined $metadataPrefix) || (! defined $offset))
      {
         $self->AddError ('badResumptionToken', 'The resumptionToken is not in the correct format');
         return '';
      }
   }

   my $count = 0;
   my @allrows = ();
   my $gotmore = 0;
   
   # check for existence of set
   if (! defined $self->{'database'}->{'set2id'}->{$set})
   {
      $self->AddError ('badArgument', 'The specified set does not exist');
      return '';
   }
   
   # got through all the identifiers in the set and extract those that match the other parameters
   foreach my $identifier (@{$self->{'database'}->{'set2id'}->{$set}})
   {
      my $datestamp = $self->{'database'}->{'id2rec'}->{$identifier}->[0];
      if ((($from eq '') || ($self->ToSeconds ($datestamp) >= $self->ToSeconds ($from, 1))) &&
          (($until eq '') || ($self->ToSeconds ($datestamp) <= $self->ToSeconds ($until))))
      {
         $count++;
         if ($count > $offset)
         {
            if ($count <= $offset+$self->{'recordlimit'}) 
            {
               push (@allrows, $identifier);
            }
            else
            {
               $gotmore = 1;
            }
         }
      }
   }

   # create a new resumptionToken if necessary
   $resumptionToken = '';
   if ($gotmore == 1)
   {
      $resumptionToken = join ($self->{'resumptionseparator'}, ($set,$from,$until,$metadataPrefix,$offset+$self->{'recordlimit'}));
   }
   if ($count == 0)
   {
      $self->AddError ('noRecordsMatch', 'The combination of the values of arguments results in an empty set');
   }

   ( \@allrows, $resumptionToken, $metadataPrefix, { 'completeListSize' => $count, 'cursor' => $offset } );
}


# list headers for all records in the archive
sub Archive_ListIdentifiers
{
   my ($self, $set, $from, $until, $metadataPrefix, $resumptionToken) = @_;

   # check for metadataPrefix if it is provided
   if ((defined $metadataPrefix) && ($metadataPrefix ne '') && ($self->MetadataFormatisValid ($metadataPrefix) == 0))
   {
      $self->AddError ('cannotDisseminateFormat', 'The value of metadataPrefix is not supported by the repository');
      return '';
   }
   
   $self->Archive_ListRecords ($set, $from, $until, $metadataPrefix, $resumptionToken);
}


1;

