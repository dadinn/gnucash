(ns com.besenczy.gnucash.specs.split
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::reconciled-state #{"y" "n" "c"})
(spec/def ::reconciled-date ::common/datetime)
(spec/def ::value ::numeric/fraction)
(spec/def ::quantity ::numeric/fraction)
(spec/def ::account ::common/guid)
(spec/def ::memo ::strings/non-empty)
(spec/def ::action ::strings/non-empty)
(spec/def ::lot ::common/guid)


