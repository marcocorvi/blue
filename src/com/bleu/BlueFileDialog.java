/** BlueFileDialog.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief Blue file dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.marcocorvi.blue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.res.Resources;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.res.AssetManager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import android.util.Log;

class BlueFileDialog extends Dialog
                     implements OnClickListener
                     , OnItemClickListener
{
  static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
  static final String BLUE_DIR = SDCARD + "/Android/data/com.marcocorvi.blue/Files";
  static String blue_dir = BLUE_DIR;
  static File mBlueDir;

  private Context  mContext;
  private BlueView mView;
  private BlueActivity mParent;

  private Button   mBtnSave;
  private Button   mBtnLoad;
  private Button   mBtnExport;
  private Button   mBtnDelete;
  private ListView mList;
  private ListView mListExt;
  private String mFilename;
  private boolean  mCanSave;
  private ArrayAdapter< String > mFiles;
  private ArrayAdapter< String > mFilesExt;

  BlueFileDialog( Context context, BlueActivity parent, BlueView view )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mView    = view;
    mCanSave = true;
    mFiles = new ArrayAdapter<String>( mContext, R.layout.message );
    mFilesExt = new ArrayAdapter<String>( mContext, R.layout.message_ext );
    mBlueDir = mContext.getExternalFilesDir( null );
    blue_dir = mBlueDir.getAbsolutePath();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.blue_file_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mBtnSave = (Button) findViewById( R.id.btn_save );
    mBtnLoad = (Button) findViewById( R.id.btn_load );
    mBtnExport = (Button) findViewById( R.id.btn_export );
    mBtnDelete = (Button) findViewById( R.id.btn_delete );

    // if ( BlueApp.mComplete ) {
      mBtnSave.setOnClickListener( this );
    // } else {
    //   mBtnSave.setVisibility( View.GONE );
    // }
    mBtnLoad.setOnClickListener( this );
    mBtnExport.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );

    mList = (ListView) findViewById( R.id.files );
    mList.setAdapter( mFiles );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mListExt = (ListView) findViewById( R.id.files_ext );
    mListExt.setAdapter( mFilesExt );
    mListExt.setOnItemClickListener( this );
    mListExt.setDividerHeight( 2 );

    updateList();
    setDefaultFilename();
    // setTitle( R.string.game_list );
  }


  private void setDefaultFilename()
  {
    if ( mView != null ) {
      mFilename = Long.toString( mView.mSeed );
      setTitle( String.format( mContext.getResources().getString(R.string.game_), mFilename ) );
    } else {
      mFilename = null;
      setTitle( R.string.game_list );
      // mFilename.setText( "" );
    }
  }

  private void updateList()
  {
    int k;
    mFiles.clear();
    // try {
    //   AssetManager assets = mContext.getAssets();
    //   String[] games = assets.list( "games" );
    //   for ( k=0; k < games.length; ++k ) {
    //     mFiles.add( games[k] );
    //   }
    // } catch ( IOException e ) { }
    // try {
      String[] files = mContext.fileList(  );
      // Log.v( BlueApp.TAG, "internal files " + files.length );
      for ( k=0; k < files.length; ++k ) {
        mFiles.add( files[k] );
      }
    // } catch ( IOException e ) { }

    // check external files only in default BLUE_DIR
    mFilesExt.clear();
    // File dir = new File( BLUE_DIR );
    // File dir = new File( blue_dir );
    // if ( ! dir.exists() ) {
    //   if ( ! dir.mkdirs() ) {
    //     // dir = new File( SDCARD );
    //   }
    // } 
    if ( mBlueDir.exists() ) {
      files = mBlueDir.list();
      if ( files != null ) {
        // Log.v( BlueApp.TAG, "external files " + files.length );
        for ( k=0; k < files.length; ++k ) {
          File file = new File( mBlueDir, files[k] );
          if ( ! file.isDirectory() ) {
            mFilesExt.add( files[k] );
          }
        }
      }
    } else {
      Log.v( BlueApp.TAG, "failed dir " + blue_dir );
      Toast.makeText( mContext, R.string.fail_blue_dir, Toast.LENGTH_LONG ).show();
    }
  }

  @Override
  public void onClick( View v )
  {
    Button b = (Button)v;
    if ( mFilename != null && mFilename.length() > 0 ) {
      if ( b == mBtnSave ) {
        if ( mCanSave ) {
          FileOutputStream fos = null;
          try {
            fos = mContext.openFileOutput( mFilename, Context.MODE_PRIVATE );
            BlueStore.saveGame( fos, mView );
          } catch ( FileNotFoundException e ) {
            // todo
          } finally {
            if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
          }
        }
        // dismiss dialog
      } else if ( b == mBtnLoad ) {
        FileInputStream fis = null;
        try { 
          fis = mContext.openFileInput( mFilename );
          BlueStore.loadGame( fis, mView, mFilename, false );
          mParent.setTheTitle();
        } catch ( FileNotFoundException e ) {
          // todo
        } finally {
          if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
        }
        // dismiss dialog
      } else if ( b == mBtnExport ) {
        exportGame( mContext, mView, mFilename );
        return; // do not dismiss dialog
      } else if ( b == mBtnDelete ) {
        mContext.deleteFile( mFilename );
        updateList();
        setDefaultFilename();
        return; // do not dismiss dialog
      }
    }
    dismiss();
  }

  static boolean checkBlueDir( Context context )
  {
    // File dir = new File( blue_dir );
    // if ( ! dir.exists() ) {
    //   if ( ! dir.mkdirs() ) {
    //     blue_dir = SDCARD;
    //     dir = new File( blue_dir );
    //   }
    // }
    if ( ! mBlueDir.exists() ) {
      Log.v( BlueApp.TAG, "failed dir " + blue_dir );
      Toast.makeText( context, R.string.fail_blue_dir, Toast.LENGTH_LONG ).show();
      return false;
    }
    return true;
  }

  static boolean exportGame( Context context, BlueView view, String filename )
  {
    
    if ( ! checkBlueDir( context ) ) return false;
    FileWriter fos = null;
    try { 
      File out = new File( mBlueDir, filename );
      Log.v( BlueApp.TAG, "Export file: " + out.getAbsolutePath() );
      fos = new FileWriter( out );
      BlueStore.exportGame( fos, view );
      // updateList();
      Toast.makeText( context, R.string.ok_export, Toast.LENGTH_LONG ).show();
    } catch ( FileNotFoundException e ) {
      Log.v( BlueApp.TAG, "File not found " + e.getMessage() );
      Toast.makeText( context, R.string.fail_export, Toast.LENGTH_LONG ).show();
      return false;
    } catch ( IOException e ) {
      Log.v( BlueApp.TAG, "IO error " + e.getMessage() );
    } finally {
      if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
    }
    return true;
  }


  // static boolean exportFile( Context context, String filename )
  // {
  //   Log.v( BlueApp.TAG, "Export game file: \"" + filename + "\"" );
  //   if ( ! checkBlueDir( context ) ) return false;
  //   FileInputStream fis = null;
  //   FileOutputStream fos = null;
  //   try { 
  //     fis = context.openFileInput( filename );
  //     byte[] buffer = BlueStore.readGame( fis );
  //     if ( buffer != null ) {
  //       // Log.v( BlueApp.TAG, "buffer length " + buffer.length );
  //       File out = new File( mBlueDir, filename );
  //       fos = new FileOutputStream( out );
  //       BlueStore.writeGame( fos, buffer );
  //       // updateList();
  //       Toast.makeText( context, R.string.ok_export, Toast.LENGTH_LONG ).show();
  //     } else {
  //       Log.v( BlueApp.TAG, "null game buffer");
  //       Toast.makeText( context, R.string.fail_buffer, Toast.LENGTH_LONG ).show();
  //       return false;
  //     }
  //   } catch ( FileNotFoundException e ) {
  //     Log.v( BlueApp.TAG, "File not found " + e.getMessage() );
  //     Toast.makeText( context, R.string.fail_export, Toast.LENGTH_LONG ).show();
  //     return false;
  //   } finally {
  //     if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
  //     if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
  //   }
  //   return true;
  // }

  private boolean importFile( Context context, String filename )
  {
    if ( ! checkBlueDir( context ) ) return false;
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try { 
      // Log.v( BlueApp.TAG, "import " + filename );
      fis = new FileInputStream( new File( mBlueDir, filename ) );
      byte[] buffer = BlueStore.readGame( fis );
      if ( buffer != null ) {
        // Log.v( BlueApp.TAG, "import buffer length " + buffer.length );
        fos = context.openFileOutput( filename, Context.MODE_PRIVATE );
        BlueStore.writeGame( fos, buffer );
        updateList();
      } else {
        Log.v( BlueApp.TAG, "null game buffer");
        Toast.makeText( context, R.string.fail_import, Toast.LENGTH_LONG ).show();
        return false;
      }
    } catch ( FileNotFoundException e ) {
      Log.v( BlueApp.TAG, "fail import " + e.getMessage() );
      Toast.makeText( context, R.string.fail_import, Toast.LENGTH_LONG ).show();
      return false;
    } finally {
      if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
      if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
    }
    return true;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    if ( item != null ) {
      if ( (ListView)parent == mListExt ) {
        importFile( mContext, item.toString() );
      } else {
        mCanSave  = false;
        mFilename = item.toString();
        setTitle( String.format( mContext.getResources().getString(R.string.game_), mFilename ) );
      }
    }
  }

}

