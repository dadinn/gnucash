(ns com.besenczy.gnucash.specs.entities
  (:require
   [com.besenczy.gnucash.specs.entities.price :as price]
   [com.besenczy.gnucash.specs.entities.account :as act]
   [com.besenczy.gnucash.specs.entities.transaction :as trn]
   [com.besenczy.gnucash.specs.entities.counterparty :as ctpy]
   [com.besenczy.gnucash.specs.entities.employee :as empl]
   [com.besenczy.gnucash.specs.entities.job :as job]
   [com.besenczy.gnucash.specs.entities.invoice :as invc]
   [com.besenczy.gnucash.specs.entities.billterm :as bt]
   [com.besenczy.gnucash.specs.entities.taxtable :as tt]
   [com.besenczy.gnucash.specs.entities.entry :as entry]
   [com.besenczy.gnucash.specs.entities.schedxaction :as sx]
   [com.besenczy.gnucash.specs.entities.budget :as bgt]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::price
  (common/keys
    :req-un
    [::price/id
     ::price/commodity
     ::price/currency
     ::price/date
     ::price/value
     ::price/source]
    :opt-un
    [::price/type]))

(spec/def ::account
  (common/keys
    :req-un
    [::act/id
     ::act/name
     ::act/type]
    :opt-un
    [::act/description
     ::act/code
     ::act/commodity
     ::act/unit
     ::act/parent
     ::act/slots
     ::act/lots]))

(spec/def ::transaction
  (spec/and
    (common/keys
      :req-un
      [::trn/id
       ::trn/currency
       ::trn/date-entered
       ::trn/date-posted
       ::trn/splits]
      :opt-un
      [::trn/num
       ::trn/description
       ::trn/slots])
    (spec/conformer
      (fn [{:keys [id] :as data}]
        (update data :splits
          (partial map (fn [split] (assoc split :parent id)))))
      (fn [{:keys [splits] :as data}]
        (update data :splits
          (partial map (fn [split] (dissoc split :parent))))))))

(spec/def ::billterm
  (common/keys
    :req-un
    [::bt/guid
     ::bt/name
     ::bt/refcount]
    :opt-un
    [::bt/description
     ::bt/invisible?
     ::bt/due-days
     ::bt/parent
     ::bt/child]))

(spec/def ::taxtable
  (common/keys
    :req-un
    [::tt/guid
     ::tt/name
     ::tt/refcount
     ::tt/invisible?
     ::tt/entries]
    :opt-un
    [::tt/parent
     ::tt/child]))

(spec/def ::customer
  (common/keys
    :req-un
    [::ctpy/guid
     ::ctpy/id
     ::ctpy/name
     ::ctpy/active?
     ::ctpy/use-tax-table?
     ::ctpy/currency
     ::ctpy/credit-limit
     ::ctpy/discount
     ::ctpy/tax-included
     ::ctpy/billing-address]
    :opt-un
    [::ctpy/shipping-address
     ::ctpy/tax-table
     ::ctpy/terms
     ::ctpy/notes
     ::ctpy/slots]))

(spec/def ::vendor
  (common/keys
    :req-un
    [::ctpy/guid
     ::ctpy/id
     ::ctpy/name
     ::ctpy/active?

     ::ctpy/use-tax-table?
     ::ctpy/currency
     ::ctpy/tax-included
     ::ctpy/billing-address]
    :opt-un
    [::ctpy/tax-table
     ::ctpy/terms
     ::ctpy/notes
     ::ctpy/slots]))

(spec/def ::employee
  (common/keys
    :req-un
    [::ctpy/guid
     ::ctpy/id
     ::empl/username
     ::ctpy/active?
     ::ctpy/billing-address
     ::empl/workday
     ::empl/rate
     ::ctpy/currency]
    :opt-un
    [::empl/language
     ::ctpy/slots]))

(spec/def ::job
  (common/keys
    :req-un
    [::job/guid
     ::job/id
     ::job/name
     ::job/owner
     ::job/active?]
    :opt-un
    [::job/reference]))

(spec/def ::invoice
  (common/keys
    :req-un
    [::invc/guid
     ::invc/id
     ::invc/owner
     ::invc/currency
     ::invc/opened
     ::invc/active?]
    :opt-un
    [::invc/billto
     ::invc/reference
     ::invc/terms
     ::invc/posted
     ::invc/postacc
     ::invc/postlot
     ::invc/posttxn
     ::invc/notes
     ::invc/slots]))

(spec/def ::entry
  (spec/and
    (spec/or
      :invoice-entry
      (common/keys
        :req-un
        [::entry/guid
         ::entry/date
         ::entry/entered
         ::entry/quantity
         ::entry/invoice
         ::entry/account
         ::entry/price
         ::entry/taxable?
         ::entry/tax-included?]
        :opt-un
        [::entry/description
         ::entry/action
         ::entry/tax-table
         ::entry/discount-type
         ::entry/discount-how
         ::entry/discount])
      :bill-entry
      (common/keys
        :req-un
        [::entry/guid
         ::entry/date
         ::entry/entered
         ::entry/quantity
         ::entry/bill
         ::entry/account
         ::entry/price
         ::entry/taxable?
         ::entry/tax-included?
         ::entry/payment]
        :opt-un
        [::entry/description
         ::entry/action
         ::entry/tax-table
         ::entry/billable?]))
    (spec/conformer
      (fn [[type value]] (assoc value :type type))
      (fn [{:keys [type] :as value}] [type (dissoc value :type)]))))

(spec/def ::schedxaction
  (common/keys
    :req-un
    [::sx/id
     ::sx/name
     ::sx/account
     ::sx/enabled?
     ::sx/start
     ::sx/schedule
     ::sx/auto-create?
     ::sx/auto-create-notify?
     ::sx/advance-create-days
     ::sx/advance-remind-days]
    :opt-un
    [::sx/end
     ::sx/last
     ::sx/instance-count]))

(spec/def ::budget
  (common/keys
    :req-un
    [::bgt/id
     ::bgt/name
     ::bgt/recurrence
     ::bgt/num-periods]
    :opt-un
    [::bgt/slots]))
