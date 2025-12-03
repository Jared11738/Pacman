package game.entities;

import game.Game;
import game.Observer;
import game.Sujet;
import game.entities.ghosts.Ghost;
import game.utils.CollisionDetector;
import game.utils.KeyHandler;
import game.utils.WallCollisionDetector;

import java.util.ArrayList;
import java.util.List;

// Class that describes Pacman
public class Pacman extends MovingEntity implements Sujet {
    private CollisionDetector collisionDetector;
    private List<Observer> observerCollection;

    public Pacman(int xPos, int yPos) {
        super(32, xPos, yPos, 2, "pacman.png", 4, 0.3f);
        observerCollection = new ArrayList<>();
    }

    // Handling movement
    public void input(KeyHandler k) {
        int new_xSpd = 0;
        int new_ySpd = 0;

        if (!onTheGrid()) return;  // Pacman must be on a grid cell in the game area
        if (!onGameplayWindow()) return;  // Pacman must be inside the game area

        // Depending on the keys pressed, Pacman's direction changes accordingly
        if (k.k_left.isPressed && xSpd >= 0 && !WallCollisionDetector.checkWallCollision(this, -spd, 0)) {
            new_xSpd = -spd;
        }
        if (k.k_right.isPressed && xSpd <= 0 && !WallCollisionDetector.checkWallCollision(this, spd, 0)) {
            new_xSpd = spd;
        }
        if (k.k_up.isPressed && ySpd >= 0 && !WallCollisionDetector.checkWallCollision(this, 0, -spd)) {
            new_ySpd = -spd;
        }
        if (k.k_down.isPressed && ySpd <= 0 && !WallCollisionDetector.checkWallCollision(this, 0, spd)) {
            new_ySpd = spd;
        }

        if (new_xSpd == 0 && new_ySpd == 0) return;

        if (!Game.getFirstInput()) Game.setFirstInput(true);

        if (Math.abs(new_xSpd) != Math.abs(new_ySpd)) {
            xSpd = new_xSpd;
            ySpd = new_ySpd;
        } else {
            if (xSpd != 0) {
                xSpd = 0;
                ySpd = new_ySpd;
            } else {
                xSpd = new_xSpd;
                ySpd = 0;
            }
        }
    }

    @Override
    public void update() {
        // Each time, we check if Pacman is in contact with a Pac-Gum, a Super Pac-Gum, or a ghost,
        // and the observers are notified accordingly
        PacGum pg = (PacGum) collisionDetector.checkCollision(this, PacGum.class);
        if (pg != null) {
            notifyObserverPacGumEaten(pg);
        }

        SuperPacGum spg = (SuperPacGum) collisionDetector.checkCollision(this, SuperPacGum.class);
        if (spg != null) {
            notifyObserverSuperPacGumEaten(spg);
        }

        Ghost gh = (Ghost) collisionDetector.checkCollision(this, Ghost.class);
        if (gh != null) {
            notifyObserverGhostCollision(gh);
        }

        // If there is no wall at Pacman's next potential position, update his position
        if (!WallCollisionDetector.checkWallCollision(this, xSpd, ySpd)) {
            updatePosition();
        }
    }

    public void setCollisionDetector(CollisionDetector collisionDetector) {
        this.collisionDetector = collisionDetector;
    }

    @Override
    public void registerObserver(Observer observer) {
        observerCollection.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observerCollection.remove(observer);
    }

    @Override
    public void notifyObserverPacGumEaten(PacGum pg) {
        observerCollection.forEach(obs -> obs.updatePacGumEaten(pg));
    }

    @Override
    public void notifyObserverSuperPacGumEaten(SuperPacGum spg) {
        observerCollection.forEach(obs -> obs.updateSuperPacGumEaten(spg));
    }

    @Override
    public void notifyObserverGhostCollision(Ghost gh) {
        observerCollection.forEach(obs -> obs.updateGhostCollision(gh));
    }
}
