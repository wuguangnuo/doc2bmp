import java.util.List;

/**
 * 外部使用
 * 将图片转文件
 */
public class Outside {
    private static IO io = IO.getInstance();
    private static Config config = io.getConfig();
    private static byte[] compare = null;

    public static void main(String[] args) {
        String input = io.scanner("输入文件夹名：");
        int length = Integer.parseInt(io.scanner("输入文件大小(byte)：").replaceAll(",", ""));

        wash(config.getDefaultPath() + io.separator() + input);

        List<String> bmpFiles = io.getNames(config.getDefaultPath() + io.separator() + input, "bmp");

        byte[] data = new byte[length];
        for (int i = 0; i < bmpFiles.size(); i++) {
            String path = bmpFiles.get(i);
            byte[] dataTrans = io.reDeflection(io.readBmp(path));
            for (int j = 0; j < dataTrans.length; j++) {
                if (dataTrans.length * i + j >= length) {
                    break;
                }
                data[i * dataTrans.length + j] = dataTrans[j];
            }
        }
        io.writeDoc(data, config.getSuffix());

        io.end();
    }

    /**
     * 清洗数据
     *
     * @param path 文件路径
     */
    private static void wash(String path) {
        io.print("开始清洗数据");
        List<String> names = io.getNames(path, "bmp");

        for (int i = 0; i < names.size(); i++) {
            io.print("清洗第" + (i + 1) + "个");
            check(names.get(i));
        }
    }

    /**
     * check and delete
     *
     * @param n bpm file name
     */
    private static void check(String n) {
        byte[] data = io.readBmp(n);
        if (data.length != config.getWidth() * config.getWidth() * 3) {
            io.deleteFile(n);
            io.print("删除原因:图片错误");
            return;
        }

        for (int i = 0; i < config.getWidth(); i++) {
            if (!accept(data[(int) (Math.random() * config.getWidth() * config.getWidth() * 3)])) {
                io.deleteFile(n);
                io.print("删除原因:严格模式");
                return;
            }
        }

        if (compare == null) {
            compare = data;
        } else {
            for (int i = 0; i < data.length; i++) {
                if (compare[i] != data[i]) {
                    compare = data;
                    return;
                }
            }
            io.deleteFile(n);
        }
    }

    // 此算法只适用于 colorful = false, deflection = 2
    public static boolean accept(byte x) {
        int a = Math.abs(x + 65);
        int b = Math.abs(x + 1);
        int c = Math.abs(x - 127);
        int d = Math.abs(x);
        int y = Math.min(Math.min(a, b), Math.min(c, d));
        return y == 0; // 最严格
    }
}
