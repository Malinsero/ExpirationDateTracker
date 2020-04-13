package server;

/**
 * Class that represents a date represented by a day, month, and year.
 * Used to compare dates and interpret XML date storage.
 * 
 * @author John Horning (johnhorning@gmail.com)
 * @version 11 April 2020
 *
 */

import java.util.Date;

public class CalendarDate {

    private int _day;
    private int _month;
    private int _year;
    
    public CalendarDate(int day, int month, int year) {
        _day = day;
        _month = month;
        _year = year;
    }
    
    public CalendarDate(Date date) {
        
    }
}
