package app.game.tictactoe.gameplay;

import app.game.tictactoe.BoardActivity;

public class PositionController {
	private static final PositionController instance = new PositionController();

	public static final int NONE = -1;
	public static final int ONE = 0;
	public static final int TWO = 1;
	public static final int THREE = 2;

	public static int currentX;
	public static int currentY;

	private int minX;
	private int minY;
	private int maxX;
	private int maxY;

	private int xArea;
	private int yArea;

	private int xMargin;
	private int yMargin;

	private PositionController() {
	}

	public static PositionController getInstance() {
		return instance;
	}

	public int checkBoardCollumn(int pointerX) {
		if (pointerX > minX + xMargin && pointerX < minX + xArea - xMargin) {
			currentX = minX + xArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return ONE;
		} else if (pointerX > minX + xArea + xMargin && pointerX < maxX - xArea - xMargin) {
			currentX = minX + xArea + xArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return TWO;
		} else if (pointerX < maxX - xMargin && pointerX > maxX - xArea + xMargin) {
			currentX = maxX - xArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return THREE;
		}
		return NONE;
	}

	public int checkBoardLine(int pointerY) {
		if (pointerY > minY + yMargin && pointerY < minY + yArea - yMargin) {
			currentY = minY + yArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return ONE;
		} else if (pointerY > minY + yArea + yMargin / 3 && pointerY < maxY - yArea - yMargin) {
			currentY = minY + yArea + yArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return TWO;
		} else if (pointerY < maxY - yMargin && pointerY > maxY - yArea + yMargin) {
			currentY = maxY - yArea / 2 - BoardActivity.ICON_LENGTH / 2;
			return THREE;
		}
		return NONE;
	}

	public void calculateArea(int screenWidth, int screenHeight) {
		minX = screenWidth / 2 - BoardActivity.BOARD_WIDTH / 2;
		minY = screenHeight / 2 - BoardActivity.BOARD_HEIGHT / 2;

		maxX = screenWidth / 2 + BoardActivity.BOARD_WIDTH / 2;
		maxY = screenHeight / 2 + BoardActivity.BOARD_HEIGHT / 2;

		xArea = BoardActivity.BOARD_WIDTH / 3;
		yArea = BoardActivity.BOARD_HEIGHT / 3;

		xMargin = (int) (xArea * 0.20);
		yMargin = (int) (yArea * 0.20);

		currentX = -1;
		currentY = -1;
	}

	public void resetPositions() {
		currentX = -1;
		currentY = -1;
	}
}
