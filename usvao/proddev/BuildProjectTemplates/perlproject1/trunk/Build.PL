use strict;
use warnings;
use Module::Build;

my $builder = Module::Build->new(
    module_name         => 'VAO',
    license             => 'perl',
    dist_author         => 'Brian Thomas <bthomas@noao.edu>',
    dist_abstract       => 'false',
    build_requires => {
        'Test::More' => '0.10',
    },
);

$builder->create_build_script();
