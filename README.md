# Simple URL shortening service

This is a simple url shortening service written on Java. It has HTTP API and it stores data in PostgreSQL database.

## How to run locally

- Download [PostgreSQL](https://www.postgresql.org/download/) on your computer.
- Run scripts from `/resources/createdb.sql` to init tables and indexes for the database.
- Set variable for logs: `-Dlog4j.configurationFile=src/main/resources/log4j2.yaml`
- Start application: run Application's main method.

## How to run tests

- Download [PostgreSQL](https://www.postgresql.org/download/) on your computer.
- Run scripts from `/resources/createdb_test.sql` to init tables and indexes for the database.
- Set variable for logs: `-Dlog4j.configurationFile=src/test/resources/log4j2.yaml`
- Run tests.

## Usage examples

How to get alias:
Request:
`curl -X PUT "127.0.0.1:8080/create?url=http://www.thejavageek.com/2013/11/20/filter-order-servlets/"`

Response: `"0nnNGmDl"`

Open your favourite browser and type:
`127.0.0.1:8080/0nnNGmDl`

By default the alias in valid only 5 minutes

 

  