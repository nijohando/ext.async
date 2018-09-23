(ns prj.user
  (:require [clojure.core.async :as ca]
            [jp.nijohando.ext.async :as xa]
            [jp.nijohando.prj.test :as prj-test]
            [prj.cljs]))

(defn test-clj
  []
  (prj-test/run-tests 'jp.nijohando.ext.async-test-clj))

(defn test-cljs
  []
  (prj.cljs/test-cljs))

(defn repl-cljs
  []
  (prj.cljs/repl-cljs))
