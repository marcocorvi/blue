/* gameft.c 
   
   Author: Marco Corvi(Revision by Kyungmi Lim & marco)
   Date:    Apr 2001
   Revision: Mar 2002 
             The part to handle the resizing of the window or resuming
             an interrupted activities is added.
             saveGame() added to save the current game.
             double click move to move a card added.
   Bugs:


*/

#include "gui.h"

#include "gameft.h"

#include "history.h"         // for HISTORY

/* Randoms*/
#define RAND()    rand()
#define SRAND(i)  srand(i)


int play_mode;
int board[DIM_BRD];        // Current status of the cards on the game board. 
                           // In the window the corresponding row and column
                           // can be obtained by taking the module and quotient
                           // of the index by 14
                           
int strBoard[DIM_BRD];     // strategy as index of the card at each position.
int deck[DIM_BRD];         // In deck[],
                           // at each random pick of a card index it is 
                           // stored in order of its selection.
                           // Then, the aces are moved to the ace row and
                           // in its place -1 is stored. This leads to
                           // the initial layout of the current game.

HISTORY theHistory;     //game history

// UTILITY FUNCTIONS ============================================
void bailout(char * msg, int exit_value) {
  printf("\n%s\n", msg);     // write the message "msg" 
  exit(exit_value);          // and exit
}

//Function prototypes 
/* 
 * Graphical User Interface (GUI) functions called
 * [already defined in gui.h]
 */
#if 0

int mouseClick(int *,int *, unsigned long *); 
                             // the indices pair of a GUI mouse click
int finalMouseClick();       // the indices pair of a GUI mouse click
int resetLayout();           // reset the GUI to the initial state
int drawDouble(int);         // mark 2 possible places to move a card
int drawCard(int,int);       // draws a card
int drawStrategy(int,int,int); //draws/removes a strategy mark 
int drawCountLabel(int);     // draws the count label
int clearCountLabel(int);    // clears the count label
int drawSave(int, int);      // draws the save message
int drawWinFlag(int);        // draws the winning message
int drawTitle(int);          // draws the window title
int drawPlayMode(int);       // highlight the current game mode
int clearStrategy();          

#endif

/* 
 * defined in gameft.h
 */
#if 0
void setgui(unsigned int);      // wrapper to set the game on the GUI 
void setgame(unsigned int);     // set a new game
void PlayGame(unsigned int);    // main logic of the game
#endif


HISTORY theHistory;          // Game history

/*-------------------------------------------------------------*/
int shuffle( unsigned int seed ) {
  int tmp[52];
  int i, j;
 
  for (i=0; i<52; i++)
     tmp[i] = i;

  srand( seed );
  for (i=52; i>1; i--) {
    j = rand( ) % i;
    deck[i-1] = tmp[j];
    for (; j<i-1; j++)
      tmp[j] = tmp[j+1];

  }
  deck[0] = tmp[0];

  return 0;
}

/*------------------------------------------------------------------*/
int resetStrategy() {
   int i;
   for( i = 0; i < DIM_BRD; i++ ) 
      strBoard[i] = -1;
   clearStrategy();
   return 0;
}
/*------------------------------------------------------------------*/
int restoreStrategy() {
   int i;
   for( i = 0; i < DIM_BRD; i++ ) 
     drawStrategy( i,  -1, strBoard[i] );
   return 0;
} 
/*------------------------------------------------------------------*/


/* ==================================================================
   setgame()
   sets the game "blue"
===================================================================== */
void setgame(unsigned int seed)
{ 
  int i,j;
  int ace_row;                  // Ace column
  int index;                    // picked card index

  
  SRAND(seed);
  shuffle(seed); 

  i = j = 0;
  ace_row = 0;
  do {
    if ( i%14 == 0) i++;
    index = deck[j];
    j++;
    if ( index%13 == 0 ) {      //ACE
       board[ace_row] = index;
       ace_row += 14;
       board[i] = -1;  
    } else {
      board[i] = index;
    }
    i++; 
  } while ( i < DIM_BRD );

}

/* =============================================================
Save the initial layout of the current game.
================================================================ */
void saveBoard() {
  int j;
  for ( j=0; j<DIM_BRD; j++) deck[j] = board[j];
}

/* =============================================================
Restore the initial layout of the current game.
================================================================ */
void restoreBoard() {
  int j;
  for ( j=0; j<DIM_BRD; j++) board[j] = deck[j];
}
/* =============================================================
setgui()  put the cards on the graphical user interface
          at the beginning of the game
================================================================ */
void setgui(unsigned int seed)
{
  int i;
  drawTitle(seed);

  for (i = 0; i < DIM_BRD; i++) 
    drawCard( board[i], i );
  drawPlayMode( play_mode );
}  /*  end setgui()  */



/*-------------------------------------------------------------------*/
int load_game( char * filename )
{
  char c;
  int _seed = 0;
  int _mode = 0;
  int _count = 0;
  int _flag;
  int _n_moves = 0;
  MOVE * _cursor = NULL;
  extern char* values[], * suits[];
  int i,j, suit,value;
  FILE * fp;
  char line[256];
  char * str;

  if ( (fp = fopen( filename, "r")) == NULL)
    return -1;
  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# SEED", 6) != 0);
  fscanf( fp, "%d", &_seed);
  printf("Seed %d\n", _seed);


  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# PLAY", 6) != 0);
  fscanf( fp, "%d", &_mode);
  play_mode =  _mode;


  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# INIT", 6) != 0);
  for (i=0; i<4; i++) {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
    str = line;
    for (j=0; j<14; ++j, str+=4) {
      if ( (*(str+1)) == '*') {
        deck[i*14+j] = -1;
      } else {
        for (suit=0; suit<4; suit++)
          if ( suits[suit][0] == (*(str+2)) )
            break;
        for (value=0; value<13; value++)
          if ( values[value][1] == (*(str+1)) )
            break;
        deck[i*14+j] = suit*13+value;
      }
    }
  }

  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# CURR", 6) != 0);
  for (i=0; i<4; i++) {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
    str = line;
    for (j=0; j<14; ++j, str+=4) {
      if ( str[1] == '*') {
        board[i*14+j] = -1;
      } else {
        for (suit=0; suit<4; suit++)
          if ( suits[suit][0] == str[2])
            break;
        for (value=0; value<13; value++)
          if ( values[value][1] == str[1])
            break;
        board[i*14+j] = suit*13+value;
      }
    }
  }

  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# STRA", 6) != 0);
  for (i=0; i<4; i++) {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
    str = line;
    for (j=0; j<14; ++j, str+=4) {
      if ( str[1] == '*') {
        strBoard[i*14+j] = -1;
      } else {
        for (suit=0; suit<4; suit++)
          if ( suits[suit][0] == str[2])
            break;
        for (value=0; value<13; value++)
          if ( values[value][1] == str[1])
            break;
        strBoard[i*14+j] = suit*13+value;
      }
    }
  }

  do {
    fscanf( fp, "%[^\n]s", line);
    fscanf( fp, "%c", &c);
  } while ( strncmp(line, "# HIST", 6) != 0);

  sscanf(line, "# HISTORY %d %d", &_count, &_flag);
  _n_moves = 0;
  while (1) {
    int _from, _to, _c;
    MOVE * mp;
    if ( fscanf( fp, "%[^\n]s", line) == EOF)
      break;
    fscanf( fp, "%c", &c);
    if ( line[0] == '#')
      break;
    _n_moves ++;
    sscanf(line, "%d%d%d", &_from, &_to, &_c);
    mp = add_top( & theHistory );
    mp->from = _from;
    mp->to   = _to;
    if (_c) 
      _cursor = mp;
  };
  theHistory.count = _count;
  theHistory.cursor = _cursor;
  theHistory.flag = _flag;
  printf("History _count %d, n of moves %d\n", _count, _n_moves);

/*
  fscanf( fp, "# HISTORY %d %d\n", moveCount(& theHistory), theHistory.flag);
  {
     MOVE * mp; 
     for (mp=theHistory.bottom; mp; mp=mp->up)
       fscanf(fp, "%d %d %d\n", mp->from, mp->to, mp==theHistory.cursor);
  }
*/

  fclose( fp );
  return _seed;
}
  


/*--------------------------------------------------------------------*/
int
saveGame(int seed, int mode, int flag)
{
  extern char* values[], * suits[];
  int i,j, index, suit, value;
  FILE * fp;
  char filename[32];
  time_t tt=time (NULL);
  MOVE * theMove;


  if (flag == 1) {
    theMove = backward(& theHistory);
    board[theMove->from] = board[theMove->to];
    board[theMove->to] = -1;
    drop_till_top(&theHistory);
  }

  sprintf(filename, "%d", seed);
  if ( (fp = fopen( filename, "w")) == NULL)
    return -1;
  fprintf( fp, "# Blue Game - %s", ctime( &tt));
  fprintf( fp, "#\n");
  fprintf( fp, "# SEED \n%d\n", seed);
  fprintf( fp, "# PLAYMODE \n%d\n", mode);
  fprintf( fp, "# INITIAL BOARD\n");
  for( i = 0; i < 4; i++) {
    for( j = 0; j < 14; j++ ) {
       index = deck[i*14+j];
       if(index == -1 ) {
         fprintf(fp, " ** ");
       } else {
         suit = index /13;
         value = index % 13;
         fprintf(fp, "%s%s ", values[value],suits[suit]);
      }
    }
    fprintf(fp,"\n");
  }
  fprintf( fp, "# CURRENT BOARD\n");
  for( i = 0; i < 4; i++) {
    for( j = 0; j < 14; j++ ) {
       index = board[i*14+j];
       if(index == -1 ) {
         fprintf(fp, " ** ");
       } else {
         suit = index /13;
         value = index % 13;
         fprintf(fp, "%s%s ", values[value],suits[suit]);
      }
    }
    fprintf(fp,"\n");
  }
  fprintf( fp, "# STRATEGY\n");
  for( i = 0; i < 4; i++) {
    for( j = 0; j < 14; j++ ) {
       index = strBoard[i*14+j];
       if(index == -1 ) {
         fprintf(fp, " ** ");
       } else {
         suit = index /13;
         value = index % 13;
         fprintf(fp, "%s%s ", values[value],suits[suit]);
      }
    }
    fprintf(fp,"\n");
  }
  fprintf( fp, "# HISTORY %d %d\n", moveCount(&theHistory), theHistory.flag);
  {
     MOVE * mp; 
     for (mp=theHistory.bottom; mp; mp=mp->up){
       fprintf(fp, "%d %d %d\n", mp->from, mp->to, mp==theHistory.cursor);
     }
  }

  fclose( fp );
  drawSave(seed, 1);
  setgui(seed);                    // put the cards on the GUI
  restoreStrategy();
  return 0;
}
  
/*--------------------------------------------------------------------*/
int lookupEvents(
int from, int to, unsigned long eventTime, unsigned long * eventTable) 
{
  if ( from != to ) {        // clear the look up table and play
     eventTable[0] = 0;
     eventTable[1] = DIM_BRD;
     return PLAYING;
  }  //from == to 
  if ( eventTime - eventTable[0] > 400 ) { //double click time over
     eventTable[0] = eventTime;
     eventTable[1] = from;
     if( play_mode == WAIT ) return PLAY_AND_WAIT;
     return WAIT;
  }

  if (from ==  eventTable[1]) {           //double clicked!
     eventTable[0] = 0;
     eventTable[1] = DIM_BRD;
     return DOUBLE_CLICK;
  }
  eventTable[0] = 0;                     //sorry not clicked at a same spot!
  eventTable[1] = DIM_BRD;               //but you were very quick clicking!
  return PLAYING;

} 

/*--------------------------------------------------------------------*/
int clearMark(int * doublePos) 
{
  drawCard( -1, doublePos[0]);
  drawCard( -1, doublePos[1]);
  doublePos[0] = -1;
  doublePos[1] = -1;
  return 0;
}


/*===============================================================
updateBoard(&from,&to,play_mode) 
if play_mode = STRATEGY: draw a line "from" to "to",
               PLAYING:  take the card at "from" and move it to "to",
     sub modes of PLAYING:
               PLAY_AND_WAIT: play then change mode to "WAIT" 
               WAIT: waiting for the next click if "from" == "to",
               BACKFOR:  moving backward or forward in theHistory,
               DOUBLE_CLICK: to move a card automatically
                             to the only one allowed place.
=================================================================*/
int updateBoard(int * ref_from, int * ref_to, int * doublePos, int mode)
{
  int fCard;
  int j1,j2;
  int from = *ref_from;
  int to = *ref_to;
  int count = 0;

  j1 = from%14;
  j2 = to%14;
  fCard = board[from];

  if( from >= DIM_BRD || to >= DIM_BRD || ( fCard == -1  && from != to) ) 
    return RING_BELL;  //can erase the strategy line on empty spot.

  if ( top(&theHistory) == NULL && j1 == 0 && j2 == 0)  {  //ACE 
    if (from == to && mode == STRATEGY) 
      return updateStrategy( from, to, fCard); //mark strategy
    board[from] = board[to];                           //ace swap 
    board[to] = fCard;
    drawCard( board[from], from );
    drawCard( board[to], to );
    return ACE_SWAP;
  }

  if ( mode == STRATEGY )  {                 //STRATEGY
    if (j2 == 0) return RING_BELL;
      return updateStrategy(from,to,fCard);
  }  

  if (j1 != 0  &&  j2 != 0 ) {                  //PLAYING, NOT ACE MOVE
    if (from == to) {
//printf("updateB: play mode: %d\n", mode);
      if (mode == PLAY_AND_WAIT) return CHANGE_TO_WAIT;
      if (mode == PLAYING) return DO_NOTHING;
      count = doubleClickMove( doublePos,fCard );
      if (count == 0) return RING_BELL;
//printf("updateB: poss doublePos %d\n", to);
    } 
    if ( count == 0 && board[to] != -1 ) return RING_BELL;    //no target space
    if ( count == 1 ) {
      to = doublePos[0];
      doublePos[0] = -1;
      j2 = to % 14;
      *ref_to = to;
    } else if ( count == 2 ) {
      drawDouble( doublePos[0]);
      drawDouble( doublePos[1]);
      return DOUBLE_MARK;
    }
      
    if (  mode == BACFOR                 ||     //back or forward in theHist. 
      ( fCard - 1 ==  board[to-1] )  ||
      ( j2 != 13  &&   fCard + 1 ==  board[to+1] ) ) {
      board[to] = fCard;
      board[from] = -1;
      drawCard( -1, from );
      drawCard( board[to], to );
      return PLAYING;
    }
  }
  return RING_BELL; 
} /*end updateBoard()*/
 

/*-----------------------------------------------------------------*/
int updateStrategy( int from, int to, int fCard ) {
  int i;
  int tVal;

  if ( from == to ) { //need to worry about empty spot only here.
    if( strBoard[from] != -1) {
       drawStrategy( from,  strBoard[from], -1 );
       strBoard[from] = -1;
    } else if( fCard == -1 ) {
       return RING_BELL;
    } else {
       drawStrategy( from, -1, fCard );
       strBoard[from] = fCard;
    }
    return STRATEGY; 
  }

  if ( from / 14  == to / 14 ) {
    tVal = fCard % 13  + to - from;
    if ( tVal >= 13  || tVal < 1 ) {
      return  RING_BELL;
    }
    if ( to > from) {
      for (i = from; i <= to; i++) {
        drawStrategy( i, strBoard[i], fCard+i-from);
        strBoard[i] = fCard+i-from;
      }
    } else {
      for (i = from; i >= to; i--) {
        drawStrategy( i, strBoard[i], fCard+i-from);
        strBoard[i] = fCard+i-from;
      }
    }
  }
  return STRATEGY;
}

/*-----------------------------------------------------------------*/
int doubleClickMove(int * pos, int fCard) {
  int i,j,k;
  int count = 0;

  for (i = 0; i < 4; i++) {
    for (j = 0; j < 14; j++) {
      k = i * 14 + j;
      if ( j != 0  &&  fCard == board[k] - 1  &&  board[k-1] == -1 ) { 
        if( count ==1 && pos[0] == k-1 ) return count;
        pos[count] = k - 1;
        count++;
      }
      if ( j != 13  &&  fCard == board[k] + 1  &&  board[k+1] == -1 ) { 
        if( count ==1 && pos[0] == k+1 ) return count;
        pos[count] = k + 1;
        count++;
      }
    }           
  }
    
  return count;
}


/*-----------------------------------------------------------------*/
int isGameOver() {
  int i, j, k;
  int suit;
  for (i=0; i<4; ++i) {
    k = i * 14;
    suit = board[k] / 13;
    for (j=1; j<13; ++j) {
      if ( (board[k+j] / 13) != suit )
        return 0;
      if ( (board[k+j] % 13) != j )
        return 0;
    }
  }
  return 1;
}


/* =================================================================
PlayGame() 
gets the positions where the mouse is clicked( from) , released(to)
, and released time(in milliseconds) from function mouseClick() 
and decide if the movement of the card is legal, move
card, and register the movement in the history list.
==================================================================== */
void PlayGame(unsigned int seed)
{
 MOVE * theMove;
 int from, to;                     //mouse click from, to  
 int quit;
 int k;
 int doublePos[2] = {-1, -1};     //2 possible places to move a card
 unsigned long eventTable[2] = { 0, DIM_BRD};
 unsigned long eventTime = 0; 
 
/*init Game*/
 quit = 0;
 
/* get the next user mouse click: from and to are converted to their 
    logical positions.*/
 while ( ! quit && (k =  mouseClick( &from, &to, &eventTime)) >= 0) {
  if (doublePos[0] != -1) clearMark( doublePos ); 

  if (k == 1) {                         // MENU
    switch (to){

     case NEW:                           // a freshly new game
        seed = time(NULL);
        resetLayout();                      // reset the Graphical Use Interface
        resetStrategy();
        setgame(seed);              // set up the game (deal the cards)
        saveBoard();

     case RESTART:                       // repeat the same game 
        play_mode = STRATEGY;
        resetHistory(& theHistory);      // reset the history to empty
        restoreBoard();
        setgui(seed);                    // put the cards on the GUI
        clearCountLabel(moveCount(&theHistory));    
        break;

     case BACKWARD:                      // moving backward in history
        theMove = backward(& theHistory);
        if (theMove != NULL) {            // if not got before the initial move
          updateBoard(&(theMove->to),&(theMove->from),doublePos,BACFOR);
        } else {                         // else (got before the start)
          ringBell();                        //   ring a warning bell
        } 
        break; 

     case FORWARD:                          // moving forward in history
        theMove = forward(& theHistory);    // get the next move in the history
        if (theMove != NULL) {           //   if did not get beyond the top
          updateBoard(&(theMove->from),&(theMove->to),doublePos,BACFOR);
        } else {                         //   else (got beyond the top)
          ringBell();                        //     ring a warning
        } 
        break; 

     case Strategy:
        play_mode = STRATEGY;
        drawPlayMode( play_mode );
        break;

     case PLAY:
        play_mode = PLAYING;
        drawPlayMode( play_mode );
        break;

     case QUIT:                    // menu QUIT item : just exit
       quit = 1;
       bailout("Bye bye!", EXIT_SUCCESS);
       break;

     case SAVE:                    // menu SAVE item : save the game
       saveGame(seed, play_mode, 0);
       break;
    
    } /*end switch*/
  } else if (k == 2) {  // repaint the window
    drawMenu();
    setgui(seed);                    // put the cards on the GUI
    restoreStrategy();

  } else {                     //not menu(gameBoard or somewhere else)          
    if ( play_mode != STRATEGY )
      play_mode = lookupEvents( from, to, eventTime, eventTable );
    if ( play_mode != WAIT ) {
    switch (updateBoard(&from, &to, doublePos, play_mode) ) {    
       case RING_BELL:
          if (play_mode == DOUBLE_CLICK) play_mode = PLAYING; 
          ringBell();
          break;
       case DOUBLE_MARK:
          play_mode = PLAYING;
          break;
       case CHANGE_TO_WAIT:
          play_mode = WAIT;         //fall through
       case DO_NOTHING:
       case STRATEGY:
       case ACE_SWAP:
          break;
       case PLAYING: 
          play_mode = PLAYING; 
          if (theHistory.cursor!=top(&theHistory))   //update theHistory
             drop_till_top(& theHistory);
          if ((theMove=add_top(& theHistory)) == NULL)
             bailout("Memory failure!", EXIT_FAILURE);
          theMove->from = from;
          theMove->to   = to;   
          break;
       default:
          break;
    }//end switch
    }
  }//end if
  drawCountLabel(moveCount(&theHistory));          // update the count label
  if ( isGameOver() ) {
     drawWinFlag( moveCount(&theHistory) );
     sleep(1);
     setgui(seed);
     drawSave(seed, 0);
     setgui(seed);
     sleep(1);
     if( finalMouseClick() == SAVE ) saveGame(seed, play_mode, 1); 
     quit = 1;
  }

 }//end while
}  /*end PlayGame()*/

