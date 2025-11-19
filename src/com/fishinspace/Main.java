package com.fishinspace;

import javax.swing.*;

public class Main {

    public static void main(String[] args){
        // window variables
        int tileSize=32;
        int rows=16;
        int columns=16;
        int boardWidth=tileSize*columns; // 32*16=512px
        int boardHeight=tileSize*rows; // 32*16=512px

        JFrame frame=new JFrame("space invaders");
        //frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false); // change this later to see what the fuck happens.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SpaceInvaders spaceInvaders=new SpaceInvaders();
        frame.add(spaceInvaders);
        frame.pack();
        frame.setVisible(true);






    }
}
