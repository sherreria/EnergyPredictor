package es.uvigo.det.netlab.predictor;

/**
 * This class extends the AnalyzerModule class to implement an average analyzer.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class AverageAnalyzerModule extends AnalyzerModule
{
    /**
     * The data list with the alpha weighting factors for UD-WCMA predictions
     */
    private DataList alphaFactorList;
    
    /**
     * The data list with the beta weighting factors for UD-WCMA predictions
     */
    private DataList betaFactorList;
    
    /**
     * Creates a new average analyzer module.
     */
    public AverageAnalyzerModule ()
    {
	super();
    }

    /**
     * Returns the most similar list in the pool to the specified data list.
     *
     * @param dataList   the data list
     * @param initSlot   initial timeslot
     * @param slotWindow number of past timeslots used to compute similarity
     * @return the most similar list in the pool to the specified data list
     */
    public DataList mostSimilarList (DataList dataList, int initSlot, int slotWindow)
    {
	DataList weightedList = new DataList("average.trace", null);
	for (int index = 0; index < dataPool.get(0).size(); index++) {
	    double sumValue = 0;
	    DataEntry entry = null;
	    for (DataList poolList : dataPool) {
		entry = poolList.getEntryByIndex(index);
		sumValue += entry.getValue();
	    }
	    weightedList.addEntry(sumValue / size(), entry.getTimeslot());
	}
	computeWeightingFactors(dataList, weightedList, initSlot, slotWindow);
	return weightedList;
    }
    
    /**
     * Returns the data list with the alpha weighting factors for UD-WCMA predictions.
     *
     * @return the data list with the alpha weighting factors for UD-WCMA predictions
     */
    public DataList alphaWeightingFactorList ()
    {
	return alphaFactorList;
    }
     
    /**
     * Returns the data list with the beta weighting factors for UD-WCMA predictions.
     *
     * @return the data list with the beta weighting factors for UD-WCMA predictions
     */
    public DataList betaWeightingFactorList ()
    {
	return betaFactorList;
    }
    
    /**
     * Computes the data lists with the weighting factors for UD-WCMA predictions.
     */
    private void computeWeightingFactors (DataList dataList, DataList weightedList, int initSlot, int slotWindow)
    {
	DataList deviationList = new DataList("deviation.trace", null);
	DataList averageVarList = new DataList("averageVar.trace", null);
	int numDataLists = size();
	DataEntry initSlotEntry, entry = null;
	int initIndex = dataPool.get(0).getIndexByTimeslot(initSlot);
	int endIndex = dataPool.get(0).size();
	for (int index = initIndex; index < endIndex; index++) {
	    double sumValue = 0;
	    double sumVarValue = 0;
	    for (DataList poolList : dataPool) {
		initSlotEntry = poolList.getEntryByTimeslot(initSlot);
		entry = poolList.getEntryByIndex(index);
		sumValue += Math.pow(entry.getValue() - weightedList.getEntryByIndex(index).getValue(), 2);
		sumVarValue += entry.getValue() - initSlotEntry.getValue();
	    }
	    deviationList.addEntry(Math.sqrt(sumValue / numDataLists), entry.getTimeslot());
	    averageVarList.addEntry(sumVarValue / numDataLists, entry.getTimeslot());
	}
	
	double[] dataDiff = new double[slotWindow - 1];
	double sumDataDiff = 0.0;
	int initDataIndex = dataList.getIndexByTimeslot(initSlot);
	for (int index = initDataIndex; index >= initDataIndex - slotWindow + 2; index--) {
	    dataDiff[initDataIndex - index] = dataList.getEntryByIndex(index).getValue() - dataList.getEntryByIndex(index - 1).getValue();
	    sumDataDiff += dataDiff[initDataIndex - index];
	}
	double avgDataDiff = sumDataDiff / (slotWindow - 1);
	double sumVarDataDiff = 0.0;
	for (int k = 0; k < slotWindow - 1; k++) {
	    sumVarDataDiff += Math.pow(dataDiff[k] - avgDataDiff, 2);
	}
	double devDataDiff = Math.sqrt(sumVarDataDiff / (slotWindow - 1));
	
	alphaFactorList = new DataList("alphaWeightingFactor.trace", null);
	betaFactorList = new DataList("betaWeightingFactor.trace", null);
	for (DataEntry averageVarEntry : averageVarList.getList()) {
	    int timeslot = averageVarEntry.getTimeslot();
	    double deviation = deviationList.getEntryByTimeslot(timeslot).getValue();
	    double sumValue = 0;
	    for (DataList poolList : dataPool) {
		initSlotEntry = poolList.getEntryByTimeslot(initSlot);
		entry = poolList.getEntryByTimeslot(timeslot);
		sumValue += Math.pow(entry.getValue() - initSlotEntry.getValue() - averageVarEntry.getValue(), 2);
	    }
	    double alphaWeightingFactor = deviation / (deviation + Math.sqrt(sumValue / numDataLists)) / 2;
	    double betaWeightingFactor = alphaWeightingFactor + deviation / (deviation + devDataDiff) / 2;
	    alphaFactorList.addEntry(alphaWeightingFactor, timeslot);
	    betaFactorList.addEntry(betaWeightingFactor, timeslot);
	}
    }
}
