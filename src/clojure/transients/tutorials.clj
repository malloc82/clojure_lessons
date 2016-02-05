(ns transients.tutorials
  (:use clojure.core
        [clojure.pprint :only [pprint]]))

(defprotocol IArrayColl
  (-nth [this idx])
  (-append [this val])
  (-append! [this val])
  ;; (-replace [this val])
  (get-cnt [this])
  (get-arr [this])
  )

(deftype ArrayColl [^:volatile-mutable cnt  ^objects arr]
  IArrayColl
  (-nth [this idx]
    (assert (< idx cnt))
    (aget arr idx))
  (-append [this val]
    (if (< cnt (alength arr))
      (let [new-array (aclone arr)]
        (aset new-array cnt val)
        (set! cnt (inc cnt))
        (ArrayColl. (inc cnt) new-array))
      (let [new-array (object-array (* 2 (alength arr)))]
        (System/arraycopy arr 0 new-array 0 cnt)
        (aset new-array cnt val)
        (ArrayColl. (inc cnt) new-array) )))
  (-append! [this _val]
    (if (< cnt (alength arr))
      (do (aset arr cnt _val)
          (set! cnt (inc cnt))
          this)
      (let [new-array (object-array (* 2 (alength arr)))]
        (println "here")
        (System/arraycopy arr 0 new-array 0 cnt)
        (aset new-array cnt _val)
        (ArrayColl. (inc cnt) new-array))))
  (get-cnt [this]
    cnt)
  (get-arr [this]
    arr))

(defn array-coll []
  (ArrayColl. 0 (object-array 1)))

(pprint (-> (array-coll)
            (-append! 1)
            (-append! 2)
            (-append! 3)
            (-nth 2)))

(let [a (array-coll)
      b (-> a
            (-append! 2)
            (-append! 3))]
  (pprint (get-arr a))
  (pprint (get-arr b))
  (identical? a b))
