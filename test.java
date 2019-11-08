import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class test {

    private static final int x = 0;
    private static final boolean c = false;

    public static void main(String[] args) {


        byte[] data = new byte[]{100, 10, 0, 127, -128};

        data = doDeflection(data);

        data = reDeflection(data);

        int l = data.length;

    }

    private static byte[] doDeflection(byte[] data) {

        // i, j, a 计数器
        int i, j, a, b = c ? 1 : 3;
        // x 误差等级, b 彩色模式调整

        int m = pow(2, x), n = pow(2, 3 - x);

        byte[] res = new byte[data.length * m * b];
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < m * b; j++) {
                a = (data[i] & getHelp(j / b)) >> (8 - n - (j / b * n));
                res[b * m * i + j] = a == 0 ? 0 : (byte) ((pow(2, (8 - n))) * (a + 1) - 1);
            }
        }

        return res;
    }

    private static byte[] reDeflection(byte[] data) {

        if (!c) {
            // 黑白/3
            data = wb2color(data);
        }

        int i, j, r, a, l = data.length >> x;
        byte[] res = new byte[l];
        for (i = 0; i < l; i++) {
            r = 0;
            for (j = 0; j < pow(2, x); j++) {
                a = correct(data[i * pow(2, x) + j]);
                r += ((a + (a < 0 ? 0x100 : 0)) >> j * pow(2, 3 - x)) & getHelp(j);
            }
            res[i] = (byte) r;
            System.out.println(res[i]);
        }
        return res;
    }

    /**
     * 矫正
     *
     * @param x
     * @return
     */
    private static int correct(int x) {
        return x;
    }

    /**
     * 黑白长度除三
     *
     * @param data
     * @return
     */
    private static byte[] wb2color(byte[] data) {
        byte[] res = new byte[data.length / 3];
        for (int i = 0; i < data.length; i += 3) {
            if (data[i] == data[i + 1] && data[i] == data[i + 2]) {
                res[i / 3] = data[i];
                tCollect++;
            } else {
                if (data[i] == data[i + 1] || data[i] == data[i + 2]) {
                    res[i / 3] = data[i];
                    wCollect++;
                } else if (data[i + 1] == data[i + 2]) {
                    res[i / 3] = data[i + 1];
                    wCollect++;
                } else {
                    res[i / 3] = (byte) ((data[i] + data[i + 1] + data[i + 2]) / 3);
                    eCollect++;
                }
            }
        }
        return res;
    }

    /**
     * 勘误
     *
     * @param a 勘误前
     * @return 勘误后
     */
    private static int checkDeflection(int a) {
        return (a & 0xf) > 8 ? (a >> 4) + 1 : a >> 4;
    }

    /**
     * 收集图片正确率
     * 正确/警告/错误
     */
    private static int tCollect = 0, wCollect = 0, eCollect = 0;

    private static int getHelp(int y) {
        int r, c, i, j;
        int[] base = new int[]{128, 64, 32, 16, 8, 4, 2, 1}, arr = new int[pow(2, x)];

        for (i = 0; i < pow(2, x); i++) {
            r = 0;
            // 每次 c 个，第 c*i 个开始，c*i+c 个结束
            c = pow(2, 3 - x);
            for (j = i * c; j < i * c + c; j++) {
                r += base[j];
            }
            arr[i] = r;
        }
        return arr[y];
    }

    private static int pow(int a, int b) {
        int r = 1;
        for (int i = 0; i < b; i++) {
            r *= a;
        }
        return r;
    }

    private static void bmp2Png() {
        String fileName = "2019-11-06 13-00-45";
        try {
            File out = null;
            File in = new File("D:\\GitHub\\doc2bmp\\" + fileName + ".bmp");
            BufferedImage input = ImageIO.read(in);
            out = new File("D:\\GitHub\\doc2bmp\\" + fileName + ".png");
            ImageIO.write(input, "BMP", out);
            input.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}