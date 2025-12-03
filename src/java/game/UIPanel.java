package game;

import game.entities.PacGum;
import game.entities.SuperPacGum;
import game.entities.ghosts.Ghost;
import game.ghostStates.FrightenedMode;

import javax.swing.*;
import java.awt.*;

// Panel for the user interface
public class UIPanel extends JPanel implements Observer {
    public static int width;
    public static int height;

    private int score = 0;
    private JLabel scoreLabel;

    public UIPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.black);
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(20.0F));
        scoreLabel.setForeground(Color.white);
        this.add(scoreLabel, BorderLayout.WEST);
    }

    public void updateScore(int incrScore) {
        this.score += incrScore;
        this.scoreLabel.setText("Score: " + score);
    }

    public int getScore() {
        return score;
    }

    // The UI is notified when Pacman is in contact with a Pac-Gum, a Super Pac-Gum or a ghost,
    // and we update the displayed score accordingly
    @Override
    public void updatePacGumEaten(PacGum pg) {
        updateScore(10);
    }

    @Override
    public void updateSuperPacGumEaten(SuperPacGum spg) {
        updateScore(100);
    }

    @Override
    public void updateGhostCollision(Ghost gh) {
        // If Pacman contacts a ghost, we only update the score when the ghost is in "frightened" mode
        if (gh.getState() instanceof FrightenedMode) {
            updateScore(500);
        }
    }
}
