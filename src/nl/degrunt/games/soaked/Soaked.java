/**
 * 
 *    ___            _          _ _ 
 *   / __| ___  __ _| |_____ __| | |
 *   \__ \/ _ \/ _` | / / -_) _` |_|
 *   |___/\___/\__,_|_\_\___\__,_(_)
 *                                  
 *   A Sokoban implementation.
 *
 */
 
package nl.degrunt.games.soaked;

import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.applet.*;
import java.io.*;

public class Soaked extends Applet implements Runnable {

	final static int IMAGE_OBJECT_WIDTH			 			= 20;
	final static int IMAGE_OBJECT_HEIGHT					= 20;
	
	final static int IMAGE_TOTAL_WIDTH			  			= 80;
	final static int IMAGE_TOTAL_HEIGHT			 			= 120;
	
	final static int TOTAL_IMAGES								= 36;
	final static int TOTAL_SOUNDS								= 4;
	
	final static int OBJECT_FLOOR								= 0;
	final static int OBJECT_WALL					  			= 1;
	
	final static int OBJECT_TARGET							= 3;
	final static int OBJECT_BOX								= 4;
	final static int OBJECT_BOX_ON_TARGET		  		   = 7;
	
	final static int OBJECT_MAN								= 8;

	final static int SOUND_WALK								= 0;
	final static int SOUND_LEVEL								= 1;
	final static int SOUND_PUSH								= 2;

	final static int GAME_STATE_INTRO1						= 1;
	final static int GAME_STATE_WAIT_INTRO1			   = 2;
	final static int GAME_STATE_PLAY							= 3;
	final static int GAME_STATE_LEVEL_ALERT			   = 4;

	final static String[] CONGRATULATIONS 					= { 	"Congratulations!", 
																				"Excellent!", 
																				"Perfect!", 
																				"Superb!", 
																				"Well Done!", 
																				"Fine!", 
																				"Nice!" };
	
	final static String SOAKED_VERSION						= "1.2.1";
	final static String SOAKED_BUILD_DATE_TIME			= "2003-08-08 @ 0:00";
	
	Thread mainThread;
	
	LevelLoader levelLoader;
	Level level;
	boolean endOfGame = false;
	int c;
	int currLevelNr;
	MediaTracker tracker;

	Image	logoImage;
	
	Image[] imageObjects = new Image[TOTAL_IMAGES];
	AudioClip[] sounds = new AudioClip[TOTAL_SOUNDS];
	
	Image bufferImage;
	Graphics bufferGraphics;
	
	// Current game-state and 100milisecond counter
	private int gameState = GAME_STATE_INTRO1;
	private int gameCounter = 0;
	
	// Variables for level pack and theme (and default setting);
	private String levelPack = "microban";
	private String levelTheme = "default";
	
	// Variables for intro level and movements of the Man	
	private Level fakeLevel = new Level();
	private int[] fakeLevelMovements = { 3, 3, 2, 3, 0, 0, 3, 0, 1, 1, 0, 1, 2, 2, 1, 2 };
	private int currFakeLevelMovementPos = 0;
	
	private int currExplScroller = -80;
	
	private String[] levelAlert;
	
	public void init() {
		
		Image mainImage;
		ImageFilter filter;
		ImageProducer producer;

		int x, y, imageCounter;
		
		try {
			levelPack = getParameter("levelpack");
			levelTheme = getParameter("theme");
			
			URL source = new URL(getCodeBase() + "multimedia/levels/"+levelPack+".slp");
			BufferedReader fr = new BufferedReader(new InputStreamReader(source.openStream()));
			
			levelLoader = new LevelLoader( fr );
			currLevelNr = 0;
			
			tracker = new MediaTracker( this );
			mainImage = getImage( getCodeBase(), "multimedia/themes/"+levelTheme+".gif" );
			tracker.addImage(mainImage,0);

			logoImage = getImage( getCodeBase(), "multimedia/themes/logo.gif" );
			tracker.addImage(logoImage,0);

			sounds[SOUND_WALK] = getAudioClip(getCodeBase(), "multimedia/sounds/walk.au");
			sounds[SOUND_LEVEL] = getAudioClip(getCodeBase(), "multimedia/sounds/level.au");
			sounds[SOUND_PUSH] = getAudioClip(getCodeBase(), "multimedia/sounds/push.au");

			imageCounter = 0;
			for( y = 0; y < IMAGE_TOTAL_HEIGHT; y += IMAGE_OBJECT_WIDTH ) {
				for( x = 0; x < IMAGE_TOTAL_WIDTH; x += IMAGE_OBJECT_WIDTH ) {
					filter = new CropImageFilter( x, y, IMAGE_OBJECT_WIDTH, IMAGE_OBJECT_HEIGHT );
					producer = new FilteredImageSource( mainImage.getSource(), filter );
					imageObjects[imageCounter] = createImage(producer);
					tracker.addImage(imageObjects[imageCounter],0);
					imageCounter++;
				}
			}
			
			try {
				tracker.waitForAll();
			} catch( InterruptedException ie ) {
				// Do nothing	
			}

			initFakeLevel();
			level = fakeLevel;
			
			// Prepare off-screen buffer image
			bufferImage = createImage( this.size().width, this.size().height );
			bufferGraphics = bufferImage.getGraphics();
			
			repaint();
		} catch( IOException ioe ) {
		}
	}
	
	public void start() {
		mainThread = new Thread( this );
		mainThread.start();
	}
	
	public void run() {
		while( true ) {
			try {
				mainThread.sleep(100);
			} catch( InterruptedException e ) {
				
			}
			gameCounter = (gameCounter+1)&255;
			repaint();
		}
	}
	
	public void stop() {

	}

	public void update( Graphics g ) {
		paint( g );
	}
	
	public boolean keyDown( Event e, int c ) {
		boolean done = false;
		
		switch( gameState ) {
			case GAME_STATE_PLAY:
				switch( c ) {
					case Event.UP:
						done = level.moveUp();
						break;
					case Event.DOWN:
						done = level.moveDown();
						break;
					case Event.LEFT:
						done = level.moveLeft();
						break;
					case Event.RIGHT:
						done = level.moveRight();
						break;
					case 'n':
					case 'N':
						gameState = GAME_STATE_LEVEL_ALERT;
						if( currLevelNr+1 < levelLoader.getNrOfLevels() ) {
							currLevelNr++;
						}
						String[] nextLevelText = { "Get ready for level "+(currLevelNr+1)+"!" };
						levelAlert = nextLevelText;
						break;
					case 'p':
					case 'P':
						gameState = GAME_STATE_LEVEL_ALERT;
						if( currLevelNr-1 >= 0 ) {
							currLevelNr--;
						}
						String[] prevLevelText = { "Get ready for level "+(currLevelNr+1)+"!" };
						levelAlert = prevLevelText;
						break;
					case 'r':
					case 'R':
						gameState = GAME_STATE_LEVEL_ALERT;
						String[] restartLevelText = { "Get ready for level "+(currLevelNr+1)+"!" };
						levelAlert = restartLevelText;
						break;
					case 'a':
					case 'A':
						level = fakeLevel;
						gameState = GAME_STATE_INTRO1;
						break;
					default:
						break;
				}
				if( done == true ) {
					sounds[SOUND_WALK].play();
				}
		
				if( level.isCompleted() ) {
					gameState = GAME_STATE_LEVEL_ALERT;
					sounds[SOUND_LEVEL].play();
					if( currLevelNr+1 < levelLoader.getNrOfLevels() ) {
						currLevelNr++;
					}
					String[] completedText = { CONGRATULATIONS[(int)(Math.random()*CONGRATULATIONS.length)-1], "Get ready for level "+(currLevelNr+1)+"!" };
					levelAlert = completedText;
				}
				break;
			case GAME_STATE_LEVEL_ALERT:
			default:
				if( c == 32 ) {	// Space
					level = levelLoader.getLevel(currLevelNr);
					gameState = GAME_STATE_PLAY;
				}
				break;
		}
		
		return( false );
	}
	
	public void paint( Graphics g ) {
		switch( gameState ) {
			case GAME_STATE_INTRO1:
				drawIntro1();
				break;
			case GAME_STATE_WAIT_INTRO1:
				waitIntro1();
				break;
			case GAME_STATE_PLAY:
				drawLevel(0,0);
				break;
			case GAME_STATE_LEVEL_ALERT:
				drawLevel(0,0);
				drawLevelAlert();
				break;
			default:
				break;
			
		}
		
		g.drawImage(bufferImage,0,0,this);
	}
	
	public void drawIntro1( ) {
		
		bufferGraphics.setColor(Color.white);
		bufferGraphics.fillRect(0,0,Level.MAX_LEVEL_WIDTH*IMAGE_OBJECT_WIDTH,Level.MAX_LEVEL_HEIGHT*IMAGE_OBJECT_HEIGHT);

		bufferGraphics.drawImage( logoImage, 72, 20, this );
		
		drawLevel( 0, 90 );
		
		bufferGraphics.setColor( Color.black );
		bufferGraphics.drawString( "Press SPACE to start", 140, 370 );
		
		drawExplanation();
		
		gameState = GAME_STATE_WAIT_INTRO1;
	}

	public void waitIntro1( ) {

		// Do a move every half-second 
		if( (gameCounter%5) == 0 ) {
			switch( fakeLevelMovements[currFakeLevelMovementPos] ) {
				case 0:
					level.moveUp();
					break;
				case 1:
					level.moveRight();
					break;
				case 2:
					level.moveDown();
					break;
				case 3:
					level.moveLeft();
					break;
			}
			currFakeLevelMovementPos++;
			if( currFakeLevelMovementPos > 15   ) {
				currFakeLevelMovementPos = 0;
			}
		} 

		drawLevel(0,90);
		if( ( gameCounter % 10 ) < 5 ) {
			bufferGraphics.setColor( Color.black );
			bufferGraphics.drawString( "Press SPACE to start", 140, 370 );
		}

		// Static things here ...
		bufferGraphics.drawImage( logoImage, 72, 20, this );
		drawExplanation();
	}
	
	public void drawLevelAlert() {
	
		int totalRows = levelAlert.length+1;
		int startY = ((Level.MAX_LEVEL_HEIGHT*IMAGE_OBJECT_HEIGHT)/2)-((totalRows*10+20)/2);
		int totalHeight = totalRows*10+20;
		
		bufferGraphics.setColor( Color.white );
		bufferGraphics.fillRect(0,startY,Level.MAX_LEVEL_WIDTH*IMAGE_OBJECT_WIDTH,totalHeight);
		bufferGraphics.setColor( Color.black );
		
		int i = 0;
		for( i = 0; i < totalRows-1; i++ ) {
			bufferGraphics.drawString( levelAlert[i], 130, 20+startY+(10*i) );
		}
		bufferGraphics.drawString( "Press SPACE to start", 130, 20+startY+(10*i) );
	
	}
	
	public void drawExplanation( ) {
		
		if( ( gameCounter % 2 ) == 0 ) {
			currExplScroller+=1;
			if( currExplScroller == 185 ) { // Total height of all text
				currExplScroller = -90;	// Height of the scrolling area + 10
			}
		}
		
		bufferGraphics.setColor( Color.black );
		drawString( "Push every box in the target space, you can only", 30, 270-currExplScroller, 270, 350 );
		drawString( "push one box at a time.", 30, 280-currExplScroller, 270, 350 );
		//----
		drawString( "Use your arrow keys to move the man, use 'N' to go to", 30, 295-currExplScroller, 270, 350 );
		drawString( "the next level, use 'P' to go to the previous level, use", 30, 305-currExplScroller, 270, 350 );
		drawString( "'R' to restart the current level and use 'A' (for Abort)", 30, 315-currExplScroller, 270, 350 );
		drawString( "to go back to this screen. If you then like to start", 30, 325-currExplScroller, 270, 350 );
		drawString( "again, Soaked will start the level you left.", 30, 335-currExplScroller, 270, 350 );
		//----
		drawString( "Please mail thoughts and suggestions to", 30, 350-currExplScroller, 270, 350 );
		drawString( "soaked@lemon8.org. Have a nice game!", 30, 360-currExplScroller, 270, 350 );
		//----
		drawString( "Soaked currently uses the "+levelPack+" level library, which", 30, 375-currExplScroller, 270, 350 );
		drawString( "contains "+levelLoader.getNrOfLevels()+" levels.", 30, 385-currExplScroller, 270, 350 );
		//----
		drawString( "Soaked v"+SOAKED_VERSION+", built on "+SOAKED_BUILD_DATE_TIME, 30, 400-currExplScroller, 270, 350 );
		drawString( "This game is actually a remake of the game 'Soaked' I made" , 30, 410-currExplScroller, 270, 350 );
		drawString( "over two years ago, of which I lost all original data and source." , 30, 420-currExplScroller, 270, 350 );
		drawString( "My hard lesson learned: Backup your data more than once!" , 30, 430-currExplScroller, 270, 350 );
		//----
		drawString( "Thanks for reading this all, please drop me a email if" , 30, 445-currExplScroller, 270, 350 );
		drawString( "you like the game or have any questions about it." , 30, 455-currExplScroller, 270, 350 );
		
		bufferGraphics.setColor(Color.white);
		bufferGraphics.fillRect(0,255,400,15);

		bufferGraphics.fillRect(0,340,400,15);
	
	}

	public void drawString( String text, int xPos, int yPos, int topYBoundary, int bottomYBoundary ) {
		if( yPos >= topYBoundary && yPos <= bottomYBoundary ) {
			bufferGraphics.drawString( text, xPos, yPos );
		}
	}
	
	public void drawLevel( int startXPos, int startYPos ) {

		bufferGraphics.setColor(Color.white);
		bufferGraphics.fillRect(0,0,Level.MAX_LEVEL_WIDTH*IMAGE_OBJECT_WIDTH,Level.MAX_LEVEL_HEIGHT*IMAGE_OBJECT_HEIGHT);
		int levelWidth = level.getTotalWidth();
		int levelHeight = level.getTotalHeight();
		int x, y;
		int startX = startXPos;
		int startY = startYPos;

		if( startX == 0 ) {
			startX = ((Level.MAX_LEVEL_WIDTH*IMAGE_OBJECT_WIDTH)-(levelWidth * IMAGE_OBJECT_WIDTH))/2;
		}
		if( startY == 0 ) {
			startY = ((Level.MAX_LEVEL_HEIGHT*IMAGE_OBJECT_HEIGHT)-(levelHeight * IMAGE_OBJECT_HEIGHT))/2;
		}
		
		for( y = 0; y < levelHeight; y++ ) {
			int firstWall = -1;
			int lastWall = -1;
			
			for( x = 0; x < levelWidth; x++ ) {
				if( level.hasWallOnPosition( x, y ) ) {
					if( firstWall == -1 ) {
						firstWall = x;
					}
					lastWall = x;
				}
			}
			for( x = 0; x < levelWidth; x++ ) {
				if( level.hasBoxOnPosition( x, y ) && level.hasTargetOnPosition( x, y ) ) {
					bufferGraphics.drawImage( imageObjects[OBJECT_BOX_ON_TARGET], startX+(x*IMAGE_OBJECT_WIDTH), startY+(y*IMAGE_OBJECT_HEIGHT), this );
				} else if( level.hasBoxOnPosition( x, y ) ) {
					bufferGraphics.drawImage( imageObjects[OBJECT_BOX], startX+x*IMAGE_OBJECT_WIDTH, startY+y*IMAGE_OBJECT_HEIGHT, this );
				} else if( level.hasTargetOnPosition( x, y ) ) {
					if( level.hasManOnPosition( x, y ) ) {
						bufferGraphics.drawImage( imageObjects[OBJECT_MAN+level.getLastMove()], startX+x*IMAGE_OBJECT_WIDTH, startY+y*IMAGE_OBJECT_HEIGHT, this );
					} else {
						bufferGraphics.drawImage( imageObjects[OBJECT_TARGET], startX+x*IMAGE_OBJECT_WIDTH, startY+y*IMAGE_OBJECT_HEIGHT, this );
					}
				} else if( level.hasWallOnPosition( x, y ) ) {
					bufferGraphics.drawImage( imageObjects[OBJECT_WALL], startX+(x*IMAGE_OBJECT_WIDTH), startY+(y*IMAGE_OBJECT_HEIGHT), this );
				} else {
					if( level.hasManOnPosition( x, y ) ) {
						bufferGraphics.drawImage( imageObjects[OBJECT_MAN+level.getLastMove()], startX+x*IMAGE_OBJECT_WIDTH, startY+y*IMAGE_OBJECT_HEIGHT, this );
					} else {
						if( level.isConfinedWithinWalls( x, y ) == true ) {
							bufferGraphics.drawImage( imageObjects[OBJECT_FLOOR], startX+x*IMAGE_OBJECT_WIDTH, startY+y*IMAGE_OBJECT_HEIGHT, this );
						}
					}
				}
			}
		}				
	}
	
	public void initFakeLevel( ) {
		fakeLevel.setGrid( 2, 0, Level.OBJECT_WALL );
		fakeLevel.setGrid( 3, 0, Level.OBJECT_WALL );
		fakeLevel.setGrid( 4, 0, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 5, 0, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 0, 1, Level.OBJECT_WALL );
		fakeLevel.setGrid( 1, 1, Level.OBJECT_WALL );
		fakeLevel.setGrid( 2, 1, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 5, 1, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 0, 2, Level.OBJECT_WALL );
		fakeLevel.setGrid( 5, 2, Level.OBJECT_WALL );
		fakeLevel.setGrid( 6, 2, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 0, 3, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 3, 3, Level.OBJECT_WALL );
		fakeLevel.setGrid( 6, 3, Level.OBJECT_WALL );
		fakeLevel.setGrid( 0, 4, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 1, 4, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 6, 4, Level.OBJECT_WALL );
		fakeLevel.setGrid( 1, 5, Level.OBJECT_WALL );
		fakeLevel.setGrid( 4, 5, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 5, 5, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 6, 5, Level.OBJECT_WALL );
		fakeLevel.setGrid( 1, 6, Level.OBJECT_WALL );
		fakeLevel.setGrid( 2, 6, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 3, 6, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 4, 6, Level.OBJECT_WALL );		
		fakeLevel.setGrid( 4, 3, Level.OBJECT_TARGET );		
		fakeLevel.setGrid( 4, 4, Level.OBJECT_BOX );
		fakeLevel.setXPosition(5);
		fakeLevel.setYPosition(4);		
	}
	
}

