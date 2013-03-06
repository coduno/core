# gamboo

[![Build Status](https://travis-ci.org/flowlo/gamboo.png)](https://travis-ci.org/flowlo/gamboo)

This is a coding competition platform, executing submitted code of multiple contestants within the same runtime.

Most of the work was done at the [Institute of Networked and Embedded Systems](http://nes.aau.at/) of the [University of Klagenfurt](http://aau.at/), funded by the [Austrian Research Promotion Agency](http://ffg.at/en).

It is still in an experimental stage. The website wrapping a user interface around the bare core you see here, is at [flowlo/gamboo-web](https://github.com/flowlo/gamboo-web).

## How to get it to run

Run

    mvn compile

to compile everything to `target/classes`, or

    mvn package
    
to generate a JAR file containing all dependencies.

## How to generate Javadocs

    mvn javadoc:javadoc
    
gives you HTML pages in `target/site/apidocs`.
