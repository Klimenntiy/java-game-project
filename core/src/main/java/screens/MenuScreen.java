package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.Klimenntiy.GameForSummer.MainGame;

public class MenuScreen implements Screen {

    private final MainGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private final String[] options = {"Start Game", "Exit"};
    private int hoveredOption = -1;

    private final float optionX = 350;
    private final float optionYStart = 400;
    private final float optionHeight = 40;

    public MenuScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        updateHoveredOption();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
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
        int mouseY = Gdx.input.getY();

        mouseY = 600 - mouseY;

        hoveredOption = -1;
        for (int i = 0; i < options.length; i++) {
            float yTop = optionYStart - i * optionHeight;
            float yBottom = yTop - optionHeight + 10;
            float optionWidth = 200;
            if (mouseX >= optionX && mouseX <= optionX + optionWidth &&
                mouseY <= yTop && mouseY >= yBottom) {
                hoveredOption = i;
                break;
            }
        }
    }

    private void handleClick() {
        if (hoveredOption != -1 && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (hoveredOption == 0) {
                game.setScreen(new GameScreen(game));
            } else if (hoveredOption == 1) {
                Gdx.app.exit();
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
