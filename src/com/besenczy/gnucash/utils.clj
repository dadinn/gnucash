(ns com.besenczy.gnucash.utils
  (:require [clojure.string :as string]))

(defn empty-seq? [x] (and (seqable? x) (not (seq x))))

(defn into-map
  "create a hashmap from key value pairs using pairs using only non-empty seqable values"
  [& kvs]
  (into {}
    (comp
      (map vec)
      (remove (comp empty-seq? second)))
    (partition 2 kvs)))

(defmacro alias-subns
  "creates a subnamespace for the current namespace with named `name`, and then creates an alias to this new namespace with `name` in the current namespace."
  ([alias & sub-path]
   `(let [subns#
          (quote
            ~(->> (or (seq sub-path) (list alias))
               (map name)
               (cons (str *ns*))
               (string/join ".")
               symbol))]
      (when (not (find-ns subns#))
        (create-ns subns#))
      (alias (quote ~alias) subns#))))
