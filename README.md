# Tandoop

## Usage

```
cd tandoop
mvn package

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/ -prj ../tandoop -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../joda-time/src/main/java -prj ../joda-time -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../commons-collections/src/main/java -prj ../commons-collections -limit 10"

java -cp "target/dependency/*":target/classes com.github.tongxin97.tandoop.Main -src ./src/main/java/ -prj ../tandoop

mvn compile assembly:single && java -cp ../joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../joda-time/src/main/java -prj ../joda-time -limit 900

mvn compile assembly:single && java -cp ../commons-collections/target/commons-collections4-4.5-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../commons-collections/src/main/java -prj ../commons-collections -limit 900

mvn compile assembly:single && java -cp ../commons-codec/target/commons-codec-1.16-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../commons-codec/src/main/java -prj ../commons-codec -limit 900 -numTests 500

mvn compile assembly:single && java -cp ../commons-compress/target/commons-compress-1.21-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar:"../commons-compress/target/dependency/*" -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../commons-compress/src/main/java -prj ../commons-compress -limit 900 -numTests 500

mvn compile assembly:single && java -cp ../commons-cli/target/commons-cli-1.5-SNAPSHOT.jar.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../commons-cli/src/main/java -prj ../commons-cli -limit 900 -numTests 500

```

### install Maven and prepare target repo
```
wget https://apache.osuosl.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
tar xvf apache-maven-3.6.3-bin.tar.gz
export M2_HOME=~/apache-maven-3.6.3
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
mvn -version
mvn clean install -DskipTests
mvn dependency:copy-dependencies
```

## Pending Questions/TODOs
* [DONE] Differentiate static methods in parser
* [DONE] Handle class/method inheritance
    - [DONE] For class/type inheritance, handle matching for compound types, eg. int[], Set<String>.
    - [DONE] Handle method inheritance
* [DONE] Deduplicate previous method sequences when constructing a new one. 
* [DONE] Handle interface
* [DONE] Static nested class
* [Done] On-demand construction of external types (no improvements)
* [DONE] Heuristic on constructor selection
    * [DONE] Prefer constructors with no arguments or only primitive ones.
* [Done] Primitive type casting
    * [Done] Debug "basic" type compilation error
    * [Done] Update primitive type variable selection
    * [Done] Fix bugs in casting
* [Done] Init String value pool
* [DONE] Handle generic types
* [DONE] BloodHound
    * [Done] get coverageInfo
        * [Done] Remove unrelated codes
    * [DONE] Select method according to its probability
    * [DONE] Update method selection probability according coverageInfo
* [DONE] Debug extensible flag

### Future Work
* Avoid using deprecated APIs:
```
Note: src/test/java/com/github/tongxin97/tandoop/TandoopTest.java uses or overrides a deprecated API.
```
* Upgrade to >= JDK 9 

## Related works:
https://people.kth.se/~artho/papers/lei-ase2015.pdf
