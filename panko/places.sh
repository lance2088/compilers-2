#!/bin/bash

sh zrob.sh test/test
sh make_panko.sh test/const
sh make_panko.sh test/calc
sh make_panko.sh test/loop
sh make_panko.sh test/function
sh make_panko.sh test/array
sh make_panko.sh test/global
