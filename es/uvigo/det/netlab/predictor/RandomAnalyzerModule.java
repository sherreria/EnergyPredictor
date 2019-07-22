package es.uvigo.det.netlab.predictor;

import java.util.Random;

/**
 * This class extends the AnalyzerModule class to implement a random analyzer.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class RandomAnalyzerModule extends AnalyzerModule
{
    /**
     * The random number generator.
     */
    private Random rng;
    
    /**
     * Creates a new random analyzer module.
     */
    public RandomAnalyzerModule ()
    {
	super();
	rng = new Random();
    }
    
    /**
     * Returns the most similar list in the pool to the specified data list.
     *
     * @param  dataList   the data list
     * @param  initSlot   initial timeslot
     * @param  slotWindow number of past timeslots used to compute similarity
     * @return the most similar list in the pool to the specified data list
     */
    public DataList mostSimilarList (DataList dataList, int initSlot, int slotWindow)
    {
	return dataPool.get(rng.nextInt(size()));
    }
}
