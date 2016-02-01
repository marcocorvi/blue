/* gameft.h
 *
 */
#ifndef GAME_FT_H
#define GAEM_FT_H

#include <stdio.h>           // for printf, putc, stderr
#include <stdlib.h>          // for NULL, exit
#include <math.h>            // for random
#include <string.h>          // for strcmp
#include <time.h>            // for time, ctime

#define NO_CARDS 52        //total number of cards
#define DIM_BRD  56        //dimension of the game board array

#define RING_BELL      -1
#define ACE_SWAP       -2
#define CHANGE_TO_WAIT -3
#define DO_NOTHING     -4
#define DOUBLE_MARK    -5

/*Play Modes*/
#define STRATEGY      0
#define PLAYING       1
#define PLAY_AND_WAIT 2
#define WAIT          3
#define DOUBLE_CLICK  4
#define BACFOR        5

/* ----------------------------------------------------------- */
void bailout(char * msg, int exit_value);
int shuffle( unsigned int seed );
int resetStrategy();
int restoreStrategy();
void setgame(unsigned int seed);
void saveBoard();
void restoreBoard();
void setgui(unsigned int seed);
int load_game( char * filename );
int saveGame(int seed, int mode, int flag);
int lookupEvents(
  int from, int to, unsigned long eventTime, unsigned long * eventTable);
int clearMark(int * doublePos);
int updateBoard(int * ref_from, int * ref_to, int * doublePos, int mode);
int updateStrategy( int from, int to, int fCard );
int doubleClickMove(int * pos, int fCard);
int isGameOver();
void PlayGame(unsigned int seed);


#endif // GAME_FT_H
