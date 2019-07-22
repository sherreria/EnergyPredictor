package es.uvigo.det.netlab.predictor;

import java.util.ArrayList;
import java.util.Date;
import java.io.*;

/**
 * This class implements a list of data entries.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class DataList
{
    /**
     * The label of the data list
     */
    private String label;
    
    /**
     * The date on which the data were collected.
     */
    private Date date;
    
    /**
     * The data list
     */
    private ArrayList<DataEntry> list;
    
    /**
     * Creates a new data list.
     *
     * @param label the label of the data list
     * @param date  the date on which the data were collected
     */
    public DataList (String label, Date date)
    {
	this.label = label;
	this.date = date;
	this.list = new ArrayList<DataEntry>();
    }
    
    /**
     * Returns the label of the data list.
     *
     * @return the label of the data list
     */
    public String getLabel ()
    {
	return label;
    }
     
    /**
     * Returns the date on which the data were collected.
     *
     * @return the date on which the data were collected
     */
    public Date getDate ()
    {
	return date;
    }
    
    /**
     * Returns the list of data entries in the data list.
     *
     * @return the list of data entries in the data list
     */
    public ArrayList<DataEntry> getList ()
    {
	return list;
    }
      
    /**
     * Returns the data entry in the data list at the specified position.
     *
     * @param  index index of the data entry to return
     * @return the data entry in the data list at the specified position 
     */
    public DataEntry getEntryByIndex (int index)
    {
	return list.get(index);
    }
    
    /**
     * Returns the data entry in the data list at the specified timeslot.
     *
     * @param  timeslot the timeslot
     * @return the data entry in the data list at the specified timeslot
     */
    public DataEntry getEntryByTimeslot (int timeslot)
    {
	int index = getIndexByTimeslot(timeslot);
	if (index < 0) {
	    return new DataEntry(0, timeslot, 0);
	}
	DataEntry entry = list.get(index);
	int entryTimeslot = entry.getTimeslot();
	if (entryTimeslot == timeslot || index == list.size() - 1) {
	    return entry;
	}
	DataEntry nextEntry = list.get(index + 1);
	double value = EnergyPredictor.energyTraces ?
	    nextEntry.getValue() * (timeslot - entryTimeslot) / (nextEntry.getTimeslot() - entryTimeslot) :
	    nextEntry.getValue();
	return new DataEntry(value, timeslot, 0);
    }
        
    /**
     * Returns the index of the data entry in the data list at the specified timeslot.
     *
     * @param  timeslot the timeslot
     * @return the index of the data entry in the data list at the specified timeslot
     */
    public int getIndexByTimeslot (int timeslot)
    {
	for (int i = 0; i < list.size(); i++) {
	    double entryTimeslot = list.get(i).getTimeslot();
	    if (entryTimeslot == timeslot) {
		return i;
	    } else if (entryTimeslot > timeslot) {
		if (i == 0) {
		    return 0;
		} else {
		    return i - 1;
		}
	    }
	}
	return list.size() - 1;
    }
    
    /**
     * Returns the number of data entries in the data list.
     *
     * @return the number of data entries in the data list
     */
    public int size ()
    {
	return list.size();
    }
    
    /**
     * Adds a new data entry to the data list with the specified value and timeslot.
     * 
     * @param  value    the data value
     * @param  timeslot the data timeslot
     * @return true if the new data entry was successfully added to the data list
     */
    public boolean addEntry (double value, int timeslot)
    {
	double weight = 0.0;
	if (list.size() > 0) {
	    DataEntry lastEntry = list.get(list.size() - 1);
	    weight = Math.log(1 + Math.abs(value - lastEntry.getValue()) * (timeslot - lastEntry.getTimeslot()));
	}
	return list.add(new DataEntry(value, timeslot, weight));
    }
    
    /**
     * Adds the data stored in the specified trace file to the data list.
     * 
     * @param  traceFile the trace file
     * @param  slotStep  the timeslot step
     * @return true if the data stored in the trace file was successfully added to the data list
     */
    public boolean addFile (File traceFile, int slotStep)
    {
	int timeslot = slotStep;
	try {
	    BufferedReader traceReader = new BufferedReader(new FileReader(traceFile));
	    try {
		for (String data; (data = traceReader.readLine()) != null; ) {
		    if (!addEntry(Double.parseDouble(data), timeslot)) {
			return false;
		    }
		    timeslot += slotStep; 
		}
	    } catch (IOException e) {
		return false;
	    }
	} catch (FileNotFoundException e) {
	    return false;
	}
	return true;
    }
    
    /**
     * Resizes the data list merging similar values and splitting very dissimilar ones.
     */
    public void resize ()
    {
	// Merge entries with very similar values
	int size = list.size();
	double sumWeight = 0.0;
	DataEntry entry = list.get(0);
	ArrayList<DataEntry> mergedList = new ArrayList<DataEntry>(size);
	for (int i = 1; i < size - 1; i++) {
	    entry = list.get(i);
	    if (entry.getWeight() <= 0.001 && list.get(i + 1).getWeight() <= 0.001) {
		continue;
	    } else {
		if (mergedList.size() == 0) {
		    mergedList.add(list.get(i - 1));
		}
		sumWeight += entry.getWeight();
		mergedList.add(new DataEntry(entry.getValue(), entry.getTimeslot(), entry.getWeight()));
	    }
	}
	entry = list.get(size - 1);
	sumWeight += entry.getWeight();
	mergedList.add(new DataEntry(entry.getValue(), entry.getTimeslot(), entry.getWeight()));
	list = mergedList;
	// Split entries with very dissimilar values
	int mergedSize = mergedList.size();
	if (mergedSize < size) {
	    ArrayList<DataEntry> resizedlist = new ArrayList<DataEntry>(size);
	    for (int i = 0; i < mergedSize; i++) {
		entry = mergedList.get(i);
		double entryValue = entry.getValue();
		int entryTimeslot = entry.getTimeslot();
		double entryWeight = entry.getWeight();
		int subslots = (int) Math.floor((size - mergedSize) * entryWeight / sumWeight) + 1;
		if (subslots > 1) {
		    DataEntry prevEntry = mergedList.get(i - 1);
		    double value = EnergyPredictor.energyTraces ? entryValue / subslots : entryValue;
		    int slotStep = (entryTimeslot - prevEntry.getTimeslot()) / subslots;
		    for (int j = 1; j <= subslots; j++) {
			int timeslot = prevEntry.getTimeslot() + j * slotStep;
			if (j == subslots && timeslot < entryTimeslot) {
			    timeslot = entryTimeslot;
			}
			entryWeight = j == 1 ? Math.log(1 + Math.abs(value - prevEntry.getValue()) * slotStep) : 0.0;
			resizedlist.add(new DataEntry(value, timeslot, entryWeight));
		    }
		} else {
		    resizedlist.add(new DataEntry(entryValue, entryTimeslot, entryWeight));
		}
	    }
	    list = resizedlist;
	}
    }
    
    /**
     * Returns the data entry with the maximum value in the data list.
     *
     * @return the data entry with the maximum value in the data list
     */
    public DataEntry getMaxEntry ()
    {
	int index = 0;
	double max = -Double.MAX_VALUE;
	for (int i = 0; i < list.size(); i++) {
	    double value = list.get(i).getValue();
	    if (value > max) {
		max = value;
		index = i;
	    }
	}
	return list.get(index);
    }
    
    /**
     * Returns the total energy harvested during all the timeslots of the data list
     *
     * @return Returns the total energy harvested during all the timeslots of the data list
     */
    public double getEnergyHarvested ()
    {
	int lastEntryTimeslot = getEntryByIndex(size() - 1).getTimeslot();
	double powerFactor = EnergyPredictor.energyTraces ? 1.0 : EnergyPredictor.SECONDS_PER_DAY / (double) lastEntryTimeslot;
	return getEnergyHarvested(0, lastEntryTimeslot, powerFactor);
    }
    
    /**
     * Returns the energy harvested between two given timeslots of the data list
     *
     * @return Returns the energy harvested between two given timeslots of the data list
     */
    public double getEnergyHarvested (int initialTimeslot, int finalTimeslot, double powerFactor)
    {
	if (finalTimeslot <= initialTimeslot) {
	    return 0.0;
	}
	int index = getIndexByTimeslot(initialTimeslot);
	DataEntry currEntry, prevEntry = getEntryByIndex(index);
	double currValue, prevValue = prevEntry.getValue();
	int currTimeslot, prevTimeslot = prevEntry.getTimeslot();
	double energy = 0.0;
	while (prevTimeslot < finalTimeslot) {
	    currEntry = getEntryByIndex(index + 1);
	    currValue = currEntry.getValue();
	    currTimeslot = currEntry.getTimeslot();
	    if (prevTimeslot < initialTimeslot) {
		energy -= EnergyPredictor.energyTraces ?
		    currValue * (initialTimeslot - prevTimeslot) / (currTimeslot - prevTimeslot) :
		    currValue * (initialTimeslot - prevTimeslot);
	    }
	    energy += EnergyPredictor.energyTraces ? currValue : currValue * (currTimeslot - prevTimeslot);
	    if (currTimeslot > finalTimeslot) {
		energy -= EnergyPredictor.energyTraces ?
		    currValue * (currTimeslot - finalTimeslot) / (currTimeslot - prevTimeslot) :
		    currValue * (currTimeslot - finalTimeslot);
	    }
	    prevValue = currValue;
	    prevTimeslot = currTimeslot;
	    index++;
	}
	energy *= powerFactor;
	return energy;
    }
    
    /**
     * Prints on standard output the data entries in the data list.
     */
    public void print ()
    {
	System.out.println();
	System.out.println("DataList: " + label + " Data entries: " + list.size() + " Energy harvested: " + getEnergyHarvested());
	System.out.println();
	for (DataEntry entry : list) {
	    entry.print();
	}
	System.out.println();
    }
}
