#!/bin/bash
echo Rendering tree...
dot -Tpng $1.dot -o $1.png

