from datetime import datetime

# ----------------------------
#       Initialization
# -----------------------------

log_time = datetime.utcnow()
log_file = open(log_time.strftime("regressive_testing_%Y%m%d%h%m%s.txt"), "w")

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
    "run.java"
]

POST_PROC = [
    "ExportReports.java",
    "PostProc.java"
]


# ----------------------------
#       Functions
# -----------------------------


def write_log(message, file=log_file):
    file.write(message)


def fatal_error(message, file=log_file):
    write_log("Fatal error: " + message + "...exiting")
    exit(1)


def log_run_date():
    # update the log file that records when the regression suite was last ran
    f = open("last_run_date.txt", "w")
    now = datetime.utcnow()
    f.write(now.strftime("%Y-%m-%dT%H:%M:%SZ"))
    f.close()


def det_test_env(file_list):
    # takes in a list of java file that has been changed and determin what tests to run
    test_envs = []
    for file in file_list:
        if file in GEOMETRY_PREP and GEOMETRY_PREP not in test_envs:
            test_envs.append(GEOMETRY_PREP)
        elif file in GEOMETRY_REPAIR and GEOMETRY_REPAIR not in test_envs:
            test_envs.append(GEOMETRY_REPAIR)
        elif file in MESH_PREP and MESH_PREP not in test_envs:
            test_envs.append(MESH_PREP)
        elif file in MESH and MESH not in test_envs:
            test_envs.append(MESH)
        elif file in MESH_REPAIR and MESH_PREP not in test_envs:
            test_envs.append(MESH_REPAIR)
        elif file in INITIAL_EXECUTION and INITIAL_EXECUTION not in test_envs:
            test_envs.append(INITIAL_EXECUTION)
        elif file in LATE_STAGE_EXECUTION and LATE_STAGE_EXECUTION not in test_envs:
            test_envs.append(LATE_STAGE_EXECUTION)
        elif file in POST_PROC and POST_PROC not in test_envs:
            test_envs.append(POST_PROC)
        else:
            fatal_error("File changed not found in testing environments")
    return test_envs


# ----------------------------
#       Execution
# -----------------------------

# exit the program
write_log("Regressive testing complete...exiting")
exit(0)
