# JMH 101
> gradle-version: 7.2  
> java-version: 16  
> shell: powershell

- test performance of Optional vs If statement
- test performance of Stream vs foreach
- check log messages of JIT compiler and GC 

All tests executed in a Docker container to restrict some resources

Build a distribution:
```
gradle distZip
docker build -t jmh .
```


```powershell
docker run --rm jmh 1️⃣

docker run --rm ` 2️
  -e "JAVA_OPTS=-XX:+PrintCompilation" `
  jmh
  
docker run --rm ` 3️
  -v "$PWD/log:/jmh/log" `
  -e "JAVA_OPTS=-Xlog:gc*:file=log/gc.log:time,uptime,level,tags:filecount=1,filesize=100m" `
  jmh
  
docker run --rm ` 4️
  -v "$PWD/log:/jmh/log" `
  -e "JAVA_OPTS=-XX:+FlightRecorder -XX:StartFlightRecording=duration=2m,filename=log/flightIf.jfr" `
  jmh
docker run --rm `
  -v "$PWD/log:/jmh/log" `
  -e "JAVA_OPTS=-XX:+FlightRecorder -XX:StartFlightRecording=duration=2m,filename=log/flightOpt.jfr" `
  jmh
  
docker run --rm --cpus="1" ` 5️
  -v "$PWD/log:/jmh/log" `
  -e "JAVA_OPTS=-Xlog:gc*:file=log/gc.log:time,uptime,level,tags:filecount=1,filesize=100m" `
  jmh
  
docker run --rm jmh 6️
docker run --rm -m=1g --cpus="1" jmh        
```

1️⃣ Basic JMH run (more op/sec -- better)  
2️Print JIT compiled methods  
3️Print GC log  
4️Print JVM Flight Recorder stats `log/flight*.jfr` to use in JDK Mission Control  
5️Print GC log for 1 CPU workload  
6️Compare unbounded to bounded app 