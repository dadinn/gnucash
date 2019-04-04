(ns com.besenczy.gnucash.specs.xml
  (:require
   [clojure.spec.alpha :as spec]))

(spec/def ::element
  (spec/keys :req-un [::tag ::attrs ::content]))

(spec/def ::tag keyword?)
(spec/def ::attrs (spec/map-of keyword? string?))
(spec/def ::content
  (spec/coll-of
    (spec/or
      :element ::element
      :string string?)))
