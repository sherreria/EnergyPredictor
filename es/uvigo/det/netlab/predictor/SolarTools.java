package es.uvigo.det.netlab.predictor;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;

/**
 * This class provides sunrise and sunset times for a given date, latitude and longitude.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class SolarTools
{
    private static String formatHttpAPI = "yyyyMMdd";
    private static String formatISO8601 = "yyyy-MM-dd'T'HH:mm:ss+'00:00'";

    private SolarTools () {}
    
    /**
     * Makes a HTTP request to the sunset and sunrise times API.
     *
     * @param  date      the date in YYYY-MM-DD or YYYYMMDD format
     * @param  latitude  the latitude in decimal degrees
     * @param  longitude the longitude in decimal degrees
     * @return the json response to the API request
     */
    private static String sunriseSunsetHttpRequest (String date, double latitude, double longitude) throws Exception
    {
	String jsonResponse = "";
	URL url = new URL("https://api.sunrise-sunset.org/json?lat=" + latitude + "&lng=" + longitude + "&date=" + date + "&formatted=0");
	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestMethod("GET");
	if (con.getResponseCode() == 200) {
	    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();
	    while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	    }
	    in.close();
	    jsonResponse = response.toString();
	}
	return jsonResponse;
    }
     
    /**
     * Obtains sunset and sunrise times for a given date, latitude and longitude using sunrise-sunset.org/api.
     *
     * @param  date      the date
     * @param  latitude  the latitude in decimal degrees
     * @param  longitude the longitude in decimal degrees
     * @return a Date array of length 2 with the corresponding sunrise and sunset times
     */   
    public static Date[] sunriseSunsetTimesHttp(Date date, double latitude, double longitude) throws Exception
    {
	SimpleDateFormat dateFormat = new SimpleDateFormat(formatHttpAPI);
	String jsonResponse = sunriseSunsetHttpRequest(dateFormat.format(date), latitude, longitude);
	String[] responseFields = jsonResponse.split("\"", 11);
	SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat(formatISO8601);
	dateFormatISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date[] sunriseSunsetTimes = new Date[2];
	sunriseSunsetTimes[0] = dateFormatISO8601.parse(responseFields[5]);
	sunriseSunsetTimes[1] = dateFormatISO8601.parse(responseFields[9]);
	return sunriseSunsetTimes;
    }    
      
    /**
     * Computes sunset and sunrise times for a given date, latitude and longitude.
     *
     * @param  date      the date
     * @param  latitude  the latitude in decimal degrees
     * @param  longitude the longitude in decimal degrees
     * @return a Date array of length 2 with the corresponding sunrise and sunset times
     */   
    public static Date[] sunriseSunsetTimes(Date date, double latitude, double longitude)
    {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	// Computation of chronological julian day number (https://www.aa.quae.nl/en/reken/juliaansedag.html)
	int m = calendar.get(Calendar.MONTH) + 1;
	int c0 = (int) Math.floor((m - 3) / 12.0);
	int x4 = calendar.get(Calendar.YEAR) + c0;
	int x3 = x4 / 100;
	int x2 = x4 - x3 * 100;
	int x1 = m - 12 * c0 - 3;
	int julianDay = 146097 * x3 / 4 + 36525 * x2 / 100 + (153 * x1 + 2) / 5 + calendar.get(Calendar.DAY_OF_MONTH) + 1721119;
	// Computation of sunrise and sunset julian times (https://en.wikipedia.org/wiki/Sunrise_equation)
	double meanSolarNoon = julianDay - 2451545 + 0.0008 - longitude / 360;
	double solarAnomaly = (357.5291 + 0.98560028 * meanSolarNoon) % 360;
	double solarAnomalyRad = solarAnomaly * Math.PI / 180;
	double centerEarth = 1.9148 * Math.sin(solarAnomalyRad) + 0.02 * Math.sin(2 * solarAnomalyRad) + 0.0003 * Math.sin(3 * solarAnomalyRad);
	double eclipticLongitude = ((solarAnomaly + centerEarth + 282.9372) % 360) * Math.PI / 180;
	double solarTransit = 2451545 + meanSolarNoon + 0.0053 * Math.sin(solarAnomalyRad) - 0.0069 * Math.sin(2 * eclipticLongitude);
	double declinationAngle = Math.asin(Math.sin(eclipticLongitude) * 0.3977882);
	double latAngle = latitude * Math.PI / 180;
	double hourAngle = Math.acos((-0.0144857 - Math.sin(latAngle) * Math.sin(declinationAngle)) / Math.cos(latAngle) / Math.cos(declinationAngle));
	double julianSunrise = solarTransit - hourAngle / 2 / Math.PI;
	double julianSunset = solarTransit + hourAngle / 2 / Math.PI;
	Date[] sunriseSunsetTimes = new Date[2];
	sunriseSunsetTimes[0] = julianDateToUTCDate(julianSunrise);
	sunriseSunsetTimes[1] = julianDateToUTCDate(julianSunset);
	return sunriseSunsetTimes;
    }
    
    /**
     * Converts the given julian date into the corresponding gregorian UTC date
     *
     * @param  jd the julian date
     * @return the corresponding gregorian UTC date 
     */
    public static Date julianDateToUTCDate(double jd)
    {
	double X = jd + 0.5;
	double Z = Math.floor(X); 
	double F = X - Z; 
	double Y = Math.floor((Z - 1867216.25) / 36524.25);
	double A = Z + 1 + Y - Math.floor(Y/4);
	double B = A + 1524;
	int C = (int) Math.floor((B - 122.1) / 365.25);
	int D = (int) Math.floor(365.25 * C);
	int G = (int) Math.floor((B - D) / 30.6001);
	int month = G < 13.5 ? G - 1 : G - 13;
	int year = month < 2.5 ? C - 4715 : C - 4716;
	double UTC = B - D - Math.floor(30.6001 * G) + F;
	int day = (int) Math.floor(UTC);
	UTC -= Math.floor(UTC);
	UTC *= 24;
	int hour = (int) Math.floor(UTC);
	UTC -= Math.floor(UTC);
	UTC *= 60;
	int minute = (int) Math.floor(UTC);
	UTC -= Math.floor(UTC);
	UTC *= 60;
	int second = (int) Math.round(UTC);
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
	calendar.set​(year, month, day, hour, minute, second);
	return calendar.getTime();
    }
    
    /**
     * Formats the given date into a date/time string using the given date format and time zone 
     *
     * @param  date   the date
     * @param  format the date format
     * @param  tz     the time zone
     * @return the corresponding date/time string using the given date format and time zone 
     */
    public static String getFormattedTime(Date date, String format, String tz)
    {
	SimpleDateFormat targetDateFormat = new SimpleDateFormat(format);
	targetDateFormat.setTimeZone(TimeZone.getTimeZone(tz));
	return targetDateFormat.format(date.getTime());
    }
}
