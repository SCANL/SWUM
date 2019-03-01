#!/usr/bin/perl -w

my @files = `ls phrasaldictionary*`;

foreach $file (@files) {
	chomp $file;
	open (FILE, "$file") or die "Couldn't open $file\n";
	my $last = "";
	while (my $line = <FILE>) {
		chomp $line;
		if ($last =~ /<tr>/ && $line =~ /<td>(.*)<\/td>/) {
			my $l = $1;
			$l =~ s/&nbsp;//g;
			print "$l\n";
		}
		$last = $line;
	}
	close FILE;
}