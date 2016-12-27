package com.example.eventuate

import java.util.Date

case class Match(matchId: String,creator: String, players: Set[String], date: Date, place: String)

case class Player(player: String)

case class ApologyAcceptance(matchId: String, player: String)