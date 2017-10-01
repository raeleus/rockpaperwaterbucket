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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.rockpaperwaterbucket.Core;
import com.ray3k.rockpaperwaterbucket.State;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/rock-paper-bucket.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        createMenu();
    }
    
    private void createMenu() {
        FileHandle fileHandle = Gdx.files.local(Core.DATA_PATH + "/data.json");
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(fileHandle);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Image image = new Image(skin, "title");
        image.setScaling(Scaling.none);
        root.add(image);
        
        root.defaults().space(30.0f).padLeft(25.0f);
        root.row();
        TextButton textButtton = new TextButton("Play", skin);
        root.add(textButtton);
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/beep.wav", Sound.class).play(.25f);
                showBooleanDialog();
            }
        });
        
        root.row();
        textButtton = new TextButton("Quit", skin);
        root.add(textButtton).expand().top();
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/beep.wav", Sound.class).play(.25f);
                Gdx.app.exit();
            }
        });
    }
    
    private void showBooleanDialog() {
        Dialog dialog = new Dialog("", skin){
            @Override
            protected void result(Object object) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/beep.wav", Sound.class).play(.25f);
                showDifficultyDialog();
            }
        };
        
        Table table = new Table();
        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFadeScrollBars(false);
        dialog.getContentTable().add(scrollPane).grow();
        
        Array<String> usedNames = new Array<String>();
        for (final String name1 : GameState.names) {
            for (final String name2 : GameState.names) {
                if (!name1.equals(name2) && !usedNames.contains(name2, false)) {
                    if (!((GameState) getCore().getStateManager().getState("game")).getPairs().containsKey(name1+name2)) {
                        ((GameState) getCore().getStateManager().getState("game")).getPairs().put(name1+name2, false);
                    }
                    table.row();
                    Label label = new Label("Does " + name1 + " defeat " + name2 + "?", skin);
                    table.add(label);
                    table.row();
                    final Button button = new Button(skin);
                    button.setChecked(((GameState) getCore().getStateManager().getState("game")).getPairs().get(name1+name2, false));
                    button.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                            ((GameState) getCore().getStateManager().getState("game")).getPairs().put(name1+name2, button.isChecked());
                        }
                    });
                    table.add(button).padBottom(20.0f);
                }
            }
            usedNames.add(name1);
        }
        
        
        dialog.button("OK");
        dialog.show(stage);
        dialog.setSize(700.0f, 500.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
        stage.setScrollFocus(scrollPane);
    }
    
    private void showDifficultyDialog() {
        final ButtonGroup buttonGroup = new ButtonGroup();
        Dialog dialog = new Dialog("", skin){
            @Override
            protected void result(Object object) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/beep.wav", Sound.class).play(.25f);
                ((GameState) getCore().getStateManager().getState("game")).setAiType((GameState.AItype) buttonGroup.getChecked().getUserObject());
                getCore().getStateManager().loadState("game");
            }
        };
        
        Label label = new Label("Choose opponent:", skin);
        dialog.getContentTable().add(label).padBottom(20.0f);
        
        dialog.getContentTable().row();
        TextButton textButton = new TextButton("Random AI", skin, "list");
        textButton.setUserObject(GameState.AItype.RANDOM);
        buttonGroup.add(textButton);
        dialog.getContentTable().add(textButton).growX();
        
        dialog.getContentTable().row();
        textButton = new TextButton("Cunning AI", skin, "list");
        textButton.setUserObject(GameState.AItype.CUNNING);
        buttonGroup.add(textButton);
        dialog.getContentTable().add(textButton).growX();
        
        dialog.button("OK");
        dialog.show(stage);
        dialog.setSize(400.0f, 400.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void act(float delta) {
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
        stage.getViewport().update(width, height, true);
    }
}