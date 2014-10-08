Mutli-Agent Taxi System
=======================

A showcase of a multi-agent environment where the agents are taxi drivers.

Environment
=======================

The environment is a 5X5 block area.
There can be 3 to 4 agents on the same 5X5 environment where some cooperate with each other and some compete.
Each agent has an internal score counting towards his success. The teams success is calculated by adding the scores.
Each move an agent makes he loses -1 point. When disembarking or embarking a client, the agent loses -1 points. The succesfull disembark though gives him a 20 point reward.

Specifically there are 2 agent teams: donkeys and roosters. Roosters are always competing the donkeys whereas the donkeys sometimes (randomly) cooperate with the roosters and other times they compete with them.
Same team members are always cooperative with each other.

Competition
=======================

These are the ways an agent can compete with another:

  1. Blocking the opposing agent's path (two opposing agents can never be on the same block otherwise a crash occurs which deducts -10 from the agent's score).
  2. Stealing a client
  
Cooperation
=======================

These are the ways an agent can cooperate with another:

  1. Informing another agent of the position of a client. This can happen when two agents from the same team are in the same block but the "conversation" can be heard in neighbouring blocks by opposing agents.
  2. Creating more effective blocks for an opposing agent.
  
Execution
=======================

  1. Download the `Release` folder
  2. Open the CommandPrompt inside the folder
  3. type the command `java -jar Taxi_Agent.jar`
