(ns arvo-tasks.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [arvo-tasks.core-test]))

(doo-tests 'arvo-tasks.core-test)

