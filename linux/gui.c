/* gui.c
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
#include "marks.h"
#include "gui.h"

/* -----------------------------------------------------------
   The directory where the xpm pixmap files are:
   (can be defined in the Makefile)
*/
#ifndef PIXMAPS
#define  PIXMAPS  "../pixmap"
#endif

#ifndef BGCOLOR
#define  BGCOLOR  "navy blue" 
#endif

#ifndef FGCOLOR
#define FGCOLOR  "white"
#endif

#ifndef FONT18
#define FONT18 "-*-clean-*-*-*-*-*-160-*-*-*-*-*-*"
#define FONT10 "-*-clean-*-*-*-*-*-100-*-*-*-*-*-*"
#endif

/* ======= LAYOUT SPECIFICATIONS =================== */
#define DX     51      /* cards' width  76* 2/3 + 1*/
#define DY     79      /* cards' height 118 * 2/3 + 1*/
#define GAPX    5      /* h. gap between the two cards in the layout*/
#define GAPY   15      /* v. gap between the two cards in the layout*/
#define OFFX   25      /* horiz. offset */
#define OFFY   24      /* vert. skip between the menu bar and the game board*/

#define MYZERO  5      /* vertical offset of the menu items*/
#define MX     65      /* menus' width*/
#define MY     15      /* menus' height*/

/* ============== X WINDOW STUFF ==================== */

#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif

Display * theDisplay;       /* the graphic display*/
GC        theGC;            /* the graphic context*/
XGCValues xgcv;
int       theScreen;        /* the display screen*/
Screen  * pScreen;          /* same as above*/
int       theDepth;         /* the screen depth (15 bit planes ???)*/
Window    rootW;            /* the root window*/
Window    theWindow;        /* the window for the game */
Visual  * theVisual;
Colormap  theCmap;          /* the default colormap*/
Font      theFont;          /* display font*/
XSetWindowAttributes xswa;
unsigned long flag;
XColor    theBlack;
XColor    theWhite;
XColor    theMark[ MARKS ];

int markIndex;
char *  markNames[] = { "red", "green", "orange", "cyan" };
char * suits[]      = { "H",   "C",     "D",      "S" };
                    //   heart  club     diamond   spade
char * values[] = {" A", " 2", " 3", " 4", " 5", " 6", " 7", " 8", " 9",
                   "10", " J", " Q", " K" };

int play_mode = STRATEGY;

int sW, sH,                 /* root window dimensions*/
    wW, wH;                 /* game window dimensions*/
int i_initGUI = 0;          /* flag (checking if the GUI has been
initialized)*/

XEvent    event1, event2;   /* a couple of X-events*/
XEvent    event3, event4;   /* a couple of X-events*/

/* The color structure: a forward linked list */
MYCOLOR * rootColor;

/* The cards: display information */
CMAP mycard[CARD_NUMBER];

/* The menu items: */
CMENU  guiMenu[nMenu];
CMENU  guiCountLabel;

char * xpmdir = PIXMAPS;
/* ------------------------------------------------------------------ */ 
int ringBell() {
  XBell( theDisplay, 50);
  return 0;
}

/* ------------------------------------------------------------------ */ 
int resetLayout() {
  strcpy(guiCountLabel.name, "          ");
  return 0;
}

/* ------------------------------------------------------------------ */ 
int initLayout(int level) {
  int i;
  
  for (i=0; i<nMenu; i++) {                    /* init the menu*/
    guiMenu[i].x = OFFX + (MX+GAPX)*i;
    guiMenu[i].y = MYZERO;
  }
  strcpy(guiMenu[0].name, " New Game ");
  strcpy(guiMenu[1].name, " Restart  ");
  strcpy(guiMenu[2].name, " Backward ");
  strcpy(guiMenu[3].name, " Forward  ");
  strcpy(guiMenu[4].name, " Strategy ");
  strcpy(guiMenu[5].name, " Play     ");
  strcpy(guiMenu[6].name, " Quit     ");
  strcpy(guiMenu[7].name, " Save     ");
  guiCountLabel.x = OFFX + (MX+GAPX)*i;  /* init the count label*/
  guiCountLabel.y = MYZERO+10;
  resetLayout();
  return (0);
}
/* --------------------------------------------------------------------- */
/* The function getIndex() return the index of the element in the
   window to which the point (x1,y1) belongs:
     indices from 0 to 55 denotes the card position in the game board.
     negative indices(-8 to -1) are used for the items of the menu bar
*/
int getIndex(int x1, int y1) {
  int i,j;

  x1 -= OFFX;
  if ( y1 > (MYZERO + MY + OFFY) ) {   /* CARDS*/
    j = -1;
    y1 -= (MYZERO+MY+OFFY);
    if ( (y1 % (DY+GAPY)) < DY) { j = y1/(DY+GAPY); }

    i = -1;
    if ( (x1 % (DX+GAPX)) < DX) { i = x1/(DX+GAPX); }

    if (i>=0 && j>=0) 
      return (j*14+i);
  } else {
    if ( y1 > MYZERO && y1 < (MYZERO+MY) ) {
      if ( (x1 % (MX+GAPX)) < MX) 
        return -(1 + x1/(MX+GAPX));
    }
  }
  return -100;
}
    
  

/* ---------------------- READ A CARD_MAP ------------------------ */
/* name : file_name of the card pixmap (XPM)                       */
/* map  : CMAP structure for the card pixmap data                  */

int read_cmap(char * name, CMAP * map) {
  FILE * fp;
  int i, j, k, w, h, ww, hh, c=0, p, i0=5, j0;
  char line[200], ch;

  fp = fopen(name, "r");
  if (fp == NULL) {
    printf( "Unable to open file %s\n", name);
    return (-1);
  }
  for (i=0; i<i0; i++) {
    fscanf(fp, "%[^\n]s", line);
    fscanf(fp, "%c", &ch);
    if (i==3) {
      sscanf(&(line[1]), "%d%d%d%d", &w, &h, &c, &p);
      ww = DX; /* w/2 + 1;*/
      hh = DY; /* h/2 + 1;*/
      map->w = ww; map->h = hh; map->c = c;
      map->col = (char *)malloc(c*sizeof(char));
      map->color = (char **)malloc(c*sizeof(char *));
      for (j=0; j<c; j++) (map->color)[j]=(char *)malloc(6*sizeof(char));
      map->map = (unsigned char *)malloc(ww*hh*sizeof(char));
      map->pixel = (unsigned long *)malloc(ww*hh*sizeof(long));
      i0 = 6+c;
    }
    if ((i>=5) && (i<5+c)) {
      sscanf(&(line[1]), "%c", &((map->col)[i-5]));
      sscanf(&(line[6]), "%6s", ((map->color)[i-5]));
  } }

  for (i=0, j0=0; i<h; i++) {
    fscanf(fp, "%[^\n]s", line);
    fscanf(fp, "%c", &ch);
    if ( (i % 3) != 2) {                                /* 2 out of 3*/
      for (j=1; j<=w; j++) if ( ((j-1) % 3) != 2 ) {    /* 2 out of 3*/
        for (k=0; k<c; k++) if (map->col[k]==line[j]) break;
        (map->map)[j0]=k;
        j0 ++;
      }
    }
  }

  fclose(fp);
  return(0);

} 
/* -------------------------------------------------------------- */
/* The pixmaps of the mycard[] (from index n1 to index n2) are      */
/* replaced by the pixel field of the corresponding XColor:       */
/* initially the pixmap entries contain the index of the color    */
/* array (which has the RGB color name);                          */
/* the MYCOLOR structure with the same name is looked for         */
/* and the pixel of its XColor is put in the pixmap entry.        */

int remapColor(CMAP * mycard, int n1, int n2) {
  MYCOLOR * wkColor;
  int i, j, j0;
  char * name;
  for (i=n1; i<n2; i++) {
    j0 = mycard[i].w * mycard[i].h;
    for (j=0; j<j0; j++) {
      name = mycard[i].color[ mycard[i].map[j] ];
      for (wkColor=rootColor; wkColor!=NULL; wkColor=wkColor->next) 
        if (strcmp(name, wkColor->name) == 0) break; 
      mycard[i].pixel[j] = wkColor->xc.pixel;
    }
  }
  return(0);
}

int setBlack(CMAP * map) {
  int i, i0;
  map->w = DX; 
  map->h = DY;
  i0 = map->w*map->h;
  map->map = (unsigned char *)malloc(i0 * sizeof(char));
  map->pixel = (unsigned long *)malloc(i0 * sizeof(long));

  for (i=0; i<i0; i++) map->pixel[i] = theBlack.pixel;
  /* printf("Set black done.\n"); */
  return 0;
}

/* ------------------ DO THE  COLOR_TABLE ------------------------ */
/* Finds the colors that appear in the mycard[] pixmaps              */
/* Considering only the card from n1 to n2                         */
/* The colors form a linked list of MYCOLOR structures             */
/* each one with the color name (RGB format),                      */
/*               the XColor                                        */

int doColorTable(CMAP * mycard, int n1, int n2) {
  MYCOLOR * wkColor;
  int i, j;
  unsigned long r,g,b;
  int notFound;
  char * name;
  for (i=n1; i<n2; i++) for (j=0; j<mycard[i].c; j++) {
    name = mycard[i].color[j];
    notFound=1;
    for (wkColor=rootColor; wkColor!=NULL; wkColor=wkColor->next) {
      if (strcmp(name, wkColor->name) == 0) { notFound = 0; break; }
    }
    if (notFound) {
      wkColor = (MYCOLOR *)malloc(sizeof(MYCOLOR));
      strcpy(wkColor->name, name);

      (wkColor->xc).flags = DoRed | DoGreen | DoBlue ;
      sscanf(&(name[0]), "%2lx", &r);
      sscanf(&(name[2]), "%2lx", &g);
      sscanf(&(name[4]), "%2lx", &b);
      (wkColor->xc).red   = (unsigned short)(256*r);
      (wkColor->xc).green = (unsigned short)(256*g);
      (wkColor->xc).blue  = (unsigned short)(256*b);

      if (rootColor==NULL) { wkColor->next = NULL; }
      else                 { wkColor->next = rootColor; }
      rootColor=wkColor;

      /* fprintf(stderr, "New color %s %4ld %4ld %4ld\n", name, r, g, b); */
  } }
  return(0);
}
/* ------------------------------------------------------------------ */
int drawMenu() {
  int i;

  XSetForeground(theDisplay, theGC, theWhite.pixel);
  for (i=0; i<nMenu; i++) {
    XDrawImageString(theDisplay, theWindow, theGC, 
      guiMenu[i].x, guiMenu[i].y+10, guiMenu[i].name, strlen(guiMenu[i].name) );
  }
  drawMarkBox();
  XFlush(theDisplay);
  return 0;
}

/* ------------------------------------------------------------------ */
int drawPlayMode( int mode ) {
  switch (mode) {
    case STRATEGY: 
      XSetForeground(theDisplay, theGC, theWhite.pixel);
      XDrawImageString(theDisplay, theWindow, theGC, 
        guiMenu[5].x, guiMenu[5].y+10, 
        guiMenu[5].name, strlen(guiMenu[5].name) );
      XSetForeground(theDisplay, theGC, theMark[3].pixel);
      XDrawImageString(theDisplay, theWindow, theGC, 
        guiMenu[4].x, guiMenu[4].y+10, 
        guiMenu[4].name, strlen(guiMenu[4].name) );
      break;
/*    case PLAYING:*/
    default:
      XSetForeground(theDisplay, theGC, theMark[3].pixel);
      XDrawImageString(theDisplay, theWindow, theGC, 
        guiMenu[5].x, guiMenu[5].y+10, 
        guiMenu[5].name, strlen(guiMenu[5].name) );
      XSetForeground(theDisplay, theGC, theWhite.pixel);
      XDrawImageString(theDisplay, theWindow, theGC, 
        guiMenu[4].x, guiMenu[4].y+10, 
        guiMenu[4].name, strlen(guiMenu[4].name) );
      break;
  }
 return 1; 
}
      

/* ------------------------------------------------------------------ */
int drawCountLabel(int c) {
  /* fprintf(stderr, "drawCountLabel %d \n", c); */
  sprintf(guiCountLabel.name, " %4d ", c);
  XSetForeground(theDisplay, theGC, theWhite.pixel);
  XDrawImageString(theDisplay, theWindow, theGC, 
      guiCountLabel.x, guiCountLabel.y,
      guiCountLabel.name, strlen(guiCountLabel.name) );
  XFlush(theDisplay);
  return c;
}

/* ------------------------------------------------------------------ */
int clearCountLabel(int c) {
  sprintf(guiCountLabel.name, " %4d ", c);
  XSetForeground(theDisplay, theGC, theBlack.pixel);
  XFillRectangle(theDisplay, theWindow, theGC, 
      guiCountLabel.x, guiCountLabel.y - 15,
      45, 15);
  XFlush(theDisplay);
  return c;
}
/* ------------------------------------------------------------------ */ 
int drawTitle(int seed) {
  char title[40];
  sprintf(title,"  B L U E  - Seed %d ", seed);
  XStoreName(theDisplay, theWindow, title);
  XFlush(theDisplay);
  return 0;
}

/* ------------------------------------------------------------------ */ 
int clearStrategy() {
  int i,j,k;
  i = 14 * DX + 13 * GAPX;
  j = MYZERO + MY + OFFY - GAPY;


  XSetForeground(theDisplay, theGC, theBlack.pixel);
  for (k=0; k<4; ++k) {
    XFillRectangle( theDisplay, theWindow, theGC, OFFX, j+k*(DY+GAPY), i, GAPY);
  }
  return 0;
}
/* ------------------------------------------------------------------ */
int drawWinFlag(int c) {
  char msg[40];
  theFont = XLoadFont(theDisplay, FONT18 );
  XSetFont(theDisplay, theGC, theFont);
  sprintf(msg, "YOU WON. With only %d moves !!!", c);
  XDrawImageString(theDisplay, theWindow, theGC, 
      (wW-10*strlen(msg))/2, wH/2, msg, strlen(msg) );
  XFlush(theDisplay);
  theFont    = XLoadFont(theDisplay, FONT10 );
  XSetFont(theDisplay, theGC, theFont);
  return c;
}

/* ------------------------------------------------------------------ */
int drawSave(int c, int flag ) {
  char msg[40];
  theFont = XLoadFont(theDisplay, FONT18 );
  XSetFont(theDisplay, theGC, theFont);
  if ( !flag ) {
    XSetForeground(theDisplay, theGC, theMark[2].pixel);
    sprintf(msg, "Click \"Save\" to save the game.");
  } else {
    sprintf(msg, "The game with seed: %d is saved", c);
  }
  XDrawImageString(theDisplay, theWindow, theGC, 
      (wW-10*strlen(msg))/2, wH/2, msg, strlen(msg) );
  XFlush(theDisplay);
  sleep(2);
  XSetForeground(theDisplay, theGC,theBlack.pixel);
  XDrawImageString(theDisplay, theWindow, theGC, 
      (wW-10*strlen(msg))/2, wH/2, msg, strlen(msg) );
  XFlush(theDisplay);
  XSetForeground(theDisplay, theGC,theWhite.pixel);
  theFont    = XLoadFont(theDisplay, FONT10 );
  XSetFont(theDisplay, theGC, theFont);
  return c;
}
/* ------------------------------------------------------------------ */ 
int initGraphics() 
{
  theDisplay = XOpenDisplay(XDisplayName(NULL));
  if (theDisplay == NULL) {
    printf("You need to run the program under X-window.\n\n");
    exit(1);
  }
  theScreen  = XDefaultScreen(theDisplay);
  rootW      =  RootWindow(theDisplay, theScreen);
  theGC      = XDefaultGC(theDisplay, theScreen);
  theCmap    = XDefaultColormap(theDisplay, theScreen);
  theVisual  =  DefaultVisual(theDisplay, theScreen);
  theDepth   = XDefaultDepth(theDisplay, theScreen);
  // fprintf(stderr, "Visual %x Depth %d\n",  theVisual, theDepth);

  pScreen    = XDefaultScreenOfDisplay(theDisplay);
  sW         = XWidthOfScreen(pScreen);
  sH         = XHeightOfScreen(pScreen);
  theFont    = XLoadFont(theDisplay, FONT10 );
  XSetFont(theDisplay, theGC, theFont);

  return 0;
}

/* ------------------------------------------------------------------ */ 
int initColors( )
{
  int i;
  int status;
  MYCOLOR * wkColor;

  if (XAllocNamedColor(theDisplay, theCmap, BGCOLOR, &theBlack, &theBlack)==0) {
    fprintf(stderr, "Unable to allocate requested background color.\n");
    XAllocNamedColor(theDisplay, theCmap, "black", &theBlack, &theBlack);
  }

  if (XAllocNamedColor(theDisplay, theCmap, FGCOLOR, &theWhite, &theWhite)==0) {
    fprintf(stderr, "Unable to allocate the requested foreground color.\n");
    XAllocNamedColor(theDisplay, theCmap, "white", &theWhite, &theWhite);
  }

  for (i=0; i<MARKS; ++i ) {
    if (XAllocNamedColor(theDisplay, theCmap, markNames[i], 
          &theMark[i], &theMark[i]) == 0) {
      fprintf(stderr, "Unable to allocate the requested mark color %s.\n",
        markNames[i]);
      XAllocNamedColor(theDisplay, theCmap, "green", &theMark[i], &theMark[i]);
    }
  }

  wkColor = rootColor;
  while (wkColor != NULL) {
    
    status = XAllocColor(theDisplay, theCmap, &(wkColor->xc) );
    /*
    if ( strcmp(wkColor->name, "cf0000") == 0) {
      status = XAllocNamedColor(theDisplay, theCmap, "yellow", 
        &(wkColor->xc), &(wkColor->xc));
    } else if ( strcmp(wkColor->name, "000000") == 0) {
      status = XAllocNamedColor(theDisplay, theCmap, "black", 
        &(wkColor->xc), &(wkColor->xc));
    } else {
      status = XAllocNamedColor(theDisplay, theCmap, "white", 
        &(wkColor->xc), &(wkColor->xc));
    }
    */

    /*
    fprintf(stderr, "XallocColor return %s -> %d %6d %6d %6d %8x\n", 
      wkColor->name, status, 
      wkColor->xc.red, wkColor->xc.green, wkColor->xc.blue,
      wkColor->xc.pixel);
    */

    wkColor = wkColor->next;
  }
  XSetBackground(theDisplay, theGC, theBlack.pixel);
  XSetForeground(theDisplay, theGC, theWhite.pixel);
  return(0);
}

/* ------------------------------------------------------------------ */ 
int initWindow()
{
  wW = 2*OFFX + 14*DX + 13*GAPX;
  wH = MYZERO + MY + 2*OFFY + 4*DY + 3*GAPY;

  XSynchronize(theDisplay, 1);

  xswa.background_pixmap = None;
  xswa.background_pixel  = theBlack.pixel;
  xswa.border_pixmap     = CopyFromParent;
  xswa.border_pixel      = theBlack.pixel;
  xswa.bit_gravity       = ForgetGravity;
  xswa.win_gravity       = NorthWestGravity;
  xswa.backing_store     = Always;
  xswa.backing_planes    = 0xffffffff;       /* default*/
  xswa.backing_pixel     = 0x00000000;       /* default*/
  xswa.save_under        = FALSE;
  xswa.event_mask        = 
                           ButtonPressMask |
                           ButtonReleaseMask | 
                           ExposureMask |
                           /* ResizeRedirectMask |*/
                         0 ;
  xswa.do_not_propagate_mask = 
                           KeyPressMask |
                           KeyReleaseMask |
                           PointerMotionMask |
                           ButtonMotionMask |
                           /* ResizeRedirectMask |*/
                         0 ;
  xswa.override_redirect = FALSE;
  xswa.colormap          = theCmap;
  flag = 
         /* CWBackPixmap |*/
         CWBackPixel |
         /* CWBorderPixmap |*/
         /* CWBorderPixel |*/
         /* CWBitGravity |*/
         /* CWWinGravity |*/
         CWBackingStore |
         /* CWBackingPlanes |*/ 
         /* CWBackingPixel | */
         CWOverrideRedirect | 
         CWSaveUnder | 
         CWEventMask  | 
         CWDontPropagate | 
         CWColormap ;

  theWindow = XCreateWindow(theDisplay, rootW, 0, 0, wW, wH, 0, theDepth,
         InputOutput, theVisual, flag, &xswa);
  XMapWindow(theDisplay, theWindow);

  /* printf("Window done \n"); */
  return 0;
}

/* ------------------------------------------------------------------ */ 
int initImages(CMAP * mycard, int n1, int n2) 
{
  int i, j, k;
  unsigned char * data;
  unsigned long tmp;
  int  dstep = (theDepth+8)/8;
  int dsize = mycard[0].w * mycard[0].h;

  /* printf(" The Depth %d \n", theDepth);  // MY DEPTH IS 15 */

  for (i=n1; i<n2; i++) {
    /*
     * this is to take into account that "depth" can be more than a 
     * char
     */ 

    data = (unsigned char *)malloc(dstep * dsize * sizeof(char) );
    for (j=0; j<dsize; j++) {
      tmp = *(mycard[i].pixel + j);
      for ( k = 0; k<dstep; k++) {
        *(data+j*dstep + k) = tmp & 0xff;
        tmp >>= 8;
      }
    }
    mycard[i].xi = XCreateImage(theDisplay, theVisual, theDepth, ZPixmap,
      0, (char *)(data), mycard[i].w, mycard[i].h, 8, 0);
/*
    mycard[i].xi = XCreateImage(theDisplay, theVisual, theDepth, ZPixmap,
      0, (char *)(mycard[i].map), mycard[i].w, mycard[i].h, 8, 0);
*/
  }
  /* printf("XImages done \n"); */

  return(0);
}
/* ------------------------------------------------------------------ */
/* mark two possible places to move a card */
int drawDouble(int index) {
  int x, y;
  int row, col;
  row = index/14;
  col = index%14;

  x = OFFX + col*(DX+GAPX);
  y = MYZERO+MY+OFFY + row*(DY+GAPY);

  XSetForeground(theDisplay, theGC, theMark[0].pixel);
  XFillRectangle(theDisplay, theWindow, theGC, x, y, 10, 10);
  XFlush(theDisplay);
  XSetForeground(theDisplay, theGC, theWhite.pixel);
  return(0);
}

/* ------------------------------------------------------------------ */
/* Utility function that draws the card (of index ic) in the position
   (x,y) on the game window.
   THe size of the rectangle if hardcoded in.
*/
int drawCard(int ic, int index) {
  int x, y;
  int row, col;
  ic++;
  row = index/14;
  col = index%14;

  x = OFFX + col*(DX+GAPX);
  y = MYZERO+MY+OFFY + row*(DY+GAPY);
  if (ic<0) {
    XSetForeground(theDisplay, theGC, theWhite.pixel);
    XDrawRectangle(theDisplay, theWindow, theGC, x, y, 74, 116);
  } else {
    XPutImage(theDisplay, theWindow, theGC, mycard[ic].xi, 0, 0, 
       x, y, mycard[ic].w, mycard[ic].h);
  }
  XFlush(theDisplay);
  return(0);
}

/* ------------------------------------------------------------------ */ 
int drawMarkBox() 
{
  int i1, j1, k;
  int i = wW - MX - 4 * 30;
/*  int i = wW - OFFX - MX - 4 * 30;*/
  int j = MYZERO;

  /* printf("Draw mark box \n"); */

  for (k=0; k<MARKS; k++) {
    XSetForeground(theDisplay, theGC, theMark[ k ].pixel);

    for (i1=0; i1<25; i1++) {
      for (j1=0; j1<25; j1++) {
        if (mark_suit[k][ i1 + j1 *25 ] ) 
          XDrawPoint( theDisplay, theWindow, theGC, i+i1, j+j1);
      }
    }
    i += 40;
  }
      
  
  return 0;
}

/*--------------------------------------------------------------*/
int finalMouseClick() {
  int index;

  index = XPending(theDisplay);
  if (index > 0) { 
    XNextEvent(theDisplay, &event1);
    if ( event1.type == ButtonPress ) {
      XNextEvent(theDisplay, &event2);
      while (event2.type != ButtonRelease) XNextEvent(theDisplay, &event2);
      index = getIndex(event1.xbutton.x, event1.xbutton.y);
    }
  }
  
  return index;
}
/*--------------------------------------------------------------*/

  

/* =================================================================== */
/*
return value: 
   0:  mouse click to move a card
   1:  mouse click on a menu item
   2:  activities not directly concerning the game.
       note that the opening of the game window belongs to this case.
mouseClick passes also the indices(by reference) where
   ii1 : the mouse button has been pressed,
   ii2 : the mouse button has been released,
   and it returns only when the mouse has been pressed and released. 
   The following indices denote the game element:
     -8 .. -1 : menu buttons
     0 .. 55 : the card position on the game board
In addtion the ButtonRelease time is passed by reference.
*/
int mouseClick(int * ii1, int * ii2, unsigned long * ev_time) {
  int i1=-1, i2=-1;
 
  do { 
    XNextEvent(theDisplay, &event1);
    switch ( event1.type ) {
      case Expose:
        if (XPending( theDisplay ) <= 1) {
          if (event1.xexpose.count == 0)
            return (2);
        }
        break;
      case ButtonPress:   /*ButtonPress:4, ButtonRelease:5*/
        XNextEvent(theDisplay, &event2); 
        while (event2.type != ButtonRelease) XNextEvent(theDisplay, &event2);
        i1 = getIndex(event1.xbutton.x, event1.xbutton.y);
        if (i1>=0) {
          i2 = getIndex(event2.xbutton.x, event2.xbutton.y);
          *ev_time = event2.xbutton.time; 
        } else if (i1 > -10) {   /* MENU */
          *ii1 = -1;
          *ii2 = i1;
          /* printf("Menu index %d \n", *ii2);*/
          return (1);
        }
        break;
      default:
        /* printf("Event type %d \n", event1.type ); */
        break;
    } 
  } while (i2<0);

  *ii1 = i1;
  *ii2 = i2;
  return(0);
}

/* initGui must be called to initialize the structures used by the GUI */
int initGUI(int level) {
  int i;
  int n = CARD_NUMBER;
  char cardName[50];
  if (i_initGUI) return(-1);  /* check the flag of GUI initialized*/

  markIndex = 0;                /* mark color index*/
  for (i=0; i<n; i++) {
    if (i<10) sprintf(cardName, "%s/0%1d.xpm", xpmdir, i);
    else      sprintf(cardName, "%s/%2d.xpm", xpmdir, i);
    read_cmap(cardName, &(mycard[i]));
  }
  initLayout(level);
  rootColor = NULL;
  doColorTable(mycard, 0, n);
  initGraphics();
  setBlack(& mycard[ EMPTY_CARD ]);

  initColors();
  remapColor(mycard, 0, CARD_NUMBER-1);
  initWindow();
  initImages(mycard, 0, CARD_NUMBER-1);
  drawMenu();

  /* printf(" Nex Graphics done \n"); */
  i_initGUI = 1;           /* set the flag of GUI initialized*/
  return(0);
}

/* ------------------------------------------------------------------ */ 
 int drawStrategy( int place, int erase, int draw ) {
  int suit;
  int val;
  int row = place / 14;
  int col = place % 14;
  static char str[4] = {0, 0, 0, 0};

  int i1 = OFFX + col*(DX+GAPX);
  int i2 = i1 + DX;
  int j1 = MYZERO + MY + OFFY + row*(DY+GAPY) - 2 ;


  XSetForeground(theDisplay, theGC, theBlack.pixel);
  XFillRectangle( theDisplay, theWindow, theGC, 
    i1-GAPX, j1+2-GAPY, DX+2*GAPX, GAPY);
  if ( erase != -1 ) 
    XSetForeground(theDisplay, theGC, theBlack.pixel);
  if ( draw != -1 ) {
    suit = draw / 13;
    val  = draw % 13;
    XSetForeground(theDisplay, theGC, theMark[suit].pixel);
    XDrawLine( theDisplay, theWindow, theGC, i1, j1, i2, j1); 
    i1 += DX/2;
    str[0] = values[val][0];
    str[1] = values[val][1];
    str[2] = suits[suit][0];
    XDrawImageString(theDisplay, theWindow, theGC, i1, j1, str, 3);
  }
  return 0;
}
   
/* ------------------------------------------------------------------ */ 

