# Overview

This project is a console-based chess application written in C++. It demonstrates the use of object-oriented programming principles, including inheritance, polymorphism, and encapsulation, while also incorporating STL containers for efficient data management. The program simulates a functional chess game with support for legal piece movement, check and checkmate detection, castling, and pawn promotion. The board is rendered in the terminal using a clear, text-based layout that allows players to follow the game step by step.

The purpose of developing this software is to strengthen my skills in modern C++ programming by building a non-trivial application that combines multiple concepts: class hierarchies, dynamic memory management with smart pointers, and structured game logic. Writing this chess game provided valuable practice in designing maintainable code, implementing game rules through conditionals and loops, and improving the user experience by presenting a readable board in the console.

Ultimately, this project serves both as a practical demonstration of C++ capabilities and as a learning tool to deepen my understanding of how to design interactive, object-oriented systems from the ground up.

[Software Demo Video](http://youtube.link.goes.here)

# Development Environment

This software was developed using the C++ programming language, chosen for its 
performance, flexibility, and strong support for object-oriented programming.  
C++ is well-suited for modeling real-world systems like chess because it allows 
the use of inheritance, polymorphism, and dynamic memory management.

## Tools Used
- **Compiler**: GNU g++ (via MinGW on Windows) / Clang++ (on other platforms).
- **Editor/IDE**: Visual Studio Code, configured with the C++ extension for 
  IntelliSense, debugging, and build tasks.
- **Build System**: Simple command-line compilation using g++.
- **Operating System**: The program is cross-platform but was tested primarily 
  on Windows 10. Conditional includes allow compatibility with Windows-specific 
  console features.

## Libraries Used
- <iostream>: Provides input/output functionality for console interaction.
- **<vector>**: STL container used to represent the 8x8 chess board.
- **<string>**: Handles user input and algebraic chess notation (e.g., "e2e4").
- **<memory>**: Provides smart pointers (`std::unique_ptr`) for safe, 
  automatic memory management of chess pieces.
- **<cctype>**: Used for character handling (e.g., tolower, isalpha).
- **<windows.h>**: (Windows-only) Used for console configuration when running 
  on a Windows system.

# Useful Websites

{Make a list of websites that you found helpful in this project}

- [Programming with Mosh](https://www.youtube.com/watch?v=ZzaPdXTrSb8&t=1303s)
- [GeeksforGeeks](https://www.geeksforgeeks.org/cpp/c-plus-plus/)
- [Microsoft C++ Language Reference](https://learn.microsoft.com/en-us/cpp/cpp/cpp-language-reference?view=msvc-170)
- [W3Schools](https://www.w3schools.com/cpp/)

# Future Work

- **Item 2: Undo and Move History**  
  Add functionality to store all moves in a history log, with the ability to 
  undo moves and step backwards through the game.

- **Item 3: Save and Load Game**  
  Allow the game to reloaded later, so players 
  can pause and continue matches.

- **Item 4: AI Opponent**  
  Introduce a computer-controlled player that can calculate and make moves 
  against a human opponent.

- **Item 6: Better Checkmate/Stalemate Detection**  
  Refine the endgame logic to properly handle all edge cases, such as stalemate, 
  threefold repetition, and insufficient mating material.

- **Item 7: Graphical Interface**  
  Make the game more visually appealing.