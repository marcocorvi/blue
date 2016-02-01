/* gui.h
   version 1.0.2
   Graphical User Interface (GUI) for the game "blue"

   Author: marco corvi(Revision by Kyungmi Lim & marco)
   Date  : Apr 2001
   Revision: Mar 2002
         1. Previously, marking/unmarking the strategies was handled by two 
           separate functions depending on the situation. 
           The work is now done by drawStrategy().          
         2. Resizing of the window or interrupted activities can be 
           resumed properly.   
         3. Save button added to save a game.

*/
#ifndef GUI_H
#define GUI_H

#include <stdio.h>        // for sprintf, sscanf, fclose
#include <stdlib.h>       // for malloc, NULL
#include <string.h>       // for strcpy
#include <math.h>
#include <time.h>         // time
#include <unistd.h>       // sleep 

/* ============== X WINDOW STUFF ==================== */
#include <X11/Xlib.h>
#include <X11/Xos.h>
#include <X11/Xutil.h>
#include <X11/X.h>
#include <X11/cursorfont.h>
#include <X11/Xatom.h>




#define nMenu  8

#define CARD_NUMBER 54                   // number of cards
#define EMPTY_CARD  53                   // just the (black) background

#define MARKS        4                   // nr. mark colors

/*Play modes */ 
#define RING_BELL -1
#define STRATEGY 0
#define PLAYING  1

// Menu labels (used in the switch statement)
#define NEW       -1    // new game
#define RESTART   -2    // repeat the same game
#define BACKWARD  -3    // backward in the history list
#define FORWARD   -4    // forward in the history list
#define Strategy  -5    // planning strategy
#define PLAY      -6    // play mode
#define QUIT      -7    // quit the game
#define SAVE      -8    // save the game



/* The color structure: a forward linked list */
typedef struct myColor {
  XColor xc;                // the XColor
  char name[6];             // the color rgb name 
  struct myColor * next;    // link to the next color
} MYCOLOR;

/* The cards: display information */
typedef struct cmap {
  int w, h, c;              // width, height and colors number
  char * col;               // 
  char ** color;            // array of color rgb names  
  unsigned char * map;      // pixmap (xpm)
  unsigned long * pixel;    // pixels <-- NEW
  XImage * xi;              // X image
} CMAP;

/* The menu items: */
typedef struct cmenu {
  int x, y;             // item position (x horiz., y vertic.)
  char name[10];        // item label
} CMENU;


/* ------------------------------------------------------------------ */ 
int ringBell();
int resetLayout();
int initLayout(int level);
int getIndex(int x1, int y1);
int read_cmap(char * name, CMAP * map);
int remapColor(CMAP * mycard, int n1, int n2);
int setBlack(CMAP * map);
int doColorTable(CMAP * mycard, int n1, int n2);
int drawMenu();
int drawPlayMode( int mode );
int drawCountLabel(int c);
int clearCountLabel(int c);
int drawTitle(int seed);
int clearStrategy();
int drawWinFlag(int c);
int drawSave(int c, int flag );
int initGraphics();
int initColors();
int initWindow();
int initImages(CMAP * mycard, int n1, int n2); 
int drawDouble(int index);
int drawCard(int ic, int index);
int drawMarkBox();
int finalMouseClick();
int mouseClick(int * ii1, int * ii2, unsigned long * ev_time);
int initGUI(int level);
int drawStrategy( int place, int erase, int draw );
void switchMark();
/* ------------------------------------------------------------------ */ 

#endif // GUI_H
