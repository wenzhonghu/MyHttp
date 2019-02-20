package com.xiaoniu.finance.myhttp.statistic.processor;

/**
 * 日志统计处理器
 * 可以根据获取的日志统计进行二次修订为服务端需要的数据格式
 * 例如：你可以转换为点评的cat服务器需要的格式进行上传部署显示
 * https://github.com/dianping/cat
 *
 * Created by zhonghu on 2019/1/29.
 */

public interface AbstractProcessor {

    /**
     * 初始化处理器需要的参数数据
     */
    void init();

    /**
     * 启动处理器服务
     * 此处基本功能实现一个完整的数据统计服务功能
     * 1/启动服务
     * 2/读取数据
     * 3/处理数据
     * 4/休眠等待
     * 5/重新第二步
     */
    void startup();

    /**
     * 关闭处理器服务
     * 回收内存等功能
     */
    void shutdown();

}
