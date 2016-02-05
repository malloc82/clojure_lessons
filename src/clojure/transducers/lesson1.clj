(ns transducers.lesson1
  (:use clojure.core clojure.repl)
  (:import [java.lang StringBuilder]))

(def data (vec (range 10)))

(defn -map [f coll]
  (reduce
   (fn [acc v]
     (conj acc (f v)))
   []
   coll))


(defn -filter [f coll]
  (reduce
   (fn [acc v]
     (if (f v)
       (conj acc v)
       acc))
   []
   coll))


(->> data
     (-map inc)
     (-filter odd?))

(defn -mapping [f job]
  (fn [acc v]
    (job acc (f v))))

(def rfn (-mapping inc conj))
(reduce rfn
        []
        data)

(defn -mapping [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (xf acc (f v))))))

(defn -filtering [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (if (f v)
         (xf acc v)
         acc)))))

;; (defmacro _mapping [f]
;;   `(fn [xf]
;;      (fn [acc v]
;;        (xf acc (~f v)))))

;; (defmacro _filtering [f]
;;   `(fn [xf]
;;      (fn [acc v]
;;        (if (~f v)
;;          (xf acc v)
;;          acc))))

;; (fn [acc v]
;;   ((fn [acc v]
;;      (if (f1 v)
;;        (xf acc v)
;;        acc)) acc (f2 v)))

;; (fn [xf]
;;   (fn [acc v]
;;     (xf acc (~f v))))

;; (def rfn (-mapping inc))

;; (reduce (rfn conj)
;;         []
;;         data)

(def xform (comp
            (-mapping int)
            (-mapping inc)
            (-filtering odd?)
            (-mapping char)))

(reduce (xform conj)
        []
        data)

;; ((xform +) 42 2)

;; Lesson 2

(defn string-rf
  ([^StringBuilder acc ^Character ch]
   (.append acc ch)))

(defn string-rf
  "Builder function"
  ([] (StringBuilder.))
  ([^StringBuilder sb]
   (.toString sb))
  ([^StringBuilder sb ^Character ch]
   (.append sb ch)))

(reduce (xform string-rf)
        (StringBuilder.)
        "Hello, world!")

(transduce xform string-rf "Hello, world")
(transduce xform conj "Hello, world")

;; Equvilent to transduce
(let [f (xform string-rf)]
  (f (reduce f (f) "Hello, world")))

(defn vec-trans
  ([] (transient []))
  ([acc] (persistent! acc))
  ([acc val]
   (conj! acc val )))

(transduce xform vec-trans "Hello, world")

;; (transducer <what you wanna do>
;;             <how you want to build result>
;;             <how you want to reduce initial collection>)

;; Lesson 3

(extend-protocol clojure.core.protocols/CollReduce
  java.io.InputStream
  (coll-reduce [this f init]
    (let [is ^java.io.InputStream this]
      (loop [acc init]
        (if (reduced? acc)
          @acc
          (let [ch (.read is)]
            (if (= ch -1)
              acc
              (recur (f acc ch)))))))))


(transduce (comp (map char)
                 (map #(Character/toUpperCase %))
                 (take 10)
                 (map int))
           +
           (java.io.ByteArrayInputStream.
            (.getBytes "Hello, world!")))
