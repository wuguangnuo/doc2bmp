import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * IO流处理
 */
public class IO {
    private static Config config = new Config();
    private static long timestmp = System.currentTimeMillis();
    private static final String separator = File.separator;
    private static final String fn = LocalDateTime.now().toString()
            .replaceAll(":", "").replaceAll("-", "").split("[.]")[0];

    // 是否界面模式
    private static final boolean isFace = true;

    public static IO getInstance() {
        return new IO();
    }

    /**
     * 获取配置
     *
     * @return config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * 获取文件大小(针对小内存大文件的处理)
     *
     * @param path 文件路径
     * @return length
     */
    public int docLength(String path) {
        long len = new File(path).length();
        if (len > Integer.MAX_VALUE) {
            throw new RuntimeException("文件[" + path + "]过大");
        } else {
            return (int) len;
        }
    }

    /**
     * 读取文档文件
     *
     * @param path 文件路径
     * @return byte[]
     */
    public byte[] readDoc(String path) {
        return readDoc(path, 0, -1);
    }

    /**
     * 读取文档文件
     *
     * @param path 文件路径
     * @param skip 跳过比特
     * @param len  读取长度
     * @return byte[]
     */
    public byte[] readDoc(String path, int skip, int len) {
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.skip(skip);
            byte[] data = new byte[len < 0 ? bis.available() : len];
            bis.read(data);
            bis.close();
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            printErr("读取文件[" + path + "]错误");
        } catch (IOException e) {
            printErr("IO异常");
        }
        return null;
    }

    /**
     * 读取BMP文件(BMP24位)
     *
     * @param path 文件路径
     * @return byte[]
     */
    public byte[] readBmp(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(fis);

            bis.skip(18L);
            byte[] b = new byte[4];
            bis.read(b);
            byte[] b2 = new byte[4];
            bis.read(b2);

            int width = byte2Int(b), heigth = byte2Int(b2);
            byte[] date = new byte[heigth * width * 3];

            bis.skip(28L);
            bis.read(date);
            bis.close();
            fis.close();
            return date;
        } catch (FileNotFoundException e) {
            printErr("读取文件[" + path + "]错误");
        } catch (IOException e) {
            printErr("IO异常");
        }
        return null;
    }

    /**
     * 写入文档文件
     *
     * @param data   byte[]
     * @param suffix 后缀
     */
    public void writeDoc(byte[] data, String suffix) {
//        data = reDeflection(data);

        String fileName = getFileName(suffix, false);
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(data, 0, data.length);
            bos.close();
            fos.close();
            print("生成文件[" + fileName + "]完成");
        } catch (FileNotFoundException e) {
            printErr("创建文件[" + fileName + "]失败");
        } catch (IOException e) {
            printErr("IO异常");
        }
    }

    /**
     * 写入BMP文件
     *
     * @param data byte[]
     */
    public void writeBmp(byte[] data) {
        data = doDeflection(data);

        int width = config.getWidth(), height = width;
//        int height = data.length % (3 * width) == 0 ? data.length / 3 / width : data.length / 3 / width + 1;

        String fileName = getFileName("bmp", true);
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // 给文件头的变量赋值
            bos.write(new byte[]{0x42, 0x4d}, 0, 2);
            bos.write(int2Byte(54 + width * height * 3), 0, 4);
            bos.write(new byte[]{0, 0, 0, 0, 0x36, 0, 0, 0, 0x28, 0, 0, 0}, 0, 12);
            bos.write(int2Byte(width), 0, 4);
            bos.write(int2Byte(height), 0, 4);
            bos.write(new byte[]{1, 0, 0x18, 0, 0, 0, 0, 0}, 0, 8);
            bos.write(int2Byte(width * height), 0, 4);
            bos.write(new byte[16], 0, 0x10);

            bos.write(data);
//            bos.write(new byte[width * height * 3 - data.length]);

            bos.close();
            fos.close();
            print("生成图片[" + fileName + "]完成");
        } catch (FileNotFoundException e) {
            printErr("创建图片[" + fileName + "]失败");
        } catch (IOException e) {
            printErr("IO异常");
        }
    }

    /**
     * 删除文件
     *
     * @param path path
     */
    public void deleteFile(String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
            print("删除文件[" + path + "]成功");
        } else {
            printErr("删除文件[" + path + "]错误,文件不存在");
        }
    }

    /**
     * 格式化打印
     *
     * @param x 打印的内容
     */
    public void print(String x) {
        long n = System.currentTimeMillis();
        String msg = "--- " + x + (config.isDebug() ? " " + (n - timestmp) + "ms" : "") + " ;";
        System.out.println(msg);

        if (config.isDebug()) {
            String logFileName = config.getDefaultPath() + separator + config.getLogFileName();
            try {
                File file = new File(logFileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(logFileName, true)));
                bw.write(LocalDateTime.now().toString().replaceAll("T", " ").split("[.]")[0] +
                        " " + msg + "\r\n");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        timestmp = n;
    }

    /**
     * 格式化打印异常信息
     *
     * @param x 打印的内容
     */
    public void printErr(String x) {
        print(x);
        if (isFace) {
            JOptionPane.showMessageDialog(null, x);
            System.exit(1);
        }
    }

    /**
     * 获取输入
     *
     * @param x message
     * @return input
     */
    public String scanner(String x) {
        System.out.println(x);
        String input = null;
        while (input == null || "".equals(input)) {
            // 一个控制台输入，一个弹窗输入，二者选一个
            if (isFace) {
                input = JOptionPane.showInputDialog(x);
            } else {
                input = new Scanner(System.in).nextLine();
            }
        }

        print(x + input);
        return input.trim();
    }

    /**
     * 执行结束
     */
    public void end() {
        print("END ---");
        if (isFace) {
            JOptionPane.showMessageDialog(null, "END");
            System.exit(1);
        }
    }

    /**
     * 关闭窗口
     */
    public void close() {
        print("END ---");
        System.exit(1);
    }

    /**
     * 增加误差(白移)
     *
     * @param data byte[]
     * @return byte[]
     */
    private byte[] doDeflection(byte[] data) {
        // i, j, a 计数器;
        int i, j, a;
        // x 误差等级, b 彩色模式调整
        int x = config.getDeflection(), b = config.isColorful() ? 1 : 3;
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

    /**
     * 辅助函数
     *
     * @param y int
     * @return int
     */
    private int getHelp(int y) {
        int r, c, i, j, x = config.getDeflection();
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

    /**
     * 减少误差
     *
     * @param data byte[]
     * @return byte[]
     */
    public byte[] reDeflection(byte[] data) {
        if (!config.isColorful()) {
            data = wb2color(data);
        }

        int i, j, r, a, x = config.getDeflection(), l = data.length >> x;
        byte[] res = new byte[l];
        for (i = 0; i < l; i++) {
            r = 0;
            for (j = 0; j < pow(2, x); j++) {
                a = checkDeflection(data[i * pow(2, x) + j]);
                r += ((a + (a < 0 ? 0x100 : 0)) >> j * pow(2, 3 - x)) & getHelp(j);
            }
            res[i] = (byte) r;
        }
        return res;
    }

    /**
     * 黑白长度除三
     *
     * @param data byte[]
     * @return byte[]
     */
    private byte[] wb2color(byte[] data) {
        byte[] res = new byte[data.length / 3];
        for (int i = 0; i < data.length; i += 3) {
            if (data[i] == data[i + 1] || data[i] == data[i + 2]) {
                res[i / 3] = data[i];
            } else if (data[i + 1] == data[i + 2]) {
                res[i / 3] = data[i + 1];
            } else {
                res[i / 3] = (byte) ((data[i] + data[i + 1] + data[i + 2]) / 3);
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
    private int checkDeflection(int a) {
        if (config.getDeflection() == 0) {
            return a;
        }
        int t = pow(2, (8 - pow(2, 3 - config.getDeflection())));
        if (a >= 0) {
            a /= t;
            return a == 0 ? 0 : a * t + t - 1;
        } else {
            a /= t;
            return a == 0 ? -1 : a * t - 1;
        }
    }

    /**
     * 生成文件命名
     *
     * @param suffix    后缀
     * @param newFolder 是否新建文件夹
     * @return 文件名
     */
    public String getFileName(String suffix, boolean newFolder) {
        String cf = config.getDefaultPath() + (newFolder ? separator + fn : "");
        File f = new File(cf);
        if (!f.exists()) {
            f.mkdir();
        }
        return cf + separator + System.currentTimeMillis() + "." + suffix;

    }

    /**
     * 获取分隔符
     *
     * @return separator
     */
    public String separator() {
        return separator;
    }

    /**
     * 将四个byte拼接成一个int
     *
     * @param b byte[4]
     * @return int
     */
    private int byte2Int(byte[] b) {
        return (b[3] & 0xff) << 24 |
                (b[2] & 0xff) << 16 |
                (b[1] & 0xff) << 8 |
                (b[0] & 0xff);
    }

    /**
     * int 转 byte[4]
     *
     * @param data int
     * @return byte[4]
     */
    private byte[] int2Byte(int data) {
        return new byte[]{(byte) (((data) << 24) >> 24),
                (byte) (((data) << 16) >> 24),
                (byte) (((data) << 8) >> 24),
                (byte) ((data) >> 24)};
    }

    /**
     * 乘方 Math.pow(a,b)
     *
     * @param a the base.
     * @param b the exponent.
     * @return the value {@code a}<sup>{@code b}</sup>.
     */
    public int pow(int a, int b) {
        if (b < 0) {
            return 1;
        }
        if (a == 2) {
            return 1 << b;
        }
        int i, r = 1;
        for (i = 0; i < b; i++) {
            r *= a;
        }
        return r;
    }

    /**
     * 获取目录下的文件
     *
     * @param path   路径
     * @param suffix 筛选后缀
     * @return 所有文件
     */
    public List<String> getNames(String path, String suffix) {
        List<String> result = new ArrayList<>();
        File f = new File(path);
        if (f.exists()) {
            for (String s : Objects.requireNonNull(f.list())) {
                if (s.endsWith("." + suffix) || suffix == null) {
                    result.add(path + separator + s);
                }
            }
            Collections.sort(result);
        }
        return result;
    }
}
