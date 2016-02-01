#
#  Makefile for the card game "blue"
#
#
# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Put the directory containing the card pixmaps
#
PIXMAPS= '"../pixmap"'
#
# Select the background and foreground colors
#
BGCOLOR= '"navy blue"'
FGCOLOR= '"white"'
#
# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Compiler (and linker)
CC=gcc
# CC=cc

# Compiler flags
CFLAGS= -g -O3 -Wall

# X library
XLIB=-L/usr/X11R6/lib -lX11 
# XLIB=-L/usr/lib -lX11
# XLIB=-L/usr/openwin/lib -lX11

# X include
XINC=
# XINC=-I/usr/openwin/include

# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

GUI=$(XINC) -DPIXMAPS=$(PIXMAPS) -DBGCOLOR=$(BGCOLOR) -DFGCOLOR=$(FGCOLOR) $(XLIB)

# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Make arguments

default: blue 

clean:
	rm -f *.o core

realclean:
	rm -f *.o core blue

#
#
# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Executable file

blue: playblue.c playblue.h gameft.o  gui.o history.o
	$(CC) $(CFLAGS) -o blue playblue.c gameft.o gui.o history.o $(GUI)

# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Object files

history.o: history.c history.h Makefile
	$(CC) $(CFLAGS) -c history.c          

gameft.o: gameft.c history.h Makefile
	$(CC) $(CFLAGS) -c gameft.c          

gui.o: gui.c  marks.h Makefile
	$(CC) $(CFLAGS) -o gui.o -c gui.c -DPIXMAPS=$(PIXMAPS)
