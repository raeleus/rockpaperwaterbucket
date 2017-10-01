/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.rockpaperwaterbucket.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import com.ray3k.rockpaperwaterbucket.Core;
import com.ray3k.rockpaperwaterbucket.EntityManager;
import com.ray3k.rockpaperwaterbucket.InputManager;
import com.ray3k.rockpaperwaterbucket.State;
import com.ray3k.rockpaperwaterbucket.entities.EnemyEntity;
import com.ray3k.rockpaperwaterbucket.entities.AnimationEntity;
import com.ray3k.rockpaperwaterbucket.entities.FailureEntity;
import com.ray3k.rockpaperwaterbucket.entities.GameOverTimerEntity;
import com.ray3k.rockpaperwaterbucket.entities.TieEntity;
import com.ray3k.rockpaperwaterbucket.entities.WinnerEntity;
import java.util.Comparator;

public class GameState extends State {
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table choiceTable;
    private Label scoreLabel;
    private EntityManager entityManager;
    public static final String[] names = {"rock", "paper", "scissors", "flower", "fire", "diamond", "water bucket", "chainsaw", "axe"};
    private final ObjectMap<String,Boolean> pairs;
    private final ObjectMap<String, Integer> playerChoices;
    private Table heartTable;
    private AItype aiType;
    private EnemyEntity enemy;
    private int lives;
    public static enum AItype {
        RANDOM, CUNNING
    }
    
    public GameState(Core core) {
        super(core);
        pairs = new ObjectMap<String, Boolean>();
        playerChoices = new ObjectMap<String, Integer>();
    }
    
    @Override
    public void start() {
        score = 0;
        
        lives = 3;
        
        inputManager = new InputManager(); 
        
        playerChoices.clear();
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/rock-paper-bucket.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        enemy = new EnemyEntity(this);
        enemy.setPosition(0.0f, Gdx.graphics.getHeight());
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin, "dark");
        root.add(scoreLabel).expandY().padTop(25.0f).top();
        
        root.row();
        choiceTable = new Table();
        root.add(choiceTable);
        
        Table table = new Table();
        choiceTable.add(table);
        
        TextButton textButton = new TextButton("axe", skin, "small");
        textButton.addListener(new ChoiceListener("axe"));
        table.add(textButton);
        
        textButton = new TextButton("chainsaw", skin, "small");
        textButton.addListener(new ChoiceListener("chainsaw"));
        table.add(textButton);
        
        textButton = new TextButton("diamond", skin, "small");
        textButton.addListener(new ChoiceListener("diamond"));
        table.add(textButton);
        
        textButton = new TextButton("fire", skin, "small");
        textButton.addListener(new ChoiceListener("fire"));
        table.add(textButton);
        
        textButton = new TextButton("flower", skin, "small");
        textButton.addListener(new ChoiceListener("flower"));
        table.add(textButton);
        
        choiceTable.row();
        table = new Table();
        choiceTable.add(table);
        
        textButton = new TextButton("paper", skin, "small");
        textButton.addListener(new ChoiceListener("paper"));
        table.add(textButton);
        
        textButton = new TextButton("rock", skin, "small");
        textButton.addListener(new ChoiceListener("rock"));
        table.add(textButton);
        
        textButton = new TextButton("scissors", skin, "small");
        textButton.addListener(new ChoiceListener("scissors"));
        table.add(textButton);
        
        textButton = new TextButton("water", skin, "small");
        textButton.addListener(new ChoiceListener("water bucket"));
        table.add(textButton);
        
        heartTable = new Table();
        heartTable.setFillParent(true);
        stage.addActor(heartTable);
        
        updateHearts();
    }
    
    private void updateHearts() {
        heartTable.clear();
        for (int i = 0; i < lives; i++) {
            Image image = new Image(skin, "heart");
            heartTable.add(image).space(5.0f).expandY().bottom().padBottom(20.0f);
        }
        
        if (heartTable.getCells().size > 0) {
            heartTable.getCells().first().padLeft(20.0f);
            heartTable.getCells().peek().expandX().left();
        }
    }
    
    private class ChoiceListener extends ChangeListener {
        private String animationName;
        
        public ChoiceListener(String animationName) {
            this.animationName = animationName;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            Array<String> names = new Array<String>(GameState.names);
            final String enemyName;
            
            if (aiType == AItype.RANDOM) {
                enemyName = names.random();
            } else {
                enemyName = calculateCunningChoice();
            }
            
            final boolean win = checkWin(animationName, enemyName);
            int count = playerChoices.get(animationName, 0) + 1;
            playerChoices.put(animationName, count);
            
            
            final AnimationEntity animation = new AnimationEntity(GameState.this, animationName){
                @Override
                public void destroy() {
                    super.destroy();
                    choiceTable.setVisible(true);
                }
                
            };
            animation.setPosition(Gdx.graphics.getWidth() - 50.0f, 50.0f);
            animation.getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
                @Override
                public void event(AnimationState.TrackEntry entry, Event event) {
                    if (event.getData().getName().equals("beep")) {
                        playBeepSound();
                    }
                }

                @Override
                public void complete(AnimationState.TrackEntry entry) {
                    if (entry.getAnimation().getName().equals("count")) {
                        if (animationName.equals(enemyName)) {
                            animation.getAnimationState().setAnimation(0, "stand", false);
                            animation.setDeathTimer(2.0f);
                            TieEntity tie = new TieEntity(GameState.this);
                            tie.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f - 30);
                            tie.setDepth(-10);
                        } else if (win) {
                            animation.getAnimationState().setAnimation(0, "stand", false);
                            animation.getAnimationState().addAnimation(0, "win", false, .5f);
                            playWinSound();
                            addScore(1);
                            WinnerEntity winner = new WinnerEntity(GameState.this);
                            winner.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f - 30);
                            winner.setDepth(-10);
                            enemy.loseAnimation();
                        } else {
                            animation.getAnimationState().setAnimation(0, "stand", false);
                            animation.getAnimationState().addAnimation(0, "lose", false, .5f);
                            FailureEntity failure = new FailureEntity(GameState.this);
                            failure.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f - 30);
                            failure.setDepth(-10);
                            playHitSound();
                            enemy.winAnimation();
                            
                            lives--;
                            updateHearts();
                            if (lives <= 0) {
                                new GameOverTimerEntity(GameState.this, 2.0f);
                            }
                        }
                        
                        final AnimationEntity animation2 = new AnimationEntity(GameState.this, enemyName);
                        animation2.getSkeleton().setFlipX(true);
                        animation2.setPosition(enemy.getX() + 275.0f, enemy.getY() - 300.0f);
                        
                        if (animationName.equals(enemyName)) {
                            animation2.getAnimationState().setAnimation(0, "stand", false);
                            animation2.setDeathTimer(2.0f);
                        } else if (win) {
                            animation2.getAnimationState().setAnimation(0, "stand", true);
                            animation2.getAnimationState().addAnimation(0, "lose", false, 1.0f);
                        } else {
                            animation2.getAnimationState().setAnimation(0, "stand", true);
                            animation2.getAnimationState().addAnimation(0, "win", false, 1.0f);
                        }
                        
                        animation2.getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
                            @Override
                            public void complete(AnimationState.TrackEntry entry) {
                                animation2.setDeathTimer(2.0f);
                            }
                        });
                    } else if (entry.getAnimation().getName().equals("win") || entry.getAnimation().getName().equals("lose")) {
                        animation.setDeathTimer(2.0f);
                    }
                }
            });
            choiceTable.setVisible(false);
        }
    }
    
    private String calculateCunningChoice() {
        ObjectMap<String, Integer> values = new ObjectMap<String, Integer>();
        Array<ObjectMap.Entry<String, Integer>> valuesSorted = new Array<ObjectMap.Entry<String, Integer>>();
        for (String name : names) {
            int count = 0;
            for (String otherName : names) {
                if (checkWin(name, otherName)) {
                    count++;
                }
            }
            values.put(name, count);
        }
        
        for (String playerName : playerChoices.keys()) {
            for (String name : names) {
                int count = values.get(name);
                if (checkWin(name, playerName)) {
                    count += playerChoices.get(playerName);
                }
                values.put(name, count);
            }
        }
        
        ObjectMap.Entries<String, Integer> iter = values.iterator();
        while (iter.hasNext) {
            ObjectMap.Entry<String, Integer> entry = iter.next();
            ObjectMap.Entry<String, Integer> newEntry = new ObjectMap.Entry<String, Integer>();
            newEntry.key = entry.key;
            newEntry.value = entry.value;
            valuesSorted.add(newEntry);
        }
        valuesSorted.sort(new Comparator<ObjectMap.Entry<String, Integer>>() {
            @Override
            public int compare(ObjectMap.Entry<String, Integer> o1,
                    ObjectMap.Entry<String, Integer> o2) {
                return o2.value - o1.value;
            }
        });
        
        System.out.println(playerChoices);
        
        return valuesSorted.get(MathUtils.random(0, 2)).key;
    }
    
    private boolean checkWin(String name, String enemyName) {
        return pairs.get(name + enemyName, false) || !pairs.get(enemyName + name, true);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }
    
    public void playBeepSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/beep.wav", Sound.class).play(.5f);
    }
    
    public void playExplosionSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/explosion.wav", Sound.class).play(.5f);
    }
    
    public void playHitSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/hit.wav", Sound.class).play(.5f);
    }
    
    public void playWinSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/win.wav", Sound.class).play(.5f);
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }

    public ObjectMap<String, Boolean> getPairs() {
        return pairs;
    }

    public AItype getAiType() {
        return aiType;
    }

    public void setAiType(AItype aiType) {
        this.aiType = aiType;
    }

    public Table getChoiceTable() {
        return choiceTable;
    }

    public EnemyEntity getEnemy() {
        return enemy;
    }
}