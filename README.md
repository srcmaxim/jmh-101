### During this workshop we were trying to:
- test perforamce of some Optional vs IF statement
- test performance of Stream vs foreach

But the main goal was to check log messages of JIT compiler and GC 

If you want to try it by yourself, you need to build a shadow jar:
```
gradle clean build shadowJar
```

All tests we do in docker container, that allows us to restrict some resources and check
what impact does it have on JVM and test results

#### First invocation Optional vs If statement (no limits for memory and cpu)
```
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```
JIT logs
```
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -XX:+PrintCompilation -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```

#### What GC is used and its stats (measurements = 1)
```
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=1,filesize=100m -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```

#### Collect GC time for IF and Optional:
```
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -XX:+FlightRecorder -XX:StartFlightRecording=duration=2m,filename=flightIf.jfr -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -XX:+FlightRecorder -XX:StartFlightRecording=duration=2m,filename=flightOpt.jfr -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```

#### Limit cpu to 1 with GC logs
```
docker run --rm --cpus="1" -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=1,filesize=100m -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```

#### Sum of prices: stream vs iterable
```
docker run --rm -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```

####  Sum of prices: 1 cpu
```
docker run --rm -m=1g --cpus="1" -v "$PWD":/app/jmh -w /app/jmh openjdk:11 java -jar /app/jmh/build/libs/microbenchmark-1.0-SNAPSHOT-all.jar
```
