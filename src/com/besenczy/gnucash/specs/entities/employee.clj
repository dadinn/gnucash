(ns com.besenczy.gnucash.specs.entities.employee
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::username ::strings/non-empty)
(spec/def ::rate ::numeric/fraction)
(spec/def ::workday ::numeric/fraction)
(spec/def ::language ::strings/non-empty)
