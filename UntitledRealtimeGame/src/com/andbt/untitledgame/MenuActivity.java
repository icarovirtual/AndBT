package com.andbt.untitledgame;

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

import android.content.Intent;
import android.util.Log;

public class MenuActivity extends BaseGameActivity {
	public static final int CAMERA_WIDTH = 320;
	public static final int CAMERA_HEIGHT = 480;

	private BitmapTextureAtlas textureBg;
	private BitmapTextureAtlas textureTitle;

	private ITextureRegion textureRegionBg;
	private ITextureRegion textureRegionTitle;

	private BuildableBitmapTextureAtlas textureButtons;
	private ITextureRegion buttonServer;
	private ITextureRegion buttonClient;
	private ITextureRegion buttonExit;

	private Scene sceneMain;
	private Camera cameraMain;

	@Override
	public EngineOptions onCreateEngineOptions() {
		cameraMain = new Camera(0, 0, MenuActivity.CAMERA_WIDTH, MenuActivity.CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.CAMERA_WIDTH, MenuActivity.CAMERA_HEIGHT), cameraMain);

		return engineOptions;
	}

	@Override
	public void onCreateResources(final OnCreateResourcesCallback pOnCreateResourcesCallback) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		textureBg = new BitmapTextureAtlas(getTextureManager(), MenuActivity.CAMERA_WIDTH, MenuActivity.CAMERA_HEIGHT, TextureOptions.DEFAULT);
		textureRegionBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureBg, this, "bg1.png", 0, 0);
		textureBg.load();

		textureTitle = new BitmapTextureAtlas(getTextureManager(), 245, 141, TextureOptions.DEFAULT);
		textureRegionTitle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureTitle, this, "title.png", 0, 0);
		textureTitle.load();

		textureButtons = new BuildableBitmapTextureAtlas(getTextureManager(), MenuActivity.CAMERA_WIDTH, MenuActivity.CAMERA_HEIGHT);
		buttonServer = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureButtons, this, "butCreate.png");
		buttonClient = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureButtons, this, "butJoin.png");
		buttonExit = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureButtons, this, "butExit.png");

		try {
			textureButtons.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
			textureButtons.load();
		} catch (final TextureAtlasBuilderException e) {
			Log.d(MenuActivity.class.getName(), e.getMessage());
		}

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(final OnCreateSceneCallback pOnCreateSceneCallback) {
		sceneMain = new Scene();
		pOnCreateSceneCallback.onCreateSceneFinished(sceneMain);
	}

	@Override
	public void onPopulateScene(final Scene pScene, final OnPopulateSceneCallback pOnPopulateSceneCallback) {
		final Sprite spriteBg = new Sprite(0, 0, textureRegionBg, getVertexBufferObjectManager());

		final int BUTTONS_SPACING = 90;
		final int TITLE_SPACING = 35;

		final Sprite spriteServer = new Sprite(MenuActivity.CAMERA_WIDTH / 2 - buttonServer.getWidth() / 2, MenuActivity.CAMERA_HEIGHT / 2 - buttonServer.getHeight() / 2, buttonServer, getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					Intent intentGame = new Intent(MenuActivity.this, GameActivity.class);
					intentGame.putExtra("who", "server");
					MenuActivity.this.startActivity(intentGame);
				}
				return true;
			}
		};
		final Sprite spriteClient = new Sprite(MenuActivity.CAMERA_WIDTH / 2 - buttonClient.getWidth() / 2, MenuActivity.CAMERA_HEIGHT / 2 - buttonClient.getHeight() / 2 + BUTTONS_SPACING, buttonClient, getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					Intent intentGame = new Intent(MenuActivity.this, GameActivity.class);
					intentGame.putExtra("who", "client");
					MenuActivity.this.startActivity(intentGame);
				}

				return true;
			}
		};
		final Sprite spriteExit = new Sprite(MenuActivity.CAMERA_WIDTH / 2 - buttonExit.getWidth() / 2, MenuActivity.CAMERA_HEIGHT / 2 - buttonExit.getHeight() / 2 + BUTTONS_SPACING + BUTTONS_SPACING, buttonExit, getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					System.exit(0);
				}
				return true;
			}
		};

		final Sprite spirteTitle = new Sprite(MenuActivity.CAMERA_WIDTH / 2 - textureTitle.getWidth() / 2, TITLE_SPACING, textureRegionTitle, getVertexBufferObjectManager());

		final ParallaxBackground backgroundMenu = new ParallaxBackground(0, 0, 0);
		backgroundMenu.attachParallaxEntity(new ParallaxEntity(0, spriteBg));

		sceneMain.setBackground(backgroundMenu);

		sceneMain.attachChild(spriteServer);
		sceneMain.attachChild(spriteClient);
		sceneMain.attachChild(spriteExit);
		sceneMain.attachChild(spirteTitle);

		sceneMain.registerTouchArea(spriteServer);
		sceneMain.registerTouchArea(spriteClient);
		sceneMain.registerTouchArea(spriteExit);

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
}
