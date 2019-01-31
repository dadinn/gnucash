(ns com.besenczy.gnucash.specs.strings
  (:require
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec]))

(spec/def ::non-empty
  (spec/and string? (complement empty?)))

