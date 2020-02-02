FROM postgres:latest

COPY ./init-sql/postgresql-init.sql /docker-entrypoint-initdb.d

EXPOSE 5432