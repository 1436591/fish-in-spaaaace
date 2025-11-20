package com.fishinspace;

public class Bullet {
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
