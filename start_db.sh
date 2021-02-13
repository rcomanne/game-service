#!/bin/bash

docker network create sb-app
docker run --name postgres-sb -p 5432:5432 -e POSTGRES_PASSWORD=sbapp --net sb-app -d postgres
