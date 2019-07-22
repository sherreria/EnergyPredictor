# EnergyPredictor
Java program that predicts future energy availability using different energy models.

# Invocation
java EnergyPredictor FILE [-solar] [-energy] [-acc]

- FILE is the configuration file. See file.config for more information.
- Use -solar option if working with solar energy/power traces.
- Use -energy option if working with energy traces. Ignore it when using power traces.
- Use -acc option if accumulated predictions are required.

# Output
The simulator outputs the available energy predicted for the selected timeslots and common error measures (MAE, RMSE, MADP, MAPE, MASE).

# Legal
Copyright ⓒ Sergio Herrería Alonso <sha@det.uvigo.es> 2019

This simulator is licensed under the GNU General Public License, version 3 (GPL-3.0). For more information see LICENSE.
