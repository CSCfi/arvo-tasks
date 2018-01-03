(ns arvo-tasks.routes.services
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [arvo-tasks.excel :as excel]
            [arvo-tasks.csv :as csv]
            [arvo-tasks.db.uraseuranta :as db]
            [arvo-tasks.integration.arvo :as arvo]
            [clojure.string :as s]
            [arvo-tasks.service.uraseuranta :as uraseuranta]))

(defn handle-upload [{:keys [filename size tempfile file data] :as params} uraseuranta-id]
  (println (str "Params: " params))
  (println (str "Uraseuranta-id: " uraseuranta-id))
  (println (str "handle-upload: Filename: " (or filename "null") " size: "
                (or size 0) " tempfile: " (str (or tempfile "null"))))
  (if (or (not filename) (= "" filename))
    (response/bad-request  {:status "ERROR"
                            :message "No file parameter sent"})
    (uraseuranta/process-virta-data filename tempfile uraseuranta-id"kissa13")))

(defroutes uraseuranta-routes
  (POST "/api/:id/upload" [id upload-file]
    (handle-upload upload-file (Integer/parseInt id))))