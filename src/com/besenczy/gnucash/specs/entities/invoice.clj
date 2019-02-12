(ns com.besenczy.gnucash.specs.entities.invoice
  (:require
   [com.besenczy.gnucash.specs.entities.job :as job]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.slot :as slot]
   [clojure.spec.alpha :as spec]))

(spec/def ::guid ::common/guid)
(spec/def ::id ::strings/non-empty)
(spec/def ::owner ::job/owner)
(spec/def ::billto ::job/owner)
(spec/def ::reference ::strings/non-empty)
(spec/def ::currency ::common/commodity)
(spec/def ::opened ::common/datetime)
(spec/def ::posted ::common/datetime)
(spec/def ::postacc ::common/guid)
(spec/def ::postlot ::common/guid)
(spec/def ::posttxn ::common/guid)
(spec/def ::terms ::common/guid)
(spec/def ::notes ::strings/non-empty)
(spec/def ::active? ::common/boolean-num)
(spec/def ::slots (spec/and ::slot/frame (complement empty?)))
