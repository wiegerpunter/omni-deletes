#!/bin/bash

# Parameters
setting="Test_two_LHS"
dataset="synthDev"
repetition="5"
runOnODC="true"
path="/app/data/"
withDeletes="false"
spreadOutDeletes="false"
useExactUnionSize="false"
ramVals="1,10,25,50,100"
noiseUpdateFractions="[0]"
bufferValuesOmni="[1]"
ingestBuffers="[0.1]"
dGridSearch="3"
bGridSearch="31,15"
parFactorGridSearch="[10,20,50,100]"
numBins="3"
numPredicates="9"
expOmniSenate="true"
expOmniHouse="true"
exp2LHS="false"
expaSH="false"
expHydra="false"
expResSample="true"
expCM="false"
expPerRow="true"
expCase1ReturnScap="true"
epsValues="[0.1,0.3,0.5,0.8,1.0]"
sizeFactorOptions="24"
noiseSize="0"
expName="eps1WithHouse"
useMultAttributes="false"
numZipfianAttrs="9"
zipfAlphas="[1.1,1.3,1.5,1.9]"

id=119
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
    -e "s|{{dGridSearch}}|$dGridSearch|g" \
    -e "s|{{bGridSearch}}|$bGridSearch|g" \
    -e "s|{{parFactorGridSearch}}|$parFactorGridSearch|g" \
    -e "s|{{numBins}}|$numBins|g" \
    -e "s|{{numPredicates}}|$numPredicates|g" \
    -e "s|{{expOmniSenate}}|$expOmniSenate|g" \
    -e "s|{{expOmniHouse}}|$expOmniHouse|g" \
    -e "s|{{exp2LHS}}|$exp2LHS|g" \
    -e "s|{{expaSH}}|$expaSH|g" \
    -e "s|{{expHydra}}|$expHydra|g"\
    -e "s|{{expResSample}}|$expResSample|g" \
    -e "s|{{expCM}}|$expCM|g" \
    -e "s|{{expPerRow}}|$expPerRow|g" \
    -e "s|{{expCase1ReturnScap}}|$expCase1ReturnScap|g" \
    -e "s|{{epsValues}}|$epsValues|g" \
    -e "s|{{sizeFactorOptions}}|$sizeFactorOptions|g" \
    -e "s|{{noiseSize}}|$noiseSize|g" \
    -e "s|{{expName}}|$expName|g" \
    -e "s|{{useMultAttributes}}|$useMultAttributes|g" \
    -e "s|{{numZipfianAttrs}}|$numZipfianAttrs|g" \
    -e "s|{{zipfAlphas}}|$zipfAlphas|g" \
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