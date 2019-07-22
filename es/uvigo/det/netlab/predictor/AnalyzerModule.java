package es.uvigo.det.netlab.predictor;

import java.util.ArrayList;

/**
 * This class implements the analyzer module.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class AnalyzerModule
{
    /**
     * The data pool.
     */
    public ArrayList<DataList> dataPool;
    
    /**
     * Creates a new analyzer module.
     */
    public AnalyzerModule ()
    {
	dataPool = new ArrayList<DataList>();
    }

    /**
     * Adds the specified data list to the data pool of the module.
     * 
     * @param  dataList the data list
     * @return true if the specified data list was successfully added to the data pool
     */
    public boolean add (DataList dataList)
    {
	return dataPool.add(dataList);
    }

    /**
     * Returns the number of data lists in the pool of the module.
     *
     * @return the number of data lists in the pool of the module
     */
    public int size ()
    {
	return dataPool.size();
    }

    /**
     * Returns the most similar list in the pool of the module to the specified data list.
     *
     * @param  dataList   the data list
     * @param  initSlot   initial timeslot
     * @param  slotWindow number of past timeslots used to compute similarity
     * @return the most similar list in the pool of the module to the specified data list
     */
    abstract public DataList mostSimilarList (DataList dataList, int initSlot, int slotWindow);
}
