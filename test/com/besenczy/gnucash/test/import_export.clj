(ns com.besenczy.gnucash.test.import-export
  (:require
   [com.besenczy.gnucash.export :as export]
   [com.besenczy.gnucash.import :as import]
   [com.besenczy.gnucash.specs.common :as common-specs]
   [com.besenczy.gnucash.specs.entities :as entity-specs]
   [com.besenczy.gnucash.specs.slot :as slot-specs]
   [com.besenczy.gnucash.specs.book :as book]
   [com.besenczy.gnucash.specs :as specs]
   [clojure.zip :as z]
   [clojure.data.xml :as x]
   [clojure.spec.alpha :as spec]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :as test]))

(defn export-reimport-prop
  [spec export-fn import-fn]
  (prop/for-all [original (spec/gen spec)]
    (let [exported (export-fn original)
          imported (import-fn exported)]
      (= original imported))))

(defmacro defspec
  [name spec export-fn import-fn
   & {:keys [times max-size]
      :or {times 100 max-size 500}
      :as options}]
  `(test/defspec ~name
     ~(assoc options :num-tests times :max-size max-size)
     (export-reimport-prop ~spec ~export-fn ~import-fn)))

(defspec slot-frame ::slot-specs/frame
  #(apply x/element :testing nil (export/frame-contents %))
  #((import/frame) (z/xml-zip %)))

(defspec counters ::common-specs/counters
  (partial map export/countdata-element)
  (fn [exported]
    (->> exported
      (map (comp import/countdata-pair z/xml-zip))
      (apply concat)
      (into {}))))

(defspec commodity ::common-specs/commodity
  (partial export/commodity-element :test/commodity)
  (comp import/commodity z/xml-zip))

(defspec price ::entity-specs/price
  export/price-element (comp import/price z/xml-zip))

(defspec account ::entity-specs/account
  export/account-element (comp import/account z/xml-zip))

(defspec transaction ::entity-specs/transaction
  export/transaction-element (comp import/transaction z/xml-zip))

(defspec customer ::entity-specs/customer
  export/customer-element (comp import/customer z/xml-zip))

(defspec vendor ::entity-specs/vendor
  export/vendor-element (comp import/vendor z/xml-zip))

(defspec employee ::entity-specs/employee
  export/employee-element (comp import/employee z/xml-zip))

(defspec job ::entity-specs/job
  export/job-element (comp import/job z/xml-zip))

(defspec invoice ::entity-specs/invoice
  export/invoice-element (comp import/invoice z/xml-zip))

(defspec billterm ::entity-specs/billterm
  export/billterm-element (comp import/billterm z/xml-zip))

(defspec taxtable ::entity-specs/taxtable
  export/taxtable-element (comp import/taxtable z/xml-zip))

(defspec entry ::entity-specs/entry
  export/entry-element (comp import/entry z/xml-zip))

(defspec budget ::entity-specs/budget
  export/budget-element (comp import/budget z/xml-zip))

(defspec schedxaction ::entity-specs/schedxaction
  export/schedxaction-element (comp import/schedxaction z/xml-zip))

(defspec tempxaction ::book/tempxactions
  export/tempxactions-element (comp import/tempxactions z/xml-zip)
  :times 10 :max-size 10)

(defspec document ::specs/document
  export/document-element (comp import/document z/xml-zip)
  :times 10 :max-size 10)
