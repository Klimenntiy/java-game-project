package maps;

import java.util.ArrayList;
import java.util.Random;

public class DungeonGenerator {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 60;
    public static final int TILE_EMPTY = 0;
    public static final int TILE_WALL = 1;
    public static final int TILE_SIZE = 16;


    private final int[][] map;
    private final Random random;
    private final ArrayList<Room> rooms;

    public DungeonGenerator() {
        map = new int[WIDTH][HEIGHT];
        random = new Random();
        rooms = new ArrayList<>();
        generate();
    }

    public int[][] getMap() {
        return map;
    }

    private void generate() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                map[x][y] = TILE_WALL;
            }
        }

        for (int i = 0; i < 10; i++) {
            int roomWidth = random.nextInt(15) + 3;
            int roomHeight = random.nextInt(15) + 3;
            int roomX = random.nextInt(WIDTH - roomWidth - 1) + 1;
            int roomY = random.nextInt(HEIGHT - roomHeight - 1) + 1;

            Room newRoom = new Room(roomX, roomY, roomWidth, roomHeight);
            rooms.add(newRoom);

            for (int x = roomX; x < roomX + roomWidth; x++) {
                for (int y = roomY; y < roomY + roomHeight; y++) {
                    map[x][y] = TILE_EMPTY;
                }
            }
        }

        connectRoomsWithCorridors();
    }

    private void connectRoomsWithCorridors() {
        for (int i = 1; i < rooms.size(); i++) {
            Room prev = rooms.get(i - 1);
            Room curr = rooms.get(i);
            connectRooms(prev, curr);
        }
    }

    private void connectRooms(Room a, Room b) {
        int x1 = a.centerX();
        int y1 = a.centerY();
        int x2 = b.centerX();
        int y2 = b.centerY();

        createHTunnel(x1, x2, y1);
        createVTunnel(y1, y2, x2);
    }

    private final int tunnelWidth = 2;

    private void createHTunnel(int x1, int x2, int yCenter) {
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);
        int halfWidth = tunnelWidth / 2;

        for (int x = min; x < max; x++) {
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                int y = yCenter + offset;
                if (y >= 0 && y < HEIGHT) {
                    map[x][y] = TILE_EMPTY;
                }
            }
        }
    }

    private void createVTunnel(int y1, int y2, int xCenter) {
        int min = Math.min(y1, y2);
        int max = Math.max(y1, y2);
        int halfWidth = tunnelWidth / 2;

        for (int y = min; y < max; y++) {
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                int x = xCenter + offset;
                if (x >= 0 && x < WIDTH) {
                    map[x][y] = TILE_EMPTY;
                }
            }
        }
    }

    public Room getLastRoom() {
        if (rooms.isEmpty()) return null;
        return rooms.get(rooms.size() - 1);
    }

}
