(ns com.besenczy.gnucash.specs.account
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [com.besenczy.gnucash.specs.lot :as lot]
   [clojure.string :as string]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::name string?)
(spec/def ::description string?)
(spec/def ::code string?)

(def account-types
  #{"ASSET" "LIABILITY" "EQUITY" "INCOME" "EXPENSE" "RECEIVABLE" "PAYABLE" "BANK" "ROOT"})

(spec/def ::type
  (spec/and string? account-types
    (spec/conformer
      (fn [s] (-> s string/lower-case keyword))
      (fn [k] (-> k name string/upper-case)))))

(spec/def ::parent ::common/guid)
(spec/def ::commodity ::common/commodity)
(spec/def ::unit ::common/numeric)

(spec/def ::slots ::slot/value)

(spec/def ::lots
  (spec/coll-of
    (spec/keys
      :req-un
      [::lot/id]
      :opt-un
      [::lot/slots])))
