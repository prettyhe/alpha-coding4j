package com.alpha.coding.bo.common.compare;

/**
 * RightOpenRangeCompare
 *
 * @version 1.0
 * Date: 2020/4/21
 */
public class RightOpenRangeCompare extends RangeCompare {

    @Override
    public int type() {
        return ConditionType.RIGHT_OPEN_RANGE.getType();
    }

    /**
     * val compare to right
     */
    @Override
    protected boolean rightCompare(int compareToRight) {
        return compareToRight < 0;
    }

}
