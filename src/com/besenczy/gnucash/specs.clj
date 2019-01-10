(ns com.besenczy.gnucash.specs
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.book :as book]
   [clojure.spec.alpha :as spec]))

(spec/def ::counters
  (spec/map-of string? ::common/number))

(spec/def ::book
  (spec/keys
    :req-un
    [::book/id
     ::book/slots
     ::book/commodities
     ::counters
     ::book/prices
     ::book/accounts
     ::book/transactions]))

(spec/def ::document
  (spec/keys :req-un [::book ::counters]))
