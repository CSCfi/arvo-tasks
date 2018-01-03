(ns arvo-tasks.db.uraseuranta
  (:require [arvo-tasks.db.core :refer [*db*] :as db]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.java.jdbc :as jdbc]
            [clojure.math.numeric-tower :refer [expt]]
            [clojure.string :as s]
            [arvo-tasks.util :refer :all]))

(def virta-fields [:uraseuranta_id :henkilotunnus :oppilaitoskoodi :oppilaitos_nimi, :valmistumisvuosi :opiskelijatunnus, :sukupuoli :ika_valmistuessa
                   :kansalaisuus :aidinkieli :koulutusalakoodi :koulutusala, :aine, :tutkinnon_taso :tutkinto_koulutuskoodi :tutkinto_nimi
                   :laajuus :valintavuosi :asuinkunta_koodi :asuinkunta_nimi :kirjoilla_olo_kuukausia :lasnaolo_lukukausia :arvosana :asteikko])

(def vrk-fields [:sukupuoli :kotikunnan_nimi :ulkomaisen_osoitteen_paikkakunta :etunimet :postitoimipaikka :ulkom_asuinvaltion_nimi
                 :aidinkieli :sukunimi :postinumero :vakinainen_ulkomainen_osoite :henkilotunnus :asuinvaltio :kotikunta
                 :kotim_osoitt_muuttopaiva :ulkomaille_muuton_pv :ulkomaisen_asuinvaltion_postinimi :lahiosoite :kuolinpaiva])

(def fonecta-fields [:matkapuhelin :yritysliittyma :haltijaliittyma])

(defn defaults [fields] (zipmap fields (repeat nil)))

(defn add-defaults [uraseuranta-id fields row]
  (->> row
      (merge {:uraseuranta_id uraseuranta-id})
      (merge (defaults fields))))

(defn format-virta-values [row]
  (update-vals row [:valmistumisajankohta :valintavuosi] c/to-sql-date))

(def time-formatter (f/formatter "yyyyMMdd"))

(defn fix-zero-day [d]
  (if (s/ends-with? (str d) "00")
    (str (inc d))
    (str d)))

(defn format-vrk-values [row]
  (update-vals row [:kotim_osoitt_muuttopaiva :ulkomaille_muuton_pv]
    (fn [field]
      (when (some? field)
        (->> field
             int
             fix-zero-day
             (f/parse time-formatter)
             c/to-sql-date)))))

(defn format-fonecta-values [row]
  (update-vals row [:yritysliittyma :haltijaliittyma] some?))

(defn add-virta-data! [uraseuranta-id virta-data]
  (let [format (comp (partial add-defaults uraseuranta-id virta-fields) format-virta-values)
        data (map format virta-data)]
    (jdbc/with-db-transaction [tx *db*]
      (doseq [row data]
        (db/insert-virta-data! tx row)))))

(defn add-vrk-data! [uraseuranta-id vrk-data]
  (let [format (comp (partial add-defaults uraseuranta-id vrk-fields) format-vrk-values)
        data (map format vrk-data)]
    (jdbc/with-db-transaction [tx *db*]
      (doseq [row data]
        (db/insert-vrk-data! tx row)))))

(defn add-fonecta-data! [fonecta-data]
  (let [data (map format-fonecta-values fonecta-data)]
    (jdbc/with-db-transaction [tx *db*]
      (doseq [row data]
        (db/insert-fonecta-data! tx row)))))

(defn remove-unwanted-virta-data [uraseuranta-id data]
  (let [hetus (map :henkilotunnus data)]
    (db/delete-virta-data-by-hetu {:hetus hetus})))

(defn get-virta-data [uraseuranta-id]
  (db/get-virta-data {:id uraseuranta-id}))

(defn get-tupa-list [uraseuranta-id tasot]
  (db/get-tupa-list {:uraseuranta_id uraseuranta-id :tasot tasot}))

(defn add-tunnukset [tunnukset]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [tunnus tunnukset]
      (db/insert-vastaajatunnus! tx tunnus))))

(defn get-data-for-fonecta [uraseuranta-id]
  (db/get-data-for-fonecta {:id uraseuranta-id}))

(defn get-vastaajat [uraseuranta-id]
  (db/get-vastaajat {:uraseuranta uraseuranta-id}))

(defn save-file-status [uraseuranta-id filename checksum]
  (db/save-file-status! {:uraseuranta_id uraseuranta-id :filename filename :checksum checksum}))

(defn lisaa-kyselykerrat [kyselykerrat uraseuranta-id]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [kyselykerta kyselykerrat]
      (db/add-kyselykerta-mapping (assoc kyselykerta :uraseuranta_id uraseuranta-id)))))

(defn hae-kyselykerrat [uraseuranta-id]
  (db/get-kyselykerrat {:uraseuranta_id uraseuranta-id}))