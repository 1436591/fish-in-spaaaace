package com.fishinspace;

/**
* Brayden is dope and allat
 * AsteroidDestroyer.java
 *
 * A complete, Asteroid Destroyer game created with Java Swing.
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
public class AsteroidDestroyer { }

class Asteroid {
    public double x, y, dx, dy;
    public int size;

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

class Bullet {
    public double x, y, dx, dy;

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

class PowerUp {
    public double x, y;
    public PowerUpType type;
    public double age; // for floating animation

    public PowerUp(double x, double y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.age = 0;
    }

    public void update() {
        age += 0.05;
        y += Math.sin(age) * 0.5; // float effect
    }
}

enum PowerUpType {
    AIM_BEAM,
    DOUBLE_SHOT,
    BOOSTER,
    RAPID_FIRE
}

/*
 * Original GamePanel implementation moved intact. Public modifier removed so
 * file can compile with AsteroidDestroyer as the single public class.
 */
class GamePanel extends javax.swing.JPanel implements java.awt.event.ActionListener, java.awt.event.KeyListener {

    // --- Game Constants ---
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int SHIP_SIZE = 20;
    private static final double SHIP_TURN_SPEED = 0.05; // radians
    private static final double SHIP_THRUST_POWER = 0.1;
    private static final double SHIP_DRAG = 0.98; // friction
    private static final int BULLET_SPEED = 7;
    private static final int BULLET_COOLDOWN = 15; // frames
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
    private static final int POWERUP_DURATION = 1200; // 20s @ 60fps
    private static final int POWERUP_DROP_MIN = 5;
    private static final int POWERUP_DROP_MAX = 10;

    // --- Game State ---
    private javax.swing.Timer gameTimer;
    private java.util.Random random;
    private boolean inGame;
    private int score;

    private java.util.List<PowerUp> powerUps;
    private int asteroidsDestroyedSinceLastPowerUp;
    private int asteroidsUntilNextPowerUp;
    private PowerUpType activePowerUp;
    private int powerUpTimeRemaining;

    // --- Ship ---
    private double shipX, shipY;
    private double shipVelX, shipVelY;
    private double shipAngle;
    private java.awt.Polygon shipShape;

    // --- Input Flags ---
    private boolean rotatingLeft;
    private boolean rotatingRight;
    private boolean thrusting;
    private boolean braking;

    // --- Objects ---
    private java.util.List<Bullet> bullets;
    private java.util.List<Asteroid> asteroids;
    private int bulletCooldownTimer;

    GamePanel() {
        setPreferredSize(new java.awt.Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(java.awt.Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new java.util.Random();
        bullets = new java.util.ArrayList<>();
        asteroids = new java.util.ArrayList<>();
        powerUps = new java.util.ArrayList<>();

        initGame();
        gameTimer = new javax.swing.Timer(16, this); // ~60 FPS
        gameTimer.start();
    }

    private void initGame() {
        shipX = PANEL_WIDTH / 2.0;
        shipY = PANEL_HEIGHT / 2.0;
        shipVelX = 0;
        shipVelY = 0;
        shipAngle = -Math.PI / 2; // up

        rotatingLeft = false;
        rotatingRight = false;
        thrusting = false;
        braking = false;

        bullets.clear();
        asteroids.clear();
        powerUps.clear();

        for (int i = 0; i < ASTEROID_INIT_COUNT; i++) {
            spawnAsteroid(ASTEROID_SIZE_LARGE);
        }

        score = 0;
        inGame = true;
        bulletCooldownTimer = 0;

        asteroidsDestroyedSinceLastPowerUp = 0;
        asteroidsUntilNextPowerUp = POWERUP_DROP_MIN + random.nextInt(POWERUP_DROP_MAX - POWERUP_DROP_MIN + 1);
        activePowerUp = null;
        powerUpTimeRemaining = 0;

        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    private void spawnAsteroid(int size) {
        double x, y;
        double angle = random.nextDouble() * 2 * Math.PI;
        double speed = (random.nextDouble() * (ASTEROID_MAX_SPEED - 1)) + 1;

        int edge = random.nextInt(4);
        if (edge == 0) { // top
            x = random.nextDouble() * PANEL_WIDTH;
            y = -size / 2.0;
        } else if (edge == 1) { // right
            x = PANEL_WIDTH + size / 2.0;
            y = random.nextDouble() * PANEL_HEIGHT;
        } else if (edge == 2) { // bottom
            x = random.nextDouble() * PANEL_WIDTH;
            y = PANEL_HEIGHT + size / 2.0;
        } else { // left
            x = -size / 2.0;
            y = random.nextDouble() * PANEL_HEIGHT;
        }

        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        asteroids.add(new Asteroid(x, y, dx, dy, size));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (inGame) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        updateShip();
        updateBullets();
        updateAsteroids();
        updatePowerUps();
        checkCollisions();

        if (asteroids.isEmpty()) {
            spawnAsteroid(ASTEROID_SIZE_LARGE);
            spawnAsteroid(ASTEROID_SIZE_LARGE);
        }

        if (bulletCooldownTimer > 0) bulletCooldownTimer--;

        if (powerUpTimeRemaining > 0) {
            powerUpTimeRemaining--;
            if (powerUpTimeRemaining == 0) {
                activePowerUp = null;
            }
        }
    }

    private void updateShip() {
        if (rotatingLeft) shipAngle -= SHIP_TURN_SPEED;
        if (rotatingRight) shipAngle += SHIP_TURN_SPEED;

        double thrustPower = SHIP_THRUST_POWER;
        if (activePowerUp == PowerUpType.BOOSTER) {
            thrustPower *= 1.5;
        }
        if (thrusting) {
            shipVelX += Math.cos(shipAngle) * thrustPower;
            shipVelY += Math.sin(shipAngle) * thrustPower;
        }
        if (braking && activePowerUp == PowerUpType.BOOSTER) {
            shipVelX *= 0.9;
            shipVelY *= 0.9;
        }
        shipVelX *= SHIP_DRAG;
        shipVelY *= SHIP_DRAG;

        shipX += shipVelX;
        shipY += shipVelY;

        wrapCoordinates(shipX, shipY, newCoords -> {
            shipX = newCoords[0];
            shipY = newCoords[1];
        });
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();
            if (b.x < 0 || b.x > PANEL_WIDTH || b.y < 0 || b.y > PANEL_HEIGHT) {
                bullets.remove(i);
            }
        }
    }

    private void updateAsteroids() {
        for (Asteroid a : asteroids) {
            a.move();
            wrapCoordinates(a.x, a.y, newCoords -> {
                a.x = newCoords[0];
                a.y = newCoords[1];
            });
        }
    }

    private void updatePowerUps() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update();
            double dist = Math.sqrt(Math.pow(shipX - p.x, 2) + Math.pow(shipY - p.y, 2));
            if (dist < (SHIP_SIZE + POWERUP_SIZE) / 2.0) {
                activePowerUp = p.type;
                powerUpTimeRemaining = POWERUP_DURATION;
                powerUps.remove(i);
            }
        }
    }

    @FunctionalInterface
    private interface CoordinateWrapper { void apply(double[] newCoords); }

    private void wrapCoordinates(double x, double y, CoordinateWrapper wrapper) {
        if (x < 0) x = PANEL_WIDTH; else if (x > PANEL_WIDTH) x = 0;
        if (y < 0) y = PANEL_HEIGHT; else if (y > PANEL_HEIGHT) y = 0;
        wrapper.apply(new double[]{x, y});
    }

    private void fireBullet() {
        int cooldown = BULLET_COOLDOWN;
        if (activePowerUp == PowerUpType.RAPID_FIRE) {
            cooldown = (int) (BULLET_COOLDOWN / 1.5);
        }
        if (bulletCooldownTimer <= 0) {
            double dx = Math.cos(shipAngle) * BULLET_SPEED;
            double dy = Math.sin(shipAngle) * BULLET_SPEED;
            if (activePowerUp == PowerUpType.DOUBLE_SHOT) {
                double offsetAngle = Math.PI / 16;
                double leftAngle = shipAngle - offsetAngle;
                double rightAngle = shipAngle + offsetAngle;
                bullets.add(new Bullet(shipX, shipY, Math.cos(leftAngle) * BULLET_SPEED, Math.sin(leftAngle) * BULLET_SPEED));
                bullets.add(new Bullet(shipX, shipY, Math.cos(rightAngle) * BULLET_SPEED, Math.sin(rightAngle) * BULLET_SPEED));
            } else {
                bullets.add(new Bullet(shipX, shipY, dx, dy));
            }
            bulletCooldownTimer = cooldown;
        }
    }

    private void checkCollisions() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid a = asteroids.get(j);
                double dist = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
                if (dist < a.size / 2.0) {
                    bullets.remove(i);
                    splitAsteroid(j);
                    break;
                }
            }
        }
        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid a = asteroids.get(i);
            double dist = Math.sqrt(Math.pow(shipX - a.x, 2) + Math.pow(shipY - a.y, 2));
            if (dist < (a.size / 2.0) + (SHIP_SIZE / 2.0)) {
                inGame = false;
                gameTimer.stop();
                break;
            }
        }
    }

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
            score += SCORE_SMALL_ASTEROID;
        }
        asteroidsDestroyedSinceLastPowerUp++;
        if (asteroidsDestroyedSinceLastPowerUp >= asteroidsUntilNextPowerUp) {
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[random.nextInt(types.length)];
            powerUps.add(new PowerUp(a.x, a.y, randomType));
            asteroidsDestroyedSinceLastPowerUp = 0;
            asteroidsUntilNextPowerUp = POWERUP_DROP_MIN + random.nextInt(POWERUP_DROP_MAX - POWERUP_DROP_MIN + 1);
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        if (inGame) {
            drawAimBeam(g2d);
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

    private void drawAimBeam(java.awt.Graphics2D g2d) {
        if (activePowerUp == PowerUpType.AIM_BEAM) {
            g2d.setColor(java.awt.Color.GREEN);
            java.awt.Stroke dashed = new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
            g2d.setStroke(dashed);
            double endX = shipX + Math.cos(shipAngle) * 1000;
            double endY = shipY + Math.sin(shipAngle) * 1000;
            g2d.drawLine((int) shipX, (int) shipY, (int) endX, (int) endY);
            g2d.setStroke(new java.awt.BasicStroke());
        }
    }

    private void drawShip(java.awt.Graphics2D g2d) {
        shipShape = new java.awt.Polygon();
        shipShape.addPoint(SHIP_SIZE / 2, 0);
        shipShape.addPoint(-SHIP_SIZE / 2, -SHIP_SIZE / 3);
        shipShape.addPoint(-SHIP_SIZE / 2, SHIP_SIZE / 3);
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(shipX, shipY);
        g2d.rotate(shipAngle);
        if (thrusting) {
            g2d.setColor(java.awt.Color.ORANGE);
            g2d.fillPolygon(new int[]{-SHIP_SIZE / 2, -SHIP_SIZE, -SHIP_SIZE / 2}, new int[]{-SHIP_SIZE / 4, 0, SHIP_SIZE / 4}, 3);
        }
        g2d.setColor(java.awt.Color.CYAN);
        g2d.draw(shipShape);
        g2d.setTransform(oldTransform);
    }

    private void drawBullets(java.awt.Graphics2D g2d) {
        g2d.setColor(java.awt.Color.YELLOW);
        for (Bullet b : bullets) {
            g2d.fillOval((int) b.x - 2, (int) b.y - 2, 4, 4);
        }
    }

    private void drawAsteroids(java.awt.Graphics2D g2d) {
        g2d.setColor(java.awt.Color.GRAY);
        for (Asteroid a : asteroids) {
            g2d.drawOval((int) (a.x - a.size / 2.0), (int) (a.y - a.size / 2.0), a.size, a.size);
        }
    }

    private void drawPowerUps(java.awt.Graphics2D g2d) {
        for (PowerUp p : powerUps) {
            java.awt.Color color;
            String label;
            switch (p.type) {
                case AIM_BEAM: color = java.awt.Color.GREEN; label = "A"; break;
                case DOUBLE_SHOT: color = java.awt.Color.BLUE; label = "D"; break;
                case BOOSTER: color = java.awt.Color.ORANGE; label = "B"; break;
                case RAPID_FIRE: color = java.awt.Color.RED; label = "R"; break;
                default: color = java.awt.Color.WHITE; label = "?";
            }
            g2d.setColor(color);
            g2d.fillRect((int) (p.x - POWERUP_SIZE / 2), (int) (p.y - POWERUP_SIZE / 2), POWERUP_SIZE, POWERUP_SIZE);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.drawString(label, (int) p.x - 4, (int) p.y + 5);
        }
    }

    private void drawScore(java.awt.Graphics2D g2d) {
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 25);
    }

    private void drawActivePowerUp(java.awt.Graphics2D g2d) {
        if (activePowerUp != null && powerUpTimeRemaining > 0) {
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 16));
            String name;
            switch (activePowerUp) {
                case AIM_BEAM: name = "AIM BEAM"; break;
                case DOUBLE_SHOT: name = "DOUBLE SHOT"; break;
                case BOOSTER: name = "BOOSTER"; break;
                case RAPID_FIRE: name = "RAPID FIRE"; break;
                default: name = ""; break;
            }
            int timeLeft = powerUpTimeRemaining / 60;
            g2d.drawString("PowerUp: " + name + " (" + timeLeft + "s)", 10, 50);
        }
    }

    private void drawGameOver(java.awt.Graphics2D g2d) {
        String msg = "Game Over";
        String scoreMsg = "Final Score: " + score;
        String restartMsg = "Press 'R' to Restart";
        java.awt.Font largeFont = new java.awt.Font("Monospaced", java.awt.Font.BOLD, 75);
        java.awt.Font mediumFont = new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30);
        java.awt.FontMetrics metricsLarge = g2d.getFontMetrics(largeFont);
        java.awt.FontMetrics metricsMedium = g2d.getFontMetrics(mediumFont);
        g2d.setColor(java.awt.Color.RED);
        g2d.setFont(largeFont);
        g2d.drawString(msg, (PANEL_WIDTH - metricsLarge.stringWidth(msg)) / 2, PANEL_HEIGHT / 2 - 50);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(mediumFont);
        g2d.drawString(scoreMsg, (PANEL_WIDTH - metricsMedium.stringWidth(scoreMsg)) / 2, PANEL_HEIGHT / 2 + 20);
        g2d.drawString(restartMsg, (PANEL_WIDTH - metricsMedium.stringWidth(restartMsg)) / 2, PANEL_HEIGHT / 2 + 60);
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        int key = e.getKeyCode();
        if (inGame) {
            if (key == java.awt.event.KeyEvent.VK_LEFT) rotatingLeft = true;
            if (key == java.awt.event.KeyEvent.VK_RIGHT) rotatingRight = true;
            if (key == java.awt.event.KeyEvent.VK_UP) thrusting = true;
            if (key == java.awt.event.KeyEvent.VK_DOWN) braking = true;
            if (key == java.awt.event.KeyEvent.VK_SPACE) fireBullet();
        } else {
            if (key == java.awt.event.KeyEvent.VK_R) initGame();
        }
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        int key = e.getKeyCode();
        if (key == java.awt.event.KeyEvent.VK_LEFT) rotatingLeft = false;
        if (key == java.awt.event.KeyEvent.VK_RIGHT) rotatingRight = false;
        if (key == java.awt.event.KeyEvent.VK_UP) thrusting = false;
        if (key == java.awt.event.KeyEvent.VK_DOWN) braking = false;
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) { /* not used */ }
}
