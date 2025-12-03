package game.entities;

import game.GameplayPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Abstract class to describe a moving entity
public abstract class MovingEntity extends Entity {
    protected int spd;
    protected int xSpd = 0;
    protected int ySpd = 0;
    protected BufferedImage sprite;
    protected float subimage = 0;
    protected int nbSubimagesPerCycle;
    protected int direction = 0;
    protected float imageSpd = 0.2f;

    public MovingEntity(int size, int xPos, int yPos, int spd, String spriteName, int nbSubimagesPerCycle, float imageSpd) {
        super(size, xPos, yPos);
        this.spd = spd;
        try {
            this.sprite = ImageIO.read(getClass().getClassLoader().getResource("img/" + spriteName));
            this.nbSubimagesPerCycle = nbSubimagesPerCycle;
            this.imageSpd = imageSpd;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        updatePosition();
    }

    public void updatePosition() {
        // Updating the entity's position
        if (!(xSpd == 0 && ySpd == 0)) {
            // If the horizontal or vertical speed is not zero, increment the horizontal and vertical position accordingly
            xPos += xSpd;
            yPos += ySpd;

            // Depending on the direction taken, we update the direction value (an integer indicating which part of the sprite to display)
            if (xSpd > 0) {
                direction = 0;
            } else if (xSpd < 0) {
                direction = 1;
            } else if (ySpd < 0) {
                direction = 2;
            } else if (ySpd > 0) {
                direction = 3;
            }

            // Increment the current animation frame value (speed may vary),
            // and based on the total number of animation frames, loop the value
            subimage += imageSpd;
            if (subimage >= nbSubimagesPerCycle) {
                subimage = 0;
            }
        }

        // If the entity goes beyond the edges of the game area, it appears on the opposite side
        if (xPos > GameplayPanel.width) {
            xPos = 0 - size + spd;
        }

        if (xPos < 0 - size + spd) {
            xPos = GameplayPanel.width;
        }

        if (yPos > GameplayPanel.height) {
            yPos = 0 - size + spd;
        }

        if (yPos < 0 - size + spd) {
            yPos = GameplayPanel.height;
        }
    }

    @Override
    public void render(Graphics2D g) {
        // By default, each sprite contains 4 animation variations (one per direction)
        // and each animation contains a set number of frames.
        // Knowing this, we display only the part of the sprite corresponding to the correct direction and animation frame.
        g.drawImage(
                sprite.getSubimage(
                        (int) subimage * size + direction * size * nbSubimagesPerCycle,
                        0,
                        size,
                        size
                ),
                this.xPos,
                this.yPos,
                null
        );
    }

    // Method to check whether the entity is properly positioned on a grid cell in the game area
    public boolean onTheGrid() {
        return (xPos % 8 == 0 && yPos % 8 == 0);
    }

    // Method to check whether the entity is inside the gameplay window
    public boolean onGameplayWindow() {
        return !(xPos <= 0 || xPos >= GameplayPanel.width || yPos <= 0 || yPos >= GameplayPanel.height);
    }

    public Rectangle getHitbox() {
        return new Rectangle(xPos, yPos, size, size);
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public void setSprite(String spriteName) {
        try {
            this.sprite = ImageIO.read(getClass().getClassLoader().getResource("img/" + spriteName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getSubimage() {
        return subimage;
    }

    public void setSubimage(float subimage) {
        this.subimage = subimage;
    }

    public int getNbSubimagesPerCycle() {
        return nbSubimagesPerCycle;
    }

    public void setNbSubimagesPerCycle(int nbSubimagesPerCycle) {
        this.nbSubimagesPerCycle = nbSubimagesPerCycle;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getxSpd() {
        return xSpd;
    }

    public void setxSpd(int xSpd) {
        this.xSpd = xSpd;
    }

    public int getySpd() {
        return ySpd;
    }

    public void setySpd(int ySpd) {
        this.ySpd = ySpd;
    }

    public int getSpd() {
        return spd;
    }
}
