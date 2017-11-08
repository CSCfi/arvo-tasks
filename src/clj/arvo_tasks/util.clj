(ns arvo-tasks.util)

(defn update-vals [map vals f]
  (reduce #(update-in % [%2] f) map vals))