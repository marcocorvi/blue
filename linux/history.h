/* history.h

   Author Kyungmi Lim
   Date   Apr 2001
   
   History is a doubly-linked list of the game moves.
   The list has three pointers:
      bottom : points to the first entered item
      top    : points to the last entered item
      cursor : points to the "current" item (which corresponds to the
               status of the game shown in the graphical interface)
               Can become NULL if the user outside of the history of  moves
               going either backward or forward
   Contains also the function prototypes to record the History. 

   notes: this history class can be used in most of games with 
          a few modifications, if necessary. 

   Bugs:
   You can never tell ...

*/
#ifndef HISTORY_H
#define HISTORY_H
/* the logical position indices of the cards are from 0 to 51.
   Since the logic applies only to the game board and the history is updated
   step by step, the card to move can be taken directly from the board:
   no need to store it in the move. */
typedef struct move {
  int  from;             //past logical position of the card taken
  int  to;               //new logical position of the card
  struct move * down;    //pointer to the backward link
  struct move * up;      //pointer to the forward link
} MOVE;                  /*end struct move*/

typedef struct history {
  MOVE * top;             // top link of the history
  MOVE * bottom;          // bottom lik of the history  
  MOVE * cursor;          // current link in the history
  int  flag;              // flags when/where the cursor exits:
                          //   0:inside the history,
                          //  -1:went out to the past of the beginning,

  int count;             // mouse count
} HISTORY;                /* end struct history */

MOVE * forward(HISTORY * h);      // move cursor forward in history
MOVE * backward(HISTORY * h);     // move the cursor backward in history
void initHistory(HISTORY * h);    // initialize the history: set to NULL 
void resetHistory(HISTORY * h);    
MOVE * add_top(HISTORY * h);     //add a new entry in the history
void delete_top(HISTORY * h);    
void drop_till_top(HISTORY * h);
int  moveCount(HISTORY * h);     //returns move count
MOVE * top(HISTORY * h);         //retruns top MOVE *
MOVE * cursor(HISTORY * h);      //returns the cursor MOVE *

#endif
