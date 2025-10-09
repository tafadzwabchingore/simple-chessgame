package com.example.chess.models

import kotlin.math.abs

// Base piece sealed class and all specific subclasses
sealed class Piece(var color: String, open var row: Int, open var col: Int) {
    abstract val type: String
    var hasMoved: Boolean = false
    abstract fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board: Array<Array<Piece?>>): Boolean
    open fun symbol(): String = when (color) {
        "white" -> when (type) {
            "Pawn" -> "♟"; "Rook" -> "♜"; "Knight" -> "♞"; "Bishop" -> "♝"; "Queen" -> "♛"; "King" -> "♚"
            else -> "?"
        }
        else -> when (type) {
            "Pawn" -> "♙"; "Rook" -> "♖"; "Knight" -> "♘"; "Bishop" -> "♗"; "Queen" -> "♕"; "King" -> "♔"
            else -> "?"
        }
    }
}

class Pawn(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "Pawn"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        val dir = if (color=="white") 1 else -1
        val startRank = if (color=="white") 1 else 6
        if (sc==ec) {
            if (er == sr + dir && board[er][ec]==null) return true
            if (sr==startRank && er==sr+2*dir && board[sr+dir][sc]==null && board[er][ec]==null) return true
        }
        if (abs(sc-ec)==1 && er==sr+dir && board[er][ec]!=null && board[er][ec]!!.color != color) return true
        return false
    }
}

class Rook(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "Rook"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        if (sr!=er && sc!=ec) return false
        val rStep = when { er>sr -> 1; er<sr -> -1; else -> 0 }
        val cStep = when { ec>sc -> 1; ec<sc -> -1; else -> 0 }
        var r = sr + rStep
        var c = sc + cStep
        while (r!=er || c!=ec) {
            if (board[r][c]!=null) return false
            r += rStep; c += cStep
        }
        return true
    }
}

class Knight(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "Knight"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        val rd = abs(sr-er); val cd = abs(sc-ec)
        return (rd==2 && cd==1) || (rd==1 && cd==2)
    }
}

class Bishop(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "Bishop"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        if (abs(sr-er) != abs(sc-ec)) return false
        val rStep = if (er>sr) 1 else -1
        val cStep = if (ec>sc) 1 else -1
        var r = sr + rStep; var c = sc + cStep
        while (r != er) {
            if (board[r][c] != null) return false
            r += rStep; c += cStep
        }
        return true
    }
}

class Queen(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "Queen"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        if (sr==er || sc==ec) return Rook(color,sr,sc).isValidMove(sr,sc,er,ec,board)
        if (abs(sr-er) == abs(sc-ec)) return Bishop(color,sr,sc).isValidMove(sr,sc,er,ec,board)
        return false
    }
}

class King(color:String,row:Int,col:Int): Piece(color,row,col) {
    override val type = "King"
    override fun isValidMove(sr:Int, sc:Int, er:Int, ec:Int, board:Array<Array<Piece?>>): Boolean {
        val rd = abs(sr-er); val cd = abs(sc-ec)
        if (rd<=1 && cd<=1) return true
        if (!hasMoved && sr==er) {
            // kingside
            if (ec - sc == 2) {
                val rook = board[sr][7]
                if (rook is Rook && !rook.hasMoved && board[sr][5]==null && board[sr][6]==null) return true
            }
            // queenside
            if (sc - ec == 2) {
                val rook = board[sr][0]
                if (rook is Rook && !rook.hasMoved && board[sr][1]==null && board[sr][2]==null && board[sr][3]==null) return true
            }
        }
        return false
    }
}