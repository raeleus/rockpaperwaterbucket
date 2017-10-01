package com.ray3k.rockpaperwaterbucket;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.ray3k.rockpaperwaterbucket.SkeletonDataLoader.SkeletonDataLoaderParameter;
import com.ray3k.rockpaperwaterbucket.states.GameOverState;
import com.ray3k.rockpaperwaterbucket.states.GameState;
import com.ray3k.rockpaperwaterbucket.states.LoadingState;
import com.ray3k.rockpaperwaterbucket.states.MenuState;
import java.awt.Desktop;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

public class Core extends ApplicationAdapter {
    public final static String VERSION = "1";
    public final static String DATA_PATH = "rock_paper_water_bucket_data";
    private final static long MS_PER_UPDATE = 10;
    private AssetManager assetManager;
    private StateManager stateManager;
    private SpriteBatch spriteBatch;
    private PixmapPacker pixmapPacker;
    private long previous;
    private long lag;
    private TextureAtlas atlas;
    private SkeletonRenderer skeletonRenderer;
    private ObjectMap<String, Array<String>> imagePacks;

    @Override
    public void create() {
        if (SplashScreen.getSplashScreen() != null) {
            SplashScreen.getSplashScreen().close();
        }
        try {
            initManagers();

            loadAssets();

            previous = TimeUtils.millis();
            lag = 0;

            stateManager.loadState("loading");
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Game Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }
    
    public void initManagers() {
        assetManager = new AssetManager(new LocalFileHandleResolver(), true);
        assetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(new LocalFileHandleResolver()));
        
        stateManager = new StateManager(this);
        stateManager.addState("loading", new LoadingState("menu", this));
        stateManager.addState("menu", new MenuState(this));
        stateManager.addState("game", new GameState(this));
        stateManager.addState("game-over", new GameOverState(this));
        
        spriteBatch = new SpriteBatch();
        
        pixmapPacker = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 5, true, new PixmapPacker.GuillotineStrategy());
        
        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);
        
        imagePacks = new ObjectMap<String, Array<String>>();
//        for (String name : new String[] {"vegetables"}) {
//            imagePacks.put(DATA_PATH + "/" + name, new Array<String>());
//        }
    }
    
    @Override
    public void render() {
        try {
            long current = TimeUtils.millis();
            long elapsed = current - previous;
            previous = current;
            lag += elapsed;

            while (lag >= MS_PER_UPDATE) {
                stateManager.act(MS_PER_UPDATE / 1000.0f);
                lag -= MS_PER_UPDATE;
            }

            stateManager.draw(spriteBatch, lag / MS_PER_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Game Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        stateManager.dispose();
        pixmapPacker.dispose();
        if (atlas != null) {
            atlas.dispose();
        }
    }
    
    public void loadAssets() {
        assetManager.clear();
        SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter(DATA_PATH + "/spine/rockpaperwaterbucket.atlas");
        assetManager.load(DATA_PATH + "/spine/axe.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/chainsaw.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/diamond.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/fire.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/flower.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/paper.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/rock.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/scissors.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/water bucket.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/enemy.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/winner.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/failure.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/tie.json", SkeletonData.class, parameter);
        
        assetManager.load(DATA_PATH + "/ui/rock-paper-bucket.json", Skin.class);

        assetManager.load(DATA_PATH + "/gfx/white.png", Pixmap.class);
        
        assetManager.load(DATA_PATH + "/sfx/beep.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/explosion.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/hit.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/win.wav", Sound.class);
        
        for (String directory : imagePacks.keys()) {
            FileHandle folder = Gdx.files.local(directory);
            for (FileHandle file : folder.list()) {
                assetManager.load(file.path(), Pixmap.class);
                imagePacks.get(directory).add(file.nameWithoutExtension());
            }
        }
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resize(int width, int height) {
        stateManager.resize(width, height);
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public PixmapPacker getPixmapPacker() {
        return pixmapPacker;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public SkeletonRenderer getSkeletonRenderer() {
        return skeletonRenderer;
    }
    
    public ObjectMap<String, Array<String>> getImagePacks() {
        return imagePacks;
    }
}
