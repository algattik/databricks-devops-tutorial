#!/bin/bash
set -euo pipefail

sudo apt-get install -y python3-setuptools
pip3 install wheel
pip3 install databricks-cli
sudo ln -s /home/vsts/.local/bin/databricks /usr/local/bin/databricks
databricks clusters list

