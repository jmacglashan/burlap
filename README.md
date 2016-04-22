BURLAP
======

The master branch now contains the new BURLAP version 2; for version 1, use the branch "v1"

Repository for the ongoing development of the Brown-UMBC Reinforcement Learning And Planning (BURLAP) java library.

BURLAP is a java code library for the use and development of single or multi-agent planning and learning algorithms and domains to accompany them. At the core of the library is a rich state and domain representation framework based on the object-oriented MDP (OO-MDP) [1] paradigm that facilitates the creation of discrete, continuous, or relational domains that can consist of any number of different "objects" in the world. Planning and learning algorithms range from classic forward search planning to value function-based stochastic planning and learning algorithms. Also included is a set of analysis tools such as a common framework for the visualization of domains and agent performance in various domains.

## Linking

BURLAP now fully supports Maven and is indexed on Maven Central, so all you need to do to have your Maven project link to BURLAP is add the following to the `<dependencies>` section of your project's pom.xml file:
```
<dependency>
  <groupId>edu.brown.cs.burlap</groupId>
  <artifactId>burlap</artifactId>
  <version>2.1.0</version>
</dependency>
```

Alternatively, you can compile from the source using either Maven or, for the time being Ant. Eventually we may be phasing out Ant support to minimize keeping track of local dependencies.

## Compiling

### Maven

Compile with `mvn compile`. Create a jar file with `mvn package` This will create a jar in the target directory along with sources and Java doc. It will also create a jar that includes all the dependencies. Install to your local repository with `mvn install`.

### Ant

To compile with ant, type "ant dist" to produce the jar file, which will be created in the directory "dist". To compile the javadoc type: "ant doc" which will put the javadoc files in the directory "doc".


## More information
You can find more information on BURLAP, including tutorials, FAQS, Java docs, and library extensions, by visiting our website at:
http://burlap.cs.brown.edu

## References
1. Diuk, C., Cohen, A., and Littman, M.L.. "An object-oriented representation for efficient reinforcement learning." Proceedings of the 25th international conference on Machine learning (2008). 240-270.
