(ns arvo-tasks.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [arvo-tasks.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[arvo-tasks started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[arvo-tasks has shut down successfully]=-"))
   :middleware wrap-dev})
