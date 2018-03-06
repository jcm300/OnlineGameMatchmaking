# OnlineGameMatchmaking
[![Build Status](https://travis-ci.org/jcm300/OnlineGameMatchmaking.svg?branch=master)](https://travis-ci.org/jcm300/OnlineGameMatchmaking)

Implementation of a Matchmaking System for an Online Game. The project should follow a [Client-Server Model](https://en.wikipedia.org/wiki/Client%E2%80%93server_model) where Server-Client communicatoins should be made via TCP sockets.

The Matchmaking System should allow:

* User registration(username and password) and authentication 
* Play a game (start the phase of matchmaking, team formation and hero pick).
    * Team Formation
        * Each team should have 5 players
        * Each player should have a ranking 
        * The System chooses 10 players (maximum rank difference between players must no exceed one) and breaks them into two teams balanced by the average ranking of the players
    * Hero pick
        * There are 30 different hero's
        * At any given time during the pick period, a player should be able to check the choices his teammates are making
        * No two heroes of the same type should be allowed on the same team
        * There's a 30s timeout period in which a player can change is pick
        * After timeout period has ended, if all the players have successfuly chosen their hero the match starts, else the match ends prematurely
    * If a match successfuly starts it's result is randomly generated and the players W/L values are updated accordingly
          

We will not implement the game, but only the matchmaking, so the score of the games will be randomly generated and used to update the user ranks.
Developed in the context of the Distributed Systems course at Minho University
