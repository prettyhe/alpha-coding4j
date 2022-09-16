package com.alpha.coding.common.mybatis.common;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
    private Set<Long> ids;
    private Long timestamp;
    private Object before;
    private Object after;
    private volatile Map<String, Object> bizParams; // 业务参数
    private Map<String, Object> extParams; // 扩展参数

    public TableUpdateDto appendBizParam(String key, Object value) {
        if (this.bizParams == null) {
            synchronized(this) {
                if (this.bizParams == null) {
                    this.bizParams = new LinkedHashMap<>();
                }
            }
        }
        this.bizParams.put(key, value);
        return this;
    }

    public TableUpdateDto appendBizParam(Map<String, Object> map) {
        if (map != null) {
            map.forEach(this::appendBizParam);
        }
        return this;
    }

    public Object fetchBizParam(String key) {
        return this.bizParams == null ? null : this.bizParams.get(key);
    }

    public TableUpdateDto appendExtParam(String key, Object value) {
        if (this.extParams == null) {
            synchronized(this) {
                if (this.extParams == null) {
                    this.extParams = new LinkedHashMap<>();
                }
            }
        }
        this.extParams.put(key, value);
        return this;
    }

    public Object fetchExtParam(String key) {
        return this.extParams == null ? null : this.extParams.get(key);
    }

}
