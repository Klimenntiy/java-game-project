package screens;

import com.Klimenntiy.GameForSummer.MainGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entities.MainCharacterTypes.*;

public class MenuScreen implements Screen {

    private final MainGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private int hoveredOption = -1;
    private final float optionX = 300;
    private final float optionYStart = 400;
    private final float optionHeight = 60;

    private enum MenuState { MAIN, CLASS_SELECT }
    private MenuState currentState = MenuState.MAIN;

    private final String[] mainMenuOptions = {"Start Game", "Exit"};
    private final String[] classMenuOptions = {"Warrior", "Archer", "Mage", "Back"};

    public MenuScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {
        updateHoveredOption();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        String[] options = (currentState == MenuState.MAIN) ? mainMenuOptions : classMenuOptions;
        for (int i = 0; i < options.length; i++) {
            if (i == hoveredOption) {
                font.setColor(1, 1, 0, 1);
            } else {
                font.setColor(1, 1, 1, 1);
            }
            font.draw(batch, options[i], optionX, optionYStart - i * optionHeight);
        }
        batch.end();

        handleClick();
    }

    private void updateHoveredOption() {
        int mouseX = Gdx.input.getX();
        int mouseY = 600 - Gdx.input.getY();

        String[] options = (currentState == MenuState.MAIN) ? mainMenuOptions : classMenuOptions;

        hoveredOption = -1;
        for (int i = 0; i < options.length; i++) {
            float yTop = optionYStart - i * optionHeight;
            float yBottom = yTop - optionHeight + 10;
            float optionWidth = 300;

            if (mouseX >= optionX && mouseX <= optionX + optionWidth &&
                mouseY <= yTop && mouseY >= yBottom) {
                hoveredOption = i;
                break;
            }
        }
    }

    private void handleClick() {
        if (hoveredOption != -1 && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (currentState == MenuState.MAIN) {
                switch (hoveredOption) {
                    case 0:
                        currentState = MenuState.CLASS_SELECT;
                        break;
                    case 1:
                        Gdx.app.exit();
                        break;
                }
            } else if (currentState == MenuState.CLASS_SELECT) {
                switch (hoveredOption) {
                    case 0:
                        game.setScreen(new GameScreen(game, new Warrior()));
                        break;
                    case 1:
                        game.setScreen(new GameScreen(game, new Archer()));
                        break;
                    case 2:
                        game.setScreen(new GameScreen(game, new Mage()));
                        break;
                    case 3:
                        currentState = MenuState.MAIN;
                        break;
                }
            }
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
