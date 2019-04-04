(ns com.besenczy.gnucash.specs.entities.taxtable
  (:require
   [com.besenczy.gnucash.specs.entities.taxtable.entry :as entry]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::name ::strings/non-empty)
(spec/def ::refcount ::numeric/natural)
(spec/def ::invisible? ::common/boolean-num)
(spec/def ::entries
  (spec/coll-of
    (common/keys
      :req-un
      [::entry/amount
       ::entry/type]
      :opt-un
      [::entry/account])
    :min-count 1
    :into []))
(spec/def ::parent ::common/guid)
(spec/def ::child ::common/guid)
