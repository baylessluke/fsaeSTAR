# This folder is the master folder for regressive testing. It has git integration to detect
# which fsaeSTAR src files have been changed since last time the test was ran. The program
# makes a decisions on which test environments to run based on the changed files. The changed
# files are copied to test_space which will be executed.

from datetime import datetime
from shutil import copyfile
import batchBuilderSupport as bbs
import os

# ----------------------------
#       Initialization
# -----------------------------

# constants
LAST_RUN_DATE_FILE_NAME = "last_run_date.txt"
TESTING_INFO_FILE_NAME = "TESTING_INFO.txt"
VERSION = 5.2
TEST_CONFIG_NAME = "testConfig.test"
config_file = open(os.getcwd() + os.sep + TEST_CONFIG_NAME, "r")
CONFIG_VARS = bbs.get_env_vals(config_file)
config_file.close()
TESTING_SPACE_DIR = CONFIG_VARS["TEST_ENVS"]

# initialize log file
log_time = datetime.utcnow()
log_file = open(log_time.strftime(TESTING_SPACE_DIR + os.sep + "regressive_testing_%Y%m%d%H%M%S.log"), "w")

# test environments
GEOMETRY_PREP = [
    "DomainSet.java",
    "RideHeight.java",
    "RollSet.java",
    "Steering.java"
]

GEOMETRY_REPAIR = [
    "SurfaceWrap.java"
]

MESH_PREP = [
    "Subtract.java",
    "Regions.java"
]

MESH = [
    "AutoMesh.java"
]

MESH_REPAIR = [
    "MeshRepair.java"
]

INITIAL_EXECUTION = [
    "yawSet.java",
    "GenReports.java",
    "SoftRun.java",
    "MeshRepair.java",
    "run.java"
]

LATE_STAGE_EXECUTION = [
    "run.java",
    "ConvergenceChecker.java"
]

POST_PROC = [
    "ExportReports.java",
    "PostProc.java"
]


# ----------------------------
#       Functions
# -----------------------------


def write_log(message="", extra_blank_line=False, file=log_file):
    # TO-DO: scratch testing folder integration

    file.write(message + "\n")
    if extra_blank_line:
        file.write("\n")


def fatal_error(message):
    write_log("Fatal error: " + message + "...exiting")
    exit(1)


def log_run_date():
    # update the log file that records when the regression suite was last ran and the version of basesim it ran on

    write_log("Documenting when this program has been ran...")

    f = open(TESTING_SPACE_DIR + os.sep + LAST_RUN_DATE_FILE_NAME, "w")
    now = datetime.utcnow()
    f.write(now.strftime("LAST_RUN_DATE = %Y-%m-%dT%H:%M:%SZ;\n"))
    f.write("VERSION = " + str(VERSION) + ";")
    f.close()

    write_log("Logged time: " + now.strftime("%Y-%m-%dT%H:%M:%SZ"))
    write_log("Logged version: " + str(VERSION), True)


def get_test_env(file_list):
    # takes in a list of java file that has been changed and determine what tests to run

    write_log("Determining test environments to run...")

    envs = []
    if test_env_version == last_run_version:
        for file in file_list:
            if file in GEOMETRY_PREP:
                if "GEOMETRY_PREP" not in envs:
                    envs.append("GEOMETRY_PREP")
            elif file in GEOMETRY_REPAIR:
                if "GEOMETRY_REPAIR" not in envs:
                    envs.append("GEOMETRY_REPAIR")
            elif file in MESH_PREP:
                if "MESH_PREP" not in envs:
                    envs.append("MESH_PREP")
            elif file in MESH:
                if "MESH" not in envs:
                    envs.append("MESH")
            elif file in MESH_REPAIR:
                if "MESH_REPAIR" not in envs:
                    envs.append("MESH_REPAIR")
            elif file in INITIAL_EXECUTION:
                if "INITIAL_EXECUTION" not in envs:
                    if file == "run.java":
                        # if run.java got changed, run both initial_execution and late_stage_execution envs
                        envs.append("LATE_STAGE_EXECUTION")
                    else:
                        envs.append("INITIAL_EXECUTION")
            elif file in LATE_STAGE_EXECUTION:
                if "LATE_STAGE_EXECUTION" not in envs:
                    envs.append("LATE_STAGE_EXECUTION")
            elif file in POST_PROC:
                if "POST_PROC" not in envs:
                    envs.append("POST_PROC")
            else:
                fatal_error("File " + file + " not found in testing environments")
    else:
        write_log("Version change detected, all cases will be ran")
        envs = ["GEOMETRY_PREP", "GEOMETRY_REPAIR", "MESH_PREP", "MESH", "MESH_REPAIR", "INITIAL_EXECUTION",
                "LATE_STAGE_EXECUTION", "POST_PROC"]

    # logging test environments
    write_log("These environments will be ran: ")
    for env in envs:
        write_log(env)
    write_log("")

    return envs


def get_files_changed():
    # TO-DO: actually write it (git integration). This is just for testing for now

    write_log("Detecting files changed...")

    files = ["run.java", "ExportReports.java", "MeshRepair.java", "ExportReports.java", "PostProc.java"]

    # logging files changed
    write_log("Files changes detected:")
    for file in files:
        write_log(file)
    write_log("")

    return files


def version_check():
    # check the version of testing suite with testing environments, if they don't match, kill the test
    write_log("Checking version...")

    if VERSION == test_env_version:
        write_log("Passed")
        write_log("")
    else:
        fatal_error("Version of the testing suite and testing environment do not match, killing test")


def copy_to_testing_space(envs):
    # copy all the testing environments to testing_space
    write_log("Copying files to Testing_Space...")

    for env in envs:
        sim_name = env + ".sim"
        origin_file = TESTING_SPACE_DIR + os.sep + sim_name
        destination_file = TESTING_SPACE_DIR + os.sep + "Testing_Space" + os.sep + sim_name
        copyfile(origin_file, destination_file)
        write_log(sim_name + " copied successfully.")

    write_log("All test environments copied.", True)


def edit_test_config(name, envs):
    # edit the test config to set appropriate test envs to true
    write_log("Editing test config...")

    file = open(name, "a")
    file.write("\nTEST_ENVS = ")
    env_string = "";
    for env in envs:
        env_string = env_string + env + ","
    env_string = env_string[:-1]
    file.write(env_string)
    file.write(";")

    file.close()
    write_log("Done.", True)


def restore_test_config():
    # restore test config to before edit state

    write_log("Restoring test config file...")

    # read all lines except for the one added by this script
    file = open(os.getcwd() + os.sep + TEST_CONFIG_NAME, "r")
    lines = file.readlines()
    lines = lines[:-1]
    file.close()

    # overwrite the file without the last line
    file = open(os.getcwd() + os.sep + TEST_CONFIG_NAME, "w")
    for line in lines:
        file.write(line)
    file.close()

    write_log("Done.", True)


# -----------------------------
#       Execution
# -----------------------------

# TESTING_INFO.txt contents
try:
    testing_info_file = open(TESTING_SPACE_DIR + os.sep + TESTING_INFO_FILE_NAME, "r")
    testing_info_vars = bbs.get_env_vals(testing_info_file)
    testing_info_file.close()
    test_env_version = float(testing_info_vars["VERSION"])  # test environment version
except FileExistsError:
    fatal_error("Testing space directory does not exist")

# last_run_date.txt contents
last_run_date_path = TESTING_SPACE_DIR + os.sep + LAST_RUN_DATE_FILE_NAME
last_run_date_file = ""
last_run_date_vars = ""
if os.path.isfile(last_run_date_path):
    last_run_date_file = open(last_run_date_path, "r")
    last_run_date_vars = bbs.get_env_vals(last_run_date_file)
    last_run_date_file.close()
else:
    # is the file doesn't exist, just give it date that's in the past and a version so ancient that no one remembers
    last_run_date_file = open(last_run_date_path, "w+")
    last_run_date_file.write("LAST_RUN_DATE = 2021-11-08T16:20:42Z;\n")
    last_run_date_file.write("VERSION = 1.0;")
    last_run_date_vars = bbs.get_env_vals(last_run_date_file)
    last_run_date_file.close()

# Last run info
last_run_time = last_run_date_vars["LAST_RUN_DATE"]
last_run_version = float(last_run_date_vars["VERSION"])

# Execution
version_check()
files_changed = get_files_changed()
test_envs = get_test_env(files_changed)
copy_to_testing_space(test_envs)
edit_test_config(TEST_CONFIG_NAME, test_envs)
# the following things will be kinda dumb but i will try to explain it...
# since I'm lazy and want to just borrow folderBuilder.py, everything has to be setup in a way that
# makes folderBuilder.py happy. But I also need custom settings for the regressive check. So the
# way I'm doing this is having a config file for regressive testing (testConfig.test), rename
# linuxConfig.config to linuxConfig.temp, rename testConfig.text to linuxConfig.config, submit the
# job, and rename everything back and pretend nothing happened...Yeah, not ideal
linux_config_old_dir = "linuxConfig.config"
linux_config_new_dir = "linuxConfig.temp"
test_config_old_dir = TEST_CONFIG_NAME
test_config_new_dir = linux_config_old_dir  # yeah i know it's useless, but it makes the code slightly easier to read
os.rename(linux_config_old_dir, linux_config_new_dir)
os.rename(test_config_old_dir, test_config_new_dir)
exec(open("test.py").read())  # queueing sims
os.rename(test_config_new_dir, test_config_old_dir)
os.rename(linux_config_new_dir, linux_config_old_dir)
restore_test_config()
# remember to delete last line in testConfig once done.


# exit the program
log_run_date()  # only log run date if the program finished executing seems to make sense, we will see
write_log("Handing off to STAR macros...exiting")
exit(0)
