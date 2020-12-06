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
```

## Pending Questions/TODOs
* [DONE] Differentiate static methods in parser
* Handle generic types
* Handle class/method inheritance
    - [DONE] For class/type inheritance, handle matching for compound types, eg. int[], Set<String>.
    - Handle method inheritance
* [DONE] Deduplicate previous method sequences when constructing a new one. 
* Remove abstract class constructor.
