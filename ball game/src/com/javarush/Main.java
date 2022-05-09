package com.javarush;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

class DrawingComponent extends JPanel {
    static int xsize = 1920;
    static int ysize = 1080;
    double v0, alpha0, x0, y0, g;
    double h; // шаг в СЕКУНДАХ
    long toWait; // шаг в МИЛЛИСЕКУНДАХ
    Ball ball;
    Platform pl1, pl2;
    double ballR;
    int platLen, platHeight;
    int platformStep;
    int first_p_score = 0, second_p_score = 0;

    int LOWER_BOUND = ysize - 50;
    int LEFT_BOUND = 0;
    int RIGHT_BOUND = 1850;
    int MIDDLE_BOUND = (RIGHT_BOUND + LEFT_BOUND) / 2;

    public DrawingComponent() {
        super();

        g = 9.81;
        toWait = 10;
        h = (double) toWait / 1000;
        x0 = 200;
        y0 = 200;
        alpha0 = Math.PI / 2;
        v0 = 12;
        ballR = 50;
        platLen = 300;
        platHeight = 50;
        platformStep = 50;

        resizeToCantiMetres();

        Ball.r = ballR;
        ball = new Ball();
        ball.x = x0;
        ball.y = y0;
        ball.vx = v0 * Math.cos(alpha0);
        ball.vy = v0 * Math.sin(alpha0);
        ball.ax = 0;
        ball.ay = -g;

        Platform.PLAT_LEN = platLen;
        Platform.PLAT_HEIGHT = platHeight;
        pl1 = new Platform();
        pl1.x = LEFT_BOUND + 50;
        pl1.y = Platform.PLAT_HEIGHT + 100;

        pl2 = new Platform();
        pl2.x = RIGHT_BOUND - Platform.PLAT_LEN - 50;
        pl2.y = Platform.PLAT_HEIGHT + 100;

        new Thread(() -> {
            while (true) {
                ballStateChange();
                DrawingComponent.this.repaint();

                try {
                    Thread.sleep(toWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    void ballStateChange() {
        //меняем координаты
        ball.x += h * ball.vx; // координата = пред. координата + шаг * скорость по этой координате
        ball.y += h * ball.vy;

        //меняем скорости
        ball.vx += h * ball.ax; //скорость = пред. скорость + шаг * ускорение
        ball.vy += h * ball.ay;

        //ускорения постоянны

        //чекаем отскок от платформ
        double R_O = 50; //радиус отбития по игреку
        if (ball.x >= pl1.x && ball.x <= pl1.x + Platform.PLAT_LEN && ball.y >= pl1.y && ball.y <= pl1.y + R_O && ball.y >= pl1.y - R_O) {
            double PL1_Centre = pl1.x + Platform.PLAT_LEN/2;
            double otrezok = (ball.x - PL1_Centre) / (Platform.PLAT_LEN/2);
            double alpha = Math.acos(otrezok);
            ball.vx = v0 * Math.cos(alpha);
            ball.vy = v0 * Math.sin(alpha);
        } else if (ball.x >= pl2.x && ball.x <= pl2.x + Platform.PLAT_LEN && ball.y >= pl2.y && ball.y <= pl2.y + R_O && ball.y >= pl2.y - R_O) {
            double PL2_Centre = pl2.x + Platform.PLAT_LEN/2;
            double otrezok = (ball.x - PL2_Centre) / (Platform.PLAT_LEN/2);
            double alpha = Math.acos(otrezok);
            ball.vx = v0 * Math.cos(alpha);
            ball.vy = v0 * Math.sin(alpha);
        }

        //чекаем отскок от стен
        if (ball.x <= LEFT_BOUND || ball.x >= RIGHT_BOUND)
            ball.vx = -ball.vx;

        //падение за нижнюю границу
        if (ball.y <= 0) {
            if (ball.x <= MIDDLE_BOUND) {
                second_p_score++;
                ball.x = pl1.x + Platform.PLAT_LEN / 2;
                ball.y = pl1.y;
            } else {
                first_p_score++;
                ball.x = pl2.x + Platform.PLAT_LEN / 2;
                ball.y = pl2.y;
            }
            ball.vx = v0 * Math.cos(alpha0);
            ball.vy = v0 * Math.sin(alpha0);
        }

    }

    void resizeToCantiMetres() {
        g *= 100; // 100 * метры в секунду^2 -> см в с^2
        v0 *= 100; //100 * м/с = см / с;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int ballTopLeft_X = (int)Math.round(ball.x - ball.r * Math.cos(Math.PI/4));
        int ballTopLeft_Y = (int)Math.round(ball.y + ball.r * Math.sin(Math.PI/4));
        g.fillOval(ballTopLeft_X, LOWER_BOUND - ballTopLeft_Y, (int)Math.round(2 * ball.r), (int)Math.round(2 * ball.r));
        g.fillRect(pl1.x, LOWER_BOUND - pl1.y, Platform.PLAT_LEN, Platform.PLAT_HEIGHT);
        g.fillRect(pl2.x, LOWER_BOUND - pl2.y, Platform.PLAT_LEN, Platform.PLAT_HEIGHT);
        g.drawLine(MIDDLE_BOUND, 0, MIDDLE_BOUND, 1080);

        Font cfont = g.getFont();
        Font nfont = cfont.deriveFont(cfont.getSize() * 5f);
        g.setFont(nfont);

        g.drawString("Score: " + first_p_score, 125, 125);
        g.drawString("Score: " + second_p_score, RIGHT_BOUND - 250, 125);
    }

    public void onKeyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A && pl1.x > LEFT_BOUND)
            pl1.x -= platformStep;
        else if (e.getKeyCode() == KeyEvent.VK_D && pl1.x + Platform.PLAT_LEN < MIDDLE_BOUND)
            pl1.x += platformStep;

        if (e.getKeyCode() == KeyEvent.VK_LEFT && pl2.x > MIDDLE_BOUND)
            pl2.x -= platformStep;
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && pl2.x + Platform.PLAT_LEN < RIGHT_BOUND)
            pl2.x += platformStep;
    }

}

public class Main extends JFrame implements KeyListener{

    DrawingComponent drawer;

    public Main () {
        super("Игруха");
        JPanel jcp = new JPanel(new BorderLayout());
        setContentPane(jcp);
        drawer = new DrawingComponent();
        jcp.add(drawer, BorderLayout.CENTER);
        jcp.setBackground(Color.gray);
        setSize(DrawingComponent.xsize, DrawingComponent.ysize);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addKeyListener(this);

    }

    public static void main(String[] args)   {
        Main f = new Main();
        f.setVisible(true);
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        drawer.onKeyReleased(e);
    }
}
