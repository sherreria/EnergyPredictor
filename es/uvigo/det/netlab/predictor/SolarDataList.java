package es.uvigo.det.netlab.predictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class extends the DataList class to implement a list of solar data entries.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class SolarDataList extends DataList
{
    /**
     * The date corresponding to the sunrise in the solar data list.
     */
    private Date sunriseDate;
    
    /**
     * The date corresponding to the sunset in the solar data list.
     */
    private Date sunsetDate;
    
    /**
     * The timeslot corresponding to the sunrise in the solar data list.
     */
    private int sunriseSlot;
    
    /**
     * The timeslot corresponding to the sunset in the solar data list.
     */
    private int sunsetSlot;
    
    /**
     * The timeslot corresponding to the noon in the solar data list.
     */
    private int noonSlot;
      
    /**
     * The time zone of the corresponding location.
     */
    private String timeZone;
    
    /**
     * The latitude of the corresponding location in decimal degrees.
     */
    private double latitude;

    /**
     * The longitude of the corresponding location in decimal degrees.
     */
    private double longitude;
    
    /**
     * Creates a new solar data list.
     *
     * @param label the label of the solar data list
     * @param date  the date on which the solar data were collected
     * @param tz    the time zone of the corresponding location
     * @param lat   the latitude of the corresponding location in decimal degrees
     * @param lng   the longitude of the corresponding location in decimal degrees
     */
    public SolarDataList (String label, Date date, String tz, double lat, double lng)
    {
	super(label, date);
	sunriseSlot = sunsetSlot = noonSlot = -1;
	sunriseDate = sunsetDate = null;
	timeZone = tz;
	latitude = lat;
	longitude = lng;
    }
    
    /**
     * Returns the latitude of the corresponding location.
     *
     * @return the latitude of the corresponding location
     */
    public double latitude ()
    {
	return latitude;
    }
      
    /**
     * Returns the longitude of the corresponding location.
     *
     * @return the longitude of the corresponding location
     */
    public double longitude ()
    {
	return longitude;
    }
    
    /**
     * Returns the timeslot corresponding to the given time/date in the solar data list.
     *
     * @param  date the date
     * @return the timeslot corresponding to the given time/date in the solar data list
     */
    private int convertDateToTimeslot (Date date)
    {
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
	calendar.setTime(date);
	int seconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600;
	seconds += calendar.get(Calendar.MINUTE) * 60;
	seconds += calendar.get(Calendar.SECOND);
	return seconds * getEntryByIndex(size() - 1).getTimeslot() / EnergyPredictor.SECONDS_PER_DAY;
    }

    /**
     * Estimates the timeslot corresponding to the sunrise in the solar data list.
     *
     * @return the timeslot corresponding to the sunrise in the solar data list
     */
    private int estimateSunriseTimeslot ()
    {
	DataEntry prevEntry = getList().get(0);
	for (DataEntry entry : getList()) {
	    if (entry.getValue() > 0.1) {
		return prevEntry.getTimeslot();
	    }
	    prevEntry = entry;
	}
	return prevEntry.getTimeslot();
    }
    
    /**
     * Returns the timeslot corresponding to the sunrise in the solar data list.
     *
     * @return the timeslot corresponding to the sunrise in the solar data list
     */
    public int sunriseTimeslot ()
    {
	if (sunriseSlot < 0) {
	    if (sunriseDate == null) {
		Date[] sunriseSunsetDates = SolarTools.sunriseSunsetTimes(getDate(), latitude, longitude);
		sunriseDate = sunriseSunsetDates[0];
		sunsetDate = sunriseSunsetDates[1];
	    }
	    sunriseSlot = convertDateToTimeslot(sunriseDate);
	}
	return sunriseSlot;
    }

    /**
     * Estimates the timeslot corresponding to the sunset in the solar data list.
     *
     * @return the timeslot corresponding to the sunset in the solar data list
     */
    private int estimateSunsetTimeslot ()
    {
	ArrayList<DataEntry> revList = new ArrayList<DataEntry>(getList());
	Collections.reverse(revList);
	DataEntry prevEntry = revList.get(0);
	for (DataEntry entry : revList) {
	    if (entry.getValue() > 0.1) {
		return prevEntry.getTimeslot();
	    }
	    prevEntry = entry;
	}
	return prevEntry.getTimeslot();
    }
    
    /**
     * Returns the timeslot corresponding to the sunset in the solar data list.
     *
     * @return the timeslot corresponding to the sunset in the solar data list
     */
    public int sunsetTimeslot ()
    {
	if (sunsetSlot < 0) {
	    if (sunsetDate == null) {
		Date[] sunriseSunsetDates = SolarTools.sunriseSunsetTimes(getDate(), latitude, longitude);
		sunriseDate = sunriseSunsetDates[0];
		sunsetDate = sunriseSunsetDates[1];
	    }
	    sunsetSlot = convertDateToTimeslot(sunsetDate);
	}
	return sunsetSlot;
    }
    
    /**
     * Returns the timeslot corresponding to the noon in the solar data list.
     *
     * @return the timeslot corresponding to the noon in the solar data list
     */
    public int noonTimeslot ()
    {
	if (noonSlot < 0) {
	    noonSlot = (sunriseTimeslot() + sunsetTimeslot()) / 2;
	}
	return noonSlot;
    }

    /**
     * Prints on standard output the data entries in the solar data list.
     */
    public void print ()
    {
	super.print();
	System.out.println("Sunrise: " + sunriseTimeslot() + " Sunset: " + sunsetTimeslot() + " Noon: " + noonTimeslot());
    }
}
