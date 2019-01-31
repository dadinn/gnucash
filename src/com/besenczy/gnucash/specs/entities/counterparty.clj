(ns com.besenczy.gnucash.specs.entities.counterparty
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.address :as addr]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::active? ::common/boolean-num)
(spec/def ::id string?)
(spec/def ::name string?)
(spec/def ::currency ::common/commodity)
(spec/def ::terms ::common/guid)
(spec/def ::tax-table ::common/guid)
(spec/def ::tax-included #{"NO" "YES" "USEGLOBAL"})
(spec/def ::use-tax-table? ::active?)
(spec/def ::discount ::common/numeric)
(spec/def ::credit-limit ::common/numeric)

(spec/def ::billing-address
  (spec/keys
    :req-un
    [::addr/line1]
    :opt-un
    [::addr/name
     ::addr/line2
     ::addr/line3
     ::addr/line4
     ::addr/phone
     ::addr/fax
     ::addr/email]))

(spec/def ::shipping-address ::billing-address)
(spec/def ::notes string?)
(spec/def ::slots ::slot/frame)
