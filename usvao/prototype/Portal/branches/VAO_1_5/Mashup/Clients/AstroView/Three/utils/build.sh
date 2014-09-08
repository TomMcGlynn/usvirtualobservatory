#!/bin/sh

python3.3 build.py --include common --include extras --output ../build/three.js
python3.3 build.py --include common --include extras --minify --output ../build/three.min.js
