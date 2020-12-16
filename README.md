# Tandoop

This repo includes a custom implementation of the basic algorithm described in [the original Randoop paper](https://homes.cs.washington.edu/~mernst/pubs/feedback-testgen-icse2007.pdf), some Java-language-specific improvements and several static/dynamic heuristics mentioned in [the GRT paper](http://www.understandingrequirements.com/resources/2.11%20%20Program-Analysis-Guided%20Random%20Testing.pdf).

## Usage

1. Compile test project using `cd path/to/test_project; mvn install -DskipTests; mvn dependency:copy-dependencies`
2. Compile Tandoop using `cd path/to/tandoop; mvn compile assembly:single`
3. Run Tandoop on test project using 
```bash
java -cp path/to/test_project/target/target_project_jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src path/to/test_project/src/main/java -prj path/to/test_project
```

Additional arguments are listed as follows:

```
usage: java -cp path/to/test_project/target/target_project_jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main
 -all,--allFeatures            If Tandoop should use all features
 -cg,--coverageGuided          If Tandoop uses coverage-guided method selection
 -cp,--constructorPreference   If Tandoop uses constructor selection preference
 -noCI,--noClassInheritance    If Tandoop should use class inheritance
 -noGT,--noGenericTypes        If Tandoop should allow for generics types
                               in generated test classes
 -noMI,--noMethodInheritance   If Tandoop should use method inheritance
 -odc,--onDemandConstruction   If Tandoop uses on-demand construction of external types
 -prj,--projectDir <arg>       Project directory
 -reg,--outputRegressionTest   If Tandoop should output regression test file
 -src,--srcDir <arg>           Project src directory
 -limit,--timeLimit <arg>      Time limit to run Tandoop for in seconds
  -numTests,--numTests <arg>    Limit the number of generated tests
```

An example of usage on [Joda-time]():

```
mvn compile assembly:single && java -cp ../joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../joda-time/src/main/java -prj ../joda-time -limit 900
```

<!-- ### install Maven and prepare target repo
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
* [DONE] Output all regression test methods in one class -->

### Future Work
* Upgrade to >= JDK 9 
* Do reduction on regression tests
