
# simple test for HelloWorld class

use VAO::HelloWorld;
use Test::More qw(no_plan);

  require_ok( 'VAO::HelloWorld' );

  # test 2, do we get the message we expect?
  my $obj = VAO::HelloWorld->new();
  my $ret = $obj->getMessage();

  is($ret, "Hello World");

