import java.awt.*;
import java.util.Random;

abstract class SceneryObject {
    static final Random rng = new Random();

    double x; // horizontal position on screen
    final double speedFactor; // how fast this layer scrolls vs. the road

    SceneryObject(double x, double speedFactor) {
        this.x = x;
        this.speedFactor = speedFactor;
    }

    void update(double roadSpeed, int panelWidth) {
        x -= roadSpeed * speedFactor;
        if (x < -100) {
            x = panelWidth + 60 + rng.nextInt(120); 
        }
    }

    abstract void draw(Graphics2D g, int groundY);
}

class Cloud extends SceneryObject {
    int y; // vertical position (height in the sky)

    Cloud(double x, int y) {
        super(x, 0.15); // slowest layer: far away
        this.y = y;
    }

    @Override
    void draw(Graphics2D g, int groundY) {
        int px = (int) x;
        g.setColor(new Color(255, 255, 255, 230));
        g.fillOval(px, y, 60, 30); // left puff
        g.fillOval(px + 25, y - 12, 55, 38); 
        g.fillOval(px + 50, y, 60, 30); 
    }
}

class Hill extends SceneryObject {
    Hill(double x) {
        super(x, 0.35); 
    }

    @Override
    void draw(Graphics2D g, int groundY) {
        int px = (int) x; // x is the hill's center
        g.setColor(new Color(70, 140, 80));
        g.fillOval(px - 100, groundY - 110, 200, 220);
    }
}

class Tree extends SceneryObject {
    Tree(double x) {
        super(x, 0.85); 
    }

    @Override
    void draw(Graphics2D g, int groundY) {
        int px = (int) x; 
        int base = groundY - 20; 
        g.setColor(new Color(100, 65, 30));
        g.fillRect(px - 5, base - 40, 10, 40); 
        g.setColor(new Color(30, 110, 40));
        g.fillOval(px - 20, base - 85, 40, 55); 
    }
}