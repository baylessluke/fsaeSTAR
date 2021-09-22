'''Applies a macro on a set of files in a given folder'''

import batchBuilderSupport as bbs
import os
import sys

path = r"E:\2022 Simulations"
sizeLimit = 400e6
fileList = bbs.get_file_list(path, sizeLimit)
starPath = r'"D:\Software\STAR\16.04.012-R8\STAR-CCM+16.04.012-R8\star\lib\win64\intel20.1vc14.2-r8\lib\starccm+.exe"'
macroPath = [r"C:\Users\Raunaq Kumaran\Documents\GitHub\fsaeSTAR\src\universalMeshClear.java"]
classPath = r"C:\Users\Raunaq Kumaran\Documents\GitHub\fsaeSTAR\src"
outputFile = "deleteEverything.bat"
output = open(outputFile, "w")
for x in fileList:
    print("Writing line for ", x)
    for y in macroPath:
        cmdString = starPath + " -power -podkey " + "Raxpoq5iIFqgy7DwEH4HrA" + " -classpath " + bbs.add_quotes(classPath) + " -batch " + bbs.add_quotes(y) + " " + bbs.add_quotes(x)
        output.write(cmdString)
        output.write(os.linesep)
output.close()
