package com.fishinspace;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {

    class Block {
        int x;
        int y;
        int width;
        int height;
        boolean alive = true;
        Color color;

        Block(int x, int y, int width, int height, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    // Board dimensions
    int tileSize = 32;
    int rows = 16;
    int columns = 16;
    int boardWidth = tileSize * columns;
    int boardHeight = tileSize * rows;

    // Ship properties
    int shipWidth = tileSize * 2;
    int shipHeight = tileSize;
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = boardHeight - tileSize * 2;
    int shipVelocity = tileSize;
    Block ship;

    // Aliens
    ArrayList<Block> alienArray;
    int alienWidth = tileSize * 2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = tileSize;
    int alienRows = 4;
    int alienColumns = 6;
    int alienCount = 0;
    int alienVelocityX = 1;

    // Bullets
    ArrayList<Block> bulletArray;
    int bulletWidth = tileSize / 8;
    int bulletHeight = tileSize / 2;
    int bulletVelocity = -10;

    // Game state
    Timer gameLoop;
    int score = 0;
    boolean gameOver = false;

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Create ship - TODO: Replace with spaceship sprite (64x32px, white/silver color)
        ship = new Block(shipX, shipY, shipWidth, shipHeight, Color.WHITE);

        // Create aliens
        alienArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();
        createAliens();

        // Game loop - 60 FPS
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    public void createAliens() {
        alienArray.clear();
        for (int r = 0; r < alienRows; r++) {
            for (int c = 0; c < alienColumns; c++) {
                int x = alienX + c * alienWidth;
                int y = alienY + r * alienHeight;
                // TODO: Replace colors with alien sprites (64x32px)
                // Row 0: Yellow aliens (top row, worth most points)
                // Row 1: Magenta aliens
                // Row 2: Cyan aliens  
                // Row 3: Green aliens (bottom row, worth least points)
                Color alienColor;
                if (r == 0) {
                    alienColor = Color.YELLOW;
                } else if (r == 1) {
                    alienColor = Color.MAGENTA;
                } else if (r == 2) {
                    alienColor = Color.CYAN;
                } else {
                    alienColor = Color.GREEN;
                }
                Block alien = new Block(x, y, alienWidth, alienHeight, alienColor);
                alienArray.add(alien);
            }
        }
        alienCount = alienArray.size();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw ship
        g.setColor(ship.color);
        g.fillRect(ship.x, ship.y, ship.width, ship.height);

        // Draw aliens
        for (Block alien : alienArray) {
            if (alien.alive) {
                g.setColor(alien.color);
                g.fillRect(alien.x, alien.y, alien.width, alien.height);
            }
        }

        // Draw bullets
        g.setColor(Color.WHITE);
        for (Block bullet : bulletArray) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        if (gameOver) {
            g.drawString("Game Over: " + score, 10, 30);
        } else {
            g.drawString("Score: " + score, 10, 30);
        }
    }

    public void move() {
        // Move aliens
        for (Block alien : alienArray) {
            if (alien.alive) {
                alien.x += alienVelocityX;

                // Check if aliens hit the edges
                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienVelocityX *= -1;
                    // Move all aliens down
                    for (Block a : alienArray) {
                        a.y += alienHeight;
                    }
                }

                // Check if aliens reached the ship
                if (alien.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        // Move bullets
        for (int i = 0; i < bulletArray.size(); i++) {
            Block bullet = bulletArray.get(i);
            bullet.y += bulletVelocity;

            // Check bullet collision with aliens
            for (Block alien : alienArray) {
                if (!bullet.alive && alien.alive && detectCollision(bullet, alien)) {
                    bullet.alive = false;
                    alien.alive = false;
                    alienCount--;
                    score += 100;
                }
            }
        }

        // Clear bullets that are off screen
        while (bulletArray.size() > 0 && (bulletArray.get(0).y < 0 || !bulletArray.get(0).alive)) {
            bulletArray.remove(0);
        }

        // Check if all aliens are defeated
        if (alienCount == 0) {
            score += alienColumns * alienRows * 100;
            alienColumns = Math.min(alienColumns + 1, columns / 2 - 2);
            alienRows = Math.min(alienRows + 1, rows - 6);
            alienVelocityX += alienVelocityX > 0 ? 1 : -1;
            alienArray.clear();
            bulletArray.clear();
            createAliens();
        }
    }

    public boolean detectCollision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            return;
        }

        // Move ship left
        if (e.getKeyCode() == KeyEvent.VK_LEFT && ship.x > 0) {
            ship.x -= shipVelocity;
        }
        // Move ship right
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship.x + shipWidth < boardWidth) {
            ship.x += shipVelocity;
        }
        // Shoot bullet
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            Block bullet = new Block(ship.x + shipWidth / 2 - bulletWidth / 2, ship.y, bulletWidth, bulletHeight, Color.WHITE);
            bulletArray.add(bullet);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
