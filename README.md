** Practical Work 3: Argumentation & negotiation simulation with JADE **
-------------

JADE (Java Agent DEvelopment Framework) is a software Framework fully implemented in the Java language. 
It simplifies the implementation of multi-agent systems through a middle-ware that complies with the FIPA specifications and through a set of graphical tools that support the debugging and deployment phases.

PW3 Engine: This practical session will be devoted to the programming of a negotiation \& argumentation simulation. 
Agents representing human engineering will need to negotiate with each other to make a common decision regarding the choice of the best engine. 
The negotiation comes when the agents  have different preferences on the criteria and the argumentation will be used to help them to decide which item to select. 
Moreover, the arguments supporting the best option will help to build the justification that the engineering should provide to their manager at the end.

1. [ Dependencies ](#dependencies)
2. [ Implement your work ](#implement-your-work)
3. [ Run your project ](#run-your-project)

-------------

## Dependencies

Java JDK 8 must be installed on the computer :

**On Windows**

1. Go to Oracle website and download the JDK corresponding to your computer architecture (x64 or x86): [Java](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

2. After the installation, you have to add java in the *PATH*.

**On Linux (Debian / Ubuntu)**

    sudo apt-get install openjdk-8-jdk
    
**On Mac OS **

1. Go to Oracle website and download the JDK *.dmg* file: [Java](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

2. From either the browser Downloads window or from the file browser, double-click the .dmg file to start it. A Finder window appears that contains an icon of an open box and the name of the .pkg file.

3. Double-click the JDK *.pkg* icon to start the installation application.

## Implement your work

Download or clone the repository.

Open the project with Eclipse or IntelliJ (or another IDE). 

Implement your agents in the agents package. Implement your other work as you want in another packages.

## Run your project

With Gradle:

    # Build your project
    gradlew build 
    # Run JADE GUI
    gradlew run 
    # Run your project
    gradlew launch 

With java command lines:

    # Run JADE GUI
    java -cp libs/jade-4.5.0.jar -cp pw3-engine-0.1.jar jade.Boot -gui
    
In the window, on the left, click right on the Main Container package and choose *Start new agent*.

In the new window, select the class of your agent and give it a name. Click on start.
    
    # Run your project
    java -cp libs/jade-4.5.0.jar -jar pw3-engine-0.1.jar

Look at the terminal to see your project run.