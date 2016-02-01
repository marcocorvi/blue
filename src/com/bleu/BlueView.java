/* @file BlueView.java
 *
 * This class is adapted from the "snake" sample (with very few changes)
 *
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marcocorvi.blue;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Random;

/**
 */
public class BlueView extends SurfaceView
                      implements SurfaceHolder.Callback
{

  static int mLevel = 5; // game level

  byte[] getState()
  {
    int i;
    int size = mHistory.size();
    int tot = 2 + 1 + 2 + 2 + 8 + 3*56 + 4*size;
    byte[] ret = new byte[ tot ];
    int k=0;
    ret[k++] = (byte)( tot & 0xff );
    ret[k++] = (byte)( (tot >> 8) & 0xff );
    ret[k++] = (byte)( mMode & 0xff );
    ret[k++] = (byte)( mHistoryPos & 0xff );
    ret[k++] = (byte)( (mHistoryPos >> 8) & 0xff );
    ret[k++] = (byte)( size & 0xff );
    ret[k++] = (byte)( (size >> 8) & 0xff );
    ret[k++] = (byte)( mSeed & 0xff );
    ret[k++] = (byte)( (mSeed >>  8) & 0xff );
    ret[k++] = (byte)( (mSeed >> 16) & 0xff );
    ret[k++] = (byte)( (mSeed >> 24) & 0xff );
    for ( i=0; i<56; ++i ) ret[k++] = (byte)( mStartBoard[i] & 0xff );
    for ( i=0; i<56; ++i ) ret[k++] = (byte)( mBoard[i] & 0xff );
    for ( i=0; i<56; ++i ) ret[k++] = (byte)( mStrategy[i] & 0xff );
    for ( i=0; i<size; ++i ) {
      BlueMove m = mHistory.get(i);
      ret[k++] = (byte)( m.col1 & 0xff );
      ret[k++] = (byte)( m.row1 & 0xff );
      ret[k++] = (byte)( m.col2 & 0xff );
      ret[k++] = (byte)( m.row2 & 0xff );
    }
    return ret;
  }

  static final byte M1 = (byte)(-1 & 0xff);

  void restore( byte[] ret, boolean complete, String filename )
  {
    int i;
    mHistory.clear();
    int tot = (int)(ret[0]) | ((int)(ret[1]) << 8);
    mMode = (int)(ret[2]);
    mHistoryPos = (int)(ret[3]) | ((int)(ret[4]) << 8);
    int size = (int)(ret[5]) | ((int)(ret[6]) << 8);
    try {
      mSeed = Long.parseLong( filename );
    } catch ( NumberFormatException e ) {
      mSeed = (int)(ret[7]) | ((int)(ret[8]) << 8) | ((int)(ret[9]) << 16) | ((int)(ret[10]) << 24);
    }
    int k=11;
    for ( i=0; i<56; ++i ) {
      byte b = ret[k++];
      mStartBoard[i] = ( b == M1 )? -1 : (int)b;
    }
    if ( complete ) {
      for ( i=0; i<56; ++i ) {
        byte b = ret[k++];
        mBoard[i] = ( b == M1 )? -1 : (int)b;
      }
      for ( i=0; i<56; ++i ) {
        byte b = ret[k++];
        mStrategy[i] = ( b == M1 )? -1 : (int)b;
      }
      for ( i=0; i<size; ++i ) {
        int c1 = (int)(ret[k++]);
        int r1 = (int)(ret[k++]);
        int c2 = (int)(ret[k++]);
        int r2 = (int)(ret[k++]);
        mHistory.add( new BlueMove( c1, r1, c2, r2 ) );
      }
    } else {
      for ( i=0; i<56; ++i ) {
        mBoard[i] = mStartBoard[i];
        mStrategy[i] = -1;
      }
      mHistoryPos = 0;
      mMode = MODE_DEFAULT;
    }
  }


  // ----------------------------------------------------------------
  final static int MODE_STRATEGY = 1;
  final static int MODE_PLAY = 2;
  final static int MODE_OVER = 3;
  final static int MODE_DEFAULT = 1;

  int mMode;

  String getModeString()
  {
    return ( mMode == MODE_PLAY )? "PLAY - " + mHistoryPos + " / " + mHistory.size() 
         : ( mMode == MODE_STRATEGY )? "STRATEGY" 
         : "GAME OVER - " + mHistoryPos;
  }

  void toggleMode()
  {
    if ( mMode == MODE_OVER ) return;
    mMode = ( mMode == MODE_PLAY )?  MODE_STRATEGY : MODE_PLAY;
  }

  // ----------------------------------------------------------------
  // DIMENSIONS

  private static int mXOffset = 0;
  private static int mYOffset = 0;
  protected static int mXCard = 21;
  protected static int mYCard = 50;
  protected static int mXGap  =  2;
  protected static int mYGap  = 10;
  protected static int mYGap2  = 2;
  protected static int mYGap8  = 8;
  static int mCanvasWidth  = 320;
  static int mCanvasHeight = 240;

  // ----------------------------------------------------------------
  // CARDS AND ICONS

  private static int colors[] = { 0xffff0000, 0xff999999, 0xffff00cc, 0xff333333 };
  private static int colorsText[] = { 0xff00ffff, 0xff000000, 0xff00ff33, 0xffffffff };
  private static int icons[] = {
                        R.drawable.c01,  // hearts
                        R.drawable.c02,
                        R.drawable.c03,
                        R.drawable.c04,
                        R.drawable.c05,
                        R.drawable.c06,
                        R.drawable.c07,
                        R.drawable.c08,
                        R.drawable.c09,
                        R.drawable.c10,
                        R.drawable.c11,
                        R.drawable.c12,
                        R.drawable.c13,
                        R.drawable.c14,  // diamonds
                        R.drawable.c15,
                        R.drawable.c16,
                        R.drawable.c17,
                        R.drawable.c18,
                        R.drawable.c19,
                        R.drawable.c20,
                        R.drawable.c21,
                        R.drawable.c22,
                        R.drawable.c23,
                        R.drawable.c24,
                        R.drawable.c25,
                        R.drawable.c26,
                        R.drawable.c27,  // clubs
                        R.drawable.c28,
                        R.drawable.c29,
                        R.drawable.c30,
                        R.drawable.c31,
                        R.drawable.c32,
                        R.drawable.c33,
                        R.drawable.c34,
                        R.drawable.c35,
                        R.drawable.c36,
                        R.drawable.c37,
                        R.drawable.c38,
                        R.drawable.c39,
                        R.drawable.c40, // spades
                        R.drawable.c41,
                        R.drawable.c42,
                        R.drawable.c43,
                        R.drawable.c44,
                        R.drawable.c45,
                        R.drawable.c46,
                        R.drawable.c47,
                        R.drawable.c48,
                        R.drawable.c49,
                        R.drawable.c50,
                        R.drawable.c51,
                        R.drawable.c52
  };

  Bitmap[] mBitmap;
  Card[] mCards;

  private Bitmap loadBitmap( int id, int w, int h )
  {
    Bitmap bm1 = BitmapFactory.decodeResource( mContext.getResources(), icons[id] );
    return Bitmap.createScaledBitmap( bm1, w, h, false );
  }

  private void makeCards()
  { 
    mBitmap = new Bitmap[52];
    mCards = new Card[52];
    for ( int k=0; k<4; ++k ) {
      for ( int i=0; i<13; ++i ) {
        mCards[13*k+i] = new Card( k, i, null, mPaint[k] );
      }
    }
  }

  private void makeBitmaps()
  {
    for ( int i=0; i<52; ++i ) {
      mBitmap[i] = loadBitmap( i, mXCard, mYCard );
    }
    for ( int i=0; i<52; ++i ) {
      mCards[i].bitmap = mBitmap[i];
    }
  }

  // ---------------------------------------------------------------
  // PAINTS

  Paint mPaintBlue;
  Paint mPaintGrey;
  Paint mPaint[];
  Paint mPaintText[];

  private void makePaints()
  {
    mPaintBlue = new Paint();
    mPaintBlue.setDither(true);
    mPaintBlue.setColor( 0xff0000ff );
    mPaintBlue.setStyle(Paint.Style.FILL);
    mPaintBlue.setStrokeJoin(Paint.Join.ROUND);
    mPaintBlue.setStrokeCap(Paint.Cap.ROUND);
    mPaintBlue.setStrokeWidth( 1 );

    mPaintGrey = new Paint();
    mPaintGrey.setDither(true);
    mPaintGrey.setColor( 0xffcccccc );
    mPaintGrey.setStyle(Paint.Style.FILL);
    mPaintGrey.setStrokeJoin(Paint.Join.ROUND);
    mPaintGrey.setStrokeCap(Paint.Cap.ROUND);
    mPaintGrey.setStrokeWidth( 1 );

    mPaint = new Paint[4];
    mPaintText = new Paint[4];
    for ( int k=0; k<4; ++k ) {
      mPaint[k] = new Paint();
      mPaint[k].setDither(true);
      mPaint[k].setColor( colors[k] );
      mPaint[k].setStyle(Paint.Style.FILL);
      mPaint[k].setStrokeJoin(Paint.Join.ROUND);
      mPaint[k].setStrokeCap(Paint.Cap.ROUND);
      mPaint[k].setStrokeWidth( 1 );
      mPaintText[k] = new Paint();
      mPaintText[k].setDither(true);
      mPaintText[k].setColor( colorsText[k] );
      mPaintText[k].setStyle(Paint.Style.STROKE);
      mPaintText[k].setStrokeJoin(Paint.Join.ROUND);
      mPaintText[k].setStrokeCap(Paint.Cap.ROUND);
      mPaintText[k].setStrokeWidth( 1 );
      mPaintText[k].setTextSize( mYGap8 );
    }
  }

  // ---------------------------------------------------------------

  private Context mContext;

  int mBoard[]      = new int[56]; // 4 * 14
  int mStartBoard[] = new int[56]; // 4 * 14
  int mStrategy[]   = new int[56]; // 4 * 14
  ArrayList< BlueMove > mHistory;
  int mHistoryPos;
  long mSeed;

  SurfaceHolder mHolder;
  MyThread mThread;
  boolean  mHasSurface;

  boolean isGameOver()
  {
    for ( int k=0; k<4; ++k ) {
      int suit = mCards[ mBoard[k*14] ].suit;
      for ( int i=1; i<13; ++i ) {
        int b = mBoard[ k*14 + i ];
        if ( b < 0 ) return false;
        Card c = mCards[b];
        if ( c.suit != suit ) return false; 
        if ( c.value != i ) return false;
      }
    }
    mMode = MODE_OVER;
    return true;
  }

  void init()
  {
    makePaints();  // must come before makeCards()
    makeCards();
    makeBitmaps();
    // shuffleCards( mLevel );
    clearStrategy();
    mHolder = getHolder();
    mHolder.addCallback( this );
    mHasSurface = false;
    mThread = null;
    mMode = MODE_DEFAULT;
    mHistory = new ArrayList<BlueMove>();
    mHistoryPos = 0;
  }      

  public BlueView(Context context, AttributeSet attrs, int defStyle )
  {
    super(context, attrs, defStyle);
    mContext = context;
    init();
  }

  public BlueView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mContext = context;
    init();
  }

  public BlueView(Context context )
  {
    super(context);
    mContext = context;
    init();
  }

  synchronized void reset()
  {
    clearStrategy();
    shuffleCards( mLevel );
    mMode = MODE_DEFAULT;
    mHistory.clear();
    mHistoryPos = 0;
  }

  synchronized void restart()
  {
    if ( mMode == MODE_PLAY ) {
      for (int k=0; k<56; ++k ) {
        if ( ( k % 14 ) == 0 ) continue;
        mBoard[k] = mStartBoard[k];
      }
      mHistory.clear();
      mHistoryPos = 0;
    } else if ( mMode == MODE_STRATEGY ) {
      for (int k=0; k<56; ++k ) mStrategy[k] = -1;
    }
    // mMode = MODE_DEFAULT;
  }

  private void clearStrategy()
  {
    for (int k=0; k<56; ++k ) mStrategy[k] = -1;
  }

  static final String valueLabel[] = new String[] {
    "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" 
  };

  private void drawStrategy( Canvas canvas, int i, int j, int col )
  {
    if ( col < 0 ) return;
    int off = col / 52;
    int suit = (col%52) / 13;
    int val  = (col%52) % 13;
    String sym = "-";
    switch ( col/52 ) {
      case 1: sym = "- " + valueLabel[ val ] + " >"; break;
      case 2: sym = "< " + valueLabel[ val ] + " -"; break;
      default: sym = valueLabel[ val ]; break;
    }  
    int i0 = mXOffset + i * ( mXCard + mXGap );
    int j0 = mYOffset + j * ( mYCard + mYGap ) + mYGap2;
    int i1 = i0 + mXCard;
    int j1 = j0 + mYGap8;
    canvas.drawRect( i0,j0, i1,j1, mPaint[suit] ); // left top right bottom paint
    int ii = i0 + mXCard/4;
    int jj = j1 - mYGap2/2;
    canvas.drawText( sym, ii, jj, mPaintText[suit] );
  }

  private void drawCard( Canvas canvas, int i, int j, int b )
  {
    int i0 = mXOffset + i * ( mXCard + mXGap );
    int j0 = mYOffset + j * ( mYCard + mYGap ) + mYGap;
    int i1 = i0 + mXCard;
    int j1 = j0 + mYCard;
    if ( b >= 0 ) {
      // canvas.drawRect( i0,j0, i1,j1, mPaint[ mCards[b].suit ] );
      canvas.drawBitmap( mCards[b].bitmap, i0, j0, mCards[b].paint );
    } else {
      canvas.drawRect( i0,j0, i1,j1, mPaintGrey );
    }
  }

  int getIndex( int x, int y )
  {
    int j = ( y - mYOffset - mYGap) / ( mYCard + mYGap);
    if ( j < 0 || j >= 4 ) return -1;
    int i = ( x - mXOffset ) / ( mXCard + mXGap );
    if ( i < 0 || i >= 14 ) return -1;
    return j*14 + i;
  }

  int getRow( int x, int y )
  {
    int j = ( y - mYOffset - mYGap) / ( mYCard + mYGap);
    return ( j < 0 || j >= 4 )? -1 : j;
  }

  int getColumn( int x, int y )
  {
    int i = ( x - mXOffset ) / ( mXCard + mXGap );
    return ( i < 0 || i >= 14 )? -1 : i;
  }


  synchronized public void drawOn(Canvas canvas) 
  {
    // if ( canvas == null ) return;
    canvas.drawRect( mXOffset, mYOffset, mXOffset + mCanvasWidth, mYOffset + mCanvasHeight, mPaintBlue );

    for (int j=0; j<4; ++j ) {
      for ( int i=1; i<14; ++i ) {
        if ( mStrategy[j*14+i] >= 0 ) {
          drawStrategy( canvas, i, j, mStrategy[j*14+i] );
        }
      }
      for ( int i=0; i<14; ++i ) {
        drawCard( canvas, i, j, mBoard[j*14+i] );
      }
    }
  }

  void dumpBoard( int[] board )
  {
    for (int j=0; j<4; ++j ) {
      Log.v("Blue",
        board[j+14+0] + " " +
        board[j+14+1] + " " +
        board[j+14+2] + " " +
        board[j+14+3] + " " +
        board[j+14+4] + " " +
        board[j+14+5] + " " +
        board[j+14+6] + " " +
        board[j+14+7] + " " +
        board[j+14+8] + " " +
        board[j+14+9] + " " +
        board[j+14+10] + " " +
        board[j+14+11] + " " +
        board[j+14+12] + " " +
        board[j+14+13] );
    }
  } 

  synchronized void shuffleCards( int n ) 
  {
    mSeed = System.nanoTime();
    Random rand = new Random( mSeed );

    int cards[] = new int[52];
    for ( int i=0; i<52; ++i ) cards[i] = i;
    while ( n > 0 ) {
      for ( int i=0; i<52; ++i ) {
        int j1 = rand.nextInt(52);
        int j2 = rand.nextInt(52);
        if ( j1 != j2 ) {
          int tmp = cards[j1];
          cards[j1] = cards[j2];
          cards[j2] = tmp;
        }
      }
      -- n;
    }
    int nc = 0;
    for ( int k=0; k<4; ++k ) {
      for ( int i=0; i<13; ++i ) {
        if ( ( cards[ k*13 + i ] % 13 ) == 0 ) {
          mBoard[ nc*14 ] = cards[ k*13 + i ];
          mBoard[ k*14 + 1 + i ] = -1;
          ++ nc;
        } else {
          mBoard[ k*14 + 1 + i ] = cards[ k*13 + i ];
        }
      }
    }
    for ( n=0; n<56; ++n ) mStartBoard[n] = mBoard[n];
    // dumpBoard( mBoard );
  }
  
  synchronized void swapAces( int row1, int row2 )
  {
    if ( mMode != MODE_STRATEGY ) return;
    if ( mHistoryPos > 0 ) return;
    int ace = mBoard[row1*14];
    mBoard[row1*14] = mBoard[row2*14];
    mBoard[row2*14] = ace;
    mHistory.clear();
    mHistoryPos = 0;
  }

  synchronized boolean markStrategy( int col1, int row1, int col2, int row2 )
  {
    if ( mMode != MODE_STRATEGY ) return false;
    int suit = -1;
    int val  = -1;
    if ( mBoard[ row1*14 + col1 ] >= 0 ) {
      suit = mCards[ mBoard[ row1*14+col1 ] ].suit;
      val  = mCards[ mBoard[ row1*14+col1 ] ].value;
    }

    int strategy_offset = (col1 == col2)? 0 : (col1 < col2)? 52 : 104;


    if ( col1 == col2 && row1 == row2 ) {
      if ( mStrategy[ row1*14+col1 ] >= 0 ) {
        mStrategy[ row1*14+col1 ] = -1;
      } else {
        mStrategy[ row1*14+col1 ] = strategy_offset + suit*13 + val;
      }
      return true;
    }

    if ( mStrategy[ row1*14 + col1 ] >= 0 && (mStrategy[ row1*14 + col1 ]%52) / 13 == suit ) { // CLEAR STRATEGY
      if ( col1 < col2 ) {
        for ( int c = col1; c < 14; ++c ) {
          if ( mStrategy[ row1*14 + c ] < 0 ) break;
          int s1 = (mStrategy[ row1*14 + c ]%52) / 13;
          int v1 = (mStrategy[ row1*14 + c ]%52) % 13;
          if ( s1 != suit || v1 != val ) break;
          mStrategy[ row1*14 + c ] = -1;
          ++val;
          if ( val >= 13 ) break;
        }
      } else if ( col1 > col2 ) {
        for ( int c = col1; c >= 1; --c ) {
          if ( mStrategy[ row1*14 + c ] < 0 ) break;
          int s1 = (mStrategy[ row1*14 + c ]%52) / 13;
          int v1 = (mStrategy[ row1*14 + c ]%52) % 13;
          if ( s1 != suit || v1 != val ) break;
          mStrategy[ row1*14 + c ] = -1;
          --val;
          if ( val < 1 ) break;
        }
      }
    } else {
      // int val = mCards[ mBoard[ row1*14+col1 ] ].value;
      if ( col1 < col2 ) {
        for ( int c = col1; c <= col2; ++c ) {
          mStrategy[ row1*14 + c ] = strategy_offset + suit*13 + val;
          ++ val;
          if ( val >= 13 ) break;
        }
      } else {
        for ( int c = col1; c >= col2; --c ) {
          mStrategy[ row1*14 + c ] = strategy_offset + suit*13 + val;
          -- val;
          if ( val < 1 ) break;
        }
      }
    }
    return true;
  }
  
  synchronized boolean moveCard( int col1, int row1, int col2, int row2 )
  {
    if ( mMode != MODE_PLAY ) return false;
    if ( col1 <= 0 || col2 <= 0 ) return false;
    int b1 = mBoard[ row1*14 + col1 ];
    if ( b1 < 0 ) return false;
    int b2 = mBoard[ row2*14 + col2 ];
    if ( b2 >= 0 ) return false;

    Card c1 = mCards[b1];
    int b21 = mBoard[ row2*14 + col2 - 1 ];
    boolean can_move1 = ( b21 >= 0 ) && ( mCards[b21].suit == c1.suit ) && ( mCards[b21].value+1 == c1.value );
    boolean can_move2 = false;
    if ( col2 < 13 ) {
      int b22 = mBoard[ row2*14 + col2 + 1 ];
      can_move2 = ( b22 >= 0 ) && ( mCards[b22].suit == c1.suit ) && ( mCards[b22].value-1 == c1.value );
    }
    if ( can_move1 || can_move2 ) {
      mBoard[ row2*14 + col2 ] = b1;
      mBoard[ row1*14 + col1 ] = -1;
      pushMove( new BlueMove( col1, row1, col2, row2 ) );
      return true;
    }
    return false;
  }

  synchronized void goBackward( )
  {
    if ( mMode != MODE_PLAY ) return;
    // Log.v("Blue", "go backward " + mHistoryPos + " " + mHistory.size() ); 
    if ( mHistoryPos > 0 ) {
      mHistoryPos --;
      BlueMove move = mHistory.get( mHistoryPos );
      mBoard[ move.row1 * 14 + move.col1 ] = mBoard[ move.row2 * 14 + move.col2 ];
      mBoard[ move.row2 * 14 + move.col2 ] = -1;
    }
  }

  synchronized void goForward( )
  {
    if ( mMode != MODE_PLAY ) return;
    // Log.v("Blue", "go forward " + mHistoryPos + " " + mHistory.size() ); 
    if ( mHistoryPos < mHistory.size() ) {
      BlueMove move = mHistory.get( mHistoryPos );
      mBoard[ move.row2 * 14 + move.col2 ] = mBoard[ move.row1 * 14 + move.col1 ];
      mBoard[ move.row1 * 14 + move.col1 ] = -1;
      mHistoryPos ++;
    }
  }

  synchronized void pushMove( BlueMove move )
  {
    // empty history from pos onward
    int size = mHistory.size();
    if ( mHistoryPos < size ) {
      // mHistory.removeRange( mHistoryPos, size );
      while ( size > mHistoryPos ) {
        -- size;
        mHistory.remove( size );
      }
    }
    mHistory.add( move );
    mHistoryPos = mHistory.size();
  }

  public void resume()
  {
    // TODO reload board;
    if ( mThread == null ) {
      mThread = new MyThread( mHolder );
    }
    if ( mHasSurface && ! mThread.isAlive() ) {
      mThread.start();
    }
  }
 
  public void pause()
  {
    // TODO save board;
    if ( mThread != null ) {
      mThread.doExit();
      mThread = null;
    }
  }

  public void surfaceCreated( SurfaceHolder holder )
  {
    mHasSurface = true;
    resume();
  }

  public void surfaceDestroyed( SurfaceHolder holder )
  {
    pause();
  }

  public void surfaceChanged( SurfaceHolder holder, int format, int w, int h )
  { 
    onWindowResize( w, h );
    if ( mThread != null ) {
      // TODO ???
    }
  }

  class MyThread extends Thread
  {
    private boolean done;
    SurfaceHolder holder;

    MyThread( SurfaceHolder hld ) 
    {
      super();
      holder = hld;
      done = false;
      // Log.v("Blue", "Blue thread created");
    }

    @Override
    public void run()
    {
      // Log.v("Blue", "Blue thread run");
      while ( ! done ) {
        Canvas canvas = holder.lockCanvas();
        if ( canvas == null ) {
          try {
            sleep(200);
          } catch (InterruptedException e ) { }
        } else {
          drawOn( canvas );
          holder.unlockCanvasAndPost( canvas );
        }
      }
      // Log.v("Blue", "Blue thread done");
    }

    public void doExit()
    {
      done = true;
      try {
        join();
      } catch ( InterruptedException e ) {
        // Log.v("Blue", "thread doExit interrupted join");
      }
    }
  }

  // -----------------------------------------------------------------

  // 768 1184 320 
  // 480  800 240 100 42
  // 240  320 120  50 21  (50+10)*4  21*14+2*13 = 294+26
  void setSizes( int ww, int hh, int dpi )
  {
    // Log.v("Blue", "Set size " + ww + " " + hh );
    onWindowResize( ww, hh );
  }

  void onWindowResize( int ww, int hh )
  {
    mCanvasWidth  = ww;
    mCanvasHeight = hh;
    mXCard = (21 * ww)/320;
    mYCard = (50 * hh)/240;
    mXGap = (2 * ww)/320;
    mYGap = (10 * hh)/240;
    mYGap2 = (2 * hh)/240;
    mYGap8 = (8 * hh)/240;
    // Log.v("Blue", "Window resize Card " + mXCard + " " + mYCard + " Gap " + mXGap + " " + mYGap );
    makePaints();
    makeBitmaps();
  }

}

