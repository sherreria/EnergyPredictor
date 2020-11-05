package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement the EWMA predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class EwmaPredictorModule extends PredictorModule
{
    /**
     * The weighting factor
     */
    private double alpha;
    
    /**
     * Creates a new EWMA predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param alpha       the weighting factor
     */
    public EwmaPredictorModule (DataList currentData, DataList pastData, double wFactor)
    {
	super(currentData, pastData);
	alpha = wFactor;
    }
    
    /**
     * Returns the data value predicted for the timeslot of the specified past data entry.
     *
     * @param initEntry the initial data entry
     * @param pastEntry the past data entry
     * @return the data value predicted for the timeslot of the specified past data entry
     */
    public double getPrediction (DataEntry initEntry, DataEntry pastEntry)
    {
	return alpha * initEntry.getValue() + (1 - alpha) * pastEntry.getValue();
    } 
}
