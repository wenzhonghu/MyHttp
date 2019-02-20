package com.xiaoniu.finance.myhttp.statistic;

import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.statistic.processor.AbstractProcessor;
import com.xiaoniu.finance.myhttp.statistic.processor.TerminalProcessor;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by zhonghu on 2019/1/29.
 */

public class StatisticProcesser {

    /**
     * 统计模式
     */
    public enum Mode {
        TERMINAL_MODE,
        FILE_MODE,
        NET_MODE
    }

    public static class ProcessorOption {

        /**
         * 推迟时间执行 ，单位毫秒
         */
        public long delayTime = 0L;
        /**
         * 模式
         */
        public Mode mode = Mode.TERMINAL_MODE;
        /**
         * 间隔时间 ，单位秒
         */
        public long mIntervalTime = 0L;

        public ProcessorOption() {

        }

        public ProcessorOption(long delayTime, Mode mode, long mIntervalTime) {
            this.delayTime = delayTime;
            this.mode = mode;
            this.mIntervalTime = mIntervalTime;
        }

        /**
         * 终端模式下的参数配置
         */
        public static ProcessorOption DEFAULT_TERMINAL = new ProcessorOption();

        /**
         * 文件模式下的参数配置
         */
        public static ProcessorOption DEFAULT_FILE = new ProcessorOption(1000L, Mode.FILE_MODE, 30 * 60);

        /**
         * 网络模式下的参数配置
         */
        public static ProcessorOption DEFAULT_NET = new ProcessorOption(1000L, Mode.NET_MODE, 30 * 60);

    }

    /**
     * 根据模式缓存处理器
     */
    private static HashMap<Mode, AbstractProcessor> POOL_MANAGER_MAP = new HashMap<>();


    private static StatisticProcesser sInstance = new StatisticProcesser();

    private StatisticProcesser() {
        POOL_MANAGER_MAP.put(Mode.TERMINAL_MODE, new TerminalProcessor(ProcessorOption.DEFAULT_TERMINAL));
    }

    public static StatisticProcesser getInstance() {
        return sInstance;
    }


    /**
     * 添加处理器
     */
    public void addProcessor(Mode mode, AbstractProcessor processor) {
        POOL_MANAGER_MAP.put(mode, processor);
    }

    /**
     * 启动统计系统
     */
    public void startProcessors() {
        for (Entry<Mode, AbstractProcessor> entry : POOL_MANAGER_MAP.entrySet()) {
            if (Global.isDebug()) {
                if (entry.getKey() == Mode.TERMINAL_MODE) {
                    AbstractProcessor processor = entry.getValue();
                    processor.init();
                    processor.startup();
                }
            } else {
                AbstractProcessor processor = entry.getValue();
                processor.init();
                processor.startup();
            }
        }
    }

    public void terminal() {
        for (Entry<Mode, AbstractProcessor> entry : POOL_MANAGER_MAP.entrySet()) {
            AbstractProcessor processor = entry.getValue();
            processor.shutdown();
        }
    }

}


