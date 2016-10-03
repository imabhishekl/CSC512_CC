#!/bin/sh

#Script reads all the input c file and print the results

if [ $# -eq "1" ] 
then
   echo "Please pass files to be tested"
   exit
fi

for f in $@
do
   echo "*********************************************************************************************"
   echo "Testing File ${f}"
   echo "java Parser ${f}"
   java Parser ${f}
done
