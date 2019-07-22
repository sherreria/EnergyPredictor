package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement a dumb predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class DumbPredictorModule extends PredictorModule
{
    /**
     * Creates a new dumb predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     */
    public DumbPredictorModule (DataList currentData, DataList pastData)
    {
	super(currentData, pastData);
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
	return initEntry.getValue();
    } 
}
