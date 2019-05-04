#!/bin/sh

TEST=$(mktemp)

for run in {1..100}; do
  echo -n "" > $TEST
  for msg in {1..100}; do
    echo "k$msg=run$run" >> $TEST
  done
  tail -n 1 $TEST
  kafkacat -b localhost:19092 -P -t topic1 -K '=' < $TEST
  curl http://localhost:8080/client
  kafkacat -b localhost:19192 -C -t topic2 -K '=' -o -1 -c 1 -e
  curl http://localhost:8080/metrics
done

rm $TEST
