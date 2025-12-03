package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.Wall;
import game.entities.ghosts.Ghost;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

// Hybrid strategy for Inky (blue ghost): uses A* when far, BFS when close
public class InkyStrategy implements IGhostStrategy {

    // This is the Inky instance using this strategy
    private Ghost ghost;

    public InkyStrategy(Ghost ghost) {
        this.ghost = ghost;
    }

    // Simple node type for grid search
    private static class Node {
        int x;
        int y;
        double g; // cost so far (for A*)
        double f; // g + h (for A*)

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Node(int x, int y, double g, double f) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
        }
    }

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

        // Start = Inky's current tile
        int startX = ghost.getxPos() / tileSize;
        int startY = ghost.getyPos() / tileSize;

        // Goal = Pacman's current tile
        int goalX = Game.getPacman().getxPos() / tileSize;
        int goalY = Game.getPacman().getyPos() / tileSize;

        // If already on Pacman's tile, just target Pacman directly
        if (startX == goalX && startY == goalY) {
            return new int[] { Game.getPacman().getxPos(), Game.getPacman().getyPos() };
        }

        // Decide which algorithm to use based on distance (in tiles)
        int dx = goalX - startX;
        int dy = goalY - startY;
        double distanceTiles = Math.sqrt(dx * dx + dy * dy);

        // Threshold: if far away, use A*; if close, use BFS
        final double DISTANCE_THRESHOLD = 12.0;

        Node nextStep;
        if (distanceTiles >= DISTANCE_THRESHOLD) {
            nextStep = findNextStepAStar(startX, startY, goalX, goalY, blocked, cols, rows);
        } else {
            nextStep = findNextStepBFS(startX, startY, goalX, goalY, blocked, cols, rows);
        }

        // If no path found, fall back to directly targeting Pacman
        if (nextStep == null) {
            int[] fallback = new int[2];
            fallback[0] = Game.getPacman().getxPos();
            fallback[1] = Game.getPacman().getyPos();
            return fallback;
        }

        // Convert grid tile back to pixel coordinates
        int[] target = new int[2];
        target[0] = nextStep.x * tileSize;
        target[1] = nextStep.y * tileSize;
        return target;
    }

    // ---------- BFS ----------

    private Node findNextStepBFS(int startX, int startY, int goalX, int goalY,
                                 boolean[][] blocked, int cols, int rows) {

        boolean[][] visited = new boolean[cols][rows];
        Node[][] parent = new Node[cols][rows];

        Deque<Node> queue = new ArrayDeque<>();
        queue.add(new Node(startX, startY));
        visited[startX][startY] = true;

        int[][] dirs = {
                { 1, 0 },  // right
                { -1, 0 }, // left
                { 0, 1 },  // down
                { 0, -1 }  // up
        };

        boolean found = false;

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.x == goalX && current.y == goalY) {
                found = true;
                break;
            }

            for (int[] d : dirs) {
                int nx = current.x + d[0];
                int ny = current.y + d[1];

                if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
                if (blocked[nx][ny]) continue;
                if (visited[nx][ny]) continue;

                visited[nx][ny] = true;
                parent[nx][ny] = current;
                queue.add(new Node(nx, ny));
            }
        }

        if (!found) {
            return null;
        }

        return reconstructNextStep(parent, startX, startY, goalX, goalY);
    }

    // ---------- A* ----------

    private Node findNextStepAStar(int startX, int startY, int goalX, int goalY,
                                   boolean[][] blocked, int cols, int rows) {

        boolean[][] closed = new boolean[cols][rows];
        double[][] gScore = new double[cols][rows];
        Node[][] parent = new Node[cols][rows];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                gScore[x][y] = Double.POSITIVE_INFINITY;
            }
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));

        double h0 = heuristic(startX, startY, goalX, goalY);
        gScore[startX][startY] = 0.0;
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
                    parent[nx][ny] = new Node(current.x, current.y);
                    openSet.add(new Node(nx, ny, tentativeG, f));
                }
            }
        }

        if (!found) {
            return null;
        }

        return reconstructNextStep(parent, startX, startY, goalX, goalY);
    }

    // Manhattan distance heuristic
    private double heuristic(int x, int y, int goalX, int goalY) {
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }

    // Reconstruct path from goal back to start and return the first step after start
    private Node reconstructNextStep(Node[][] parent, int startX, int startY, int goalX, int goalY) {
        List<Node> path = new ArrayList<>();

        int cx = goalX;
        int cy = goalY;

        while (!(cx == startX && cy == startY)) {
            Node p = parent[cx][cy];
            if (p == null) {
                break;
            }
            path.add(new Node(cx, cy));
            cx = p.x;
            cy = p.y;
        }

        if (path.isEmpty()) {
            return null;
        }

        java.util.Collections.reverse(path);
        return path.get(0);
    }

    // When resting, Inky still targets the bottom-right corner
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = GameplayPanel.width;
        position[1] = GameplayPanel.height;
        return position;
    }
}
