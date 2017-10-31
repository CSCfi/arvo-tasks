(ns user
  (:require [mount.core :as mount]
            [arvo-tasks.figwheel :refer [start-fw stop-fw cljs]]
            arvo-tasks.core))

(defn start []
  (mount/start-without #'arvo-tasks.core/repl-server))

(defn stop []
  (mount/stop-except #'arvo-tasks.core/repl-server))

(defn restart []
  (stop)
  (start))


