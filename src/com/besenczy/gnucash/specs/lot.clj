(ns com.besenczy.gnucash.specs.lot
  (:require
   [com.besenczy.gnucash.specs.slot :as slot]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::slots ::slot/frame)
