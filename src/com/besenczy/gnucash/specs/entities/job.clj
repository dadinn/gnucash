(ns com.besenczy.gnucash.specs.entities.job
  (:require
   [com.besenczy.gnucash.specs.owner :as owner]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::active? ::common/boolean-num)
(spec/def ::id ::strings/non-empty)
(spec/def ::name ::strings/non-empty)
(spec/def ::reference ::strings/non-empty)

(spec/def ::owner
  (spec/keys
    :req-un
    [::owner/id
     ::owner/type]))

