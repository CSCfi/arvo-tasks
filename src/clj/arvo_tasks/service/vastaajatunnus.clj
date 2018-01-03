(ns arvo-tasks.service.vastaajatunnus
  (:require [arvo-tasks.db.core :as db]
            [arvo-tasks.config :refer [env]]))

(def allowed-chars "ACEFHJKLMNPRTWXY347")
(def tunnus-length 5)

(def start-val (int (reduce #(+ (Math/pow (count allowed-chars) %2) %1) (range tunnus-length))))

(defn get-nth [a m seed]
  (fn [x] (mod (+ a x seed) m)))

(def closest-prime 2476081)

(def lazyrange (drop 1 (iterate (get-nth 131943 closest-prime (:vastaajatunnus-seed env)) 0)))

(defn -generate-tunnus [val]
  (let [quot (quot val (count allowed-chars))
        char (nth allowed-chars (mod val (count allowed-chars)))]
    (str (when (< 0 quot) (-generate-tunnus (dec quot))) char)))

(defn generate-tunnus [val]
  (-generate-tunnus (+ val start-val)))

(defn luo-tunnuksia [n]
  (let [tunnuksia (:count (db/vastaajatunnus-count))]
    (->> lazyrange
         (drop tunnuksia)
         (take n)
         (map generate-tunnus))))

