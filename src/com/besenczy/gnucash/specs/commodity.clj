(ns com.besenczy.gnucash.specs.commodity
  (:require
   [com.besenczy.gnucash.specs.strings :as strings]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::strings/non-empty)
(spec/def ::space ::strings/non-empty)
(spec/def ::name ::strings/non-empty)
(spec/def ::get-quotes ::strings/non-empty)
(spec/def ::quote-source ::strings/non-empty)
(spec/def ::quote-timezone ::strings/non-empty)
(spec/def ::xcode ::strings/non-empty)
(spec/def ::fraction ::strings/non-empty)
