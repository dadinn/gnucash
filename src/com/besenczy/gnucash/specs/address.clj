(ns com.besenczy.gnucash.specs.address
  (:require
   [com.besenczy.gnucash.specs.strings :as strings]
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.spec.alpha :as spec]))

(spec/def ::name ::strings/non-empty)

(spec/def ::line1 ::strings/non-empty)
(spec/def ::line2 ::strings/non-empty)
(spec/def ::line3 ::strings/non-empty)
(spec/def ::line4 ::strings/non-empty)

(spec/def ::phone ::strings/non-empty)
(spec/def ::fax ::strings/non-empty)
(spec/def ::email ::strings/non-empty)

