package com.alpha.coding.bo.common.compare;

/**
 * LeftOpenRangeCompare
 *
 * @version 1.0
 * Date: 2020/4/21
 */
public class LeftOpenRangeCompare extends RangeCompare {

    @Override
    public int type() {
        return ConditionType.LEFT_OPEN_RANGE.getType();
    }

    /**
     * val compare to left
     */
    @Override
    protected boolean leftCompare(int compareToLeft) {
        return compareToLeft > 0;
    }

}
