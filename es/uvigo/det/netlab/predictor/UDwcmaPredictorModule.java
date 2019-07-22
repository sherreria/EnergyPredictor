package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement the UD-WCMA predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class UDwcmaPredictorModule extends PredictorModule
{
    /**
     * The data list corresponding to the most similar past day
     */
    private DataList similarData;
    
    /**
     * The data list with the alpha weighting factors used for predictions
     */
    private DataList alphaFactorList;
    
    /**
     * The data list with the beta weighting factors used for predictions
     */
    private DataList betaFactorList;
    
    /**
     * The GAP factor
     */
    private double gap;
    
    /**
     * Creates a new UD-WCMA predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with average observations in the past days
     * @param similarData the data list corresponding to the most similar past day
     * @param factorLists the data lists with the weighting factors required for predictions
     * @param initSlot    initial timeslot
     * @param slotWindow  number of past timeslots used to compute similarity
     */
    public UDwcmaPredictorModule (DataList currentData, DataList pastData, DataList similarData, DataList[] factorLists, int initSlot, int slotWindow)
    {
	super(currentData, pastData);
	this.similarData = similarData;
	alphaFactorList = factorLists[0];
	betaFactorList = factorLists[1];
	// GAP factor computation
	double sumGap = 0.0;
	int initIndex = currentData.getIndexByTimeslot(initSlot);
	for (int i = 1; i <= slotWindow; i++) {
	    double pastValue = pastData.getEntryByIndex(initIndex - slotWindow + i).getValue();
	    if (pastValue == 0) {
		continue;
	    }
	    sumGap += i * currentData.getEntryByIndex(initIndex - slotWindow + i).getValue() / pastValue;
	}
	gap = 2 * sumGap / slotWindow / (slotWindow + 1);
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
	int pastTimeslot = pastEntry.getTimeslot();
	double alpha = alphaFactorList.getEntryByTimeslot(pastTimeslot).getValue();
	double beta = betaFactorList.getEntryByTimeslot(pastTimeslot).getValue();
	double similarValue = similarData.getEntryByTimeslot(pastTimeslot).getValue();
	return alpha * (beta * initEntry.getValue() + (1 - beta) * similarValue) + (1 - alpha) * gap * pastEntry.getValue();
    } 
}
