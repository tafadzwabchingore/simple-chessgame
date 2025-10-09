package com.example.chess.service

import com.example.chess.models.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import java.util.UUID

// Manages multiple games in memory
object GameService {
    val games: MutableMap<String, GameInstance> = ConcurrentHashMap()

    // Create a new game and return id
    fun createGame(): String {
        val id = UUID.randomUUID().toString()
        games[id] = GameInstance(id)
        return id
    }

    fun getGame(id:String): GameInstance? = games[id]
}

// Single game instance contains board, players, history
class GameInstance(val id:String) {
    val boardModel = Board()
    val players: MutableMap<String, Player> = mutableMapOf()
    val history: MutableList<MoveRecord> = mutableListOf()
    var currentTurn: String = "white"
    var status: String = "waiting"

    // Join a player, assign color
    fun join(name:String): Player {
        val pid = UUID.randomUUID().toString()
        val color = when (players.size) {
            0 -> "white"
            1 -> { status = "playing"; "black" }
            else -> "spectator"
        }
        val p = Player(pid, name, color)
        players[pid] = p
        return p
    }

    // Make a move: returns null on success or string error
    fun makeMove(playerId:String, from:String, to:String): String? {
        val player = players[playerId] ?: return "Player not found"
        if (player.color != currentTurn) return "Not your turn"
        val coordsFrom = Board.algebraicToCoords(from) ?: return "Invalid 'from' square"
        val coordsTo = Board.algebraicToCoords(to) ?: return "Invalid 'to' square"
        val (sr, sc) = coordsFrom; val (er, ec) = coordsTo

        val piece = boardModel.board[sr][sc] ?: return "No piece at source"
        if (piece.color != player.color) return "That's not your piece"
        val dest = boardModel.board[er][ec]
        if (dest != null && dest.color == piece.color) return "Cannot capture your own piece"

        // Validate piece movement rules
        if (!piece.isValidMove(sr,sc,er,ec, boardModel.board)) return "Invalid move for this piece"

        // Simulate move
        val backupDest = boardModel.board[er][ec]
        val backupSrc = boardModel.board[sr][sc]
        boardModel.board[er][ec] = boardModel.board[sr][sc]
        boardModel.board[sr][sc] = null

        // Handle castling rook move temporarily
        var rookBackup: Pair<Int, Piece?>? = null
        if (piece is King && abs(ec - sc) == 2) {
            if (ec - sc == 2) {
                rookBackup = Pair(7, boardModel.board[er][7])
                boardModel.board[er][5] = boardModel.board[er][7]
                boardModel.board[er][7] = null
            } else {
                rookBackup = Pair(0, boardModel.board[er][0])
                boardModel.board[er][3] = boardModel.board[er][0]
                boardModel.board[er][0] = null
            }
        }

        // Check leaving own king in check
        val leavesInCheck = boardModel.isInCheck(player.color)
        if (leavesInCheck) {
            // undo
            boardModel.board[sr][sc] = boardModel.board[er][ec]
            boardModel.board[er][ec] = backupDest
            if (rookBackup != null) {
                val (sc0, _) = rookBackup
                if (ec - sc == 2) {
                    boardModel.board[er][7] = boardModel.board[er][5]; boardModel.board[er][5] = null
                } else {
                    boardModel.board[er][0] = boardModel.board[er][3]; boardModel.board[er][3] = null
                }
            }
            return "Illegal move: your king would be in check"
        }

        // Finalize
        boardModel.board[er][ec]?.hasMoved = true

        // Auto-promote pawn to Queen if reaches end (simplified)
        if (boardModel.board[er][ec] is Pawn && (er == 7 || er == 0)) {
            boardModel.board[er][ec] = Queen(player.color, er, ec)
        }

        val capturedType = backupDest?.type
        history.add(MoveRecord(sr, sc, er, ec, piece.type, capturedType))

        // switch turn
        currentTurn = if (currentTurn == "white") "black" else "white"
        status = "playing"

        // Check for end conditions
        if (boardModel.isInCheck(currentTurn)) {
            if (!boardModel.hasLegalMoves(currentTurn)) {
                status = "finished"
                return "${currentTurn} is checkmated"
            }
        } else {
            if (!boardModel.hasLegalMoves(currentTurn)) {
                status = "finished"
                return "stalemate"
            }
        }

        return null
    }

    fun getStateDTO(): GameStateDTO {
        val playersList = players.values.toList()
        return GameStateDTO(boardModel.boardAsTextRows(), currentTurn, playersList, history.toList(), status)
    }
}