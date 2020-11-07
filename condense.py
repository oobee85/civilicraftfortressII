
import json

def readFile(filename):
    with open(filename) as f:
        data = json.load(f)
        return data


units = readFile("./src/resources/costs/UnitType.json")
units = units['unittypes']

with open('lootlist.txt', 'w') as f:
    for asdf in units:
        if 'loot' in asdf:
            string = str(asdf['name']) +  ' drops ' + str(asdf['loot']) + '\n'
            f.write(string)
