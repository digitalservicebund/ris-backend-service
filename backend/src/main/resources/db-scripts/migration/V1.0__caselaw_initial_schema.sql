CREATE TABLE IF NOT EXISTS
  xml_publication (
    mail_subject character varying(256) COLLATE pg_catalog."default" NOT NULL,
    xml text COLLATE pg_catalog."default",
    status_code character varying(20) COLLATE pg_catalog."default",
    status_messages text COLLATE pg_catalog."default",
    file_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    publish_date timestamp without time zone NOT NULL,
    receiver_address character varying(256) COLLATE pg_catalog."default",
    id uuid NOT NULL,
    documentation_unit_id uuid,
    CONSTRAINT xml_publication_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  publication_report (
    id uuid NOT NULL,
    content text COLLATE pg_catalog."default",
    received_date timestamp without time zone NOT NULL,
    document_unit_id uuid,
    CONSTRAINT publish_report_attachment_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  original_file_document (
    id uuid NOT NULL,
    upload_timestamp timestamp without time zone NOT NULL,
    extension character varying(10) COLLATE pg_catalog."default" NOT NULL,
    filename character varying(255) COLLATE pg_catalog."default" NOT NULL,
    s3_object_path character varying(255) COLLATE pg_catalog."default" NOT NULL,
    documentation_unit_id uuid NOT NULL,
    CONSTRAINT original_file_document_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  document_number_counter (
    id SERIAL NOT NULL PRIMARY KEY,
    nextnumber INT NOT NULL,
    currentyear INT NOT NULL
  );

INSERT INTO
  document_number_counter (nextnumber, currentyear)
SELECT
  1,
  date_part('year', CURRENT_DATE)
WHERE
  NOT EXISTS (
    SELECT
      id
    FROM
      document_number_counter
  );

CREATE INDEX
  IF NOT EXISTS original_file_document_document_unit_id_idx ON public.original_file_document USING btree (documentation_unit_id);
