(ns core-async.lensson1
  (:require [clojure.core.async :as async :refer [<!! >!!]]))

;; Episode 1 pipelines

(let [c (async/chan 10)]
  (>!! c 42)
  (<!! (pipeline< [4 inc
                   ;; 1 inc
                   ;; 2 dec
                   ;; 3 str
                   ]
                  c)))

(defn to-proc< [in]
  (let [out (async/chan 1)]
    (async/pipe in out)
    out ))

(defn pipeline< [desc c]
  "Take initial value from c, apply list of pairs from desc to it.
   Each pair is [n : int,  f : function] format.
   n is number of parallel process to be created, f is the function to be applied.

   ** Note ** map< is bad here, it make the parallel prcess sequential."

  (let [p (partition 2 desc)]
    (reduce
     (fn [prev-c [n f]]
       (async/map< f prev-c))
     c
     p)))

(defn pipeline< [desc c]
  "Take initial value from c, apply list of pairs from desc to it.
   Each pair is [n : int,  f : function] format.
   n is number of parallel process to be created, f is the function to be applied."
  (let [p (partition 2 desc)]
    (reduce
     (fn [prev-c [n f]]
       (-> (for [_ (range n)]
             (-> (async/map< f prev-c)
                 to-proc<))
           async/merge))
      c
     p)))

(defn new-agent [cmd-port chans]
  (let [local-chans (vec chans)]
    (async/thread
      (loop []
        ;; check input command
        (let [[v p] (async/alts! [cmd-port (async/timeout 0.001)])])
        ;; execute the body

        (recur)))))

