package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.Wall;
import game.entities.ghosts.Ghost;

// A*‐based strategy for Pinky (the pink ghost)
// got help from chatgpt
public class PinkyStrategy implements IGhostStrategy {

    private Ghost ghost; // Pinky instance

    public PinkyStrategy(Ghost ghost) {
        this.ghost = ghost;
    }

    // Helper node class for A*
    private static class Node {
        int x;
        int y;
        double g; // cost from start
        double f; // g + heuristic

        Node(int x, int y, double g, double f) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
        }
    }

    // Manhattan distance heuristic
    private double heuristic(int x, int y, int goalX, int goalY) {
        return Math.abs(x - goalX) + Math.abs(y - goalY);
    }

    // Use A* to find a shortest path from Pinky to Pacman, then return the next tile along that path
    @Override
    public int[] getChaseTargetPosition() {
        final int tileSize = 8;

        int cols = GameplayPanel.width / tileSize;
        int rows = GameplayPanel.height / tileSize;

        // Build blocked grid from walls
        boolean[][] blocked = new boolean[cols][rows];
        for (Wall wall : Game.getWalls()) {
            int wx = wall.getxPos() / tileSize;
            int wy = wall.getyPos() / tileSize;
            if (wx >= 0 && wx < cols && wy >= 0 && wy < rows) {
                blocked[wx][wy] = true;
            }
        }

        // Get Pinky and Pacman's pixel positions
        int pinkyX = ghost.getxPos();
        int pinkyY = ghost.getyPos();
        int pacX   = Game.getPacman().getxPos();
        int pacY   = Game.getPacman().getyPos();

        // Clamp to valid pixel range to avoid negative / overflow indices
        pinkyX = Math.max(0, Math.min(GameplayPanel.width  - 1, pinkyX));
        pinkyY = Math.max(0, Math.min(GameplayPanel.height - 1, pinkyY));
        pacX   = Math.max(0, Math.min(GameplayPanel.width  - 1, pacX));
        pacY   = Math.max(0, Math.min(GameplayPanel.height - 1, pacY));

        // Convert to grid coords
        int startX = pinkyX / tileSize;
        int startY = pinkyY / tileSize;
        int goalX  = pacX / tileSize;
        int goalY  = pacY / tileSize;

        startX = Math.max(0, Math.min(cols - 1, startX));
        startY = Math.max(0, Math.min(rows - 1, startY));
        goalX  = Math.max(0, Math.min(cols - 1, goalX));
        goalY  = Math.max(0, Math.min(rows - 1, goalY));

        // If already on Pacman's tile, just target him
        if (startX == goalX && startY == goalY) {
            return new int[] { pacX, pacY };
        }

        double[][] gScore = new double[cols][rows];
        boolean[][] closed = new boolean[cols][rows];
        Node[][] parent = new Node[cols][rows];

        // Init gScore to "infinite"
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                gScore[x][y] = Double.POSITIVE_INFINITY;
            }
        }
        gScore[startX][startY] = 0.0;

        java.util.PriorityQueue<Node> openSet =
                new java.util.PriorityQueue<>((a, b) -> Double.compare(a.f, b.f));

        double h0 = heuristic(startX, startY, goalX, goalY);
        openSet.add(new Node(startX, startY, 0.0, h0));

        int[][] dirs = {
                { 1, 0 },  // right
                { -1, 0 }, // left
                { 0, 1 },  // down
                { 0, -1 }  // up
        };

        boolean found = false;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (closed[current.x][current.y]) {
                continue;
            }
            closed[current.x][current.y] = true;

            if (current.x == goalX && current.y == goalY) {
                found = true;
                break;
            }

            for (int[] d : dirs) {
                int nx = current.x + d[0];
                int ny = current.y + d[1];

                if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
                if (blocked[nx][ny]) continue;
                if (closed[nx][ny]) continue;

                double tentativeG = gScore[current.x][current.y] + 1.0;

                if (tentativeG < gScore[nx][ny]) {
                    gScore[nx][ny] = tentativeG;
                    double h = heuristic(nx, ny, goalX, goalY);
                    double f = tentativeG + h;
                    parent[nx][ny] = current;
                    openSet.add(new Node(nx, ny, tentativeG, f));
                }
            }
        }

        // If no path found, fall back to targeting Pacman's position directly
        if (!found) {
            int[] fallback = new int[2];
            fallback[0] = pacX;
            fallback[1] = pacY;
            return fallback;
        }

        // Reconstruct path from goal back to start
        java.util.List<Node> path = new java.util.ArrayList<>();
        Node curr = new Node(goalX, goalY, 0, 0);

        while (!(curr.x == startX && curr.y == startY)) {
            path.add(curr);
            curr = parent[curr.x][curr.y];
            if (curr == null) break; // safety
        }

        java.util.Collections.reverse(path);

        if (path.isEmpty()) {
            int[] fallback = new int[2];
            fallback[0] = pacX;
            fallback[1] = pacY;
            return fallback;
        }

        // Next step along the A* path
        Node nextStep = path.get(0);

        int[] target = new int[2];
        target[0] = nextStep.x * tileSize;
        target[1] = nextStep.y * tileSize;
        return target;
    }

    // When resting, Pinky still targets the top-left corner
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = 0;
        position[1] = 0;
        return position;
    }
}
