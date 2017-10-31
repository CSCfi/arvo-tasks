(ns arvo-tasks.service.uraseuranta
  (:require [arvo-tasks.excel :as excel]
            [arvo-tasks.csv :as csv]
            [clojure.string :as s]
            [clojure.core.match :refer [match]]
            [arvo-tasks.db.uraseuranta :as db]
            [ring.util.http-response :as response]
            [arvo-tasks.service.vastaajatunnus :as vastaajatunnus])
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
    (luo-tunnukset! uraseuranta-id)
    (save-file-status uraseuranta-id filename path)
    (count data)))

(defn process-vrk-data [filename path uraseuranta-id password]
  (let [data (load-data filename path password)]
    (db/add-vrk-data! uraseuranta-id (filter (comp nil? :kuolinpaiva) data))
    (save-file-status uraseuranta-id filename path)
    (count data)))

(def fonecta-fields [:id  :sukunimi :etunimet :lahiosoite :postinumero :postitoimipaikka :kotikunnan_nimi
                     :vakinainen_ulkomainen_osoite :ulkomaisen_osoitteen_paikkakunta :ulkomaisen_asuinvaltion_postinimi
                     :ulkom_asuinvaltion_nimi :puhelin1 :puhelin2])

(defn has-valid-address [data]
  (or
    (some? (and (:lahiosoite data) (or (:postinumero data) (:postitoimipaikka data))))
    (every? #(some? (data %)) [:vakinainen_ulkomainen_osoite :ulkomaisen_osoitteen_paikkakunta :ulkomaisen_asuinvaltion_postinimi])))

(defn get-fonecta-list [uraseuranta-id password]
  (let [virta-data (->> (db/get-data-for-fonecta uraseuranta-id)
                       (filter has-valid-address)
                       (sort-by :id))]
    (excel/save-excel virta-data fonecta-fields "fonecta" password)))
