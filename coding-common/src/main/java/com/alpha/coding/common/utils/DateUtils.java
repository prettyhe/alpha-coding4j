package com.alpha.coding.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DateUtils default parse-format:{yyyy-MM-dd HH:mm:ss}
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class DateUtils {

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
    public static final String DEFAULT_DATE_MONTH_FORMAT = "yyyyMM";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_1 = "dd/MM/yyyy";
    public static final String DATE_FORMAT_2 = "yyyy/MM/dd";
    public static final String DATE_HOUR_FORMAT = "yyyyMMddHH";
    public static final String DATE_HOUR_FORMAT_1 = "yyyy-MM-dd HH";
    public static final String DATE_HOUR_MINUTE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String CRON_FORMAT = "ss mm HH dd MM ?";
    public static final String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";
    public static final String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static final long MILLIS_DAY = 24 * 3600 * 1000;
    public static final long MILLIS_HOUR = 3600 * 1000;
    public static final long MILLIS_MINUTE = 60 * 1000;
    public static final long MILLIS_SECOND = 1000;

    /**
     * 格式化时间
     *
     * @param timestamp 时间戳，精确到毫秒
     * @param format    格式
     */
    public static String format(long timestamp, String format) {
        return format(new Date(timestamp), format);
    }

    /**
     * 格式化时间，yyyy-MM-dd HH:mm:ss
     *
     * @param timestamp 时间戳，精确到毫秒
     */
    public static String format(long timestamp) {
        return format(timestamp, DEFAULT_FORMAT);
    }

    /**
     * 格式化时间
     *
     * @param date   时间
     * @param format 格式
     */
    public static String format(Date date, String format) {
        return date == null ? null : new SimpleDateFormat(format).format(date);
    }

    /**
     * 格式化时间，yyyy-MM-dd HH:mm:ss
     *
     * @param date 时间
     */
    public static String format(Date date) {
        return format(date, DEFAULT_FORMAT);
    }

    /**
     * 指定时区的格式化
     *
     * @param date     时间
     * @param format   格式
     * @param timeZone 时区
     */
    public static String format(Date date, String format, TimeZone timeZone) {
        return date == null ? null : buildDateFormat(format, timeZone).format(date);
    }

    private static SimpleDateFormat buildDateFormat(String format, TimeZone timeZone) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        return dateFormat;
    }

    /**
     * 格式化时间，yyyy-MM-dd HH:mm:ss
     *
     * @param date 时间
     */
    public static String formatTime(Date date) {
        return format(date, DEFAULT_FORMAT);
    }

    /**
     * 格式化日期，yyyy-MM-dd
     *
     * @param date 时间
     */
    public static String formatDate(Date date) {
        return format(date, DATE_FORMAT);
    }

    /**
     * 转换到日期格式，yyyy-MM-dd
     *
     * @param str 时间
     */
    public static String formatDate(String str) {
        return changeFormat(str, DATE_FORMAT);
    }

    /**
     * 转换格式
     *
     * @param dateStr      时间
     * @param srcFormat    原始格式
     * @param targetFormat 目标格式
     */
    public static String changeFormat(String dateStr, String srcFormat, String targetFormat) {
        return format(parse(dateStr, srcFormat), targetFormat);
    }

    /**
     * 转换格式
     *
     * @param dateStr      时间
     * @param targetFormat 目标格式
     */
    public static String changeFormat(String dateStr, String targetFormat) {
        return format(smartParse(dateStr), targetFormat);
    }

    /**
     * 解析时间，有异常时抛出
     *
     * @param dateStr 时间
     * @param format  格式
     */
    public static Date parseWithException(String dateStr, String format) throws ParseException {
        return dateStr == null ? null : new SimpleDateFormat(format).parse(dateStr);
    }

    /**
     * 对一指定时间进行精度调整(只能调大，比如消除:秒数/分钟数+秒数)
     *
     * @param date       时间
     * @param formatFrom 时间取样精度
     * @param formatTo   时间还原精度
     * @return 调整精度后的时间
     */
    public static Date rePreciseTime(Date date, String formatFrom, String formatTo) {
        return parse(format(date, formatFrom), formatTo);
    }

    /**
     * 使用于本工具里定义的几种格式的字符串转换成日期, 时间戳(或ms)自行转换即可
     */
    public static Date smartParse(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        final String trimDate = dateStr.trim();
        if (StringUtils.isNumeric(trimDate)) {
            return parseByChoose(trimDate, Arrays.asList(
                    PURE_DATETIME_MS_PATTERN,
                    PURE_DATETIME_PATTERN,
                    DATE_HOUR_FORMAT,
                    DEFAULT_DATE_FORMAT
            ));
        }
        return parseByChoose(trimDate, Arrays.asList(
                DATE_MILLIS_FORMAT,
                DEFAULT_FORMAT,
                DATE_HOUR_MINUTE_FORMAT,
                DATE_HOUR_FORMAT_1,
                DATE_FORMAT,
                DATE_FORMAT_1,
                DATE_FORMAT_2
        ));
    }

    /**
     * 使用指定的几种格式依次解析
     *
     * @param dateStr 时间
     * @param formats 格式
     */
    public static Date parseByChoose(String dateStr, List<String> formats) {
        for (String format : formats) {
            try {
                if (dateStr.length() != format.length()) {
                    continue;
                }
                return parseWithException(dateStr, format);
            } catch (Exception e) {
                // continue;
            }
        }
        return null;
    }

    /**
     * 解析时间
     *
     * @param dateStr 时间
     * @param format  格式
     */
    public static Date parse(String dateStr, String format) {
        return parse(dateStr, format, null);
    }

    /**
     * 解析时间
     *
     * @param dateStr 时间
     * @param format  格式
     * @param locale  场所
     */
    public static Date parse(String dateStr, String format, Locale locale) {
        if (dateStr == null) {
            return null;
        }
        DateFormat dateFormat = locale == null ? new SimpleDateFormat(format) : new SimpleDateFormat(format, locale);
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 解析时间，使用 yyyy-MM-dd HH:mm:ss
     *
     * @param dateStr 时间
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, DEFAULT_FORMAT);
    }

    /**
     * 获取日期(零点)
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public static Date getDate(int year, int month, int day) {
        return getDate(year, month, day, 0, 0, 0);
    }

    /**
     * 获取时间
     *
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   时(24小时制)
     * @param minute 分
     * @param second 秒
     */
    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取日期(零点)
     */
    public static Date getDayDate(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 新日期(零点)
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public static Date newDate(int year, int month, int day) {
        return getDate(year, month, day);
    }

    /**
     * 获取N年后的那天(零点)
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getNextNYear(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.YEAR, n);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取N月后的那天(零点)
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getNextNMonth(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.MONTH, n);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取N天后的那天(零点)
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getNextNDay(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.DAY_OF_MONTH, n);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取N年后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNYear(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.YEAR, n);
        return calendar.getTime();
    }

    /**
     * 获取N月后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNMonth(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.MONTH, n);
        return calendar.getTime();
    }

    /**
     * 获取N天后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNDay(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.DAY_OF_MONTH, n);
        return calendar.getTime();
    }

    /**
     * 获取N小时后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNHour(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.HOUR_OF_DAY, n);
        return calendar.getTime();
    }

    /**
     * 获取N分钟后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNMinute(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.MINUTE, n);
        return calendar.getTime();
    }

    /**
     * 获取N秒后的那个时刻
     *
     * @param dt 时间
     * @param n  间隔
     */
    public static Date getTimeNextNSecond(Date dt, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        calendar.add(Calendar.SECOND, n);
        return calendar.getTime();
    }

    /**
     * 获取下一天(零点)
     *
     * @param dt 时间
     */
    public static Date getNextDay(Date dt) {
        return getNextNDay(dt, 1);
    }

    /**
     * 获取时间间隔（end - start）
     *
     * @param start 起始时间
     * @param end   终止时间
     */
    public static String getTimeInterval(long start, long end) {
        long secondInterval = (end - start) / 1000;
        if (secondInterval < 60) {
            return "1分钟以内";
        }
        if (secondInterval < 3600) {
            long min = secondInterval / 60;
            return min + "分钟前";
        }
        if (secondInterval < 86400) {
            long hour = secondInterval / 3600;
            return hour + "小时前";
        }
        Date date = new Date();
        date.setTime(start);
        return format(date, "yyyy年MM月dd日");
    }

    /**
     * 获取时间间隔（end - start）
     *
     * @param start 起始时间
     * @param end   终止时间
     */
    public static String getTimeInterval(Date start, Date end) {
        return getTimeInterval(start.getTime(), end.getTime());
    }

    /**
     * 固定时间: 1970-01-01 00:00:00
     */
    public static Date getOriginTime() {
        return getDate(1970, 1, 1);
    }

    /**
     * 固定时间: 1971-01-01 00:59:59
     */
    @Deprecated
    public static Date getDefaultStartDate() {
        return getDate(1971, 1, 1, 0, 59, 59);
    }

    /**
     * 固定时间: 1971-01-01 00:59:59
     */
    @Deprecated
    public static String getDefaultStartDateStr() {
        return "1971-01-01 00:59:59";
    }

    /**
     * 固定时间: 2037-01-01 00:59:59
     */
    @Deprecated
    public static Date getDefaultEndDate() {
        return getDate(2037, 1, 1, 0, 59, 59);
    }

    /**
     * 固定时间: 2037-01-01 00:59:59
     */
    @Deprecated
    public static String getDefaultEndDateStr() {
        return "2037-01-01 00:59:59";
    }

    /**
     * 计算字符串dateStr所表示的时间相对于起始时间(1970-01-01 00:00:00)所走过的秒数
     *
     * @param dateStr 时间
     */
    public static long getDeltaSeconds(String dateStr) {
        Date date = smartParse(dateStr);
        return (date.getTime() - getOriginTime().getTime()) / 1000;
    }

    /**
     * 获取指定时间的前几天时间
     *
     * @param specificDate 时间
     * @param days         间隔
     * @see DateUtils#getTimeNextNDay(Date, int)
     */
    public static Date getTimeBeforeByDayInterval(Date specificDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(specificDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1 * days);
        return calendar.getTime();
    }

    /**
     * 获取指定时间的前几小时时间
     *
     * @param specificDate 时间
     * @param hours        间隔
     * @see DateUtils#getTimeNextNHour(Date, int)
     */
    public static Date getTimeBeforeByHourInterval(Date specificDate, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(specificDate);
        calendar.add(Calendar.HOUR_OF_DAY, -1 * hours);
        return calendar.getTime();
    }

    /**
     * 获取指定时间那天的开始时间：00:00:00
     */
    public static Date getDayStart(Date date) {
        return date == null ? null : getDayDate(date);
    }

    /**
     * 获取指定时间那天的结束时间：23:59:59
     */
    public static Date getDayEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 取当前时间前几天的0点
     *
     * @param days 间隔
     */
    public static Date getDayBefore(int days) {
        return getNextNDay(new Date(), -days);
    }

    /**
     * 获取小时起始(分、秒、毫秒 抹零)
     *
     * @param date 时间
     */
    public static Date getHourStart(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 将时间的秒数抹零，e.g. 2015-11-11 12:34:45 ==> 2015-11-11 12:34:00
     *
     * @param date 时间
     */
    public static Date getDateOverlookSecond(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 按天拆分，每天的0点作为分界线
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 拆分的时间序列
     */
    public static List<Date> splitByDay(Date start, Date end) {
        List<Date> dates = new ArrayList<>();
        if (start.after(end)) {
            return dates;
        }
        dates.add(start);
        Date tmp = getDayStart(start);
        while (true) {
            tmp = getTimeBeforeByDayInterval(tmp, -1);
            if (tmp.after(end)) {
                break;
            }
            dates.add(tmp);
        }
        if (dates.get(dates.size() - 1).before(end)) {
            dates.add(end);
        }
        return dates;
    }

    /**
     * 按小时拆分，每小时的0分作为分界线
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 拆分的时间序列
     */
    public static List<Date> splitByHour(Date start, Date end) {
        List<Date> dates = new ArrayList<>();
        if (start.after(end)) {
            return dates;
        }
        dates.add(start);
        Date tmp = getHourStart(start);
        while (true) {
            tmp = getTimeBeforeByHourInterval(tmp, -1);
            if (tmp.after(end)) {
                break;
            }
            dates.add(tmp);
        }
        if (dates.get(dates.size() - 1).before(end)) {
            dates.add(end);
        }
        return dates;
    }

    /**
     * 按分钟拆分，每小时的0分作为分界线
     *
     * @param start          开始时间
     * @param end            结束时间
     * @param minuteInterval 分钟间隔,须为正整数
     * @return 拆分的时间序列
     */
    public static List<Date> splitByMinute(Date start, Date end, int minuteInterval) {
        List<Date> dates = new ArrayList<>();
        if (start.after(end)) {
            return dates;
        }
        if (minuteInterval <= 0) {
            return dates;
        }
        long deltaMs = minuteInterval * 60 * 1000;
        dates.add(start);
        long endTime = end.getTime();
        long tmp = start.getTime();
        while (true) {
            tmp += deltaMs;
            if (tmp > endTime) {
                break;
            }
            dates.add(new Date(tmp));
        }
        if (dates.get(dates.size() - 1).before(end)) {
            dates.add(end);
        }
        return dates;
    }

    /**
     * 获取本月第一天(零点)
     */
    public static Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getDayDate(calendar.getTime());
    }

    /**
     * 获取指定时间当月的第一天(零点)
     *
     * @param date 指定时间
     */
    public static Date getFirstDayOfTheMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return parse(format(calendar.getTime(), DATE_FORMAT), DATE_FORMAT);
    }

    /**
     * 获取本月最后一天(零点)
     */
    public static Date getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return getDayDate(calendar.getTime());
    }

    /**
     * 获取指定时间当月的最后一天
     *
     * @param date 指定时间
     */
    public static Date getLastDayOfTheMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return getDayDate(calendar.getTime());
    }

    /**
     * 获取指定时间在该月的天数
     *
     * @param date 指定时间
     */
    public static int getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取指定时间在改天的小时数
     *
     * @param date 指定时间
     */
    public static int getHourOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取指定时间在上个月的当天时间
     *
     * @param date 指定时间
     */
    public static Date getTimeOfLastMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    /**
     * 格式化时间差值
     *
     * @param st 起始时间
     * @param et 终止时间
     */
    public static String formatMinusDate(Date st, Date et) {
        return formatMinusDate(st.getTime(), et.getTime());
    }

    /**
     * 格式化时间差值
     *
     * @param st 起始时间
     * @param et 终止时间
     */
    public static String formatMinusDate(long st, long et) {
        long delta = et - st;
        StringBuilder sb = new StringBuilder();
        final long day = delta / MILLIS_DAY;
        if (day > 0) {
            sb.append(day).append("天");
        }
        delta %= MILLIS_DAY;
        final long hour = delta / MILLIS_HOUR;
        if (day > 0 || hour > 0) {
            sb.append(hour).append("时");
        }
        delta %= MILLIS_HOUR;
        final long minute = delta / MILLIS_MINUTE;
        if (day > 0 || hour > 0 || minute > 0) {
            sb.append(minute).append("分");
        }
        delta %= MILLIS_MINUTE;
        final long second = delta / MILLIS_SECOND;
        if (day > 0 || hour > 0 || minute > 0 || second > 0) {
            sb.append(second).append("秒");
        }
        delta %= MILLIS_SECOND;
        sb.append(delta).append("毫秒");
        return sb.toString();
    }

    /**
     * 昨天(零点)
     */
    public static Date yesterday() {
        return getNextNDay(new Date(), -1);
    }

    /**
     * 昨天此刻
     */
    public static Date yesterdayTime() {
        return getTimeNextNDay(new Date(), -1);
    }

    /**
     * 计算用户岁数，算足的
     *
     * @param birth 生日
     * @return 年龄
     */
    public static int age(Date birth) {
        return age(new Date(), birth);
    }

    /**
     * 计算用户岁数，算足的
     *
     * @param birth 生日
     * @return 年龄
     */
    public static int age(Date specifyDate, Date birth) {
        int result = 0;
        int userYear = getYear(birth);
        int userMonth = getMonth(birth);
        int userDay = getDay(birth);

        int nowYear = getYear(specifyDate);
        int nowMonth = getMonth(specifyDate);
        int nowDay = getDay(specifyDate);
        result = nowYear - userYear - 1;
        // 如果今年生日已过，则+1
        if (userMonth < nowMonth || (userMonth == nowMonth && userDay < nowDay)) {
            result += 1;
        }
        return result;
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
    }

    /**
     * 返回日期的年，即yyyy-MM-dd中的yyyy
     *
     * @param date Date
     * @return 年份
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 返回日期的月份，1-12，即yyyy-MM-dd中的MM
     *
     * @param date 时间
     * @return 月份
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 返回日期的天数，0-31，即yyyy-MM-dd中的dd
     *
     * @param date 时间
     * @return 天数
     */
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    /**
     * 比较日期，精确到天
     */
    public static int compareDay(Date d1, Date d2) {
        return rePreciseTime(d1, DEFAULT_FORMAT, DATE_FORMAT)
                .compareTo(rePreciseTime(d2, DEFAULT_FORMAT, DATE_FORMAT));
    }

    /**
     * getCron 将指定时间转成cron表达式
     *
     * @param date 日期
     * @return java.lang.String
     */
    public static String convertToCron(Date date) {
        return format(date, CRON_FORMAT);
    }

    /**
     * getCron 将指定时间转成cron表达式
     *
     * @param date           日期
     * @param targetTimezone 目标时区
     * @return java.lang.String
     */
    public static String convertToCron(Date date, TimeZone targetTimezone) {
        return format(date, CRON_FORMAT, targetTimezone);
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA);
    }

    /**
     * 获取当天剩余秒数
     */
    public static long getDayRemainSeconds() {
        Date currentDate = new Date();
        Date endDate = getDayEnd(currentDate);
        return (endDate.getTime() - currentDate.getTime()) / 1000;
    }

    /**
     * 时间丢弃毫秒
     */
    public static Date discardMillis(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime LocalDateTime
     * @param zoneId        ZoneId
     */
    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime == null ? null
                : Date.from(localDateTime.atZone(zoneId == null ? ZoneId.systemDefault() : zoneId).toInstant());
    }

    /**
     * LocalDateTime 转 Date，使用系统默认ZoneId
     *
     * @param localDateTime LocalDateTime
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate 转 LocalDateTime
     *
     * @param localDate LocalDate
     */
    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        return localDate == null ? null : localDate.atStartOfDay();
    }

    /**
     * 获取指定日期当前天数在N月之后那天的前一天
     * <li>(2022-12-01, 1) => 2022-12-31</li>
     * <li>(2022-12-02, 1) => 2023-01-01</li>
     * <li>(2023-03-31, 1) => 2023-04-30</li>
     * <li>(2023-03-30, 1) => 2023-04-29</li>
     * <li>(2023-01-30, 1) => 2023-02-28</li>
     * <li>(2023-01-30, 13) => 2024-02-29</li>
     * <li>(2023-01-28, 1) => 2023-02-27</li>
     * <li>(2023-01-29, 1) => 2023-02-28</li>
     * <li>(2023-01-29, 13) => 2024-02-28</li>
     */
    public static Date getDayBeforeTheDayOfNextNMonth(Date start, int n) {
        final int dayOfMonth = getDay(start);
        if (dayOfMonth <= 28) {
            return getNextNDay(getNextNMonth(start, n), -1);
        } else {
            Date dateTime = getNextNMonth(start, n);
            int day = getDay(dateTime);
            if (day == dayOfMonth) {
                return getNextNDay(dateTime, -1);
            } else {
                while (getDay(dateTime) < 4) {
                    dateTime = getNextNDay(dateTime, -1);
                }
                return dateTime;
            }
        }
    }

}
