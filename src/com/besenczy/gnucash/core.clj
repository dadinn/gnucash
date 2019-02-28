(ns com.besenczy.gnucash.core
  (:require
   [com.besenczy.gnucash.import :as import]
   [clojure.java.io :as jio]
   [clojure.edn :as edn]
   [clojure.data.xml :as x]
   [clojure.zip :as z]))

(defn load-doc [path]
  (-> (slurp path)
    (x/parse-str)
    (z/xml-zip)
    (import/document)))
