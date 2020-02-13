# stealFile

- 需求：将云桌面中的文件拷贝出来。（云桌面可以读取本地磁盘，反之不可以）
- 思路：云桌面下，将文件转换成图片；本机截图，并将图片还原成文件
- 功能：实现所有格式的文件与BMP格式图片间的转换

### 配置文件(config.txt)说明:
    
    // 生成的文件后缀
    suffix = zip
    // 图片宽度(像素)
    width = 1024
    // 误差模式 (加强抗干扰能力: 0~3)
    deflection = 2
    // 彩色模式
    colorful = false
    // Debug 模式
    debug = true
    // 延时间隔(单位ms)
    delay = 1000
    // 文件分块大小(单位MB)
    blockSize = 50

### 使用方法：

#### 云桌面环境下
```
java Inside
java ShowBmp
```
#### 本机环境下
```
java ScreenShot
java Outside
```

### 注意事项：
> - 默认工作文件夹位置：桌面/stealFile
> - 云桌面运行于全屏模式下
> - 云桌面执行ShowBmp.class的同时在本机执行ScreenShot.class
>