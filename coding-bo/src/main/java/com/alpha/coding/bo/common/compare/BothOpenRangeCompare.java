package com.alpha.coding.bo.common.compare;

/**
 * BothOpenRangeCompare
 *
 * @version 1.0
 * Date: 2020/4/21
 */
public class BothOpenRangeCompare extends RangeCompare {

    @Override
    public int type() {
        return ConditionType.BOTH_OPEN_RANGE.getType();
    }

    /**
     * val compare to left
     */
    @Override
    protected boolean leftCompare(int compareToLeft) {
        return compareToLeft > 0;
    }

    /**
     * val compare to right
     */
    @Override
    protected boolean rightCompare(int compareToRight) {
        return compareToRight < 0;
    }

}
