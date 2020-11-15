# Tandoop

## Usage

```
cd tandoop
mvn package

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/ -prj ../tandoop -limit 10"
mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ../joda-time/src/main/java/ -prj ../joda-time -limit 10"

java -cp "target/dependency/*":target/classes com.github.tongxin97.tandoop.Main -src ./src/main/java/ -prj ../tandoop

mvn surefire:test -Dtest=TandoopTest
```

## Pending Questions/TODOs
* Differentiate static methods in parser
* Handle generic types
* Handle class/method inheritance

