#!/bin/sh

cp=$(find lib -name '*.jar' | tr '\n' ':')

java -cp ${cp} com.clarkparsia.dig20.client.admin.ServerShutdown "$@"
