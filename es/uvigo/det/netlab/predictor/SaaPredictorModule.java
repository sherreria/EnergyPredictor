package es.uvigo.det.netlab.predictor;

import java.util.Calendar;

/**
 * This class extends the PredictorModule class to implement the SAA (Solar Altitude Angle) predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class SaaPredictorModule extends PredictorModule
{
    /**
     * The sunrise timeslot
     */
    private int sunriseTimeslot;
    
    /**
     * The sunset timeslot
     */
    private int sunsetTimeslot;

    /**
     * The noon timeslot
     */
    private int noonTimeslot;

    /**
     * Use exact solar model if true
     */
    private boolean exactSolarModel;
   
    /**
     * Factor used to compute the hour angle
     */
    private float hourFactor;

    /**
     * Half the duration of a timeslot
     */
    private int halfTimeslot;
    
    /**
     * Creates a new SAA predictor module.
     *
     * @param currentData   the data list with current observations
     * @param pastData      the data list with past observations
     * @param exactModel    use exact solar model to make predictions if true
     * @param seriesDegree  the degree of the Taylor/Chebyshev series used to approximate trigonometric functions
     */
    public SaaPredictorModule (DataList currentData, DataList pastData, boolean exactModel, int seriesDegree)
    {
	super(currentData, pastData);
	sunriseTimeslot = ((SolarDataList) currentData).sunriseTimeslot();
	sunsetTimeslot = ((SolarDataList) currentData).sunsetTimeslot();
	noonTimeslot = ((SolarDataList) currentData).noonTimeslot();
	exactSolarModel = exactModel;
	TrigTools.seriesDegree = seriesDegree;
	int currentDataSize = currentData.size();
	int lastEntryTimeslot = currentData.getEntryByIndex(currentDataSize - 1).getTimeslot();
	hourFactor = 24f / lastEntryTimeslot;
	halfTimeslot = lastEntryTimeslot / currentDataSize / 2;
    }
    
    /**
     * Returns the data value predicted for the timeslot of the specified past data entry.
     *
     * @param  initEntry the initial data entry
     * @param  pastEntry the past data entry
     * @return the data value predicted for the timeslot of the specified past data entry
     */
    public double getPrediction (DataEntry initEntry, DataEntry pastEntry)
    {
	double predictedValue = 0.0;
	double initEntryValue = initEntry.getValue();
	int initEntryTimeslot = initEntry.getTimeslot() - halfTimeslot;
	int pastEntryTimeslot = pastEntry.getTimeslot() - halfTimeslot;

	if (exactSolarModel) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(currentData.getDate());
	    int dayofyear = calendar.get(Calendar.DAY_OF_YEAR);
	    float latAngle = (float) ((SolarDataList) currentData).latitude() * TrigTools.PI / 180;
	    float sinLatAngle = TrigTools.sin(latAngle);
	    float cosLatAngle = TrigTools.cos(latAngle);
	    float declAngle = -0.40928f * TrigTools.cos((dayofyear + 10) * 2 * TrigTools.PI / 365);
	    float sinDeclAngle = TrigTools.sin(declAngle);
	    float cosDeclAngle = TrigTools.cos(declAngle);
	    float initHourAngle = TrigTools.PI / 12 * (initEntryTimeslot - noonTimeslot) * hourFactor;
	    float initAngle = TrigTools.asin(sinDeclAngle * sinLatAngle + cosDeclAngle * cosLatAngle * TrigTools.cos(initHourAngle));
	    if (initAngle > 0.00873) { // 0.5 degree
		float pastHourAngle = TrigTools.PI / 12 * (pastEntryTimeslot - noonTimeslot) * hourFactor;
		float pastAngle = TrigTools.asin(sinDeclAngle * sinLatAngle + cosDeclAngle * cosLatAngle * TrigTools.cos(pastHourAngle));
		predictedValue = initEntryValue * pastAngle / initAngle;
	    }
	} else {
	    float initSin = TrigTools.sin(TrigTools.PI * (initEntryTimeslot - sunriseTimeslot) / (sunsetTimeslot - sunriseTimeslot));
	    if (initSin > 0.00873) { // 0.5 degree
		float pastSin = TrigTools.sin(TrigTools.PI * (pastEntryTimeslot - sunriseTimeslot) / (sunsetTimeslot - sunriseTimeslot));
		predictedValue = initEntryValue * pastSin / initSin;
	    }
	}
	return predictedValue;
    } 
}
