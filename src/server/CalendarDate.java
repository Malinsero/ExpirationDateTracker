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
import java.util.Calendar;

public class CalendarDate implements Comparable<CalendarDate> {

    private int _day;
    private int _month;
    private int _year;
    
    public CalendarDate(int year, int month, int day) {
        _day = day;
        _month = month;
        _year = year;
    }
    
    /**
     * Construct a date from its string representation.
     * Strings are in the form YYYY:MM:DD
     * 
     * @param date the string representation of a CalendarDate.
     */
    public CalendarDate(String date) {
        _year = Integer.parseInt(date.substring(0, 4));
        _month = Integer.parseInt(date.substring(5, 7));
        _day = Integer.parseInt(date.substring(8));
    }
    
    /**
     * Given a date object, create a calendarDate from it.
     * 
     * @param date A date object.
     */
    public CalendarDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        _year = cal.get(Calendar.YEAR);
        _month = cal.get(Calendar.MONTH);
        _day = cal.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Getter method for the day of month.
     * 
     * @return The day of the month this date object represents.
     */
    public int getDay() {
        return _day;
    }
    
    /**
     * Getter method for the month.
     * 
     * @return The month of the date this object represents.
     */
    public int getMonth() {
        return _month;
    }
    
    /**
     * Getter method for the year.
     * 
     * @return The year for the date this object represents.
     */
    public int getYear() {
        return _year;
    }
    
    /**
     * Add the provided number of days to this date. Can be negative to subtract days.
     * 
     * @param days The number of days to be added to this object.
     */
    public void addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.set(_year, _month, _day);
        cal.add(Calendar.DAY_OF_MONTH, days);
        
        _year = cal.get(Calendar.YEAR);
        _month = cal.get(Calendar.MONTH);
        _day = cal.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Add the provided number of months to this date. Can be negative to subtract months.
     * 
     * @param months
     */
    public void addMonths(int months) {
        Calendar cal = Calendar.getInstance();
        cal.set(_year, _month, _day);
        cal.add(Calendar.MONTH, months);
        
        _year = cal.get(Calendar.YEAR);
        _month = cal.get(Calendar.MONTH);
        _day = cal.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Add the provided number of years to this date. Can be negative to subtract years.
     * 
     * @param years
     */
    public void addYears(int years) {
        Calendar cal = Calendar.getInstance();
        cal.set(_year, _month, _day);
        cal.add(Calendar.YEAR, years);
        
        _year = cal.get(Calendar.YEAR);
        _month = cal.get(Calendar.MONTH);
        _day = cal.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Returns a string representation of the CalendarDate.
     * 
     * @return String representation of the date in the form YYYY:MM:DD
     */
    public String toString() {
        String ret = "";
        ret += String.format("%04d:", _year);
        ret += String.format("%02d:", _month);
        ret += String.format("%02d:", _day);
        
        return ret;
    }
    
    /**
     * Compare this date to another CalendarDate object. Smaller means that it comes earlier.
     * null is considered the lowest date.
     */
    public int compareTo(CalendarDate date) {
        if (date == null) return 1;
        
        if (_year < date.getYear()) return -1;
        if (_year > date.getYear()) return 1;
        
        if (_month < date.getMonth()) return -1;
        if (_month > date.getMonth()) return 1;
        
        if (_day < date.getDay()) return -1;
        if (_day > date.getDay()) return 1;
        
        return 0;
    }
}
