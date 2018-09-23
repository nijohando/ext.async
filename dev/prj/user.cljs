(ns prj.user
  (:require [clojure.core.async :as ca :include-macros true]
            [jp.nijohando.ext.async :as xa :include-macros true]
            [jp.nijohando.ext.async-test-cljs]
            [cljs.test :refer-macros [run-tests]]))

(defn test-cljs
  []
  (run-tests 'jp.nijohando.ext.async-test-cljs))
