package es.uvigo.det.netlab.predictor;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Arrays;

/**
 * EnergyPredictor: Java program that predicts future energy availability using different energy models.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class EnergyPredictor
{
    static final int SECONDS_PER_DAY = 86400;

    /**
     * Solar trace files if true.
     */
    public static boolean solarTraces = false;

    /**
     * Energy (power) values are stored in the trace files if true (false).
     */
    public static boolean energyTraces = false;
    
    /**
     * Accumulated predictions if true.
     */
    public static boolean accPredictions = false;  
    
    private EnergyPredictor () {}

    /**
     * Prints the specified message on standard error and exits.
     */
    public static void printError (String s)
    {
	System.err.println("\nERROR: " + s + "\n");
	System.exit(1);
    }
    
    /**
     * Main method
     * Usage: java EnergyPredictor FILE [-solar] [-energy] [-acc]
     */
    public static void main (String[] args)
    {
	// Trace settings variables
	File challengePath = null;
	String challengeDateFormat = "yyyyMMdd'.trace'";
	File tracePath = null;
	String traceExtension = ".trace";
	String traceDateFormat = "yyyyMMdd'.trace'";
	// Location settings variables
	double latitude = 0.0;
	double longitude = 0.0;
	String timeZone = "";
	// Prediction settings variables
	int initialTimeslot = 0;
	int finalTimeslot = 0;
	int timeslotStep = 1;
	int predictionHorizon = 1;
	int predictionStep = 1;
	// Predictor settings variables
	String analyzerMode = "void";
	String predictorMode = "dumb";
	int timeslotWindow = 1;
	int combinedTraceFiles = 1;
	int numPreviousDays = 0;
	double weightingFactor = 0.5;
	double correlationFactor = 0;
	boolean accurateSaaModel = false;
	int seriesDegree = 0;
	
	// Arguments parsing
	if (args.length < 1) {
	    printError("Usage: java EnergyPredictor FILE [-solar] [-energy] [-acc]");
	}
	BufferedReader configFile = null;
	try {
	    configFile = new BufferedReader(new FileReader(args[0]));
	} catch (FileNotFoundException e) {
	    printError("Config file " + args[1] + " not found!");
	}
	for (int i = 1; i < args.length; i++) {
	    if (args[i].equals("-solar")) {
		solarTraces = true;
	    } else if (args[i].equals("-energy")) {
		energyTraces = true;
	    } else if (args[i].equals("-acc")) {
		accPredictions = true;
	    } else {
		printError("Unknown argument: " + args[i] + "\n\nUsage: java EnergyPredictor FILE [-solar] [-energy] [-acc]");
	    }
	}

	// Configuration file parsing
        try {
            for (String line; (line = configFile.readLine()) != null;) {
                if (line.startsWith(";")) { // Just a comment
                    continue;
                }
                String[] line_fields = line.split("\\s+");
                if (line_fields[0].equals("CHALLENGE") && line_fields.length > 1) {
		    challengePath = new File(line_fields[1]);
		    if (!challengePath.exists()) {
			printError("Config file: challenge file " + line_fields[1] + " does not exist!");
		    }
		    if (challengePath.isDirectory()) {
			printError("Config file: challenge file " + line_fields[1] + " is a directory!");
		    }
		    if (line_fields.length > 2) {
			challengeDateFormat = line_fields[2];
		    }
		} else if (line_fields[0].equals("TRACES") && line_fields.length > 1) {
		    tracePath = new File(line_fields[1]);
		    if (line_fields.length > 2) {
			traceExtension = line_fields[2];
			if (line_fields.length > 3) {
			    traceDateFormat = line_fields[3];
			}
		    }
		} else if (line_fields[0].equals("LOCATION")) {
		    if (line_fields.length < 4) {
			printError("Config file: not enough location parameters!");
		    }
		    try {
			latitude = Double.parseDouble(line_fields[1]);
			longitude = Double.parseDouble(line_fields[2]);
		    } catch (NumberFormatException e) {
			printError("Config file: invalid challenge location!");
		    }
		    timeZone = line_fields[3];
		    if (Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone) == false) {
			printError("Config file: invalid challenge time zone!");
		    }
		} else if (line_fields[0].equals("SETTINGS")) {
		    if (line_fields.length < 6) {
			printError("Config file: not enough settings parameters!");
		    }
		    try {
			initialTimeslot = Integer.parseInt(line_fields[1]);
			finalTimeslot = Integer.parseInt(line_fields[2]);
			timeslotStep = Integer.parseInt(line_fields[3]);
			predictionHorizon = Integer.parseInt(line_fields[4]);
			predictionStep = Integer.parseInt(line_fields[5]);
		    } catch (NumberFormatException e) {
                        printError("Config file: invalid settings configuration!");
                    }
		    if (initialTimeslot < 0) {
			printError("Config file: invalid initial timeslot!");
		    }
		    if (finalTimeslot < initialTimeslot) {
			printError("Config file: invalid final timeslot!");
		    }
		    if (timeslotStep <= 0) {
			printError("Config file: invalid timeslot step!");
		    }
		    if (predictionHorizon < predictionStep) {
			printError("Config file: invalid prediction horizon!");
		    }
		    if (initialTimeslot > 0 && finalTimeslot > 0 && finalTimeslot < initialTimeslot + predictionHorizon) {
			printError("Config file: invalid prediction horizon!");
		    }
		    if (predictionStep < 0) {
			printError("Config file: invalid prediction step!");
		    } else if (predictionStep == 0) {
			predictionStep = timeslotStep;
		    }
		} else if (line_fields[0].equals("PREDICTOR") && line_fields.length > 1) {
		    if (line_fields[1].matches("dumb|pro-energy|pro-energy-vlt|ipro-energy|dwcma|udwcma|saa|saa-sine|wep")) {
			predictorMode = line_fields[1];
		    } else {
                        printError("Config file: invalid predictor mode!");
                    }
		    if (predictorMode.matches("pro-energy|pro-energy-vlt|ipro-energy|dwcma|udwcma")) {
			try {
			    timeslotWindow = Integer.parseInt(line_fields[2]);
			    combinedTraceFiles = Integer.parseInt(line_fields[3]);
			} catch (NumberFormatException e) {
			    printError("Config file: invalid " + predictorMode + " predictor configuration!");
			}
			if (timeslotWindow <= 1) {
			    printError("Config file: invalid timeslot window!");
			}
			if (combinedTraceFiles <= 0) {
			    printError("Config file: invalid number of trace files to combine!");
			}
			if (predictorMode.matches("dwcma|udwcma")) {
			    analyzerMode = "average";
			    numPreviousDays = combinedTraceFiles;
			} else {
			    analyzerMode = "mae";
			    try {
				numPreviousDays = Integer.parseInt(line_fields[4]);
				weightingFactor = Double.parseDouble(line_fields[5]);
				if (predictorMode.matches("pro-energy|pro-energy-vlt")) {
				    correlationFactor = Double.parseDouble(line_fields[6]);
				}
			    } catch (NumberFormatException e) {
				printError("Config file: invalid " + predictorMode + " predictor configuration!");
			    }
			    if (numPreviousDays < 0) {
				printError("Config file: invalid number of previous trace files!");
			    }
			    if (weightingFactor < 0 || weightingFactor > 1) {
				printError("Config file: invalid weighting factor!");
			    }
			    if (correlationFactor < 0) {
				printError("Config file: invalid correlation factor!");
			    }
			}
		    }
		    if (predictorMode.matches("saa|saa-sine")) {
			if (predictorMode.equals("saa")) {
			    accurateSaaModel = true;
			}
			if (line_fields.length > 2) {
			    try {
				seriesDegree = Integer.parseInt(line_fields[2]);
			    } catch (NumberFormatException e) {
				printError("Config file: invalid degree for Taylor/Chebyshev series!");
			    }
			    if (seriesDegree < 0 || seriesDegree > 13) {
				printError("Config file: invalid degree for Taylor/Chebyshev series!");
			    }
			}
		    }
		    if (predictorMode.equals("wep")) {
			if (line_fields.length > 2) {
			    try {
				timeslotWindow = Integer.parseInt(line_fields[2]);
			    } catch (NumberFormatException e) {
				printError("Config file: invalid timeslot window!");
			    }
			    if (timeslotWindow < 1) {
				printError("Config file: invalid timeslot window!");
			    }
			}
		    }
                }
            }
            configFile.close();
        } catch (IOException e) {
            printError("Error while reading config file!");
        }
	
	// Processing challenge file
	if (challengePath == null) {
	    printError("Challenge file not specified!");
	}
	String challengeFilename = challengePath.getName();
	Date challengeDate = null;
	try {
	    challengeDate = new SimpleDateFormat(challengeDateFormat).parse(challengeFilename);
	} catch (Exception e) {
	    printError("Error while obtaining challenge file date!");
	}
	DataList challengeList = solarTraces ?
	    new SolarDataList(challengeFilename, challengeDate, timeZone, latitude, longitude) :
	    new DataList(challengeFilename, challengeDate);
	if (challengeList.addFile(challengePath, timeslotStep) == false) {
	    printError(challengeFilename + " reading error!");
	}
	if (predictorMode.equals("pro-energy-vlt")) {
	    challengeList.resize();
	}
	//challengeList.print();
	if (solarTraces && initialTimeslot == 0 && finalTimeslot == 0) {
	    initialTimeslot = timeslotStep * (int) (Math.round(((SolarDataList) challengeList).sunriseTimeslot() / (double) timeslotStep) + 1);
	    finalTimeslot = timeslotStep * (int) Math.round(((SolarDataList) challengeList).sunsetTimeslot() / (double) timeslotStep);
	    if (finalTimeslot < initialTimeslot + predictionHorizon) {
		printError("Invalid final timeslot!");
	    }
	}
	
	// Processing trace files
	final String traceFileExtension = traceExtension;
	FileFilter traceFilefilter = new FileFilter() {
		public boolean accept (File file) {
		    return file.getName().endsWith(traceFileExtension);
		}
	    };
	File[] traceFiles = null;
	if (!analyzerMode.equals("void")) {
	    if (!tracePath.exists()) {
		printError("Config file: trace path " + tracePath.getName() + " does not exist!");
	    }
	    if (!tracePath.isDirectory()) {
		printError("Config file: trace path " + tracePath.getName() + " is not a directory!");
	    }
	    traceFiles = tracePath.listFiles(traceFilefilter);
	    if (traceFiles.length == 0) {
		printError(tracePath.getName() + " does not contain any trace file!");
	    }
	}
	AnalyzerModule analyzer = null, udwcmaAnalyzer = null;
	if (analyzerMode.equals("void")) {
	    analyzer = new VoidAnalyzerModule();
	} else if (analyzerMode.equals("random")) {
	    analyzer = new RandomAnalyzerModule();
	} else if (analyzerMode.equals("mae")) {
	    analyzer = new MaeAnalyzerModule(combinedTraceFiles);
	} else if (analyzerMode.equals("average")) {
	    analyzer = new AverageAnalyzerModule();
	}
	if (predictorMode.equals("udwcma")) {
	    udwcmaAnalyzer = new MaeAnalyzerModule(1);
	}
	if (!analyzerMode.equals("void")) {
	    DataList traceList;
	    String traceFilename;
	    Date traceDate = null;
	    for (File f : traceFiles) {
		traceFilename = f.getName();
		try {
		    traceDate = new SimpleDateFormat(traceDateFormat).parse(traceFilename);
		} catch (Exception e) {
		    printError("Error while obtaining trace date!");
		}
		traceList = solarTraces ?
		    new SolarDataList(traceFilename, traceDate, timeZone, latitude, longitude) : new DataList(traceFilename, traceDate);
		if (numPreviousDays > 0) {
		    long diffDays = (challengeList.getDate().getTime() - traceList.getDate().getTime()) / SECONDS_PER_DAY / 1000;
		    if (diffDays <= 0 || diffDays > numPreviousDays) {
			continue;
		    }
		}
		if (traceList.addFile(f, timeslotStep) == false) {
		    printError(f.getPath() + " reading error!");
		}
		analyzer.add(traceList);
		if (predictorMode.equals("udwcma")) {
		    udwcmaAnalyzer.add(traceList);
		}
	    }
	}
	System.out.println("\nTrace pool size: " + analyzer.size() + "\n");
	
	// Accumulated data lists
	int numAccList = (finalTimeslot - initialTimeslot) / timeslotStep;
	DataList[] accPredictionsList = new DataList[numAccList];
	DataList[] accChallengesList = new DataList[numAccList];
	DataList initChallengeList = new DataList("init-" + challengeFilename, null);
	double powerFactor = energyTraces ? 1.0 : SECONDS_PER_DAY / (double) challengeList.getEntryByIndex(challengeList.size() - 1).getTimeslot();
	    
	// Computing predictions
	int numHorizons = predictionHorizon / predictionStep;
	DataList[] predictionsList = new DataList[numHorizons];
	for (int i = 0; i < numHorizons; i++) {
	    int horizon = (i + 1) * predictionStep;
	    predictionsList[i] = new DataList(horizon + "-horizon.predictions", null);
	}
	PredictorModule predictor = null;
	for (int t = initialTimeslot; t < finalTimeslot; t += timeslotStep) {
	    DataList similarList = analyzer.mostSimilarList(challengeList, t, timeslotWindow);
	    if (similarList == null) {
		printError("Null similar list!");
	    }
	    //similarList.print();
	    if (predictorMode.equals("dumb")) {
		predictor = new DumbPredictorModule(challengeList, similarList);
	    } else if (predictorMode.matches("pro-energy|pro-energy-vlt")) {
		predictor = new ProEnergyPredictorModule(challengeList, similarList, weightingFactor, correlationFactor);
	    } else if (predictorMode.equals("ipro-energy")) {
		predictor = new IproEnergyPredictorModule(challengeList, similarList, weightingFactor);
	    } else if (predictorMode.matches("dwcma|udwcma")) {
		DataList factorLists[] = new DataList[2];
		factorLists[0] = ((AverageAnalyzerModule) analyzer).alphaWeightingFactorList();
		if (predictorMode.equals("dwcma")) {
		    predictor = new DwcmaPredictorModule(challengeList, similarList, factorLists[0], t, timeslotWindow);
		} else {
		    DataList udwcmaSimilarList = udwcmaAnalyzer.mostSimilarList(challengeList, t, timeslotWindow);
		    factorLists[1] = ((AverageAnalyzerModule) analyzer).betaWeightingFactorList();
		    predictor = new UDwcmaPredictorModule(challengeList, similarList, udwcmaSimilarList, factorLists, t, timeslotWindow);
		}
	    } else if (predictorMode.matches("saa|saa-sine")) {
		predictor = new SaaPredictorModule(challengeList, similarList, accurateSaaModel, seriesDegree);
	    } else if (predictorMode.equals("wep")) {
		predictor = new WepPredictorModule(challengeList, similarList, timeslotWindow);
	    }
	    int horizonTimeslot = t + predictionHorizon;
	    if (horizonTimeslot > finalTimeslot) {
		horizonTimeslot = finalTimeslot;
	    }
	    DataList predictions = predictor.getPredictions(t, horizonTimeslot, predictionStep);
	    //predictions.print();
	    for (int j = 1; j < predictions.size(); j++) {
		DataEntry entry = predictions.getEntryByIndex(j);
		predictionsList[j-1].addEntry(entry.getValue(), entry.getTimeslot());
	    }
	    if (accPredictions) {
		int accPredictionsIndex = (t - initialTimeslot) / timeslotStep;
		accPredictionsList[accPredictionsIndex] = new DataList(t + "-predictions.acc", null);
		for (int horizon = t + predictionStep; horizon <= t + predictionHorizon && horizon <= finalTimeslot; horizon += predictionStep) {
		    accPredictionsList[accPredictionsIndex].addEntry(predictions.getEnergyHarvested(t, horizon, powerFactor), horizon);
		}
		//accPredictionsList[accPredictionsIndex].print();
	    }
	}

	// Error analysis
	DataEntry challengeEntry, predictionEntry;
	double challengeValue, predictionValue;
	double absError, sumAbsError; // MAE
	double sumSquaredAbsError; // RMSE
	double perError, sumPerError; // MAPE
	int numMapeValues; // MAPE
	double mapeValueThreshold = 0.1 * challengeList.getMaxEntry().getValue(); // MAPE
	double sumChallengeValue; // MADP
	double sumDiffChallengeValue, prevChallengeValue; // MASE
	
	if (accPredictions) {
	    initChallengeList.addFile(challengePath, timeslotStep);
	    for (int i = 0; i < numAccList; i++) {
		int t = initialTimeslot + i * timeslotStep;
		accChallengesList[i] = new DataList(t + "-challenge.acc", null);
		for (int horizon = t + predictionStep; horizon <= t + predictionHorizon && horizon <= finalTimeslot; horizon += predictionStep) {
		    accChallengesList[i].addEntry(initChallengeList.getEnergyHarvested(t, horizon, powerFactor), horizon);
		}
		//accChallengesList[i].print();
	    }	    
	}

	for (int horizon = predictionStep; horizon <= predictionHorizon; horizon += predictionStep) {
	    int horizonIndex = horizon / predictionStep - 1;
	    int numValues = predictionsList[horizonIndex].size();
	    sumAbsError = sumSquaredAbsError = sumPerError = sumChallengeValue = sumDiffChallengeValue = 0.0;
	    numMapeValues = 0;
	    prevChallengeValue = -1.0;
	    for (int i = 0; i < numValues; i++) {
		if (accPredictions) {
		    challengeEntry = accChallengesList[i].getEntryByIndex(horizonIndex);
		    predictionEntry = accPredictionsList[i].getEntryByIndex(horizonIndex);
		    mapeValueThreshold = 0.1 * accChallengesList[i].getMaxEntry().getValue(); // MAPE
		} else {
		    predictionEntry = predictionsList[horizonIndex].getEntryByIndex(i);
		    challengeEntry = challengeList.getEntryByTimeslot(predictionEntry.getTimeslot());
		}
		predictionValue = predictionEntry.getValue();
		challengeValue = challengeEntry.getValue();
		sumChallengeValue += challengeValue;
		absError = Math.abs(challengeValue - predictionValue);
		sumAbsError += absError;
		sumSquaredAbsError += absError * absError;
		perError = challengeValue > 0 ? absError * 100 / challengeValue : 0;
		if (challengeValue > mapeValueThreshold) {
		    sumPerError += perError;
		    numMapeValues++;
		}
		if (prevChallengeValue >= 0) {
		    sumDiffChallengeValue += Math.abs(challengeValue - prevChallengeValue);
		}
		prevChallengeValue = challengeValue;
		System.out.printf("Horizon: %d Timeslot: %d Actual: %.3f Predicted: %.3f AbsError: %.3f PerError: %.3f %n",
				  horizon, predictionEntry.getTimeslot(), challengeValue, predictionValue, absError, perError);
	    }
	    System.out.printf("Horizon: %d MAE: %.3f MAPE: %.3f RMSE: %.3f MASE: %.3f MADP: %.3f %n%n",
			      horizon, sumAbsError / numValues, sumPerError / numMapeValues,
			      Math.sqrt(sumSquaredAbsError / numValues),
			      sumAbsError * (numValues - 1) / numValues / sumDiffChallengeValue,
			      sumAbsError * 100 / sumChallengeValue);
	}
    }
}
