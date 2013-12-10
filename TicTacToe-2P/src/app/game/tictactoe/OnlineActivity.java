package app.game.tictactoe;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.HorizontalAlign;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import app.game.tictactoe.gameplay.IconsController;
import app.game.tictactoe.gameplay.PositionController;
import app.game.tictactoe.util.AlertDialogFragment;
import app.game.tictactoe.util.BoardTouchPoint;
import app.game.tictactoe.util.Utils;
import br.pucrs.tcii.SessionManager;
import br.pucrs.tcii.activities.DeviceListActivity;
import br.pucrs.tcii.exceptions.BluetoothNotConnectedExeption;
import br.pucrs.tcii.exceptions.BluetoothNotSupportedException;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.models.GameControl;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.What;

public class OnlineActivity extends BaseGameActivity
{
	private static final int CAMERA_WIDTH = 320;
	private static final int CAMERA_HEIGHT = 480;

	public static final int ICON_LENGTH = 53;
	public static final int BOARD_WIDTH = 240;
	public static final int BOARD_HEIGHT = 240;

	protected static final String TAG = OnlineActivity.class.getName();

	private BitmapTextureAtlas bgTexture;
	private BitmapTextureAtlas boardTexture;
	private ITextureRegion bgTextureRegion;
	private ITextureRegion boardTextureRegion;

	private BitmapTextureAtlas crossTexture;
	private ITextureRegion crossTextureRegion;
	private BitmapTextureAtlas circleTexture;
	private ITextureRegion circleTextureRegion;

	private final Sprite[] crossSprites = new Sprite[5];
	private final Sprite[] circleSprites = new Sprite[5];

	private Scene boardScene;
	private Camera boardCamera;

	private AlertDialogFragment endGameDialog;

	private Bundle boardBundle;

	private final IconsController iconsController = IconsController.getInstance();
	private final PositionController positionController = PositionController.getInstance();

	/** Handler for BT messages */
	private BluetoothHandler handler;
	private GameControl gc;

	private int lastTouchX, lastTouchY;
	/** It's my turn to play? */
	boolean isMyTurn;
	private BitmapTextureAtlas butTexture;
	private TextureRegion butTextureRegion;

	private Font fontRealtime;
	private Font fontPing;
	private Text textRealtime;
	private Text textPing;

	private int pingCount;

	@Override
	public EngineOptions onCreateEngineOptions()
	{
		this.boardCamera = new Camera(0, 0, OnlineActivity.CAMERA_WIDTH, OnlineActivity.CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(OnlineActivity.CAMERA_WIDTH, OnlineActivity.CAMERA_HEIGHT), this.boardCamera);

		try
		{
			this.configBluetooth();
		}
		catch (final RemoteException e)
		{
			Log.e(TAG, "Error: ", e);
		}
		catch (final BluetoothNotConnectedExeption e)
		{
			Log.e(TAG, "Error: ", e);
		}

		this.pingCount = 0;

		return engineOptions;
	}

	private void configBluetooth() throws RemoteException, BluetoothNotConnectedExeption
	{
		this.gc = new GameControl()
		{
			@Override
			public Object prepareTurn()
			{
				return new BoardTouchPoint(OnlineActivity.this.lastTouchX, OnlineActivity.this.lastTouchY);
			}

			public Object prepareRealtime()
			{
				return System.currentTimeMillis();
			}
		};

		this.handler = new BluetoothHandler()
		{

			public void onREAD_DATA(BluetoothState state, Message msg)
			{
				pingCount += 1;

				final String pingStr = "Ping #" + String.valueOf(pingCount);
				textPing.setText(pingStr);
				textPing.setX((float) (textPing.getWidth() / 3.8));
				textPing.setY((float) (textPing.getHeight() * 1.3));
			}

			@Override
			public void onREAD_TURN(final BluetoothState state, final Message msg)
			{
				// Receive other player touch and execute the game turn
				final BoardTouchPoint points = SessionManager.getInstance().processHandlerMessage(state, msg, BoardTouchPoint.class);

				final int posX = OnlineActivity.this.positionController.checkBoardCollumn(points.touchX);
				final int posY = OnlineActivity.this.positionController.checkBoardLine(points.touchY);

				OnlineActivity.this.iconsController.nextTurn(posX, posY);
				OnlineActivity.this.updateIcons();

				if (OnlineActivity.this.iconsController.currentIcon != IconsController.NONE)
				{
					OnlineActivity.this.iconsController.endTurn();
					OnlineActivity.this.updateGameState();
				}

				// It's my turn now
				OnlineActivity.this.isMyTurn |= true;
			}

			@Override
			public void onREAD_REAL_TIME(final BluetoothState state, final Message msg)
			{
				long timeInMillis = SessionManager.getInstance().processHandlerMessage(state, msg, Long.class);

				final String dateStr = "Opponent time: " + String.valueOf(timeInMillis);
				textRealtime.setText(dateStr);
				textRealtime.setX(CAMERA_WIDTH / 2 - textRealtime.getWidth() / 2);
				textRealtime.setY(CAMERA_HEIGHT - textRealtime.getHeight() * 3);
			}

			@Override
			public void onWRITE_TURN(final BluetoothState state, final Message msg)
			{
				// Sent my turn, it's not my turn anymore
				OnlineActivity.this.isMyTurn &= false;
			}


			@Override
			public void onCONNECTED_TO(BluetoothState state, Message msg, String deviceAddress, float connectionTime) {
				Toast.makeText(OnlineActivity.this.getApplicationContext(), "Connected to " + deviceAddress, Toast.LENGTH_SHORT).show();
			}
		};

		try
		{
			final int N_PLAYERS = 2;
			SessionManager.initialize(this.handler, Utils.NAME_SECURE, N_PLAYERS);
			if (!SessionManager.getInstance().isBluetoothEnabled())
			{
				SessionManager.getInstance().enableBluetooth(this);
			}
		}
		catch (final BluetoothNotSupportedException e)
		{
			Toast.makeText(OnlineActivity.this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
		}

		SessionManager.getInstance().startSession();

		SessionManager.getInstance().setGameControl(gc);
		final long REALTIME_DELAY = 100;
		SessionManager.getInstance().executeRealtime(REALTIME_DELAY);

		final Bundle bundle = getIntent().getExtras();

		if (getIntent().getStringExtra("mode").equals("create"))
		{
			SessionManager.getInstance().makeDiscoverable(this);
			this.isMyTurn = true;
		}
		else if (bundle.getCharSequence("mode").equals("join"))
		{
			this.startActivityForResult(new Intent(this, DeviceListActivity.class), What.REQUEST_CONNECT_DEVICE.v());
			this.isMyTurn = false;
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		switch (SessionManager.getInstance().processActivityResult(requestCode, resultCode, data)) {
		case CONNECT_OK:
			break;
		case CONNECT_CANCELLED:
			break;
		case ENABLE_BT_OK:
			break;
		case ENABLE_BT_CANCELLED:
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreateResources(final OnCreateResourcesCallback pOnCreateResourcesCallback)
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.bgTexture = new BitmapTextureAtlas(this.getTextureManager(), 320, 480, TextureOptions.DEFAULT);
		this.bgTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.bgTexture, this, "bg1.png", 0, 0);
		this.bgTexture.load();

		this.boardTexture = new BitmapTextureAtlas(this.getTextureManager(), OnlineActivity.BOARD_WIDTH, OnlineActivity.BOARD_HEIGHT, TextureOptions.DEFAULT);
		this.boardTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.boardTexture, this, "board1.png", 0, 0);
		this.boardTexture.load();

		this.crossTexture = new BitmapTextureAtlas(this.getTextureManager(), 53, 53, TextureOptions.DEFAULT);
		this.crossTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.crossTexture, this, "cross.png", 0, 0);
		this.crossTexture.load();
		this.circleTexture = new BitmapTextureAtlas(this.getTextureManager(), 53, 53, TextureOptions.DEFAULT);
		this.circleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.circleTexture, this, "circle.png", 0, 0);
		this.circleTexture.load();

		this.butTexture = new BitmapTextureAtlas(this.getTextureManager(), 500, 402, TextureOptions.DEFAULT);
		this.butTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.butTexture, this, "but.png", 0, 0);
		this.butTexture.load();

		Sprite mSprite;
		for (int i = 0; i < 5; i++)
		{
			mSprite = new Sprite(0, 0, this.crossTextureRegion, this.getVertexBufferObjectManager());
			this.crossSprites[i] = mSprite;
			this.crossSprites[i].setVisible(false);
			mSprite = new Sprite(0, 0, this.circleTextureRegion, this.getVertexBufferObjectManager());
			this.circleSprites[i] = mSprite;
			this.circleSprites[i].setVisible(false);
		}

		this.positionController.calculateArea(OnlineActivity.CAMERA_WIDTH, OnlineActivity.CAMERA_HEIGHT);

		fontRealtime = FontFactory.create(getFontManager(), getTextureManager(), 128, 128, Typeface.SANS_SERIF, 16);
		fontRealtime.load();

		fontPing = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, Typeface.SANS_SERIF, 32);
		fontPing.load();

		this.boardBundle = new Bundle();
		this.boardBundle.putCharSequence("positive", "Restart");
		this.boardBundle.putCharSequence("negative", "Exit");

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(final OnCreateSceneCallback pOnCreateSceneCallback)
	{
		this.boardScene = new Scene();
		this.restartGame();
		pOnCreateSceneCallback.onCreateSceneFinished(this.boardScene);
	}

	@Override
	public void onPopulateScene(final Scene pScene, final OnPopulateSceneCallback pOnPopulateSceneCallback)
	{
		final Sprite bgSprite = new Sprite(0, 0, this.bgTextureRegion, this.getVertexBufferObjectManager())
		{
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
			{
				// DRAW SPRITES ON TOUCH
				if (pSceneTouchEvent.isActionDown())
				{
					// If game has both players and it's my turn, register my
					// touch
					if (SessionManager.getInstance().isConnected() && OnlineActivity.this.isMyTurn)
					{
						final int posX = OnlineActivity.this.positionController.checkBoardCollumn((int) pTouchAreaLocalX);
						final int posY = OnlineActivity.this.positionController.checkBoardLine((int) pTouchAreaLocalY);
						OnlineActivity.this.lastTouchX = (int) pTouchAreaLocalX;
						OnlineActivity.this.lastTouchY = (int) pTouchAreaLocalY;

						if (posX != PositionController.NONE && posY != PositionController.NONE)
						{
							OnlineActivity.this.iconsController.nextTurn(posX, posY);
							OnlineActivity.this.updateIcons();

							if (OnlineActivity.this.iconsController.currentIcon != IconsController.NONE)
							{
								OnlineActivity.this.iconsController.endTurn();
								OnlineActivity.this.updateGameState();

								try
								{
									SessionManager.getInstance().executeTurn();
								}
								catch (final BluetoothNotConnectedExeption e)
								{
									Log.e(TAG, "Error: ", e);
								}
							}
						}
					}
				}
				return true;
			}
		};

		final Sprite boardSprite = new Sprite(OnlineActivity.CAMERA_WIDTH / 2 - OnlineActivity.BOARD_WIDTH / 2, OnlineActivity.CAMERA_HEIGHT / 2 - OnlineActivity.BOARD_HEIGHT / 2, this.boardTextureRegion, this.getVertexBufferObjectManager());

		final Sprite butSprite = new Sprite((float) (bgSprite.getWidth() - butTextureRegion.getWidth() * 1.3), butTextureRegion.getHeight() / 4, butTextureRegion, getVertexBufferObjectManager())
		{
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
			{
				if (pSceneTouchEvent.isActionDown())
				{
					try
					{
						SessionManager.getInstance().sendData("Hi");
					}
					catch (final BluetoothNotConnectedExeption e)
					{
						Log.e(TAG, "Error: ", e);
					}
				}
				return true;
			}
		};

		final ParallaxBackground boardBackground = new ParallaxBackground(0, 0, 0);
		boardBackground.attachParallaxEntity(new ParallaxEntity(0, bgSprite));

		final int MAX_CHAR = 512;
		textPing = new Text(0, 0, fontPing, "", MAX_CHAR, new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		textRealtime = new Text(0, 0, fontRealtime, "", MAX_CHAR, new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());

		boardScene.attachChild(boardSprite);
		boardScene.attachChild(butSprite);
		boardScene.attachChild(textRealtime);
		boardScene.attachChild(textPing);

		this.boardScene.setBackground(boardBackground);
		this.boardScene.registerTouchArea(butSprite);
		this.boardScene.registerTouchArea(bgSprite);

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	protected void updateIcons()
	{
		if (this.iconsController.currentIcon == IconsController.CROSS)
		{
			this.crossSprites[this.iconsController.crossIndex].setX(PositionController.currentX);
			this.crossSprites[this.iconsController.crossIndex].setY(PositionController.currentY);

			this.crossSprites[this.iconsController.crossIndex].setVisible(true);

			if (!this.crossSprites[this.iconsController.crossIndex].hasParent())
			{
				this.boardScene.attachChild(this.crossSprites[this.iconsController.crossIndex]);
			}
		}
		else if (this.iconsController.currentIcon == IconsController.CIRCLE)
		{
			this.circleSprites[this.iconsController.circleIndex].setX(PositionController.currentX);
			this.circleSprites[this.iconsController.circleIndex].setY(PositionController.currentY);

			this.circleSprites[this.iconsController.circleIndex].setVisible(true);

			if (!this.circleSprites[this.iconsController.circleIndex].hasParent())
			{
				this.boardScene.attachChild(this.circleSprites[this.iconsController.circleIndex]);
			}
		}
	}

	@SuppressLint("NewApi")
	private void updateGameState()
	{
		boolean restart = false;

		if (this.iconsController.currentState == IconsController.CROSS_WIN)
		{
			this.boardBundle.putCharSequence("state", "CROSS Victory!");
			this.endGameDialog = AlertDialogFragment.newInstance(this.boardBundle);
			restart = true;
		}
		else if (this.iconsController.currentState == IconsController.CIRCLE_WIN)
		{
			this.boardBundle.putCharSequence("state", "CIRCLE Victory!");
			this.endGameDialog = AlertDialogFragment.newInstance(this.boardBundle);
			restart = true;
		}
		else if (this.iconsController.currentState == IconsController.DRAW)
		{
			this.boardBundle.putCharSequence("state", "DRAW!!");
			this.endGameDialog = AlertDialogFragment.newInstance(this.boardBundle);
			restart = true;
		}

		if (restart)
		{
			this.endGameDialog.show(this.getFragmentManager(), "end");
		}
	}

	public void restartGame()
	{
		for (int i = 0; i < 5; i++)
		{
			this.crossSprites[i].setVisible(false);
			this.circleSprites[i].setVisible(false);
		}

		this.iconsController.resetBoard();
		this.positionController.resetPositions();
	}

	public void doPositiveClick()
	{
		this.restartGame();
	}

	public void doNegativeClick()
	{
		this.finish();
	}
}
