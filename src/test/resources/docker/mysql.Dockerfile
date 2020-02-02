FROM mysql:latest

ENV MYSQL_DATABASE="qb4j"
ENV MYSQL_ROOT_PASSWORD="qb4j"

COPY ./init-sql/mysql-init.sql /docker-entrypoint-initdb.d

EXPOSE 3306