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
  private Context mContext;

  BlueInfoDialog( Context context )
  {
    super( context );
    mContext = context;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.blue_info_dialog);
    // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // mBtnOK = (Button) findViewById( R.id.btn_ok );
    // mBtnOK.setOnClickListener( this );

    setTitle( R.string.game_over );
  }

  @Override
  public void onClick( View v )
  {
    dismiss();
  }

}

