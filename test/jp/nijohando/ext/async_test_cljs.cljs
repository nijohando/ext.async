(ns jp.nijohando.ext.async-test-cljs
  (:require [clojure.test :as t :refer-macros [is deftest testing async]]
            [jp.nijohando.ext.async :as xa :include-macros true]
            [jp.nijohando.failable :as f :include-macros true]
            [jp.nijohando.deferable :as d :include-macros true]
            [clojure.core.async :as ca :include-macros true]))

(deftest <!-read-value
  (testing "A value can be read from the channel"
    (async end
      (d/do** done
        (let [c (ca/chan 1)
              _ (d/defer (ca/close! c))]
          (ca/go
            (ca/>! c "foo")
            (is (= "foo" (xa/<! c)))
            (done)
            (end)))))))

(deftest <!-closed-channel
  (testing "A nil is returned if the channel is closed"
    (async end
      (d/do** done
        (let [c (ca/chan 1)]
          (ca/close! c)
          (ca/go
            (is (nil? (xa/<! c)))
            (done)
            (end)))))))

(deftest <!-with-timeout-option
  (testing "Read timeout is enabled with the option"
    (async end
      (d/do** done
        (let [c (ca/chan)
              _ (d/defer (ca/close! c))]
          (ca/go
            (let [x (xa/<! c :timeout 300)]
              (is (f/fail? x))
              (is (= ::xa/timeout @x)))
            (done)
            (end)))))))

(deftest >!-write-value
  (testing "A value can be written to the channel"
    (async end
      (d/do** done
        (let [c (ca/chan 1)
              _ (d/defer (ca/close! c))]
          (ca/go
            (xa/>! c "foo")
            (is (= "foo" (ca/<! c)))
            (done)
            (end)))))))

(deftest >!-closed-channel
  (testing "A failure is returned if the cnannel is closed"
    (async end
      (d/do** done
        (let [c (ca/chan)]
          (ca/close! c)
          (ca/go
            (let [x (xa/>! c "foo")]
              (is (f/fail? x))
              (is (= ::xa/closed @x)))
            (done)
            (end)))))))

(deftest >!-with-timeout-option
  (testing "A failure is returned when getting a write timeout"
    (async end
      (d/do** done
        (let [c (ca/chan)
              _ (d/defer (ca/close! c))]
          (ca/go
            (let [x (xa/>! c "foo" :timeout 300)]
              (is (f/fail? x))
              (is (= ::xa/timeout @x)))
            (done)
            (end)))))))

(deftest close!
  (testing "Channels can be closed at once"
    (async end
      (let [chs (repeatedly 10 #(ca/chan))]
        (apply xa/close! chs)
        (ca/go
          (doseq [ch chs]
            (is (nil? (ca/<! ch))))
          (end))))))
