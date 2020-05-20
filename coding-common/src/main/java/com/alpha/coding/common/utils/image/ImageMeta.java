package com.alpha.coding.common.utils.image;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ImageMeta
 *
 * @version 1.0
 * Date: 2020-01-14
 */
@Data
@Accessors(chain = true)
public class ImageMeta implements Serializable {

    private String type; // jpeg,gif,png
    private int width;
    private int height;

}
