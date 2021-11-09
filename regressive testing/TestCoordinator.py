from git import Git
from git import Repo
from datetime import datetime

''' temporarily disabled for testing
# update the log file that records when the regression suite was last ran
f = open("last_run_date.txt", "w")
now = datetime.utcnow()
f.write(now.strftime("%Y-%m-%dT%H:%M:%SZ"))
f.close()
'''

# get git commits from last date
repo = Repo()
g = Git(self.repo_directory)
