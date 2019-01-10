(ns com.besenczy.gnucash.specs.account
  (:require
   [com.besenczy.gnucash.specs.common :as common]
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
(spec/def ::unit ::common/number)
