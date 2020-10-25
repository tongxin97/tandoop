# Tandoop

## Usage

```
cd tandoop
mvn package
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="../calculator"
mvn exec:java -Dexec.mainClass="com.github.tongxin97.tandoop.Main" -Dexec.args="-pkg com.github.tongxin97.tandoop -src src/main -test src/test/java/com/github/tongxin97/tandoop"
```