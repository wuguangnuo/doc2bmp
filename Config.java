import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 统一配置
 */
public class Config {
    // 默认路径（读取、输出文件均在此文件夹下）
    //private final String defaultPath = System.getProperty("user.dir");
    private final String defaultPath;
    // 配置文件
    private final String configFileName;
    // 日志文件
    private final String logFileName;

    // 生成的文件后缀
    private String suffix;
    // 图片宽度(像素)
    private int width;
    // 误差模式 (加强抗干扰能力: 0~3)
    private int deflection;
    // 彩色模式
    private boolean colorful;
    // Debug 模式
    private boolean debug;
    // 延时间隔
    private int delay;
    // 文件分块大小(单位MB)
    private int blockSize;

    public Config() {
        this.defaultPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + File.separator + "stealFile";
        this.configFileName = "config.txt";
        this.logFileName = "log.txt";
        this.suffix = "zip";
        this.width = 1024;
        this.deflection = 2;
        this.colorful = false;
        this.debug = true;
        this.delay = 1000;
        this.blockSize = 50;

        initialize();
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getWidth() {
        return width;
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

    public int getDelay() {
        return delay;
    }

    public int getBlockSize() {
        return blockSize * 1024 * 1024;
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

    public void setDelay(String delay) {
        this.delay = Integer.parseInt(delay);
    }

    public void setBlockSize(String blockSize) {
        this.blockSize = Integer.parseInt(blockSize);
    }

    /**
     * 初始化配置
     */
    private void initialize() {
        print("\r\n--- =*= <<< Steal File >>> =*= ---");
        String s, configFile = this.getDefaultPath() + File.separator + this.getConfigFileName();
        try {
            File steal = new File(this.getDefaultPath());
            if (!steal.exists()) {
                steal.createNewFile();
            }

            File file = new File(configFile);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                while ((s = br.readLine()) != null) {
                    reFlush(s);
                }
                br.close();
            }
        } catch (IOException e) {
            print("读取配置[" + getConfigFileName() + "]失败");
        } finally {
            print("初始化[" + getConfigFileName() + "]完成");
        }
    }

    /**
     * 更新配置数据
     *
     * @param s 一行配置
     */
    private void reFlush(String s) {
        if (s.trim().startsWith("//")) {
            return;
        }
        String[] a = s.trim().split("=");
        if (a.length != 2) {
            return;
        }
        Field[] fields = this.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                if ((field.getModifiers() & 0x10) == 0x10) {
                    continue;
                }
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase(a[0].trim())) {
                    Method method = this.getClass().getDeclaredMethod(
                            "set" + field.getName().substring(0, 1).toUpperCase()
                                    + field.getName().substring(1), String.class);
                    method.invoke(this, a[1].trim());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            print("填充异常[" + e.getCause().getMessage() + "]");
            System.exit(1);
        }
    }

    private void print(String msg) {
        System.out.println(msg);

        if (this.isDebug()) {
            String logFileName = this.getDefaultPath() + File.separator + this.getLogFileName();
            try {
                File file = new File(logFileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(logFileName, true)));
                bw.write(msg + "\r\n");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
