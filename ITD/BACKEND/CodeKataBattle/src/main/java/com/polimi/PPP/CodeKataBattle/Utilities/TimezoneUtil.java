package com.polimi.PPP.CodeKataBattle.Utilities;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimezoneUtil {

    /**
     * Converts the given ZonedDateTime to UTC.
     *
     * @param localDateTime ZonedDateTime in any timezone
     * @return ZonedDateTime in UTC
     */
    public static ZonedDateTime convertToUtc(ZonedDateTime localDateTime) {
        return localDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Converts a UTC ZonedDateTime to a specified timezone.
     *
     * @param utcDateTime ZonedDateTime in UTC
     * @return ZonedDateTime in the target timezone
     */
    public static ZonedDateTime convertUtcToLocalTime(ZonedDateTime utcDateTime) {
        return utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
    }
}