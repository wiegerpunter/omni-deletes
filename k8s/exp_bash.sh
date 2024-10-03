#!/bin/bash

# Parameters
setting="Test_two_LHS"
dataset="synthEquiDepthBins"
repetition="1"
runOnODC="true"
path="/app/data/"
withDeletes="true"
spreadOutDeletes="true"
useExactUnionSize="false"
ramVals="50,100,150,200"
noiseUpdateFractions="[0,0.5,1,3,9]"
bufferValuesOmni="[1]"
numBins="3"
numPredicates="3"
exp2LHS="true"
expaSH="true"
expHydra="false"
onlyKmin="false"
widthOptionsGridSearch="8,10,12,14,15,16,17,18,19,20,22,24,28,30"
depthOptionsGridSearch="1,2,3,4,5"
sizeFactorOptions="10"
expName="increasingNoise"
useMultAttributes="false"

id=49
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
    -e "s|{{noiseUpdateFractions}}|$noiseUpdateFractions|g" \
    -e "s|{{bufferValuesOmni}}|$bufferValuesOmni|g" \
    -e "s|{{numBins}}|$numBins|g" \
    -e "s|{{numPredicates}}|$numPredicates|g" \
    -e "s|{{exp2LHS}}|$exp2LHS|g" \
    -e "s|{{expaSH}}|$expaSH|g" \
    -e "s|{{expHydra}}|$expHydra|g"\
    -e "s|{{onlyKmin}}|$onlyKmin|g" \
    -e "s|{{widthOptionsGridSearch}}|$widthOptionsGridSearch|g" \
    -e "s|{{depthOptionsGridSearch}}|$depthOptionsGridSearch|g" \
    -e "s|{{sizeFactorOptions}}|$sizeFactorOptions|g" \
    -e "s|{{expName}}|$expName|g" \
    -e "s|{{useMultAttributes}}|$useMultAttributes|g" \
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
#kubectl apply -f job.yaml

# Clean up the temporary file
#rm job.yaml