/** @file BlueActivity.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief Blue main activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.marcocorvi.blue;

import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Window;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.MotionEvent;

import android.widget.Toast;

import android.view.Display;
import android.util.DisplayMetrics;
import android.media.AudioManager;
import android.media.ToneGenerator;

import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.util.DisplayMetrics;

public class BlueActivity extends Activity
                          implements View.OnTouchListener
{
  private BlueView mView = null;
  private final ToneGenerator mToneGenerator = new ToneGenerator( AudioManager.STREAM_ALARM, 50 ); // 50 = volume

  private BlueApp mApp;

  private void beep()
  {
    mToneGenerator.startTone( ToneGenerator.TONE_PROP_BEEP, 100 ); // 100 ms
  }


  int mXOff1, mXOff2, mYOff1, mYOff2;

  @Override
  public void onCreate(Bundle state )
  {
    super.onCreate(state );
    // Log.v("Blue", "on create");

    mApp = (BlueApp) getApplication();
    mApp.mActivity = this;

    setContentView(R.layout.main);

    mView = (BlueView) findViewById( R.id.blue_view );

    int width  = getResources().getDisplayMetrics().widthPixels;
    int height = getResources().getDisplayMetrics().heightPixels;

    DisplayMetrics dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics( dm );
    int hh = dm.heightPixels;
    int ww = dm.widthPixels;
    int dpi = dm.densityDpi;
    // Log.v("Blue", "metrics " + ww + " " + hh + " " + dpi );

    mView.setSizes( ww, hh, dpi );
    // mView.init( );
    mView.setOnTouchListener( this );

    // onRestoreInstanceState( state );
    if ( state != null ) {
      restoreState( state );
    } else {
      mView.reset();
      // setCurrentGame();
    }
    setTheTitle();
  }

  void setCurrentGame()
  {
    String filename = mApp.getCurrentGame();
    FileInputStream fis = null;
    try { 
      fis = openFileInput( filename );
      BlueStore.loadGame( fis, mView, filename );
    } catch ( FileNotFoundException e ) {
      // todo
    } finally {
      if ( fis != null ) {
        try { fis.close(); } catch ( IOException e ) { }
      }
    }
  }

  @Override
  protected void onSaveInstanceState( Bundle state )
  {
    saveState( state );
    super.onSaveInstanceState(state);
  }

  private void saveState( Bundle state )
  {
    byte[] ret = mView.getState();
    // Log.v("Blue", "save state " + ret.length );
    if ( state == null ) return;
    state.putString( "BLUE_SEED", Long.toString( mView.mSeed ) );
    state.putByteArray( "BLUE_STATE", mView.getState() );
  }

  @Override
  protected void onRestoreInstanceState( Bundle state )
  {
    super.onRestoreInstanceState(state);
    restoreState( state );
    setTheTitle();
  }

  private void restoreState( Bundle state )
  {
    // Log.v("Blue", "restore state");
    if ( state == null ) return;
    if ( mView == null ) return;
    String seed = state.getString( "BLUE_SEED" );
    byte[] ret = state.getByteArray( "BLUE_STATE" );
    if ( ret == null ) return;
    // Log.v("Blue", "restore state " + ret.length );
    mView.restore( ret, true, seed );
  }

  private MenuItem mMIhelp;
  private MenuItem mMIexit;
  private MenuItem mMIplay = null;
  private MenuItem mMIreset = null;
  private MenuItem mMIrestart  = null;
  private MenuItem mMIfile = null;
  // private MenuItem mMInext = null;

  void setTheTitle()
  {
    // setTitle( "BLUE - " + mApp.mAvailableGames + "/" + mApp.mGamesNumber + ": " + mView.getModeString() );
    setTitle( "BLUE - " + mView.mSeed + ": " + mView.getModeString() );
    if ( mMIplay != null ) {
      mMIplay.setTitle( mView.mMode == mView.MODE_PLAY ? R.string.btn_strategy : R.string.btn_play );
    }
    if ( mMIrestart != null ) {
      mMIrestart.setTitle( mView.mMode == mView.MODE_PLAY ? R.string.btn_restart : R.string.btn_clear );
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIplay  = menu.add( R.string.btn_play );
    mMIrestart = menu.add( R.string.btn_restart );
    mMIreset = menu.add( R.string.btn_reset );
    mMIfile  = menu.add( R.string.btn_file );
    mMIhelp  = menu.add( R.string.btn_help );
    mMIexit  = menu.add( R.string.btn_exit );
    setTheTitle();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    if ( item == mMIhelp ) {
      (new BlueHelp( this ) ).show();
    } else if ( item == mMIplay && mView.mMode != mView.MODE_OVER ) {
      mView.toggleMode();
      setTheTitle();
    } else if ( mMIrestart != null && item == mMIrestart ) {
      askRestart();
    // } else if ( mMInext != null && item == mMInext ) {
    //   askNextGame();
    } else if ( mMIreset != null && item == mMIreset ) {
      askReset();
    } else if ( item == mMIexit ) {
      finish();
    } else if ( item == mMIfile ) {
      (new BlueFileDialog( this, this, mView )).show();
    }
    return true;
  }

  private boolean isComplete( int[] b )
  {
    return false;
  }

  void warning( String warn )
  {
    Toast.makeText( this, warn, Toast.LENGTH_SHORT ).show();
  }

  private void askReset()
  {
    new BlueAlertDialog( this, getResources(), getResources().getString( R.string.ask_reset ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          mView.reset();
          setTheTitle();
        }
    } );
  }

  // private void askNextGame()
  // {
  //   new BlueAlertDialog( this, getResources(), getResources().getString( R.string.ask_next_game ),
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mApp.getNextGame();
  //         setCurrentGame();
  //         setTheTitle();
  //       }
  //   } );
  // }

  private void askRestart()
  {
    String question = ( mView.mMode == mView.MODE_PLAY )? getResources().getString( R.string.ask_restart )
                                                        : getResources().getString( R.string.ask_clear );
    new BlueAlertDialog( this, getResources(), question,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          mView.restart();
          setTheTitle();
        }
    } );
  }

  int row1 = -1;
  int col1 = -1;
  int row2 = -1;
  int col2 = -1;

  private void goBackForthInHistory( int row1, int row2 )
  {
    if ( row1 > row2 ) {
      mView.goBackward();
      setTheTitle();
    } else if ( row1 < row2 ) {
      mView.goForward();
      setTheTitle();
    } else {
      // beep();
    }
  }

  @Override
  public boolean onTouch( View v, MotionEvent ev )
  { 
    if ( mView.mMode == mView.MODE_OVER ) return true;

    int x = (int)ev.getX();
    int y = (int)ev.getY();
    int action = ev.getAction() & MotionEvent.ACTION_MASK;
   
    if ( action == MotionEvent.ACTION_DOWN ) {
      row1 = mView.getRow( x, y );
      col1 = mView.getColumn( x, y );
    } else if ( action == MotionEvent.ACTION_UP ) {
      row2 = mView.getRow( x, y );
      col2 = mView.getColumn( x, y );
      // Log.v("Blue", "Touch from " + row1 + "," + col1 + " to " + row2 + "," + col2 );
      if ( col1 >= 0 && col2 >= 0 && row1 >= 0 && row2 >= 0 ) {
        if ( mView.mMode == mView.MODE_STRATEGY ) {
          if ( col2 == 0 && col1 == 0 ) {
            if ( mView.mHistoryPos == 0 ) {
              mView.swapAces( row1, row2 );
            } else {
              goBackForthInHistory( row1, row2 );
            }
          } else if ( row1 == row2 ) {
            if ( ! mView.markStrategy( col1, row1, col2, row2 ) ) {
              beep();
            }
          } else {
            beep();
          }
        } else { // mView.MODE_PLAY
          if ( col1 > 0 && col2 > 0 ) {
            if ( mView.moveCard( col1, row1, col2, row2 ) ) { 
              if ( mView.isGameOver() ) {
                (new BlueInfoDialog( this )).show();
                // mView.mMode = mView.MODE_OVER;
              }
              setTheTitle();
            } else {
              beep();
            }
          } else if ( col1 == 0 && col2 == 0 ) {
            goBackForthInHistory( row1, row2 );
          } else {
            beep();
          }
        }
      } else {
        beep();
      }
    }
    return true;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    if ( mView != null ) mView.resume();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    // mData.save();
    if ( mView != null ) mView.pause();
  }

  @Override
  public void onStop()
  {
    super.onStop();
  }

  @Override
  public void onBackPressed()
  {
    if ( mView.mMode == mView.MODE_PLAY ) {
      mView.goBackward();
      setTheTitle();
    }
  }

  @Override
  public boolean onSearchRequested()
  {
    if ( mView.mMode == mView.MODE_PLAY ) {
      mView.goForward();
      setTheTitle();
    }
    return false; // block search
  }

}
