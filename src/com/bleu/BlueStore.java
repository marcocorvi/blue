

package com.marcocorvi.blue;

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

import java.util.regex.Pattern;

import android.util.Log;

class BlueStore
{
  static final char strategyChar[] = 
  { 'A', '2', '3', '4', '5', '6', '7', '8', '9', '1', 'J', 'Q', 'K' };

  static final String strategySuit[] = { "H", "C", "D", "S" };

  static String strategyToString( int s ) 
  {
    if ( s < 0 ) return "**";
    int suit = s / 13;
    int val  = s % 13;
    if ( val == 9 ) { // 10
      return "10" + strategySuit[suit];
    }
    return strategyChar[val] + strategySuit[suit];
  }

  static int stringToStrategy( String str )
  {
    char c = str.charAt( 0 );
    char s = str.charAt( str.length() - 1 );
    if ( c != '*' ) {
      int suit = 0;
      switch (s) {
          case 'H': suit =  0; break;
          case 'C': suit = 13; break;
          case 'D': suit = 26; break;
          case 'S': suit = 39; break;
      }
      for ( int k=0; k<13; ++k ) {
        if ( c == strategyChar[k] ) return suit + k;
      }
    }
    return -1;
  }

  static boolean writeGame( FileOutputStream fos, byte[] buffer )
  {
    // Log.v("Blue", "write buffer size " + buffer.length );
    try {
      fos.write( buffer, 0, buffer.length );
    } catch ( IOException e ) {
      return false;
    }
    return true;
  }

  static byte[] readGame( FileInputStream fis )
  {
    byte[] buffer = null;
    try {
      int t1 = fis.read();
      int t2 = fis.read();
      int tot = t1 | ( t2 << 8 );
      // Log.v("Blue", "read buffer size " + tot + " " + t1 + " " + t2 );
      if ( t1 < 0 || t2 < 0 || tot <= 2 ) return null;
      buffer = new byte[ tot ];
      buffer[0] = (byte)t1;
      buffer[1] = (byte)t2;
      int n = fis.read( buffer, 2, tot-2 );
      if ( n != tot-2 ) return null;
    } catch ( IOException e ) {
      return null;
    }
    return buffer;
  }

  static boolean saveGame( FileOutputStream fos, BlueView view )
  {
    byte[] buffer = view.getState();
    return writeGame( fos, buffer );
  }

  static boolean loadGame( FileInputStream fis, BlueView view, String filename )
  {
    byte[] buffer = readGame( fis );
    if ( buffer == null ) return false;
    view.restore( buffer, false, filename );
    return true;
  }

  static boolean exportGame( FileOutputStream fos, BlueView view )
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    String date = sdf.format( new Date() );
    // try {
      // FileWriter fw = new FileWriter( fos );
      PrintWriter pw = new PrintWriter( fos );

      pw.format("# Blue Game - %s\n", date );
      pw.format("#\n");
      pw.format("# SEED\n");
      pw.format("%d\n", view.mSeed );
      pw.format("# PLAYMODE\n");
      pw.format("%d\n", view.mMode );

      int[] b = view.mStartBoard;
      pw.format("# INITIAL BOARD\n");
      for ( int j=0; j<4; ++j ) {
        for ( int i=0; i < 14; ++i ) {
          pw.format("%3s ", Card.indexToString( b[j*14+i] ) );
        }
        pw.format("\n");
      }

      b = view.mBoard;
      pw.format("# CURRENT BOARD\n");
      for ( int j=0; j<4; ++j ) {
        for ( int i=0; i < 14; ++i ) {
          pw.format("%3s ", Card.indexToString( b[j*14+i] ) );
        }
        pw.format("\n");
      }

      int[] s = view.mStrategy;
      pw.format("# STRATEGY\n");
      for ( int j=0; j<4; ++j ) {
        pw.format(" ** ");
        for ( int i=1; i < 14; ++i ) {
          pw.format("%3s ", strategyToString( s[j*14+i] ) );
        }
        pw.format("\n");
      }

      List<BlueMove> h = view.mHistory;
      pw.format("# HISTORY %d %d\n", view.mHistoryPos, h.size() );
      for ( BlueMove move: h ) {
        pw.format("%d %d 0\n", move.row1*14+move.col1, move.row2*14+move.col2 );
      }

    // } catch ( IOException e ) { return false; }
    return true;
  }

  static String readLine( BufferedReader br )
  {
    String line = "";
    try {
      line = br.readLine().trim();
      // Log.v("Blue", "LINE: " + line );
    } catch ( IOException e ) {
      Log.v("Blue", "IOException " + e.toString() );
    }
    return line;
  }

  static boolean importGame( FileInputStream fis, BlueView view )
  {
    Pattern pattern = Pattern.compile( "\\s+" );
    String line;
    String[] vals;
      // FileReader fr = new FileReader( fis );
      InputStreamReader isr = new InputStreamReader( fis );
      BufferedReader br = new BufferedReader( isr );
      line = readLine( br ); // # Blue Game - date
      line = readLine( br ); // # 
      line = readLine( br ); // # SEED
      line = readLine( br ); // SEED_VALUE
      try {
        view.mSeed = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        return false;
      }
      line = readLine( br ); // # PLAYMODE
      line = readLine( br ); // PLAYMODE_VALUE
      view.mMode = Integer.parseInt( line );

      line = readLine( br ); // # INITIAL BOARD
      for ( int j=0; j<4; ++j ) {
        line = readLine( br );
        vals = pattern.split(line); 
        for ( int i=0; i < 14; ++i ) {
          view.mStartBoard[j*14+i] = Card.stringToIndex( vals[i] );
        }
      }

      line = readLine( br ); // # CURRENT BOARD
      for ( int j=0; j<4; ++j ) {
        line = readLine( br );
        vals = pattern.split(line); 
        for ( int i=0; i < 14; ++i ) {
          view.mBoard[j*14+i] = Card.stringToIndex( vals[i] );
        }
      }

      line = readLine( br ); // # STRATEGY
      for ( int j=0; j<4; ++j ) {
        line = readLine( br );
        vals = pattern.split(line); 
        for ( int i=0; i < 14; ++i ) {
          view.mStrategy[j*14+i] = stringToStrategy( vals[i] );
        }
      }

      view.mHistory.clear();
      int size = 0;
      List<BlueMove> h = view.mHistory;
      line = readLine( br ); // # HISTORY %d %d\n", view.mHistoryPos, h.size()
      vals = pattern.split(line); 
      try {
        view.mHistoryPos = Integer.parseInt( vals[2] );
        size = Integer.parseInt( vals[2] );
      } catch ( NumberFormatException e ) {
        view.mHistoryPos = 0;
        return true;
      }
      for ( ; size > 0; --size ) {
        line = readLine( br );
        vals = pattern.split(line); 
        int rc1 = Integer.parseInt( vals[0] );
        int rc2 = Integer.parseInt( vals[1] );
        view.mHistory.add( new BlueMove( rc1/14, rc1%14, rc2/14, rc2%14 ) );
      }
    return true;
  }

}
