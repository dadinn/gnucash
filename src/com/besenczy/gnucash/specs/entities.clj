(ns com.besenczy.gnucash.specs.entities
  (:require
   [com.besenczy.gnucash.utils :refer [alias-subns]]
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

(alias-subns bt billterm)

(spec/def ::bt/guid ::common/guid)
(spec/def ::bt/name ::strings/non-empty)
(spec/def ::bt/description ::strings/non-empty)
(spec/def ::bt/refcount ::numeric/natural)
(spec/def ::bt/invisible? ::common/boolean-num)
(spec/def ::bt/due-days ::numeric/natural)
(spec/def ::bt/parent ::common/guid)
(spec/def ::bt/child ::common/guid)

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

(alias-subns tte taxtable entry)

(spec/def ::tte/account ::common/guid)
(spec/def ::tte/amount ::numeric/fraction)
(spec/def ::tte/type
  (spec/and #{"PERCENT" "VALUE"}
    (spec/conformer
      {"PERCENT" :percent "VALUE" :value}
      {:percent "PERCENT" :value "VALUE"})))

(alias-subns tt taxtable)

(spec/def ::tt/guid ::common/guid)
(spec/def ::tt/name ::strings/non-empty)
(spec/def ::tt/refcount ::numeric/natural)
(spec/def ::tt/invisible? ::common/boolean-num)
(spec/def ::tt/parent ::common/guid)
(spec/def ::tt/child ::common/guid)
(spec/def ::tt/entries
  (spec/coll-of
    (common/keys
      :req-un
      [::tte/amount
       ::tte/type]
      :opt-un
      [::tte/account])
    :min-count 1
    :into []))

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

(alias-subns addr counterparty addr)

(spec/def ::addr/name ::strings/non-empty)

(spec/def ::addr/line1 ::strings/non-empty)
(spec/def ::addr/line2 ::strings/non-empty)
(spec/def ::addr/line3 ::strings/non-empty)
(spec/def ::addr/line4 ::strings/non-empty)

(spec/def ::addr/phone ::strings/non-empty)
(spec/def ::addr/fax ::strings/non-empty)
(spec/def ::addr/email ::strings/non-empty)

(alias-subns ctpy counterparty)

(spec/def ::ctpy/guid ::common/guid)
(spec/def ::ctpy/active? ::common/boolean-num)
(spec/def ::ctpy/id ::strings/non-empty)
(spec/def ::ctpy/name ::strings/non-empty)
(spec/def ::ctpy/currency ::common/commodity)
(spec/def ::ctpy/terms ::common/guid)
(spec/def ::ctpy/tax-table ::common/guid)
(spec/def ::ctpy/tax-included #{"NO" "YES" "USEGLOBAL"})
(spec/def ::ctpy/use-tax-table? ::common/boolean-num)
(spec/def ::ctpy/discount ::numeric/fraction)
(spec/def ::ctpy/credit-limit ::numeric/fraction)

(spec/def ::ctpy/billing-address
  (common/keys
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

(spec/def ::ctpy/shipping-address ::ctpy/billing-address)
(spec/def ::ctpy/notes ::strings/non-empty)
(spec/def ::ctpy/slots (spec/and ::slot/frame (complement empty?)))

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

(alias-subns empl employee)

(spec/def ::empl/username ::strings/non-empty)
(spec/def ::empl/rate ::numeric/fraction)
(spec/def ::empl/workday ::numeric/fraction)
(spec/def ::empl/language ::strings/non-empty)

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

(alias-subns owner)

(spec/def ::owner/id ::common/guid)
(spec/def ::owner/type
  (spec/and
    #{"gncCustomer" "gncVendor" "gncEmployee" "gncJob"}
    (spec/conformer
      {"gncCustomer" :customer
       "gncVendor" :vendor
       "gncEmployee" :employee
       "gncJob" :job}
      {:customer "gncCustomer"
       :vendor "gncVendor"
       :employee "gncEmployee"
       :job "gncJob"})))

(alias-subns job)

(spec/def ::job/guid ::common/guid)
(spec/def ::job/active? ::common/boolean-num)
(spec/def ::job/id ::strings/non-empty)
(spec/def ::job/name ::strings/non-empty)
(spec/def ::job/reference ::strings/non-empty)

(spec/def ::job/owner
  (common/keys
    :req-un
    [::owner/id
     ::owner/type]))

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

(alias-subns invc invoice)

(spec/def ::invc/guid ::common/guid)
(spec/def ::invc/id ::strings/non-empty)
(spec/def ::invc/owner ::job/owner)
(spec/def ::invc/billto ::job/owner)
(spec/def ::invc/reference ::strings/non-empty)
(spec/def ::invc/currency ::common/commodity)
(spec/def ::invc/opened ::common/datetime)
(spec/def ::invc/posted ::common/datetime)
(spec/def ::invc/postacc ::common/guid)
(spec/def ::invc/postlot ::common/guid)
(spec/def ::invc/posttxn ::common/guid)
(spec/def ::invc/terms ::common/guid)
(spec/def ::invc/notes ::strings/non-empty)
(spec/def ::invc/active? ::common/boolean-num)
(spec/def ::invc/slots (spec/and ::slot/frame (complement empty?)))

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
