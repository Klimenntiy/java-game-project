package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.Klimenntiy.GameForSummer.MainGame;
import com.badlogic.gdx.math.Matrix4;
import entities.Enemy;
import maps.DungeonGenerator;

import java.util.ArrayList;

public class GameScreen implements Screen {

    private final MainGame game;
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private static final int ENEMY_COUNT = 5;
    private float attackCooldown = 0.3f;
    private static final float ATTACK_COOLDOWN_TIME = 0.3f;

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private int[][] map;

    private float playerX;
    private float playerY;

    private static final int TILE_SIZE = 16;
    private static final int PLAYER_SIZE = 12;

    private static final int VIEW_WIDTH = 10;
    private static final int VIEW_HEIGHT = 7;

    private boolean showFullMap = false;

    private float attackBoxX, attackBoxY, attackBoxW, attackBoxH;
    private float showAttackBoxTimer = 0f;

    private int playerHealth = 10;

    private float playerFacingAngle = 0f;

    private Matrix4 uiMatrix;

    private boolean playerDead = false;
    private float deathTimer;
    private float teleportTimer;

    private float portalX, portalY;
    private static final int PORTAL_SIZE = 20;
    private boolean levelCompleted = false;



    public GameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_WIDTH * TILE_SIZE, VIEW_HEIGHT * TILE_SIZE);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        uiMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.getData().setScale(3f);



        DungeonGenerator dungeon = new DungeonGenerator();
        map = dungeon.getMap();

        placePlayerRandomly();

        spawnEnemies();

        placePortalInLastRoom(dungeon);

        updateCameraPosition();
    }

    private void placePlayerRandomly() {
        while (true) {
            int x = (int)(Math.random() * DungeonGenerator.WIDTH);
            int y = (int)(Math.random() * DungeonGenerator.HEIGHT);
            if (map[x][y] == DungeonGenerator.TILE_EMPTY) {
                playerX = x * TILE_SIZE + (TILE_SIZE - PLAYER_SIZE) / 2f;
                playerY = y * TILE_SIZE + (TILE_SIZE - PLAYER_SIZE) / 2f;
                break;
            }
        }
    }

    private void spawnEnemies() {
        int spawned = 0;
        while (spawned < ENEMY_COUNT) {
            int x = (int)(Math.random() * DungeonGenerator.WIDTH);
            int y = (int)(Math.random() * DungeonGenerator.HEIGHT);
            if (map[x][y] == DungeonGenerator.TILE_EMPTY) {
                float ex = x * TILE_SIZE + (TILE_SIZE - Enemy.SIZE) / 2f;
                float ey = y * TILE_SIZE + (TILE_SIZE - Enemy.SIZE) / 2f;
                enemies.add(new Enemy(ex, ey));
                spawned++;
            }
        }
    }

    private void placePortalInLastRoom(DungeonGenerator dungeon) {
        maps.Room lastRoom = dungeon.getLastRoom();
        if (lastRoom != null) {
            int x = lastRoom.centerX();
            int y = lastRoom.centerY();
            portalX = x * TILE_SIZE + (TILE_SIZE - PORTAL_SIZE) / 2f;
            portalY = y * TILE_SIZE + (TILE_SIZE - PORTAL_SIZE) / 2f;
        }
    }

    private boolean canMoveTo(float x, float y) {
        return isTileEmpty(x, y) &&
            isTileEmpty(x + PLAYER_SIZE - 1, y) &&
            isTileEmpty(x, y + PLAYER_SIZE - 1) &&
            isTileEmpty(x + PLAYER_SIZE - 1, y + PLAYER_SIZE - 1);
    }

    private boolean isTileEmpty(float x, float y) {
        int tileX = (int)(x / TILE_SIZE);
        int tileY = (int)(y / TILE_SIZE);
        if (tileX < 0 || tileX >= DungeonGenerator.WIDTH || tileY < 0 || tileY >= DungeonGenerator.HEIGHT) {
            return false;
        }
        return map[tileX][tileY] == DungeonGenerator.TILE_EMPTY;
    }

    private void updateCameraPosition() {
        if (showFullMap) {
            camera.position.set(
                (DungeonGenerator.WIDTH * TILE_SIZE) / 2f,
                (DungeonGenerator.HEIGHT * TILE_SIZE) / 2f,
                0);
            camera.viewportWidth = DungeonGenerator.WIDTH * TILE_SIZE;
            camera.viewportHeight = DungeonGenerator.HEIGHT * TILE_SIZE;
        } else {
            camera.viewportWidth = VIEW_WIDTH * TILE_SIZE;
            camera.viewportHeight = VIEW_HEIGHT * TILE_SIZE;

            camera.position.set(
                playerX + PLAYER_SIZE / 2f,
                playerY + PLAYER_SIZE / 2f,
                0);

            float halfWidth = camera.viewportWidth / 2f;
            float halfHeight = camera.viewportHeight / 2f;
            float maxX = DungeonGenerator.WIDTH * TILE_SIZE - halfWidth;
            float maxY = DungeonGenerator.HEIGHT * TILE_SIZE - halfHeight;

            if (camera.position.x < halfWidth) camera.position.x = halfWidth;
            if (camera.position.x > maxX) camera.position.x = maxX;
            if (camera.position.y < halfHeight) camera.position.y = halfHeight;
            if (camera.position.y > maxY) camera.position.y = maxY;
        }

        camera.update();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateCameraPosition();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int x = 0; x < DungeonGenerator.WIDTH; x++) {
            for (int y = 0; y < DungeonGenerator.HEIGHT; y++) {
                if (map[x][y] == DungeonGenerator.TILE_WALL) {
                    shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1);
                } else {
                    shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
                }
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        ArrayList<Enemy> deadEnemies = new ArrayList<>();
        for (Enemy e : enemies) {
            e.update(delta, playerX, playerY, map);

            if (e.canAttack(playerX, playerY)) {
                playerHealth--;
                e.attack();
            }

            if (e.isDead()) {
                deadEnemies.add(e);
            }
        }
        enemies.removeAll(deadEnemies);

        shapeRenderer.setColor(1, 0, 0, 1);
        for (Enemy e : enemies) {
            shapeRenderer.rect(e.x, e.y, Enemy.SIZE, Enemy.SIZE);
        }

        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        if (playerDead) {
            shapeRenderer.setColor(1, 0, 0, 1);

            shapeRenderer.rectLine(
                playerX, playerY + PLAYER_SIZE,
                playerX + PLAYER_SIZE, playerY,
                2
            );

            shapeRenderer.rectLine(
                playerX, playerY,
                playerX + PLAYER_SIZE, playerY + PLAYER_SIZE,
                2
            );
        }

        if (!playerDead && !levelCompleted) {
            if (playerX + PLAYER_SIZE > portalX && playerX < portalX + PORTAL_SIZE &&
                playerY + PLAYER_SIZE > portalY && playerY < portalY + PORTAL_SIZE) {
                levelCompleted = true;
                teleportTimer = 0.01f;
            }
        }

        if (showAttackBoxTimer > 0f) {
            shapeRenderer.setColor(1, 1, 0, 1);
            shapeRenderer.rect(attackBoxX, attackBoxY, attackBoxW, attackBoxH);
            showAttackBoxTimer -= delta;
        }

        shapeRenderer.setColor(0.3f, 0.3f, 1f, 1f);
        shapeRenderer.rect(portalX, portalY, PORTAL_SIZE, PORTAL_SIZE);

        shapeRenderer.end();

        if (!showFullMap) {
            batch.setProjectionMatrix(uiMatrix);
            batch.begin();
            if (playerHealth < 0){
                playerHealth = 0;
            }
            int maxPlayerHealth = 10;
            font.draw(batch, "HP: " + playerHealth + "/" + maxPlayerHealth, 10, Gdx.graphics.getHeight() - 10);
            batch.end();
        }

        if (playerHealth <= 0 && !playerDead) {
            playerDead = true;
            deathTimer = 3.0f;
        }


    }

    private void handleInput(float delta) {

        if (playerDead) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                game.setScreen(new MenuScreen(game));
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.M)) {
            showFullMap = !showFullMap;
        }

        if (showFullMap) return;

        float newX = playerX;
        float newY = playerY;
        float speed = 150;

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) newY += speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) newY -= speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) newX -= speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) newX += speed * delta;

        if (canMoveTo(newX, newY)) {
            float dx = newX - playerX;
            float dy = newY - playerY;
            if (dx != 0 || dy != 0) {
                playerFacingAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
            }
            playerX = newX;
            playerY = newY;
        }

        attackCooldown -= delta;

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) && attackCooldown <= 0f) {
            float attackW = 16;
            float attackH = 16;
            float attackX = playerX + (PLAYER_SIZE - attackW) / 2f;
            float attackY = playerY + (PLAYER_SIZE - attackH) / 2f;

            double rad = Math.toRadians(playerFacingAngle);
            float offsetX = (float) (Math.cos(rad) * PLAYER_SIZE);
            float offsetY = (float) (Math.sin(rad) * PLAYER_SIZE);

            attackX += offsetX;
            attackY += offsetY;

            attackBoxX = attackX;
            attackBoxY = attackY;
            attackBoxW = attackW;
            attackBoxH = attackH;
            showAttackBoxTimer = 0.15f;

            attackCooldown = ATTACK_COOLDOWN_TIME;

            ArrayList<Enemy> deadEnemies = new ArrayList<>();
            for (Enemy e : enemies) {
                if (e.isHit(attackX, attackY, attackW, attackH)) {
                    e.takeDamage(1);
                }
            }
            enemies.removeAll(deadEnemies);
        }

        if (levelCompleted) {
            teleportTimer -= delta;
            if (teleportTimer <= 0) {
                game.setScreen(new MenuScreen(game));
            }
            return;
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = VIEW_WIDTH * TILE_SIZE;
        camera.viewportHeight = VIEW_HEIGHT * TILE_SIZE;
        camera.update();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
