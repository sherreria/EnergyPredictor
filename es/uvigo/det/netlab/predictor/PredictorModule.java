package es.uvigo.det.netlab.predictor;

/**
 * This class implements the predictor module.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class PredictorModule
{
    /**
     * The data list with current observations.
     */
    public DataList currentData;
    
    /**
     * The data list with past observations.
     */
    public DataList pastData;
    
    /**
     * Creates a new predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     */
    public PredictorModule (DataList currentData, DataList pastData)
    {
	this.currentData = currentData;
	this.pastData = pastData;
    }
    
    /**
     * Returns a data list with predictions from the initial timeslot to the final one.
     *
     * @param initialTimeslot the initial timeslot
     * @param finalTimeslot   the final timeslot
     * @param stepTimeslot    the timeslot step
     * @return the data list with predictions from the initial timeslot to the final one
     */
    public DataList getPredictions (int initialTimeslot, int finalTimeslot, int stepTimeslot)
    {
	DataEntry currentEntry = currentData.getEntryByTimeslot(initialTimeslot);
	DataList predictionsList = new DataList(initialTimeslot + ".predictions", null);
	predictionsList.addEntry(0, initialTimeslot);
	for (int timeslot = initialTimeslot + stepTimeslot; timeslot <= finalTimeslot; timeslot += stepTimeslot) {
	    predictionsList.addEntry(getPrediction(currentEntry, pastData.getEntryByTimeslot(timeslot)), timeslot);
	}
	return predictionsList;
    }
    
    /**
     * Returns the data value predicted for the timeslot of the specified past data entry.
     *
     * @param initEntry the initial data entry
     * @param pastEntry the past data entry
     * @return the data value predicted for the timeslot of the specified past data entry
     */
    abstract public double getPrediction (DataEntry initEntry, DataEntry pastEntry);
}
