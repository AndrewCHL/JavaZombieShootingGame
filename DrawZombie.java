import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * This class extends thread to create moving zombies heading toward player
 *
 * @author Chen Hsun Lee
 * @pid A53106852
 * @email chl594@ucsd.edu
 */
public class DrawZombie extends Thread {
  private Pane pane; //receive from Final.java
  private final String ZOMBIE_PATH = "Zombie.png";
  private File fZombie;
  private Final game; //receive from Final.java

  //ArrayList to store zombies' positions
  public ArrayList<Double> zomPosX = new ArrayList<Double>();
  public ArrayList<Double> zomPosY = new ArrayList<Double>();
  public ArrayList<Node> zombieList = new ArrayList<Node>();

  private final int DELAY_TIME = 90; //The delay time between each zombies
  private final int MOVE_SPEED = 2; //Zombies' moving speed
  private int delay = 0; //Counter for delay time

  public DrawZombie(Pane pane, Final game) {
    this.game = game;
    this.pane = pane;
    this.game.zomPosX = this.zomPosX;
    this.game.zomPosY = this.zomPosY;
    fZombie = new File(ZOMBIE_PATH);

    //create 5 zombies by default
    for(int i = 0; i<5; ++i){
      createZombie();
    }
    game.zomPosX = zomPosX;
    game.zomPosY = zomPosY;
  }

  /**
   * Helpler method to create a zombie
   */
  private void createZombie() {
    try {
      Random rdm = new Random();
      Image imageZombie = new Image(new FileInputStream(fZombie));
      zombieList.add(new ImageView(imageZombie));

      //To create a random position within this canvas
      zomPosX.add((double) (rdm.nextInt(game.CANVAS_WIDTH -
          (int) imageZombie.getWidth()) + 1));
      zomPosY.add((double) (rdm.nextInt(game.CANVAS_HEIGHT -
          (int) imageZombie.getHeight()) + 1));

      this.pane.getChildren().add(zombieList.get(zombieList.size() - 1));
      zombieList.get(zombieList.size() - 1).relocate(
          zomPosX.get(zomPosX.size() - 1), zomPosY.get(zomPosY.size() - 1));

    } catch (FileNotFoundException e) {
    }

  }

  /**
   * The helpler method to kill zombie by reading info from Final.java
   */
  public void killObject(){
    zomPosX.remove(this.game.killZombieIndex);
    zomPosY.remove(this.game.killZombieIndex);
    this.pane.getChildren().remove(zombieList.get(this.game.killZombieIndex));
    zombieList.remove(this.game.killZombieIndex);
  }

  /**
   * The helpler method to move zombies
   */
  private void moveTo() {
    for (int i = 0; i < zombieList.size(); ++i) {

      // To make dx and dy toward player
      double dx = game.heroPosiX -
          (zombieList.get(i).getBoundsInLocal().getWidth() / 2
          + zombieList.get(i).getLayoutX());
      double dy = game.heroPosiY -
          (zombieList.get(i).getBoundsInLocal().getHeight() / 2
              + zombieList.get(i).getLayoutY());

      // Use Point2D to normalize dx and dy
      Point2D vector1 = new Point2D(dx, dy);
      zomPosX.set(i, new Double(zomPosX.get(i)
          + vector1.normalize().getX()*MOVE_SPEED));
      zomPosY.set(i, new Double(zomPosY.get(i)
          + vector1.normalize().getY()*MOVE_SPEED));
      zombieList.get(i).relocate(zomPosX.get(i), zomPosY.get(i));
    }
  }

  /**
   * the main part of this class including the animation timer
   */
  public void run() {
    AnimationTimer timerZombie = new AnimationTimer() {
      @Override
      public void handle(long now) {
        //Read in the sign provided by Final.java whether to kill a zombie
        if(game.killZombie){
          killObject();
          game.currentScore += 100;
          game.scoreCurrent.setText("Score: "
              + Integer.toString(game.currentScore));
          
          // Signal to kill the corresponding bullet
          game.killBullet = true;
          game.killZombie = false;
        }
        
        moveTo(); //Move a zombie
        
        //Create two zombie with a given delay time
        delay += 1;
        if (delay == DELAY_TIME) {
          delay = 0;
          createZombie();
          createZombie();
        }
      }
    };
    timerZombie.start();
  }
}