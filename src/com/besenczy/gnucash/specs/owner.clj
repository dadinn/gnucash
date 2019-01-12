(ns com.besenczy.gnucash.specs.owner
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [clojure.string :as string]
   [clojure.spec.alpha :as spec]))

(spec/def ::id ::common/guid)
(spec/def ::type
  (spec/and
    #{"gncCustomer" "gncVendor"}
    (spec/conformer
      {"gncCustomer" :customer
       "gncVendor" :vendor}
      {:customer "gncCustomer"
       :vendor "gncVendor"})))
