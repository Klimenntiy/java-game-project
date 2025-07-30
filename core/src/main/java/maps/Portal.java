package maps;

public class Portal {
    private final float x;
    private final float y;
    private final int size;

    public Portal(float x, float y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }

    public boolean isPlayerInPortal(float playerX, float playerY, int playerSize) {
        return playerX + playerSize > x &&
            playerX < x + size &&
            playerY + playerSize > y &&
            playerY < y + size;
    }
}
