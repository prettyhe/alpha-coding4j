package com.alpha.coding.common.utils.xls;

import com.alpha.coding.bo.annotation.XLSLabel;
import com.alpha.coding.bo.handler.XLSCellHandler;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * XLSLabelContext
 *
 * @version 1.0
 * Date: 2022/8/19
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class XLSLabelContext {

    private String memo;
    private int order;
    private Class<?> javaType;
    private Class<? extends XLSCellHandler>[] headCellHandler;
    private Class<? extends XLSCellHandler>[] cellHandler;
    private String outDateFormat;

    public XLSLabelContext(Label label) {
        this.memo = label.memo();
        this.order = label.order();
        this.javaType = label.javaType();
        this.headCellHandler = label.headCellHandler();
        this.cellHandler = label.cellHandler();
        this.outDateFormat = label.outDateFormat();
    }

    public XLSLabelContext(XLSLabel label) {
        this.memo = label.memo();
        this.order = label.order();
        this.javaType = label.javaType();
        this.headCellHandler = label.headCellHandler();
        this.cellHandler = label.cellHandler();
        this.outDateFormat = label.outDateFormat();
    }

}
