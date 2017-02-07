# SelectFile
android图片选择器

实现思路

①通过contentResolver获取手机中的图片、视频文件，也可以直接拍摄，这个时候不会重新获取手机里的资源。

②定义一个全局集合，用来存放选中的图片和视频，并用一个变量来表示是视频还是图片。

③将查询出来的图片全部显示的时候，需要防止内存溢出，这是用来一个网上的优化工具类，挺好用的。

④在预览图片的时候使用matrix对图片进行放大缩小，平移处理

效果实现如下

![image](https://github.com/panyiyiyi/SelectFile/blob/master/test1.gif)
