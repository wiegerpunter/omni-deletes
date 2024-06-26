#!/bin/bash

# Get output to ODC output

name_pod=check-pvc
data_set=synthEquiDepthBins
output_dir=/app/data/output/pointQueries/$data_set
odc_output_dir=/home/wieger/omni-deletes/odc_output/pointQueries/$data_set

kubectl cp $name_pod:$output_dir $odc_output_dir
# java -Xmx450G -jar /app/DSCM_jar/DSCM.jar "Test_two_LHS" "synthEquiDepthBins" "4"