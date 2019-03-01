#!/bin/bash

letters="A B C D E F G H I J K L M N O P Q R S T U V W X Y Z"

for t in $letters; do
	wget http://www.englishpage.com/prepositions/phrasaldictionary$t.html
done

wget http://www.englishpage.com/prepositions/window2.html
mv window2.html README.html