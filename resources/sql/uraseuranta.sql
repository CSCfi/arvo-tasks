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

-- :name insert-vrk-data :! :n
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

-- :name get-data-for-fonecta :? :*
SELECT virta.id, vrk.sukunimi, vrk.etunimet, vrk.lahiosoite, vrk.postinumero, vrk.postitoimipaikka, vrk.kotikunnan_nimi,
 vrk.vakinainen_ulkomainen_osoite, vrk.ulkomaisen_osoitteen_paikkakunta, vrk.ulkomaisen_asuinvaltion_postinimi, vrk.ulkom_asuinvaltion_nimi
FROM uraseuranta.virta_data virta
 JOIN uraseuranta.vrk_data vrk ON virta.henkilotunnus = vrk.henkilotunnus
WHERE virta.uraseuranta_id = :id AND vrk.uraseuranta_id = :id;

-- :name save-file-status! :! :n
INSERT INTO uraseuranta.file (uraseuranta_id, filename, checksum, date)
 VALUES (:uraseuranta_id, :filename, :checksum, now());
