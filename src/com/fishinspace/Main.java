package com.fishinspace;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Launch Asteroid Destroyer game
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Asteroid Destroyer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Add the main game panel
            frame.add(new GamePanel());

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
