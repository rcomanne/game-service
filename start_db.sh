#!/bin/bash

docker run --name postgres-sb -p 5432:5432 -e POSTGRES_PASSWORD=sbapp -d postgres
