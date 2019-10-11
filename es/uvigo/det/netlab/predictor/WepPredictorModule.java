package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement a WEP predictor.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class WepPredictorModule extends PredictorModule
{
    /**
     * The number of past timeslots used to compute predictions
     */
    private int slotWindow;

    /**
     * Apply error adjusment if true
     */
    private boolean errorAdjustment;
    
    /**
     * The past predictions
     */
    private double[] pastPredictions;

    /**
     * The past prediction errors
     */
    private double[] pastErrors;
    
    /**
     * Creates a new WEP predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param slotWindow  number of past timeslots used to compute predictions
     */
    public WepPredictorModule (DataList currentData, DataList pastData, int slotWindow)
    {
	super(currentData, pastData);
	this.slotWindow = slotWindow;
	errorAdjustment = false;
	pastPredictions = new double[currentData.size()];
	pastErrors = new double[currentData.size()];
    }
    
    /**
     * Returns the data value predicted for the given timeslot index.
     *
     * @param  index        the timeslot index
     * @param  currentIndex the current timeslot index
     * @return the data value predicted for the given timeslot index
     */
    private double computePrediction (int index, int currentIndex)
    {
	int firstIndex = index - slotWindow + 1;
	if (firstIndex < 1) {
	    firstIndex = 1;
	}
	int weight = 1;
	int sumWeights = 0;
	double sumValues = 0.0;
	double sumErrors = 0.0;
	for (int i = firstIndex; i <= index; i++) {
	    double value = i > currentIndex ? pastPredictions[i - 1] : currentData.getEntryByIndex(i).getValue();
	    sumValues += weight * value;
	    sumWeights += weight;
	    sumErrors += pastErrors[i - 1];
	    weight++;
	}
	double prediction = sumValues / sumWeights;
	if (errorAdjustment) {
	    prediction += sumErrors / (index - firstIndex + 1);
	}
	return prediction;
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
	int initIndex = currentData.getIndexByTimeslot(initEntry.getTimeslot());
	int pastIndex = currentData.getIndexByTimeslot(pastEntry.getTimeslot());
	int firstIndex = errorAdjustment ? 1 : initIndex;
	for (int index = firstIndex; index < pastIndex; index++) {
	    pastPredictions[index] = computePrediction(index, initIndex);
	    pastErrors[index] = index >= initIndex ? 0.0 : currentData.getEntryByIndex(index + 1).getValue() - pastPredictions[index];
	}
	return pastPredictions[pastIndex - 1];
    }
}
