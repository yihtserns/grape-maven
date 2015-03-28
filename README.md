Grape Maven
===========
Groovy GrapeEngine that uses Maven API directly.

Motivation
----------
Groovy's default GrapeEngine uses Apache Ivy.  If you are primarily using Apache Maven, this means having to keep two separate jar repositories.

Sure, Ivy can be asked to resolve from local Maven repository, but for anything not available in the latter, it is still going to download and store the jars into its own repository.  Also, anything that can be resolved from local Maven repository is going to be cached in its own repository as well.

Dealing with *-SNAPSHOT jars is another headache as well.

Tested on
---------
- Groovy 2.2.2
- Java 1.7.0_75
- Windows 8.1
- Apache Maven 3.2.5's local repository

Warning
-------
Breaks Groovy Console (groovyConsole.bat).

Usage instruction
-----------------
1. Download the project (preferably tag) and build it
  - Requires Maven - but if you need this, you'd already have it ;)
2. Drop the built jar into $GROOVY_HOME/lib/, together with these dependencies:
  - org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-impl-maven:2.1.1
  - org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-api-maven:2.1.1
  - org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-api:2.1.1
  - org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-spi-maven:2.1.1
  - org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-spi:2.1.1
  - org.eclipse.aether:aether-api:0.9.0.M2
  - org.eclipse.aether:aether-impl:0.9.0.M2
  - org.eclipse.aether:aether-spi:0.9.0.M2
  - org.eclipse.aether:aether-util:0.9.0.M2
  - org.eclipse.aether:aether-connector-wagon:0.9.0.M2
  - org.apache.maven:maven-aether-provider:3.1.1
  - org.apache.maven:maven-model:3.1.1
  - org.apache.maven:maven-model-builder:3.1.1
  - org.apache.maven:maven-repository-metadata:3.1.1
  - org.apache.maven:maven-settings:3.1.1
  - org.apache.maven:maven-settings-builder:3.1.1
  - org.apache.maven:wagon:wagon-provider-api:2.6
  - org.apache.maven:wagon:wagon-file:2.6
  - org.apache.maven:wagon:wagon-http-lightweight:2.6
  - org.apache.maven:wagon:wagon-http-shared:2.6
  - org.codehaus.plexus:plexus-component-annotations:1.5.5
  - org.codehaus.plexus:plexus-interpolation:1.19
  - org.codehaus.plexus:plexus-utils:3.0.15
  - org.sonatype.plexus:plexus-sec-dispatcher:1.3
  - org.sonatype.plexus:plexus-cipher:1.4
  - commons-lang:commons-lang:2.6
  - commons-io:commons-io:2.2
  - org.jsoup:jsoup:1.7.2
```powershell
# TIPS: Download these jars into C:/temp by running this in console:
C:\> groovy https://raw.githubusercontent.com/yihtserns/scripts/master/deps.groovy --folder C:/temp --artifact org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-impl-maven:2.1.1
```

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

Completed features
------------------
- [x] Supports `@Grab("<group>:<module>:<version>")`
- [x] Supports `groovy.grape.report.downloads` flag
