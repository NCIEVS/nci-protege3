#!/bin/sh

if [ ! -e server.pid ] ; then
    echo 'Pid file not found.  Server not running? Cannot restart.'
    exit 1
fi

if [ ! -e server-args.txt ] ; then
    echo 'Server argument file not found, cannot restart.  Stop and start explicitly.'
    exit 1
fi

dir=$(dirname $0)
args=$(cat server-args.txt)

/bin/sh ${dir}/stop_explanation_server.sh
/bin/sh ${dir}/start_explanation_server.sh ${args}
