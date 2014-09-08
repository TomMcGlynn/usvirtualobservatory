
# simple little package for testing out Hudson integration
# using Perl

package VAO::HelloWorld;

sub new ($) {
   my ($proto,$cap_node) = @_;
   my $self = bless ({}, $proto);

   $self->{message} = "Hello World";
   return $self;

}

sub getMessage($) { my $self = shift; return $self->{message}; }

1;
