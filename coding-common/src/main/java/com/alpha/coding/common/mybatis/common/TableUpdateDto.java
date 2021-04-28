package com.alpha.coding.common.mybatis.common;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TableUpdateDto
 *
 * @version 1.0
 * Date: 2021/4/13
 */
@Data
@Accessors(chain = true)
public class TableUpdateDto implements Serializable {

    private String tableName;
    private int type; // 0-insert, 1-update
    private Long id;
    private String sqlId;

}
