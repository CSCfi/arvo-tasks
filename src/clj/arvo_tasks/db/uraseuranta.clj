(ns arvo-tasks.db.uraseuranta
  (:require [arvo-tasks.db.core :refer [*db*] :as db]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.java.jdbc :as jdbc]
            [clojure.math.numeric-tower :refer [expt]]
            [clojure.string :as s]))

(def virta-fields [:uraseuranta_id :henkilotunnus :oppilaitoskoodi :oppilaitos_nimi, :valmistumisvuosi :opiskelijatunnus, :sukupuoli :ika_valmistuessa
                   :kansalaisuus :aidinkieli :koulutusalakoodi :koulutusala, :aine, :tutkinnon_taso :tutkinto_koulutuskoodi :tutkinto_nimi
                   :laajuus :valintavuosi :asuinkunta_koodi :asuinkunta_nimi :kirjoilla_olo_kuukausia :lasnaolo_lukukausia :arvosana :asteikko])

(def vrk-fields [:sukupuoli :kotikunnan_nimi :ulkomaisen_osoitteen_paikkakunta :etunimet :postitoimipaikka :ulkom_asuinvaltion_nimi
                 :aidinkieli :sukunimi :postinumero :vakinainen_ulkomainen_osoite :henkilotunnus :asuinvaltio :kotikunta
                 :kotim_osoitt_muuttopaiva :ulkomaille_muuton_pv :ulkomaisen_asuinvaltion_postinimi :lahiosoite :kuolinpaiva])

(defn defaults [fields] (zipmap fields (repeat nil)))

(defn add-defaults [uraseuranta-id fields row]
  (->> row
      (merge {:uraseuranta_id uraseuranta-id})
      (merge (defaults fields))))

(defn update-vals [map vals f]
  (reduce #(update-in % [%2] f) map vals))

(defn format-virta-values [row]
  (update-vals row [:valmistumisajankohta :valintavuosi] c/to-sql-date))

(def time-formatter (f/formatter "yyyyMMdd"))

(defn fix-zero-day [d]
  (if (s/ends-with? (str d) "00")
    (str (inc d))
    (str d)))

(defn format-vrk-values [row]
  (update-vals row [:kotim_osoitt_muuttopaiva :ulkomaille_muuton_pv :kuolinpaiva]
    (fn [field]
      (when (some? field)
        (->> field
             int
             fix-zero-day
             (f/parse time-formatter)
             c/to-sql-date)))))

(defn add-virta-data! [uraseuranta-id virta-data]
  (let [format (comp (partial add-defaults uraseuranta-id virta-fields) format-virta-values)
        data (map format virta-data)]
    (println "Inserting virta data: " (first data))
    (jdbc/with-db-transaction [tx *db*]
      (doseq [row data]
        (db/insert-virta-data! tx row)))))

(defn add-vrk-data! [uraseuranta-id vrk-data]
  (let [format (comp (partial add-defaults uraseuranta-id vrk-fields) format-vrk-values)
        data (map format vrk-data)]
    (println "Inserting vrk data: " (first data))
    (jdbc/with-db-transaction [tx *db*]
      (doseq [row data]
        (db/insert-vrk-data tx row)))))

(defn get-virta-data [uraseuranta-id]
  (db/get-virta-data {:id uraseuranta-id}))

(defn add-tunnukset [tunnukset]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [tunnus tunnukset]
      (db/insert-vastaajatunnus! tx tunnus))))

(defn get-data-for-fonecta [uraseuranta-id]
  (db/get-data-for-fonecta {:id uraseuranta-id}))

(defn save-file-status [uraseuranta-id filename checksum]
  (db/save-file-status! {:uraseuranta_id uraseuranta-id :filename filename :checksum checksum}))
