/* @file Card.java
 *
 * @author marco corvi
 *
 * @brief Blue cards
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.marcocorvi.blue;

import android.graphics.Bitmap;
import android.graphics.Paint;

class Card
{
  int suit;
  int value;
  Bitmap bitmap;
  // int color;
  Paint paint;

  private static final String[] cardName = {
    "AH", "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "10H", "JH", "QH", "KH",
    "AC", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "10C", "JC", "QC", "KC",
    "AD", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "10D", "JD", "QD", "KD",
    "AS", "2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "10S", "JS", "QS", "KS"
  };

  static String indexToString( int index )
  {
    return ( index < 0 )? "**" : cardName[index]; 
  }

  static int stringToIndex( String str )
  {
    for ( int i=0; i<52; ++i ) {
      if ( str.equals( cardName[i] ) ) return i;
    }
    return -1;
  }

  Card( int k, int i, Bitmap bmap, Paint pnt )
  {
    suit   = k;
    value  = i;
    bitmap = bmap;
    // color  = colors[k];
    paint  = pnt;
  }
}
