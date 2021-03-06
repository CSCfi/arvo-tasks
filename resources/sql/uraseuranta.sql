-- :name add-uraseuranta! :! :n
INSERT INTO uraseuranta.uraseuranta (name) VALUES (:name);

-- :name list-uraseuranta :? :*
SELECT * FROM uraseuranta.uraseuranta;

-- :name insert-virta-data! :! :n
INSERT INTO uraseuranta.virta_data
(uraseuranta_id, oppilaitoskoodi, henkilotunnus, oppilaitos_nimi, valmistumisvuosi, valmistumisajankohta, opiskelijatunnus, sukupuoli, ika_valmistuessa,
 kansalaisuus, aidinkieli, koulutusalakoodi, koulutusala, paaaine, tutkinnon_taso, tutkinto_koulutuskoodi, tutkinto_nimi,
 laajuus, valintavuosi, asuinkunta_koodi, asuinkunta_nimi, kirjoilla_olo_kuukausia, lasnaolo_lukukausia, arvosana, asteikko)
VALUES
(:uraseuranta_id, :oppilaitoskoodi, :henkilotunnus, :oppilaitos_nimi, :valmistumisvuosi, to_date(:valmistumisajankohta, 'YYYY-MM-DD'), :opiskelijatunnus, :sukupuoli, :ika_valmistuessa,
 :kansalaisuus, :aidinkieli, :koulutusalakoodi, :koulutusala, :aine, :tutkinnon_taso, :tutkinto_koulutuskoodi, :tutkinto_nimi,
 :laajuus, to_date(:valintavuosi, 'YYYY-MM-DD'), :asuinkunta_koodi, :asuinkunta_nimi, :kirjoilla_olo_kuukausia, :lasnaolo_lukukausia, :arvosana, :asteikko);

-- :name insert-vastaajatunnus! :! :n
INSERT INTO uraseuranta.vastaajatunnus (virta_data_id, tunnus) VALUES (:id, :tunnus);

-- :name vastaajatunnus-count :? :1
SELECT COUNT(*) FROM uraseuranta.vastaajatunnus;

-- :name get-virta-data :? :*
SELECT * FROM uraseuranta.virta_data WHERE uraseuranta_id = :id;

-- :name insert-vrk-data! :! :n
INSERT INTO uraseuranta.vrk_data (
 uraseuranta_id, henkilotunnus, sukunimi, etunimet, sukupuoli, aidinkieli,
 lahiosoite, postinumero, postitoimipaikka, kotim_osoitt_muuttopaiva, kotikunta, kotikunnan_nimi,
 vakinainen_ulkomainen_osoite, ulkomaisen_osoitteen_paikkakunta, ulkomaisen_asuinvaltion_postinimi,
 asuinvaltio, ulkom_asuinvaltion_nimi, ulkomaille_muuton_pv)
VALUES
 (:uraseuranta_id, :henkilotunnus, :sukunimi, :etunimet, :sukupuoli, :aidinkieli,
  :lahiosoite, :postinumero, :postitoimipaikka, :kotim_osoitt_muuttopaiva, :kotikunta, :kotikunnan_nimi,
  :vakinainen_ulkomainen_osoite, :ulkomaisen_osoitteen_paikkakunta, :ulkomaisen_asuinvaltion_postinimi,
  :asuinvaltio, :ulkom_asuinvaltion_nimi, :ulkomaille_muuton_pv);

-- :name insert-fonecta-data! :! :n
INSERT INTO uraseuranta.fonecta_data (virta_data_id, matkapuhelin, haltijaliittyma, yritysliittyma)
    VALUES (:id, :matkapuhelin, :haltijaliittyma, :yritysliittyma);

-- :name get-data-for-fonecta :? :*
SELECT virta.id, vrk.sukunimi, vrk.etunimet, vrk.lahiosoite, vrk.postinumero, vrk.postitoimipaikka, vrk.kotikunnan_nimi,
 vrk.vakinainen_ulkomainen_osoite, vrk.ulkomaisen_osoitteen_paikkakunta, vrk.ulkomaisen_asuinvaltion_postinimi, vrk.ulkom_asuinvaltion_nimi
FROM uraseuranta.virta_data virta
 JOIN uraseuranta.vrk_data vrk ON virta.henkilotunnus = vrk.henkilotunnus
WHERE virta.uraseuranta_id = :id AND vrk.uraseuranta_id = :id;

-- :name get-tupa-list :? :*
SELECT vt.tunnus,
 v.oppilaitoskoodi, v.oppilaitos_nimi, v.valmistumisvuosi, v.valmistumisajankohta, v.opiskelijatunnus, v.ika_valmistuessa,
 v.kansalaisuus, v.koulutusalakoodi, v.koulutusala, v.paaaine, v.tutkinnon_taso, v.tutkinto_koulutuskoodi, v.tutkinto_nimi,
 v.laajuus, v.valintavuosi, v.asuinkunta_koodi, v.asuinkunta_nimi, v.kirjoilla_olo_kuukausia, v.lasnaolo_lukukausia, v.arvosana, v.asteikko,
 vrk.sukunimi, vrk.etunimet, vrk.sukupuoli, vrk.aidinkieli, vrk.lahiosoite, vrk.postinumero, vrk.postitoimipaikka, vrk.kotim_osoitt_muuttopaiva,
 vrk.kotikunta, vrk.kotikunnan_nimi, vrk.vakinainen_ulkomainen_osoite, vrk.ulkomaisen_osoitteen_paikkakunta, vrk.ulkomaisen_asuinvaltion_postinimi,
 vrk.asuinvaltio, vrk.ulkom_asuinvaltion_nimi, vrk.ulkomaille_muuton_pv,
 fonecta.matkapuhelin, fonecta.haltijaliittyma, fonecta.yritysliittyma
FROM uraseuranta.virta_data v
 JOIN uraseuranta.vastaajatunnus vt ON v.id = vt.virta_data_id
 LEFT JOIN uraseuranta.vrk_data vrk ON v.henkilotunnus = vrk.henkilotunnus
 LEFT JOIN uraseuranta.fonecta_data fonecta ON v.id = fonecta.virta_data_id
WHERE v.uraseuranta_id = :uraseuranta_id AND v.tutkinnon_taso IN (:v*:tasot);

-- :name get-vastaajat :? :*
SELECT t.tunnus, v.oppilaitoskoodi, v.oppilaitos_nimi, v.tutkinnon_taso, v.aidinkieli FROM uraseuranta.vastaajatunnus t
 JOIN uraseuranta.virta_data v ON t.virta_data_id = v.id
WHERE v.uraseuranta_id = :uraseuranta;

-- :name save-file-status! :! :n
INSERT INTO uraseuranta.file (uraseuranta_id, filename, checksum, date)
 VALUES (:uraseuranta_id, :filename, :checksum, now());

-- :name delete-virta-data-by-hetu :! :n
DELETE FROM uraseuranta.virta_data WHERE henkilotunnus IN (:v*:hetus);

-- :name add-kyselykerta-mapping :! :n
INSERT INTO uraseuranta.kyselykerta (uraseuranta_id, kyselykertaid, oppilaitoskoodi, uraseuranta_tyyppi, vastaajia)
    VALUES (:uraseuranta_id, :kyselykertaid, :oppilaitoskoodi, :uraseuranta_tyyppi, :vastaajia)

-- :name get-kyselykerrat :? :*
SELECT * FROM uraseuranta.kyselykerta WHERE uraseuranta_id = :uraseuranta_id;
