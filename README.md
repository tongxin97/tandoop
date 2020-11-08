# Tandoop

## Usage

```
cd tandoop
mvn package
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="../calculator"
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="../joda-time/src/main"

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/ -prj ../tandoop"

java -cp "target/dependency/*":target/classes com.github.tongxin97.tandoop.Main -src ./src/main/java/ -prj ../tandoop

mvn surefire:test -Dtest=TandoopTest
```

## Pending Questions/TODOs
