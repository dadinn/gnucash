(ns com.besenczy.gnucash.core
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as spec]
   [clojure.java.io :as jio]
   [clojure.string :as s]
   [java-time :as jt]
   [clojure.zip :as z]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as x])
  (:import
   [java.util UUID]))

(defn void?
  "returns true if `x` is either nil, or an empty list, vector, map, or string"
  [x]
  (or (nil? x)
    (and (or (list? x) (vector? x) (map? x) (string? x))
      (not (seq x)))))

(defn make-hashmap
  "create a hashmap from key value pairs using pairs with non-void values"
  [& kvs]
  #_
  (reduce
    (fn [acc [k v]] (if (meh? v) acc (assoc acc k v)))
    {} (partition 2 kvs))
  (into {}
    (comp
      (map vec)
      (remove (comp void? second)))
    (partition 2 kvs)))

(declare ->frame)

(defn ->slotvalue
  ([loc type slot-key]
   (case type
     "frame"
     ;; TODO effectively a hardcoded :slot, therefore breaks the recursion!
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       (->frame slot-key))
     "string"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text)
     "integer"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text)
     "gdate"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text)
     "guid"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text)
     "timespec"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text))))

(defn ->slot [slot-key]
  (fn [loc]
    (let [k (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/key zx/text)
          t (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value (zx/attr :type))]
      (list [k (->slotvalue loc t slot-key)]))))

(defn ->frame
  ([] (->frame :slot))
  ([slot-key]
   {:pre [(keyword? slot-key)]}
   (fn [loc] (into {} (zx/xml-> loc slot-key (->slot slot-key))))))

(defn ->commodity [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/id
      zx/text)
    :space
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/space
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/name
      zx/text)
    :get-quotes
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/get_quotes
      zx/text)
    :quote-source
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/quote_source
      zx/text)
    :quote-timezone
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/quote_tz
      zx/text)
    :xcode
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/xcode
      zx/text)
    :fraction
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/fraction
      zx/text)))

(defn ->price [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/id
      zx/text)
    :commodity
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/commodity
      ->commodity)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/currency
      ->commodity)
    :date
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/time
      zx/text)
    :source
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/source
      zx/text)
    :value
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/value
      zx/text)))

(def account-type
  {"ROOT" :root
   "ASSET" :asset
   "BANK" :bank
   "EQUITY" :equity
   "INCOME" :income
   "EXPENSE" :expense
   "RECEIVABLE" :receive
   "PAYABLE" :payable
   "LIABILITY" :liability})

(defn ->account [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/id
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/name
      zx/text)
    :description
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/description
      zx/text)
    :code
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/code
      zx/text)
    :type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/type
      zx/text)
    :parent
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/parent
      zx/text)
    :commodity
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity
      ->commodity)
    :unit
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity-scu
      zx/text)))

(defn ->split [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/id
      zx/text)
    :reconciled-state
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/reconciled-state
      zx/text)
    :value
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/value
      zx/text)
    :quantity
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/quantity
      zx/text)
    :account
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/account
      zx/text)))

(defn ->transaction [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/id
      zx/text)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/currency
      ->commodity)
    :date-entered
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/date-entered
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :date-posted
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/date-posted
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :description
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/description
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/slots
      (->frame))
    :splits
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/splits
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/split
      ->split)))

(defn countdata-pair [{:keys [tag attrs content] :as e}]
  "Extract key-value pair from count-data XML element"
  (let [k (-> attrs :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd/type)
        v (first content)]
    [k v]))

(defn ->book [loc]
  (make-hashmap
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots
      (->frame))
    :commodities
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/commodity
      ->commodity)
    :counters
    (into {}
      (map countdata-pair)
      (zx/xml-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        z/node))
    :prices
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/pricedb
      :price
      ->price)
    :accounts
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/account
      ->account)
    :transactions
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/transaction
      ->transaction)))

(defn ->document [loc]
  (make-hashmap
    :book
    (zx/xml1-> loc
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      ->book)
    :counters
    (into {}
      (map countdata-pair)
      (zx/xml-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        z/node))))

(defn load-doc [path]
  (-> (slurp path)
    (x/parse-str)
    (z/xml-zip)
    (->document)))

(defn emit-book [{:keys [content]}]
  (x/emit-str (z/root content)))
