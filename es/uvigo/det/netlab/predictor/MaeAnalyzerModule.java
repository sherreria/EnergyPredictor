package es.uvigo.det.netlab.predictor;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class extends the AnalyzerModule class to implement a MAE (Mean Absolute Error) analyzer.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class MaeAnalyzerModule extends AnalyzerModule
{
    /**
     * Number of data lists combined to compute the similar data list
     */
    private int combinedDataLists;
    
    /**
     * Creates a new random analyzer module.
     *
     * @param combinedDataLists the number of data lists combined to compute the similar data list
     */
    public MaeAnalyzerModule (int combinedDataLists)
    {
	super();
	this.combinedDataLists = combinedDataLists;
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
	TreeMap<Double, DataList> listMap = new TreeMap<Double, DataList>();
	for (DataList poolList : dataPool) {
	    double mae = computeMAE(dataList, poolList, initSlot, slotWindow);
	    listMap.put(Double.valueOf(mae), poolList);
	}
	if (combinedDataLists <= 1) {
	    return listMap.firstEntry().getValue();
	}
	
	int counter = 0;
	double sumMae = 0.0;
	for (Map.Entry<Double, DataList> entryMap : listMap.entrySet()) {
	    sumMae += entryMap.getKey();
	    counter++;
	    if (counter == combinedDataLists) {
		break;
	    }
	}
	DataList weightedList = new DataList("mae.trace", null);
	for (DataEntry entry : dataList.getList()) {
	    int entryTimeslot = entry.getTimeslot();
	    counter = 0;
	    double value = 0.0;
	    for (Map.Entry<Double, DataList> entryMap : listMap.entrySet()) {
		value += (1 - entryMap.getKey() / sumMae) * entryMap.getValue().getEntryByTimeslot(entryTimeslot).getValue();
		counter++;
		if (counter == combinedDataLists) {
		    break;
		}
	    }
	    weightedList.addEntry(value / (combinedDataLists - 1), entry.getTimeslot());
	}
	return weightedList;
    }
    
    private static double computeMAE (DataList dataList1, DataList dataList2, int initSlot, int slotWindow)
    {
	int lastIndex = dataList1.getIndexByTimeslot(initSlot);
	int firstIndex = lastIndex - slotWindow + 1;
	if (firstIndex < 0) {
	    firstIndex = 0;
	}
	DataEntry entry1, entry2;
	double mae = 0.0;	
	for (int i = firstIndex; i <= lastIndex; i++) {
	    entry1 = dataList1.getEntryByIndex(i);
	    entry2 = dataList2.getEntryByTimeslot(entry1.getTimeslot());
	    mae += Math.abs(entry1.getValue() - entry2.getValue());
	}
	mae /= (lastIndex - firstIndex + 1);
	return mae;
    }
}
