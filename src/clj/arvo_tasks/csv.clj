(ns arvo-tasks.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def valid-chars (apply str (map char (range 256))))

(defn clean-str [val]
  (->> (apply str val)
    (filter #(<= 32 (int %) 126))
    (apply str)
    (s/trim)))

(defn read-csv [filename]
  (with-open [reader (io/reader filename)]
    (let [file (csv/read-csv reader :separator \tab)
          header-row (map (comp keyword clean-str) (first file))
          data (doall (map #(zipmap header-row %) (rest file)))]
      data)))
