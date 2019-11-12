import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

public class Main {
    private static Config config = new Config();
    private static long timestmp = System.currentTimeMillis();

    public static void main(String[] args) {
        initialize();

        switch (config.getModel()) {
            case 1:
                // 读取文件并生成图片
                writeBmp(readDoc(config.getDefaultPath() + "\\" + config.getDocFileName()));
                break;
            case 2:
                // 读取图片并还原文件
                writeDoc(readBmp(config.getDefaultPath() + "\\" + config.getBmpFileName()), config.getSuffix());
                break;
        }

        print("END ---");
    }

    /**
     * 初始化配置
     */
    private static void initialize() {
        String s, configFile = config.getDefaultPath() + "\\" + config.getConfigFileName();
        try {
            File file = new File(configFile);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                while ((s = br.readLine()) != null) {
                    reFlush(s);
                }
                br.close();
            }
        } catch (IOException e) {
            print("读取配置失败");
            e.printStackTrace();
        } finally {
            print("初始化完成");
        }
    }

    /**
     * 更新配置数据
     *
     * @param s 一行配置
     */
    private static void reFlush(String s) {
        if (s.trim().startsWith("//")) {
            return;
        }
        String[] a = s.trim().split("=");
        if (a.length != 2) {
            return;
        }
        Field[] fields = config.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                if ((field.getModifiers() & 0x10) == 0x10) {
                    continue;
                }
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase(a[0].trim())) {
                    Method method = config.getClass().getDeclaredMethod(
                            "set" + field.getName().substring(0, 1).toUpperCase()
                                    + field.getName().substring(1), String.class
                    );
                    method.invoke(config, a[1].trim());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            print("填充异常[" + e.getCause().getMessage() + "]");
            System.exit(1);
        }
    }

    /**
     * 读取文档文件
     *
     * @param path 文件路径
     * @return byte[]
     */
    private static byte[] readDoc(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] data = new byte[bis.available()];
            bis.read(data);
            print("ReadDoc Complete");
            bis.close();
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            print("读取文件错误");
        } catch (IOException e) {
            print("IO异常");
        }
        return null;
    }

    /**
     * 读取BMP文件(BMP24位)
     *
     * @param path 文件路径
     * @return byte[]
     */
    private static byte[] readBmp(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(fis);

            bis.skip(18L);
            byte[] b = new byte[4];
            bis.read(b);
            byte[] b2 = new byte[4];
            bis.read(b2);

            int width = byte2Int(b), heigth = byte2Int(b2);
            byte[] date = new byte[heigth * width * 3 - config.getComplementary()];
            print("Height：" + heigth + "，Width：" + width);

            bis.skip(28L);
            bis.read(date);
            print("ReadBmp Complete");
            bis.close();
            fis.close();
            return date;
        } catch (FileNotFoundException e) {
            print("读取文件错误");
        } catch (IOException e) {
            print("IO异常");
        }
        return null;
    }

    /**
     * 写入文档文件
     *
     * @param data   byte[]
     * @param suffix 后缀
     */
    private static void writeDoc(byte[] data, String suffix) {
        data = reDeflection(data);

        String fileName = getFileName(suffix);
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(data, 0, data.length);
            print("WriteDoc Success");
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            print("创建文件失败");
        } catch (IOException e) {
            print("IO异常");
        }
    }

    /**
     * 写入BMP文件
     *
     * @param data byte[]
     */
    private static void writeBmp(byte[] data) {
        data = doDeflection(data);

        int width = config.getWidth();
        int height = data.length % (3 * width) == 0 ? data.length / 3 / width : data.length / 3 / width + 1;
        print("补位大小：" + (width * height * 3 - data.length) + "Byte");

        String fileName = getFileName("bmp");
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
            bos.write(new byte[width * height * 3 - data.length]);

            bos.close();
            fos.close();
            print("WriteBmp Success");
        } catch (FileNotFoundException e) {
            print("创建文件失败");
        } catch (IOException e) {
            print("IO异常");
        }
    }

    /**
     * 增加误差(白移)
     *
     * @param data byte[]
     * @return byte[]
     */
    private static byte[] doDeflection(byte[] data) {
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
    private static int getHelp(int y) {
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
    private static byte[] reDeflection(byte[] data) {
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
    private static byte[] wb2color(byte[] data) {
        byte[] res = new byte[data.length / 3];
        for (int i = 0; i < data.length; i += 3) {
            if (data[i] == data[i + 1] && data[i] == data[i + 2]) {
                res[i / 3] = data[i];
            } else {
                if (data[i] == data[i + 1] || data[i] == data[i + 2]) {
                    res[i / 3] = data[i];
                } else if (data[i + 1] == data[i + 2]) {
                    res[i / 3] = data[i + 1];
                } else {
                    res[i / 3] = (byte) ((data[i] + data[i + 1] + data[i + 2]) / 3);
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
        if (config.getDeflection() == 0) {
            return a;
        }
        int t = pow(2, (8 - pow(2, 3 - config.getDeflection())));
        if (a >= 0) {
            a /= t;
            if (a == 0) {
                return 0;
            } else {
                return a * t + t - 1;
            }
        } else {
            a /= t;
            if (a == 0) {
                return -1;
            } else {
                return a * t - 1;
            }
        }
    }

    /**
     * 生成文件名
     *
     * @param suffix 文件后缀
     * @return FilePath
     */
    private static String getFileName(String suffix) {
        return config.getDefaultPath() + "\\" + LocalDateTime.now().toString()
                .replaceAll(":", "-")
                .replaceAll("T", " ")
                .substring(0, 19) + "." + suffix;
    }

    /**
     * 将四个byte拼接成一个int
     *
     * @param b byte[4]
     * @return int
     */
    private static int byte2Int(byte[] b) {
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
    private static byte[] int2Byte(int data) {
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
    private static int pow(int a, int b) {
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
     * 格式化打印
     *
     * @param x 打印的内容
     */
    private static void print(String x) {
        long n = System.currentTimeMillis();
        String msg = "--- " + x + (config.isDebug() ? " " + (n - timestmp) + "ms" : "") + " ;";
        System.out.println(msg);

        if (config.isDebug()) {
            String logFileName = config.getDefaultPath() + "\\" + config.getLogFileName();
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
}

class Config {
    // 默认路径（读取、输出文件均在此文件夹下）
    private final String defaultPath = System.getProperty("user.dir");
    // 配置文件
    private final String configFileName = "config.txt";
    // 日志文件
    private final String logFileName = "log.txt";

    // 模式 文件转图片:1 图片转文件:2
    private int model = 1;
    // 待读取的文件名
    private String docFileName = "s.zip";
    // 待读取图片名
    private String bmpFileName = "s.bmp";
    // 生成的文件后缀
    private String suffix = "zip";
    // 图片宽度(像素)
    private int width = 1024;
    // 补位大小 complementary 大小（Byte）
    private int complementary = 0;
    // 误差模式 (加强抗干扰能力: 0~3)
    private int deflection = 0;
    // 彩色模式
    private boolean colorful = true;
    // Debug 模式
    private boolean debug = true;

    public String getDefaultPath() {
        return defaultPath;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public int getModel() {
        return model;
    }

    public String getDocFileName() {
        return docFileName;
    }

    public String getBmpFileName() {
        return bmpFileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getWidth() {
        return width;
    }

    public int getComplementary() {
        return complementary;
    }

    public int getDeflection() {
        return deflection;
    }

    public boolean isColorful() {
        return colorful;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setModel(String model) {
        if ("1".equals(model)) {
            this.model = 1;
        } else if ("2".equals(model)) {
            this.model = 2;
        } else {
            throw new RuntimeException("config.model 错误");
        }
    }

    public void setDocFileName(String docFileName) {
        this.docFileName = docFileName;
    }

    public void setBmpFileName(String bmpFileName) {
        String[] f = bmpFileName.split("[.]");
        if (f.length < 2 || !"bmp".equalsIgnoreCase(f[f.length - 1])) {
            throw new RuntimeException("config.bmpFileName 错误,应为bmp格式");
        }
        this.bmpFileName = bmpFileName;
    }

    public void setSuffix(String suffix) {
        if (suffix.contains(".")) {
            throw new RuntimeException("config.suffix 错误,不能包含‘.’");
        }
        this.suffix = suffix;
    }

    public void setWidth(String width) {
        this.width = Integer.parseInt(width);
    }

    public void setComplementary(String complementary) {
        this.complementary = Integer.parseInt(complementary);
    }

    public void setDeflection(String deflection) {
        int d = Integer.parseInt(deflection);
        if (d < 0 || d > 3) {
            throw new RuntimeException("config.deflection 错误,取值0~3整数");
        }
        this.deflection = d;
    }

    public void setColorful(String colorful) {
        if ("true".equalsIgnoreCase(colorful)) {
            this.colorful = true;
        } else if ("false".equalsIgnoreCase(colorful)) {
            this.colorful = false;
        } else {
            throw new RuntimeException("config.colorful 错误");
        }
    }

    public void setDebug(String debug) {
        if ("true".equalsIgnoreCase(debug)) {
            this.debug = true;
        } else if ("false".equalsIgnoreCase(debug)) {
            this.debug = false;
        } else {
            throw new RuntimeException("config.debug 错误");
        }
    }

    @Override
    public String toString() {
        return "Config{" +
                "defaultPath='" + defaultPath + '\'' +
                ", configFileName='" + configFileName + '\'' +
                ", logFileName='" + logFileName + '\'' +
                ", model=" + model +
                ", docFileName='" + docFileName + '\'' +
                ", bmpFileName='" + bmpFileName + '\'' +
                ", width=" + width +
                ", complementary=" + complementary +
                ", deflection=" + deflection +
                ", colorful=" + colorful +
                ", debug=" + debug +
                '}';
    }
}
