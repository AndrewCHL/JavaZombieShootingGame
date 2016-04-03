import java.awt.MouseInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * This class extends thread to create moving bullet directed to the mouse
 * position when it's created
 *
 * @author Chen Hsun Lee
 * @pid A53106852
 * @email chl594@ucsd.edu
 */
public class DrawBullet extends Thread {
  private Pane pane; //receive from Final.java
  private final String BULLET_PATH = "Poop.png";
  private File fBullet;
  private Final game; //receive from Final.java

  //ArrayList to store bullet's position and direction to move
  public ArrayList<Double> bulletPosX = new ArrayList<Double>();
  public ArrayList<Double> bulletPosY = new ArrayList<Double>();
  public ArrayList<Double> bulletDX = new ArrayList<Double>();
  public ArrayList<Double> bulletDY = new ArrayList<Double>();

  //ArrayList to store bullet's nodes
  public ArrayList<Node> bulletList = new ArrayList<Node>();

  private final int DELAY_TIME = 30; //The delay time between each bullets
  private final int bulletSpeed = 10; //Bullet's speed
  private int delay = 0; //Counter for delay time

  public DrawBullet(Pane pane, Final game) {
    this.game = game;
    this.pane = pane;
    fBullet = new File(BULLET_PATH);
    this.game.bulletPosX = this.bulletPosX;
    this.game.bulletPosY = this.bulletPosY;
  }

  /**
   * Helpler method to create a bullet
   */
  private void createBullet() {
    try {
      Image imageBullet = new Image(new FileInputStream(fBullet));
      bulletList.add(new ImageView(imageBullet));
      bulletPosX.add(game.heroPosiX);
      bulletPosY.add(game.heroPosiY);

      //Read in mouse's position to decide the direction to move
      double dx = MouseInfo.getPointerInfo().getLocation().getX()
          - (game.heroPosiX);
      double dy = MouseInfo.getPointerInfo().getLocation().getY()
          - (game.heroPosiY);

      //Use Point2D to normalize dx and dy
      Point2D vector = new Point2D(dx, dy);
      bulletDX.add(new Double(vector.normalize().getX()*bulletSpeed));
      bulletDY.add(new Double(vector.normalize().getY()*bulletSpeed));

      //put a bullet on the pane
      this.pane.getChildren().add(bulletList.get(bulletList.size() - 1));
      bulletList.get(bulletList.size() - 1).relocate(game.heroPosiX, 
          game.heroPosiY);

    } catch (FileNotFoundException e) {
    }
  }

  /**
   * The helpler method to move bullets
   */
  private void moveTo() {
    for (int i = 0; i < bulletList.size(); ++i) {
      bulletPosX.set(i, new Double(bulletPosX.get(i) + bulletDX.get(i)));
      bulletPosY.set(i, new Double(bulletPosY.get(i) + bulletDY.get(i)));
      bulletList.get(i).relocate(bulletPosX.get(i), bulletPosY.get(i));
    }
  }

  /**
   * The helpler method to kill bullet by reading info from Final.java
   */
  public void killObject(){
    bulletPosX.remove(this.game.killBulletIndex);
    bulletPosY.remove(this.game.killBulletIndex);
    bulletDX.remove(this.game.killBulletIndex);
    bulletDY.remove(this.game.killBulletIndex);
    this.pane.getChildren().remove(bulletList.get(this.game.killBulletIndex));
    bulletList.remove(this.game.killBulletIndex);
  }

  /**
   * The helpler method to kill bullet when any one of the bullet exceed the
   * boundary of this stage
   * @param i the index of object to be killed
   */
  public void killObject(int i){
    bulletPosX.remove(i);
    bulletPosY.remove(i);
    bulletDX.remove(i);
    bulletDY.remove(i);
    this.pane.getChildren().remove(bulletList.get(i));
    bulletList.remove(i);
  }

  /**
   * the main part of this class including the animation timer
   */
  public void run() {
    AnimationTimer timerBullet = new AnimationTimer() {
      @Override
      public void handle(long now) {
        //Read in the sign provided by Final.java whether to kill a bullet
        if(game.killBullet){
          killObject();
          game.killBullet = false;
        }

        //To check whether any one of bullet exceed the boundary of this stage
        for(int i=0; i<bulletList.size();++i){
          if(bulletPosX.get(i) <=-20 ||
              bulletPosX.get(i) >= game.CANVAS_WIDTH||
              bulletPosY.get(i) <=-20 ||
              bulletPosY.get(i) >= game.CANVAS_HEIGHT
              ){
            killObject(i); //kill bullet
          }
        }
        moveTo(); //Move bullet

        //Create bullet with a given delay time
        delay += 1;
        if (delay == DELAY_TIME) {
          delay = 0;
          createBullet();
        }
      }
    };
    timerBullet.start();
  }
}