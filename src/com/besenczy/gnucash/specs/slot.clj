(ns com.besenczy.gnucash.specs.slot
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::value
  (spec/and
    (spec/or
      :frame ::frame
      :integer ::common/numeric
      :gdate ::common/date
      :timespec ::common/datetime
      ;; string case must be the last!
      :string string?)
    (spec/conformer second str)))

(spec/def ::frame
  (spec/map-of string? ::value))
