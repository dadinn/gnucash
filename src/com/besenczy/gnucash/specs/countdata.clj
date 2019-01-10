(ns com.besenczy.gnucash.specs.countdata
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::counters
  (spec/map-of string? int?))
