import java.io.*;
import java.time.LocalDateTime;

public class Demo {
    // 默认路径（读取、输出文件均在此文件夹下）
    private static String DEFAULTPATH = "C:\\Users\\Administrator\\Desktop\\test\\";
    // 待读取的文件名
    private static String DOCFILENAME = "w.bmp";
    // 待读取图片名
    private static String BMPFILENAME = "2019-10-25 15-37-03.bmp";
    // 配置文件
    private static final String CONFIG = "config.txt";
    // 图片宽度(像素)
    private static int WIDTH = 100;
    // 补位大小 complementary 大小（Byte）
    private static int COMP = 0;
    // 误差模式 (加强抗干扰能力: 0~3)
    private static int DEFLECTION = 3;
    // Debug 模式
    private static boolean ENDEBUG = true;
    private static long timestmp = System.currentTimeMillis();

    public static void main(String[] args) {
        initialize();
        // 读取文件并生成图片
        writeBmp(doDeflection(readDoc(DEFAULTPATH + DOCFILENAME)));

        // 读取图片并还原文件
//        writeDoc(readBmp(DEFAULTPATH + BMPFILENAME), "txt");

        print("END ---");
    }

    /**
     * 初始化配置
     */
    private static void initialize() {
        StringBuilder sb = new StringBuilder();
        String s;
        try {
            File f = new File(DEFAULTPATH + CONFIG);
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(DEFAULTPATH + CONFIG));
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
     * @param s
     */
    private static void reFlush(String s) {
        if (s.trim().startsWith("//")) {
            return;
        }
        String[] a = s.trim().split("=");
        if (a.length != 2) {
            return;
        }
        switch (a[0].trim()) {
            case "DEFAULTPATH":
                DEFAULTPATH = a[1].trim();
                break;
            case "DOCFILENAME":
                DOCFILENAME = a[1].trim();
                break;
            case "BMPFILENAME":
                BMPFILENAME = a[1].trim();
                break;
            case "WIDTH":
                WIDTH = Integer.parseInt(a[1].trim());
                break;
            case "COMP":
                COMP = Integer.parseInt(a[1].trim());
                break;
            case "ENDEBUG":
                ENDEBUG = "true".equals(a[1].trim().toLowerCase());
                break;
        }
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
            byte[] date = new byte[heigth * width * 3 - COMP];
            print("Height：" + heigth + "，Width：" + width);

            bis.skip(28L);
            for (int i = 0; i < heigth * width * 3 - COMP; i++) {
                date[i] = (byte) bis.read();
            }
            print("ReadBmp Complete");
            fis.close();
            bis.close();
            return date;
        } catch (FileNotFoundException e) {
            print("读取文件错误");
        } catch (IOException e) {
            print("IO异常");
        }
        return null;
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
            int n = fis.available();
            byte[] data = new byte[n];
            for (int i = 0; i < n; i++) {
                data[i] = (byte) fis.read();
            }
            print("ReadDoc Complete");
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
     * 写入BMP文件
     *
     * @param data byte[]
     */
    private static void writeBmp(byte[] data) {
        int height = data.length % (3 * WIDTH) == 0 ? data.length / 3 / WIDTH : data.length / 3 / WIDTH + 1;
        print("补位大小：" + (WIDTH * height * 3 - data.length) + "Byte");

        String fileName = getFileName("bmp");
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // 给文件头的变量赋值
            bos.write(new byte[]{0x42, 0x4d}, 0, 2);
            bos.write(int2Byte(54 + WIDTH * height * 3), 0, 4);
            bos.write(new byte[]{0, 0, 0, 0, 0x36, 0, 0, 0, 0x28, 0, 0, 0}, 0, 12);
            bos.write(int2Byte(WIDTH), 0, 4);
            bos.write(int2Byte(height), 0, 4);
            bos.write(new byte[]{1, 0, 0x18, 0, 0, 0, 0, 0}, 0, 8);
            bos.write(int2Byte(WIDTH * height), 0, 4);
            bos.write(new byte[16], 0, 16);

            int x = 0, y = data.length;
            for (int i = height - 1; i >= 0; i--) {
                for (int j = 0; j < WIDTH; j++) {
                    bos.write(x < y ? new byte[]{(byte) data[x]} : new byte[1], 0, 1);
                    bos.write(x + 1 < y ? new byte[]{(byte) data[x + 1]} : new byte[1], 0, 1);
                    bos.write(x + 2 < y ? new byte[]{(byte) data[x + 2]} : new byte[1], 0, 1);
                    x += 3;
                }
            }
            bos.flush();
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
     * 写入文档文件
     *
     * @param data   byte[]
     * @param suffix 后缀
     */
    private static void writeDoc(byte[] data, String suffix) {
        if (DEFLECTION != 0) {
            data = reDeflection(data);
        }

        String fileName = getFileName(suffix);
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            for (byte c : data) {
                fos.write(c);
            }
            print("WriteDoc Success");
            fos.close();
        } catch (FileNotFoundException e) {
            print("创建文件失败");
        } catch (IOException e) {
            print("IO异常");
        }
    }

    /**
     * 将四个byte拼接成一个int
     *
     * @param b byte[4]
     * @return int
     */
    private static int byte2Int(byte[] b) {
        return (b[3] & 0xff) << 24 | (b[2] & 0xff) << 16 | (b[1] & 0xff) << 8 | b[0] & 0xff;
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
     * 格式化打印
     *
     * @param x 打印的内容
     */
    private static void print(String x) {
        long n = System.currentTimeMillis();
        System.out.println("--- " + x + (ENDEBUG ? " " + (n - timestmp) + "ms" : "") + " ;");
        timestmp = n;
    }

    /**
     * 生成文件名
     *
     * @param suffix 文件后缀
     * @return FilePath
     */
    private static String getFileName(String suffix) {
        return DEFAULTPATH + LocalDateTime.now().toString()
                .replaceAll(":", "-")
                .replaceAll("T", " ")
                .substring(0, 19) + "." + suffix;
    }

    /**
     * 增加误差
     *
     * @param data byte[]
     * @return byte[]
     */
    private static byte[] doDeflection(byte[] data) {
        /**
         * 原数 -> 16进制-> *16
         * 100 -> 0x64 -> (6*16,4*16)
         */
        int i = 0, j = 0, l = data.length, m = (int) Math.pow(2, DEFLECTION);
        // m = 1,2,4,8
        byte[] res = new byte[l << DEFLECTION];
        for (; i < l; i++) {
            if (DEFLECTION == 1) {
                res[i << 1] = (byte) (data[i] & 0xf0); // 0110 0000
                res[(i << 1) + 1] = (byte) ((data[i] & 0xf) << 4); // 0100 0000
            }
            if (DEFLECTION == 2) {
                res[i << 2] = (byte) (data[i] & 0xc0); // 0100 0000
                res[(i << 2) + 1] = (byte) ((data[i] & 0x30) << 2); // 1000 0000
                res[(i << 2) + 2] = (byte) ((data[i] & 0xc) << 4); // 0100 0000
                res[(i << 2) + 3] = (byte) ((data[i] & 0x3) << 6); // 0000 0000
            }
            if (DEFLECTION == 3) {
                for (j = 0; j < m; j++) {
                    res[(i << DEFLECTION) + j] = (byte) ((data[i] & getFixNum(j)) << j * 8 / m);
                }
            }
        }
        return fillLight(res);
    }

    /**
     * 补光
     *
     * @param data byte[]
     * @return byte[]
     */
    private static byte[] fillLight(byte[] data) {
        int i = 0, l = data.length;
        for (; i < l; i++) {
            if (DEFLECTION == 1) {
                data[i] += 8;
            }
            if (DEFLECTION == 2) {
                data[i] += 32;
            }
            if (DEFLECTION == 3) {
                data[i] += 64;
            }
        }
        return data;
    }

    // & num
    private static int getFixNum(int j) {

        int[] a = {128, 64, 32, 16, 8, 4, 2, 1};
        return a[j];
//        int m = (int) Math.pow(2, DEFLECTION);
//        int i = 0;
//        int res = 0;
//        switch (DEFLECTION) {
//            case 3:
//                res = a[j];
//                break;
//            case 2:
//                for (; i < m; i++) {
//
//                }
//                break;
//            case 1:
//                break;
//            case 0:
//                res = 0xff;
//                break;
//        }
//
//        return res;
    }

    /**
     * 减少误差
     *
     * @param data byte[]
     * @return byte[]
     */
    private static byte[] reDeflection(byte[] data) {
        /**
         * 误差数据 ->  纠正        -> 原来数据
         * (95,49) -> (6*16,4*16) -> 100
         */
        if ((data.length & 1) == 1) {
            print("校验异常");
            return null;
        }
        int i = 0, l = data.length;
        byte[] res = new byte[l >> 1];
        for (; i < l; i += 2) {
            res[i >> 1] = (byte) ((checkDeflection(data[i]) << 4) + checkDeflection(data[i + 1]));
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
}
