package com.alpha.coding.common.bean.define;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DefineBeanConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class DefineBeanConfig {

    private DefineType type;
    private String beanName;
    private String srcLocation;

}
