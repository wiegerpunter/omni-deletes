FROM eclipse-temurin:19-jdk-focal

## create input and output directory
#RUN mkdir -p /app/input /app/output /app/data/ /app/DSCM_jar

COPY /out/artifacts/DSCM_jar /app/DSCM_jar/
#COPY /k8s/mkdirs.sh /app/mkdirs.sh
WORKDIR /app/data
ENTRYPOINT ["java", "-jar", "/app/DSCM_jar/DSCM.jar"]

# default arguments
CMD ["/app/data/config/config.json"]
LABEL authors="wieger"

# "Test_two_LHS" "synthEquiDepthBins" "4"