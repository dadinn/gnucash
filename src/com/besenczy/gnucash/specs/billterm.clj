(ns com.besenczy.gnucash.specs.billterm
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::name string?)
(spec/def ::description string?)
(spec/def ::refcount ::common/number)
(spec/def ::invisible? ::common/boolean-num)
(spec/def ::due-days ::common/number)
(spec/def ::parent ::common/guid)
(spec/def ::child ::common/guid)

