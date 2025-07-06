package entities;

import maps.DungeonGenerator;

import static entities.BehaviorType.PATROL;
import static entities.BehaviorType.WAIT;

public class Enemy {
    public static final int SIZE = 12;
    private static final float SPEED = 40f;
    private static final float ATTACK_RANGE = 16f;
    private static final float ATTACK_COOLDOWN = 1f;

    public float x, y;

    private float attackTimer = 0f;
    private int health = 3;

    private final BehaviorType behavior;

    private float patrolChangeDirectionTimer = 0f;
    private float patrolDirectionX = 0f;
    private float patrolDirectionY = 0f;

    private float facingAngle = 0f;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;

        if (Math.random() < 0.7) {
            behavior = PATROL;
            chooseNewPatrolDirection();
        } else {
            behavior = WAIT;
        }
    }

    private void chooseNewPatrolDirection() {
        double angle = Math.random() * 2 * Math.PI;
        patrolDirectionX = (float) Math.cos(angle);
        patrolDirectionY = (float) Math.sin(angle);
        patrolChangeDirectionTimer = 2 + (float)(Math.random() * 3);
    }

    public void update(float delta, float playerX, float playerY, int[][] map) {
        attackTimer -= delta;

        if (canSeePlayer(playerX, playerY)) {
            moveTowardsPlayer(delta, playerX, playerY, map);
        } else {
            if (behavior == PATROL) {
                patrol(delta, map);
            }
        }
    }

    private void patrol(float delta, int[][] map) {
        patrolChangeDirectionTimer -= delta;
        if (patrolChangeDirectionTimer <= 0) {
            chooseNewPatrolDirection();
        }

        facingAngle = (float) Math.toDegrees(Math.atan2(patrolDirectionY, patrolDirectionX));

        float newX = x + patrolDirectionX * SPEED * delta;
        float newY = y + patrolDirectionY * SPEED * delta;

        if (canWalkThrough(newX, newY, map)) {
            x = newX;
            y = newY;
        } else {
            chooseNewPatrolDirection();
        }
    }

    private void moveTowardsPlayer(float delta, float playerX, float playerY, int[][] map) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        float minDist = (SIZE + 12) / 2f + 2;
        if (dist <= minDist) return;

        float dirX = dx / dist;
        float dirY = dy / dist;

        facingAngle = (float) Math.toDegrees(Math.atan2(dirY, dirX));

        float newX = x + dirX * SPEED * delta;
        float newY = y + dirY * SPEED * delta;

        if (canWalkThrough(newX, newY, map)) {
            x = newX;
            y = newY;
        }
    }

    private boolean canWalkThrough(float nx, float ny, int[][] map) {
        int tileX1 = (int)(nx / DungeonGenerator.TILE_SIZE);
        int tileY1 = (int)(ny / DungeonGenerator.TILE_SIZE);
        int tileX2 = (int)((nx + SIZE - 1) / DungeonGenerator.TILE_SIZE);
        int tileY2 = (int)((ny + SIZE - 1) / DungeonGenerator.TILE_SIZE);

        return isTileEmpty(tileX1, tileY1, map) &&
            isTileEmpty(tileX2, tileY1, map) &&
            isTileEmpty(tileX1, tileY2, map) &&
            isTileEmpty(tileX2, tileY2, map);
    }

    private boolean isTileEmpty(int tileX, int tileY, int[][] map) {
        if (tileX < 0 || tileY < 0 || tileX >= DungeonGenerator.WIDTH || tileY >= DungeonGenerator.HEIGHT) {
            return false;
        }
        return map[tileX][tileY] == DungeonGenerator.TILE_EMPTY;
    }

    private boolean canSeePlayer(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= 100;
    }

    public boolean canAttack(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= ATTACK_RANGE && attackTimer <= 0f;
    }

    public void resetAttackCooldown() {
        attackTimer = ATTACK_COOLDOWN;
    }

    public void attack() {
        resetAttackCooldown();
    }

    public boolean isHit(float attackX, float attackY, float attackW, float attackH) {
        return attackX < x + SIZE &&
            attackX + attackW > x &&
            attackY < y + SIZE &&
            attackY + attackH > y;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public float getFacingAngle() {
        return facingAngle;
    }
}
