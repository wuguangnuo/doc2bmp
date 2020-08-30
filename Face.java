import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Face {
    public static void main(String[] args) {
        final int pWidth = 200, pHeight = 200;
        JFrame frame = new JFrame("StealFile");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        frame.setBounds(d.width / 2 - pWidth / 2, d.height / 2 - pHeight / 2, pWidth, pHeight);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FacePanel());
        frame.setVisible(true);
    }
}

class FacePanel extends JPanel {
    final boolean[] isOn = {false, false, false, false};

    public FacePanel() {
        JButton[] buttons = new JButton[4];
        String[] texts = {"inside", "show", "screen", "outside"};

        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton(texts[i]);
            buttons[i].setPreferredSize(new Dimension(80, 73));
            add(buttons[i]);
        }

        buttons[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[0]) {
                    isOn[0] = false;
                    buttons[0].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[0] = true;
                        buttons[0].setBackground(Color.GREEN);
                        Inside.main(null);
                    }
                }
            }
        });

        buttons[1].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[1]) {
                    isOn[1] = false;
                    buttons[1].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[1] = true;
                        buttons[1].setBackground(Color.GREEN);
                        ShowBmp.main(null);
                    }
                }
            }
        });

        buttons[2].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[2]) {
                    isOn[2] = false;
                    buttons[2].setBackground(null);
                    ScreenShot.kill();
                } else {
                    if (canOn()) {
                        isOn[2] = true;
                        buttons[2].setBackground(Color.GREEN);
                        ScreenShot.main(null);
                    }
                }
            }
        });

        buttons[3].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[3]) {
                    isOn[3] = false;
                    buttons[3].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[3] = true;
                        buttons[3].setBackground(Color.GREEN);
                        Outside.main(null);
                    }
                }
            }
        });
    }

    private boolean canOn() {
        for (boolean b : isOn) {
            if (b) {
                return false;
            }
        }
        return true;
    }
}