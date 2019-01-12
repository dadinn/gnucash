(ns com.besenczy.gnucash.specs.counterparty
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.address :as addr]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::active?
  (spec/and #{"0" "1"}
    (spec/conformer
      {"0" false "1" true}
      {true "1" false "0"})))
(spec/def ::id string?)
(spec/def ::name string?)
(spec/def ::currency ::common/commodity)
(spec/def ::terms ::common/guid)
(spec/def ::tax-table ::common/guid)
(spec/def ::tax-included #{"NO" "YES" "USEGLOBAL"})
(spec/def ::use-tax-table? ::active?)
(spec/def ::discount ::common/number)
(spec/def ::credit-limit ::common/number)

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
