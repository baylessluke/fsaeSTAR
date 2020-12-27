import os
import sys
import datetime

GPU_CORES = 16
CPU_CORES = 20

def get_timestamp():
    timeStamp = datetime.datetime.now()
    timeStampString = timeStamp.strftime("%Y%m%d_%H_%M_%S")
    return timeStampString


def posix_write_flag(header, value, file):
    writestr = ("export " + str(header) + "=" + '"' + str(value) + '"' + '\n').encode()
    file.write(writestr)


def posix_write_blanks(file, blankcount):
    for i in range(0, blankcount):
        file.write('\n'.encode())
        i += 1


def get_file_list(path, size=0):
    sim_list = []
    file_list = os.listdir(path)
    for f in file_list:
        filePath = path + os.sep + f
        if os.path.isdir(filePath):
            subList = get_file_list(filePath, size)
            for y in subList:
                sim_list.append(y)
        if f.endswith(".sim"):
            fileSize = os.path.getsize(filePath)
            if fileSize >= size:
                sim_list.append(filePath)
    return sim_list


def get_config_var(var, file):
    file.seek(0)
    lines = file.read()
    lines = lines.split(";")
    for line in lines:
        line = line.strip()
        if line.startswith(var):
            split_line = line.split("=")
            val = split_line[1].strip()
            return val
    return ""


def get_env_vals(file):
    var_dict = dict()
    file.seek(0)
    lines = file.read()
    lines = lines.split(";")

    for line in lines:
        line = line.strip()
        if line is "":
            break
        val = line.split("=")[0].strip()
        var_dict[val] = get_config_var(val, file)

    if "CLUSTER" in var_dict.keys():
        if "WALLTIME" not in var_dict.keys():
            if var_dict["CLUSTER"] == "gpu" or var_dict["CLUSTER"] == "scholar":
                var_dict["WALLTIME"] = 240
            elif var_dict["CLUSTER"] == "long":
                var_dict["WALLTIME"] = 4310
    return var_dict


def individuals(file_list, config_file, command):
    output_files = []
    time_stamp_string = get_timestamp()
    for x in file_list:
        output_file_name = x + "_" + time_stamp_string + "_script.sh"
        output_files.append(output_file_name)
        output_file = open(output_file_name, "wb")
        output_file.write("#!/bin/sh\n".encode())
        config_list = get_env_vals(config_file)
        for key, val in config_list.items():
            posix_write_flag(key, val, output_file)
        posix_write_flag("FILENAME", x, output_file)
        output_file.write(command.encode())
        output_file.close()
    return output_files


def clumped(file_list, config_list, command):
    time_stamp_string = get_timestamp()
    output_file_name = time_stamp_string + "_" + "clumped_run.sh"
    output_file = open(output_file_name, "wb")
    output_file.write("#!/bin/sh\n".encode())
    i = 0
    for key, val in config_list.items():
        posix_write_flag(key, val, output_file)
    posix_write_blanks(output_file, 2)
    for x in file_list:
        output_file.write("(".encode())
        posix_write_flag("FILENAME", x, output_file)
        output_file.write(command.encode())
        output_file.write(") &".encode())
        i += 1
        if i % int(config_list['JOBS_IN_PARALLEL']) == 0:
            output_file.write(" wait\n".encode())
        posix_write_blanks(output_file, 2)

    output_file.close()
    return output_file_name


def generatecommand(config_list):
    command = '"' + "$STARLOC" + '"'
    command = command + " -licpath " + "$LICPATH" + " -collab "
    command = command + " -classpath " + '"' + "$CP" + '" '
    command = command + '"' + "$FILENAME" + '"'
    if 'PROCS' in config_list:
        if float(config_list['PROCS']) != 1:
            command = command + " -np " + "$JOB_PROCS"
    if 'PODKEY' in config_list and "cd-adapco" in config_list['LICPATH']:
        command = command + " -power -podkey " + "$PODKEY"
    command = command + " -batch " + '"' + "$CP" + os.sep + "$MACRO" + '"'
    if config_list['CLUSTER'] != "LOCAL":
        command = command

    command += " -hardwarebatch -machinefile nodefile.$SLURM_JOB_ID"
    command += " -batch-report"
    if config_list['CLUSTER'] == "gpu":
        command += " -rgraphics egl"
    command = command + "| tee " + '"${FILENAME}' + "_" + "${SLURM_JOB_ID}" + "_" + get_timestamp() + '.txt"'
    return command


def parseWalltime(walltime):
    walltimeArr = walltime.split(":")
    totalTime = float(walltimeArr[0]) * 3600 + float(walltimeArr[1]) * 60 + float(walltimeArr[2])
    return totalTime


def generateqsub(config_list):
    if config_list['CLUSTER'] != "LOCAL":
        qsub = 'sbatch -A $CLUSTER --ntasks=$PROCS --time=$WALLTIME --exclusive '
        if len(sys.argv) > 1:
            qsub += ' --dependency=afterany'
            for i in range(1, len(sys.argv)):
                qsub = qsub + ':' + sys.argv[i]
            qsub += ' '
        if config_list['CLUSTER'] == "gpu":
            qsub += ' --gres=gpu:1 '
    else:
        qsub = "sh ./"

    return qsub
