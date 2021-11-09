from datetime import datetime
import batchBuilderSupport as bbs
import os

# ----------------------------
#       Initialization
# -----------------------------

# constants
LAST_RUN_DATE_FILE_NAME = "last_run_date.txt"
TESTING_INFO_FILE_NAME = "TESTING_INFO.txt"
VERSION = 5.2

# get testConfig.config setting
config_file = open(os.getcwd() + os.sep + "testConfig.config", "r")
config_vars = bbs.get_env_vals(config_file)
config_file.close()

# last_run_date.txt contents
test_src_dir = config_vars["test_src"]
last_run_date_file = open(test_src_dir + os.sep + LAST_RUN_DATE_FILE_NAME, "r")
last_run_date_vars = bbs.get_env_vals(last_run_date_file)
last_run_date_file.close()

# TESTING_INFO.txt contents
testing_space_dir = config_vars["TEST_ENVS"]
testing_info_file = open(testing_space_dir + os.sep + TESTING_INFO_FILE_NAME, "r")
testing_info_vars = bbs.get_env_vals(testing_info_file)
testing_info_file.close()

# initialize log file
log_time = datetime.utcnow()
log_file = open(log_time.strftime(test_src_dir + os.sep + "regressive_testing_%Y%m%d%H%M%S.log"), "w")

# Variables that are needed later
last_run_time = last_run_date_vars["LAST_RUN_DATE"]
last_run_version = float(last_run_date_vars["VERSION"])
test_env_version = float(testing_info_vars["VERSION"])

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

    f = open(test_src_dir + os.sep + LAST_RUN_DATE_FILE_NAME, "w")
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
                if GEOMETRY_PREP not in envs:
                    envs.append(GEOMETRY_PREP)
            elif file in GEOMETRY_REPAIR:
                if GEOMETRY_REPAIR not in envs:
                    envs.append(GEOMETRY_REPAIR)
            elif file in MESH_PREP:
                if MESH_PREP not in envs:
                    envs.append(MESH_PREP)
            elif file in MESH:
                if MESH not in envs:
                    envs.append(MESH)
            elif file in MESH_REPAIR:
                if MESH_PREP not in envs:
                    envs.append(MESH_REPAIR)
            elif file in INITIAL_EXECUTION:
                if INITIAL_EXECUTION not in envs:
                    if file == "run.java":
                        # if run.java got changed, run both initial_execution and late_stage_execution envs
                        envs.append(LATE_STAGE_EXECUTION)
                    else:
                        envs.append(INITIAL_EXECUTION)
            elif file in LATE_STAGE_EXECUTION:
                if LATE_STAGE_EXECUTION not in envs:
                    envs.append(LATE_STAGE_EXECUTION)
            elif file in POST_PROC:
                if POST_PROC not in envs:
                    envs.append(POST_PROC)
            else:
                fatal_error("File " + file + " not found in testing environments")
    else:
        write_log("Version change detected, all cases will be ran")
        envs = [GEOMETRY_PREP, GEOMETRY_REPAIR, MESH_PREP, MESH, MESH_REPAIR, INITIAL_EXECUTION, LATE_STAGE_EXECUTION,
                POST_PROC]

    # logging test environments
    write_log("These environments will be ran: ")
    for env in envs:
        if env == GEOMETRY_PREP:
            write_log("GEOMETRY_PREP")
        elif env == GEOMETRY_REPAIR:
            write_log("GEOMETRY_REPAIR")
        elif env == MESH_PREP:
            write_log("MESH_PREP")
        elif env == MESH:
            write_log("MESH")
        elif env == MESH_REPAIR:
            write_log("MESH_REPAIR")
        elif env == INITIAL_EXECUTION:
            write_log("INITIAL_EXECUTION")
        elif env == LATE_STAGE_EXECUTION:
            write_log("LATE_STAGE_EXECUTION")
        elif env == POST_PROC:
            write_log("POST_PROC")
        else:
            fatal_error("Something weird has happened with testing environments")
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


# ----------------------------
#       Execution
# -----------------------------

version_check()
files_changed = get_files_changed()
test_envs = get_test_env(files_changed)

# exit the program
log_run_date()  # only log run date if the program finished executing seems to make sense, we will see
write_log("Regressive testing complete...exiting")
exit(0)
