(ns com.besenczy.gnucash.specs.slot
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::value
  (spec/and
    (spec/or
      :frame
      (spec/cat
        :type #{:frame}
        :value ::frame)
      :integer
      (spec/cat
        :type #{:integer}
        :value ::numeric/natural)
      :numeric
      (spec/cat
        :type #{:numeric}
        :value ::numeric/fraction)
      :guid
      (spec/cat
        :type #{:guid}
        :value ::common/guid)
      :gdate
      (spec/cat
        :type #{:gdate}
        :value ::common/date)
      :timespec
      (spec/cat
        :type #{:timespec}
        :value ::common/datetime)
      ;; string case must be the last!
      :string
      (spec/cat
        :type #{:string}
        :value string?))
    (spec/conformer second
      (fn [{:keys [type] :as value}]
        [type value]))))

(spec/def ::frame
  (spec/map-of ::strings/non-empty ::value))
