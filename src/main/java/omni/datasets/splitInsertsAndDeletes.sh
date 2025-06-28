#!/bin/bash

# Usage: ./phase1.sh <input_csv_file>

set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <input_csv_file>"
    exit 1
fi

INPUT_FILE="$1"
BASENAME=$(basename "$INPUT_FILE" .csv)

# Extract the 5th underscore-separated field
RESIDU_EXPONENT_RAW=$(echo "$BASENAME" | awk -F'_' '{print $5}')

# Take only the integer part (everything before the dot)
RESIDU_EXPONENT=${RESIDU_EXPONENT_RAW%%.*}

# Now compute residu size safely
RESIDU_SIZE=$((2 ** RESIDU_EXPONENT))
DATA_FOLDER=$(dirname "$INPUT_FILE")

echo "Detected residu exponent: $RESIDU_EXPONENT"
echo "Computed residu size: $RESIDU_SIZE"

# Run awk to split the file based on id and sign
awk -F',' -v residu_size="$RESIDU_SIZE" -v data_folder="$DATA_FOLDER" '
{
    id = $1 + 0;
    sign = $NF + 0;
    if (sign == 1 && id < residu_size)
        print > (data_folder  "/residu.csv");
    else if (sign == 1)
        print > (data_folder "/noise_inserts.csv");
    else if (sign == -1)
        print > (data_folder "/noise_deletes.csv");
}
' "$INPUT_FILE"

echo "Splitting complete:"
