#!/bin/bash

source_file_name="grep"
gcno_file="${source_file_name}.gcno"
gcda_file="${source_file_name}.gcda"
o_file="${source_file_name}.o"
gcov_file="${source_file_name}.c.gcov"

remove_old_files(){
	if [ -f $1 ]
	then
		echo "Cleaning $1"
		rm -rf $1
	fi
}

remove_old_files ${gcno_file}
remove_old_files ${gcda_file}
remove_old_files ${o_file}
remove_old_files ${gcov_file}

gcc --coverage ${source_file_name}.c -c -I. -I/home/anfu/proteumIM2.0/LINUX/bin
gcc --coverage ${o_file} -no-pie -o ${source_file_name}


