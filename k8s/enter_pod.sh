#!/bin/bash

# Get output to ODC output

name_pod=check-pvc

# Enter the pod
kubectl exec -it $name_pod -- /bin/bash
