Grape Maven
===========
Groovy GrapeEngine that uses Maven API directly.

Motivation
----------
Groovy's default GrapeEngine uses Apache Ivy.  If you are primarily using Apache Maven, this means having to keep two separate jar repositories: `%USERPROFILE%/.groovy/grapes` and `%USERPROFILE%/.m2/repository`.

Sure, Ivy can be asked to resolve from local Maven repository, but for anything not available in the latter, it is still going to download and store the jars into its own repository.  Also, anything that can be resolved from local Maven repository is going to be cached in its own repository as well.

Dealing with *-SNAPSHOT jars is another headache as well.

Tested on
---------
- Groovy 2.2.2
- Java 1.7.0_75
- Windows 8.1
- Apache Maven 3.2.5's local repository

Usage instruction
-----------------
1. Download the project (preferably tag), build it and drop the resulting jar into `$GROOVY_HOME/lib/`.
  - Requires Maven - but if you need this, you'd already have it ;)
2. Run `mvn clean package assembly:single -DdescriptorId=jar-with-dependencies` on the project and copy `grape-maven-<version>-jar-with-dependencies.jar` into `$GROOVY_HOME/lib/`.
3. To check if it works, run any Groovy script that contains `@Grab("<group>:<module>:<version>")` with `groovy.grape.report.downloads` turned on, e.g.:
```powershell
C:\> groovy -Dgroovy.grape.report.downloads=true MyScript.groovy
```
You should see messages similar to this in console:
```
...
Resolving artifact org.jsoup:jsoup:jar:1.7.2
Resolved artifact org.jsoup:jsoup:jar:1.7.2 from central (http://repo1.maven.org/maven2, releases+snapshots)
Resolving artifact commons-io:commons-io:jar:2.2
Resolved artifact commons-io:commons-io:jar:2.2 from central (http://repo1.maven.org/maven2, releases+snapshots)
...
```

Troubleshooting
---------------
Since Groovy uses `groovy.grape.GrapeIvy` as the default GrapeEngine, and there is no extension mechanism, Grape Maven overrides this by supplying a fake `groovy.grape.GrapeIvy` (that redirects to `GrapeMaven`) in its jar.

For this to happen, `grape-maven-<version>.jar` has to be loaded before `groovy-<version>.jar`.  In Windows, you shouldn't have to do anything special because I *think* jars are loaded based on name, and 'grape-maven' comes before 'groovy'.

But if for some reason this doesn't work (different platform, etc) what you can do is to remove `groovy/grape/GrapeIvy.class` from `%GROOVY_HOME%/lib/groovy-<version>.jar` (which you'd first backup somewhere safe, of course).

Milestones
----------
- [x] Supports `@Grab("<group>:<module>:<version>")`
- [x] Supports `groovy.grape.report.downloads` flag
- [x] Supports extension modules
- [x] Allows Groovy Console (`groovyConsole.bat`) to start up