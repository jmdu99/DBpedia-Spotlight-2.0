#!/bin/bash

mkdir models_packed
cd models

for m in *; do
    tar -zcvf "../models_packed/$m.tar.gz" $m
done

cd ..
mkdir data_packed
cd data
for m in *; do
    tar -zcvf "../data_packed/$m.tar.gz" $m
done
cd ..

