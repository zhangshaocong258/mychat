package com.szl.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zsc on 2016/4/15.
 */
public class DayTime {
    private Date date = new Date();
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private String dateString = formatter.format(date);

    public String getDateString() {
        return dateString;
    }
}
