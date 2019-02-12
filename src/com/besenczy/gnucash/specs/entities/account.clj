(ns com.besenczy.gnucash.specs.entities.account
  (:require
   [com.besenczy.gnucash.specs.entities.lot :as lot]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.string :as string]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::name ::strings/non-empty)
(spec/def ::description ::strings/non-empty)
(spec/def ::code ::strings/non-empty)

(spec/def ::type
  (spec/and #{"ASSET" "LIABILITY" "EQUITY" "INCOME" "EXPENSE" "RECEIVABLE" "PAYABLE" "BANK" "ROOT"}
    (spec/conformer
      (comp keyword string/lower-case)
      (comp string/upper-case name))))

(spec/def ::parent ::common/guid)
(spec/def ::commodity ::common/commodity)
(spec/def ::unit ::numeric/natural)

(spec/def ::slots (spec/and ::slot/frame (complement empty?)))

(spec/def ::lots
  (spec/coll-of
    (spec/keys
      :req-un
      [::lot/id]
      :opt-un
      [::lot/slots])
    :min-count 1
    :into []))
