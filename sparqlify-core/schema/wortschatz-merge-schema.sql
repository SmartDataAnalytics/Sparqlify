--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: co_n; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE co_n (
    w1_id bigint DEFAULT 0 NOT NULL,
    w2_id bigint DEFAULT 0 NOT NULL,
    freq bigint,
    sig real,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: co_s; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE co_s (
    w1_id bigint DEFAULT 0 NOT NULL,
    w2_id bigint DEFAULT 0 NOT NULL,
    freq bigint,
    sig real,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: inv_so; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE inv_so (
    so_id integer DEFAULT 0 NOT NULL,
    s_id bigint DEFAULT 0 NOT NULL,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: inv_w; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE inv_w (
    w_id bigint DEFAULT 0 NOT NULL,
    s_id bigint DEFAULT 0 NOT NULL,
    lang smallint DEFAULT 99 NOT NULL,
    pos integer
);


--
-- Name: languages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE languages (
    l_id smallint,
    lang text
);


--
-- Name: meta; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE meta (
    run integer DEFAULT 0 NOT NULL,
    attribute character varying(255) DEFAULT ''::character varying NOT NULL,
    value character varying(255) DEFAULT ''::character varying NOT NULL,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: sentences_s_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sentences_s_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sentences; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sentences (
    s_id integer DEFAULT nextval('sentences_s_id_seq'::regclass) NOT NULL,
    sentence text,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: sources_so_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sources_so_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sources; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sources (
    so_id integer DEFAULT nextval('sources_so_id_seq'::regclass) NOT NULL,
    source character varying(255),
    date date,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: words_w_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE words_w_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: words; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE words (
    w_id integer DEFAULT nextval('words_w_id_seq'::regclass) NOT NULL,
    word character varying(255),
    freq bigint,
    lang smallint DEFAULT 99 NOT NULL
);


--
-- Name: co_n_w1_id_w2_id_lang_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY co_n
    ADD CONSTRAINT co_n_w1_id_w2_id_lang_pkey PRIMARY KEY (w1_id, w2_id, lang);


--
-- Name: co_s_w1_id_w2_id_lang_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY co_s
    ADD CONSTRAINT co_s_w1_id_w2_id_lang_pkey PRIMARY KEY (w1_id, w2_id, lang);


--
-- Name: words_w_id_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY words
    ADD CONSTRAINT words_w_id_pkey PRIMARY KEY (w_id);


--
-- Name: co_n_w1_id_sig; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX co_n_w1_id_sig ON co_n USING btree (w1_id, sig);


--
-- Name: co_n_w2_id_sig; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX co_n_w2_id_sig ON co_n USING btree (w2_id, sig);


--
-- Name: co_s_w1_id_sig; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX co_s_w1_id_sig ON co_s USING btree (w1_id, sig);


--
-- Name: co_s_w2_id_sig; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX co_s_w2_id_sig ON co_s USING btree (w2_id, sig);


--
-- Name: inv_so_lang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_so_lang ON inv_so USING btree (lang);


--
-- Name: inv_so_s_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_so_s_id ON inv_so USING btree (s_id);


--
-- Name: inv_so_so_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_so_so_id ON inv_so USING btree (so_id);


--
-- Name: inv_w_lang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_w_lang ON inv_w USING btree (lang);


--
-- Name: inv_w_s_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_w_s_id ON inv_w USING btree (s_id);


--
-- Name: inv_w_w_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inv_w_w_id ON inv_w USING btree (w_id);


--
-- Name: meta_lang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX meta_lang ON meta USING btree (lang);


--
-- Name: meta_run_attribute; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX meta_run_attribute ON meta USING btree (run, attribute);


--
-- Name: sentences_lang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sentences_lang ON sentences USING btree (lang);


--
-- Name: sentences_s_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sentences_s_id ON sentences USING btree (s_id);


--
-- Name: sources_date; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sources_date ON sources USING btree (date);


--
-- Name: sources_lang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sources_lang ON sources USING btree (lang);


--
-- Name: sources_so_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sources_so_id ON sources USING btree (so_id);


--
-- Name: words_word; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX words_word ON words USING btree (word);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

