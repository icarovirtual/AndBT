package app.game.tictactoe.gameplay;

import java.util.Arrays;

public class IconsController {
	private static final IconsController instance = new IconsController();

	public int crossIndex;
	public int circleIndex;

	public int currentIcon;
	public int currentState;

	private int freePositions;

	public static int CROSS = -1;
	public static int CIRCLE = 1;
	public static int NONE = 0;

	public static int CROSS_WIN = -10;
	public static int CIRCLE_WIN = 10;
	public static int DRAW = 5;

	private final int boardPositions[][] = new int[3][3];

	private IconsController() {
		resetBoard();
	}

	public static IconsController getInstance() {
		return instance;
	}

	public void nextTurn(final int posX, final int posY) {
		if (boardPositions[posX][posY] == NONE) {
			if (crossIndex < circleIndex && crossIndex < 5) {
				boardPositions[posX][posY] = CROSS;
				currentIcon = CROSS;
			} else if (crossIndex > circleIndex && circleIndex < 5) {
				boardPositions[posX][posY] = CIRCLE;
				currentIcon = CIRCLE;
			} else if (crossIndex == circleIndex && crossIndex < 5) {
				boardPositions[posX][posY] = CROSS;
				currentIcon = CROSS;
			} else {
				currentIcon = NONE;
			}
		} else {
			currentIcon = NONE;
		}
	}

	public void endTurn() {
		freePositions--;

		checkBoardState();

		if (currentIcon == CROSS) {
			crossIndex++;
		} else if (currentIcon == CIRCLE) {
			circleIndex++;
		}
	}

	private void checkBoardState() {
		boolean winState = false;
		int boardIcon = NONE;

		// TEST WIN STATE
		if (boardPositions[1][1] != NONE && !winState) {
			boardIcon = boardPositions[1][1];
			winState = checkDiagonals(1, 1, boardIcon, 1, 1);
			if (!winState) {
				winState = checkDiagonals(1, 1, boardIcon, -1, 1);
			}
			if (!winState) {
				winState = checkCross(1, 1, boardIcon, 1, 0);
			}
			if (!winState) {
				winState = checkCross(1, 1, boardIcon, 0, 1);
			}
		}
		if (boardPositions[0][0] != NONE && !winState) {
			boardIcon = boardPositions[0][0];
			winState = checkCorners(0, 1, boardIcon, 0, 1);
			if (!winState) {
				winState = checkCorners(1, 0, boardIcon, 1, 0);
			}
		}
		if (boardPositions[2][2] != NONE && !winState) {
			boardIcon = boardPositions[2][2];
			winState = checkCorners(2, 1, boardIcon, 0, -1);
			if (!winState) {
				winState = checkCorners(1, 2, boardIcon, -1, 0);
			}
		}

		if (winState) {
			if (boardIcon == CROSS) {
				currentState = CROSS_WIN;
			} else if (boardIcon == CIRCLE) {
				currentState = CIRCLE_WIN;
			}
		}
		// TEST DRAW STATE
		else if (freePositions == 0) {
			currentState = DRAW;
		}
	}

	/**
	 * CHECK THESE RESULTS: </br> x | | x | x | x | | x | | </br> x | | | | | |
	 * x | | </br> x | | | | | | x x | x | x </br>
	 **/
	private boolean checkCorners(final int posLine, final int posColumn, final int boardIcon, final int incLine, final int incColumn) {
		if (posColumn < boardPositions.length && posLine < boardPositions.length && posColumn >= 0 && posLine >= 0) {
			if (boardPositions[posLine][posColumn] == boardIcon) {
				return checkCorners(posLine + incLine, posColumn + incColumn, boardIcon, incLine, incColumn);
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * CHECK THESE RESULTS: </br> x | | | | x </br> | x | | x | </br> | | x x |
	 * | </br>
	 **/
	private boolean checkDiagonals(final int posLine, final int posColumn, final int boardIcon, final int incLine, final int incColumn) {
		boolean validTop = false;
		boolean validBottom = false;
		validTop = checkDiagonalsAux(posLine, posColumn, boardIcon, incLine, incColumn);
		validBottom = checkDiagonalsAux(posLine, posColumn, boardIcon, incLine * -1, incColumn * -1);
		return validTop && validBottom;
	}

	private boolean checkDiagonalsAux(final int posLine, final int posColumn, final int boardIcon, final int incLine, final int incColumn) {
		if (posColumn < boardPositions.length && posLine < boardPositions.length && posColumn >= 0 && posLine >= 0) {
			if (boardPositions[posLine][posColumn] == boardIcon) {
				boolean validGame = false;
				validGame = checkDiagonalsAux(posLine + incLine, posColumn + incColumn, boardIcon, incLine, incColumn);
				return validGame;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * CHECK THESE RESULTS: </br> | | | x | </br> x | x | x | x | </br> | | | x
	 * | </br>
	 **/
	private boolean checkCross(final int posLine, final int posColumn, final int boardIcon, final int incLine, final int incColumn) {
		boolean validHigh = false;
		boolean validLow = false;
		validHigh = checkCrossAux(posLine, posColumn, boardIcon, incLine, incColumn);
		validLow = checkCrossAux(posLine, posColumn, boardIcon, incLine * -1, incColumn * -1);
		return validHigh && validLow;
	}

	private boolean checkCrossAux(final int posLine, final int posColumn, final int boardIcon, final int incLine, final int incColumn) {
		if (posColumn < boardPositions.length && posLine < boardPositions.length && posColumn >= 0 && posLine >= 0) {
			if (boardPositions[posLine][posColumn] == boardIcon) {
				boolean validGame = false;
				validGame = checkCrossAux(posLine + incLine, posColumn + incColumn, boardIcon, incLine, incColumn);
				return validGame;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public void resetBoard() {
		crossIndex = 0;
		circleIndex = 0;

		currentIcon = CROSS;
		currentState = NONE;

		freePositions = 9;

		Arrays.fill(boardPositions[0], NONE);
		Arrays.fill(boardPositions[1], NONE);
		Arrays.fill(boardPositions[2], NONE);
	}
}