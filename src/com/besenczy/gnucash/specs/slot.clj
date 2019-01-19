(ns com.besenczy.gnucash.specs.slot
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::value
  (spec/and
    (spec/or
      :frame (spec/tuple #{:frame} ::frame)
      :integer (spec/tuple #{:integer} ::common/numeric)
      :numeric (spec/tuple #{:numeric} ::common/numeric)
      :guid (spec/tuple #{:guid} ::common/guid)
      :gdate (spec/tuple #{:gdate} ::common/date)
      :timespec (spec/tuple #{:timespec} ::common/datetime)
      ;; string case must be the last!
      :string (spec/tuple #{:string} string?))
    (spec/conformer second str)))

(spec/def ::frame
  (spec/map-of string? ::value))
