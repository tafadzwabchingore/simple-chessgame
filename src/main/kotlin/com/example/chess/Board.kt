package com.example.chess.models

class Board {
    // 8x8 board where board[r][c] is Piece? (null means empty)
    val board: Array<Array<Piece?>> = Array(8) { Array<Piece?>(8) { null } }

    init { initializeBoard() }

    fun initializeBoard() {
        for (r in 0..7) for (c in 0..7) board[r][c] = null
        for (i in 0..7) {
            board[1][i] = Pawn("white",1,i)
            board[6][i] = Pawn("black",6,i)
        }
        board[0][0] = Rook("white",0,0); board[0][1] = Knight("white",0,1); board[0][2] = Bishop("white",0,2)
        board[0][3] = Queen("white",0,3); board[0][4] = King("white",0,4); board[0][5] = Bishop("white",0,5)
        board[0][6] = Knight("white",0,6); board[0][7] = Rook("white",0,7)

        board[7][0] = Rook("black",7,0); board[7][1] = Knight("black",7,1); board[7][2] = Bishop("black",7,2)
        board[7][3] = Queen("black",7,3); board[7][4] = King("black",7,4); board[7][5] = Bishop("black",7,5)
        board[7][6] = Knight("black",7,6); board[7][7] = Rook("black",7,7)
    }

    fun boardAsTextRows(): List<String> {
        val rows = mutableListOf<String>()
        for (r in 7 downTo 0) {
            val sb = StringBuilder()
            for (c in 0..7) {
                val p = board[r][c]
                sb.append(p?.symbol() ?: ".")
                sb.append(" ")
            }
            rows.add("${r+1} | ${sb}")
        }
        rows.add("    a b c d e f g h")
        return rows
    }

    fun findKing(color:String): Pair<Int,Int>? {
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c]
            if (p is King && p.color == color) return Pair(r,c)
        }
        return null
    }

    // Check if color is in check
    fun isInCheck(color:String): Boolean {
        val kp = findKing(color) ?: return false
        val (kr,kc) = kp
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c]
            if (p!=null && p.color != color) {
                if (p.isValidMove(r,c,kr,kc,board)) return true
            }
        }
        return false
    }

    // Check if color has any legal moves (simulate)
    fun hasLegalMoves(color:String): Boolean {
        for (sr in 0..7) for (sc in 0..7) {
            val p = board[sr][sc] ?: continue
            if (p.color != color) continue
            for (er in 0..7) for (ec in 0..7) {
                val dest = board[er][ec]
                if (dest != null && dest.color == color) continue
                if (!p.isValidMove(sr,sc,er,ec,board)) continue
                // simulate
                val tmp = board[er][ec]
                board[er][ec] = board[sr][sc]
                board[sr][sc] = null
                val leaves = isInCheck(color)
                // undo
                board[sr][sc] = board[er][ec]
                board[er][ec] = tmp
                if (!leaves) return true
            }
        }
        return false
    }

    companion object {
        // algebraic e2 -> (1,4)
        fun algebraicToCoords(sq:String): Pair<Int,Int>? {
            if (sq.length!=2) return null
            val file = sq[0] - 'a'
            val rank = sq[1] - '1'
            if (file in 0..7 && rank in 0..7) return Pair(rank, file)
            return null
        }
    }
}