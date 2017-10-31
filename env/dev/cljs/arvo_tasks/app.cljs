(ns ^:figwheel-no-load arvo-tasks.app
  (:require [arvo-tasks.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
