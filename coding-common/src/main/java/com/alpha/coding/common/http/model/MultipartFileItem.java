package com.alpha.coding.common.http.model;

import java.io.InputStream;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MultipartFileItem
 *
 * @version 1.0
 * @date 2024年06月28日
 */
@Data
@Accessors(chain = true)
public class MultipartFileItem {

    /**
     * 文件名
     */
    private String filename;
    /**
     * 文件流
     */
    private InputStream inputStream;
    /**
     * 文件ContentType
     */
    private String contentType;

}
