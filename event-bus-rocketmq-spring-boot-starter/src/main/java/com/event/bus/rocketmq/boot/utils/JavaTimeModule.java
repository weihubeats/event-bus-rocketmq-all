package com.event.bus.rocketmq.boot.utils;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : wh
 * @date : 2023/11/29 10:30
 * @description:
 */
public class JavaTimeModule extends SimpleModule {

    public static final String YYYYMMddHHmmss = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYYMMdd = "yyyy-MM-dd";

    public static final String HHmmss = "HH:mm:ss";

    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern(YYYYMMddHHmmss);

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(YYYYMMdd);

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(HHmmss);

    public JavaTimeModule() {
        super(PackageVersion.VERSION);
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMAT));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMAT));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMAT));
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMAT));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMAT));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMAT));
    }

}