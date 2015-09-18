BURLAP
======

This is the branch for version 1 of BURLAP. Use master for version 2.

Repository for the ongoing development of the Brown-UMBC Reinforcement Learning And Planning (BURLAP) java library.

BURLAP is a java code library for the use and development of single or multi-agent planning and learning algorithms and domains to accompany them. At the core of the library is a rich state and domain representation framework based on the object-oriented MDP (OO-MDP) [1] paradigm that facilitates the creation of discrete, continuous, or relational domains that can consist of any number of different "objects" in the world. Planning and learning algorithms range from classic forward search planning to value function-based stochastic planning and learning algorithms. Also included is a set of analysis tools such as a common framework for the visualization of domains and agent performance in various domains.

You can compile this code using ant. From the command line, type "ant dist" to produce the jar file, which will be created in the directory "dist". To compile the javadoc type: "ant doc" which will put the javadoc files in the directory "doc".

You can find more information on BURLAP by visiting our website at:
http://burlap.cs.brown.edu


1. Diuk, C., Cohen, A., and Littman, M.L.. "An object-oriented representation for efficient reinforcement learning." Proceedings of the 25th international conference on Machine learning (2008). 240-270.
