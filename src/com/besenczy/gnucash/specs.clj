(ns com.besenczy.gnucash.specs
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.book :as book]
   [clojure.spec.alpha :as spec]))

(spec/def ::counters
  (spec/map-of string? ::common/integer))

(spec/def ::book
  (spec/keys
    :req-un
    [::book/id
     ::book/slots
     ::book/commodities
     ::counters
     ::book/prices
     ::book/accounts
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
     ::book/budgets]))

(spec/def ::document
  (spec/keys :req-un [::book ::counters]))
