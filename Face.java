import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// OS interface
public class Face {

    public static void main(String[] args) {
        final int pWidth = 200, pHeight = 200;
//        final int lWidth = 400, lHeight = 400;

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
    final Boolean[] isOn = {false, false, false, false};

    public FacePanel() {

        JButton[] jButtons = new JButton[4];
        String[] jbText = {"inside", "show", "screen", "outside"};

        for (int i = 0; i < 4; i++) {
            jButtons[i] = new JButton(jbText[i]);
            jButtons[i].setPreferredSize(new Dimension(80, 70));
            add(jButtons[i]);
        }

        jButtons[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[0]) {
                    isOn[0] = false;
                    jButtons[0].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[0] = true;
                        jButtons[0].setBackground(Color.GREEN);

                        Inside.main(null);
                    }
                }
            }
        });

        jButtons[1].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[1]) {
                    isOn[1] = false;
                    jButtons[1].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[1] = true;
                        jButtons[1].setBackground(Color.GREEN);

                        ShowBmp.main(null);
                    }
                }
            }
        });

        jButtons[2].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[2]) {
                    isOn[2] = false;
                    jButtons[2].setBackground(null);
                    ScreenShot.kill();
                } else {
                    if (canOn()) {
                        isOn[2] = true;
                        jButtons[2].setBackground(Color.GREEN);

                        ScreenShot.main(null);
                    }
                }
            }
        });

        jButtons[3].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOn[3]) {
                    isOn[3] = false;
                    jButtons[3].setBackground(null);
                } else {
                    if (canOn()) {
                        isOn[3] = true;
                        jButtons[3].setBackground(Color.GREEN);

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
