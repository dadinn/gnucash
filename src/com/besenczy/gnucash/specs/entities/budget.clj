(ns com.besenczy.gnucash.specs.entities.budget
  (:require
   [com.besenczy.gnucash.specs.recurrence :as recur]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::name string?)
(spec/def ::description string?)
(spec/def ::num-periods ::common/numeric)

(spec/def ::recurrence
  (spec/keys
    :req-un
    [::recur/start
     ::recur/period-type
     ::recur/multiplier]
    :opt-un
    [::recur/weekend-adjustment]))

(spec/def ::slots ::slot/frame)
