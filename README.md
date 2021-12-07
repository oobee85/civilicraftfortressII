# Settlers of Might and Magic

System Requirements:
- Minimum supported JDK: 8
- Maximum supported JDK: 15 (16 might be okay if you use VM argument `--illegal-access=permit`)

Dependencies:
- Included in the repo:
	- JSON Release jar from: [github.com/stleary/JSON-java](https://github.com/stleary/JSON-java)  
	- jply.jar built from: [github.com/0leks/jPLY](https://github.com/0leks/jPLY) 
		- (The 0leks fork includes a bug-fix for parsing ints)
- Not included in the repo:
	- download the jogamp-all-platforms release from [jogamp.org](https://jogamp.org/) and add jogamp-all-platforms/jar/gluegen-rt.jar and jogamp-all-platforms/jar/jogl-all.jar to the classpath
