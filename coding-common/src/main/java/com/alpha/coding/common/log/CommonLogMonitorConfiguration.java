package com.alpha.coding.common.log;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * CommonLogMonitorConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
@Import(LogMonitorConfiguration.class)
@EnableLogMonitor(logorBean = "annotationLogor", order = 2,
        logConfig = @LogMonitor(logType = LogType.OTHER, isRequestLog = true, isResponseLog = true),
        pointcut = {"@within(com.alpha.coding.common.log.LogMonitor)",
                "|| @annotation(com.alpha.coding.common.log.LogMonitor)"})
@EnableLogMonitor(logorBean = "defaultLogor", order = 3,
        logConfig = @LogMonitor(logType = LogType.SERV_OUT, isRequestLog = true, isResponseLog = true),
        pointcut = {"@within(org.springframework.stereotype.Controller)",
                "|| @annotation(org.springframework.stereotype.Controller)",
                "|| @within(org.springframework.web.bind.annotation.RestController)",
                "|| @annotation(org.springframework.web.bind.annotation.RestController)"})

public class CommonLogMonitorConfiguration {

}
