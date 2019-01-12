(ns com.besenczy.gnucash.specs.entities
  (:require
   [com.besenczy.gnucash.specs.price :as price]
   [com.besenczy.gnucash.specs.account :as act]
   [com.besenczy.gnucash.specs.transaction :as trn]
   [com.besenczy.gnucash.specs.counterparty :as ctpy]
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
     ::act/type
     ::act/commodity
     ::act/unit]
    :opt-un
    [::act/description
     ::act/code
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
     ::trn/slots
     ::trn/splits]
    :opt-un
    [::trn/num]))

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
     ::ctpy/notes]))

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
    [::ctpy/notes]))
