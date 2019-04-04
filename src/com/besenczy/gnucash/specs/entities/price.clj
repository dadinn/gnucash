(ns com.besenczy.gnucash.specs.entities.price
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::commodity ::common/commodity)
(spec/def ::currency ::common/commodity)
(spec/def ::date ::common/datetime)
(spec/def ::source string?)
(spec/def ::type string?)
(spec/def ::source ::strings/non-empty)
(spec/def ::type ::strings/non-empty)
(spec/def ::value ::numeric/fraction)

