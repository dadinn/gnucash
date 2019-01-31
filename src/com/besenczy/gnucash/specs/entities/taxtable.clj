(ns com.besenczy.gnucash.specs.entities.taxtable
  (:require
   [com.besenczy.gnucash.specs.entities.taxtable-entry :as entry]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::name string?)
(spec/def ::refcount ::common/numeric)
(spec/def ::invisible? ::common/boolean-num)
(spec/def ::entries
  (spec/coll-of
    (spec/keys
      :req-un
      [::entry/account
       ::entry/amount
       ::entry/type])))
(spec/def ::parent ::common/guid)
(spec/def ::child ::common/guid)
