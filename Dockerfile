FROM gradle:7.2-jdk16-hotspot as build
WORKDIR /project
COPY  build/distributions/jmh.zip /project/jmh.zip
RUN jar -xf jmh.zip && rm jmh.zip

FROM adoptopenjdk/openjdk16:jre-16.0.1_9-alpine
WORKDIR /jmh
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
COPY --from=build /project/jmh/ /jmh/
RUN chown -R javauser:javauser /jmh && \
    chmod 500 /jmh/bin/jmh
USER javauser
EXPOSE 8080
CMD "/jmh/bin/jmh"