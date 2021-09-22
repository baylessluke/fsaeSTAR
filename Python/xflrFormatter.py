import sys
import shutil
import math
import numpy


def split_line_to_double_line(split_line):
    new_arr = []
    for x in split_line:
        try:
            x_val = float(x)
            new_arr.append(x_val)
        except ValueError:
            return None
    return new_arr

def rotate_list(xy_list, rotation_matrix):
    v = numpy.matrix(xy_list)
    v = v.transpose()
    vp = rotation_matrix * v
    vp = vp.tolist()
    vp = [x[0] for x in vp]
    return vp

argv_length = len(sys.argv)

# set defaults here. alpha in degrees.
filepath = r"C:\Users\rauna\OneDrive\Formula SAE\2022\NX_Open_Trials\FW_F_1-1.dat" if argv_length <= 1 else sys.argv[1]
alpha = 5 if argv_length <= 2 else float(sys.argv[2])
chord = 10 if argv_length <= 3 else float(sys.argv[3])
y_offset = 4 if argv_length <= 4 else float(sys.argv[4])
alpha = math.radians(alpha)
rot_mat = numpy.matrix([[math.cos(alpha), -math.sin(alpha)], [math.sin(alpha), math.cos(alpha)]])

tempfilepath = "~tempxflr.dat"

fin = open(filepath, "r")
fout = open(tempfilepath, "w")

for line in fin.readlines():
    line_split = line.split()
    numeric_split = split_line_to_double_line(line_split)
    if numeric_split is None:
        continue
    scaled_split = [x * chord for x in numeric_split]
    rotated_split = rotate_list(scaled_split, rot_mat)
    xyz = [rotated_split[0], y_offset, rotated_split[1]]
    xyz = [str(x) for x in xyz]
    joined_string = ",".join(xyz)
    fout.write(joined_string + "\n")

fin.close()
fout.close()
shutil.move(tempfilepath, filepath)