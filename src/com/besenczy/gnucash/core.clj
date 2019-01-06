(ns com.besenczy.gnucash.core
  (:require
   [com.besenczy.gnucash.protocols :as proto]
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

(defn parse-date [time-str]
  (->> time-str
    (re-find #"[0-9]{4}-[0-9]{2}-[0-9]{2}")
    (jt/local-date (jt/formatter "yyyy-MM-dd"))))

(defn parse-time [time-str]
  (->> time-str
    (re-find #"[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")
    (jt/local-date-time (jt/formatter "yyyy-MM-dd hh:mm:ss"))))

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
       zx/text edn/read-string)
     "gdate"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text parse-date)
     "guid"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text)
     "timespec"
     (zx/xml1-> loc
       :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
       zx/text parse-time))))

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
  (let [space (zx/xml1-> loc :space zx/text)
        id (zx/xml1-> loc :id zx/text)]
    {:space space
     :id id}))

(defn ->commodity [loc]
  {:space (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/space zx/text)
   :id (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/id zx/text)})

(defn ->price [loc]
  (let [id (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/id zx/text)
        commodity (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/commodity ->commodity)
        currency (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/currency ->commodity)
        date (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/time zx/text parse-date)
        source (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/source zx/text)
        v (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/value zx/text)
        v (edn/read-string v)]
    {:id id
     :commodity commodity
     :currency currency
     :date date
     :source source
     :value v}))

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
  (let [id (zx/xml1-> loc :id zx/text)
        name (zx/xml1-> loc :name zx/text)
        code (zx/xml1-> loc :code zx/text)
        desc (zx/xml1-> loc :description zx/text)
        type (zx/xml1-> loc :type zx/text)
        type (account-type type)
        parent (zx/xml1-> loc :parent zx/text)
        commodity (zx/xml1-> loc :commodity ->commodity)
        unit (zx/xml1-> loc :commodity-scu zx/text)
        unit (edn/read-string unit)]
    {:id id
     :name name
     :description desc
     :code code
     :type type
     :parent parent
     :commodity commodity
     :unit unit}))

(defn ->transaction [loc]
  (let [id (zx/xml1-> loc :id zx/text)]))

(defn countdata-pair [e]
  "Extract key-value pair from count-data XML element"
  (let [k (-> e :attrs :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd/type)
        v (edn/read-string (first (:content e)))]
    [k v]))

(defrecord GnucashDocument [content]
  proto/Book
  (slots [this]
    (zx/xml1-> content
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots
      (->frame)))
  (counters [this]
    (into {}
      (map countdata-pair)
      (zx/xml-> content :gnc-v2
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        z/node)))
  (prices [this]
    (zx/xml-> content
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/pricedb
      :price
      ->price))
  (accounts [this]
    (zx/xml-> content
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/account
      ->account))
  (transactions [this]
    (zx/xml-> content
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/transaction
      ->transaction)))

(defn load-book [path]
  (-> (slurp path)
    (x/parse-str)
    (z/xml-zip)
    (->GnucashDocument)))

(defn emit-book [{:keys [content]}]
  (x/emit-str (z/root content)))
