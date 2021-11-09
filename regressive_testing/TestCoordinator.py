from datetime import datetime

# ----------------------------
#       Initialization
# -----------------------------

# version of basesim this regression test is written for
VERSION = 5.2

# initialize log file
log_time = datetime.utcnow()
log_file = open(log_time.strftime("regressive_testing_%Y%m%d%H%M%S.txt"), "w")

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
    file.write(message + "\n")
    if extra_blank_line:
        file.write("\n")


def fatal_error(message, file=log_file):
    write_log("Fatal error: " + message + "...exiting")
    exit(1)


def log_run_date():
    # update the log file that records when the regression suite was last ran and the version of basesim it ran on

    write_log("Documenting when this program has been ran...")

    f = open("last_run_date.txt", "w")
    now = datetime.utcnow()
    f.write(now.strftime("%Y-%m-%dT%H:%M:%SZ\n"))
    f.write("VERSION = " + str(VERSION))
    f.close()

    write_log("Logged time: " + now.strftime("%Y-%m-%dT%H:%M:%SZ"))
    write_log("Logged version: " + str(VERSION), True)


def get_test_env(file_list):
    # takes in a list of java file that has been changed and determin what tests to run
    # TO-DO: add integration for complete test

    write_log("Determining test environments to run...")

    envs = []
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
    write_log(extra_blank_line=True)

    return envs


def get_files_changed():
    # TO-DO: actually write it (git integration). This is just for testing for now

    write_log("Detecting files changed...")

    files = ["run.java", "ExportReports.java", "MeshRepair.java", "ExportReports.java", "PostProc.java"]

    # logging files changed
    write_log("Files changes detected:")
    for file in files:
        write_log(file)
    write_log(extra_blank_line=True)

    return files


# ----------------------------
#       Execution
# -----------------------------

files_changed = get_files_changed()
test_envs = get_test_env(files_changed)

# exit the program
log_run_date()  # only log run date if the program finished executing seems to make sense, we will see
write_log("Regressive testing complete...exiting")
exit(0)
