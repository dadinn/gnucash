(ns com.besenczy.gnucash.specs.transaction
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [com.besenczy.gnucash.specs.split :as split]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::currency ::common/commodity)
(spec/def ::date-entered ::common/date)
(spec/def ::date-posted ::common/date)
(spec/def ::description string?)
(spec/def ::slots ::slot/frame)

(spec/def ::split
  (spec/keys
    :req-un
    [::split/id
     ::split/reconciled-state
     ::split/value
     ::split/quantity
     ::split/account]))

(spec/def ::splits
  (spec/coll-of ::split))
