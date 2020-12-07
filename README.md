# Tandoop

## Usage

```
cd tandoop
mvn package

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/ -prj ../tandoop -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../joda-time/src/main/java -prj ../joda-time -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../commons-collections/src/main/java -prj ../commons-collections -limit 10"

java -cp "target/dependency/*":target/classes com.github.tongxin97.tandoop.Main -src ./src/main/java/ -prj ../tandoop

mvn compile assembly:single && java -cp ../joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.tongxin97.tandoop.Main -src ../joda-time/src/main/java -prj ../joda-time -limit 900

mvn compile assembly:single && java -cp ../commons-collections/target/commons-collections4-4.5-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.tongxin97.tandoop.Main -src ../commons-collections/src/main/java -prj ../commons-collections -limit 900

mvn compile assembly:single && java -cp ../joda-time/target/joda-time-2.10.9-SNAPSHOT.jar:target/tandoop-1.0-SNAPSHOT-jar-with-dependencies.jar -Xbootclasspath/a:jacocoagent.jar -javaagent:jacocoagent.jar com.github.tongxin97.tandoop.Main -src ../joda-time/src/main/java -prj ../joda-time -limit 900

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
* Heuristic on constructor selection
    * [DONE] Prefer constructors with no arguments or only primitive ones.
* [Done] Primitive type casting
    * [Done] Debug "basic" type compilation error
    * [Done] Update primitive type variable selection
    * Fix bugs in casting
* [Done] Init String value pool
* Handle generic types
* BloodHound
    * [Done] get coverageInfo
        * Remove unrelated codes
    * Select method according to its probablity
    * Update method selection probability according coverageInfo
* Debug extensible flag

## Related works:
https://people.kth.se/~artho/papers/lei-ase2015.pdf
