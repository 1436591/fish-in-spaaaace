package com.fishinspace;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * AsteroidDestroyer.java
 *
 * A complete, single-file Asteroid Destroyer game created with Java Swing.
 *
 * --- CONTROLS ---
 * Arrow Up:    Thrust
 * Arrow Down:  Brake (with BOOSTER powerup)
 * Arrow Left:  Rotate Left
 * Arrow Right: Rotate Right
 * Spacebar:    Fire Bullet
 * R Key:       Restart Game (after Game Over)
 *
 * Compile: javac AsteroidDestroyer.java
 * Run:     java AsteroidDestroyer
 */
public class AsteroidDestroyer {

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Asteroid Destroyer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Add the main game panel
            frame.add(new GamePanel());

            frame.pack(); // Size the window to fit the preferred size of GamePanel
            frame.setLocationRelativeTo(null); // Center the window on the screen
            frame.setVisible(true);
        });
    }
}/**
 * GamePanel Class
 *
 * This class handles all the game logic, rendering, and player input.
 * It's a JPanel that lives inside the main JFrame.
 */
class GamePanel extends JPanel implements ActionListener, KeyListener {

    // --- Game Constants ---
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int SHIP_SIZE = 20;
    private static final double SHIP_TURN_SPEED = 0.05; // in radians
    private static final double SHIP_THRUST_POWER = 0.1;
    private static final double SHIP_DRAG = 0.98; // Friction
    private static final int BULLET_SPEED = 7;
    private static final int BULLET_COOLDOWN = 15; // Frames between shots
    private static final int ASTEROID_INIT_COUNT = 5;
    private static final int ASTEROID_MAX_SPEED = 2;
    private static final int ASTEROID_SIZE_LARGE = 60;
    private static final int ASTEROID_SIZE_MEDIUM = 30;
    private static final int ASTEROID_SIZE_SMALL = 15;
    private static final int SCORE_LARGE_ASTEROID = 20;
    private static final int SCORE_MEDIUM_ASTEROID = 50;
    private static final int SCORE_SMALL_ASTEROID = 100;

    // --- PowerUp Constants ---
    private static final int POWERUP_SIZE = 20;
    private static final int POWERUP_DURATION = 1200; // 20 seconds at 60 FPS
    private static final int POWERUP_DROP_MIN = 5; // Min asteroids before drop
    private static final int POWERUP_DROP_MAX = 10; // Max asteroids before drop

    // PowerUp types
    enum PowerUpType {
        AIM_BEAM,      // Shows dotted aim line
        DOUBLE_SHOT,   // Fires two bullets
        BOOSTER,       // Faster movement + brake ability
        RAPID_FIRE     // Shoots 1.5x faster
    }

    // --- Game State Variables ---
    private Timer gameTimer;
    private Random random;
    private boolean inGame;
    private int score;

    // PowerUp tracking
    private List<PowerUp> powerUps;
    private int asteroidsDestroyedSinceLastPowerUp;
    private int asteroidsUntilNextPowerUp;
    private PowerUpType activePowerUp;
    private int powerUpTimeRemaining;

    // --- Player/Ship Variables ---
    private double shipX, shipY;       // Ship's center coordinates
    private double shipVelX, shipVelY; // Ship's velocity
    private double shipAngle;          // Ship's rotation angle in radians
    private Polygon shipShape;         // The shape of the ship

    // --- Input Flags ---
    private boolean rotatingLeft;
    private boolean rotatingRight;
    private boolean thrusting;
    private boolean braking;

    // --- Game Object Lists ---
    private List<Bullet> bullets;
    private List<Asteroid> asteroids;
    private int bulletCooldownTimer;
    /**
     * Constructor for GamePanel.
     * Sets up the panel, initializes game state, and starts the game loop.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true); // Required to receive key events

        random = new Random();
        bullets = new ArrayList<>();
        asteroids = new ArrayList<>();
        powerUps = new ArrayList<>();

        // Add the key listener to this panel
        addKeyListener(this);

        // Initialize and start the game
        initGame();
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    /**
     * Initializes or restarts the game.
     * Resets all game state variables to their defaults.
     */
    private void initGame() {
        shipX = PANEL_WIDTH / 2.0;
        shipY = PANEL_HEIGHT / 2.0;
        shipVelX = 0;
        shipVelY = 0;
        shipAngle = -Math.PI / 2; // Point "up"

        rotatingLeft = false;
        rotatingRight = false;
        thrusting = false;

        bullets.clear();
        asteroids.clear();
        powerUps.clear();

        // Create the initial set of large asteroids
        for (int i = 0; i < ASTEROID_INIT_COUNT; i++) {
            spawnAsteroid(ASTEROID_SIZE_LARGE);
        }

        score = 0;
        inGame = true;
        bulletCooldownTimer = 0;

        // PowerUp system
        asteroidsDestroyedSinceLastPowerUp = 0;
        asteroidsUntilNextPowerUp = POWERUP_DROP_MIN + random.nextInt(POWERUP_DROP_MAX - POWERUP_DROP_MIN + 1);
        activePowerUp = null;
        powerUpTimeRemaining = 0;
        braking = false;

        // Restart timer if it was stopped
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }
    /**
     * Spawns a new asteroid of a
     * given size, at a random edge location.
     */
    private void spawnAsteroid(int size) {
        double x, y;
        double angle = random.nextDouble() * 2 * Math.PI;
        double speed = (random.nextDouble() * (ASTEROID_MAX_SPEED - 1)) + 1;

        // Pick a random edge to spawn from (0=top, 1=right, 2=bottom, 3=left)
        int edge = random.nextInt(4);
        if (edge == 0) { // Top
            x = random.nextDouble() * PANEL_WIDTH;
            y = -size / 2.0;
        } else if (edge == 1) { // Right
            x = PANEL_WIDTH + size / 2.0;
            y = random.nextDouble() * PANEL_HEIGHT;
        } else if (edge == 2) { // Bottom
            x = random.nextDouble() * PANEL_WIDTH;
            y = PANEL_HEIGHT + size / 2.0;
        } else { // Left
            x = -size / 2.0;
            y = random.nextDouble() * PANEL_HEIGHT;
        }

        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;

        asteroids.add(new Asteroid(x, y, dx, dy, size));
    }
    /**
     * The main game loop, called by the Timer every ~16ms.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            updateGame();
        }
        repaint(); // Request a redraw of the panel
    }
    /**
     * Groups all game state update logic.
     */
    private void updateGame() {
        updateShip();
        updateBullets();
        updateAsteroids();
        updatePowerUps();
        checkCollisions();

        // Spawn more asteroids if needed
        if (asteroids.isEmpty()) {
            spawnAsteroid(ASTEROID_SIZE_LARGE);
            spawnAsteroid(ASTEROID_SIZE_LARGE);
        }

        // Manage bullet cooldown
        if (bulletCooldownTimer > 0) {
            bulletCooldownTimer--;
        }

        // Manage active powerup duration
        if (powerUpTimeRemaining > 0) {
            powerUpTimeRemaining--;
            if (powerUpTimeRemaining == 0) {
                activePowerUp = null; // PowerUp expired
            }
        }
    }
    /**
     * Updates the ship's position, angle, and velocity based on input flags.
     */
    private void updateShip() {
        // Rotation
        if (rotatingLeft) {
            shipAngle -= SHIP_TURN_SPEED;
        }
        if (rotatingRight) {
            shipAngle += SHIP_TURN_SPEED;
        }

        // Determine thrust power (boosted if BOOSTER powerup active)
        double thrustPower = SHIP_THRUST_POWER;
        if (activePowerUp == PowerUpType.BOOSTER) {
            thrustPower *= 1.5; // 50% faster
        }

        // Thrust
        if (thrusting) {
            shipVelX += Math.cos(shipAngle) * thrustPower;
            shipVelY += Math.sin(shipAngle) * thrustPower;
        }

        // Braking (only available with BOOSTER powerup)
        if (braking && activePowerUp == PowerUpType.BOOSTER) {
            shipVelX *= 0.9; // Rapid deceleration
            shipVelY *= 0.9;
        }

        // Apply friction/drag
        shipVelX *= SHIP_DRAG;
        shipVelY *= SHIP_DRAG;

        // Update position
        shipX += shipVelX;
        shipY += shipVelY;

        // Screen wrapping (teleport to other side)
        wrapCoordinates(shipX, shipY, (newCoords) -> {
            shipX = newCoords[0];
            shipY = newCoords[1];
        });
    }
    /**
     * Updates all bullets' positions and removes them if they go off-screen.
     */
    private void updateBullets() {
        // Iterate backwards to allow safe removal
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();
            if (b.x < 0 || b.x > PANEL_WIDTH || b.y < 0 || b.y > PANEL_HEIGHT) {
                bullets.remove(i);
            }
        }
    }
    /**
     * Updates all asteroids' positions.
     */
    private void updateAsteroids() {
        for (Asteroid a : asteroids) {
            a.move();
            // Screen wrapping
            wrapCoordinates(a.x, a.y, (newCoords) -> {
                a.x = newCoords[0];
                a.y = newCoords[1];
            });
        }
    }

    /**
     * Updates all powerups and checks for collection.
     */
    private void updatePowerUps() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update();

            // Check if ship collected the powerup
            double dist = Math.sqrt(Math.pow(shipX - p.x, 2) + Math.pow(shipY - p.y, 2));
            if (dist < (SHIP_SIZE + POWERUP_SIZE) / 2.0) {
                activePowerUp = p.type;
                powerUpTimeRemaining = POWERUP_DURATION;
                powerUps.remove(i);
            }
        }
    }
    /**
     * Helper functional interface for the wrapCoordinates method.
     */
    @FunctionalInterface
    private interface CoordinateWrapper {
        void apply(double[] newCoords);
    }
    /**
     * Handles screen wrapping logic for any game object.
     */
    private void wrapCoordinates(double x, double y, CoordinateWrapper wrapper) {
        if (x < 0) x = PANEL_WIDTH;
        if (x > PANEL_WIDTH) x = 0;
        if (y < 0) y = PANEL_HEIGHT;
        if (y > PANEL_HEIGHT) y = 0;
        wrapper.apply(new double[]{x, y});
    }
    /**
     * Fires a bullet from the ship's nose.
     */
    private void fireBullet() {
        // Determine cooldown (faster with RAPID_FIRE)
        int cooldown = BULLET_COOLDOWN;
        if (activePowerUp == PowerUpType.RAPID_FIRE) {
            cooldown = (int) (BULLET_COOLDOWN / 1.5); // 1.5x faster
        }

        if (bulletCooldownTimer <= 0) {
            double dx = Math.cos(shipAngle) * BULLET_SPEED;
            double dy = Math.sin(shipAngle) * BULLET_SPEED;

            if (activePowerUp == PowerUpType.DOUBLE_SHOT) {
                // Fire two bullets, slightly offset
                double offsetAngle = Math.PI / 16; // Small angle offset
                double leftAngle = shipAngle - offsetAngle;
                double rightAngle = shipAngle + offsetAngle;

                bullets.add(new Bullet(shipX, shipY, Math.cos(leftAngle) * BULLET_SPEED, Math.sin(leftAngle) * BULLET_SPEED));
                bullets.add(new Bullet(shipX, shipY, Math.cos(rightAngle) * BULLET_SPEED, Math.sin(rightAngle) * BULLET_SPEED));
            } else {
                // Fire single bullet
                bullets.add(new Bullet(shipX, shipY, dx, dy));
            }

            bulletCooldownTimer = cooldown; // Reset cooldown
        }
    }
    /**
     * Checks for collisions between all game objects.
     */
    private void checkCollisions() {
        // Check Bullets vs Asteroids
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid a = asteroids.get(j);

                // Simple distance-based collision check
                double dist = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
                if (dist < a.size / 2.0) {
                    bullets.remove(i); // Remove bullet
                    splitAsteroid(j);  // Split or remove asteroid
                    break; // Bullet is gone, no need to check against other asteroids
                }
            }
        }

        // Check Ship vs Asteroids
        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid a = asteroids.get(i);

            // Simple distance-based collision check
            double dist = Math.sqrt(Math.pow(shipX - a.x, 2) + Math.pow(shipY - a.y, 2));
            if (dist < (a.size / 2.0) + (SHIP_SIZE / 2.0)) {
                inGame = false; // Game Over
                gameTimer.stop();
                break;
            }
        }
    }
    /**
     * Splits an asteroid into smaller pieces or removes it.
     * @param asteroidIndex The index of the asteroid in the asteroids list.
     */
    private void splitAsteroid(int asteroidIndex) {
        Asteroid a = asteroids.remove(asteroidIndex);

        if (a.size == ASTEROID_SIZE_LARGE) {
            score += SCORE_LARGE_ASTEROID;
            asteroids.add(new Asteroid(a.x, a.y, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, ASTEROID_SIZE_MEDIUM));
            asteroids.add(new Asteroid(a.x, a.y, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, ASTEROID_SIZE_MEDIUM));
        } else if (a.size == ASTEROID_SIZE_MEDIUM) {
            score += SCORE_MEDIUM_ASTEROID;
            asteroids.add(new Asteroid(a.x, a.y, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, ASTEROID_SIZE_SMALL));
            asteroids.add(new Asteroid(a.x, a.y, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, ASTEROID_SIZE_SMALL));
        } else {
            // Small asteroid, just remove it
            score += SCORE_SMALL_ASTEROID;
        }

        // PowerUp drop logic
        asteroidsDestroyedSinceLastPowerUp++;
        if (asteroidsDestroyedSinceLastPowerUp >= asteroidsUntilNextPowerUp) {
            // Spawn random powerup
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[random.nextInt(types.length)];
            powerUps.add(new PowerUp(a.x, a.y, randomType));

            // Reset counter
            asteroidsDestroyedSinceLastPowerUp = 0;
            asteroidsUntilNextPowerUp = POWERUP_DROP_MIN + random.nextInt(POWERUP_DROP_MAX - POWERUP_DROP_MIN + 1);
        }
    }


    // --- Rendering Methods ---
    /**
     * The main paint method, called whenever repaint() is invoked.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the panel (draws background)
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (inGame) {
            drawAimBeam(g2d); // Draw aim beam first (behind ship)
            drawShip(g2d);
            drawBullets(g2d);
            drawAsteroids(g2d);
            drawPowerUps(g2d);
            drawScore(g2d);
            drawActivePowerUp(g2d);
        } else {
            drawGameOver(g2d);
        }
    }
    private void drawAimBeam(Graphics2D g2d) {
        if (activePowerUp == PowerUpType.AIM_BEAM) {
            g2d.setColor(Color.GREEN);
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
            g2d.setStroke(dashed);
            double endX = shipX + Math.cos(shipAngle) * 1000;
            double endY = shipY + Math.sin(shipAngle) * 1000;
            g2d.drawLine((int) shipX, (int) shipY, (int) endX, (int) endY);
            g2d.setStroke(new BasicStroke());
        }
    }
    /**
     * Draws the player's ship, handling rotation.
     */
    private void drawShip(Graphics2D g2d) {
        // Define the ship's shape (a triangle) centered at (0,0)
        // Nose is at (SHIP_SIZE / 2, 0)
        shipShape = new Polygon();
        shipShape.addPoint(SHIP_SIZE / 2, 0);
        shipShape.addPoint(-SHIP_SIZE / 2, -SHIP_SIZE / 3);
        shipShape.addPoint(-SHIP_SIZE / 2, SHIP_SIZE / 3);

        // Save the current graphics transform
        AffineTransform oldTransform = g2d.getTransform();

        // Translate and rotate the graphics context
        g2d.translate(shipX, shipY);
        g2d.rotate(shipAngle);

        // Draw thrust flame if thrusting
        if (thrusting) {
            g2d.setColor(Color.ORANGE);
            g2d.fillPolygon(new int[]{-SHIP_SIZE / 2, -SHIP_SIZE, -SHIP_SIZE / 2},
                    new int[]{-SHIP_SIZE / 4, 0, SHIP_SIZE / 4}, 3);
        }

        // Draw the ship
        g2d.setColor(Color.CYAN);
        g2d.draw(shipShape);

        // Restore the original transform
        g2d.setTransform(oldTransform);
    }
    /**
     * Draws all active bullets.
     */
    private void drawBullets(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g2d.fillOval((int)b.x - 2, (int)b.y - 2, 4, 4); // Draw a 4x4 oval
        }
    }
    /**
     * Draws all active asteroids.
     */
    private void drawAsteroids(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        for (Asteroid a : asteroids) {
            g2d.drawOval((int)(a.x - a.size / 2.0), (int)(a.y - a.size / 2.0), a.size, a.size);
        }
    }

    /**
     * Draws all powerups.
     */
    private void drawPowerUps(Graphics2D g2d) {
        for (PowerUp p : powerUps) {
            // Draw colored square based on powerup type
            Color color;
            String label;
            switch (p.type) {
                case AIM_BEAM:
                    color = Color.GREEN;
                    label = "A";
                    break;
                case DOUBLE_SHOT:
                    color = Color.BLUE;
                    label = "D";
                    break;
                case BOOSTER:
                    color = Color.ORANGE;
                    label = "B";
                    break;
                case RAPID_FIRE:
                    color = Color.RED;
                    label = "R";
                    break;
                default:
                    color = Color.WHITE;
                    label = "?";
            }

            g2d.setColor(color);
            g2d.fillRect((int)(p.x - POWERUP_SIZE / 2), (int)(p.y - POWERUP_SIZE / 2), POWERUP_SIZE, POWERUP_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, (int)p.x - 4, (int)p.y + 5);
        }
    }

    /**
     * Draws the current score.
     */
    private void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 25);
    }

    /**
     * Draws the active powerup indicator.
     */
    private void drawActivePowerUp(Graphics2D g2d) {
        if (activePowerUp != null && powerUpTimeRemaining > 0) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            String powerUpName = "";
            switch (activePowerUp) {
                case AIM_BEAM:
                    powerUpName = "AIM BEAM";
                    break;
                case DOUBLE_SHOT:
                    powerUpName = "DOUBLE SHOT";
                    break;
                case BOOSTER:
                    powerUpName = "BOOSTER";
                    break;
                case RAPID_FIRE:
                    powerUpName = "RAPID FIRE";
                    break;
            }
            int timeLeft = powerUpTimeRemaining / 60; // Convert frames to seconds
            g2d.drawString("PowerUp: " + powerUpName + " (" + timeLeft + "s)", 10, 50);
        }
    }

    /**
     * Draws the Game Over message.
     */
    private void drawGameOver(Graphics2D g2d) {
        String msg = "Game Over";
        String scoreMsg = "Final Score: " + score;
        String restartMsg = "Press 'R' to Restart";

        Font largeFont = new Font("Monospaced", Font.BOLD, 75);
        Font mediumFont = new Font("Monospaced", Font.BOLD, 30);
        FontMetrics metricsLarge = g2d.getFontMetrics(largeFont);
        FontMetrics metricsMedium = g2d.getFontMetrics(mediumFont);

        g2d.setColor(Color.RED);
        g2d.setFont(largeFont);
        g2d.drawString(msg, (PANEL_WIDTH - metricsLarge.stringWidth(msg)) / 2, PANEL_HEIGHT / 2 - 50);

        g2d.setColor(Color.WHITE);
        g2d.setFont(mediumFont);
        g2d.drawString(scoreMsg, (PANEL_WIDTH - metricsMedium.stringWidth(scoreMsg)) / 2, PANEL_HEIGHT / 2 + 20);
        g2d.drawString(restartMsg, (PANEL_WIDTH - metricsMedium.stringWidth(restartMsg)) / 2, PANEL_HEIGHT / 2 + 60);
    }


    // --- KeyListener Methods ---

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (inGame) {
            if (key == KeyEvent.VK_LEFT) {
                rotatingLeft = true;
            }
            if (key == KeyEvent.VK_RIGHT) {
                rotatingRight = true;
            }
            if (key == KeyEvent.VK_UP) {
                thrusting = true;
            }
            if (key == KeyEvent.VK_DOWN) {
                braking = true;
            }
            if (key == KeyEvent.VK_SPACE) {
                fireBullet();
            }
        } else {
            if (key == KeyEvent.VK_R) {
                initGame(); // Restart game
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            rotatingLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rotatingRight = false;
        }
        if (key == KeyEvent.VK_UP) {
            thrusting = false;
        }
        if (key == KeyEvent.VK_DOWN) {
            braking = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, but required by the interface
    }

    // --- Inner Data Classes ---
    /**
     * Simple class to hold Bullet data.
     */
    private static class Bullet {
        double x, y, dx, dy;

        public Bullet(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        public void move() {
            x += dx;
            y += dy;
        }
    }
    /**
     * Simple class to hold Asteroid data.
     */
    private static class Asteroid {
        double x, y, dx, dy;
        int size;

        public Asteroid(double x, double y, double dx, double dy, int size) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.size = size;
        }

        public void move() {
            x += dx;
            y += dy;
        }
    }

    /**
     * Simple class to hold PowerUp data.
     */
    private static class PowerUp {
        double x, y;
        PowerUpType type;
        double age; // For floating animation

        public PowerUp(double x, double y, PowerUpType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.age = 0;
        }

        public void update() {
            age += 0.05;
            // Float up and down
            y += Math.sin(age) * 0.5;
        }
    }
}

