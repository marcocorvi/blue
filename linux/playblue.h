/* playblue.h
   
   Header file for playblue.c version 1.0.2:
   contains prototypes of the functions called by the main().

   Author: Marco Corvi 
   Date    Apr. 2001
   Revision: Apr. 2001, Kyungmi Lim:
            1. The original version was written in one single file, gui.c.
               The logic of the game is now separated from gui part
               and put into "gameft.c".
            2. History struct and functions are rewritten.
            3. Other minor changes.
   Bugs:   resizing of the window.
   Revision: Mar. 2002, Kyungmi Lim & marco corvi
            1. "drawStrategy()" now replaces  "drawMark()" and "markStrategy()"
                to mark/unmark strategy in gui.c.
            2. Resizing the window/resuming interrupted activities 
               is now incorporated. 
            3. save() is added to save a game. "blue" can now be played
               with an option -f filename : to load a game saved in a file
               or -s seed: to give a seed for shuffle.

*/

/*Play Modes*/
#define STRATEGY      0
#define PLAYING       1
#define PLAY_AND_WAIT 2
#define WAIT          3
#define DOUBLE_CLICK  4
#define BACFOR        5


int  initGUI(int);               // initialize the Graphical User Interface
void setgame(unsigned int);   // set-up a new game
void saveBoard();
void  setgui(unsigned int);   // set the game on the GUI
void PlayGame(unsigned int);  // play the game (core of the game)
