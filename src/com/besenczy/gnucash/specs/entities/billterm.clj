(ns com.besenczy.gnucash.specs.entities.billterm
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::name string?)
(spec/def ::description string?)
(spec/def ::refcount ::common/numeric)
(spec/def ::invisible? ::common/boolean-num)
(spec/def ::due-days ::common/numeric)
(spec/def ::parent ::common/guid)
(spec/def ::child ::common/guid)

