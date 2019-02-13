(ns com.besenczy.gnucash.specs.entities.recurrence
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::start ::common/date)
(spec/def ::multiplier ::numeric/natural)

(spec/def ::period-type
  (spec/and #{"day" "week" "month"}
    (spec/conformer
      {"day" :day
       "week" :week
       "month" :month}
      {:day "day"
       :week "week"
       :month "month"})))

(spec/def ::weekend-adjustment
  (spec/and #{"forward" "back"}
    (spec/conformer
      {"forward" :forward
       "back" :backward}
      {:forward "forward"
       :backward "back"})))
