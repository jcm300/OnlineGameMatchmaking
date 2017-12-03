# OnlineGameMatchmaking
Implementation of a Matchmaking on a Online Game. In general we have to do a Client-Server model. Communications between Server and Clients are by TCP socket's.
Functionalities to implement:

* user register(username and password). 
* Play a game (start the fase of matchmaking, team formation and hero pick).
  * Team Formation
      * Each team have 5 players
      * Each player have a ranking 
      * The Server choose 10 players (their rankings could not variate more than one between each other, divided in two balanced teams by the ranking average.
  * Hero pick
      * There are 30 different hero's

We will not implement the game, but only the matchmaking, so the score of the games will be randomly generated and used to update the user ranks.
Developed in the context of the Distributed Systems course at Minho University
