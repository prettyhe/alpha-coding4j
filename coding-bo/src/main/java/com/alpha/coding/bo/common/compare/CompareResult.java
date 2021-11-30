package com.alpha.coding.bo.common.compare;

import java.util.function.Supplier;

import com.alpha.coding.bo.enums.util.CodeSupplyEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CompareResult
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum CompareResult implements CodeSupplyEnum<CompareResult> {

    PASS(1, "通过"),
    NOT_FAIL(2, "非失败"),
    UNKNOWN(3, "未知"),
    FAIL(4, "失败");

    private final int code;
    private final String desc;

    @Override
    public Supplier codeSupply() {
        return this::getCode;
    }

    public static CompareResult and(CompareResult r1, CompareResult r2) {
        if (r1 == null) {
            return r2;
        }
        if (r2 == null) {
            return r1;
        }
        CompareResult ret = null;
        switch (r1) {
            case PASS:
                ret = r2;
                break;
            case UNKNOWN:
                switch (r2) {
                    case PASS:
                    case UNKNOWN:
                        ret = r1;
                        break;
                    case NOT_FAIL:
                    case FAIL:
                        ret = r2;
                        break;
                    default:
                        throw new RuntimeException("unknown result");
                }
                break;
            case FAIL:
                ret = FAIL;
                break;
            case NOT_FAIL:
                switch (r2) {
                    case PASS:
                    case NOT_FAIL:
                        ret = r1;
                        break;
                    case UNKNOWN:
                    case FAIL:
                        ret = r2;
                        break;
                    default:
                        throw new RuntimeException("unknown result");
                }
                break;
            default:
                throw new RuntimeException("unknown result");
        }
        return ret;
    }

    public static CompareResult or(CompareResult r1, CompareResult r2) {
        if (r1 == null) {
            return r2;
        }
        if (r2 == null) {
            return r1;
        }
        CompareResult ret = null;
        switch (r1) {
            case PASS:
                ret = PASS;
                break;
            case UNKNOWN:
                switch (r2) {
                    case PASS:
                    case NOT_FAIL:
                    case UNKNOWN:
                        ret = r2;
                        break;
                    case FAIL:
                        ret = r1;
                        break;
                    default:
                        throw new RuntimeException("unknown result");
                }
                break;
            case FAIL:
                ret = r2;
                break;
            case NOT_FAIL:
                switch (r2) {
                    case PASS:
                        ret = r2;
                        break;
                    case NOT_FAIL:
                    case UNKNOWN:
                    case FAIL:
                        ret = r1;
                        break;
                }
                break;
            default:
                throw new RuntimeException("unknown result");
        }
        return ret;
    }

    public CompareResult reverse() {
        switch (this) {
            case PASS:
            case NOT_FAIL:
                return FAIL;
            case UNKNOWN:
                return UNKNOWN;
            case FAIL:
                return PASS;
            default:
                throw new RuntimeException("unknown result");
        }
    }

    public static CompareResult valueOf(int code) {
        return CodeSupplyEnum.valueOf(code);
    }

    public static String getDescByCode(int code) {
        return CodeSupplyEnum.getDescByCode(code);
    }

}
