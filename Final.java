import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This program use Javafx to make a zombie shooting game. Use w, s, a, d to
 * move up, down, left, and right. Use mouse to change direction of shooting.
 *
 * compile: javac Final.java
 * run: java Final
 *
 * @author Chen Hsun Lee
 * @pid A53106852
 * @email chl594@ucsd.edu
 */
public class Final extends Application {
  private static final String BACK_G_PATH = "Background.jpg";
  private final String HERO_PATH = "Hero.png";
  private Node myHero; //instance of player
  public final int CANVAS_WIDTH = 1280;
  public final int CANVAS_HEIGHT = 720;
  public double heroPosiX = 565;
  public double heroPosiY = 245;
  private final int heroSpeed = 10; //The speed that hero moves
  private boolean goNorth, goSouth, goEast, goWest;
  public ArrayList<Double> zomPosX = new ArrayList<Double>();
  public ArrayList<Double> zomPosY = new ArrayList<Double>();
  public ArrayList<Double> bulletPosX = new ArrayList<Double>();
  public ArrayList<Double> bulletPosY = new ArrayList<Double>();

  //Use to decide whether to kill bullet and zombie when they touch each other
  public boolean killZombie = false;
  public boolean killBullet = false;
  public int killZombieIndex = 0;
  public int killBulletIndex = 0;

  public int currentScore = 0;
  public Label scoreCurrent = null;
  public Label heroLifeLabel = null;
  public int heroLife = 5; //Default life of hero
  public int bestScore = 0;
  public Label scoreBest = null;
  public Label endText;

  //Use to make a buffer when hero touch zombies
  public boolean startCounterDie = false;
  public int counterDie = 0;


  /**
   * To start stage of UI
   * @param primaryStage the starting stage
   * @throws Exception throw IOException
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    File fBg = new File(BACK_G_PATH);

    //The last and ending scene
    Pane pane2 = new Pane();
    Scene scene2 = new Scene(pane2);
    Image imageBg2 = new Image(new FileInputStream(fBg));
    ImageView bg2 = new ImageView(imageBg2);
    pane2.getChildren().add(bg2);

    endText = new Label("Game Over");
    endText.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size: 100px;" +
        "-fx-font-weight: bold;");
    VBox finalPane = new VBox();
    finalPane.setStyle("-fx-background-color: #ECCE78;" +
        "-fx-border-color:#8E7A67;-fx-border-width:10px");
    finalPane.getChildren().add(endText);
    finalPane.relocate(CANVAS_WIDTH / 2 - 300, 250);
    pane2.getChildren().add(finalPane);

    //The first and main scene
    Pane pane = new Pane();
    Scene scene = new Scene(pane);
    Image imageBg = new Image(new FileInputStream(fBg));
    ImageView bg = new ImageView(imageBg);
    pane.getChildren().add(bg);

    //To show score bar in the top middle positoin
    scoreCurrent = new Label("Score: " + currentScore);
    scoreCurrent.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size: 25px;" +
        "-fx-font-weight: bold;");
    heroLifeLabel = new Label("     Life: " + heroLife);
    heroLifeLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size: 25px;" +
        "-fx-font-weight: bold;");
    scoreBest = new Label("     Best: " + bestScore);
    scoreBest.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size: 25px;" +
        "-fx-font-weight: bold;");
    HBox scorePane = new HBox();
    scorePane.setStyle("-fx-background-color: #ECCE78;" +
        "-fx-border-color:#8E7A67;-fx-border-width:5px");
    scorePane.getChildren().add(scoreCurrent);
    scorePane.getChildren().add(scoreBest);
    scorePane.getChildren().add(heroLifeLabel);
    scorePane.relocate(CANVAS_WIDTH / 2 - 200, 30);
    scorePane.setMinWidth(200);
    pane.getChildren().add(scorePane);

    updateBestScore(); //update the best score before starting

    //create a hero object and relocate to the middle position
    Image imageHero = new Image(new FileInputStream(HERO_PATH));
    myHero = new ImageView(imageHero);
    pane.getChildren().add(myHero);
    myHero.relocate(heroPosiX, heroPosiY);

    //Show an alert of instruction of how to play
    Alert alertStart = new Alert(AlertType.NONE, "Controls:\nW - Move Up\n" +
        "S - Move Down\nA - Move Left\nD - Move Right\n" +
        "Mouse - Move around to change direction of shooting",
        new ButtonType("Play"));
    alertStart.showAndWait();

    //create a thread of moving zombie
    Thread zombie = new DrawZombie(pane, this);
    zombie.start();

    //create a thread of moving bullet
    Thread bullet = new DrawBullet(pane, this);
    bullet.start();

    primaryStage.setTitle("Zombie Shooting Game");
    primaryStage.setScene(scene);
    primaryStage.show();

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        switch (event.getCode()) {
          case W:
            goNorth = true;
            break;
          case S:
            goSouth = true;
            break;
          case A:
            goWest = true;
            break;
          case D:
            goEast = true;
            break;
          default:
            break;
        }
      }
    });

    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        switch (event.getCode()) {
          case W:
            goNorth = false;
            break;
          case S:
            goSouth = false;
            break;
          case A:
            goWest = false;
            break;
          case D:
            goEast = false;
            break;
          default:
            break;
        }
      }
    });

    AnimationTimer timerHero = new AnimationTimer() {
      @Override
      public void handle(long now) {
        int incrementX = 0;
        int incrementY = 0;
        if (goNorth) {
          incrementY -= heroSpeed;
        }
        if (goSouth) {
          incrementY += heroSpeed;
        }
        if (goEast) {
          incrementX += heroSpeed;
        }
        if (goWest) {
          incrementX -= heroSpeed;
        }
        //move hero in every frame
        moveHeroBy(incrementX, incrementY);

        //check whether bullet touches zombie in every frame
        checkKillZombie();

        //to create a one minute buffer before next deduction of life
        if (startCounterDie) {
          counterDie += 1;
          if (counterDie == 60) {
            startCounterDie = false;
            counterDie = 0;
          }
        } else {
          checkHeroLife();
        }

        //To show ending scene when hero die
        if (heroLife <= 0) {
          primaryStage.setScene(scene2);
          updateBestScore(); //Update the best score when hero dies
        }
      }
    };
    timerHero.start();
  }

  /**
   * To check whether hero touches zombie and deduce life
   */
  private void checkHeroLife() {
    for (int i = 0; i < zomPosX.size(); ++i) {
      if (zomPosX.get(i) >= heroPosiX - 100 &&
          zomPosX.get(i) <= heroPosiX + 50 &&
          zomPosY.get(i) >= heroPosiY - 100 &&
          zomPosY.get(i) <= heroPosiY + 50) {
        heroLife -= 1;
        startCounterDie = true; //Start the buffer before next deduction
      }
    }
    heroLifeLabel.setText("     Life: " + heroLife); //Update hero's life
  }

  /**
   * To check whether bullet touches zombie and store signs in static variables
   * to let show DrawZombie class whether to kill object
   */
  private void checkKillZombie() {
    for (int i = 0; i < zomPosX.size(); ++i) {
      for (int j = 0; j < bulletPosX.size(); ++j) {
        if (zomPosX.get(i) >= bulletPosX.get(j) - 100 &&
            zomPosX.get(i) <= bulletPosX.get(j) + 50 &&
            zomPosY.get(i) >= bulletPosY.get(j) - 100 &&
            zomPosY.get(i) <= bulletPosY.get(j) + 50) {
          killZombie = true;
          killZombieIndex = i;
          killBulletIndex = j;
        }
      }
    }
  }

  /**
   * Helpler function to change delta into position to move
   *
   * @param incrementX send in delta X to move
   * @param incrementY send in delta Y to move
   */
  private void moveHeroBy(int incrementX, int incrementY) {
    if (incrementX == 0 && incrementY == 0) {
      return;
    }

    double cx = myHero.getBoundsInLocal().getWidth() / 2;
    double cy = myHero.getBoundsInLocal().getHeight() / 2;

    double x = cx + myHero.getLayoutX() + incrementX;
    double y = cy + myHero.getLayoutY() + incrementY;

    moveHeroTo(x, y);
  }

  /**
   * To let player move to the designated positon
   * @param x send in the position x to move to
   * @param y send in the position y to move to
   */
  private void moveHeroTo(double x, double y) {
    double cx = myHero.getBoundsInLocal().getWidth() / 2;
    double cy = myHero.getBoundsInLocal().getHeight() / 2;

    if (x - cx >= 0 && x + cx <= CANVAS_WIDTH &&
        y - cy >= 0 && y + cy <= CANVAS_HEIGHT) {
      myHero.relocate(x - cx, y - cy);
      heroPosiX = x;
      heroPosiY = y;
    }
  }

  /**
   * To read in saving file and update best score
   */
  public void updateBestScore() {
    try{
      int input = 0;
      FileInputStream f = new FileInputStream("SavingFile.txt");
      Scanner scnr = new Scanner(f);
      input = scnr.nextInt();
      scnr.close();
      if(input < currentScore){
        bestScore = currentScore;
        PrintWriter pw = new PrintWriter("SavingFile.txt");
        pw.print(bestScore);
        pw.close();
      } else if(input > currentScore){
        bestScore = input;
      }

    } catch(IOException e){
      try{
        PrintWriter pw = new PrintWriter("SavingFile.txt");
        pw.print("0");
        pw.close();
      } catch(Exception e1){}
    }
    scoreBest.setText("     Best: " + bestScore);
  }

  /**
   * The starting of this program
   *
   * @param args string array command line argument
   */
  public static void main(String[] args) {
    launch(args);
  }
}
