#!/bin/bash
if [ ! -e javacc.jar ]; then
	echo "You need javacc.jar in this directory to compile ROLL successfully."
	echo "Please make it available before trying again"
	exit 1;
fi

BASEDIR=`pwd`
mkdir -p ROLL

cd "$BASEDIR"/src/main/javacc/roll/parser/ba/
java -cp "$BASEDIR"/javacc.jar javacc JBAParser.jj

cd "$BASEDIR"/src/main/java
JF=""
for i in `find . -name '*.java'` `find ../javacc/ -name '*.java'`; do JF="$JF $i"; done

CP=".:$BASEDIR/ROLL/"
for i in "$BASEDIR"/lib/*jar; do 
	CP=$CP:$i
done

javac -cp "$CP" -d "$BASEDIR"/ROLL/ $JF

cd "$BASEDIR"/ROLL
for j in "$BASEDIR"/lib/*.jar; do
	jar xf "$j"; 
done

mkdir -p META-INF
echo "Manifest-Version: 1.0" > META-INF/MANIFEST.MF
echo "Main-Class: roll.main.ROLL" >> META-INF/MANIFEST.MF
jar cfm "$BASEDIR"/ROLL.jar META-INF/MANIFEST.MF .

cd "$BASEDIR"
rm -r ROLL
