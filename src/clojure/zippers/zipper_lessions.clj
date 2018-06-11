(ns zippers.zippers1
  (:use clojure.core clojure.repl)
  (:require [clojure.zip :as z]))


;; tutorial 1
(def data [1
           [2 3 4]
           [[5 6]
            [7 8]
            [[9 10 11]]]
           12])


(-> (z/vector-zip data)
    (z/down)
    (z/right)
    (z/right)
    (z/down)
    (z/down)
    (z/replace 42)
    (z/edit + 41)
    (z/root))


(-> (z/vector-zip data)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    (z/next)
    ;; (z/branch?)
    ;; (z/node)
    (z/end?)
    )

(defn zip-map [f z]
  (loop [z z]
    (if (z/end? z) #_(= (z/next z) z)
        (z/root z)
        (if (z/branch? z)
          (recur (z/next z))
          (recur (-> z (z/edit f) z/next))))))

(zip-map inc (z/vector-zip data))
