/** @file BlueHelp.java
 *
 * @author marco corvi
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 */
package com.marcocorvi.blue;

import android.content.Context;
// import android.content.Intent;
// import android.content.ActivityNotFoundException;

import android.app.Dialog;
import android.view.View;
import android.webkit.WebView;
import android.view.ViewGroup.LayoutParams;

class BlueHelp extends Dialog
{
  private Context mContext;

  BlueHelp( Context context )
  {
    super( context );
    mContext = context;
    setContentView(R.layout.help);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( context.getResources().getString(R.string.app_name) );
    WebView wv = (WebView) findViewById( R.id.text );
    String html = context.getResources().getString( R.string.help ).replaceAll("BR", "<br><p>");
    wv.loadData( "<html><body>" + html + "</body></html>", "text/html", null );
  }
  
}
