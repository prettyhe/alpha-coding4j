package com.alpha.coding.common.utils;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.base.Triple;
import com.alpha.coding.common.model.CityLevelInfo;

/**
 * IdcardAddressUtil
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class IdcardAddressUtil {

    private static CityLevelInfo cityLevelInfo;

    static {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/resource/cityMeta.json");
            if (resources != null && resources.length > 0) {
                try (InputStream inputStream = resources[0].getInputStream()) {
                    final String src = IOUtils.readFromInputStream(inputStream, Charset.forName("UTF-8"), false);
                    cityLevelInfo = JSON.parseObject(src, CityLevelInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Triple<String, String, String> parseAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        Triple<String, String, String> triple = new Triple<>();
        triple.setT(address);
        try {
            String addressStr = address;
            // 以省开头的
            for (String province : cityLevelInfo.getProvinces()) {
                if (addressStr.indexOf(province) == 0) {
                    triple.setF(province);
                    addressStr = addressStr.substring(province.length());
                    for (String city : cityLevelInfo.getProvinceCityMap().get(province)) {
                        if (addressStr.indexOf(city) == 0) {
                            triple.setS(city);
                            addressStr = addressStr.substring(city.length());
                            break;
                        }
                    }
                    triple.setT(addressStr);
                    return triple;
                }
            }
            // 以直辖市开头的
            for (String city : cityLevelInfo.getMunicipalities()) {
                if (addressStr.indexOf(city) == 0) {
                    triple.setF(city);
                    triple.setS(city);
                    addressStr = addressStr.substring(city.length());
                    triple.setT(addressStr);
                    return triple;
                }
            }
            // 以省会城市开头的
            for (String city : cityLevelInfo.getProvincialCapitals()) {
                if (addressStr.indexOf(city) == 0) {
                    triple.setF(cityLevelInfo.getCityProvinceMap().get(city));
                    triple.setS(city);
                    addressStr = addressStr.substring(city.length());
                    triple.setT(addressStr);
                    return triple;
                }
            }
            triple.setT(addressStr);
            return triple;
        } catch (Exception e) {
            return triple;
        }
    }

}
