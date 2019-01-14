(ns com.besenczy.gnucash.specs.taxtable-entry
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::account ::common/guid)
(spec/def ::amount ::common/number)
(spec/def ::type
  (spec/and #{"PERCENT" "VALUE"}
    (spec/conformer
      {"PERCENT" :percent "VALUE" :value}
      {:percent "PERCENT" :value "VALUE"})))
