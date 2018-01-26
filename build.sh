#!/bin/bash
mvn package
if [ $? -eq 0 ]; then
	mkdir ROLL;
	cd ROLL;
	for j in ../target/*.jar; do
		jar xf "$j"; 
	done
	jar xf ../target/roll-library-*.jar
	jar cfm ../ROLL.jar META-INF/MANIFEST.MF .
	cd ..
	rm -r ROLL
fi
