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

(defmacro _mapping [f1]
  `(fn [xf]
     (fn [acc v]
       (xf acc (~f1 v)))))

(defmacro _filtering [f2]
  `(fn [xf]
     (fn [acc v]
       (if (~f2 v)
         (xf acc v)
         acc))))


(fn [xf]
  (fn [acc v]
    (xf acc (~f1 v))))

(fn [acc v]
  ((fn [acc v]
     (if (f2 v)
       (xf acc v)
       acc)) acc (f1 v)))

(fn [xf]
  (fn [acc v]
    (xf acc (~f v))))

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

;; faster way would be to implement clojure.lang.IReduce

(transduce (comp (map char)
                 (map #(Character/toUpperCase %))
                 (take 10)
                 (map int))
           +
           (java.io.ByteArrayInputStream.
            (.getBytes "Hello, world!")))

;; Lesson 4: return more than one value

(def high-low
  (fn [xf]
    (fn
      ([] (xf))
      ([result] (xf result))
      ([result item]
       (-> result
           (xf (inc item))
           (xf (dec item)))
       ;; (xf (xf result (inc item))
       ;;     (dec item))
       ))))

(defn preserving-reduced
  [f1]
  #(let [ret (f1 %1 %2)]
     (if (reduced? ret)
       (reduced ret)
       ret)))

(def -cat ;; assume each item is collection
  (fn [xf]
    (let [pr (preserving-reduced xf)]
     (fn
       ([] (xf))
       ([result] (xf result))
       ([result coll]
        (reduce pr result coll))))))

(transduce high-low conj [1 2 3])

(transduce -cat conj [[1 2] [3 4] [5 6] [7 8] [9 10]])

(def print-stuff
 (map (fn [x] (println "-x-" x) x)))
(transduce (comp -cat print-stuff (take 3)) conj [[1 2] [3 4] [5 6] [7 8] [9 10]])

(transduce (comp high-low print-stuff (take 3)) conj [1 2 3 4 5 6 7])


;; Lesson 5: stateful transducer

(defn -take [n]
  (fn [xf]
    (let [left (atom n)]
      (fn
        ([] (xf))
        ([result] (xf result))
        ([result item]
         (if (> @left 0)
           (do (swap! left dec)
               (xf result item))
           (reduced result)))))))

(transduce (-take 3) conj (range 5))


;; Lesson 6:

(type (sequence (map inc) [1 2 3 4]))

(def xfrom (comp (filter even?)
                 (partition-all 2)
                 cat
                 ;; (map inc)
                 (map str)))

(def a (atom []))

(def rf (fn [_ item]
          (swap! a conj item)))

(def f (xfrom rf))

(reset! a [])

(println @a)

(f nil 4)

(let [val 2]
  (reset! a [])
  (f nil val)
  @a)
