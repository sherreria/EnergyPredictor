package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement the IPro-Energy predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class IproEnergyPredictorModule extends PredictorModule
{
    /**
     * The weighting factor
     */
    private double wFactor;
    
    /**
     * The smarting factor weight
     */
    private double sFactor;
    
    /**
     * Creates a new IProEnergy predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param slotStep    the timeslot step
     * @param wFactor     the weighting factor
     */
    public IproEnergyPredictorModule (DataList currentData, DataList pastData, double wFactor)
    {
	super(currentData, pastData);
	this.wFactor = wFactor;
	sFactor = 0.5;
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
	double initEntryValue = initEntry.getValue();
	double prevInitEntryValue = currentData.getEntryByIndex(currentData.getIndexByTimeslot(initEntry.getTimeslot()) - 1).getValue();
	double smartingFactor = initEntryValue + prevInitEntryValue == 0 ?
	    0.0 : 2 * sFactor * prevInitEntryValue * (initEntryValue - prevInitEntryValue) / (initEntryValue + prevInitEntryValue);
	return wFactor * initEntryValue + (1 - wFactor) * pastEntry.getValue() + smartingFactor;
    } 
}
