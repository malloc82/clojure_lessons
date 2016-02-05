(ns oop.lesson1
  (:use clojure.core
        [clojure.pprint :only [pprint]])
  (:require [clojure.core.async
             :as async
             :refer [go <! >! chan close! <!! >!! thread]]))

"A schematic paradigm for computer programming in which the linear
concepts of procedures and tasks are replaced by the concepts of
objects and messages. An object includes a packages of data and a
description of the operations that can be performed on that data.
A message specifies one of the operations, but unlike a procedure,
does not describe how the operation should be carried our. C++ is
an example of an object-oriented programming langaues."

(defprotocol IObject
  (send-msg [this msg args]))

(extend-protocol IObject
  clojure.lang.PersistentArrayMap
  (send-msg [obj msg-type args]
    (let [op (get-in obj [:ops msg-type]
                     (get-in obj [:ops :default]))]
      (apply op obj args))))

(defn get-methods [obj]
  (keys (:ops obj)))

(def my-object
  {:ops {:add-score (fn [state score]
                      (update-in state [:data :scores] conj score))
         :sum-scores (fn [state]
                       (apply + (get-in state [:data :scores])))
         :default (fn [state & rest]
                    (println "CANNOT EXECUTE"))}
   :data {:scores []}})

(def my-object2
  (assoc-in my-object [:ops :avg-score ]
            (fn [state]
              (let [scores (get-in state [:data :scores])]
                (/ (apply + scores)
                   (count scores))))))

#_(defn send-msg [obj msg-type & args]
  (let [op (get-in obj [:ops msg-type])]
    (apply op obj args)))

(let [op :add-score]
  (-> my-object
      (send-msg op [42])
      (send-msg op [1])
      (send-msg :sum-score [])))

(let [op :add-score]
  (-> my-object2
    (send-msg op [42])
    (send-msg op [1])
    (send-msg :avg-score [])
    #_int))

;; clojure version

;; This OOP style gives away being data driven
(defprotocol IMyObject
  (add-score [this score])
  (sum-scores [this]))

(defrecord MyObject [scores]
  IMyObject
  (add-score [this score]
    (assoc this :scores (conj scores score)))
  (sum-scores [this]
    (apply + scores)))

(-> (->MyObject [])
    (add-score 42)
    (add-score 1))


;; oop async style
;; couple object with states and commands


(defn print-obj [prefix]
  (let [c (chan)]
    (thread
      (loop []
        (when-some [[cmd data] (<!! c)]
          (case cmd
            :print (do (print prefix data)
                       (recur))
            (recur)))))
    c))

(let [o (print-obj "Printed -> ")]
  (>!! o [:print 42])
  (close! o))

(defn scoring-obj []
  (let [c (chan)]
    (thread
      ;; "Actor" block
      (loop [scores []]
        (when-some [[cmd data] (<!! c)]
          (println "Got Command " cmd data)
          (case cmd
            :add-score (recur (conj scores data))
            :sum-scores (do (>!! data (apply + scores))
                            (recur scores))
            (do
              (println "Bad Command : " cmd data)
              (recur scores))))))
    c))

(let [o (scoring-obj)]
  (>!! o [:add-score 42])
  (>!! o [:add-score 1])
  (let [c (chan)]
    (>!! o [:sum-scores c])
    (println "Summed: " (<!! c)))
  (close! o))

;; dispatch

(let [impls (atom {})]
  (defn add-scores [this val]
    ((@impls (type this)) this val))
  (defn extend-add-scores [tp fn]
    (swap! impls assoc tp fn)))

(extend-add-scores (type []) conj)
(extend-add-scores (type []) (fn [acc i]
                               (assoc acc i i)))

(add-scores {} 1)

;; defprotocol, function itself owns the method, not object
