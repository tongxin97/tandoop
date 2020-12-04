# Tandoop

## Usage

```
cd tandoop
mvn package

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/ -prj ../tandoop -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../joda-time/src/main/java -prj ../joda-time -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../commons-collections/src/main/java -prj ../commons-collections -limit 10"

java -cp "target/dependency/*":target/classes com.github.tongxin97.tandoop.Main -src ./src/main/java/ -prj ../tandoop

mvn surefire:test -Dtest=TandoopTest

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../toradocu-coverage/toradocu/commons-collections/inputs/commons-collections/src/main/java/ -prj ../toradocu-coverage/toradocu/commons-collections/inputs/commons-collections -limit 1200"

java -ea -cp inputs/joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:../libs/randoop.jar -Xbootclasspath/a:../libs/replacecall.jar -javaagent:../libs/replacecall.jar randoop.main.Main gentests --testjar=inputs/joda-time/target/joda-time-2.10.9-SNAPSHOT.jar --time-limit=900 --stop-on-error-test=false --junit-output-dir=src/test/java --flaky-test-behavior=output --no-error-revealing-tests --junit-reflection-allowed=false --usethreads --output-limit=2000

/Users/touhomaregen/viz/toradocu-coverage/toradocu/joda-time/build/classes/test
javac -cp ../../../inputs/joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:../../../../libs/junit-4.12.jar:. -d . ../../../src/test/java/RegressionTestDriver.java

java -cp ../joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.tongxin97.tandoop.Main -src ../joda-time/src/main/java -prj ../joda-time -limit 10 

java -cp ../commons-collections/target/commons-collections4-4.5-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.tongxin97.tandoop.Main -src ../commons-collections/src/main/java -prj ../commons-collections -limit 900
```

## Pending Questions/TODOs
* Differentiate static methods in parser
* Handle generic types
* Handle class/method inheritance
    - [DONE] For class/type inheritance, handle matching for compound types, eg. int[], Set<String>.
    - Handle method inheritance
* Create new instances on the spot (to avoid having too many null parameters).
* Deduplicate previous method sequences when constructing a new one. 
* Handle abstract class
* Handle static method