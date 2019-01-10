(ns com.besenczy.gnucash.specs.split
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::reconciled-state #{"y" "n" "c"})
(spec/def ::value ::common/number)
(spec/def ::quantity ::common/number)
(spec/def ::account ::common/guid)

