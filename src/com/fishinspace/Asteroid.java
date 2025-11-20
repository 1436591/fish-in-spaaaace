package com.fishinspace;

public class Asteroid {
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
