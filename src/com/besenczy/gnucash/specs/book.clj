(ns com.besenczy.gnucash.specs.book
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.entities :as entities]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::slots ::slot/frame)

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
  (spec/coll-of any?))
(spec/def ::vendors
  (spec/coll-of any?))
(spec/def ::employees
  (spec/coll-of any?))
(spec/def ::jobs
  (spec/coll-of any?))
(spec/def ::invoices
  (spec/coll-of any?))
(spec/def ::tax-table
  (spec/coll-of any?))
(spec/def ::entries
  (spec/coll-of any?))
(spec/def ::budgets
  (spec/coll-of any?))
(spec/def ::billing-term
  (spec/coll-of any?))




