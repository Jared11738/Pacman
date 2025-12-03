package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.Utils;

// Concrete strategy for Clyde (the yellow ghost)
public class ClydeStrategy implements IGhostStrategy {
    private Ghost ghost;

    public ClydeStrategy(Ghost ghost) {
        this.ghost = ghost;
    }

    // Clyde directly targets Pacman if he is beyond a radius of 8 tiles (256px),
    // otherwise he targets his scatter (rest) position
    @Override
    public int[] getChaseTargetPosition() {
        if (Utils.getDistance(ghost.getxPos(), ghost.getyPos(), Game.getPacman().getxPos(), Game.getPacman().getyPos()) >= 256) {
            int[] position = new int[2];
            position[0] = Game.getPacman().getxPos();
            position[1] = Game.getPacman().getyPos();
            return position;
        } else {
            return getScatterTargetPosition();
        }
    }

    // When resting, Clyde targets the bottom-left corner
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = 0;
        position[1] = GameplayPanel.height;
        return position;
    }
}
