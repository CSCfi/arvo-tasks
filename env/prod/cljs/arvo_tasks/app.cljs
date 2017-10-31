(ns arvo-tasks.app
  (:require [arvo-tasks.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
