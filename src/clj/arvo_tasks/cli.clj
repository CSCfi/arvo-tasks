(ns arvo-tasks.cli
  (:require [cli4clj [cli :as cli]]
            [clojure.string :as s]
            [arvo-tasks.service.uraseuranta :as uraseuranta]
            [arvo-tasks.db.core :refer [*db*] :as db]))

(defn lisaa-poiminta [path uraseuranta-id]
  (let [filename (last (s/split path #"/"))
        updated (uraseuranta/process-virta-data (str filename) (str path) uraseuranta-id "")]
    (println "Lisätty" updated "tunnusta")))

(defn lisaa-vrk [path uraseuranta-id password]
  (let [filename (last (s/split path #"/"))
        updated (uraseuranta/process-vrk-data (str filename) (str path) uraseuranta-id (str password))]
    (println "Lisätty VRK tiedot " updated "henkilölle")))

(defn lisaa-fonecta [path uraseuranta-id password]
  (let [filename (last (s/split path #"/"))
        updated (uraseuranta/process-fonecta-data (str filename) (str path) uraseuranta-id (str password))]
    (println "Lisätty matkapuhelinnumerot " updated "henkilölle")))

(defn lataa-fonecta-lista [uraseuranta-id password]
  (let [file (uraseuranta/get-fonecta-list uraseuranta-id (str password))]
    (println "Fonecta-lista tallennettu: " file)))

(defn listaa-uraseurannat []
  (let [uraseurannat (db/list-uraseuranta)]
    (doseq [uraseuranta uraseurannat]
      (println (:id uraseuranta) ":" (:name uraseuranta)))))

(defn luo-tupa-listat [uraseuranta-id password]
  (let [tiedostot (uraseuranta/get-tupa-lists uraseuranta-id (str password))]
    (println "TUPA-tiedostot luotu:")
    (doseq [tiedosto tiedostot]
      (println tiedosto))))

(defn luo-tunnukset [uraseuranta-id]
  (uraseuranta/luo-tunnukset! uraseuranta-id))

(defn luo-uraseuranta [nimi]
  (db/add-uraseuranta! {:name nimi})
  (println "Uraseuranta" nimi "luotu"))

(defn pad [s len]
  (apply str s (take (- len (count s)) (repeat " "))))

(defn print-uraseuranta-stats [stats]
  (println "Liitettävissä:")
  (doseq [row (sort-by :oppilaitos_nimi (:liitettavissa stats))]
    (println (format "%5d | %s | %s" (:vastaajia row) (pad (:oppilaitos_nimi row) 40) (:kyselykerta_nimi row))))
  (println "Ei kyselykertaa:")
  (doseq [row (sort-by :oppilaitos_nimi (:puuttuvat stats))]
    (println (format "%5d | %s" (:vastaajia row) (:oppilaitos_nimi row)))))

(defn listaa-kyselyt [uraseuranta-id]
  (let [stats (uraseuranta/get-uraseuranta-stats uraseuranta-id)]
    (print-uraseuranta-stats stats)))

(defn liita-tunnukset [uraseuranta-id]
  (let [liitetyt (uraseuranta/attach-tunnnus-to-kyselykerta uraseuranta-id)]
    (println "Liitetty" (reduce + (map :vastaajia liitetyt)) "tunnusta")))

(defn hae-vastaajat [uraseuranta-id]
  (println "Vastaajat: ")
  (println (uraseuranta/hae-vastaajat uraseuranta-id)))

(defn start []
  (cli/start-cli {:cmds
                    {:luo-uraseuranta {:fn luo-uraseuranta}
                     :lisaa-poiminta {:fn lisaa-poiminta}
                     :lisaa-vrk {:fn lisaa-vrk}
                     :lisaa-fonecta {:fn lisaa-fonecta}
                     :luo-tunnukset {:fn luo-tunnukset}
                     :lataa-fonecta {:fn lataa-fonecta-lista}
                     :listaa-uraseurannat {:fn listaa-uraseurannat}
                     :luo-tupa-listat {:fn luo-tupa-listat}
                     :listaa-kyselyt {:fn listaa-kyselyt}
                     :liita-tunnukset {:fn liita-tunnukset}
                     :hae-vastaajat {:fn hae-vastaajat}}}))

