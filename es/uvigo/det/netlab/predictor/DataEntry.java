package es.uvigo.det.netlab.predictor;

/**
 * This class implements a data entry.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class DataEntry
{
    /**
     * The data value
     */
    private double value;
    
    /**
     * The data timeslot
     */
    private int timeslot;
       
    /**
     * The data weight
     */
    private double weight;
    
    /**
     * Creates a new data entry.
     *
     * @param value    the data value
     * @param timeslot the data timeslot
     * @param weight   the data weight
     */
    public DataEntry (double value, int timeslot, double weight)
    {
	this.value = value;
	this.timeslot = timeslot;
	this.weight = weight;
    }
    
    /**
     * Returns the value of the data entry.
     *
     * @return the value of the data entry
     */
    public double getValue ()
    {
	return value;
    }
    
    /**
     * Returns the timeslot of the data entry.
     *
     * @return the timeslot of the data entry
     */
    public int getTimeslot ()
    {
	return timeslot;
    }
      
    /**
     * Returns the weight of the data entry.
     *
     * @return the weight of the data entry
     */
    public double getWeight ()
    {
	return weight;
    }
    
    /**
     * Sets the value of the data entry.
     *
     * @param value the value of the data entry
     */
    public void setValue (double value)
    {
	this.value = value;
    }

    /**
     * Sets the timeslot of the data entry.
     *
     * @param timeslot the timeslot of the data entry
     */
    public void setTimeslot (int timeslot)
    {
	this.timeslot = timeslot;
    }
    
    /**
     * Sets the weight of the data entry.
     *
     * @param weight the weight of the data entry
     */
    public void setWeight (double weight)
    {
	this.weight = weight;
    }

    /**
     * Prints on standard output the data entry.
     */
    public void print ()
    {
	System.out.printf("Timeslot: %d Value: %.6f Weight: %.6f %n", timeslot, value, weight);
    }
}
