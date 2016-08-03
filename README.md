[![Snap CI branch](https://img.shields.io/snap-ci/ThoughtWorksStudios/eb_deployer/master.svg?maxAge=2592000)]() [![Maven Central](https://maven-badges.herokuapp.com/maven-central/edu.brown.cs.burlap/burlap/badge.svg)](https://maven-badges.herokuapp.com/maven-central/edu.brown.cs.burlap/burlap) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]() ![java6](https://img.shields.io/badge/java-6-blue.svg) ![java7](https://img.shields.io/badge/java-7-blue.svg) ![java8](https://img.shields.io/badge/java-8-blue.svg)

BURLAP
======

Repository for the ongoing development of the Brown-UMBC Reinforcement Learning And Planning (BURLAP) java library.

BURLAP is a java code library for the use and development of single or multi-agent planning and learning algorithms and domains to accompany them. The library uses a highly flexible state/observation representation where you define states with your own Java classes, enabling support for domains that are discrete, continuous, relational, or anything else. Planning and learning algorithms range from classic forward search planning to value function-based stochastic planning and learning algorithms. Various tools are also included, such as an extendable experiment shell and a common framework for the visualization of domains and agent performance.

## Important Links

* Home page: http://burlap.cs.brown.edu
* Written tutorials: http://burlap.cs.brown.edu/tutorials/index.html
* Example code repository: http://github.com/jmacglashan/burlap_examples/
* Discussion board: https://groups.google.com/forum/#!forum/burlap-discussion
* BURLAP ROS Bridge library: https://github.com/h2r/burlap_rosbridge
* Minecraft Interface: http://github.com/h2r/burlapcraft

## Simple Example
Here is example code of creating a grid world problem, creating a Q-learning agent, running the agent for 100 episodes, and then visualizing the episodes after it's complete.

```
//define the problem
GridWorldDomain gwd = new GridWorldDomain(11, 11);
gwd.setMapToFourRooms();
gwd.setTf(new GridWorldTerminalFunction(10, 10));
SADomain domain = gwd.generateDomain();
Environment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));

//create a Q-learning agent
QLearning agent = new QLearning(domain, 0.99, new SimpleHashableStateFactory(), 1.0, 1.0);

//run 100 learning episode and save the episode results
List<Episode> episdoes = new ArrayList<>();
for(int i = 0; i < 100; i++){
	episdoes.add(agent.runLearningEpisode(env));
	env.resetEnvironment();
}

//visualize the completed learning episodes
new EpisodeSequenceVisualizer(GridWorldVisualizer.getVisualizer(gwd.getMap()), domain, episdoes);
```

## Linking

BURLAP builds using Maven and is indexed on Maven Central, so all you need to do to have your Maven project link to BURLAP is add the following to the `<dependencies>` section of your project's pom.xml file:
```
<dependency>
  <groupId>edu.brown.cs.burlap</groupId>
  <artifactId>burlap</artifactId>
  <version>3.0.1</version>
</dependency>
```

Alternatively, you can compile and install from the source using Maven. The current master branch may be ahead of the latest release on Maven Central, so if you compile and install from source, make sure your external project's POM dependency uses the version you installed.

## Compiling

BURLAP uses [Maven](https://maven.apache.org/). If you have Maven intalled already, then use the following commands for the desired operation from the same directory as the code.

Create a jar file with sources and Java doc in the target directory (will be created):
```
mvn package
```
Install to your local Maven repository: 
```
mvn install
```

If you want to install and skip the tests, use
```
mvn -DskipTests install
```


## Older versions
Github branches contain older versions of the code repository. Some are also available on Maven Central.
