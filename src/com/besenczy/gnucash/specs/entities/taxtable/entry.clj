(ns com.besenczy.gnucash.specs.entities.taxtable.entry
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [clojure.spec.alpha :as spec]))

(spec/def ::account ::common/guid)
(spec/def ::amount ::numeric/fraction)
(spec/def ::type
  (spec/and #{"PERCENT" "VALUE"}
    (spec/conformer
      {"PERCENT" :percent "VALUE" :value}
      {:percent "PERCENT" :value "VALUE"})))
