package com.alpha.coding.common.utils.xls;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alpha.coding.bo.handler.XLSCellHandler;

/**
 * XLSCellHandlerRegister
 *
 * @version 1.0
 */
public class XLSCellHandlerRegister implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            Class.forName("org.apache.poi.ss.usermodel.Cell");
            Map<String, XLSCellHandler> beans = applicationContext.getBeansOfType(XLSCellHandler.class);
            for (XLSCellHandler cellHandler : beans.values()) {
                XLSWriter.registerCellHandler(cellHandler);
            }
        } catch (ClassNotFoundException e) {
            // nothing
        }
    }

}
