(ns transducers.playground
  (:use clojure.core clojure.repl)
  (:require [clojure.string :as str]
            [clojure.core.async :as a :refer [go go-loop chan <! <!! >!! >! close!]])
  (:import [java.lang StringBuilder]))

(transduce (comp (map (fn [s] (str/join ["-" s])))
                 (map (fn [s] (str/join ["=" s]))))
           conj
           ["a" "b" "c"])

(transduce (comp (map #(str/join ["-" (str %)])))
           (fn
             ([] [])
             ([v] (conj v))
             ([acc v]
              (if (= v "-6")
                (reduced (conj acc v))
                (conj acc v))))
           (range 10))


(extend-protocol clojure.core.protocols/CollReduce
  clojure.core.async.impl.channels.ManyToManyChannel
  (coll-reduce [this f init]
    (let [ch ^clojure.core.async.impl.channels.ManyToManyChannel this]
      (go-loop [acc init]
        (if (reduced? acc)
          @acc
          (let [val (<! ch)]
            (if (= val -1)
              acc
              (recur (f acc val)))))))))


(defn test-fn [ch]
  (transduce (comp (map (fn [s] (str/join ["-" (str s)])))
                        (map (fn [s] (str/join ["=" (str s)]))))
                  conj
                  ch))

;; (defn test-fn2 [ch-in]
;;   (let [ch-out (chan 10)]
;;     (go
;;       (transduce (comp (map (fn [s] (str/join ["-" (str s)])))
;;                         (map (fn [s] (str/join ["=" (str s)]))))
;;                  conj
;;                  ch-out
;;                  ch-in))
;;     ch-out))
