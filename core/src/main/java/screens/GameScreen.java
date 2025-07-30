package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.Klimenntiy.GameForSummer.MainGame;
import com.badlogic.gdx.math.Matrix4;
import entities.*;
import entities.MainCharacterTypes.PlayerClass;
import maps.DungeonGenerator;
import maps.Portal;

import java.util.ArrayList;

public class GameScreen implements Screen {

    private final MainGame game;
    private final PlayerClass playerClass;
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private int[][] map;

    private float playerX, playerY;
    private float playerFacingAngle = 0f;
    private int playerHealth;

    private float attackCooldown;

    private static final int ENEMY_COUNT = 5;
    private static final int TILE_SIZE = 16;
    private static final int PLAYER_SIZE = 12;
    private static final int VIEW_WIDTH = 10;
    private static final int VIEW_HEIGHT = 7;

    private Portal portal;
    private boolean portalDiscovered = false;
    private boolean levelCompleted = false;
    private float deathTimer;
    private float teleportTimer;

    private float attackBoxX, attackBoxY, attackBoxW, attackBoxH;
    private float showAttackBoxTimer = 0f;

    private boolean[][] explored;
    private boolean showFullMap = false;

    private Matrix4 uiMatrix;

    public GameScreen(MainGame game, PlayerClass playerClass) {
        this.game = game;
        this.playerClass = playerClass;
        this.playerHealth = playerClass.getMaxHealth();
        this.attackCooldown = 0f;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_WIDTH * TILE_SIZE, VIEW_HEIGHT * TILE_SIZE);

        explored = new boolean[DungeonGenerator.WIDTH][DungeonGenerator.HEIGHT];

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        font.getData().setScale(3f);
        uiMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        DungeonGenerator dungeon = new DungeonGenerator();
        map = dungeon.getMap();

        placePlayerRandomly();
        spawnEnemies();
        placePortalInLastRoom(dungeon);
        updateCameraPosition();
    }

    private void placePlayerRandomly() {
        while (true) {
            int x = (int) (Math.random() * DungeonGenerator.WIDTH);
            int y = (int) (Math.random() * DungeonGenerator.HEIGHT);
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
            int x = (int) (Math.random() * DungeonGenerator.WIDTH);
            int y = (int) (Math.random() * DungeonGenerator.HEIGHT);
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
            portal = new Portal(
                x * TILE_SIZE + (TILE_SIZE - 20) / 2f,
                y * TILE_SIZE + (TILE_SIZE - 20) / 2f,
                20);
        }
    }

    private void updateExplored() {
        int radius = 6;
        int centerX = (int) (playerX / TILE_SIZE);
        int centerY = (int) (playerY / TILE_SIZE);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && y >= 0 && x < DungeonGenerator.WIDTH && y < DungeonGenerator.HEIGHT) {
                    explored[x][y] = true;
                    if (!portalDiscovered && portal != null) {
                        float px = x * TILE_SIZE;
                        float py = y * TILE_SIZE;
                        if (px <= portal.getX() + portal.getSize() && px + TILE_SIZE >= portal.getX() &&
                            py <= portal.getY() + portal.getSize() && py + TILE_SIZE >= portal.getY()) {
                            portalDiscovered = true;
                        }
                    }
                }
            }
        }
    }

    private boolean canMoveTo(float x, float y) {
        return isTileEmpty(x, y) &&
            isTileEmpty(x + PLAYER_SIZE - 1, y) &&
            isTileEmpty(x, y + PLAYER_SIZE - 1) &&
            isTileEmpty(x + PLAYER_SIZE - 1, y + PLAYER_SIZE - 1);
    }

    private boolean isTileEmpty(float x, float y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);
        return tileX >= 0 && tileX < DungeonGenerator.WIDTH && tileY >= 0 && tileY < DungeonGenerator.HEIGHT
            && map[tileX][tileY] == DungeonGenerator.TILE_EMPTY;
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
            camera.position.set(playerX + PLAYER_SIZE / 2f, playerY + PLAYER_SIZE / 2f, 0);
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            camera.position.x = Math.max(halfW, Math.min(camera.position.x, DungeonGenerator.WIDTH * TILE_SIZE - halfW));
            camera.position.y = Math.max(halfH, Math.min(camera.position.y, DungeonGenerator.HEIGHT * TILE_SIZE - halfH));
        }
        camera.update();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateExplored();
        updateCameraPosition();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int x = 0; x < DungeonGenerator.WIDTH; x++) {
            for (int y = 0; y < DungeonGenerator.HEIGHT; y++) {
                if (!explored[x][y]) continue;
                shapeRenderer.setColor(map[x][y] == DungeonGenerator.TILE_WALL ? 0.4f : 0.1f, 0.1f, 0.1f, 1);
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
            if (e.isDead()) deadEnemies.add(e);
        }
        enemies.removeAll(deadEnemies);

        if (!showFullMap) {
            shapeRenderer.setColor(1, 0, 0, 1);
            for (Enemy e : enemies) {
                shapeRenderer.rect(e.x, e.y, Enemy.SIZE, Enemy.SIZE);
            }
        }

        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        if (!showFullMap || portalDiscovered) {
            shapeRenderer.setColor(0.3f, 0.3f, 1f, 1f);
            shapeRenderer.rect(portal.getX(), portal.getY(), portal.getSize(), portal.getSize());
        }

        if (playerHealth <= 0) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rectLine(playerX, playerY + PLAYER_SIZE, playerX + PLAYER_SIZE, playerY, 2);
            shapeRenderer.rectLine(playerX, playerY, playerX + PLAYER_SIZE, playerY + PLAYER_SIZE, 2);
        }

        if (showAttackBoxTimer > 0f) {
            shapeRenderer.setColor(1, 1, 0, 1);
            shapeRenderer.rect(attackBoxX, attackBoxY, attackBoxW, attackBoxH);
            showAttackBoxTimer -= delta;
        }

        shapeRenderer.end();

        if (!showFullMap) {
            batch.setProjectionMatrix(uiMatrix);
            batch.begin();
            font.draw(batch, "HP: " + Math.max(playerHealth, 0) + "/" + playerClass.getMaxHealth(), 10, Gdx.graphics.getHeight() - 10);
            batch.end();
        }

        if (playerHealth <= 0 && deathTimer <= 0) {
            deathTimer = 3.0f;
        }

        if (playerHealth <= 0) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                game.setScreen(new MenuScreen(game));
            }
        }
    }

    private void handleInput(float delta) {
        if (playerHealth <= 0) return;

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.M)) showFullMap = !showFullMap;
        if (showFullMap) return;

        float speed = 150;
        float newX = playerX, newY = playerY;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) newY += speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) newY -= speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) newX -= speed * delta;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) newX += speed * delta;

        if (canMoveTo(newX, newY)) {
            float dx = newX - playerX, dy = newY - playerY;
            if (dx != 0 || dy != 0) playerFacingAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
            playerX = newX; playerY = newY;
        }

        attackCooldown -= delta;

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) && attackCooldown <= 0f) {
            double rad = Math.toRadians(playerFacingAngle);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);

            float forwardX = cos;
            float forwardY = sin;
            float rightX = -sin;
            float rightY = cos;

            float centerX = playerX + PLAYER_SIZE / 2f;
            float centerY = playerY + PLAYER_SIZE / 2f;

            float attackLength = 100f;
            float attackThickness = 8f;
            float startOffset = PLAYER_SIZE / 2f;

            if (playerClass.getClassName().equals("Mage")) {
                attackLength = 12f;
                attackThickness = 12f;
                startOffset = PLAYER_SIZE + 20f;
            } else if (playerClass.getClassName().equals("Warrior")) {
                attackLength = 16f;
                attackThickness = 16f;
                startOffset = PLAYER_SIZE / 2f;
            }

            float edgeX = centerX + forwardX * startOffset;
            float edgeY = centerY + forwardY * startOffset;

            float attackCenterX = edgeX + forwardX * (attackLength / 2f);
            float attackCenterY = edgeY + forwardY * (attackLength / 2f);

            float attackW = Math.abs(forwardX * attackLength + rightX * attackThickness);
            float attackH = Math.abs(forwardY * attackLength + rightY * attackThickness);

            float attackX = attackCenterX - attackW / 2f;
            float attackY = attackCenterY - attackH / 2f;

            attackBoxX = attackX;
            attackBoxY = attackY;
            attackBoxW = attackW;
            attackBoxH = attackH;
            showAttackBoxTimer = 0.15f;
            attackCooldown = playerClass.getAttackSpeed();

            ArrayList<Enemy> deadEnemies = new ArrayList<>();
            for (Enemy e : enemies) {
                if (e.isHit(attackBoxX, attackBoxY, attackBoxW, attackBoxH)) {
                    e.takeDamage(playerClass.getAttackDamage());
                    if (e.isDead()) deadEnemies.add(e);
                }
            }
            enemies.removeAll(deadEnemies);
        }

        if (!levelCompleted && portal != null && portal.isPlayerInPortal(playerX, playerY, PLAYER_SIZE)) {
            levelCompleted = true;
            teleportTimer = 0.1f;
        }

        if (levelCompleted) {
            teleportTimer -= delta;
            if (teleportTimer <= 0) game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int width, int height) { camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
