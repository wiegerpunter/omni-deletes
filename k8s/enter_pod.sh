#!/bin/bash

# Get output to ODC output

name_pod=omnisketch2lhs-8sl58

# Enter the pod
kubectl exec -it $name_pod -- /bin/bash
