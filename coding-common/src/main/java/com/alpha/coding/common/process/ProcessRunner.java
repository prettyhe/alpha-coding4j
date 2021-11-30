package com.alpha.coding.common.process;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.common.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessRunner
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class ProcessRunner {

    private static final long DEFAULT_SLEEP_MS = 2000;
    private static final int INIT_EXIT_CODE = 0;
    private static final int ERR_EXIT_CODE = 1;

    /**
     * 执行process
     *
     * @param clazz 要执行的Process
     * @param args  执行参数
     */
    public static void run(AbstractApplicationContext ctx, Class<? extends BaseExecutor> clazz, String... args) {
        final String simpleName = clazz.getSimpleName();
        initProcess(null, simpleName);
        log.info("Process {} start with args={}", simpleName, JSON.toJSONString(args));
        int sysCode = INIT_EXIT_CODE;
        long startTime = System.currentTimeMillis();
        try {
            BaseExecutor exec = ctx.getBean(clazz);
            exec.execute(args);
            log.info("Process {} execute success.", simpleName);
        } catch (Throwable t) {
            sysCode = ERR_EXIT_CODE;
            log.error("Process {} execute error.", simpleName, t);
        } finally {
            if (ctx != null) {
                ctx.registerShutdownHook();
            }
            long endTime = System.currentTimeMillis();
            log.info("Process {} finish and cost {}ms, explain as {}",
                    simpleName, (endTime - startTime), DateUtils.formatMinusDate(startTime, endTime));
        }
        sleep(DEFAULT_SLEEP_MS);
        System.exit(sysCode);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // nothing to do
        }
    }

    /**
     * 执行process
     *
     * @param context 执行process的上下文
     */
    public static void run(AbstractApplicationContext ctx, ProcessContext context) {
        if (context.getBeforeProcess() != null) {
            log.info("------ do beforeFilter process ------");
            context.getBeforeProcess().call(ctx);
        }
        String simpleName = context.getClazz().getSimpleName();
        initProcess(null, simpleName);
        log.info("Process {} start with args={}", simpleName, JSON.toJSONString(context.getArgs()));
        int sysCode = context.getOkExitCode() != null ? context.getOkExitCode().intValue() : INIT_EXIT_CODE;
        final long startTime = System.currentTimeMillis();
        try {
            BaseExecutor exec = ctx.getBean(context.getClazz());
            exec.execute(context.getArgs());
            log.info("Process {} execute success.", simpleName);
        } catch (Throwable t) {
            sysCode = context.getErrExitCode() != null ? context.getErrExitCode().intValue() : ERR_EXIT_CODE;
            log.error("Process {} execute error.", simpleName, t);
            if (context.getExceptionHandler() != null) {
                log.info("------ handle process exception ------");
                context.getExceptionHandler().handle(ctx, t);
            }
        } finally {
            if (context.getAfterProcess() != null) {
                log.info("------ do afterFilter process ------");
                context.getAfterProcess().call(ctx);
            }
            CustomShutdownHook customShutdownHook = context.getCustomShutdownHook();
            if (customShutdownHook != null) {
                Runnable[] runnables = customShutdownHook.genShutdownHooks(ctx);
                if (runnables != null && runnables.length > 0) {
                    for (Runnable runnable : runnables) {
                        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
                    }
                }
            }
            if (ctx != null) {
                ctx.registerShutdownHook();
            }
            long endTime = System.currentTimeMillis();
            log.info("Process {} finish and cost {}ms, explain as {}",
                    simpleName, (endTime - startTime), DateUtils.formatMinusDate(startTime, endTime));
        }
        sleep(DEFAULT_SLEEP_MS);
        System.exit(sysCode);
    }

    private static void initProcess(Class<?> mainClazz, String processName) {
        if (StringUtils.isEmpty(System.getProperty("processName"))) {
            setSystemProperty("processName", mainClazz != null ? mainClazz.getSimpleName() : processName);
        }
        if (StringUtils.isEmpty(System.getProperty("hostName"))) {
            try {
                setSystemProperty("hostName", InetAddress.getLocalHost().getHostName());
            } catch (Throwable e) {
                log.warn("{}", e.getMessage());
            }
        }
        if (StringUtils.isEmpty(System.getProperty("pid"))) {
            try {
                setSystemProperty("pid", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            } catch (Throwable e) {
                log.warn("{}", e.getMessage());
            }
        }
    }

    private static void setSystemProperty(String key, String value) {
        try {
            System.setProperty(key, value);
        } catch (Throwable e) {
            log.warn("set {}={} error, {}", key, value, e.getMessage());
        }
    }

}
