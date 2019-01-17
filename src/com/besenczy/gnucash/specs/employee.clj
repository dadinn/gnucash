(ns com.besenczy.gnucash.specs.employee
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::username string?)
(spec/def ::rate ::common/numeric)
(spec/def ::workday ::common/numeric)
(spec/def ::language string?)
