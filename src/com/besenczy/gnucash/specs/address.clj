(ns com.besenczy.gnucash.specs.address
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::name string?)

(spec/def ::line1 string?)
(spec/def ::line2 string?)
(spec/def ::line3 string?)
(spec/def ::line4 string?)

(spec/def ::phone string?)
(spec/def ::fax string?)
(spec/def ::email string?)

