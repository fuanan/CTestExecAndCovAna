#!/bin/bash

source_file_name="grep"
lines_executed=`grep -E '^lcount:[[:digit:]]{1,},[1-9][0-9]*' ${source_file_name}.c.gcov | cut -d ":" -f2 | cut -d "," -f1`
rm -rf ${source_file_name}.c.gcov
rm -rf ${source_file_name}.gcda
for line_executed in ${lines_executed}
do
    echo ${line_executed}
done











