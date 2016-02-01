/* playblue.c

   Author: Marco Corvi 
   Date:   Apr, 2001
   version 1.0.2: by Kyungmi Lim & marco, Mar 2002

   main function for card game "blue"

*/
#include <stdlib.h>      // NULL atoi
#include <stdio.h>       // printf
#include <unistd.h>      // getopt
#include <time.h>        // time
     
#include "playblue.h"    // contains the game board array and prototypes 
                         // of the functions called by the main  
#include "gameft.h"

#ifdef USE_LONG_OPTIONS
#  define _GNU_SOURCE
#  include <getopt.h>      // for getopt_long
#endif

#include "history.h"

extern HISTORY theHistory;
extern int play_mode;

int main(int argc, char ** argv) 
{
  int do_usage = 0;
  unsigned int seed = 0;  // seed for random setup of the game
  char * filename = NULL; // to load game from a file

  chdir( "/home/games/blue" );


  printf("\n     BLUE. Version 1.0.2\n"
    "     Copyright (c) 2002\n"
    "Blue comes with ABSOLUTELY NO WARRANTY.\n"
    "This software is free and you are welcome to redistribute it\n"
    "under certain conditions; for more details see the file \n"
    "COPYRIGHT included in the distribution.\n\n");

  initGUI(0);

  while (1) {
     int c;
#ifdef USE_LONG_OPTIONS
     int option_index = 0;
     static struct option long_options[] =
     {
       {"seed", 1, 0, 's'},
       {"file", 1, 0, 'f'},
       {0, 0, 0, 0}
     };

     if ( (c = getopt_long (argc, argv, "s:f:",
                        long_options, &option_index)) == -1)
#else
     if ( (c = getopt (argc, argv, "s:f:") ) == -1 )
#endif
     break;
     switch (c) {
       case 's': // seed
         seed = atoi(optarg);
         break;
       case 'f': // file
         filename = optarg;
         break;
       default:
         do_usage = 1;
     }
   }
   if (do_usage) {
     printf("Usage: blue [-f file] [-s seed]\n");
  }
/*init Game*/
  initHistory( & theHistory );
  resetStrategy();
  play_mode = STRATEGY;

  if (filename) {
    // printf("load game from %s\n", filename);
    // TODO : load game from file
    if ( ( seed = load_game( filename )) < 0) {
      seed = time( NULL );         // else use the default seed(=time)
      setgame(seed);
      saveBoard();
    }
  } else {
    if (! seed) {
      seed = time( NULL );         // else use the default seed(=time)
    }
    setgame(seed);
    saveBoard();
  }

  setgui(seed);
  PlayGame(seed);                // play the game( core of the game)
  return (0);
}
