#!/bin/bash

jar_path=CS455/ASG2/build/libs/ASG2-1.0.jar
user=jhfitzg
domain=cs.colostate.edu
registry_host=129.82.44.157
registry_port=7000
machine_list=./machine_list

for machine in `cat $machine_list`
do
  gnome-terminal -- bash -c "ssh -t -v $user@$machine.$domain 'java -cp $jar_path cs455.scaling.client.Client $registry_host $registry_port 2 ; /bin/bash'"
done
