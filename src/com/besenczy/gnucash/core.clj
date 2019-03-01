(ns com.besenczy.gnucash.core
  (:require
   [com.besenczy.gnucash.import :as import]
   [com.besenczy.gnucash.export :as export]
   [com.besenczy.gnucash.specs :as specs]
   [clojure.spec.alpha :as spec]
   [clojure.java.io :as jio]
   [clojure.edn :as edn]
   [clojure.data.xml :as x]
   [clojure.zip :as z])
  (:import [java.io StringReader StringWriter]))

(defn parse [reader]
  (let [imported (-> reader x/parse z/xml-zip import/document)]
    (if-let [expl (spec/explain-data ::specs/document imported)]
      (throw (ex-info "Invalid GnuCash document" expl))
      (spec/conform ::specs/document imported))))

(defn parse-str [s]
  (with-open [rdr (StringReader. s)]
    (parse rdr)))

(defn emit
  ([document writer]
   (let [unformed (spec/unform ::specs/document document)]
     (if-let [expl (spec/explain-data ::specs/document unformed)]
       (throw (ex-info "Invalid Gnucash document" expl))
       (let [exported (export/document-element unformed)]
         (x/emit exported writer :encoding "utf-8" :doctype "\n"))))))

(defn emit-str [document]
  (with-open [wtr (StringWriter.)]
    (str (emit document wtr))))
