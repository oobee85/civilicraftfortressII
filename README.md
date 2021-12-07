# Settlers of Might and Magic

## Playtesters
Download and run client.jar from [https://github.com/oobee85/civilicraftfortressII/releases](https://github.com/oobee85/civilicraftfortressII/releases)
(or server.jar if trying to host server to play over the interwebs)

## Developers
Feel free to explore the code base and make contributions however you see fit but be aware that documentation is slim and sparse.

### System Requirements:
- Minimum supported JDK: 8
- Maximum supported JDK: 15 (16 might be okay if you use VM argument `--illegal-access=permit`)

### Dependencies
- Included in the repo in /lib:
	- [json.jar](https://github.com/stleary/JSON-java)
	- [jPLY.jar](https://github.com/0leks/jPLY/releases/download/1/jply.jar)
- Not included in the repo:
	- Download the jogamp-all-platforms release from [jogamp.org](https://jogamp.org/) and add jogamp-all-platforms/jar/gluegen-rt.jar and jogamp-all-platforms/jar/jogl-all.jar to the classpath
		- Recommended setup is to make a user library in eclipse that links against the above two mentioned jars.

### Testing
To run the project, run ClientDriver.java 
(or ServerDriver.java if trying to host server to play over the interwebs)

Run with the command line argument `DEBUG` to enable the debug settings menu.
