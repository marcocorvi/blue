/* history.c

   Author Kyungmi Lim
   Date   Apr 2001
   
   defines the functions to record the history of the game: 
   a doubly-linked list of the game moves
   The list has three pointers:
      bottom : points to the first entered item
      top    : points to the last entered item
      cursor : points to the "current" item (which corresponds to the
               status of the game shown in the graphical interface)
               Can become NULL if the user outside of the history of  moves
               going either backward or forward 

   Bugs:
   You can never tell ...

*/

/* ============================================================= */

#include <stdio.h>                 // NULL
#include <stdlib.h>                // malloc
#include "history.h"

MOVE * forward(HISTORY * h)        // move cursor forward
{
   // fprintf(stderr, "history forward: count %d\n", h->count);

   if (h->cursor != NULL) {
     if (h->cursor->up == NULL) return NULL;
     h->cursor = (h->cursor)->up;
     h->count++;
   } else if (h->flag== -1) {
     h->cursor = h->bottom;
     h->flag=0;
     h->count = 1;
   }  
   return h->cursor;
}

MOVE * backward(HISTORY * h)      // move the cursor backward
{
  MOVE * pt = h->cursor;
  // fprintf(stderr, "history backward: count %d\n", h->count);

  if (h->cursor != NULL) {
    h->cursor = (h->cursor)->down;
    if (h->cursor == NULL) h->flag = -1;
    h->count--;
  }
  return pt ;
}

void initHistory(HISTORY * h)  //initialize the history ; set everything NULL 
{
  h->top    = NULL;
  h->bottom = NULL;
  h->cursor = NULL;
  h->flag   = 0;
  h->count  = 0;
}

void resetHistory(HISTORY * h) 
{
  h->cursor = NULL;
  drop_till_top(h);
  h->flag   = 0;
  h->count  = 0;
}

MOVE *  add_top(HISTORY * h)
{
  MOVE * pt;
  // fprintf(stderr, "history add_top: count %d\n", h->count);

  pt  = (MOVE *)malloc(sizeof(MOVE));
  if (pt == NULL)  return NULL;   // Exception: unable to allocate memory 

  if (h->top == NULL) {            // if the history is empty
    h->flag   = 0;                 //   history flag is 0 (inside)
    h->bottom = pt;                //   set "buttom" equal to the new item 
  } else                           // else
    (h->top)->up = pt;             //   set up pointer of "top" to the new item
 
  pt->down = h->top;               // set down pointer of new item to "top"
  pt->up   = NULL;                 // set up pointer of new item to NULL
  h->top    = pt;                  // set "top" and "cursor" to point the
  h->cursor = pt;                  //   new item
  h->count++;
  return pt;                       // return the new item
}

void delete_top(HISTORY * h)
{
  MOVE * pt = h->top;              // store a pointer to the top item
  // fprintf(stderr, "history del_top: count %d\n", h->count);

  if (h->top == NULL) return;      // if the "top" is NULL : nothing to do
     
  h->top = (h->top)->down;         // move down the "top"
  if (h->top != NULL)              // if the "top" item is not NULL
    (h->top)->up = NULL;           //   set its up pointer to NULL
  else                             // else (the whole history becomes empty)
    h->bottom = NULL;              //   set the "buttom" also to NULL

  if (h->cursor == pt)             // if the "cursor" points to the top item
    h->cursor = h->top;            //   set the "cursor" to the new "top" 
    
  free(pt);                        // delete the top item
}

void drop_till_top(HISTORY * h)
{                                        // delete the item from the "top"
  while (h->top!=h->cursor) delete_top(h); // way down to the "cursor" (excluded)
}

int moveCount(HISTORY *h) 
{
  return h->count;
}



MOVE * top(HISTORY * h)       
{
  return h->top;
}


MOVE * cursor(HISTORY * h)     
{
  return h->cursor;
}
