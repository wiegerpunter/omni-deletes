FROM eclipse-temurin:19-jdk-focal

# create input and output directory
#RUN mkdir -p /app/input /app/output /app/input/pointQueries/CAIDA /app/input/pointQueries/SNMP  \
#    /app/input/pointQueries/synthEquiDepthBins /app/input/pointQueries/synthUniform /app/input/pointQueries/zipf \
#    /app/input/data/CAIDA /app/input/data/SNMP /app/input/data/synthEquiDepthBins /app/input/data/synthUniform \
#    /app/input/data/Zipf /app/output/pointQueries/CAIDA /app/output/pointQueries/SNMP /app/output/pointQueries/synthEquiDepthBins \
#    /app/output/pointQueries/synthUniform /app/output/pointQueries/zipf /app/output/logs

COPY /out/artifacts/DSCM_jar /app/DSCM_jar/
COPY /k8s/mkdirs.sh /app/mkdirs.sh
WORKDIR /app
ENTRYPOINT ["java", "-jar", "DSCM_jar/DSCM.jar"]

# default arguments
CMD ["Test_two_LHS", "synthEquiDepthBins", "4"]
LABEL authors="wieger"

# "Test_two_LHS" "synthEquiDepthBins" "4"