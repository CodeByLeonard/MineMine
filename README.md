# Minesweeper in Minecraft

This PaperMC Plugin allows to play Minesweeper in Minecraft.

The game is launched by creating a structure as seen in the first screenshot.
The bottom block is ***TNT***, the middle one is ***Smooth Stone*** and the top one has to be
a ***Heavy Weighted Pressure Plate*** (an Iron Pressure Plate).

<div style="text-align: center;">
  <!--suppress CheckImageSize -->
  <img src="img/spawn-structure.png" alt="Spawn Structure" width="400">
</div>

That then spawns a field...
<div style="text-align: center;">
  <!--suppress CheckImageSize -->
  <img src="img/field.png" alt="Generated Field" width="400">
</div>

You can interact with the field by moving onto a pressure plate and
by either dropping (preferred) or placing the flag that you have received
upon game creation onto a pressure plate.
Whenever you place a flag, the boss bar goes down, indicating how many mines
still remain.
Notably, the bar goes down regardless of whether you correctly flagged the location.
<div style="text-align: center;">
  <!--suppress CheckImageSize -->
  <img src="img/player-ui.png" alt="Player UI" width="400">
  <!--suppress CheckImageSize -->
  <img src="img/flag.png" alt="Flag" width="400">
</div>

Lastly, the game automatically ends whenever you have either flagged all mines
or if you have uncovered all non-mines.
<div style="text-align: center;">
  <!--suppress CheckImageSize -->
  <img src="img/end-game.png" alt="Generated Field" width="400">
</div>