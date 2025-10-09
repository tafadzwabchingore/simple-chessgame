package com.example.chess.models

import kotlinx.serialization.Serializable

@Serializable
data class MoveRequest(val playerId: String, val from: String, val to: String)

@Serializable
data class MoveRecord(val fromR:Int, val fromC:Int, val toR:Int, val toC:Int, val pieceType:String, val captured: String? = null)

@Serializable
data class Player(val id:String, val name:String, val color:String)

@Serializable
data class GameStateDTO(val boardText: List<String>, val currentTurn:String, val players: List<Player>, val history: List<MoveRecord>, val status:String)