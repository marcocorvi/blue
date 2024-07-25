/** BlueInfoDialog.java
 *
 * @author marco corvi
 *
 * @brief Blue info
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.marcocorvi.blue;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.res.Resources;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

class BlueInfoDialog extends Dialog
                     implements View.OnClickListener
{
  private Button mBtnOK;
  private Button mBtnExport;
  private Context mContext;
  private BlueActivity mParent;
  private BlueView mView;

  BlueInfoDialog( Context context, BlueActivity parent, BlueView view )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mView    = view;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.blue_info_dialog);
    // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mBtnOK     = (Button) findViewById( R.id.btn_ok );
    mBtnExport = (Button) findViewById( R.id.btn_export );
    mBtnOK.setOnClickListener( this );
    mBtnExport.setOnClickListener( this );

    setTitle( R.string.game_over );
  }

  @Override
  public void onClick( View v )
  {
    if ( v.getId() == R.id.btn_ok ) {
    } else if ( v.getId() == R.id.btn_export ) {
      BlueFileDialog.exportGame( mContext, mView, Long.toString( mView.mSeed ) + ".txt" );
    }
    dismiss();
  }

}

