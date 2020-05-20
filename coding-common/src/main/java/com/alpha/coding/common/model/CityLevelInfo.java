package com.alpha.coding.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CityLevelInfo
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class CityLevelInfo implements Serializable {

    private List<String> provinces = new ArrayList<>(); // 省
    private List<String> municipalities = new ArrayList<>(); // 直辖市
    private List<String> provincialCapitals = new ArrayList<>(); // 省会城市
    private List<String> generalCities = new ArrayList<>(); // 一般城市
    private Map<String, List<String>> provinceCityMap = new HashMap<>(); // 省=>城市映射关系
    private Map<String, String> cityProvinceMap = new HashMap<>(); // 城市=>省映射关系

}
