(ns jp.nijohando.ext.async-test-clj
  (:require [clojure.test :as t :refer [run-tests is deftest testing]]
            [clojure.core.async :as ca]
            [jp.nijohando.ext.async :as xa]
            [jp.nijohando.failable :as f]
            [jp.nijohando.deferable :as d]))

(deftest <!!
  (testing "A value can be read from the channel"
    (d/do*
      (let [c (ca/chan 1)
            _ (d/defer (ca/close! c))]
        (ca/>!! c "foo")
        (is (= "foo" (xa/<!! c))))))
  (testing "A nil is returned if the channel is closed"
    (d/do*
      (let [c (ca/chan 1)]
        (ca/close! c)
        (is (nil? (xa/<!! c))))))
  (testing "Read timeout is enabled with the option"
    (d/do*
      (let [c (ca/chan 1)
            _ (d/defer (ca/close! c))
            x (xa/<!! c :timeout 500)]
        (is (f/fail? x))
        (is (= :jp.nijohando.ext.async/timeout @x))))))

(deftest <!
  (testing "A value can be read from the channel"
    (d/do*
      (let [c (ca/chan 1)
            _ (d/defer (ca/close! c))]
        (-> (ca/go
              (ca/>! c "foo")
              (is (= "foo" (xa/<! c))))
            (ca/<!!)))))
  (testing "A nil is returned if the channel is closed"
    (d/do*
      (let [c (ca/chan)]
        (ca/close! c)
        (-> (ca/go
              (is (nil? (xa/<! c))))
            (ca/<!!)))))
  (testing "A failure is returned when getting a read timeout"
    (d/do*
      (let [c (ca/chan)
            _ (d/defer (ca/close! c))]
        (-> (ca/go
              (let [x (xa/<! c :timeout 500)]
                (is (f/fail? x))
                (is (= xa/failure-timeout @x))))
            (ca/<!!))))))

(deftest >!!
  (testing "A value can be written to the channel"
    (d/do*
      (let [c (ca/chan 1)
            _ (d/defer (ca/close! c))]
        (xa/>!! c "foo")
        (is (= "foo" (ca/<!! c))))))
  (testing "A failure is returned if the cnannel is closed"
    (d/do*
      (let [c (ca/chan 1)]
        (ca/close! c)
        (let [x (xa/>!! c "foo")]
          (is (f/fail? x))
          (is (= xa/failure-closed @x))))))
  (testing "A failure is returned when getting a write timeout"
    (d/do*
      (let [c (ca/chan)
            _ (d/defer (ca/close! c))]
        (let [x (xa/>!! c "foo" :timeout 500)]
          (is (f/fail? x))
          (is (= xa/failure-timeout @x)))))))

(deftest >!
  (testing "A value can be written to the channel"
    (d/do*
      (let [c (ca/chan 1)
            _ (d/defer (ca/close! c))]
        (-> (ca/go
              (xa/>! c "foo")
              (is (= "foo" (ca/<! c))))
            (ca/<!!)))))
  (testing "A failure is returned if the cnannel is closed"
    (d/do*
      (let [c (ca/chan 1)]
        (ca/close! c)
        (-> (ca/go
              (let [x (xa/>! c "foo")]
                (is (f/fail? x))
                (is (= xa/failure-closed @x))))
            (ca/<!!)))))
  (testing "A failure is returned when getting a write timeout"
    (d/do*
      (let [c (ca/chan)
            _ (d/defer (ca/close! c))]
        (-> (ca/go
              (let [x (xa/>! c "foo" :timeout 500)]
                (is (f/fail? x))
                (is (= xa/failure-timeout @x))))
            (ca/<!!))))))

(deftest close!
  (testing "Channels can be closed at once"
    (let [chs (repeatedly 10 #(ca/chan))]
      (apply xa/close! chs)
      (doseq [ch chs]
        (is (nil? (ca/<!! ch)))))))
