import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内部使用
 * 显示图片
 */
public class ShowBmp extends JFrame {
    private JLabel label;
    private int t = -1;
    private static IO io = IO.getInstance();
    private static Config config = io.getConfig();
    private static long initialDelay = 3 * config.getDelay();

    public static void main(String[] args) {
        String input = io.scanner("输入文件夹名：");

        List<String> fileNames = io.getNames(config.getDefaultPath() + io.separator() + input, "bmp");

        new ShowBmp(fileNames).setVisible(true);
    }

    private ShowBmp(List<String> bmpFiles) {
        super("ShowBmp");
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(bmpFiles.get(0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert image != null;
        label = new JLabel(new ImageIcon(image));
        add(label);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(config.getWidth() + 100, config.getWidth() + 100);
        getContentPane().setBackground(Color.green);
        getContentPane().setVisible(true);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
        service.scheduleWithFixedDelay(() -> {
                    if (t >= bmpFiles.size()) {
                        this.setVisible(false);
                        io.end();
                    }
                    if (t >= 0) {
                        io.print("第" + (t + 1) + "张: " + bmpFiles.get(t).substring(config.getDefaultPath().length()));
                        try {
                            label.setIcon(new ImageIcon(ImageIO.read(new File(bmpFiles.get(t)))));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        io.print("等待..");
                    }
                    t++;
                },
                initialDelay,
                config.getDelay(),
                TimeUnit.MILLISECONDS);
    }
}
