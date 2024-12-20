#!/bin/bash

# Get output to ODC output

name_pod=check-pvc
data_set=synthDev
output_dir=/app/data/output/pointQueries/$data_set
odc_output_dir=/home/wieger/omni-deletes/odc_output/pointQueries/$data_set

# Get the current date in YYYY-MM-DD format
#current_date=$(date +%Y-%m-%d)
#
## Find files modified today and copy them
#files=$(kubectl exec $name_pod -- find $output_dir -mtime -1)
##echo $files
#for file in $files; do
#  kubectl cp $name_pod:$file $odc_output_dir
#done

kubectl cp $name_pod:$output_dir $odc_output_dir

#kubectl cp $name_pod:$output_dir/results_Test_two_LHS_SpreadOutDeletesNewHashes_dataset_synthEquiDepthBins_2024-07-02 10:02:29.csv $odc_output_dir/results_Test_two_LHS_SpreadOutDeletesNewHashes_dataset_synthEquiDepthBins_2024-07-02 10:02:29.csv
# java -Xmx450G -jar /app/DSCM_jar/DSCM.jar "Test_two_LHS" "synthEquiDepthBins" "4"

# kubectl cp check-pvc:/app/data/output/pointQueries/synthEquiDepthBins/"results_Test_two_LHS_SpreadOutDeletesNewHashes_dataset_synthEquiDepthBins_2024-07-02 10:02:29.csv" /home/wieger/omni-deletes/odc_output/pointQueries/synthEquiDepthBins/results.csv