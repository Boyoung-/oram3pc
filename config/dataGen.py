import sys

dLen = int(sys.argv[1])
dNum = int(sys.argv[2])
data = 0x00
out = ""
for i in range(0, dNum):
	out = out + str(data) + ("F" * (dLen - 1))
	data = (data + 1) % 255
print(out)

