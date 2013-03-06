#!/usr/bin/env bash

#
# Lorenz Leutgeb <e1127842@student.tuwien.ac.at>
#

echo USAGE OF THIS SCRIPT IS DEPRECATED

#JAVAC=$(whereis javac)
JAVAC="/usr/java/jdk1.7.0_05/bin/javac"

# directory where user-code and -classes are stored
PLAYERS=/home/lorenz/Dropbox/gamboo/java/players

# directory where framework sources (gamboo.*) is stored.
GAMBOO=/home/lorenz/Dropbox/gamboo/java

GAMES=/home/lorenz/Dropbox/gamboo/java/games

# libraries
LIBS=$(ls -1 $GAMBOO/lib/*.jar | tr "\n" ":")$(ls -1 $GAMES/*.jar | tr "\n" ":")$GAMBOO/gamboo.jar

# compilerflags for javac
FLAGS="-g -Xlint:unchecked -Xlint:deprecation -cp $LIBS"
#FLAGS="$FLAGS -Werror"

function log {
	echo -e "$(basename $0): $1"
}

clear

for i in $@ ; do
	if [ "$i" -eq "$i" -a -e $PLAYERS/$i ] 2> /dev/null ; then
		mkdir $PLAYERS/$i/tmp

		find $PLAYERS/$i/src -name *.java -exec $JAVAC -d $PLAYERS/$i/tmp $FLAGS -processor gamboo.annotation.ServiceProcessor {} +
		if [ $? -ne 0 ] ; then
			log "[$i/javac] failed"
		fi

		if [ -e $PLAYERS/$i.jar ] ; then
			log "[$i/jar] update"
			jar uf $PLAYERS/$i.jar -C $PLAYERS/$i/tmp .
		else
			log "[$i/jar] create"
			jar cf $PLAYERS/$i.jar -C $PLAYERS/$i/tmp .
		fi
		if [ $? -ne 0 ] ; then
			log "[$i/jar] failed"
		fi
		rm -rf $PLAYERS/$i/tmp
		log "[$i] ok"
	elif [ -e $GAMES/$i/ ] ; then
		mkdir $GAMES/$i/tmp
		
		find $GAMES/$i -name *.java -exec $JAVAC -d $GAMES/$i/tmp $FLAGS -sourcepath /tmp/ -processor gamboo.annotation.ServiceProcessor {} +
		if [ $? -ne 0 ] ; then
			log "[$i/javac] failed"
		fi

		if [ -e $GAMES/$i.jar ] ; then
			log "[$i/jar] update"
			jar uf $GAMES/$i.jar -C $GAMES/$i/tmp .
		else
			log "[$i/jar] create"
			jar cf $GAMES/$i.jar -C $GAMES/$i/tmp .
		fi
		if [ $? -ne 0 ] ; then
			log "[$i/jar] failed"
		fi
		rm -rf $GAMES/$i/tmp
		log "[$i] ok"
	else
		log "[$i] skip"
	fi
done
