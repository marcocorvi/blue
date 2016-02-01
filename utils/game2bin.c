#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>

int main( int argc, char ** argv )
{
  FILE * fin;
  FILE * fout;
  char line[1024];
  int seed;
  int mode;
  int tot = 2 + 1 + 2 + 2 + 4 + 56 + 56 + 56 + 0;
  unsigned char b4[56];
  int i, j;

  if ( argc < 3 ) {
    printf("Usage: %s <game_file> <bin_file>\n", argv[0] );
    return 0;
  }
  fin = fopen( argv[1], "r" );
  fout = fopen( argv[2], "w" );
  if ( fin == NULL || fout == NULL ) {
    printf("Cannot open file(s)\n");
    return 0;
  }
  fgets( line, 1023, fin ); // # Blue Game - date
  fgets( line, 1023, fin ); // #

  fgets( line, 1023, fin ); // # SEED
  fgets( line, 1023, fin ); 
  sscanf( line, "%d", &seed );

  fgets( line, 1023, fin ); // # PLAYMODE
  fgets( line, 1023, fin ); 
  sscanf( line, "%d", &mode );

  b4[0] = (unsigned char)( tot & 0xff );
  b4[1] = (unsigned char)( (tot >> 8) & 0xff );
  b4[2] = mode & 0xff;
  fwrite( b4, 1, 3, fout ); // tot + mode
  b4[0] = 0;
  b4[1] = 0;
  b4[2] = 0; 
  b4[3] = 0; 
  fwrite( b4, 1, 4, fout ); // history_pos + size
  b4[0] = (unsigned char)( seed & 0xff );
  b4[1] = (unsigned char)( (seed >>  8) & 0xff );
  b4[2] = (unsigned char)( (seed >> 16) & 0xff );
  b4[3] = (unsigned char)( (seed >> 24) & 0xff );
  fwrite( b4, 1, 4, fout ); // seed
  fgets( line, 1023, fin ); // # INITIAL BOARD

  for ( j=0; j<4; ++j ) {
    int v=0, s=0;
    char * ch;
    fgets( line, 1023, fin ); // # BOARD j-th row
    ch = line;
    for ( i = 0; i<14; ++i ) {
    while( isspace( *ch ) ) ++ch;
      switch (*ch) {
        case '*': v = -1; break;
        case 'A': v = 0; break;
        case '1': v = 9; ++ch; break;
        case 'J': v = 10; break;
        case 'Q': v = 11; break;
        case 'K': v = 12; break;
        default: v = *ch - '1'; break;
      }
      ++ ch;
      switch (*ch) {
        case '*': s= 0; break;
        case 'H': s= 0; break;
        case 'C': s=13; break;
        case 'D': s=26; break;
        case 'S': s=39; break;
      }
      b4[ j*14 + i ] = (unsigned char)(v + s);
      ++ch;
    }
  }
  fwrite( b4, 1, 56, fout ); // start board
  fwrite( b4, 1, 56, fout ); // board
  for ( i=0; i<56; ++i ) b4[i] = (unsigned char)(-1);
  fwrite( b4, 1, 56, fout ); // strategy
  fclose( fin );
  fclose( fout );
  return 0;
}
