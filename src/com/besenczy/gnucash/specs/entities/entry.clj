(ns com.besenczy.gnucash.specs.entities.entry
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::billable? ::common/boolean-num)

(spec/def ::bill ::common/guid)
(spec/def ::invoice ::common/guid)

(spec/def ::date-recorded ::common/datetime)
(spec/def ::date-entered ::common/datetime)

(spec/def ::description string?)

(spec/def ::action
  (spec/and
    #{"Project" "Material" "Hours"}
    (spec/conformer
      {"Project" :project
       "Material" :material
       "Hours" :hours}
      {:project "Project"
       :material "Material"
       :hours "Hours"})))

(spec/def ::price ::numeric/fraction)
(spec/def ::quantity ::numeric/fraction)

(spec/def ::account ::common/guid)

(spec/def ::taxable? ::common/boolean-num)
(spec/def ::tax-table ::common/guid)
(spec/def ::tax-included? ::common/boolean-num)

(spec/def ::discount-type
  (spec/and
    #{"PERCENT" "VALUE"}
    (spec/conformer
      {"PERCENT" :percent
       "VALUE" :value}
      {:percent "PERCENT"
       :value "VALUE"})))

(spec/def ::discount-how
  (spec/and
    #{"PRETAX" "POSTTAX" "SAMETIME"}
    (spec/conformer
      {"PRETAX" :pretax
       "POSTTAX" :posttax
       "SAMETIME" :sametime}
      {:pretax "PRETAX"
       :posttax "POSTTAX"
       :sametime "SAMETIME"})))

(spec/def ::discount ::numeric/fraction)

(spec/def ::payment
  (spec/and
    #{"CASH"}
    (spec/conformer
      {"CASH" :cash}
      {:cash "CASH"})))
