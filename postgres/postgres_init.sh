#!/bin/bash

psql -f /docker-entrypoint-initdb.d/init_databases
psql -f /docker-entrypoint-initdb.d/init_spark_account_service spark_account_service
psql -f /docker-entrypoint-initdb.d/init_spark_inventory_service spark_inventory_service
psql -f /docker-entrypoint-initdb.d/init_spark_order_service spark_order_service
psql -f /docker-entrypoint-initdb.d/init_spark_web_gateway spark_web_gateway
