(ns jp.nijohando.ext.async
  (:refer-clojure :exclude [take])
  (:require [clojure.core.async :as ca :include-macros true]
            [jp.nijohando.failable :as f :include-macros true]))

(defmacro take
  [takef altf ch opts]
  (if-some [to (some->> opts
                        (apply hash-map)
                        :timeout)]
    `(~altf ~ch ([v#] v#)
            (ca/timeout ~to) ([_#] (f/fail ::timeout)))
    `(~takef ~ch)))

(defmacro put
  [putf altf ch val opts]
  (if-some [to (some->> opts
                        (apply hash-map)
                        :timeout)]
    `(~altf [[~ch ~val]] ([v# _#] (or v# (f/fail ::closed)))
            (ca/timeout ~to) ([_#] (f/fail ::timeout)))
    `(-> (~putf ~ch ~val)
         (or (f/fail ::closed)))))

(defmacro <!
  [ch & opts]
  `(take ca/<! ca/alt! ~ch ~opts))

(defmacro <!!
  [ch & opts]
  `(take ca/<!! ca/alt!! ~ch ~opts))

(defmacro >!
  [ch val & opts]
  `(put ca/>! ca/alt! ~ch ~val ~opts))

(defmacro >!!
  [ch val & opts]
  `(put ca/>!! ca/alt!! ~ch ~val ~opts))

(defn close!
  [& chs]
  (doseq [ch chs]
    (ca/close! ch)))
