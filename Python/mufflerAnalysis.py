import pandas
import os
import matplotlib.pyplot as plt

data_path = r"D:\OneDrive\Formula SAE\2022\Muffler CFD\Straight_Pipe_Data"
output_file = r"D:\OneDrive\Formula SAE\2022\Muffler CFD\Straight_Pipe_Data\RPM_Data.xlsx"
string_contains = "avg exhaust.text"
outlet_plot = r"D:\OneDrive\Formula SAE\2022\Muffler CFD\Straight_Pipe_Data\outlet_plot.png"
rpm_data = pandas.DataFrame(columns=["RPM", "Average Exhaust Mdot (kg/s)"])

for filename in os.listdir(data_path):
    if string_contains not in filename:
        continue
    file_split = filename.split()
    rpm_val = float(file_split[0])
    mdot = None
    with open(data_path + os.sep + filename) as data_file:
        for lines in data_file:
            line = lines.strip()
            if line.startswith("Computed Value:"):
                line_split = line.split(":")
                mdot = line_split[1].split()[0]
                mdot = mdot.strip()
                mdot = float(mdot)
    if mdot is None:
        raise ValueError("Couldn't figure out mdot!")

    rpm_data.loc[len(rpm_data)] = [rpm_val, mdot]

rpm_data = rpm_data.astype(float)
rpm_data = rpm_data.sort_values(rpm_data.columns[0])
rpm_data.to_excel(output_file, index=False)
plt.figure("Mdot vs RPM")
rpm_data.set_index(rpm_data.columns[0]).plot()
plt.title("Mdot")
plt.xlabel("RPM")
plt.ylabel("Exhaust Average Mdot (kg/s)")
plt.savefig(outlet_plot)