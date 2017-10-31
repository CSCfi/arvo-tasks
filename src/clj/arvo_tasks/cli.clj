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

(defn lataa-fonecta-lista [uraseuranta-id password]
  (let [file (uraseuranta/get-fonecta-list uraseuranta-id (str password))]
    (println "Fonecta-lista tallennettu: " file)))

(defn listaa-uraseurannat []
  (let [uraseurannat (db/list-uraseuranta)]
    (doseq [uraseuranta uraseurannat]
      (println (:id uraseuranta) ":" (:name uraseuranta)))))

(defn luo-uraseuranta [nimi]
  (db/add-uraseuranta! {:name nimi})
  (println "Uraseuranta" nimi "luotu"))

(defn start []
  (cli/start-cli {:cmds
                  {:lisaa-poiminta {:fn lisaa-poiminta}
                   :lisaa-vrk {:fn lisaa-vrk}
                   :lataa-fonecta {:fn lataa-fonecta-lista}
                   :listaa-uraseurannat {:fn listaa-uraseurannat}
                   :luo-uraseuranta {:fn luo-uraseuranta}}}))
