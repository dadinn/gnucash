(ns com.besenczy.gnucash.specs.entities.transaction
  (:require
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [com.besenczy.gnucash.specs.split :as split]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::currency ::common/commodity)
(spec/def ::date-entered ::common/datetime)
(spec/def ::date-posted ::common/datetime)
(spec/def ::description ::strings/non-empty)
(spec/def ::slots (spec/and ::slot/frame (complement empty?)))
(spec/def ::num #{"Invoice" "Bill" "Credit Note" "Expense" "Payment"})

(spec/def ::split
  (common/keys
    :req-un
    [::split/id
     ::split/reconciled-state
     ::split/value
     ::split/quantity
     ::split/account]
    :opt-un
    [::split/reconciled-date
     ::split/memo
     ::split/action
     ::split/lot]))

(spec/def ::splits
  (spec/coll-of ::split
    :min-count 1
    :into []))
