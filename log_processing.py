import os

def find_files(filename, search_path):
   result = []

# Wlaking top-down from the root
   for root, dir, files in os.walk(search_path):
      if filename in files:
         result.append(os.path.join(root, filename))
   return result

path = input("enter the path name for the log files: ")
copy = find_files("log.txt", path)


avgTS = 0
avgTJ = 0
avgQ = 0 
counter = 0
with open(copy[0], "r") as a_file:
  for line in a_file: 
    stripped_line = line.strip().split()
    avgQ += (int(stripped_line[0]) + int(stripped_line[1]))
    avgTS += int(stripped_line[0])
    avgTJ += int(stripped_line[1])
    counter += 1
    
avgTJ = float(avgTJ/counter)
avgQ = float(avgQ/counter)
avgTS = float(avgTS/counter)

print("AVERAGE TJ:", avgTJ/1000000, "ms")
print("AVERAGE Q:", avgQ/1000000, "ms")
print("AVERAGE TS:", avgTS/1000000, "ms")