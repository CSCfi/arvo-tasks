(ns arvo-tasks.service.uraseuranta
  (:require [arvo-tasks.excel :as excel]
            [arvo-tasks.csv :as csv]
            [clojure.string :as s]
            [clojure.core.match :refer [match]]
            [arvo-tasks.db.uraseuranta :as db]
            [ring.util.http-response :as response]
            [arvo-tasks.service.vastaajatunnus :as vastaajatunnus]
            [arvo-tasks.util :refer :all]
            [clj-time.format :as f]
            [arvo-tasks.integration.arvo :as arvo])
  (:import (java.security MessageDigest)
           (javassist.bytecode ByteArray)))

(defn md5 [^ByteArray bytes]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm bytes)]
    (format "%032x" (BigInteger. 1 raw))))

(defn read-bytes [^String path]
  (let [file (java.io.File. path)
        bytearr (byte-array (.length file))
        is (java.io.FileInputStream. file)]
    (.read is bytearr)
    (.close is)
    bytearr))

(defn save-file-status [uraseuranta-id filename path]
  (db/save-file-status uraseuranta-id filename (md5 (read-bytes path))))

(defn load-data [filename tempfile password]
  (match [(last (s/split filename #"\."))]
         ["csv"] (csv/read-csv tempfile)
         ["xlsx"] (excel/load-excel tempfile password)))

(defn luo-tunnukset! [uraseuranta-id]
  (let [ids (map #(select-keys % [:id]) (db/get-virta-data uraseuranta-id))
        tunnukset (vastaajatunnus/luo-tunnuksia (count ids))
        vastaajatunnus-data (map #(assoc %1 :tunnus %2) ids tunnukset)]
    (db/add-tunnukset vastaajatunnus-data)))

(defn process-virta-data [filename path uraseuranta-id password]
  (let [data (load-data filename path password)]
    (db/add-virta-data! uraseuranta-id data)
    (save-file-status uraseuranta-id filename path)
    (count data)))

(defn process-vrk-data [filename path uraseuranta-id password]
  (let [data (load-data filename path password)
        {valid-data true unwanted-data false} (group-by (comp nil? :kuolinpaiva) data)]
    (db/remove-unwanted-virta-data uraseuranta-id unwanted-data)
    (db/add-vrk-data! uraseuranta-id valid-data)
    (save-file-status uraseuranta-id filename path)
    (count valid-data)))

(defn process-fonecta-data [filename path uraseuranta-id password]
  (let [data (load-data filename path password)
        filtered (->> data
                  (filter (comp some? :matkapuhelin))
                  (map #(select-keys % [:id :matkapuhelin :robinson :haltijaliittyma :yritysliittyma])))]
    (db/add-fonecta-data! filtered)
    (save-file-status uraseuranta-id filename path)
    (count filtered)))

(def fonecta-fields [:id  :sukunimi :etunimet :lahiosoite :postinumero :postitoimipaikka :kotikunnan_nimi
                     :vakinainen_ulkomainen_osoite :ulkomaisen_osoitteen_paikkakunta :ulkomaisen_asuinvaltion_postinimi
                     :ulkom_asuinvaltion_nimi])

(defn has-valid-address [data]
  (or
    (some? (and (:lahiosoite data) (or (:postinumero data) (:postitoimipaikka data))))
    (every? #(some? (data %)) [:vakinainen_ulkomainen_osoite :ulkomaisen_osoitteen_paikkakunta :ulkomaisen_asuinvaltion_postinimi])))

(defn get-fonecta-list [uraseuranta-id password]
  (let [virta-data (->> (db/get-data-for-fonecta uraseuranta-id)
                       (filter has-valid-address)
                       (sort-by :id))]
    (excel/save-excel virta-data fonecta-fields "fonecta" password)))

(def tupa-fields [:tunnus :oppilaitoskoodi :oppilaitos_nimi :valmistumisvuosi :valmistumisajankohta :opiskelijatunnus,
                  :sukupuoli :ika_valmistuessa :kansalaisuus :aidinkieli :koulutusalakoodi :koulutusala :paaaine :tutkinnon_taso :tutkinto_koulutuskoodi :tutkinto_nimi
                  :laajuus :valintavuosi :asuinkunta_koodi :asuinkunta_nimi :kirjoilla_olo_kuukausia :lasnaolo_lukukausia :arvosana :asteikko
                  :sukunimi :etunimet :sukupuoli :aidinkieli :lahiosoite :postinumero :postitoimipaikka :kotim_osoitt_muuttopaiva
                  :kotikunta :kotikunnan_nimi :vakinainen_ulkomainen_osoite :ulkomaisen_osoitteen_paikkakunta :ulkomaisen_asuinvaltion_postinimi :asuinvaltio :ulkom_asuinvaltion_nimi :ulkomaille_muuton_pv
                  :matkapuhelin :robinson :haltijaliittyma :yritysliittyma])

(def date-format (f/formatter "yyyy-MM-dd"))

(defn format-dates [row]
  (update-vals row [:valmistumisajankohta :kotim_osoitt_muuttopaiva :valintavuosi :ulkomaille_muuton_pv]
    (fn [val]
      (when (some? val)
        (f/unparse date-format val)))))

(defn get-tupa-list [uraseuranta-id tasot nimi password]
  (let [data (->> (db/get-tupa-list uraseuranta-id tasot)
                 (map format-dates))]
    (excel/save-excel data tupa-fields nimi password)))

(defn list-active-questionnaires []
  (arvo/get-uraseuranta-questionnaires))

(defn uraseuranta-tyyppi [vastaaja]
  (if (= "7" (:tutkinnon_taso vastaaja))
      "tohtorit"
      "maisterit"))

(defn get-kyselykerta [kyselyt vastaaja]
  (->> kyselyt
      (filter #(and (= (:oppilaitoskoodi %) (:oppilaitoskoodi vastaaja))
                    (= (:uraseuranta_tyyppi %) (uraseuranta-tyyppi vastaaja))))
      first
      :kyselykertaid))

(defn get-kieli [vastaaja]
  (if (some #(= (:aidinkieli vastaaja) %) ["fi" "sv" "en"])
    (:aidinkieli vastaaja)
    "en"))

(defn format-vastaaja [kyselykerrat vastaaja]
  (let [kyselykertaid (get-kyselykerta kyselykerrat vastaaja)
        kieli (get-kieli vastaaja)]
    (-> vastaaja
      (assoc :kyselykertaid kyselykertaid :kieli kieli :uraseuranta_tyyppi (uraseuranta-tyyppi vastaaja)))))

(defn get-missing-questionnaires [vastaajat]
  (let [grouped (group-by :oppilaitoskoodi vastaajat)
        missing-count (map (fn [[k v]] {:oppilaitoskoodi k :oppilaitos_nimi (:oppilaitos_nimi (first v)) :vastaajia (count v)}) grouped)]
    missing-count))

(defn find-kyselykerta [kyselykerrat kyselykertaid vastaajia]
  (let [kyselykerta (first (filter #(= (:kyselykertaid %) kyselykertaid) kyselykerrat))]
    (assoc kyselykerta :vastaajia vastaajia)))

(defn generate-uraseuranta-stats [kyselykerrat vastaajatunnukset]
  (let [grouped (group-by :kyselykertaid vastaajatunnukset)
        available (map (fn [[k v]]
                         (find-kyselykerta kyselykerrat k (count v)))
                       (dissoc grouped nil))]
    {:liitettavissa available
     :puuttuvat (get-missing-questionnaires (get grouped nil))}))

(defn get-uraseuranta-stats [uraseuranta-id]
  (let [kyselykerrat (arvo/get-uraseuranta-questionnaires)
        vastaajat (db/get-vastaajat uraseuranta-id)
        vastaajatunnukset (map #(format-vastaaja kyselykerrat %) vastaajat)]
    (generate-uraseuranta-stats kyselykerrat vastaajatunnukset)))

(defn attach-tunnnus-to-kyselykerta [uraseuranta-id]
  (let [kyselykerrat (arvo/get-uraseuranta-questionnaires)
        vastaajat (db/get-vastaajat uraseuranta-id)
        vastaajatunnukset (map #(format-vastaaja kyselykerrat %) vastaajat)
        liitetyt (arvo/liita-vastaajatunnukset! (filter #(some? (:kyselykertaid %)) vastaajatunnukset))]
    (db/lisaa-kyselykerrat liitetyt uraseuranta-id)
    liitetyt))

(defn hae-kyselykerran-vastaajat [kyselykerta]
  (let [vastaajat (arvo/hae-vastaajat (:kyselykertaid kyselykerta))]
    (map #(assoc {} :tunnus % :tyyppi (:uraseuranta_tyyppi kyselykerta)) vastaajat)))

(defn hae-vastaajat [uraseuranta-id]
  (let [kyselykerrat (db/hae-kyselykerrat uraseuranta-id)
        vastaajat (mapcat hae-kyselykerran-vastaajat kyselykerrat)]
    (excel/save-excel vastaajat [:tunnus :tyyppi] "vastanneet")))

(defn get-tupa-lists [uraseuranta-id password]
  (let [tohtorit (get-tupa-list uraseuranta-id ["7"] "Uraseuranta-tohtorit" password)
        maisterit (get-tupa-list uraseuranta-id ["2" "4"] "Uraseuranta-maisterit" password)]
    [maisterit tohtorit]))