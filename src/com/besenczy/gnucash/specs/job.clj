(ns com.besenczy.gnucash.specs.job
  (:require
   [com.besenczy.gnucash.specs.owner :as owner]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::active? ::common/boolean-num)
(spec/def ::id string?)
(spec/def ::name string?)
(spec/def ::reference string?)

(spec/def ::owner
  (spec/keys
    :req-un
    [::owner/id
     ::owner/type]))

