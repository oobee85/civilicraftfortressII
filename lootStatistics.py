
import json
import matplotlib.pyplot as plt

def readFile(filename):
    with open(filename) as f:
        data = json.load(f)
        return data

def computePower(unit):
    stats = unit['stats']
    attackspeed = stats['attackspeed'] if stats['attackspeed'] > 0 else 1
    healspeed = stats['healspeed'] if stats['healspeed'] > 0 else 1
    effectiveHealth = stats['health'] + stats['health']/healspeed
    power = float(effectiveHealth) * stats['attack'] / attackspeed
    return power

def barChart(x, labels, xaxistitle, yaxistitle, title=''):
    x, labels = zip(*sorted(zip(x, labels)))

    plt.style.use('ggplot')
    x_pos = [i for i, _ in enumerate(labels)]
    plt.barh(x_pos, x, color='green')
    plt.xlabel(xaxistitle)
    plt.ylabel(yaxistitle)
    plt.title(title)

    plt.yticks(x_pos, labels)

    plt.show()


units = readFile("./src/resources/costs/UnitType.json")
units = units['unittypes']
units = [unit for unit in units if unit['name'] != 'TWIG']

lootDictionary = {}

with open('lootStatistics.txt', 'w') as f:
    for asdf in units:
        if 'loot' in asdf:
            for loottype in asdf['loot']:
                element = {asdf['name'] : asdf['loot'][loottype]}
                if not loottype in lootDictionary:
                    lootDictionary[loottype] = {}
                lootDictionary[loottype][asdf['name']] = asdf['loot'][loottype]

            string = str(asdf['name']) +  ': ' + str(asdf['loot'])
            f.write(string + '\n')
            print(string)

    f.write('\n\n')
    for item in lootDictionary:
        line = str(item) + ': ' + str(lootDictionary[item])
        f.write(line + '\n')
        print(line)



for asdf in units:
    power = computePower(asdf)
    print(str(asdf['name']) + ': ' + str(power))


barChart([computePower(asdf) for asdf in units], [asdf['name'] for asdf in units], 'power', 'unit', 'ehp * damage / attspeed')
