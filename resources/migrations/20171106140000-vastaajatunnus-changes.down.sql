ALTER TABLE uraseuranta.vastaajatunnus DROP COLUMN id;

ALTER TABLE uraseuranta.vastaajatunnus ADD PRIMARY KEY (virta_data_id, tunnus)