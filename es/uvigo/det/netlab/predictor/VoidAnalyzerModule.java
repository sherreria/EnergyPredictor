package es.uvigo.det.netlab.predictor;

/**
 * This class extends the AnalyzerModule class to implement a void analyzer.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class VoidAnalyzerModule extends AnalyzerModule
{
    /**
     * Creates a new void analyzer module.
     */
    public VoidAnalyzerModule ()
    {
	super();
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
	return new DataList("void", null);
    }
}
