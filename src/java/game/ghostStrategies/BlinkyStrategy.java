package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.Wall;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Concrete strategy for Blinky (the red ghost) using BFS pathfinding
public class BlinkyStrategy implements IGhostStrategy {

    // Node for BFS
    private static class Node {
        int x;
        int y;
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public int[] getChaseTargetPosition() {
        final int tileSize = 8;

        int cols = GameplayPanel.width / tileSize;
        int rows = GameplayPanel.height / tileSize;

        // Build a grid of blocked cells from walls
        boolean[][] blocked = new boolean[cols][rows];

        for (Wall wall : Game.getWalls()) {
            int wx = wall.getxPos() / tileSize;
            int wy = wall.getyPos() / tileSize;
            if (wx >= 0 && wx < cols && wy >= 0 && wy < rows) {
                blocked[wx][wy] = true;
            }
        }

        // ----- Normalize Blinky & Pacman positions to be inside the grid -----
        int blinkyX = Game.getBlinky().getxPos();
        int blinkyY = Game.getBlinky().getyPos();
        int pacX = Game.getPacman().getxPos();
        int pacY = Game.getPacman().getyPos();

        // Clamp to [0, width/height-1] so we never get -1 or cols/rows
        blinkyX = Math.max(0, Math.min(GameplayPanel.width  - 1, blinkyX));
        blinkyY = Math.max(0, Math.min(GameplayPanel.height - 1, blinkyY));
        pacX    = Math.max(0, Math.min(GameplayPanel.width  - 1, pacX));
        pacY    = Math.max(0, Math.min(GameplayPanel.height - 1, pacY));

        // Start = Blinky's current cell
        int startX = blinkyX / tileSize;
        int startY = blinkyY / tileSize;

        // Goal = Pacman's current cell
        int goalX = pacX / tileSize;
        int goalY = pacY / tileSize;

        // Safety clamp for indices (extra belt-and-suspenders)
        startX = Math.max(0, Math.min(cols - 1, startX));
        startY = Math.max(0, Math.min(rows - 1, startY));
        goalX  = Math.max(0, Math.min(cols - 1, goalX));
        goalY  = Math.max(0, Math.min(rows - 1, goalY));

        // If we’re already on Pacman, just target him directly
        if (startX == goalX && startY == goalY) {
            return new int[] { pacX, pacY };
        }

        boolean[][] visited = new boolean[cols][rows];
        Node[][] parent = new Node[cols][rows];

        Deque<Node> queue = new ArrayDeque<>();
        queue.add(new Node(startX, startY));
        visited[startX][startY] = true;

        // 4-directional BFS (right, left, down, up)
        int[][] dirs = {
                { 1, 0 },
                { -1, 0 },
                { 0, 1 },
                { 0, -1 }
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

        // If no path was found, fall back to directly targeting Pacman
        /*if (!found) {
            int[] fallback = new int[2];
            fallback[0] = pacX;
            fallback[1] = pacY;
            return fallback;
        }*/

        // Reconstruct path from goal back to start
        List<Node> path = new ArrayList<>();
        Node curr = new Node(goalX, goalY);

        while (!(curr.x == startX && curr.y == startY)) {
            path.add(curr);
            curr = parent[curr.x][curr.y];
            if (curr == null) break; // Safety check
        }

        // Reverse to get start -> goal order
        java.util.Collections.reverse(path);

        // If for some reason the path is empty, fall back to Pacman
        /*if (path.isEmpty()) {
            int[] fallback = new int[2];
            fallback[0] = pacX;
            fallback[1] = pacY;
            return fallback;
        }*/

        // Next step in the path after Blinky's current position
        Node nextStep = path.get(0);

        // Convert cell coords back to pixel coords
        int[] target = new int[2];
        target[0] = nextStep.x * tileSize;
        target[1] = nextStep.y * tileSize;
        System.out.printf(
                "BFS: Blinky (%d,%d) -> Pacman (%d,%d), pathLen=%d, nextStep=(%d,%d)%n",
                startX, startY, goalX, goalY, path.size(), nextStep.x, nextStep.y
        );
        return target;
    }

    // When resting, Blinky still targets the top-right corner
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = GameplayPanel.width;
        position[1] = 0;
        return position;
    }
}

