#!/bin/bash

# Get output to ODC output

name_pod=check-pvc
filename=recording-7.jfr
output_dir=/app/data/output/profiler/$filename
odc_output_dir=/home/wieger/omni-deletes/odc_output/profiler/$filename

kubectl cp $name_pod:$output_dir $odc_output_dir
# java -Xmx450G -jar /app/DSCM_jar/DSCM.jar "Test_two_LHS" "synthEquiDepthBins" "4"