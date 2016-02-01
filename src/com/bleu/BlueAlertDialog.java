/** BlueAlertDialog.java
 *
 */
package com.marcocorvi.blue;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;

import android.widget.TextView;

class BlueAlertDialog
{

  BlueAlertDialog( Context context, Resources res, String title, DialogInterface.OnClickListener pos )
  {
      // NEED API LEVEL 11 for custom background color

      AlertDialog.Builder alert_builder = new AlertDialog.Builder( context );

      alert_builder.setMessage( title );

      alert_builder.setPositiveButton( res.getString( R.string.btn_cancel ), 
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int btn ) { }
          }
      );

      alert_builder.setNegativeButton( res.getString( R.string.btn_ok ), pos );

      AlertDialog alert = alert_builder.create();
      // alert.getWindow().setBackgroundDrawableResource( R.color.background );
      alert.show();
  }

}