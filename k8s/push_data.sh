#!/bin/bash

# Get output to ODC output

name_pod=check-pvc
input_dir=/app/data/input/paramTable

take_from=/home/wieger/omni-deletes/input/paramTable/bufferMinwiseTable.csv

kubectl cp $take_from $name_pod:$input_dir