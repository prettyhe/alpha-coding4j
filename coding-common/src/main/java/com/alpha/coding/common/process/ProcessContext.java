package com.alpha.coding.common.process;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ProcessContext
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class ProcessContext {

    private Class<? extends BaseExecutor> clazz;
    private String[] args;
    private Byte okExitCode;
    private Byte errExitCode;
    private ProcessCallback beforeProcess;
    private ExceptionHandler exceptionHandler;
    private ProcessCallback afterProcess;
    private CustomShutdownHook customShutdownHook;

}
