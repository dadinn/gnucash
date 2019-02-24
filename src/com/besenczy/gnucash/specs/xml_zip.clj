(ns com.besenczy.gnucash.specs.xml-zip
  (:require
   [com.besenczy.gnucash.specs.xml :as xml]
   [clojure.spec.alpha :as spec]))

(spec/def ::node ::xml/element)

(spec/def ::l (spec/coll-of ::xml/content))
(spec/def ::r (spec/coll-of ::xml/content))
(spec/def ::pnodes (spec/coll-of ::node))
(spec/def ::ppath (spec/nilable ::zipper))

(spec/def ::zipper
  (spec/keys
    :req-un [::l ::r ::pnodes ::ppath]))

(spec/def ::loc
  (spec/tuple ::node ::ppath))
