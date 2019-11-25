CREATE DATABASE shorturl_test;
CREATE USER shorturl_test WITH ENCRYPTED PASSWORD 'shorturl_test';
GRANT ALL PRIVILEGES ON DATABASE shorturl_test TO shorturl_test;

CREATE TABLE IF NOT EXISTS alias (
  id character (8) NOT NULL,
  CONSTRAINT pk_alias PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS urls (
    id character (8) NOT NULL,
    url character varying(2048) NOT NULL,
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_urls PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS create_time_idx ON urls (create_time ASC);
CREATE INDEX IF NOT EXISTS expire_time_idx ON urls (expire_time ASC);
CREATE INDEX IF NOT EXISTS url_idx ON urls (url ASC);

GRANT ALL PRIVILEGES ON TABLE urls TO shorturl_test;
GRANT ALL PRIVILEGES ON TABLE alias TO shorturl_test;