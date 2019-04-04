(ns com.besenczy.gnucash.specs.entities.budget
  (:require
   [com.besenczy.gnucash.specs.entities.recurrence :as recur]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::name ::strings/non-empty)
(spec/def ::description ::strings/non-empty)
(spec/def ::num-periods ::numeric/natural)

(spec/def ::recurrence
  (common/keys
    :req-un
    [::recur/start
     ::recur/period-type
     ::recur/multiplier]
    :opt-un
    [::recur/weekend-adjustment]))

(spec/def ::slots (spec/and ::slot/frame (complement empty?)))
