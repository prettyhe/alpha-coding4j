package com.alpha.coding.common.dubbo.filter;

import java.util.Locale;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.constant.Keys;

import lombok.extern.slf4j.Slf4j;

/**
 * LocaleRecognizeFilter
 *
 * @version 1.0
 * Date: 2020/4/11
 */
@Slf4j
@Activate(group = Constants.PROVIDER, order = -180)
public class LocaleRecognizeFilter implements Filter {

    private static final String LOCALE_KEY = "locale";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final String localeName = invocation.getAttachment(LOCALE_KEY);
        final Locale locale = lookupLocale(localeName);
        try {
            MapThreadLocalAdaptor.put(Keys.LOCALE_NAME, localeName);
            MapThreadLocalAdaptor.put(Keys.LOCALE, locale);
            return invoker.invoke(invocation);
        } finally {
            MapThreadLocalAdaptor.remove(Keys.LOCALE_NAME);
            MapThreadLocalAdaptor.remove(Keys.LOCALE);
        }
    }

    /**
     * 解析出Locale对象
     *
     * @param localeName 名字(如:en,zh_CN,zh_TW)
     * @return Locale 对象
     */
    private Locale lookupLocale(String localeName) {
        if (localeName == null) {
            return null;
        }
        final String[] items = localeName.split("_");
        if (items.length <= 0) {
            return null;
        }
        Locale locale = null;
        for (Locale availableLocale : Locale.getAvailableLocales()) {
            if (items.length > 0 && !availableLocale.getLanguage().equalsIgnoreCase(items[0].trim())) {
                continue;
            }
            if (items.length > 1 && availableLocale.getCountry().equalsIgnoreCase(items[1].trim())) {
                locale = availableLocale;
                break;
            } else if (availableLocale.getCountry() == null || availableLocale.getCountry().isEmpty()) {
                locale = availableLocale;
                break;
            }
        }
        return locale;
    }

}
