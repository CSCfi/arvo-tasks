CREATE SCHEMA uraseuranta;

CREATE TABLE uraseuranta.uraseuranta
(id SERIAL PRIMARY KEY,
 name TEXT NOT NULL);

CREATE TABLE uraseuranta.file(
  uraseuranta_id INTEGER REFERENCES uraseuranta.uraseuranta(id),
  filename TEXT NOT NULL,
  checksum TEXT NOT NULL,
  date TIMESTAMP NOT NULL,
  PRIMARY KEY (uraseuranta_id, checksum)
);

CREATE TABLE uraseuranta.virta_data(
  id SERIAL PRIMARY KEY,
  uraseuranta_id INTEGER REFERENCES uraseuranta.uraseuranta(id),
  oppilaitoskoodi TEXT NOT NULL,
  oppilaitos_nimi TEXT,
  valmistumisvuosi TEXT,
  opiskelijatunnus TEXT,
  henkilotunnus TEXT,
  sukupuoli TEXT,
  ika_valmistuessa TEXT,
  kansalaisuus TEXT,
  aidinkieli TEXT,
  koulutusalakoodi TEXT,
  koulutusala TEXT,
  paaaine TEXT,
  tutkinnon_taso TEXT,
  tutkinto_koulutuskoodi TEXT,
  tutkinto_nimi TEXT,
  laajuus TEXT,
  valintavuosi DATE,
  asuinkunta_koodi TEXT,
  asuinkunta_nimi TEXT,
  valmistumisajankohta DATE,
  opiskelupaikkakunta_koodi TEXT,
  opiskelupaikkakunta_nimi TEXT,
  kirjoilla_olo_kuukausia TEXT,
  lasnaolo_lukukausia TEXT,
  arvosana TEXT,
  asteikko TEXT);

CREATE TABLE uraseuranta.vastaajatunnus(
  virta_data_id INTEGER NOT NULL REFERENCES uraseuranta.virta_data(id),
  tunnus TEXT NOT NULL,
  PRIMARY KEY (virta_data_id, tunnus)
);

CREATE TABLE uraseuranta.vrk_data(
  id SERIAL PRIMARY KEY,
  uraseuranta_id INTEGER REFERENCES uraseuranta.uraseuranta(id),
  henkilotunnus TEXT NOT NULL,
  sukunimi TEXT NOT NULL,
  etunimet TEXT NOT NULL,
  sukupuoli INTEGER,
  aidinkieli TEXT,
  lahiosoite TEXT,
  postinumero TEXT,
  postitoimipaikka TEXT,
  kotim_osoitt_muuttopaiva DATE,
  kotikunta TEXT,
  kotikunnan_nimi TEXT,
  vakinainen_ulkomainen_osoite TEXT,
  ulkomaisen_osoitteen_paikkakunta TEXT,
  ulkomaisen_asuinvaltion_postinimi TEXT,
  asuinvaltio TEXT,
  ulkom_asuinvaltion_nimi TEXT,
  ulkomaille_muuton_pv DATE
)

