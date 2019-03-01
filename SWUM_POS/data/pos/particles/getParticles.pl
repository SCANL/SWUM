#!/usr/bin/perl -w

# perl getParticles.pl | sort | uniq

my $file = "phrasal_verbs.txt";
open (FILE, "$file") or die "Couldn't open $file\n";
while (my $line = <FILE>) {
   chomp $line;
   my @f = split(/\s+/, $line);
   
   if (defined $f[1]) { print "$f[1]\n"; }
}
close FILE;
