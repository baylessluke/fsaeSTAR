import os


def posix_write_flag(header, value, file):
    writestr = ("export " + str(header) + "=" + '"' + str(value) + '"' + '\n').encode()
    file.write(writestr)


def posix_write_command(file):
    command_file = open("linux_command.txt")
    writestr = command_file.read()
    writestr = writestr.encode()
    file.write(writestr)
    command_file.close()


def posix_write_blanks(file, blankcount):
    for i in range(0, blankcount):
        file.write('\n'.encode())
        i += 1


def get_file_list(path):
    file_list = os.listdir(path)
    sim_list = []
    for f in file_list:
        if f.endswith(".sim"):
            sim_list.append(f)
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

    return var_dict


def individuals(file_list, config_file, command):
    output_files = []
    for x in file_list:
        output_file_name = x + "_script.sh"
        output_files.append(output_file_name)
        output_file = open(output_file_name, "wb")
        config_list = get_env_vals(config_file)
        for key, val in config_list.items():
            posix_write_flag(key, val, output_file)
        posix_write_flag("FILENAME", x, output_file)
        posix_write_flag("newName", x, output_file)
        output_file.write(command.encode())
        output_file.close()
    return output_files


def clumped(file_list, config_file, command):
    output_file_name = "clumped_run.sh"
    output_file = open(output_file_name, "wb")
    config_list = get_env_vals(config_file)
    for key, val in config_list.items():
        posix_write_flag(key, val, output_file)
    for x in file_list:
        posix_write_flag("FILENAME", x, output_file)
        posix_write_flag("newName", x, output_file)
        output_file.write(command.encode())
        posix_write_blanks(output_file, 2)
    output_file.close()
    return output_file_name


def generatecommand(config_list):
    command = ""
    command = command + '"' + "$STARLOC" + '"'
    command = command + " -licpath " + "$LICPATH" + " -collab "
    command = command + " -classpath " + '"' + "$CP" + '" '
    command = command + '"' + "$SIMPATH" + os.sep + "$FILENAME" + '"'
    if 'PROCS' in config_list:
        if float(config_list['PROCS']) != 1:
            command = command + " -np " + "$PROCS"
    if 'PODKEY' in config_list and "cd-adapco" in config_list['LICPATH']:
        command = command + " -power -podkey " + "$PODKEY"
    command = command + " -batch " + '"' + "$CP" + os.sep + "$MACRO" + '"'
    if config_list['CLUSTER'] != "LOCAL":
        command = command + " -machinefile $PBS_NODEFILE"

    command = command + " -hardwarebatch"
    command = command + " -batch-report"
    return command


def parseWalltime(walltime):

    walltimeArr = walltime.split(":")
    totalTime = float(walltimeArr[0]) * 3600 + float(walltimeArr[1]) * 60 + float(walltimeArr[2])

    return totalTime


def generateqsub(config_list):
    if config_list['CLUSTER'] != "LOCAL":
        qsub = 'sbatch --partition=$CLUSTER --ntasks=$PROCS --time=$WALLTIME'
    else:
        qsub = "sh ./"

    return qsub
