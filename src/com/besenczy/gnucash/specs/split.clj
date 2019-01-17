(ns com.besenczy.gnucash.specs.split
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::reconciled-state #{"y" "n" "c"})
(spec/def ::reconciled-date ::common/datetime)
(spec/def ::value ::common/numeric)
(spec/def ::quantity ::common/numeric)
(spec/def ::account ::common/guid)
(spec/def ::memo string?)
(spec/def ::action string?)
(spec/def ::lot ::common/guid)


