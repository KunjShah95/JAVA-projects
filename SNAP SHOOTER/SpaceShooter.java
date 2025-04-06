import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class SpaceShooter extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private final GamePanel gamePanel;

    public SpaceShooter() {
        setTitle("Space Shooter");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceShooter());
    }

    // Main game panel
    class GamePanel extends JPanel implements ActionListener, KeyListener {
        private final Timer timer;
        private Player player;
        private final ArrayList<Laser> lasers;
        private final ArrayList<Enemy> enemies;
        private final ArrayList<Explosion> explosions;
        private final Random random;
        private int score;
        private boolean gameOver;
        private final int ENEMY_SPAWN_DELAY = 50;
        private int enemySpawnCounter;

        public GamePanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);

            player = new Player(WIDTH / 2, HEIGHT - 100);
            lasers = new ArrayList<>();
            enemies = new ArrayList<>();
            explosions = new ArrayList<>();
            random = new Random();
            score = 0;
            gameOver = false;
            enemySpawnCounter = 0;

            timer = new Timer(16, this); // ~60 FPS
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw stars in the background
            drawStars(g2d);

            // Draw game elements
            if (!gameOver) {
                player.draw(g2d);

                for (Laser laser : lasers) {
                    laser.draw(g2d);
                }

                for (Enemy enemy : enemies) {
                    enemy.draw(g2d);
                }
            }

            // Draw explosions
            for (Explosion explosion : explosions) {
                explosion.draw(g2d);
            }

            // Draw UI
            drawUI(g2d);
        }

        private void drawStars(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < 100; i++) {
                int x = random.nextInt(WIDTH);
                int y = (random.nextInt(HEIGHT) + (int)(System.currentTimeMillis() / 50) % HEIGHT) % HEIGHT;
                int size = random.nextInt(2) + 1;
                g2d.fillRect(x, y, size, size);
            }
        }

        private void drawUI(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Score: " + score, 20, 30);

            if (gameOver) {
                g2d.setFont(new Font("Arial", Font.BOLD, 50));
                g2d.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2);
                g2d.setFont(new Font("Arial", Font.BOLD, 25));
                g2d.drawString("Press SPACE to play again", WIDTH / 2 - 150, HEIGHT / 2 + 50);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameOver) {
                // Update player
                player.update();

                // Update lasers
                updateLasers();

                // Spawn and update enemies
                updateEnemies();

                // Check collisions
                checkCollisions();
            }

            // Update explosions
            updateExplosions();

            repaint();
        }

        private void updateLasers() {
            Iterator<Laser> it = lasers.iterator();
            while (it.hasNext()) {
                Laser laser = it.next();
                laser.update();
                if (laser.getY() < 0) {
                    it.remove();
                }
            }
        }

        private void updateEnemies() {
            enemySpawnCounter++;
            if (enemySpawnCounter >= ENEMY_SPAWN_DELAY) {
                enemySpawnCounter = 0;
                spawnEnemy();
            }

            Iterator<Enemy> it = enemies.iterator();
            while (it.hasNext()) {
                Enemy enemy = it.next();
                enemy.update();
                if (enemy.getY() > HEIGHT) {
                    it.remove();
                }
            }
        }

        private void spawnEnemy() {
            int x = random.nextInt(WIDTH - 40);
            int type = random.nextInt(3); // 3 different enemy types
            enemies.add(new Enemy(x, -50, type));
        }

        private void updateExplosions() {
            Iterator<Explosion> it = explosions.iterator();
            while (it.hasNext()) {
                Explosion explosion = it.next();
                explosion.update();
                if (explosion.isFinished()) {
                    it.remove();
                }
            }
        }

        private void checkCollisions() {
            // Check laser-enemy collisions
            for (Iterator<Laser> laserIt = lasers.iterator(); laserIt.hasNext();) {
                Laser laser = laserIt.next();
                Rectangle laserBounds = laser.getBounds();

                for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext();) {
                    Enemy enemy = enemyIt.next();
                    Rectangle enemyBounds = enemy.getBounds();

                    if (laserBounds.intersects(enemyBounds)) {
                        // Create explosion at enemy position
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        
                        // Remove both laser and enemy
                        laserIt.remove();
                        enemyIt.remove();
                        
                        // Increase score
                        score += (enemy.getType() + 1) * 10;
                        
                        break;
                    }
                }
            }

            // Check player-enemy collisions
            Rectangle playerBounds = player.getBounds();
            for (Enemy enemy : enemies) {
                if (playerBounds.intersects(enemy.getBounds())) {
                    gameOver = true;
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    break;
                }
            }
        }

        private void resetGame() {
            player = new Player(WIDTH / 2, HEIGHT - 100);
            lasers.clear();
            enemies.clear();
            explosions.clear();
            score = 0;
            gameOver = false;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (gameOver) {
                if (key == KeyEvent.VK_SPACE) {
                    resetGame();
                }
                return;
            }

            if (key == KeyEvent.VK_LEFT) {
                player.setMovingLeft(true);
            } else if (key == KeyEvent.VK_RIGHT) {
                player.setMovingRight(true);
            }

            if (key == KeyEvent.VK_SPACE) {
                fireLaser();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT) {
                player.setMovingLeft(false);
            } else if (key == KeyEvent.VK_RIGHT) {
                player.setMovingRight(false);
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Not used
        }

        private void fireLaser() {
            lasers.add(new Laser(player.getX() + player.getWidth() / 2 - 2, player.getY()));
        }
    }

    // Player class
    class Player {
        private int posX, posY;
        private final int width = 50;
        private final int height = 50;
        private final int speed = 5;
        private boolean movingLeft;
        private boolean movingRight;

        public Player(int x, int y) {
            this.posX = x;
            this.posY = y;
        }

        public void update() {
            if (movingLeft) {
                posX -= speed;
            }
            if (movingRight) {
                posX += speed;
            }

            // Keep player within bounds
            if (posX < 0) {
                posX = 0;
            }
            if (posX > WIDTH - width) {
                posX = WIDTH - width;
            }
        }

        public void draw(Graphics2D g2d) {
            // Draw player spaceship
            g2d.setColor(Color.CYAN);
            
            // Ship body
            Polygon ship = new Polygon();
            ship.addPoint(posX + width / 2, posY);
            ship.addPoint(posX, posY + height);
            ship.addPoint(posX + width, posY + height);
            g2d.fillPolygon(ship);
            
            // Engine flame
            g2d.setColor(Color.ORANGE);
            Polygon flame = new Polygon();
            flame.addPoint(posX + width / 2 - 10, posY + height);
            flame.addPoint(posX + width / 2, posY + height + 15);
            flame.addPoint(posX + width / 2 + 10, posY + height);
            g2d.fillPolygon(flame);
        }

        public Rectangle getBounds() {
            return new Rectangle(posX, posY, width, height);
        }

        public void setMovingLeft(boolean movingLeft) {
            this.movingLeft = movingLeft;
        }

        public void setMovingRight(boolean movingRight) {
            this.movingRight = movingRight;
        }

        public int getX() {
            return posX;
        }

        public int getY() {
            return posY;
        }

        public int getWidth() {
            return width;
        }
    }

    // Laser class
    class Laser {
        private final int posX;
        private int posY;
        private final int width = 4;
        private final int height = 15;
        private final int speed = 10;

        public Laser(int x, int y) {
            this.posX = x;
            this.posY = y;
        }

        public void update() {
            posY -= speed;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.GREEN);
            g2d.fillRect(posX, posY, width, height);
        }

        public Rectangle getBounds() {
            return new Rectangle(posX, posY, width, height);
        }

        public int getY() {
            return posY;
        }
    }

    // Enemy class
    class Enemy {
        private final int posX;
        private int posY;
        private final int width = 40;
        private final int height = 40;
        private final int speed;
        private final int type; // 0: slow, 1: medium, 2: fast
        private final Color[] colors = {Color.RED, Color.ORANGE, Color.MAGENTA};

        public Enemy(int x, int y, int type) {
            this.posX = x;
            this.posY = y;
            this.type = type;
            this.speed = 2 + type;
        }

        public void update() {
            posY += speed;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(colors[type]);
            
            switch (type) {
                case 0 -> // Simple enemy
                    g2d.fillOval(posX, posY, width, height);
                case 1 -> {
                    // Medium enemy - diamond shape
                    Polygon diamond = new Polygon();
                    diamond.addPoint(posX + width/2, posY);
                    diamond.addPoint(posX + width, posY + height/2);
                    diamond.addPoint(posX + width/2, posY + height);
                    diamond.addPoint(posX, posY + height/2);
                    g2d.fillPolygon(diamond);
                }
                default -> {
                    // Fast enemy - X shape
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawLine(posX, posY, posX + width, posY + height);
                    g2d.drawLine(posX + width, posY, posX, posY + height);
                }
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(posX, posY, width, height);
        }

        public int getX() {
            return posX;
        }

        public int getY() {
            return posY;
        }

        public int getType() {
            return type;
        }
    }

    // Explosion class
    class Explosion {
        private final int x, y;
        private int size;
        private final int maxSize = 50;
        private final int growthRate = 2;
        private final int duration = 20; // frames the explosion lasts
        private int frame = 0;

        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            this.size = 10;
        }

        public void update() {
            frame++;
            if (frame < duration / 2) {
                size = Math.min(size + growthRate, maxSize);
            } else {
                size = Math.max(size - growthRate, 0);
            }
        }

        public void draw(Graphics2D g2d) {
            // Create a gradient for the explosion
            RadialGradientPaint gradient = new RadialGradientPaint(
                new Point(x + size/2, y + size/2),
                size/2,
                new float[]{0.0f, 0.8f, 1.0f},
                new Color[]{Color.WHITE, Color.ORANGE, Color.RED}
            );
            g2d.setPaint(gradient);
            g2d.fillOval(x, y, size, size);
        }

        public boolean isFinished() {
            return frame >= duration;
        }
    }
}