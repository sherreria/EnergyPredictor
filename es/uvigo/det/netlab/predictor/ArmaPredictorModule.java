package es.uvigo.det.netlab.predictor;

/**
 * This class extends the PredictorModule class to implement a predictor using an ARMA model of first order.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ArmaPredictorModule extends PredictorModule
{        
    /**
     * The autoregressive coefficients
     */
    private double[] ar;

    /**
     * The autoregressive order
     */
    private int arOrder;
    
    /**
     * The moving average coefficients
     */
    private double[] ma;

    /**
     * The moving average order
     */
    private int maOrder;
    
    /**
     * Creates a new ARMA predictor module.
     *
     * @param currentData the data list with current observations
     * @param pastData    the data list with past observations
     * @param ar          the autoregressive coefficients
     * @param ma          the moving average coefficients
     */
    public ArmaPredictorModule (DataList currentData, DataList pastData, double[] ar, double[] ma)
    {
	super(currentData, pastData);
	this.ar = ar;
	arOrder = ar.length;
	this.ma = ma;
	maOrder = ma.length;
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
	double[] prediction = new double[pastIndex + 1];
	int firstIndex = ma[0] == 0 ? initIndex + 1 : arOrder + 1;
	for (int i = firstIndex; i <= pastIndex; i++) {
	    prediction[i] = 0;
	    for (int p = 1; p <= arOrder; p++) {
		double arValue = i > initIndex + p ? prediction[i - p] : currentData.getEntryByIndex(i - p).getValue();
		prediction[i] += ar[p - 1] * arValue;
	    }
	    for (int q = 1; q <= maOrder; q++) {
		double maValue = i > initIndex + q || prediction[i - q] == 0 ? 0.0 : currentData.getEntryByIndex(i - q).getValue() - prediction[i - q];
		prediction[i] -= ma[q - 1] * maValue;
	    }
	}
	return prediction[pastIndex];
    } 
}
