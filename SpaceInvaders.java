import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class SpaceInvaders extends JPanel {

    class Block{
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive=true; //for the aliens
        boolean used=false; //for the bullets

        //constructor
        Block(int x,int y,int width,int height,Image img){
            this.x=x;
            this.y=y;
            this.width=width;
            this.height=height;
            this.img=img;
        }

    }

    //board
    int tileSize=32;
    int rows=16;
    int columns=16;
    
    int boardWidth=tileSize*columns; // 32*16=512px
    int boardHeight=tileSize*rows; // 32*16=512px

    Image shipImg;
    Image alienImg;
    Image alienCyanImg;
    Image alienMagentaImg;
    Image alienYellowImg;
    ArrayList<Image> alienImgArray;

    //ship
    int shipWidth=tileSize*2;//64px
    int shipHeight=tileSize;//32px
    int shipX=tileSize*columns/2-tileSize;
    int shipY=boardHeight-tileSize*2;





    SpaceInvaders(){
        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setBackground(new Color(0,0,30));// color manipulation of the background

        shipImg=new ImageIcon(getClass().getResource("./ship.png")).getImage();
        alienImg=new ImageIcon(getClass().getResource("./alien.png")).getImage();
        alienCyanImg=new ImageIcon(getClass().getResource("./alien-cyan.png")).getImage();
        alienMagentaImg=new ImageIcon(getClass().getResource("./alien-magenta.png")).getImage();
        alienYellowImg=new ImageIcon(getClass().getResource("./alien-yellow.png")).getImage();

        alienImgArray=new ArrayList<Image>();
        alienImgArray.add(alienImg);
        alienImgArray.add(alienCyanImg);
        alienImgArray.add(alienMagentaImg);
        alienImgArray.add(alienYellowImg);

        ship=new Block(shipX,shipY,shipWidth,shipHeight,shipImg);

    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.drawImage(ship.img, ship.x,ship.y, ship.width, ship.height, null);
    }

}
