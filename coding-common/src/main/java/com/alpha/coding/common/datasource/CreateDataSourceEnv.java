package com.alpha.coding.common.datasource;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CreateDataSourceEnv
 *
 * @version 1.0
 * Date: 2020/6/5
 */
@Data
@Accessors(chain = true)
public class CreateDataSourceEnv {

    private String prefix;
    private String type;

}
