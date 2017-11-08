ALTER TABLE uraseuranta.vastaajatunnus DROP CONSTRAINT vastaajatunnus_pkey;

ALTER TABLE uraseuranta.vastaajatunnus ADD COLUMN id SERIAL PRIMARY KEY;

ALTER TABLE uraseuranta.vastaajatunnus ADD CONSTRAINT tunnus_unique UNIQUE (tunnus);
ALTER TABLE uraseuranta.vastaajatunnus ADD CONSTRAINT virta_data_unique UNIQUE (virta_data_id);