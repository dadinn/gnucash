(ns com.besenczy.gnucash.specs.book
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.entities :as entities]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::slots ::slot/value)

(spec/def ::commodities
  (spec/coll-of ::common/commodity))

(spec/def ::prices
  (spec/coll-of ::entities/price))

(spec/def ::accounts
  (spec/coll-of ::entities/account))

(spec/def ::transactions
  (spec/coll-of ::entities/transaction))

(spec/def ::shedxactions
  (spec/coll-of any?))
(spec/def ::customers
  (spec/coll-of ::entities/customer))
(spec/def ::vendors
  (spec/coll-of ::entities/vendor))
(spec/def ::employees
  (spec/coll-of ::entities/employee))
(spec/def ::jobs
  (spec/coll-of ::entities/job))
(spec/def ::invoices
  (spec/coll-of ::entities/invoice))
(spec/def ::billing-terms
  (spec/coll-of ::entities/billterm))
(spec/def ::tax-table
  (spec/coll-of any?))
(spec/def ::entries
  (spec/coll-of any?))
(spec/def ::budgets
  (spec/coll-of any?))




