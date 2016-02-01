/** BlueFileDialog.java
 *
 */
package com.marcocorvi.blue;

import java.io.File;
import java.io.FileInputStream;
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
import android.view.ViewGroup.LayoutParams;

import android.util.Log;

class BlueFileDialog extends Dialog
                     implements OnClickListener
                     , OnItemClickListener
{
  static final String BLUE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bleu";

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
      // Log.v("Blue", "internal files " + files.length );
      for ( k=0; k < files.length; ++k ) {
        mFiles.add( files[k] );
      }
    // } catch ( IOException e ) { }

    mFilesExt.clear();
    File dir = new File( BLUE_DIR );
    if ( ! dir.exists() ) {
      // dir.mkdirs();
    } else {
      files = dir.list();
      // Log.v("Blue", "external files " + files.length );
      for ( k=0; k < files.length; ++k ) {
        File file = new File( dir, files[k] );
        if ( ! file.isDirectory() ) {
          mFilesExt.add( files[k] );
        }
      }
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
          BlueStore.loadGame( fis, mView, mFilename );
          mParent.setTheTitle();
        } catch ( FileNotFoundException e ) {
          // todo
        } finally {
          if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
        }
        // dismiss dialog
      } else if ( b == mBtnExport ) {
        exportFile( mFilename );
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

  private void exportFile( String filename )
  {
    File dir = new File( BLUE_DIR );
    if ( ! dir.exists() ) dir.mkdirs();

    FileInputStream fis = null;
    FileOutputStream fos = null;
    try { 
      fis = mContext.openFileInput( filename );
      byte[] buffer = BlueStore.readGame( fis );
      if ( buffer != null ) {
        // Log.v("Blue", "buffer length " + buffer.length );
        File out = new File( BLUE_DIR + "/" + filename );
        fos = new FileOutputStream( out );
        BlueStore.writeGame( fos, buffer );
        updateList();
      } else {
        Log.v("Blue", "null game buffer");
      }
    } catch ( FileNotFoundException e ) {
      // todo
    } finally {
      if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
      if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
    }
  }

  private void importFile( String filename )
  {
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try { 
      // Log.v("Blue", "import " + filename );
      fis = new FileInputStream( BLUE_DIR + "/" + filename );
      byte[] buffer = BlueStore.readGame( fis );
      if ( buffer != null ) {
        // Log.v("Blue", "import buffer length " + buffer.length );
        fos = mContext.openFileOutput( filename, Context.MODE_PRIVATE );
        BlueStore.writeGame( fos, buffer );
        updateList();
      } else {
        Log.v("Blue", "null game buffer");
      }
    } catch ( FileNotFoundException e ) {
      // todo
    } finally {
      if ( fis != null ) try { fis.close(); } catch ( IOException e ) { }
      if ( fos != null ) try { fos.close(); } catch ( IOException e ) { }
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    if ( item != null ) {
      if ( (ListView)parent == mListExt ) {
        importFile( item.toString() );
      } else {
        mCanSave  = false;
        mFilename = item.toString();
        setTitle( String.format( mContext.getResources().getString(R.string.game_), mFilename ) );
      }
    }
  }

}

