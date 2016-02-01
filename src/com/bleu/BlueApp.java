/** BlueAapp.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief Blue app
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.marcocorvi.blue;

import java.io.File;
// import java.io.FileFilter;
import java.io.IOException;
// import java.io.StringWriter;
// import java.io.PrintWriter;
// import java.io.PrintStream;
// import java.io.FileWriter;
import java.io.FileNotFoundException;
// import java.io.FileReader;
// import java.io.BufferedReader;
import java.io.InputStream;
// import java.io.FileInputStream;
// import java.io.BufferedInputStream;
import java.io.FileOutputStream;
// import java.io.BufferedOutputStream;
// import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
// import java.util.zip.ZipFile;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Debug;

import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Activity;

import android.preference.PreferenceManager;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;

import android.view.WindowManager;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;

import android.util.Log;
import android.util.DisplayMetrics;

import android.widget.Toast;

public class BlueApp extends Application
                             implements OnSharedPreferenceChangeListener
{
  static String VERSION = "00"; 
  static int VERSION_CODE;
  // static boolean mComplete = false;
  
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;

  static long mAvailableGames = 0;
  static long mGamesNumber = 30; // FIXME read number from context
  static String[] mGames;

  static String getCurrentGame() { return mGames[ (int)mAvailableGames ]; }

  // ----------------------------------------------------------------------

  private SharedPreferences mPrefs;

  BlueActivity mActivity = null; 

  @Override
  public void onTerminate()
  {
    super.onTerminate();
    // Log.v(TAG, "onTerminate app");
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    try {
      VERSION      = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
      VERSION_CODE = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode;
    } catch ( NameNotFoundException e ) {
      // FIXME
      e.printStackTrace();
    }

    // Log.v(TAG, "onCreate app");
    mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
    mPrefs.registerOnSharedPreferenceChangeListener( this );

    // mComplete = mPrefs.getBoolean( "COMPLETE", false );
    // if ( mComplete ) {
    //   Editor editor = mPrefs.edit();
    //   editor.putBoolean( "COMPLETE", mComplete );
    //   editor.commit();
    // }

    int currentVersionCode = (int)( mPrefs.getLong( "VERSION", 0 ) );
    // Log.v("Blue", "VERSION " + currentVersionCode + " " + VERSION_CODE );

    if ( VERSION_CODE > currentVersionCode ) {
      Editor editor = mPrefs.edit();
      editor.putLong( "VERSION", VERSION_CODE );
      editor.commit();
      InputStream is = getResources().openRawResource( R.raw.games );
      uncompressGames( is );
    }
    mGames = fileList(  );
    mGamesNumber = mGames.length;
    mAvailableGames = mPrefs.getLong( "GAMES", 0L );
    mAvailableGames %= mGamesNumber;
    Log.v("Blue", "GAMES " + mAvailableGames + " " + mGamesNumber );
  }

  
  String getNextGame( )
  {
    mAvailableGames = ( mAvailableGames + 1 ) % mGamesNumber;
    Editor editor = mPrefs.edit();
    editor.putLong( "GAMES", mAvailableGames );
    editor.commit();
    return mGames[ (int)mAvailableGames ];
  }

  // -----------------------------------------------------------------

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    // TODO
  }

  void decreaseGames( )
  {
    mAvailableGames --;
    Editor editor = mPrefs.edit();
    editor.putLong( "GAMES", mAvailableGames );
    editor.commit();
  }

  // -------------------------------------------------------------
  private int uncompressGames( InputStream fis )
  {
    if ( fis == null ) return -1;
    int cnt = 0;
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        // File file = new File( filepath );
        // Log.v("Blue", "Uncompressing " + filepath );
        ++cnt;
        // FileOutputStream fout = new FileOutputStream( file );
        FileOutputStream fout = this.openFileOutput( filepath, Context.MODE_PRIVATE );
        int c;
        while ( ( c = zin.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c); // offset 0 in buffer
        }
        fout.close();
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    Log.v("Blue", "uncompressed " + cnt + " files " );
    return cnt;
  }


}
