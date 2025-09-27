#include <iostream>   // For input/output (cin, cout)
#include <vector>     // For using std::vector to store the chess board
#include <string>     // For handling player input as strings (like "e2e4")
#include <memory>     // For smart pointers (std::unique_ptr to manage Pieces)
#include <cctype>     // For character handling (e.g., tolower, isalpha)

#ifdef _WIN32
#include <windows.h>  // Windows-specific functions (like enabling ANSI colors in console)
#endif

// ===========================================================
//  PIECE HIERARCHY (Base class + specific piece subclasses)
// ===========================================================

// Base class for all chess pieces
class Piece {
public:
    std::string color;   // "white" or "black"
    std::string type;    // Pawn, Rook, Knight, etc.
    int row, col;        // Current position on the board
    bool hasMoved = false; // Tracks whether piece has moved (important for castling/pawns)

    // Pure virtual function: all derived pieces must define their own movement rules
    virtual bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const = 0;

    virtual ~Piece() = default; // Virtual destructor for safe polymorphism

    // Returns the correct Unicode symbol for each piece depending on its color
    std::string getSymbol() const {
        if(color=="white"){
            if(type=="Pawn") return "♟"; if(type=="Rook") return "♜"; if(type=="Knight") return "♞";
            if(type=="Bishop") return "♝"; if(type=="Queen") return "♛"; if(type=="King") return "♚";
        } else {
            if(type=="Pawn") return "♙"; if(type=="Rook") return "♖"; if(type=="Knight") return "♘";
            if(type=="Bishop") return "♗"; if(type=="Queen") return "♕"; if(type=="King") return "♔";
        }
        return " ";
    }
};

// ------------------- Specific Pieces -------------------

// Pawn movement rules
class Pawn : public Piece {
public:
    Pawn(std::string c,int r,int l){ color=c; type="Pawn"; row=r; col=l;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const override {
        int dir = (color=="white")?1:-1; // White pawns move up, black pawns move down
        int startRank = (color=="white")?1:6;

        // Normal one-square forward
        if(sc==ec){
            if(er==sr+dir && !board[er][ec]) return true;
            // Two-square forward from start rank
            if(sr==startRank && er==sr+2*dir && !board[sr+dir][sc] && !board[er][ec]) return true;
        }
        // Diagonal capture
        if(abs(sc-ec)==1 && er==sr+dir && board[er][ec]) return true;
        return false;
    }
};

// Rook movement rules
class Rook : public Piece {
public:
    Rook(std::string c,int r,int l){ color=c; type="Rook"; row=r; col=l; hasMoved=false;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const override {
        if(sr!=er && sc!=ec) return false; // Must move in straight line
        int rStep = (er>sr)?1:((er<sr)?-1:0);
        int cStep = (ec>sc)?1:((ec<sc)?-1:0);
        int r=sr+rStep, c=sc+cStep;
        // Check path is clear
        while(r!=er || c!=ec){ if(board[r][c]) return false; r+=rStep;c+=cStep;}
        return true;
    }
};

// Knight movement rules
class Knight : public Piece {
public:
    Knight(std::string c,int r,int l){ color=c; type="Knight"; row=r; col=l;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>&) const override {
        int rd=abs(sr-er), cd=abs(sc-ec);
        return (rd==2 && cd==1) || (rd==1 && cd==2); // "L"-shaped move
    }
};

// Bishop movement rules
class Bishop : public Piece {
public:
    Bishop(std::string c,int r,int l){ color=c; type="Bishop"; row=r; col=l;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const override {
        if(abs(sr-er)!=abs(sc-ec)) return false; // Must move diagonally
        int rStep = (er>sr)?1:-1;
        int cStep = (ec>sc)?1:-1;
        int r=sr+rStep, c=sc+cStep;
        while(r!=er){ if(board[r][c]) return false; r+=rStep; c+=cStep;} // Check clear path
        return true;
    }
};

// Queen movement rules (combines rook + bishop logic)
class Queen : public Piece {
public:
    Queen(std::string c,int r,int l){ color=c; type="Queen"; row=r; col=l;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const override {
        if(sr==er || sc==ec){ Rook temp(color,sr,sc); return temp.isValidMove(sr,sc,er,ec,board);}
        if(abs(sr-er)==abs(sc-ec)){ Bishop temp(color,sr,sc); return temp.isValidMove(sr,sc,er,ec,board);}
        return false;
    }
};

// King movement rules (normal + castling)
class King : public Piece {
public:
    King(std::string c,int r,int l){ color=c; type="King"; row=r; col=l; hasMoved=false;}
    bool isValidMove(int sr,int sc,int er,int ec,
        const std::vector<std::vector<std::unique_ptr<Piece>>>& board) const override {
        int rd=abs(sr-er), cd=abs(sc-ec);
        if(rd<=1 && cd<=1) return true; // One square in any direction
        // Castling
        if(!hasMoved && sr==er){
            if(ec-sc==2){ // Kingside castle
                Piece* rook=board[sr][7].get();
                if(rook && rook->type=="Rook" && !rook->hasMoved && !board[sr][5] && !board[sr][6]) return true;
            }
            if(sc-ec==2){ // Queenside castle
                Piece* rook=board[sr][0].get();
                if(rook && rook->type=="Rook" && !rook->hasMoved && !board[sr][1] && !board[sr][2] && !board[sr][3]) return true;
            }
        }
        return false;
    }
};

// ===========================================================
//  BOARD CLASS (represents the whole chessboard + rules)
// ===========================================================
class Board {
public:
    std::vector<std::vector<std::unique_ptr<Piece>>> board; // 8x8 grid of smart pointers

    // Constructor initializes the 8x8 grid with nullptrs and sets up pieces
    Board(){ board.resize(8); for(auto &r:board) r.resize(8); initializeBoard();}

    // Put all pieces in their starting positions
    void initializeBoard(){
        for(int r=0;r<8;++r) for(int c=0;c<8;++c) board[r][c].reset();
        for(int i=0;i<8;++i){
            board[1][i]=std::make_unique<Pawn>("white",1,i);
            board[6][i]=std::make_unique<Pawn>("black",6,i);
        }
        // White pieces
        board[0][0]=std::make_unique<Rook>("white",0,0);
        board[0][1]=std::make_unique<Knight>("white",0,1);
        board[0][2]=std::make_unique<Bishop>("white",0,2);
        board[0][3]=std::make_unique<Queen>("white",0,3);
        board[0][4]=std::make_unique<King>("white",0,4);
        board[0][5]=std::make_unique<Bishop>("white",0,5);
        board[0][6]=std::make_unique<Knight>("white",0,6);
        board[0][7]=std::make_unique<Rook>("white",0,7);

        // Black pieces
        board[7][0]=std::make_unique<Rook>("black",7,0);
        board[7][1]=std::make_unique<Knight>("black",7,1);
        board[7][2]=std::make_unique<Bishop>("black",7,2);
        board[7][3]=std::make_unique<Queen>("black",7,3);
        board[7][4]=std::make_unique<King>("black",7,4);
        board[7][5]=std::make_unique<Bishop>("black",7,5);
        board[7][6]=std::make_unique<Knight>("black",7,6);
        board[7][7]=std::make_unique<Rook>("black",7,7);
    }

    // Prints a "large" styled board with coordinates
    void drawBigBoard() const {
        std::cout << "\n    a     b     c     d     e     f     g     h\n";
        std::cout << "  +-----+-----+-----+-----+-----+-----+-----+-----+\n";

        for (int r = 7; r >= 0; --r) {
            for (int line = 0; line < 3; ++line) {
                if (line == 1) std::cout << r + 1 << " |";
                else std::cout << "  |";

                for (int c = 0; c < 8; ++c) {
                    if (line == 1 && board[r][c])
                        std::cout << "  " << board[r][c]->getSymbol() << "  |";
                    else
                        std::cout << "     |";
                }
                std::cout << "\n";
            }
            std::cout << "  +-----+-----+-----+-----+-----+-----+-----+-----+\n";
        }
        std::cout << "    a     b     c     d     e     f     g     h\n\n";
    }

    // Checks if a given color is in check (their king attacked)
    bool isInCheck(const std::string& color) const {
        int kr=-1,kc=-1;
        // Find king position
        for(int r=0;r<8;++r) for(int c=0;c<8;++c)
            if(board[r][c] && board[r][c]->type=="King" && board[r][c]->color==color){ kr=r; kc=c; break;}
        if(kr==-1) return false; // No king found (shouldn’t happen)

        // See if any enemy piece can move to the king’s square
        for(int r=0;r<8;++r) for(int c=0;c<8;++c)
            if(board[r][c] && board[r][c]->color!=color && board[r][c]->isValidMove(r,c,kr,kc,board)) return true;
        return false;
    }

    // Checks if the given color has *any* legal move
    bool hasLegalMoves(const std::string& color){
        for(int sr=0;sr<8;++sr) for(int sc=0;sc<8;++sc){
            if(board[sr][sc] && board[sr][sc]->color==color){
                for(int er=0;er<8;++er) for(int ec=0;ec<8;++ec){
                    if(board[er][ec] && board[er][ec]->color==color) continue;
                    if(!board[sr][sc]->isValidMove(sr,sc,er,ec,board)) continue;

                    // Temporarily make the move
                    auto temp = std::move(board[er][ec]);
                    board[er][ec] = std::move(board[sr][sc]);
                    board[er][ec]->row=er; board[er][ec]->col=ec;

                    bool inCheck = isInCheck(color);

                    // Undo the move
                    board[sr][sc] = std::move(board[er][ec]);
                    board[sr][sc]->row=sr; board[sr][sc]->col=sc;
                    board[er][ec] = std::move(temp);

                    if(!inCheck) return true; // Found at least one legal move
                }
            }
        }
        return false;
    }
};

// ===========================================================
//  HELPER FUNCTIONS
// ===========================================================

// Converts move input like "e2e4" into numeric board coordinates
bool parseMove(const std::string& m,int &sr,int &sc,int &er,int &ec){
    if(m.length()!=4) return false;
    sc=m[0]-'a'; sr=m[1]-'1'; ec=m[2]-'a'; er=m[3]-'1';
    return sr>=0 && sr<8 && sc>=0 && sc<8 && er>=0 && er<8 && ec>=0 && ec<8;
}

// Pawn promotion helper: lets user pick a piece
std::unique_ptr<Piece> promotePawn(std::string color, int row, int col){
    char choice;
    std::cout << "Promote pawn! Choose (Q/R/B/N): ";
    std::cin >> choice;
    choice = toupper(choice);
    if(choice=='R') return std::make_unique<Rook>(color,row,col);
    if(choice=='B') return std::make_unique<Bishop>(color,row,col);
    if(choice=='N') return std::make_unique<Knight>(color,row,col);
    return std::make_unique<Queen>(color,row,col);
}

// ===========================================================
//  MAIN GAME LOOP
// ===========================================================
int main(){
#ifdef _WIN32
    SetConsoleOutputCP(CP_UTF8); // Ensures Unicode symbols (♞ etc.) display correctly on Windows
#endif
    Board gameBoard;                // Create and initialize the chessboard
    std::string currentPlayer="white",input;

    while(true){
        gameBoard.drawBigBoard();   // Print board
        std::cout<<currentPlayer<<" to move (e.g., e2e4) or 'exit': ";
        std::cin>>input;
        if(input=="exit") break;

        int sr,sc,er,ec;
        if(!parseMove(input,sr,sc,er,ec)){ std::cout<<"Invalid format!\n"; continue;}

        Piece* startPiece=gameBoard.board[sr][sc].get();
        if(!startPiece){ std::cout<<"No piece at start!\n"; continue;}
        if(startPiece->color!=currentPlayer){ std::cout<<"Not your piece!\n"; continue;}
        Piece* endPiece=gameBoard.board[er][ec].get();
        if(endPiece && endPiece->color==currentPlayer){ std::cout<<"Cannot capture your own piece!\n"; continue;}

        if(!startPiece->isValidMove(sr,sc,er,ec,gameBoard.board)){ std::cout<<"Invalid move for this piece!\n"; continue;}

        // ------------------ SIMULATE MOVE ------------------
        auto temp = std::move(gameBoard.board[er][ec]);
        gameBoard.board[er][ec] = std::move(gameBoard.board[sr][sc]);
        gameBoard.board[er][ec]->row = er;
        gameBoard.board[er][ec]->col = ec;

        // Handle castling if king moves 2 squares
        std::unique_ptr<Piece> castlingRook;
        int rookStartCol=-1, rookEndCol=-1;
        if(gameBoard.board[er][ec]->type=="King" && abs(ec-sc)==2){
            if(ec-sc==2){ rookStartCol=7; rookEndCol=5;} // Kingside
            else { rookStartCol=0; rookEndCol=3;}        // Queenside
            castlingRook = std::move(gameBoard.board[er][rookStartCol]);
            gameBoard.board[er][rookEndCol] = std::move(castlingRook);
            gameBoard.board[er][rookEndCol]->col = rookEndCol;
        }

        // Check if move leaves your own king in check
        if(gameBoard.isInCheck(currentPlayer)){
            std::cout << "Illegal move: Your king would be in check!\n";
            // Undo move
            gameBoard.board[sr][sc] = std::move(gameBoard.board[er][ec]);
            gameBoard.board[sr][sc]->row = sr;
            gameBoard.board[sr][sc]->col = sc;
            gameBoard.board[er][ec] = std::move(temp);
            if(rookStartCol!=-1){ gameBoard.board[er][rookStartCol] = std::move(gameBoard.board[er][rookEndCol]); gameBoard.board[er][rookStartCol]->col=rookStartCol;}
            continue;
        }

        // ------------------ FINALIZE MOVE ------------------
        gameBoard.board[er][ec]->hasMoved = true;

        // Handle pawn promotion
        if(gameBoard.board[er][ec]->type=="Pawn" && (er==7 || er==0)){
            gameBoard.board[er][ec] = promotePawn(currentPlayer, er, ec);
        }

        // Switch player
        currentPlayer = (currentPlayer=="white")?"black":"white";

        // Check for check, checkmate, stalemate
        if(gameBoard.isInCheck(currentPlayer)){
            if(!gameBoard.hasLegalMoves(currentPlayer)){
                gameBoard.drawBigBoard();
                std::cout<<currentPlayer<<" is checkmated! "<<((currentPlayer=="white")?"Black":"White")<<" wins!\n";
                break;
            } else {
                std::cout<<currentPlayer<<" is in check!\n";
            }
        } else if(!gameBoard.hasLegalMoves(currentPlayer)){
            gameBoard.drawBigBoard();
            std::cout<<"Stalemate! The game is a draw.\n";
            break;
        }
    }

    std::cout<<"Thanks for playing!\n";
    return 0;
}