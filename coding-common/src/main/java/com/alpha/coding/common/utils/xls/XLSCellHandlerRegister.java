package com.alpha.coding.common.utils.xls;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * XLSCellHandlerRegister
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class XLSCellHandlerRegister implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, CellHandler> beans = applicationContext.getBeansOfType(CellHandler.class);
        for (CellHandler cellHandler : beans.values()) {
            XLSWriter.registerCellHandler(cellHandler);
        }
    }
}
