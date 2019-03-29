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
   [clojure.spec.alpha :as spec]))

(spec/def ::price
  (spec/keys
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
  (spec/keys
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
  (spec/keys
    :req-un
    [::trn/id
     ::trn/currency
     ::trn/date-entered
     ::trn/date-posted
     ::trn/description
     ::trn/splits]
    :opt-un
    [::trn/num
     ::trn/slots]))

(spec/def ::billterm
  (spec/keys
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
  (spec/keys
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
  (spec/keys
    :req-un
    [::ctpy/guid
     ::ctpy/id
     ::ctpy/name
     ::ctpy/active?
     ::ctpy/terms
     ::ctpy/tax-table
     ::ctpy/use-tax-table?
     ::ctpy/currency
     ::ctpy/credit-limit
     ::ctpy/discount
     ::ctpy/tax-included
     ::ctpy/billing-address]
    :opt-un
    [::ctpy/shipping-address
     ::ctpy/notes
     ::ctpy/slots]))

(spec/def ::vendor
  (spec/keys
    :req-un
    [::ctpy/guid
     ::ctpy/id
     ::ctpy/name
     ::ctpy/active?
     ::ctpy/terms
     ::ctpy/tax-table
     ::ctpy/use-tax-table?
     ::ctpy/currency
     ::ctpy/tax-included
     ::ctpy/billing-address]
    :opt-un
    [::ctpy/notes
     ::ctpy/slots]))

(spec/def ::employee
  (spec/keys
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
  (spec/keys
    :req-un
    [::job/guid
     ::job/id
     ::job/name
     ::job/owner
     ::job/active?]
    :opt-un
    [::job/reference]))

(spec/def ::invoice
  (spec/keys
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
  (spec/or
    :invoice-entry
    (spec/keys
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
    (spec/keys
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
       ::entry/billable?])))

(spec/def ::schedxaction
  (spec/keys
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
  (spec/keys
    :req-un
    [::bgt/id
     ::bgt/name
     ::bgt/recurrence
     ::bgt/num-periods]
    :opt-un
    [::bgt/slots]))
