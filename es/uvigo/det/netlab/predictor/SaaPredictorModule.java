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
     * Use accurate model if true
     */
    private boolean accurateModel;
   
    /**
     * Factor used to compute the hour angle
     */
    private float hourFactor;
    
    /**
     * Creates a new SAA predictor module.
     *
     * @param currentData   the data list with current observations
     * @param pastData      the data list with past observations
     * @param accurateModel use accurate solar model to make predictions if true
     * @param seriesDegree  the degree of the Taylor/Chebyshev series used to approximate trigonometric functions
     */
    public SaaPredictorModule (DataList currentData, DataList pastData, boolean accurateModel, int seriesDegree)
    {
	super(currentData, pastData);
	sunriseTimeslot = ((SolarDataList) currentData).sunriseTimeslot();
	sunsetTimeslot = ((SolarDataList) currentData).sunsetTimeslot();
	noonTimeslot = ((SolarDataList) currentData).noonTimeslot();
	this.accurateModel = accurateModel;
	hourFactor = accurateModel ? 24f / currentData.getEntryByIndex(currentData.size() - 1).getTimeslot() : 0;
	TrigTools.seriesDegree = seriesDegree;
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
	int initEntryTimeslot = initEntry.getTimeslot();
	int pastEntryTimeslot = pastEntry.getTimeslot();

	if (accurateModel) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(currentData.getDate());
	    int dayofyear = calendar.get(Calendar.DAY_OF_YEAR);
	    float latAngle = (float) ((SolarDataList) currentData).latitude() * TrigTools.PI / 180;
	    float sinLatAngle = TrigTools.sin(latAngle);
	    float cosLatAngle = TrigTools.cos(latAngle);
	    float declAngle = -0.4092797f * TrigTools.cos((dayofyear + 10) * 2 * TrigTools.PI / 365);
	    float sinDeclAngle = TrigTools.sin(declAngle);
	    float cosDeclAngle = TrigTools.cos(declAngle);
	    float initHourAngle = TrigTools.PI / 12 * (initEntryTimeslot - noonTimeslot) * hourFactor;
	    float initAngle = TrigTools.asin(sinDeclAngle * sinLatAngle + cosDeclAngle * cosLatAngle * TrigTools.cos(initHourAngle));
	    if (initAngle < 0) {
		predictedValue = 0.0;
	    } else {
		float pastHourAngle = TrigTools.PI / 12 * (pastEntryTimeslot - noonTimeslot) * hourFactor;
		float pastAngle = TrigTools.asin(sinDeclAngle * sinLatAngle + cosDeclAngle * cosLatAngle * TrigTools.cos(pastHourAngle));
		predictedValue = initEntryValue * pastAngle / initAngle;
	    }
	} else {
	    float initSin = TrigTools.sin(TrigTools.PI * (initEntryTimeslot - sunriseTimeslot) / (sunsetTimeslot - sunriseTimeslot));
	    float pastSin = TrigTools.sin(TrigTools.PI * (pastEntryTimeslot - sunriseTimeslot) / (sunsetTimeslot - sunriseTimeslot));
	    predictedValue = initEntryValue * pastSin / initSin;
	}
	if (predictedValue < 0) {
	    predictedValue = 0.0;
	}
	return predictedValue;
    } 
}
