package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement the Pro-Energy predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ProEnergyPredictorModule extends PredictorModule
{
    /**
     * The weighting factor
     */
    private double wFactor;
    
    /**
     * The correlation factor
     */
    private double cFactor;
    
    /**
     * Creates a new ProEnergy predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param wFactor     the weighting factor
     * @param cFactor     the correlation factor
     */
    public ProEnergyPredictorModule (DataList currentData, DataList pastData, double wFactor, double cFactor)
    {
	super(currentData, pastData);
	this.wFactor = wFactor;
	this.cFactor = cFactor;
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
	double gamma = wFactor;
	if (cFactor > 0) {
	    int initTimeslot = initEntry.getTimeslot();
	    int slotStep = initTimeslot - currentData.getEntryByIndex(currentData.getIndexByTimeslot(initTimeslot) - 1).getTimeslot();
	    gamma = wFactor * (1 - ((pastEntry.getTimeslot() - initTimeslot) / (double) slotStep - 1) / cFactor);
	    if (gamma < 0) {
		gamma = 0;
	    }
	}
	return gamma * initEntry.getValue() + (1 - gamma) * pastEntry.getValue();
    } 
}
