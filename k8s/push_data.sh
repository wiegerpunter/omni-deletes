#!/bin/bash

# Get output to ODC output

name_pod=check-pvc
input_dir=/app/data/input/data/SNMP/

take_from=/home/wieger/omni-deletes/input/data/SNMP/

kubectl cp $take_from $name_pod:$input_dir