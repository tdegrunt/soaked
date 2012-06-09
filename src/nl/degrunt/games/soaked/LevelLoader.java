package nl.degrunt.games.soaked;

import java.util.*;
import java.io.*;

public class LevelLoader {

	private Vector levels;
	
	private int nrOfLevels;
	
	
	
	public LevelLoader( BufferedReader reader ) {
		levels = new Vector( 100 );
		nrOfLevels = 0;
		parsePack(reader);
	}
	
	private void parsePack( BufferedReader reader ) {
	
		String str;
		int x = 0;
		int y = 0;
		char currChar;
		Level thisLevel = new Level();
		
		try {
			while( (str = reader.readLine()) != null ) {

				if( str.startsWith("Title:") == true ) {
					thisLevel.setTitle( str.substring(7) );
				} else if( str.startsWith("Author:") == true ) {
					thisLevel.setAuthor( str.substring(7) );
				} else if( str.startsWith("Comment:") == true ) {
					thisLevel.setComment( str.substring(8) );
				} else if( str.trim().length() == 0 ) {
					levels.addElement( thisLevel );
					thisLevel = null;
					thisLevel = new Level();
					nrOfLevels++;
					y = 0;
				} else {
					for( x = 0; x < Level.MAX_LEVEL_WIDTH && x < str.length(); x++ ) {
						currChar = str.charAt(x);
						switch( currChar ) {
							case '$':
						      thisLevel.setGrid(x,y,Level.OBJECT_BOX);
								break;
							case '.':
								thisLevel.setGrid(x,y,Level.OBJECT_TARGET);
								break;
							case '*':
								thisLevel.setGrid(x,y,(byte)(Level.OBJECT_BOX|Level.OBJECT_TARGET));
								break;
							case '@':
								thisLevel.setXPosition(x);
								thisLevel.setYPosition(y);
								break;
							case '+':
								thisLevel.setGrid(x,y,Level.OBJECT_TARGET);
								thisLevel.setXPosition(x);
								thisLevel.setYPosition(y);
								break;
							case '#':
								thisLevel.setGrid(x,y,Level.OBJECT_WALL);
								break;
							default:
								break;
						}
					}
					y++;
				}
			}
			levels.addElement(thisLevel);
		} catch( FileNotFoundException fnfe ) {
			System.err.println(fnfe);
		} catch( IOException ioe ) {
			System.err.println(ioe);
		}		
	
	}
	
	public Level getLevel( int levelNr ) {
		Level result = new Level( (Level)levels.elementAt(levelNr) ); 
		return( result );
	}
	
	public int getNrOfLevels( ) {
		return( nrOfLevels );
	}
	
}

