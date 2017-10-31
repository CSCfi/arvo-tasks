(ns arvo-tasks.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [arvo-tasks.layout :refer [error-page]]
            [arvo-tasks.routes.home :refer [home-routes]]
            [arvo-tasks.routes.services :refer [uraseuranta-routes]]
            [compojure.route :as route]
            [arvo-tasks.env :refer [defaults]]
            [mount.core :as mount]
            [arvo-tasks.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'uraseuranta-routes
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
