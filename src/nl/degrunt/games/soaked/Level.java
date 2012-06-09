package nl.degrunt.games.soaked;

import java.util.*;

public class Level {

	public final static int MAX_LEVEL_WIDTH			= 20;
	public final static int MAX_LEVEL_HEIGHT  		= 20;
	
	public final static byte OBJECT_WALL				= 1;
	public final static byte OBJECT_BOX		 			= 2;
	public final static byte OBJECT_TARGET	 			= 4;

	public final static int  MOVE_UP						= 0;
	public final static int  MOVE_RIGHT					= 1;
	public final static int  MOVE_DOWN					= 2;
	public final static int  MOVE_LEFT					= 3;

	public final static int  MOVE_UP_PUSH				= 4;
	public final static int  MOVE_RIGHT_PUSH			= 5;
	public final static int  MOVE_DOWN_PUSH			= 6;
	public final static int  MOVE_LEFT_PUSH			= 7;

	public final static int  MOVE_UP_TARGET			= 8;
	public final static int  MOVE_RIGHT_TARGET		= 9;
	public final static int  MOVE_DOWN_TARGET			= 10;
	public final static int  MOVE_LEFT_TARGET			= 11;

	public final static int  MOVE_UP_PUSH_TARGET		= 12;
	public final static int  MOVE_RIGHT_PUSH_TARGET	= 13;
	public final static int  MOVE_DOWN_PUSH_TARGET	= 14;
	public final static int  MOVE_LEFT_PUSH_TARGET	= 15;

	private int lastMove = MOVE_UP;
	
	private int xPos = -1;
	private int yPos = -1;
	
	private byte[][] grid;
	private String comment;
	private String author;
	private String title;
	
	public Level() {
		int x, y;
		grid = new byte[MAX_LEVEL_WIDTH][MAX_LEVEL_HEIGHT];
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				grid[x][y] = 0;
			}
		}
	}

	public Level( Level otherLevel ) {
		int x, y;

		this.title = otherLevel.getTitle();
		this.author = otherLevel.getAuthor();
		this.comment = otherLevel.getComment();
		setXPosition( otherLevel.getXPosition() );
		setYPosition( otherLevel.getYPosition() );

		grid = new byte[MAX_LEVEL_WIDTH][MAX_LEVEL_HEIGHT];
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				grid[x][y] = otherLevel.getGrid(x,y);
			}
		}
		
	}
	
	public void setTitle( String aTitle ) {
		this.title = aTitle;
	}
	
	public void setAuthor( String anAuthor ) {
		this.author = anAuthor;
	}

	public void setComment( String aComment ) {
		this.comment = aComment;
	}

	public String getTitle( ) {
		return( title );
	}
	
	public String getAuthor( ) {
		return( author );
	}

	public String getComment( ) {
		return( comment );
	}
	
	public void setGrid( int x, int y, byte cell ) {
		grid[x][y] = cell;
	}
	
	public byte getGrid( int x, int y ) {
		return( grid[x][y] );
	}
	
	public void setXPosition( int x ) {
		xPos = x;
	}
	
	public void setYPosition( int y ) {
		yPos = y;
	}
	
	public int getXPosition( ) {
		return( xPos );
	}
	
	public int getYPosition( ) {
		return( yPos );
	}

	public boolean moveUp( ) {
		boolean done = false;
		int currYPos, currXPos;
		currYPos = getYPosition();
		currXPos = getXPosition(); 
		lastMove = MOVE_UP;
		if( currYPos - 1 >= 0 ) {
			if( !hasWallOnPosition( currXPos, currYPos - 1 ) ) {
				if( hasBoxOnPosition( currXPos, currYPos - 1 ) ) {
					// We might need to move the box!
					if( !hasBoxOnPosition( currXPos, currYPos - 2 ) && !hasWallOnPosition( currXPos, currYPos - 2 ) ) {
						// First remove the box!
						if( !hasTargetOnPosition( currXPos, currYPos - 1 ) ) {
							setGrid( currXPos, currYPos - 1, (byte)0 );
							lastMove = MOVE_UP_PUSH;
						} else {
							setGrid( currXPos, currYPos - 1, Level.OBJECT_TARGET );
							lastMove = MOVE_UP_PUSH_TARGET;
						}

						// Second put the box on!
						if( !hasTargetOnPosition( currXPos, currYPos - 2 ) ) {
							setGrid( currXPos, currYPos - 2, Level.OBJECT_BOX );	
						} else {
							setGrid( currXPos, currYPos - 2, (byte)(Level.OBJECT_BOX|Level.OBJECT_TARGET) );
						}
						setYPosition( currYPos - 1 );
						done = true;
					} else {
						// We cannot move the box
						if( hasTargetOnPosition( currXPos, currYPos ) ) {
							lastMove = MOVE_UP_TARGET;
						}
					}
				} else {
					// Just move
					if( hasTargetOnPosition( currXPos, currYPos - 1 ) ) {
						lastMove = MOVE_UP_TARGET;
					}
					setYPosition( currYPos - 1 );
					done = true;	
				}
			} else {
				if( hasTargetOnPosition( currXPos, currYPos ) ) {
					lastMove = MOVE_UP_TARGET;
				}
			}
		}
		return( done );
	}

	public boolean moveDown( ) {
		boolean done = false;
		int currYPos, currXPos;
		currYPos = getYPosition();
		currXPos = getXPosition();
		lastMove = MOVE_DOWN;
		if( currYPos + 1 < Level.MAX_LEVEL_HEIGHT ) {
			if( !hasWallOnPosition( currXPos, currYPos + 1 ) ) {
				if( hasBoxOnPosition( currXPos, currYPos + 1 ) ) {
					// We might need to move the box!
					if( !hasBoxOnPosition( currXPos, currYPos + 2 ) && !hasWallOnPosition( currXPos, currYPos + 2 ) ) {
						// First remove the box!
						if( !hasTargetOnPosition( currXPos, currYPos + 1 ) ) {
							setGrid( currXPos, currYPos + 1, (byte)0 );
							lastMove = MOVE_DOWN_PUSH;
						} else {
							setGrid( currXPos, currYPos + 1, Level.OBJECT_TARGET );
							lastMove = MOVE_DOWN_PUSH_TARGET;
						}

						// Second put the box on!
						if( !hasTargetOnPosition( currXPos, currYPos + 2 ) ) {
							setGrid( currXPos, currYPos + 2, Level.OBJECT_BOX );	
						} else {
							setGrid( currXPos, currYPos + 2, (byte)(Level.OBJECT_BOX|Level.OBJECT_TARGET) );
						}
						setYPosition( currYPos + 1 );
						done = true;
					} else {
						// We cannot move the box
						if( hasTargetOnPosition( currXPos, currYPos ) ) {
							lastMove = MOVE_DOWN_TARGET;
						}
					}
				} else {
					// Just move
					if( hasTargetOnPosition( currXPos, currYPos + 1 ) ) {
						lastMove = MOVE_DOWN_TARGET;
					}
					setYPosition( currYPos + 1 );
					done = true;	
				}
			} else {
				if( hasTargetOnPosition( currXPos, currYPos ) ) {
					lastMove = MOVE_DOWN_TARGET;
				}
			}
		}
		return( done );
	}

	public boolean moveLeft( ) {
		boolean done = false;
		int currYPos, currXPos;
		currYPos = getYPosition();
		currXPos = getXPosition();
		lastMove = MOVE_LEFT;
		if( currXPos - 1 >= 0 ) {
			if( !hasWallOnPosition( currXPos - 1, currYPos ) ) {
				if( hasBoxOnPosition( currXPos - 1, currYPos ) ) {
					// We might need to move the box!
					if( !hasBoxOnPosition( currXPos - 2, currYPos ) && !hasWallOnPosition( currXPos - 2, currYPos ) ) {
						// First remove the box!
						if( !hasTargetOnPosition( currXPos - 1, currYPos ) ) {
							setGrid( currXPos - 1, currYPos, (byte)0 );
							lastMove = MOVE_LEFT_PUSH;
						} else {
							setGrid( currXPos - 1, currYPos, Level.OBJECT_TARGET );
							lastMove = MOVE_LEFT_PUSH_TARGET;
						}

						// Second put the box on!
						if( !hasTargetOnPosition( currXPos - 2, currYPos ) ) {
							setGrid( currXPos - 2, currYPos, Level.OBJECT_BOX );	
						} else {
							setGrid( currXPos - 2, currYPos, (byte)(Level.OBJECT_BOX|Level.OBJECT_TARGET) );
						}
						setXPosition( currXPos - 1 );
						done = true;
					} else {
						// We cannot move the box
						if( hasTargetOnPosition( currXPos, currYPos ) ) {
							lastMove = MOVE_LEFT_TARGET;
						}
					}
				} else {
					// Just move
					if( hasTargetOnPosition( currXPos - 1, currYPos ) ) {
						lastMove = MOVE_LEFT_TARGET;
					}
					setXPosition( currXPos - 1 );
					done = true;	
				}
			} else {
				if( hasTargetOnPosition( currXPos, currYPos ) ) {
					lastMove = MOVE_LEFT_TARGET;
				}
			}
		}
		return( done );
	}

	public boolean moveRight( ) {
		boolean done = false;
		int currYPos, currXPos;
		currYPos = getYPosition();
		currXPos = getXPosition();
		lastMove = MOVE_RIGHT;
		if( currXPos + 1 < MAX_LEVEL_WIDTH ) {
			if( !hasWallOnPosition( currXPos + 1, currYPos ) ) {
				if( hasBoxOnPosition( currXPos + 1, currYPos ) ) {
					// We might need to move the box!
					if( !hasBoxOnPosition( currXPos + 2, currYPos ) && !hasWallOnPosition( currXPos + 2, currYPos ) ) {
						// First remove the box!
						if( !hasTargetOnPosition( currXPos + 1, currYPos ) ) {
							setGrid( currXPos + 1, currYPos, (byte)0 );
							lastMove = MOVE_RIGHT_PUSH;
						} else {
							setGrid( currXPos + 1, currYPos, Level.OBJECT_TARGET );
							lastMove = MOVE_RIGHT_PUSH_TARGET;
						}

						// Second put the box on!
						if( !hasTargetOnPosition( currXPos + 2, currYPos ) ) {
							setGrid( currXPos + 2, currYPos, Level.OBJECT_BOX );	
						} else {
							setGrid( currXPos + 2, currYPos, (byte)(Level.OBJECT_BOX|Level.OBJECT_TARGET) );
						}
						setXPosition( currXPos + 1 );
						done = true;
					} else {
						// We cannot move the box
						if( hasTargetOnPosition( currXPos, currYPos ) ) {
							lastMove = MOVE_RIGHT_TARGET;
						}
					}
				} else {
					// Just move
					if( hasTargetOnPosition( currXPos + 1, currYPos ) ) {
						lastMove = MOVE_RIGHT_TARGET;
					}
					setXPosition( currXPos + 1 );
					done = true;	
				}
			} else {
				if( hasTargetOnPosition( currXPos, currYPos ) ) {
					lastMove = MOVE_RIGHT_TARGET;
				}
			}
		}
		return( done );
	}
	
	public boolean hasWallOnPosition( int x, int y ) {
		return( ( grid[x][y] & OBJECT_WALL ) > 0 );
	}

	public boolean hasTargetOnPosition( int x, int y ) {
		return( ( grid[x][y] & OBJECT_TARGET ) > 0 );
	}

	public boolean hasBoxOnPosition( int x, int y ) {
		return( ( grid[x][y] & OBJECT_BOX ) > 0 );
	}
	
	public boolean hasManOnPosition( int x, int y ) {
		return( x == xPos && y == yPos );
	}

	public int getLastMove( ) {
		return( lastMove );
	}
	
	public String toString( ) {
		int x,y;
		String result = "";
		String resultLine;
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			resultLine = "";
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				if( hasBoxOnPosition( x, y ) && hasTargetOnPosition( x, y ) ) {
					resultLine += "*";
				} else if( hasBoxOnPosition( x, y ) ) {
					resultLine += "$";
				} else if( hasTargetOnPosition( x, y ) ) {
					if( hasManOnPosition( x, y ) ) {
						resultLine += "+";
					} else {
						resultLine += ".";
					}
				} else if( hasWallOnPosition( x, y ) ) {
					resultLine += "#";
				} else {
					if( hasManOnPosition( x, y ) ) {
						resultLine += "@";
					} else {
						resultLine += " ";
					}
				}
			}
			if( resultLine.trim().length() != 0 ) {
				result += resultLine+"\n";
			}
		}
		
		return( result );
	}
	
	public boolean isCompleted() {
		boolean completed = true;
		int x, y;
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				if( hasTargetOnPosition( x, y ) && !hasBoxOnPosition( x, y ) ) {
					completed = false;
				}
			}
		}
		return( completed );
	}

	public int getTotalWidth() {
		int maxWidth = 0;
		int x, y;
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				if( getGrid(x,y) != (byte)0 && x > maxWidth ) {
					maxWidth = x;
				}
			}
		}
		return( maxWidth+1 );
	}

	public int getTotalHeight() {
		int maxHeight = 0;
		int x, y;
		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
				if( getGrid(x,y) != (byte)0 && y > maxHeight ) {
					maxHeight = y;
				}
			}
		}
		return( maxHeight+1 );
	}

	public boolean isConfinedWithinWalls( int xPos, int yPos ) {

		int x, y;
		
		int firstXWall = -1;
		int lastXWall = -1;
		int firstYWall = -1;
		int lastYWall = -1;
		
		for( x = 0; x < MAX_LEVEL_WIDTH; x++ ) {
			if( hasWallOnPosition( x, yPos ) ) {
				if( firstXWall == -1 ) {
					firstXWall = x;
				}
				lastXWall = x;
			}
		}

		for( y = 0; y < MAX_LEVEL_HEIGHT; y++ ) {
			if( hasWallOnPosition( xPos, y ) ) {
				if( firstYWall == -1 ) {
					firstYWall = y;
				}
				lastYWall = y;
			}
		}
		
		boolean result = false;
		if( xPos >= firstXWall && xPos <= lastXWall && yPos >= firstYWall && yPos <= lastYWall ) {
			result = true;
		}
		
		return( result );
	}
	
}
