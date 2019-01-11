CREATE TABLE organizations(
  id           INT          NOT NULL,
  name         VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE api_keys(
  org_id           INT          NOT NULL,
  api_key          VARCHAR(255) NOT NULL UNIQUE ,
  title            VARCHAR(255),
  deleted_at       TIMESTAMP,
  PRIMARY KEY (org_id, api_key)
);