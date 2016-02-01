#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char suit[4] = { 'H', 'C', 'D', 'S' };
char val[13] = { 'A', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K' }; 


const char * label( unsigned char b )
{
  static char ret[4];
  unsigned char s = b / 13;
  unsigned char v = b % 13;
  if ( b >= 52 ) return " --";
  ret[0] = (v == 9)? '1' : ' ';
  ret[1] = val[ v ];
  ret[2] = suit[ s ];
  ret[3] = 0;
  return ret;
}

int main( int argc, char ** argv )
{
  int show_history = 0;
  FILE * fp;
  unsigned char header[11];
  unsigned char board[56*3];
  unsigned char move[4];
  const char * filename = argv[1];

  if ( argc < 2 ) {
    fprintf(stderr, "Usage: %s <blue-file>\n", argv[0] );
    return 0;
  }
  if ( argc > 2 ) {
    if ( strcmp(argv[1], "-v") == 0 ) show_history = 1;
    filename = argv[2];
  }

  fp = fopen( filename, "rb" );
  if ( fp == NULL ) {
    fprintf(stderr, "Cannot open file \'%s\'\n", filename );
    return 0;
  }
  if ( fread( &header, sizeof(unsigned char), 11, fp ) != 11 ) {
    fprintf(stderr, "Bad header\n");
  } else {
    unsigned int tot  = header[0] + ((unsigned int)(header[1]) << 8);
    unsigned int mode = header[2];
    unsigned int hpos = header[3] + ((unsigned int)(header[4]) << 8);
    unsigned int size = header[5] + ((unsigned int)(header[6]) << 8);
    unsigned int seed = header[7] + ((unsigned int)(header[8]) << 8)
             + ((unsigned int)(header[9])<<16) + ((unsigned int)(header[10]) << 24);
    printf(" Total %d Mode %d Pos %d Size %d Seed %ud \n", tot, mode, hpos, size, seed );
    if ( fread( &board, sizeof(unsigned char), 56*3, fp ) != 56*3 ) {
      fprintf(stderr, "Bad board data\n");
    } else {
      int i, j;
      unsigned int s;
      printf("Initial board:\n");
      for ( j=0; j<4; ++j ) {
        for ( i=0; i<14; ++i ) {
          printf("%s ", label( board[j*14+i] ) );
        }
        printf("\n");
      }
      printf("Current board:\n");
      for ( j=0; j<4; ++j ) {
        for ( i=0; i<14; ++i ) {
          printf("%s ", label( board[56 + j*14+i] ) );
        }
        printf("\n");
      }
      printf("Strategy board:\n");
      for ( j=0; j<4; ++j ) {
        for ( i=0; i<14; ++i ) {
          printf("%s ", label( board[112 + j*14+i] ) );
        }
        printf("\n");
      }
      if ( show_history ) {
        for ( s = 0; s < size; ++s ) {
          if ( fread( move, sizeof(unsigned char), 4, fp ) != 4 ) {
            printf("cannot read move %d/%d\n", s, size );
            break;
          }
          printf("Move %3d:  %3d/%3d  %3d/%3d\n", s, move[0], move[1], move[2], move[3] );
        }
      }
    }
  }
  fclose( fp );
  return 0;
}  
