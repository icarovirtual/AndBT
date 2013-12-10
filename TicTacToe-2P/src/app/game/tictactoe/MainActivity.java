package app.game.tictactoe;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import app.game.tictactoe.util.AlertDialogFragment;

public class MainActivity extends BaseGameActivity {
	public static final int CAMERA_WIDTH = 320;
	public static final int CAMERA_HEIGHT = 480;

	private BitmapTextureAtlas bgTexture;
	private BitmapTextureAtlas titleTexture;

	private ITextureRegion bgTextureRegion;
	private ITextureRegion titleTextureRegion;

	private BuildableBitmapTextureAtlas buttonsTexture;

	private ITextureRegion onlineButton;
	private ITextureRegion localButton;
	private ITextureRegion exitButton;

	private Scene mainScene;

	private Camera mainCamera;

	// BTHandler handler;

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mainCamera = new Camera(0, 0, MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
				MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT), this.mainCamera);

		return engineOptions;
	}

	@Override
	public void onCreateResources(final OnCreateResourcesCallback pOnCreateResourcesCallback) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.bgTexture = new BitmapTextureAtlas(this.getTextureManager(), MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT, TextureOptions.DEFAULT);
		this.bgTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.bgTexture, this, "bg1.png", 0, 0);
		this.bgTexture.load();

		this.titleTexture = new BitmapTextureAtlas(this.getTextureManager(), 245, 141, TextureOptions.DEFAULT);
		this.titleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.titleTexture, this, "title.png", 0, 0);
		this.titleTexture.load();

		this.buttonsTexture = new BuildableBitmapTextureAtlas(this.getTextureManager(), MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT);
		this.onlineButton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.buttonsTexture, this, "butOnline.png");
		this.localButton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.buttonsTexture, this, "butLocal.png");
		this.exitButton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.buttonsTexture, this, "butExit.png");

		try {
			this.buttonsTexture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
			this.buttonsTexture.load();
		} catch (final TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(final OnCreateSceneCallback pOnCreateSceneCallback) {
		this.mainScene = new Scene();
		pOnCreateSceneCallback.onCreateSceneFinished(this.mainScene);
	}

	@Override
	public void onPopulateScene(final Scene pScene, final OnPopulateSceneCallback pOnPopulateSceneCallback) {
		final Sprite bgSprite = new Sprite(0, 0, this.bgTextureRegion, this.getVertexBufferObjectManager());

		final int BUTTONS_SPACING = 90;
		final int TITLE_SPACING = 35;
		final Sprite onlinePlaySprite = new Sprite(MainActivity.CAMERA_WIDTH / 2 - this.onlineButton.getWidth() / 2, MainActivity.CAMERA_HEIGHT / 2
				- this.onlineButton.getHeight() / 2, this.onlineButton, this.getVertexBufferObjectManager()) {
			@SuppressLint("NewApi")
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				// CHANGE ACTIVITY ON TOUCH
				if (pSceneTouchEvent.isActionDown()) {
					final Bundle bundleBT = new Bundle();
					bundleBT.putCharSequence("title", "Multiplayer game");
					bundleBT.putCharSequence("state", "Create or join game?");
					bundleBT.putCharSequence("positive", "Create");
					bundleBT.putCharSequence("negative", "Join");

					final AlertDialogFragment dialogBT = AlertDialogFragment.newInstance(bundleBT);

					dialogBT.show(MainActivity.this.getFragmentManager(), "bt");
				}
				return true;
			}
		};
		final Sprite twoPlaySprite = new Sprite(MainActivity.CAMERA_WIDTH / 2 - this.localButton.getWidth() / 2, MainActivity.CAMERA_HEIGHT / 2 - this.localButton.getHeight()
				/ 2 + BUTTONS_SPACING, this.localButton, this.getVertexBufferObjectManager()) {
			@SuppressLint("NewApi")
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				// CHANGE ACTIVITY ON TOUCH
				if (pSceneTouchEvent.isActionDown()) {
					MainActivity.this.startActivity(new Intent(MainActivity.this, BoardActivity.class));
				}
				return true;
			}
		};
		final Sprite exitSprite = new Sprite(MainActivity.CAMERA_WIDTH / 2 - this.exitButton.getWidth() / 2, MainActivity.CAMERA_HEIGHT / 2 - this.exitButton.getHeight() / 2
				+ BUTTONS_SPACING + BUTTONS_SPACING, this.exitButton, this.getVertexBufferObjectManager()) {
			@SuppressLint("NewApi")
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					System.exit(0);
				}
				return true;
			}
		};

		final Sprite titleSprite = new Sprite(MainActivity.CAMERA_WIDTH / 2 - this.titleTexture.getWidth() / 2, TITLE_SPACING, this.titleTextureRegion,
				this.getVertexBufferObjectManager());

		final ParallaxBackground mainBackground = new ParallaxBackground(0, 0, 0);
		mainBackground.attachParallaxEntity(new ParallaxEntity(0, bgSprite));

		this.mainScene.setBackground(mainBackground);

		this.mainScene.attachChild(onlinePlaySprite);
		this.mainScene.attachChild(twoPlaySprite);
		this.mainScene.attachChild(exitSprite);
		this.mainScene.attachChild(titleSprite);

		this.mainScene.registerTouchArea(onlinePlaySprite);
		this.mainScene.registerTouchArea(twoPlaySprite);
		this.mainScene.registerTouchArea(exitSprite);

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	/**
	 * Start game as server
	 */
	@SuppressLint("NewApi")
	public void doPositiveClick() {
		// SessionManager.getInstance().makeDiscoverable(this);
		final Intent intentBT = new Intent(MainActivity.this, OnlineActivity.class);
		intentBT.putExtra("mode", "create");
		this.startActivity(intentBT);
	}

	/**
	 * Join an already existing game
	 */
	@SuppressLint("NewApi")
	public void doNegativeClick() {
		// Intent intentBT = new Intent(MainActivity.this,
		// DeviceListActivity.class);
		final Intent intentBT = new Intent(MainActivity.this, OnlineActivity.class);
		intentBT.putExtra("mode", "join");
		this.startActivity(intentBT);
		// startActivityForResult(intentBT,
		// Utils.REQUEST_CONNECT_DEVICE_SECURE);
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data)
	// {
	// switch (requestCode)
	// {
	// case Utils.REQUEST_CONNECT_DEVICE_SECURE:
	// // When DeviceListActivity returns with a device to connect
	// if (resultCode == Activity.RESULT_OK)
	// {
	// Log.d(TAG, "SECURE CONN");
	// // Get the device MAC address
	// final String address =
	// data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	// SessionManager.getInstance().connectDevice(true, address);
	// }
	// break;
	// case Utils.REQUEST_ENABLE_BT:
	// // When the request to enable Bluetooth returns
	// if (resultCode == Activity.RESULT_OK)
	// {
	// // Bluetooth is now enabled, so set up a chat session
	// Log.d(TAG, "ENABLE BT OK");
	// }
	// else
	// {
	// // User did not enable Bluetooth or an error occurred
	// Log.d(TAG, "ENABLE BT FAIL");
	// }
	// }
	//
	// super.onActivityResult(requestCode, resultCode, data);
	// }
}
