package game.ghostStrategies;

// Interface describing the strategies of the different ghosts
// (this video explains them well: https://www.youtube.com/watch?v=ataGotQ7ir8)
public interface IGhostStrategy {
    int[] getChaseTargetPosition();   // Target tile when the ghost is chasing Pacman
    int[] getScatterTargetPosition(); // Target tile when the ghost is in scatter (rest) mode
}

