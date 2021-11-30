package com.alpha.coding.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.bo.enums.util.CodeSupplyEnum;
import com.alpha.coding.bo.enums.util.EnumUtils;
import com.alpha.coding.common.http.HttpClientUtils;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * BankCardUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class BankCardUtils {

    private static List<BankInfoItem> bankInfoItems;
    private static long refreshTimestamp = System.currentTimeMillis();
    private static final Map<String, BankInfoItem> bankMap = Maps.newHashMap();
    private static final Map<String, String> bankCodeMap = Maps.newHashMap();
    private static final Map<String, CardCheckResult> cardBinBankMap = Maps.newHashMap();
    private static final String aliValidateBankNoUrl = "https://ccdcapi.alipay.com/validateAndCacheCardInfo"
            + ".json?_input_charset=utf-8&cardNo=%s&cardBinCheck=true";

    @Getter
    @AllArgsConstructor
    public static enum CardTypeEnum implements CodeSupplyEnum<CardTypeEnum> {
        DC("DC", "借记卡"),
        CC("CC", "贷记卡"),
        SCC("SCC", "准贷记卡"),
        PC("PC", "预付费卡");

        private final String code;
        private final String desc;

        @Override
        public Supplier codeSupply() {
            return this::getCode;
        }

        public static CardTypeEnum valueOfCode(String code) {
            return CodeSupplyEnum.valueOf(code);
        }

        public static String getDescByCode(String code) {
            return CodeSupplyEnum.getDescByCode(code);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class BankInfoItem implements Serializable {
        private String bankCode;
        private String bankName;
        private List<String> bankAlias;
        private List<CardPattern> patterns;
    }

    @Data
    @Accessors(chain = true)
    public static class CardPattern implements Serializable {
        private String cardType;
        private String regexp;
    }

    @Data
    @Accessors(chain = true)
    public static class CardCheckResult implements Serializable {
        private Boolean validate; // 卡bin校验:null表示卡号不全未做校验
        private String bankCode; // 银行编号
        private String bankName; // 银行名
        private List<String> bankAlias; // 银行别名
        private CardTypeEnum cardType; // 卡类型
        private Boolean aliValidate; // 卡号阿里API校验结果
        private int cardLength; // 卡号长度
    }

    private static String loadResourceAsString(String path, Charset charset, boolean withLineSeparator)
            throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
        if (resources != null && resources.length > 0) {
            try (InputStream inputStream = resources[0].getInputStream()) {
                return IOUtils.readFromInputStream(inputStream, charset, withLineSeparator);
            }
        }
        return null;
    }

    public static List<BankInfoItem> loadBankInfoItem() throws IOException {
        if (bankInfoItems == null || System.currentTimeMillis() - refreshTimestamp > 300000) {
            synchronized(BankCardUtils.class) {
                if (bankInfoItems == null || System.currentTimeMillis() - refreshTimestamp > 300000) {
                    loadBankCodeMap();
                    final String bankInfo = loadResourceAsString("classpath*:/resource/bank-info.json",
                            StandardCharsets.UTF_8, false);
                    if (bankInfo != null) {
                        bankInfoItems = JSON.parseArray(bankInfo, BankInfoItem.class);
                        bankMap.clear();
                        for (BankInfoItem item : bankInfoItems) {
                            bankMap.put(item.getBankCode(), item);
                            if (CollectionUtils.isNotEmpty(item.getPatterns())) {
                                for (CardPattern pattern : item.getPatterns()) {
                                    final String regexp = pattern.getRegexp();
                                    if (StringUtils.isBlank(regexp)) {
                                        continue;
                                    }
                                    try {
                                        final CardTypeEnum cardType = EnumUtils
                                                .safeParse(CardTypeEnum.class, pattern.getCardType());
                                        final String[] tokens = pattern.getRegexp().split("[()]");
                                        int cardLenExcludeBin = Integer.parseInt(tokens[2].split("[{}]")[1]);
                                        for (String bin : tokens[1].split("\\|")) {
                                            CardCheckResult info = new CardCheckResult()
                                                    .setBankCode(item.getBankCode())
                                                    .setBankName(item.getBankName())
                                                    .setBankAlias(item.getBankAlias())
                                                    .setCardType(cardType)
                                                    .setCardLength(bin.length() + cardLenExcludeBin);
                                            cardBinBankMap.put(bin, info);
                                        }
                                    } catch (Exception e) {
                                        log.error("parseCardBinBankMap fail for {}", JSON.toJSONString(item), e);
                                    }
                                }
                            }
                        }
                    }
                    refreshTimestamp = System.currentTimeMillis();
                }
            }
        }
        return bankInfoItems;
    }

    private static void loadBankCodeMap() {
        try {
            final String text = loadResourceAsString("classpath*:/resource/bank-code.json",
                    StandardCharsets.UTF_8, false);
            final JSONObject jsonObject = JSON.parseObject(text);
            for (String key : jsonObject.keySet()) {
                bankCodeMap.put(key, jsonObject.getString(key));
            }
        } catch (Exception e) {
            log.error("loadBankCodeMap fail, msg={}", e.getMessage());
        }
    }

    public static CardCheckResult validate(String cardNo) {
        try {
            return validate(loadBankInfoItem(), cardNo);
        } catch (IOException e) {
            log.error("loadBankInfoItem error");
            return null;
        }
    }

    public static CardCheckResult validate(List<BankInfoItem> bankInfoItems, String cardNo) {
        if (bankInfoItems == null || StringUtils.isBlank(cardNo)
                || !Pattern.compile("\\d+").matcher(cardNo).matches()) {
            return null;
        }
        CardCheckResult result = new CardCheckResult();
        int checkCode = Integer.parseInt(cardNo.substring(cardNo.length() - 1));
        String cardNoCheckCode = cardNo.substring(0, cardNo.length() - 1);
        int sum = 0;
        for (int i = 0; i < cardNoCheckCode.length(); i++) {
            int temp = Integer.parseInt(
                    cardNoCheckCode.substring(cardNoCheckCode.length() - i - 1, cardNoCheckCode.length() - i));
            if (i % 2 == 0) {
                temp *= 2;
                sum += temp % 10 + temp / 10;
            } else {
                sum += temp;
            }
        }
        int myCheckCode = sum % 10 == 0 ? 0 : 10 - (sum % 10);
        result.setValidate(checkCode == myCheckCode);
        for (BankInfoItem item : bankInfoItems) {
            if (CollectionUtils.isEmpty(item.getPatterns())) {
                continue;
            }
            for (CardPattern pattern : item.getPatterns()) {
                if (StringUtils.isBlank(pattern.getRegexp())) {
                    continue;
                }
                if (Pattern.compile(pattern.getRegexp()).matcher(cardNo).matches()) {
                    result.setBankCode(item.getBankCode())
                            .setBankName(item.getBankName())
                            .setBankAlias(item.getBankAlias())
                            .setCardType(EnumUtils.safeParse(CardTypeEnum.class, pattern.getCardType()))
                            .setCardLength(cardNo.length());
                }
            }
        }
        return result;
    }

    /**
     * 通过#{aliValidateBankNoUrl}校验
     *
     * @param cardNo  卡号
     * @param timeout 超时时间，ms
     */
    public static CardCheckResult validateByAli(String cardNo, int timeout) throws IOException {
        final String res =
                HttpClientUtils.get(String.format(aliValidateBankNoUrl, cardNo), "UTF-8", timeout, timeout, 0);
        if (StringUtils.isBlank(res)) {
            return null;
        }
        JSONObject ret = JSON.parseObject(res);
        CardCheckResult result = new CardCheckResult();
        final Boolean validated = ret.getBoolean("validated");
        result.setAliValidate(validated != null && validated);
        result.setCardType(EnumUtils.safeParse(CardTypeEnum.class, ret.getString("cardType")));
        result.setBankCode(ret.getString("bank"));
        if (bankMap.isEmpty()) {
            loadBankInfoItem();
        }
        final BankInfoItem bankInfoItem = bankMap.get(result.getBankCode());
        if (bankInfoItem != null) {
            result.setBankName(bankInfoItem.getBankName());
            result.setBankAlias(bankInfoItem.getBankAlias());
        } else {
            result.setBankName(bankCodeMap.get(result.getBankCode()));
        }
        return result;
    }

    /**
     * 本地与远程校验
     *
     * @param cardNo  卡号
     * @param timeout 超时时间，ms
     */
    public static CardCheckResult fullValidate(String cardNo, int timeout) throws IOException {
        CardCheckResult result = validate(cardNo);
        if (result == null) {
            result = new CardCheckResult();
        }
        if (result.getCardType() == null) {
            result = validateByAli(cardNo, timeout);
        }
        return result;
    }

    /**
     * 根据卡bin识别出银行卡信息
     */
    public static CardCheckResult getCardInfoByCardBin(String cardBin) {
        if (cardBin == null || cardBin.length() < 2) {
            return null;
        }
        if (cardBinBankMap.isEmpty()) {
            try {
                loadBankInfoItem();
            } catch (IOException e) {
                // nothing
            }
        }
        for (int i = 2; i < (Math.min(cardBin.length(), 10)); i++) {
            final CardCheckResult info = cardBinBankMap.get(cardBin.substring(0, i));
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    /**
     * 根据卡号动态识别或校验
     * <p>CardCheckResult.validate为null时表示未做卡bin校验(可能卡号输入不完整)</p>
     *
     * @param cardNo  卡号
     * @param timeout 超时时间
     */
    public static CardCheckResult dynamicCheckCard(String cardNo, int timeout) throws IOException {
        if (StringUtils.isBlank(cardNo)) {
            return null;
        }
        final CardCheckResult cardInfoByCardBin = getCardInfoByCardBin(cardNo);
        if (cardNo.length() < 14
                || (cardInfoByCardBin != null && cardInfoByCardBin.getCardLength() > cardNo.length())) {
            return cardInfoByCardBin;
        }
        return fullValidate(cardNo, timeout);
    }

}
