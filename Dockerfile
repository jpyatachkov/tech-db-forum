FROM ubuntu:16.04

ENV APP_ROOT "/var/www/databases"
ENV POSTGRES_VERSION 9.6

RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ xenial-pgdg main" > /etc/apt/sources.list.d/pgdg.list
RUN apt-get update -y && apt-get install -y wget
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN apt-get update -y && apt-get install -y postgresql-$POSTGRES_VERSION openjdk-8-jdk-headless

RUN mkdir -p $APP_ROOT
COPY . $APP_ROOT

USER postgres

RUN service postgresql start &&\
    psql -c "ALTER USER postgres WITH PASSWORD 'postgres';" &&\
    psql -c "CREATE DATABASE databases;" &&\
    psql -c "GRANT ALL PRIVILEGES ON DATABASE databases TO postgres;" &&\
    service postgresql stop

RUN echo "synchronous_commit = off" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "logging_collector = off" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "fsync = off" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "shared_buffers = 256MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "effective_cache_size = 512MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "work_mem = 16MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "maintenance_work_mem = 128MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "wal_buffers = 1MB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "min_wal_size = 1GB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "max_wal_size = 2GB" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "effective_io_concurrency = 300" >> /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf &&\
    echo "checkpoint_completion_target = 0.7" >>  /etc/postgresql/$POSTGRES_VERSION/main/postgresql.conf

USER root

EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

WORKDIR $APP_ROOT
RUN ./mvnw clean package

EXPOSE 5000

CMD service postgresql start &&  java -Xmx450M -Xms450M -jar $APP_ROOT/target/databases-0.0.1-SNAPSHOT.jar
