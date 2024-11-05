#!/bin/bash

# Parameters
setting="Test_two_LHS"
dataset="synthEquiDepthBins"
repetition="5"
runOnODC="true"
path="/app/data/"
withDeletes="false"
spreadOutDeletes="true"
useExactUnionSize="false"
ramVals="25000"
noiseUpdateFractions="[0]"
bufferValuesOmni="[1]"
ingestBuffers="[0.1]"
densityGridSearch="[0.01]"
dGridSearch="3"
bGridSearch="32"
wGridSearch="57,115"
BGridSearch="8000,9000,10000,11000,12000,13000,14000,15000,16000,17000,18000,19000,20000,21000"
numBins="10"
numPredicates="3"
exp2LHS="false"
expaSH="false"
expHydra="false"
expResSample="true"
expCM="false"
sizeFactorOptions="23,24,25,26"
noiseSize="0"
expName="growingB"
useMultAttributes="false"

id=97
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
    -e "s|{{ingestBuffers}}|$ingestBuffers|g" \
    -e "s|{{densityGridSearch}}|$densityGridSearch|g" \
    -e "s|{{dGridSearch}}|$dGridSearch|g" \
    -e "s|{{bGridSearch}}|$bGridSearch|g" \
    -e "s|{{wGridSearch}}|$wGridSearch|g" \
    -e "s|{{BGridSearch}}|$BGridSearch|g" \
    -e "s|{{numBins}}|$numBins|g" \
    -e "s|{{numPredicates}}|$numPredicates|g" \
    -e "s|{{exp2LHS}}|$exp2LHS|g" \
    -e "s|{{expaSH}}|$expaSH|g" \
    -e "s|{{expHydra}}|$expHydra|g"\
    -e "s|{{expResSample}}|$expResSample|g" \
    -e "s|{{expCM}}|$expCM|g" \
    -e "s|{{sizeFactorOptions}}|$sizeFactorOptions|g" \
    -e "s|{{noiseSize}}|$noiseSize|g" \
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