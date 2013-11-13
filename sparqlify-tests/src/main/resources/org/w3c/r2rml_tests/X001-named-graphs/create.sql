CREATE TABLE "tags"
(
  "id" character varying(64) NOT NULL,
  "name" character varying(256),
  "owner" character varying(256),
  CONSTRAINT "tagsPK" PRIMARY KEY ("id")
);

INSERT INTO "tags" VALUES (1, 'programming', 'thomas');
INSERT INTO "tags" VALUES (2, 'statistics', 'willem');
