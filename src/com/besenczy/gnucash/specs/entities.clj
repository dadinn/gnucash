(ns com.besenczy.gnucash.specs.entities
  (:require
   [com.besenczy.gnucash.utils :refer [alias-subns]]
   [com.besenczy.gnucash.specs.entities.counterparty :as ctpy]
   [com.besenczy.gnucash.specs.entities.employee :as empl]
   [com.besenczy.gnucash.specs.entities.job :as job]
   [com.besenczy.gnucash.specs.entities.invoice :as invc]
   [com.besenczy.gnucash.specs.entities.billterm :as bt]
   [com.besenczy.gnucash.specs.entities.taxtable :as tt]
   [com.besenczy.gnucash.specs.entities.entry :as entry]
   [com.besenczy.gnucash.specs.entities.schedxaction :as sx]
   [com.besenczy.gnucash.specs.entities.budget :as bgt]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.string :as string]
   [clojure.spec.alpha :as spec]))

(alias-subns price)

(spec/def ::price/id ::common/guid)
(spec/def ::price/commodity ::common/commodity)
(spec/def ::price/currency ::common/commodity)
(spec/def ::price/date ::common/datetime)
(spec/def ::price/source ::strings/non-empty)
(spec/def ::price/type ::strings/non-empty)
(spec/def ::price/value ::numeric/fraction)

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

(alias-subns lot account lot)

(spec/def ::lot/id ::common/guid)
(spec/def ::lot/slots (spec/and ::slot/frame (complement empty?)))

(alias-subns act account)

(spec/def ::act/id ::common/guid)
(spec/def ::act/name ::strings/non-empty)
(spec/def ::act/description ::strings/non-empty)
(spec/def ::act/code ::strings/non-empty)

(spec/def ::act/type
  (spec/and #{"ASSET" "LIABILITY" "EQUITY" "INCOME" "EXPENSE" "RECEIVABLE" "PAYABLE" "CASH" "BANK" "CREDIT" "TRADING" "ROOT"}
    (spec/conformer
      (comp keyword string/lower-case)
      (comp string/upper-case name))))

(spec/def ::act/parent ::common/guid)
(spec/def ::act/commodity ::common/commodity)
(spec/def ::act/unit ::numeric/natural)

(spec/def ::act/slots (spec/and ::slot/frame (complement empty?)))

(spec/def ::act/lots
  (spec/coll-of
    (common/keys
      :req-un
      [::lot/id]
      :opt-un
      [::lot/slots])
    :min-count 1
    :into []))

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

(alias-subns split transaction split)

(spec/def ::split/id ::common/guid)
(spec/def ::split/reconciled-state #{"y" "n" "c"})
(spec/def ::split/reconciled-date ::common/datetime)
(spec/def ::split/value ::numeric/fraction)
(spec/def ::split/quantity ::numeric/fraction)
(spec/def ::split/account ::common/guid)
(spec/def ::split/memo ::strings/non-empty)
(spec/def ::split/action ::strings/non-empty)
(spec/def ::split/lot ::common/guid)

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

(alias-subns trn transaction)

(spec/def ::trn/id ::common/guid)
(spec/def ::trn/currency ::common/commodity)
(spec/def ::trn/date-entered ::common/datetime)
(spec/def ::trn/date-posted ::common/datetime)
(spec/def ::trn/description ::strings/non-empty)
(spec/def ::trn/slots (spec/and ::slot/frame (complement empty?)))
(spec/def ::trn/num #{"Invoice" "Bill" "Credit Note" "Expense" "Payment"})
(spec/def ::trn/splits
  (spec/coll-of ::split
    :min-count 1
    :into []))

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
