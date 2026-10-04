package graphtheory;

import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.java2d.opengl", "true");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        new Canvas("GraphStudio", 1120, 720, Canvas.BG_DARK);
    }
}