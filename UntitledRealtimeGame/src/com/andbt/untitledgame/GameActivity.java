package com.andbt.untitledgame;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import br.pucrs.tcii.SessionManager;
import br.pucrs.tcii.activities.DeviceListActivity;
import br.pucrs.tcii.exceptions.BluetoothNotConnectedExeption;
import br.pucrs.tcii.exceptions.BluetoothNotSupportedException;
import br.pucrs.tcii.handlers.BluetoothHandler;
import br.pucrs.tcii.models.GameControl;
import br.pucrs.tcii.utils.BluetoothState;
import br.pucrs.tcii.utils.What;

public class GameActivity extends BaseGameActivity {
	private static final int CAMERA_WIDTH = 320;
	private static final int CAMERA_HEIGHT = 480;

	public static final int ICON_W = 50;
	public static final int ICON_H = 50;

	private BitmapTextureAtlas textureBg;
	private ITextureRegion textureRegionBg;
	private Sprite spriteBg;

	private Sprite[] spritesPlayers = new Sprite[4];
	private int countPlayers = 0;
	private int myPlayer;

	private boolean isServer = false;

	private Scene sceneGame;
	private Camera cameraGame;

	private BitmapTextureAtlas textureOnScreenControl;
	private TextureRegion textureRegionOnScreenControlBase;
	private TextureRegion textureRegionOnScreenControlKnob;
	private AnalogOnScreenControl analogOnScreenControl;

	private SessionManager andbt;
	private GameControl gc;
	private BluetoothHandler bthandler;

	protected boolean started = false;;

	private float getInitX(int player) {
		switch (player) {
		case 0:
		case 2:
			return 0 + ICON_W;
		case 1:
		case 3:
		default:
			return CAMERA_WIDTH - ICON_W * 2;
		}
	}

	private float getInitY(int player) {
		switch (player) {
		case 0:
		case 1:
			return 0 + ICON_H;
		case 2:
		case 3:
		default:
			return CAMERA_HEIGHT - ICON_H * 2;
		}
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		Bundle bundle = getIntent().getExtras();
		String value = (String) bundle.getCharSequence("who");

		configAndBT();

		if (value.equals("server")) {
			andbt.makeDiscoverable(GameActivity.this);
			isServer = true;
			myPlayer = 0;
			countPlayers = 1;
		} else if (value.equals("client")) {
			startActivityForResult(new Intent(GameActivity.this, DeviceListActivity.class), What.REQUEST_CONNECT_DEVICE.v());
		}

		try {
			andbt.executeRealtime(50);
		} catch (BluetoothNotConnectedExeption e) {
			e.printStackTrace();
		}

		super.onCreate(pSavedInstanceState);
	}

	private void configAndBT() {
		gc = new GameControl() {
			@Override
			public Object prepareRealtime() {
				if (started) {
					float[] data = new float[3];

					data[0] = myPlayer;
					data[1] = spritesPlayers[myPlayer].getX();
					data[2] = spritesPlayers[myPlayer].getY();

					return data;
				}
				return null;
			}

			@Override
			public Object prepareBegin() {
				spritesPlayers[countPlayers].setVisible(true);
				countPlayers += 1;
				return countPlayers - 1;
			}
		};

		bthandler = new BluetoothHandler() {
			@Override
			public void onCONNECTED_TO(BluetoothState state, Message msg, String deviceAddress, float connectionTime) {
				if (isServer) {
					try {
						Thread.sleep(100);
						andbt.sendBegin();
					} catch (BluetoothNotConnectedExeption e) {
						// TODO Log
					} catch (InterruptedException e) {
						// TODO Log
					}
				}
			}

			public void onREAD_BEGIN(BluetoothState state, Message msg) {
				myPlayer = andbt.processHandlerMessage(state, msg, int.class);
				for (int x = 0; x <= myPlayer; x++) {
					spritesPlayers[x].setVisible(true);
				}

				final Sprite me = spritesPlayers[myPlayer];
				final PhysicsHandler physicsHandler = new PhysicsHandler(me);
				me.registerUpdateHandler(physicsHandler);

				analogOnScreenControl = new AnalogOnScreenControl(0, 0, cameraGame, textureRegionOnScreenControlBase, textureRegionOnScreenControlKnob, 0.1f, 200, getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
						physicsHandler.setVelocity(pValueX * 250, pValueY * 250);

						analogOnScreenControl.setX(me.getX() - me.getHeight());
						analogOnScreenControl.setY(me.getY() - me.getWidth());
					}

					@Override
					public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
						me.registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.25f, 1, 1.5f), new ScaleModifier(0.25f, 1.5f, 1)));
					}
				});

				analogOnScreenControl.getControlBase().setVisible(false);
				analogOnScreenControl.getControlKnob().setVisible(false);

				sceneGame.setChildScene(analogOnScreenControl);
			}

			@Override
			public void onREAD_REAL_TIME(BluetoothState state, Message msg) {
				float[] data = andbt.processHandlerMessage(state, msg, float[].class);

				int player = (int) data[0];
				spritesPlayers[player].setX(data[1]);
				spritesPlayers[player].setY(data[2]);
			}
		};

		try {
			if (!SessionManager.isInitialized()) {
				final int N_PLAYERS = 2;
				SessionManager.initialize(bthandler, "BenchmarkActivity", N_PLAYERS);
			}
			andbt = SessionManager.getInstance();
			if (!andbt.isBluetoothEnabled()) {
				andbt.enableBluetooth(GameActivity.this);
			} else {
				andbt.startSession();
			}
		} catch (BluetoothNotSupportedException e) {
			e.printStackTrace();
		}

		SessionManager.getInstance().setGameControl(gc);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		andbt.processActivityResult(requestCode, resultCode, data);
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.cameraGame = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), cameraGame);

		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		textureBg = new BitmapTextureAtlas(getTextureManager(), 320, 480, TextureOptions.DEFAULT);
		textureRegionBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureBg, this, "bg1.png", 0, 0);
		textureBg.load();

		String[] iconsPlayers = { "p-circle.png", "p-square.png", "p-triangle.png", "p-star.png" };

		Sprite spritePlayer;
		BitmapTextureAtlas texturePlayer;
		ITextureRegion textureRegionPlayer;
		for (int x = 0; x < iconsPlayers.length; x++) {
			texturePlayer = new BitmapTextureAtlas(getTextureManager(), ICON_W, ICON_H, TextureOptions.DEFAULT);
			textureRegionPlayer = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texturePlayer, this, iconsPlayers[x], 0, 0);
			texturePlayer.load();

			spritePlayer = new Sprite(getInitX(x), getInitY(x), textureRegionPlayer, getVertexBufferObjectManager());
			spritePlayer.setVisible(false);

			spritesPlayers[x] = spritePlayer;
		}

		spriteBg = new Sprite(0, 0, textureRegionBg, getVertexBufferObjectManager());

		textureOnScreenControl = new BitmapTextureAtlas(getTextureManager(), 128, 128, TextureOptions.DEFAULT);
		textureRegionOnScreenControlBase = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureOnScreenControl, this, "control_base.png", 0, 0);
		textureRegionOnScreenControlKnob = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureOnScreenControl, this, "control_knob.png", 64, 0);

		pOnCreateResourcesCallback.onCreateResourcesFinished();

		started = true;
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		sceneGame = new Scene();
		pOnCreateSceneCallback.onCreateSceneFinished(sceneGame);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		ParallaxBackground backgroundGame = new ParallaxBackground(0, 0, 0);
		backgroundGame.attachParallaxEntity(new ParallaxEntity(0, spriteBg));
		sceneGame.setBackground(backgroundGame);

		Sprite spritePlayer;
		for (int x = 0; x < spritesPlayers.length; x++) {
			spritePlayer = spritesPlayers[x];
			spritePlayer.setVisible(false);
			sceneGame.attachChild(spritePlayer);
		}

		if (isServer) {
			final Sprite me = spritesPlayers[myPlayer];
			me.setVisible(true);
			final PhysicsHandler physicsHandler = new PhysicsHandler(me);
			me.registerUpdateHandler(physicsHandler);

			analogOnScreenControl = new AnalogOnScreenControl(0, 0, this.cameraGame, this.textureRegionOnScreenControlBase, this.textureRegionOnScreenControlKnob, 0.1f, 200, getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
				@Override
				public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
					physicsHandler.setVelocity(pValueX * 250, pValueY * 250);

					analogOnScreenControl.setX(me.getX() - me.getHeight());
					analogOnScreenControl.setY(me.getY() - me.getWidth());
				}

				@Override
				public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
					me.registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.25f, 1, 1.5f), new ScaleModifier(0.25f, 1.5f, 1)));
				}
			});

			analogOnScreenControl.getControlBase().setVisible(false);
			analogOnScreenControl.getControlKnob().setVisible(false);

			sceneGame.setChildScene(analogOnScreenControl);
		}

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
}
