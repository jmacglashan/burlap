[![Snap CI branch](https://img.shields.io/snap-ci/ThoughtWorksStudios/eb_deployer/master.svg?maxAge=2592000)]() [![Maven Central](https://maven-badges.herokuapp.com/maven-central/edu.brown.cs.burlap/burlap/badge.svg)](https://maven-badges.herokuapp.com/maven-central/edu.brown.cs.burlap/burlap) [![license](https://img.shields.io/badge/license-LGPL-blue.svg)](http://www.gnu.org/licenses/lgpl-3.0.en.html)  ![java6](https://img.shields.io/badge/java-6-blue.svg) ![java7](https://img.shields.io/badge/java-7-blue.svg) ![java8](https://img.shields.io/badge/java-8-blue.svg)

BURLAP
======

Repository for the ongoing development of the Brown-UMBC Reinforcement Learning And Planning (BURLAP) java library.

BURLAP is a java code library for the use and development of single or multi-agent planning and learning algorithms and domains to accompany them. At the core of the library is a rich state and domain representation framework based on the object-oriented MDP (OO-MDP) [1] paradigm that facilitates the creation of discrete, continuous, or relational domains that can consist of any number of different "objects" in the world. Planning and learning algorithms range from classic forward search planning to value function-based stochastic planning and learning algorithms. Also included is a set of tools such as an extendable experiment shell and a common framework for the visualization of domains and agent performance.

## Important Links

* Home page: http://burlap.cs.brown.edu
* Written tutorials: http://burlap.cs.brown.edu/tutorials/index.html
* Example code repository: http://github.com/jmacglashan/burlap_examples/
* Discussion board: https://groups.google.com/forum/#!forum/burlap-discussion
* BURLAP ROS Bridge library: https://github.com/h2r/burlap_rosbridge
* Minecraft Interface: http://github.com/h2r/burlapcraft

## Linking

BURLAP now fully supports Maven and is indexed on Maven Central, so all you need to do to have your Maven project link to BURLAP is add the following to the `<dependencies>` section of your project's pom.xml file:
```
<dependency>
  <groupId>edu.brown.cs.burlap</groupId>
  <artifactId>burlap</artifactId>
  <version>2.1.1</version>
</dependency>
```

Alternatively, you can compile from the source using either Maven or, for the time being Ant. Eventually we may be phasing out Ant support to minimize keeping track of local dependencies.

## Compiling

### Maven
The recommended builds sytem for BURLAP is [Maven](https://maven.apache.org/). If you have Maven intalled already then use the follow commands for the desired operation from the same directory as the code.

Create a jar file with sources and Java doc in the target directory (will be created):
```
mvn package
```
Install to your local Maven repository: 
```
mvn install
```

### Ant

For the time being, you can also use ant as a build system. But we will be phasing this out for Maven.

Compile a jar file into the dist directory:
```
ant dist
```

Compile  java doc into the doc folder:
```
ant doc
```

## Older versions
Github branches contain older versions of the code repository. See branches v1 and v2 for the version 1 and version iterations of BURLAP.

## References
1. Diuk, C., Cohen, A., and Littman, M.L.. "An object-oriented representation for efficient reinforcement learning." Proceedings of the 25th international conference on Machine learning (2008). 240-270.
