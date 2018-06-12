FROM ubuntu:16.04

ENV APP_ROOT "/var/www/databases"
ENV POSTGRES_VERSION 9.6

RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ xenial-pgdg main" > /etc/apt/sources.list.d/pgdg.list
RUN apt-get -y update && apt-get install -y wget
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN apt-get update -y && apt-get install -y postgresql-$POSTGRES_VERSION openjdk-8-jdk-headless maven

RUN mkdir -p $APP_ROOT
COPY . $APP_ROOT

USER postgres

RUN service postgresql start &&\
    psql -c "ALTER USER postgres WITH PASSWORD 'postgres';" &&\
    psql -c "CREATE DATABASE databases;" &&\
    psql -c "GRANT ALL PRIVILEGES ON DATABASE databases TO postgres;" &&\
    service postgresql stop

RUN echo "synchronous_commit = off" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "logging_collector = 'off'" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "fsync = 'off'" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "max_wal_size = 1GB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "shared_buffers = 128MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "effective_cache_size = 256MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "work_mem = 64MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf

USER root

WORKDIR $APP_ROOT
RUN mvn package

EXPOSE 5000

CMD service postgresql start &&  java -Xmx384M -Xms384M -jar $APP_ROOT/target/databases-0.0.1-SNAPSHOT.jar
