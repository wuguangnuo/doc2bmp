/**
 * 内部使用
 * 将文件转成图片
 */
public class Inside {
    private static IO io = IO.getInstance();
    private static Config config = io.getConfig();
    private static int blockSize = config.getBlockSize();

    public static void main(String[] args) {
        String input = io.scanner("输入文件名称：");

        String path = config.getDefaultPath() + io.separator() + input;
        int docLength = io.docLength(path);
        if (docLength > blockSize) {
            int n = docLength / blockSize, r = docLength % blockSize;
            int pageSize = config.getWidth() * config.getWidth() * (config.isColorful() ? 3 : 1) / io.pow(2, config.getDeflection());
            int pageNum = (docLength / pageSize) + (docLength % pageSize == 0 ? 0 : 1);
            io.print("文件大小(byte)=" + docLength + ", 图片数量=" + pageNum + ", 触发大文件处理，分块数=" + (n + (r == 0 ? 0 : 1)));
            for (int i = 0; i < n; i++) {
                io.print("大文件分块，第" + (i + 1) + "块，共" + (n + (r == 0 ? 0 : 1)) + "块");
                writeBmps(io.readDoc(path, i * blockSize, blockSize));
            }
            if (r != 0) {
                io.print("大文件分块，第" + (n + 1) + "块，共" + (n + 1) + "块");
                writeBmps(io.readDoc(path, n * blockSize, docLength % blockSize));
            }
        } else {
            writeBmps(io.readDoc(path));
        }

        io.print("END ---");
    }

    private static void writeBmps(byte[] data) {
        int pageSize = config.getWidth() * config.getWidth() * (config.isColorful() ? 3 : 1) / io.pow(2, config.getDeflection());
        int pageNum = (data.length / pageSize) + (data.length % pageSize == 0 ? 0 : 1);
        io.print("文件大小(byte)=" + data.length + ", 图片数量=" + pageNum);
        byte[] dataTrans;
        for (int i = 0; i < pageNum; i++) {
            dataTrans = new byte[pageSize];
            for (int j = 0; j < pageSize; j++) {
                if (i * pageSize + j >= data.length) {
                    break;
                }
                dataTrans[j] = data[i * pageSize + j];
            }
            io.writeBmp(dataTrans);
        }
    }
}