(ns com.besenczy.gnucash.specs.entities.billterm
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.strings :as strings]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::name ::strings/non-empty)
(spec/def ::description ::strings/non-empty)
(spec/def ::refcount ::numeric/natural)
(spec/def ::invisible? ::common/boolean-num)
(spec/def ::due-days ::numeric/natural)
(spec/def ::parent ::common/guid)
(spec/def ::child ::common/guid)

