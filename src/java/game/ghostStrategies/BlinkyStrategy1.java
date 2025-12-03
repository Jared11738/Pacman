package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;

// Concrete strategy for Blinky (the red ghost)
public class BlinkyStrategy1 implements IGhostStrategy {

    // Blinky directly targets Pacman's position
    @Override
    public int[] getChaseTargetPosition() {
        int[] position = new int[2];
        position[0] = Game.getPacman().getxPos();
        position[1] = Game.getPacman().getyPos();
        return position;
    }

    // When resting, Blinky targets the top-right corner
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = GameplayPanel.width;
        position[1] = 0;
        return position;
    }
}
