# Tandoop

## Usage

```
cd tandoop
mvn package
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="../calculator"
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="../joda-time/src/main"

mvn compile && mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-src ./src/main/java/com/github/tongxin97/tandoop -test ./src/test/java/com/github/tongxin97/tandoop -prj ../tandoop"

mvn surefire:test -Dtest=TandoopTest
```