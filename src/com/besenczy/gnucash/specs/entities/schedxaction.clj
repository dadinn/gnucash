(ns com.besenczy.gnucash.specs.entities.schedxaction
  (:require
   [com.besenczy.gnucash.specs.entities.budget :as budget]
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::name string?)
(spec/def ::enabled? ::common/boolean-char)
(spec/def ::start ::common/date)
(spec/def ::end ::common/date)
(spec/def ::last ::common/date)
(spec/def ::account ::common/guid)
(spec/def ::auto-create? ::common/boolean-char)
(spec/def ::auto-create-notify? ::common/boolean-char)
(spec/def ::advance-create-days ::numeric/fraction)
(spec/def ::advance-remind-days ::numeric/fraction)
(spec/def ::instance-count ::numeric/fraction)

(spec/def ::schedule
  (spec/coll-of ::budget/recurrence))
