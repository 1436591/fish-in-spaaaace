package com.fishinspace;

public class PowerUp {
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
