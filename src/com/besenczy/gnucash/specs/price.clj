(ns com.besenczy.gnucash.specs.price
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::commodity ::common/commodity)
(spec/def ::currenty ::common/commodity)
(spec/def ::date ::common/datetime)
(spec/def ::source string?)
(spec/def ::value ::common/number)
