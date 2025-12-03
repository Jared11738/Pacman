package game.entities.ghosts;

import game.Game;
import game.entities.MovingEntity;
import game.ghostStates.*;
import game.ghostStrategies.IGhostStrategy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Abstract class to describe ghosts
public abstract class Ghost extends MovingEntity {
    protected GhostState state;

    protected final GhostState chaseMode;
    protected final GhostState scatterMode;
    protected final GhostState frightenedMode;
    protected final GhostState eatenMode;
    protected final GhostState houseMode;

    protected int modeTimer = 0;
    protected int frightenedTimer = 0;
    protected boolean isChasing = false;

    protected static BufferedImage frightenedSprite1;
    protected static BufferedImage frightenedSprite2;
    protected static BufferedImage eatenSprite;

    protected IGhostStrategy strategy;

    public Ghost(int xPos, int yPos, String spriteName) {
        super(32, xPos, yPos, 2, spriteName, 2, 0.1f);

        // Creation of the different ghost states
        chaseMode = new ChaseMode(this);
        scatterMode = new ScatterMode(this);
        frightenedMode = new FrightenedMode(this);
        eatenMode = new EatenMode(this);
        houseMode = new HouseMode(this);

        state = houseMode; // Initial state

        try {
            frightenedSprite1 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened.png"));
            frightenedSprite2 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened_2.png"));
            eatenSprite = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_eaten.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods for transitions between the different states
    public void switchChaseMode() {
        state = chaseMode;
    }
    public void switchScatterMode() {
        state = scatterMode;
    }

    public void switchFrightenedMode() {
        frightenedTimer = 0;
        state = frightenedMode;
    }

    public void switchEatenMode() {
        state = eatenMode;
    }

    public void switchHouseMode() {
        state = houseMode;
    }

    public void switchChaseModeOrScatterMode() {
        if (isChasing) {
            switchChaseMode();
        } else {
            switchScatterMode();
        }
    }

    public IGhostStrategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(IGhostStrategy strategy) {
        this.strategy = strategy;
    }

    public GhostState getState() {
        return state;
    }

    @Override
    public void update() {
        if (!Game.getFirstInput()) return; // Ghosts do not move until the player has moved

        // If the ghost is in the frightened state, a 7s timer starts,
        // and the state will later be notified to apply the appropriate transition
        if (state == frightenedMode) {
            frightenedTimer++;

            if (frightenedTimer >= (60 * 7)) {
                state.timerFrightenedModeOver();
            }
        }

        // Ghosts alternate between chaseMode and scatterMode using a timer.
        // If the ghost is in chaseMode or scatterMode, a timer runs, and after
        // 5s or 20s (depending on the state), the state is notified to apply the transition.
        if (state == chaseMode || state == scatterMode) {
            modeTimer++;

            if ((isChasing && modeTimer >= (60 * 20)) || (!isChasing && modeTimer >= (60 * 5))) {
                state.timerModeOver();
                isChasing = !isChasing;
            }
        }

        // If the ghost is on the cell just above its house, the state is notified to apply the appropriate transition
        if (xPos == 208 && yPos == 168) {
            state.outsideHouse();
        }

        // If the ghost is on the cell in the middle of its house, the state is notified to apply the appropriate transition
        if (xPos == 208 && yPos == 200) {
            state.insideHouse();
        }

        // Depending on the current state, the ghost calculates its next direction,
        // and its position is then updated
        state.computeNextDir();
        updatePosition();
    }

    @Override
    public void render(Graphics2D g) {
        // Different sprites are used depending on the ghost’s state
        // (In retrospect, it might have been better to put a "render" method in GhostState)
        if (state == frightenedMode) {
            if (frightenedTimer <= (60 * 5) || frightenedTimer % 20 > 10) {
                g.drawImage(frightenedSprite1.getSubimage((int)subimage * size, 0, size, size), this.xPos, this.yPos, null);
            } else {
                g.drawImage(frightenedSprite2.getSubimage((int)subimage * size, 0, size, size), this.xPos, this.yPos, null);
            }
        } else if (state == eatenMode) {
            g.drawImage(eatenSprite.getSubimage(direction * size, 0, size, size), this.xPos, this.yPos, null);
        } else {
            g.drawImage(sprite.getSubimage((int)subimage * size + direction * size * nbSubimagesPerCycle, 0, size, size), this.xPos, this.yPos, null);
        }
    }
}
