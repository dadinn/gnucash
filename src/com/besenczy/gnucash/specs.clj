(ns com.besenczy.gnucash.specs
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.book :as book]
   [clojure.spec.alpha :as spec]))

(spec/def ::book
  (spec/keys
    :req-un
    [::book/id
     ::book/accounts
     ::common/counters]
    :opt-un
    [::book/prices
     ::book/transactions
     ::book/billing-terms
     ::book/tax-tables
     ::book/customers
     ::book/vendors
     ::book/employees
     ::book/jobs
     ::book/invoices
     ::book/entries
     ::book/schedxactions
     ::book/tempxactions
     ::book/budgets
     ::book/commodities
     ::book/slots]))

(spec/def ::document
  (spec/keys :req-un [::book ::common/counters]))
