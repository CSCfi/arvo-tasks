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

(defn start []
  (cli/start-cli {:cmds
                  {:luo-uraseuranta {:fn luo-uraseuranta}
                   :lisaa-poiminta {:fn lisaa-poiminta}
                   :lisaa-vrk {:fn lisaa-vrk}
                   :lisaa-fonecta {:fn lisaa-fonecta}
                   :luo-tunnukset {:fn luo-tunnukset}
                   :lataa-fonecta {:fn lataa-fonecta-lista}
                   :listaa-uraseurannat {:fn listaa-uraseurannat}
                   :luo-tupa-listat {:fn luo-tupa-listat}}}))

