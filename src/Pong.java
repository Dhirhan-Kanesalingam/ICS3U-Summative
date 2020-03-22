package pong;

/*Author : Dhirhan Lanesalingam
*Title : Pong
*Course : IC3SU
*Teacher : Mrs. Torres
 */

 /*INSTRUCTIONS
        W TO MOVE UP FOR PLAYER 1 (LEFT PLAYER)
        S TO MOVE DOWN FOR PLAYER 2 (LEFT PLAYER)
        UP ARROW TO MOVE UP FOR PLAYER 2 (RIGHT PLAYER) IF NOT PLAYING AGAINST BOT
        DOWN ARROW TO MOVW DOWN FOR PLAYER 2 (RIGHT PLAYER) IF NOT PLAING AGAINST BOT
        
        RIGHT & LEFT ARROWS IN MAIN MAIN MENU TO SET SCORE LIMIT
        SPACE BUTTON IN MAIN MENU TO START GAME FOR PLAYER VS PLAYER
        SHIFT IN MAIN MENU TO GO TO MENU FOR PLAYER VS BOT
        
        RIGHT & LEFT ARROWS IN PLAYER VS BOT MENU FOR SELECTING BOT DIFFICULTY
        SPACE BUTTON IN BOT MENU TO START GAME FOR PLAYER VS BOT

        SPACE WHILE IN GAME TO PAUSE GAME, SPACE AGAIN TO UNPAUSE GAME
        ESCAPE WHILE IN GAME TO RETURN TO MAIN MENU
        CANNOT USE ESCAPE KEY WHILE IN PAUSED STATE, AND IN BOT MENU
 */
import java.awt.*;          //Needed for colours, font, graphics etc
import java.awt.event.*;    //Needed for the Action and Key listener and event
import java.util.Random;    //Needed to determine how the ball reacts upon collision with wall
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.util.Scanner;

import java.io.*;

import java.lang.String;
import java.util.Arrays;

public class Pong implements ActionListener, KeyListener {

    public static Pong pong;

    //Initialization of vaiables
    public int width = 700, height = 700;
    public Renderer renderer;
    public Paddle player1;
    public Paddle player2;
    public Ball ball;
    public boolean bot = false, selectingDifficulty;
    public boolean w, s, up, down;
    public int gameStatus = 0, scoreLimit = 7, playerWon; //0 = Menu, 1 = Paused, 2 = Playing, 3 = Over
    public int numHits;
    public int botDifficulty, botMoves, botCooldown = 0;
    public String playerName;
    public static String[][] highScores = new String[10][2];       //For File IO

    public Random random;
    public JFrame jframe;

    //Main contructor
    public Pong() {

        potentialCreateFile();  //If there is no created file, one will be made
                                //If there is one, nothing will happen
        fileReader();           //To fill the array with the contents from the file
        nullRemover(highScores);    //Rids of any accidental nulls

        Timer timer = new Timer(20, this);  //For bot cooldown
        random = new Random();

        jframe = new JFrame("Pong");

        renderer = new Renderer();

        jframe.setSize(width + 15, height + 35);
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(renderer);
        jframe.addKeyListener(this);

        timer.start();

    }

    //To start game
    public void start() {
        gameStatus = 2;
        player1 = new Paddle(this, 1);
        player2 = new Paddle(this, 2);
        ball = new Ball(this);

    }

    public void update() {

        if (player1.score == scoreLimit) {
            player1.score = 0;
            player2.score = 0;
            gameStatus = 4;
            highScoreListSorter();

        } else if (player2.score == scoreLimit) {
            player1.score = 0;
            player2.score = 0;
            gameStatus = 4;
            highScoreListSorter();
        }

        if (w) {
            player1.move(true);
        }
        if (s) {
            player1.move(false);
        }

        if (!bot) {
            if (up) {
                player2.move(true);
            }
            if (down) {
                player2.move(false);
            }
        } else {
            if (botCooldown > 0) {
                botCooldown--;

                if (botCooldown == 0) {
                    botMoves = 0;
                }
            }

            if (botMoves < 10) {
                if (player2.y + player2.height / 2 < ball.y) {
                    player2.move(false);
                    botMoves = 0;
                    botMoves++;
                }

                if (player2.y + player2.height / 2 > ball.y) {
                    player2.move(true);
                    botMoves++;
                }

                if (botDifficulty == 0) {
                    botCooldown = 20;
                }
                if (botDifficulty == 1) {
                    botCooldown = 15;
                }
                if (botDifficulty == 2) {
                    botCooldown = 10;
                }

                if (botDifficulty == 3) {
                    botCooldown = 1;
                }

            }
        }

        ball.update(player1, player2);
    }

    public void render(Graphics2D g) //Drawing out the different game status screens
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        if (gameStatus == 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", 1, 50));

            g.drawString("PONG", width / 2 - 75, 50);

            if (!selectingDifficulty) //Ensuring in game status 0, as there is no bot difficulty present
            {
                g.setFont(new Font("Arial", 1, 30));

                g.drawString("Press Space to Play", width / 2 - 150, height / 2 - 25);
                g.drawString("Press Shift to Play with Bot", width / 2 - 200, height / 2 + 25);
                g.drawString("<< Score Limit: " + scoreLimit + " >>", width / 2 - 150, height / 2 + 75);

            }
        }

        if (selectingDifficulty) {
            String string = botDifficulty == 0 ? "Easy" : botDifficulty == 1 ? "Medium" : (botDifficulty == 2 ? "Hard" : "Impossible");

            g.setFont(new Font("Arial", 1, 30));

            g.drawString("<< Bot Difficulty: " + string + " >>", width / 2 - 180, height / 2 - 25);
            g.drawString("Press Space to Play", width / 2 - 150, height / 2 + 25);
        }

        if (gameStatus == 1) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", 1, 50));
            g.drawString("PAUSED", width / 2 - 103, height / 2 - 25);
        }

        if (gameStatus == 1 || gameStatus == 2) {
            g.setColor(Color.WHITE);

            g.setStroke(new BasicStroke(5f));

            g.drawLine(width / 2, 0, width / 2, height);

            g.setStroke(new BasicStroke(2f));

            g.drawOval(width / 2 - 150, height / 2 - 150, 300, 300);

            g.setFont(new Font("Arial", 1, 50));

            g.drawString(String.valueOf(player1.score), width / 2 - 90, 50);
            g.drawString(String.valueOf(player2.score), width / 2 + 65, 50);

            player1.render(g);      //To set size and colour of paddle 1
            player2.render(g);      //To set size and colour of paddle 2
            ball.render(g);         //To set size and colour of ball
        }

        if (gameStatus == 3) {

        }

        if (gameStatus == 4) {

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", 1, 50));

            g.drawString("PONG", width / 2 - 75, 50);

            if (bot && playerWon == 2) {
                g.drawString("The Bot Wins!", width / 2 - 170, 200);
            } else {
                g.drawString("Player " + playerWon + " Wins!", width / 2 - 165, 200);
            }

            g.setFont(new Font("Arial", 1, 30));

            g.drawString("Press Space to Play Again", width / 2 - 185, height / 2 - 25);
            g.drawString("Press ESC for Menu", width / 2 - 140, height / 2 + 25);

        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStatus == 2 || gameStatus == 3 || gameStatus == 4) {
            update();
        }

        renderer.repaint();         //Will constantly repaint jframe

        if (gameStatus == 3) {
            gameStatus = 4;

        }

    }

    public static void main(String[] args) {
        pong = new Pong();          //Runs Constructor
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int id = e.getKeyCode();    //To get Which key was pressed

        if (id == KeyEvent.VK_W) //Case w
        {
            w = true;
        } else if (id == KeyEvent.VK_S) //Case s
        {
            s = true;
        } else if (id == KeyEvent.VK_UP) //Case up arrow
        {
            up = true;
        } else if (id == KeyEvent.VK_DOWN) //Case down arrow
        {
            down = true;
        } else if (id == KeyEvent.VK_RIGHT) //Case right arrow for difficulty selection 
        {
            if (selectingDifficulty) {
                if (botDifficulty < 3) {

                    botDifficulty++;
                } else {
                    botDifficulty = 0;
                }
            } else if (gameStatus == 0 && scoreLimit < 50) {   //When in initial game status
                //Max score Limit is 50
                {                           //Only thing arrow could be used for is manipulating score limit

                    scoreLimit++;

                }
            }
        } else if (id == KeyEvent.VK_LEFT) {
            if (selectingDifficulty) {
                if (botDifficulty > 0) {
                    botDifficulty--;    //Can only decrease bot difficulty by changing 
                } else {
                    botDifficulty = 3;
                }
            } else if (gameStatus == 0 && scoreLimit > 1) {
                scoreLimit--;

            }
        } else if (id == KeyEvent.VK_ESCAPE && (gameStatus == 2 || gameStatus == 3 || gameStatus == 4)) //Can escape to main screen only if in middle of a game
        {
            selectingDifficulty = false;
            gameStatus = 0;
        } else if (id == KeyEvent.VK_SHIFT && gameStatus == 0) //To select bot mode
        {
            bot = true;
            selectingDifficulty = true;
        } else if (id == KeyEvent.VK_SPACE) {
            switch (gameStatus) {
                case 0:
                case 3:
                case 4:
                    if (!selectingDifficulty) {
                        bot = false;
                    } else {
                        selectingDifficulty = true;
                    }
                    start();
                    break;
                case 1:
                    gameStatus = 2;
                    break;
                case 2:
                    gameStatus = 1;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) //Whenever the key is rleased, being pressed is set to false
    {
        int id = e.getKeyCode();

        switch (id) {
            case KeyEvent.VK_W:
                w = false;
                break;
            case KeyEvent.VK_S:
                s = false;
                break;
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            default:
                break;
        }
    }

    public String getPlayerName() {
        playerName = JOptionPane.showInputDialog(null, "You are in the HighScore List for the Most Total Hits", "Enter Your Name:", JOptionPane.INFORMATION_MESSAGE);
        return playerName;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public void nullRemover(String[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j] == null) {
                    arr[i][j] = "0";
                }
            }
        }
    }

    public void fileOutputter() {
        try {
            File file = new File("HighScore.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter pw = new PrintWriter(file);

            for (int i = 0; i < highScores.length; i++) {
                for (int j = 0; j < highScores[i].length; j++) {
                    pw.println(highScores[i][j]);
                }
            }

            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void potentialCreateFile() {
        try {
            File file = new File("HighScore.txt");

            if (!file.exists()) {
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file);

                System.out.println("Before For Loop Create");

                for (int x = 0; x < 20; x++) {
                    pw.println("0");
                    System.out.println(x);

                }
                pw.close();
                System.out.println("After For Loop Create");

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void highScoreListSorter() {

        numHits = ball.getNumHits();
        String hits = Integer.toString(numHits);
        int i, j, highscoreId = 0;
        playerWon = 1;
        gameStatus = 4;
        player1.score = 0;
        player2.score = 0;

        if (highScores[0][0].compareTo("0") == 0) {
            getPlayerName();
            int OK = JOptionPane.showConfirmDialog(null, "You are in the HighScore List for the Most Total Hits", "", JOptionPane.DEFAULT_OPTION);
            if (OK == JOptionPane.OK_OPTION) {
                gameStatus = 4;
            }

            highScores[0][0] = hits;
            highScores[0][1] = playerName;
        }

        for (i = 0; i < 10; i++) {

            if (hits.compareTo(highScores[i][0]) >= 0) {
                highscoreId = i;
                getPlayerName();
                int OK = JOptionPane.showConfirmDialog(null, "You are in the HighScore List for the Most Total Hits", "", JOptionPane.DEFAULT_OPTION);
                if (OK == JOptionPane.OK_OPTION) {
                    gameStatus = 4;
                }

                for (j = 8; j >= highscoreId; j--) {
                    highScores[j + 1][0] = highScores[j][0];
                    highScores[j + 1][1] = highScores[j][1];
                }

                highScores[highscoreId][0] = hits;
                highScores[highscoreId][1] = playerName;

                fileOutputter();
                System.out.println("Here is the Highscore List for the most amouts of hits in a single game");
                System.out.println("Scores" + "             Names");
                for (int x = 0; x < highScores.length; x++) {
                    String subArray[] = highScores[x];
                    System.out.println();
                    for (int y = 0; y < subArray.length; y++) {
                        String item = subArray[y];
                        System.out.print(item + "                  ");
                    }

                }
                fileOutputter();
                System.exit(0);
            }

        }
        System.exit(0);

    }

    public void fileReader() {

        BufferedReader br = null;
        FileReader fr = null;

        try {
            File file = new File("HighScore.txt");

            if (!file.exists()) {
                file.createNewFile();

            }

            //br = new BufferedReader(new FileReader(FILENAME));
            fr = new FileReader("HighScore.txt");
            br = new BufferedReader(fr);

            String str;

            for (int i = 0; i < highScores.length; i++) {
                for (int j = 0; j < 2; j++) {

                    highScores[i][j] = br.readLine();
                }
            }
            br.close();
            fr.close();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
