--
-- Name: ap; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ap (
    ap integer NOT NULL,
    dt date DEFAULT now() NOT NULL,
    tt text NOT NULL,
    ur text,
    bn text,
    au text,
    ti text,
    pb text,
    yr smallint,
    uq smallint,
    ui smallint,
    ul text,
    li character(2),
    ip text,
    co text,
    ad text,
    fp text
);


--
-- Name: TABLE ap; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE ap IS 'approvers';


--
-- Name: COLUMN ap.ap; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ap IS 'ID';


--
-- Name: COLUMN ap.dt; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.dt IS 'registration date';


--
-- Name: COLUMN ap.tt; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.tt IS 'label';


--
-- Name: COLUMN ap.ur; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ur IS 'URI';


--
-- Name: COLUMN ap.bn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.bn IS 'ISBN';


--
-- Name: COLUMN ap.au; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.au IS 'author';


--
-- Name: COLUMN ap.ti; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ti IS 'title';


--
-- Name: COLUMN ap.pb; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.pb IS 'monograph publisher or serial title, volume, and page range';


--
-- Name: COLUMN ap.yr; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.yr IS 'year of publication';


--
-- Name: COLUMN ap.uq; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.uq IS 'quality measure specified by the user';


--
-- Name: COLUMN ap.ui; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ui IS 'numeric ID specified by the user';


--
-- Name: COLUMN ap.ul; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ul IS 'miscellaneous information';


--
-- Name: COLUMN ap.li; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.li IS 'type of offered license';


--
-- Name: COLUMN ap.ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ip IS 'summary of intellectual-property claim';


--
-- Name: COLUMN ap.co; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.co IS 'name of apparent intellectual-property claimant';


--
-- Name: COLUMN ap.ad; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.ad IS 'SMTP address for licensing correspondence';


--
-- Name: COLUMN ap.fp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN ap.fp IS 'source file or directory path';


--
-- Name: offer; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer (
    nr integer NOT NULL,
    product integer,
    producer integer,
    vendor integer,
    price double precision,
    "validFrom" timestamp, --- without time zone,
    "validTo" timestamp, --- without time zone,
    "deliveryDays" integer,
    "offerWebpage" character varying(100),
    publisher integer,
    "publishDate" date
);


--
-- Name: person; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE person (
    nr integer NOT NULL,
    name character varying(30),
    mbox_sha1sum character(40),
    country character(2),
    publisher integer,
    "publishDate" date
);


--
-- Name: producer; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE producer (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    homepage character varying(100),
    country character(2),
    publisher integer,
    "publishDate" date
);


--
-- Name: product; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE product (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    producer integer,
    "propertyNum1" integer,
    "propertyNum2" integer,
    "propertyNum3" integer,
    "propertyNum4" integer,
    "propertyNum5" integer,
    "propertyNum6" integer,
    "propertyTex1" character varying(250),
    "propertyTex2" character varying(250),
    "propertyTex3" character varying(250),
    "propertyTex4" character varying(250),
    "propertyTex5" character varying(250),
    "propertyTex6" character varying(250),
    publisher integer,
    "publishDate" date
);


--
-- Name: productfeature; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE productfeature (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    publisher integer,
    "publishDate" date
);


--
-- Name: productfeatureproduct; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE productfeatureproduct (
    product integer NOT NULL,
    productfeature integer NOT NULL
);


--
-- Name: producttype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE producttype (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    parent integer,
    publisher integer,
    "publishDate" date
);


--
-- Name: producttypeproduct; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE producttypeproduct (
    product integer NOT NULL,
    producttype integer NOT NULL
);


--
-- Name: review; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE review (
    nr integer NOT NULL,
    product integer,
    producer integer,
    person integer,
    "reviewDate" timestamp, --- without time zone,
    title character varying(200),
    text text,
    language character(2),
    rating1 integer,
    rating2 integer,
    rating3 integer,
    rating4 integer,
    publisher integer,
    "publishDate" date
);


--
-- Name: test; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE test (
    id character(2)
);


--
-- Name: test10; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE test10 (
    id character(10)
);


--
-- Name: vendor; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE vendor (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    homepage character varying(100),
    country character(2),
    publisher integer,
    "publishDate" date
);


--
-- Name: ap_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ap
    ADD CONSTRAINT ap_pkey PRIMARY KEY (ap);

--- ALTER TABLE ap CLUSTER ON ap_pkey;


--
-- Name: ap_tt_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

--- ALTER TABLE ap
---    ADD CONSTRAINT ap_tt_key UNIQUE (tt);


--
-- Name: offer_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE offer
    ADD CONSTRAINT offer_pkey PRIMARY KEY (nr);


--
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE person
    ADD CONSTRAINT person_pkey PRIMARY KEY (nr);


--
-- Name: producer_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE producer
    ADD CONSTRAINT producer_pkey PRIMARY KEY (nr);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE product
    ADD CONSTRAINT product_pkey PRIMARY KEY (nr);


--
-- Name: productfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE productfeature
    ADD CONSTRAINT productfeature_pkey PRIMARY KEY (nr);


--
-- Name: productfeatureproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE productfeatureproduct
    ADD CONSTRAINT productfeatureproduct_pkey PRIMARY KEY (product, productfeature);


--
-- Name: producttype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE producttype
    ADD CONSTRAINT producttype_pkey PRIMARY KEY (nr);


--
-- Name: producttypeproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE producttypeproduct
    ADD CONSTRAINT producttypeproduct_pkey PRIMARY KEY (product, producttype);


--
-- Name: review_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE review
    ADD CONSTRAINT review_pkey PRIMARY KEY (nr);


--
-- Name: vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE vendor
    ADD CONSTRAINT vendor_pkey PRIMARY KEY (nr);


--
-- Name: idx_review_language_upper; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX idx_review_language_upper ON review USING btree (upper((language)::text));


--
-- Name: offer_producer_product; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE UNIQUE INDEX offer_producer_product ON offer USING btree (producer, product, nr);
CREATE INDEX offer_producer_product ON offer (producer, product, nr);


--
-- Name: offer_product; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX offer_product ON offer USING btree (product, "deliveryDays");
CREATE INDEX offer_product ON offer (product, "deliveryDays");

--
-- Name: offer_validto; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX offer_validto ON offer USING btree ("validTo");
CREATE INDEX offer_validto ON offer ("validTo");

--
-- Name: offer_vendor_product; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX offer_vendor_product ON offer USING btree (vendor, product);
CREATE INDEX offer_vendor_product ON offer (vendor, product);

--
-- Name: offer_webpage; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX offer_webpage ON offer USING btree ("offerWebpage");
CREATE INDEX offer_webpage ON offer ("offerWebpage");

--
-- Name: pfeature_inv; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX pfeature_inv ON productfeatureproduct USING btree (productfeature, product);
CREATE INDEX pfeature_inv ON productfeatureproduct (productfeature, product);

--
-- Name: producer_country; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX producer_country ON producer USING btree (country);
CREATE INDEX producer_country ON producer (country);

--
-- Name: producer_homepage; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX producer_homepage ON producer USING btree (homepage);
CREATE INDEX producer_homepage ON producer (homepage);

--
-- Name: product_label_nr; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX product_label_nr ON product USING btree (label text_pattern_ops, nr);
CREATE INDEX product_label_nr ON product (label, nr);

--
-- Name: product_lbl; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX product_lbl ON product USING btree (label);
CREATE INDEX product_lbl ON product (label);

--
-- Name: product_pn1; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX product_pn1 ON product USING btree ("propertyNum1");
CREATE INDEX product_pn1 ON product ("propertyNum1");

--
-- Name: product_pn2; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX product_pn2 ON product USING btree ("propertyNum2");
CREATE INDEX product_pn2 ON product ("propertyNum2");

--
-- Name: product_pn3; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX product_pn3 ON product USING btree ("propertyNum3");
CREATE INDEX product_pn3 ON product ("propertyNum3");

--
-- Name: product_producer_nr; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE UNIQUE INDEX product_producer_nr ON product USING btree (producer, nr);
CREATE UNIQUE INDEX product_producer_nr ON product (producer, nr);

--
-- Name: ptype_inv; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX ptype_inv ON producttypeproduct USING btree (producttype, product);
CREATE INDEX ptype_inv ON producttypeproduct (producttype, product);

--
-- Name: review_person; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX review_person ON review USING btree (person);
CREATE INDEX review_person ON review (person);

--
-- Name: review_person_1; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX review_person_1 ON review USING btree (person, product, title);
CREATE INDEX review_person_1 ON review (person, product, title);

--
-- Name: review_producer_product; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE UNIQUE INDEX review_producer_product ON review USING btree (producer, product, nr);
CREATE UNIQUE INDEX review_producer_product ON review (producer, product, nr);

--
-- Name: review_product_person_producer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE UNIQUE INDEX review_product_person_producer ON review USING btree (product, person, producer, nr);
CREATE UNIQUE INDEX review_product_person_producer ON review (product, person, producer, nr);


--
-- Name: review_textlang; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX review_textlang ON review USING btree (language);
CREATE INDEX review_textlang ON review (language);

--
-- Name: vendor_country; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX vendor_country ON vendor USING btree (country);
CREATE INDEX vendor_country ON vendor (country);

--
-- Name: vendor_homepage; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

--- CREATE INDEX vendor_homepage ON vendor USING btree (homepage);
CREATE INDEX vendor_homepage ON vendor (homepage);

--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

--- REVOKE ALL ON SCHEMA public FROM PUBLIC;
--- REVOKE ALL ON SCHEMA public FROM postgres;
--- GRANT ALL ON SCHEMA public TO postgres;
--- GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: ap; Type: ACL; Schema: public; Owner: -
--

--- REVOKE ALL ON TABLE ap FROM PUBLIC;
--- REVOKE ALL ON TABLE ap FROM postgres;
--- GRANT ALL ON TABLE ap TO postgres;


--
-- PostgreSQL database dump complete
--

