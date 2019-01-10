(ns com.besenczy.gnucash.specs.commodity
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::id string?)
(spec/def ::space string?)
(spec/def ::name string?)
(spec/def ::get-quotes string?)
(spec/def ::quote-source string?)
(spec/def ::quote-timezone string?)
(spec/def ::xcode string?)
(spec/def ::fraction string?)
