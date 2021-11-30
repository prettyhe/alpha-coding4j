package com.alpha.coding.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

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
    public static final String DATE_HOUR_FORMAT = "yyyyMMddHH";
    public static final String DATE_HOUR_FORMAT_1 = "yyyy-MM-dd HH";
    public static final String DATE_HOUR_MINUTE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String CRON_FORMAT = "ss mm HH dd MM ?";

    public static final long MILLIS_DAY = 24 * 3600 * 1000;
    public static final long MILLIS_HOUR = 3600 * 1000;
    public static final long MILLIS_MINUTE = 60 * 1000;
    public static final long MILLIS_SECOND = 1000;

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static String format(long timestamp, String format) {
        return format(new Date(timestamp), format);
    }

    public static String format(long timestamp) {
        return format(timestamp, DEFAULT_FORMAT);
    }

    public static String format(Date date, String format) {
        return date == null ? null : new SimpleDateFormat(format).format(date);
    }

    private static SimpleDateFormat buildDateFormat(String format, TimeZone timeZone) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        return dateFormat;
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

    public static String format(Date date) {
        return format(date, DEFAULT_FORMAT);
    }

    public static String formatTime(Date date) {
        return format(date, DEFAULT_FORMAT);
    }

    @Deprecated
    public static String formatDate(String str) {
        return changeFormat(str, DEFAULT_DATE_FORMAT, DATE_FORMAT);
    }

    public static String changeFormat(String dateStr, String srcFormat, String targetFormat) {
        return format(parse(dateStr, srcFormat), targetFormat);
    }

    public static String changeFormat(String dateStr, String targetFormat) {
        return format(smartParse(dateStr), targetFormat);
    }

    public static Date parseWithException(String dateStr, String format) throws ParseException {
        return dateStr == null ? null : new SimpleDateFormat(format).parse(dateStr);
    }

    public static Date parseWithException(String dateStr, String format, TimeZone timeZone) throws ParseException {
        return dateStr == null ? null : buildDateFormat(format, timeZone).parse(dateStr);
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
        return smartParse(dateStr, (TimeZone) null);
    }

    /**
     * 使用于本工具里定义的几种格式的字符串转换成日期, 时间戳(或ms)自行转换即可
     */
    public static Date smartParse(String dateStr, TimeZone timeZone) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        if (StringUtils.isNumeric(dateStr.trim())) {
            return parseByChoose(dateStr.trim(), Arrays.asList(
                    DATE_HOUR_FORMAT,
                    DEFAULT_DATE_FORMAT
            ), timeZone);
        }
        return parseByChoose(dateStr, Arrays.asList(
                DATE_MILLIS_FORMAT,
                DEFAULT_FORMAT,
                DATE_HOUR_MINUTE_FORMAT,
                DATE_HOUR_FORMAT_1,
                DATE_FORMAT,
                DATE_FORMAT_1
        ), timeZone);
    }

    public static Date parseByChoose(String dateStr, List<String> formats) {
        return parseByChoose(dateStr, formats, (TimeZone) null);
    }

    public static Date parseByChoose(String dateStr, List<String> formats, TimeZone timeZone) {
        for (String format : formats) {
            try {
                return parseWithException(dateStr, format, timeZone);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    public static Date parse(String dateStr, String format) {
        return parse(dateStr, format, null);
    }

    public static Date parse(String dateStr, String format, Locale locale) {
        return parse(dateStr, format, locale, null);
    }

    public static Date parse(String dateStr, String format, Locale locale, TimeZone timeZone) {
        if (dateStr == null) {
            return null;
        }
        DateFormat dateFormat = locale == null ? new SimpleDateFormat(format) : new SimpleDateFormat(format, locale);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parse(String dateStr) {
        return parse(dateStr, DEFAULT_FORMAT);
    }

    /**
     * 带偏移的格式化(将源端时间格式化成目标时区时间字符换)
     *
     * @param date                 源端时间
     * @param targetFormat         目标格式
     * @param timezoneOffsetMillis 目标时区偏移量(毫秒)
     * @return 目标时区按照格式展示的时间
     */
    public static String formatWithOffset(Date date, String targetFormat, Long timezoneOffsetMillis) {
        if (timezoneOffsetMillis == null) {
            return format(date, targetFormat, null);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime() + timezoneOffsetMillis);
        return format(calendar.getTime(), targetFormat, GMT);
    }

    /**
     * 带偏移的解析(将源端时间字符串解析成目标时区时间)
     *
     * @param dateStr              源端时间字符串
     * @param timezoneOffsetMillis 源端时区偏移量(毫秒)
     * @return 目标时区时间
     */
    public static Date parseWithOffset(String dateStr, Long timezoneOffsetMillis) {
        if (timezoneOffsetMillis == null) {
            return DateUtils.smartParse(dateStr);
        }
        final Date date = smartParse(dateStr, GMT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime() - timezoneOffsetMillis);
        return calendar.getTime();
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month - 1, day);
        return cal.getTime();
    }

    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        return cal.getTime();
    }

    public static Date getDayDate(Date d) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        return newDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static Date newDate(int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month - 1, day);
        return cal.getTime();
    }

    public static Date getNextDay(Date dt) {
        if (dt == null) {
            return null;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH) + 1).getTime();
    }

    public static Date getNextNYear(Date dt, int n) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        return new GregorianCalendar(cal.get(Calendar.YEAR) + n, cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).getTime();
    }

    public static Date getNextNDay(Date dt, int n) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH) + n).getTime();
    }

    public static Date getTimeNextNMinute(Date dt, int n) {
        long time = dt.getTime() + n * 60 * 1000;
        Date result = new Date(time);
        return result;
    }

    public static Date getTimeNextNHour(Date dt, int n) {
        long time = dt.getTime() + n * 60 * 60 * 1000;
        Date result = new Date(time);
        return result;
    }

    public static String getTimeInterval(long timestamp1, long timestamp2) {
        long secondInterval = (timestamp2 - timestamp1) / 1000;
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
        date.setTime(timestamp1);
        return format(date, "yyyy年MM月dd日");
    }

    public static String getTimeInterval(Date time1, Date time2) {
        return getTimeInterval(time1.getTime(), time2.getTime());
    }

    public static Date getOriginTime() {
        return parse("1970-01-01 00:00:00");
    }

    public static Date getDefaultStartDate() {
        return parse("1971-01-01 00:59:59", "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDefaultStartDateStr() {
        return "1971-01-01 00:59:59";
    }

    public static Date getDefaultEndDate() {
        return parse("2037-01-01 00:59:59", "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDefaultEndDateStr() {
        return "2037-01-01 00:59:59";
    }

    /**
     * 计算字符串dateStr所表示的时间相对于起始时间(1970-01-01 00:00:00)所走过的秒数
     */
    public static long getDeltaSeconds(String dateStr) {
        Date date = parse(dateStr);
        return (long) ((date.getTime() - getOriginTime().getTime()) / 1000);
    }

    /**
     * 获取指定时间的前几天时间
     */
    public static Date getTimeBeforeByDayInterval(Date specificDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(specificDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1 * days);
        Date dBefore = calendar.getTime();
        return dBefore;
    }

    /**
     * 获取指定时间的前几小时时间
     */
    public static Date getTimeBeforeByHourInterval(Date specificDate, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(specificDate);
        calendar.add(Calendar.HOUR_OF_DAY, -1 * hours);
        Date dBefore = calendar.getTime();
        return dBefore;
    }

    /**
     * 获取指定时间那天的开始时间：00:00:00
     */
    public static Date getDayStart(Date date) {
        return date == null ? null : parse(format(date, "yyyy-MM-dd") + " 00:00:00");
    }

    /**
     * 获取指定时间那天的结束时间：23:59:59
     */
    public static Date getDayEnd(Date date) {
        return date == null ? null : parse(format(date, "yyyy-MM-dd") + " 23:59:59");
    }

    /**
     * 取当前时间前几天的0点
     */
    public static Date getDayBefore(int days) {
        return getDayStart(getTimeBeforeByDayInterval(new Date(), days));
    }

    public static Date getHourStart(Date date) {
        return parse(format(date, "yyyy-MM-dd HH") + ":00:00");
    }

    /**
     * 将时间的秒数抹零，e.g. 2015-11-11 12:34:45 ==> 2015-11-11 12:34:00
     */
    public static Date getDateOverlookSecond(Date date) {
        long minutes = date.getTime() / 1000 / 60;
        return new Date(minutes * 60 * 1000);
    }

    /**
     * 按天拆分，每天的0点作为分界线
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 拆分的时间序列
     */
    public static List<Date> splitByDay(Date start, Date end) {
        List<Date> dates = Lists.newArrayList();
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
        List<Date> dates = Lists.newArrayList();
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
        List<Date> dates = Lists.newArrayList();
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
     * 获取本月第一天
     */
    public static Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return parse(format(calendar.getTime(), DATE_FORMAT), DATE_FORMAT);
    }

    /**
     * 获取指定时间当月的第一天
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
     * 获取本月最后一天
     */
    public static Date getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return parse(format(calendar.getTime(), DATE_FORMAT), DATE_FORMAT);
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
        return parse(format(calendar.getTime(), DATE_FORMAT), DATE_FORMAT);
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

    public static String formatMinusDate(Date st, Date et) {
        return formatMinusDate(st.getTime(), et.getTime());
    }

    public static String formatMinusDate(Long st, Long et) {
        long delta = et - st;
        StringBuilder sb = new StringBuilder();
        final long day = delta / MILLIS_DAY;
        if (day > 0) {
            sb.append(day + "天");
        }
        delta %= MILLIS_DAY;
        final long hour = delta / MILLIS_HOUR;
        if (day > 0 || hour > 0) {
            sb.append(hour + "时");
        }
        delta %= MILLIS_HOUR;
        final long minute = delta / MILLIS_MINUTE;
        if (day > 0 || hour > 0 || minute > 0) {
            sb.append(minute + "分");
        }
        delta %= MILLIS_MINUTE;
        final long second = delta / MILLIS_SECOND;
        if (day > 0 || hour > 0 || minute > 0 || second > 0) {
            sb.append(second + "秒");
        }
        delta %= MILLIS_SECOND;
        sb.append(delta + "毫秒");
        return sb.toString();
    }

    public static Date yesterday() {
        return getTimeBeforeByDayInterval(getDayStart(new Date()), 1);
    }

    public static Date yesterdayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    /**
     * 计算用户岁数，算足的
     *
     * @param birth
     * @return 年龄
     */
    public static int age(Date birth) {
        return age(new Date(), birth);
    }

    /**
     * 计算用户岁数，算足的
     *
     * @param birth
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
     * @param date
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
     * @param date
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
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
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
        Date endDate = getDayEnd(new Date());
        return (endDate.getTime() - currentDate.getTime()) / 1000;
    }

}
