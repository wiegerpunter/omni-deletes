#!/bin/bash

# Parameters
setting="Test_two_LHS"
dataset="synthEquiDepthBins"
repetition="4"
runOnODC="false"
path="/app/data/"
withDeletes="true"
spreadOutDeletes="false"
useExactUnionSize="false"
ramVals="50,100,150,200"
onlyKmin="false"
widthOptionsGridSearch="8,12,16"
depthOptionsGridSearch="3,4,5"

id=2
# Substitute parameters in the template
sed -e "s|{{ID}}|$id|g" \
    -e "s|{{setting}}|$setting|g" \
    -e "s|{{dataset}}|$dataset|g" \
    -e "s|{{repetition}}|$repetition|g" \
    -e "s|{{runOnODC}}|$runOnODC|g" \
    -e "s|{{path}}|$path|g" \
    -e "s|{{withDeletes}}|$withDeletes|g" \
    -e "s|{{spreadOutDeletes}}|$spreadOutDeletes|g" \
    -e "s|{{useExactUnionSize}}|$useExactUnionSize|g" \
    -e "s|{{ramVals}}|$ramVals|g" \
    -e "s|{{onlyKmin}}|$onlyKmin|g" \
    -e "s|{{widthOptionsGridSearch}}|$widthOptionsGridSearch|g" \
    -e "s|{{depthOptionsGridSearch}}|$depthOptionsGridSearch|g" \
    job-template.yaml > job.yaml

# Apply the YAML
kubectl apply -f job.yaml

# Clean up the temporary file
rm job.yaml


##!/bin/bash
#
## Define your arguments here
#setting="Test_two_LHS"
#dataset="synthEquiDepthBins"
#repetition="4"
#runOnODC="false"
#path="/app/data/"
#withDeletes="true"
#spreadOutDeletes="false"
#useExactUnionSize="false"
#ramVals="50,100,150,200"
#onlyKmin="false"
#
#id=1
## Substitute parameters in the template
#sed -e "s|{{ID}}|$id|g" \
#    -e "s|{{setting}}|$setting|g" \
#    -e "s|{{dataset}}|$dataset|g" \
#    -e "s|{{repetition}}|$repetition|g" \
#    -e "s|{{runOnODC}}|$runOnODC|g" \
#    -e "s|{{path}}|$path|g" \
#    -e "s|{{withDeletes}}|$withDeletes|g" \
#    -e "s|{{spreadOutDeletes}}|$spreadOutDeletes|g" \
#    -e "s|{{useExactUnionSize}}|$useExactUnionSize|g" \
#    -e "s|{{ramVals}}|$ramVals|g" \
#    -e "s|{{onlyKmin}}|$onlyKmin|g" \
#    job-template.yaml > job.yaml

#
#
#sed -e "s|{{IMAGE}}|$IMAGE|g" \
#    -e "s|{{COMMAND}}|$COMMAND|g" \
#    -e "s|{{CPU}}|$CPU|g" \
#    -e "s|{{MEMORY}}|$MEMORY|g" \
#    -e "s|{{POWER}}|$POWER|g" \
#    job-template.yaml > job.yaml

# Apply the YAML
kubectl apply -f job.yaml

# Clean up the temporary file
rm job.yaml