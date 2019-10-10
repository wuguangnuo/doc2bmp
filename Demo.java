import java.io.*;
import java.time.LocalDateTime;

public class Demo {
    // 默认路径（读取、输出文件均在此文件夹下）
    private static final String DEFAULTPATH = "C:\\Users\\Administrator\\Desktop\\test\\";
    // 待读取的文件名
    private static final String DOCFILENAME = "s.zip";
    // 待读取图片名
    private static final String BMPFILENAME = "s.bmp";
    // 图片宽度(像素)
    private static final int WIDTH = 1024;
    // 补位 complementary 大小（Byte）
    private static final int COMP = 1013;

    public static void main(String[] args) {
        writeBmp(readDoc(DEFAULTPATH + DOCFILENAME));

//        writeDoc(readBmp(DEFAULTPATH + BMPFILENAME), "zip");

        print("END ---");
    }

    /**
     * 读取BMP文件(BMP24位)
     *
     * @param path 文件路径
     * @return char[]
     */
    private static char[] readBmp(String path) {
        try {
            // 创建读取文件的字节流
            FileInputStream fis = new FileInputStream(path);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // 读取时丢掉前面的18位，
            // 读取图片的18~21的宽度
            bis.skip(18);
            byte[] b = new byte[4];
            bis.read(b);
            // 读取图片的高度22~25
            byte[] b2 = new byte[4];
            bis.read(b2);

            // 得到图片的高度和宽度
            int width = byte2Int(b), heigth = byte2Int(b2);
            // 使用数组保存得图片的高度和宽度
            char[] date = new char[heigth * width * 3 - COMP];
            print("Height：" + heigth + "，Width：" + width);

            // 读取位图中的数据，位图中数据时从54位开始的，在读取数据前要丢掉前面的数据
            bis.skip(28);
            for (int i = 0; i < heigth * width * 3 - COMP; i++) {
                date[i] = (char) bis.read();
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
     * @return char[]
     */
    private static char[] readDoc(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            int n = fis.available();
            char[] data = new char[n];
            for (int i = 0; i < n; i++) {
                data[i] = (char) fis.read();
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
     * @param data char[]
     */
    private static void writeBmp(char[] data) {
        if (data == null) {
            print("数据异常");
            return;
        }

        int height = data.length % (3 * WIDTH) == 0 ? data.length / 3 / WIDTH : data.length / 3 / WIDTH + 1;
        print("补位大小：" + (WIDTH * height * 3 - data.length) + "Byte");

        String fileName = getFileName("bmp");
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            DataOutputStream dos = new DataOutputStream(fos);

            // 给文件头的变量赋值
            int bfType = 0x424d; // 位图文件类型（0—1字节）
            int bfSize = 54 + WIDTH * height * 3; // bmp文件的大小（2—5字节）
            int bfReserved1 = 0; // 位图文件保留字，必须为0（6-7字节）
            int bfReserved2 = 0; // 位图文件保留字，必须为0（8-9字节）
            int bfOffBits = 54; // 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

            // 输入数据的时候要注意输入的数据在内存中要占几个字节，
            // 然后再选择相应的写入方法，而不是它自己本身的数据类型
            // 输入文件头数据
            dos.writeShort(bfType); // 输入位图文件类型'BM'
            dos.write(int2Byte(bfSize), 0, 4); // 输入位图文件大小
            dos.write(int2Byte(bfReserved1), 0, 2); // 输入位图文件保留字
            dos.write(int2Byte(bfReserved2), 0, 2); // 输入位图文件保留字
            dos.write(int2Byte(bfOffBits), 0, 4); // 输入位图文件偏移量

            // 给信息头的变量赋值
            int biSize = 40; // 信息头所需的字节数（14-17字节）
            int biWidth = WIDTH; // 位图的宽（18-21字节）
            int biHeight = height; // 位图的高（22-25字节）
            int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
            int biBitcount = 24; // 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
            int biCompression = 0; // 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
            int biSizeImage = WIDTH * height; // 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
            int biXPelsPerMeter = 0; // 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
            int biYPelsPerMeter = 0; // 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
            int biClrUsed = 0; // 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
            int biClrImportant = 0; // 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

            // 因为java是大端存储，那么也就是说同样会大端输出。
            // 但计算机是按小端读取，如果我们不改变多字节数据的顺序的话，那么机器就不能正常读取。
            // 所以首先调用方法将int数据转变为多个byte数据，并且按小端存储的顺序。

            // 输入信息头数据
            dos.write(int2Byte(biSize), 0, 4); // 输入信息头数据的总字节数
            dos.write(int2Byte(biWidth), 0, 4); // 输入位图的宽
            dos.write(int2Byte(biHeight), 0, 4); // 输入位图的高
            dos.write(int2Byte(biPlanes), 0, 2); // 输入位图的目标设备级别
            dos.write(int2Byte(biBitcount), 0, 2); // 输入每个像素占据的字节数
            dos.write(int2Byte(biCompression), 0, 4); // 输入位图的压缩类型
            dos.write(int2Byte(biSizeImage), 0, 4); // 输入位图的实际大小
            dos.write(int2Byte(biXPelsPerMeter), 0, 4); // 输入位图的水平分辨率
            dos.write(int2Byte(biYPelsPerMeter), 0, 4); // 输入位图的垂直分辨率
            dos.write(int2Byte(biClrUsed), 0, 4); // 输入位图使用的总颜色数
            dos.write(int2Byte(biClrImportant), 0, 4); // 输入位图使用过程中重要的颜色数

            // 因为是24位图，所以没有颜色表
            // 通过遍历输入位图数据
            // 这里遍历的时候注意，在计算机内存中位图数据是从左到右，从下到上来保存的，
            // 也就是说实际图像的第一行的点在内存是最后一行
            int x = 0, y = data.length;
            for (int i = height - 1; i >= 0; i--) {
                for (int j = 0; j < WIDTH; j++) {
                    dos.write(x < y ? int2Byte(data[x]) : int2Byte(0x00), 0, 1);
                    dos.write(x + 1 < y ? int2Byte(data[x + 1]) : int2Byte(0x00), 0, 1);
                    dos.write(x + 2 < y ? int2Byte(data[x + 2]) : int2Byte(0x00), 0, 1);
                    x += 3;
                }
            }
            //关闭数据的传输
            dos.flush();
            dos.close();
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
     * @param data   char[]
     * @param suffix 后缀
     */
    private static void writeDoc(char[] data, String suffix) {
        String fileName = getFileName(suffix);
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            for (char c : data) {
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
        return (b[3] & 0xff << 24) | (b[2] & 0xff) << 16 | (b[1] & 0xff) << 8 | b[0] & 0xff;
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
        System.out.println("--- " + x + " ;");
    }

    /**
     * 生成文件名
     *
     * @param suffix 文件后缀
     * @return FilePath
     */
    private static String getFileName(String suffix) {
        return DEFAULTPATH + LocalDateTime.now().toString().replaceAll(":", "-").replaceAll("T", " ").substring(0, 19) + "." + suffix;
    }
}
