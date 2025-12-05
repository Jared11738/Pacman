<h1 align="center">
  <br>
  <a href=""><img src="https://i.pinimg.com/originals/b4/ee/c4/b4eec4d093adbe9d8a3cbb40d024836a.png" width="450"></a>
  <br>
  <br>
</h1>

## Summary

This implementation of Pacman introduces different behaviors for each ghost by giving their own targeting strategy and its own pathfinding algorithm. Blinky uses a full Breadth-First Search pathfinder that converts the map into an 8-pixel tile grid, marks walls as blocked, and computes the shortest path to Pacman by exploring neighbors by layer. Pinky uses a predictive chase method that targets a point several spots ahead of Pacman’s current movement direction, making the behavior feel more anticipatory and aggressive. Inky uses a hybrid system that switches between A* when it is far from Pacman and BFS when it is close. This allows faster long distance navigation while maintaining accurate movement in tighter areas. Clyde follows classic Pacman behavior by chasing Pacman only when far away and retreating to a scatter corner when nearby. The strengths of this design are that it produces intelligent  ghost movements that are aware of the grid. It also avoids walking through walls, and allows a unique strategy for each ghost for Pacman to avoid. The main limitations are that grid-based BFS and A* can be computationally expensive on larger maps, requires maintaining accurate wall grids, and can cause movement to feel robotic compared to smoother vector-based chasing.
___
## Setup

Run GameLauncher.java
