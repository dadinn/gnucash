(ns com.besenczy.gnucash.specs.book
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.entities :as entities]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::slots (spec/and ::slot/frame (complement empty?)))

(spec/def ::commodities
  (spec/coll-of ::common/commodity
    :min-count 1 :into []))

(spec/def ::prices
  (spec/coll-of ::entities/price
    :min-count 1 :into []))

(spec/def ::accounts
  (spec/coll-of ::entities/account
    :min-count 1 :into []))

(spec/def ::transactions
  (spec/coll-of ::entities/transaction
    :min-count 1 :into []))

(spec/def ::customers
  (spec/coll-of ::entities/customer
    :min-count 1 :into []))
(spec/def ::vendors
  (spec/coll-of ::entities/vendor
    :min-count 1 :into []))
(spec/def ::employees
  (spec/coll-of ::entities/employee
    :min-count 1 :into []))
(spec/def ::jobs
  (spec/coll-of ::entities/job
    :min-count 1 :into []))
(spec/def ::invoices
  (spec/coll-of ::entities/invoice
    :min-count 1 :into []))
(spec/def ::billing-terms
  (spec/coll-of ::entities/billterm
    :min-count 1 :into []))
(spec/def ::tax-tables
  (spec/coll-of ::entities/taxtable
    :min-count 1 :into []))
(spec/def ::entries
  (spec/coll-of ::entities/entry
    :min-count 1 :into []))

(spec/def ::budgets
  (spec/coll-of ::entities/budget
    :min-count 1 :into []))

(spec/def ::schedxactions
  (spec/coll-of ::entities/schedxaction
    :min-count 1 :into []))

(spec/def ::tempxactions
  (spec/coll-of
    (spec/keys
      :req-un
      [::accounts
       ::transactions])
    :min-count 1 :into []))
