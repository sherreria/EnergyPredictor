package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement the D-WCMA predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class DwcmaPredictorModule extends PredictorModule
{
    /**
     * The data list with the alpha weighting factors used for predictions
     */
    private DataList alphaFactorList;
    
    /**
     * The GAP factor
     */
    private double gap;
    
    /**
     * Creates a new D-WCMA predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param factorList  the data list with the alpha weighting factors required for predictions
     * @param initSlot    initial timeslot
     * @param slotWindow  number of past timeslots used to compute similarity
     */
    public DwcmaPredictorModule (DataList currentData, DataList pastData, DataList factorList, int initSlot, int slotWindow)
    {
	super(currentData, pastData);
	alphaFactorList = factorList;
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
	double alpha = alphaFactorList.getEntryByTimeslot(pastEntry.getTimeslot()).getValue();
	return alpha * initEntry.getValue() + (1 - alpha) * gap * pastEntry.getValue();
    } 
}
