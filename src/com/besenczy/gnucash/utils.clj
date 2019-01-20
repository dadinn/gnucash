(ns com.besenczy.gnucash.utils)

(defn empty-seq? [x] (and (seqable? x) (not (seq x))))

(defn into-map
  "create a hashmap from key value pairs using pairs using only non-empty seqable values"
  [& kvs]
  (into {}
    (comp
      (map vec)
      (remove (comp empty-seq? second)))
    (partition 2 kvs)))
