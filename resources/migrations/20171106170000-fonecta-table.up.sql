CREATE TABLE uraseuranta.fonecta_data (
  virta_data_id INTEGER NOT NULL REFERENCES uraseuranta.virta_data(id),
  matkapuhelin TEXT NOT NULL,
  haltijaliittyma BOOLEAN NOT NULL DEFAULT FALSE,
  yritysliittyma BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (virta_data_id, matkapuhelin)
);