package com.polimi.PPP.CodeKataBattle.Utilities;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimezoneUtil {

    public static ZonedDateTime convertToUtc(String localDateTimeString, String timeZoneId) {
        ZonedDateTime localDateTime = ZonedDateTime.parse(
                localDateTimeString,
                DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of(timeZoneId))
        );
        return localDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    }

    public static ZonedDateTime convertUtcToLocalTime(ZonedDateTime utcDateTime) {
        return utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
    }
}