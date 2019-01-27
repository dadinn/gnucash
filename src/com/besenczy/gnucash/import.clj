(ns com.besenczy.gnucash.import
  (:require
   [com.besenczy.gnucash.utils :as utils]
   [clojure.edn :as edn]
   [clojure.java.io :as jio]
   [clojure.string :as s]
   [java-time :as jt]
   [clojure.zip :as z]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as x]))

(declare ->frame)

(defn ->slotvalue
  ([loc type kw]
   (case type
     "frame"
     ;; TODO effectively a hardcoded :slot, therefore breaks the recursion!
     [:frame
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        (->frame kw))]
     "string"
     [:string
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)]
     "integer"
     [:integer
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)]
     "numeric"
     [:numeric
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)]
     "gdate"
     [:gdate
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)]
     "guid"
     [:guid
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)]
     "timespec"
     [:timespec
      (zx/xml1-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value
        zx/text)])))

(defn ->slot [kw]
  (fn [loc]
    (let [k (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/key zx/text)
          t (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value (zx/attr :type))]
      (list [k (->slotvalue loc t kw)]))))

(defn ->frame
  ([] (->frame :slot))
  ([kw]
   {:pre [(keyword? kw)]}
   (fn [loc] (into {} (zx/xml-> loc kw (->slot kw))))))

(defn ->commodity [loc]
  (utils/into-map
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
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/id
      (zx/attr= :type "guid")
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
    :type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/type
      zx/text)
    :value
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/value
      zx/text)))

(defn ->lot [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Flot/id
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Flot/slots
      (->frame))))

(defn ->account [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/id
      (zx/attr= :type "guid")
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
      (zx/attr= :type "guid")
      zx/text)
    :commodity
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity
      ->commodity)
    :unit
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity-scu
      zx/text)
    :lots
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/lots
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/lot
     ->lot)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/slots
      (->frame :slot))))

(defn ->split [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/id
      (zx/attr= :type "guid")
      zx/text)
    :reconciled-state
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/reconciled-state
      zx/text)
    :reconciled-date
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/reconcile-date
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
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
      (zx/attr= :type "guid")
      zx/text)
    :memo
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/memo
      zx/text)
    :action
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/action
      zx/text)
    :lot
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/lot
      (zx/attr= type "guid")
      zx/text)))

(defn ->transaction [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/id
      (zx/attr= :type "guid")
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
    :num
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/num
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

(defn countdata-pair [loc]
  "Extract key-value pair from count-data XML element"
  (let [{:keys [tag attrs content] :as e} (z/node loc)
        k (-> attrs :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd/type)
        v (first content)]
    (list [k v])))

(defn ->address [loc]
  (utils/into-map
    :name
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/name zx/text)
    :line1
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr1 zx/text)
    :line2
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr2 zx/text)
    :line3
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr3 zx/text)
    :line4
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr4 zx/text)
    :phone
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/phone zx/text)
    :fax
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/fax zx/text)
    :email
    (zx/xml1-> loc :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/email zx/text)))

(defn ->customer [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/guid
      (zx/attr= :type "guid")
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/id
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/name
      zx/text)
    :active?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/active
      zx/text)
    :discount
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/discount
      zx/text)
    :credit-limit
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/credit
      zx/text)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/currency
      ->commodity)
    :terms
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/terms
      (zx/attr= :type "guid")
      zx/text)
    :tax-table
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/taxtable
      (zx/attr= :type "guid")
      zx/text)
    :billing-address
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/addr
      (zx/attr= :version "2.0.0")
      ->address)
    :shipping-address
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/shipaddr
      (zx/attr= :version "2.0.0")
      ->address)
    :tax-included
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/taxincluded
      zx/text)
    :use-tax-table?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/use-tt
      zx/text)
    :notes
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/notes
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/slots
      (->frame))))

(defn ->vendor [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/guid
      (zx/attr= :type "guid")
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/id
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/name
      zx/text)
    :active?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/active
      zx/text)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/currency
      ->commodity)
    :terms
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/terms
      (zx/attr= :type "guid")
      zx/text)
    :tax-table
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/taxtable
      (zx/attr= :type "guid")
      zx/text)
    :billing-address
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/addr
      (zx/attr= :version "2.0.0")
      ->address)
    :tax-included
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/taxincluded
      zx/text)
    :use-tax-table?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/use-tt
      zx/text)
    :notes
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/notes
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/slots
      (->frame))))

(defn ->employee [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/guid
      (zx/attr= :type "guid")
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/id
      zx/text)
    :username
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/username
      zx/text)
    :active?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/active
      zx/text)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/currency
      ->commodity)
    :workday
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/workday
      zx/text)
    :rate
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/rate
      zx/text)
    :billing-address
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/addr
      (zx/attr= :version "2.0.0")
      ->address)
    :language
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/language
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/slots
      (->frame))))

(defn ->owner [loc]
  (utils/into-map
    :type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fowner/type
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fowner/id
      (zx/attr= :type "guid")
      zx/text)))

(defn ->job [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/guid
      (zx/attr= :type "guid")
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/id
       zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/name
      zx/text)
    :reference
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/reference
      zx/text)
    :owner
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/owner
      (zx/attr= :version "2.0.0")
      ->owner)
    :active?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/active
      zx/text)))

(defn ->invoice [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/guid
      (zx/attr= :type "guid")
      zx/text)
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/id
      zx/text)
    :owner
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/owner
      (zx/attr= :version "2.0.0")
      ->owner)
    :billto
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/billto
      (zx/attr= :version "2.0.0")
      ->owner)
    :reference
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/billing_id
      zx/text)
    :active?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/active
      zx/text)
    :currency
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/currency
      ->commodity)
    :opened
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/opened
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :posted
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/posted
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :postlot
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/postlot
      (zx/attr= :type "guid")
      zx/text)
    :posttxn
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/posttxn
      (zx/attr= :type "guid")
      zx/text)
    :postacc
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/postacc
      (zx/attr= :type "guid")
      zx/text)
    :terms
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/terms
      (zx/attr= :type "guid")
      zx/text)
    :notes
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/notes
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/slots
      (->frame))))

(defn ->billterm [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/guid
      (zx/attr= :type "guid")
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/name
      zx/text)
    :description
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/desc
      zx/text)
    :refcount
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/refcount
      zx/text)
    :invisible?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/invisible
      zx/text)
    :parent
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/parent
      (zx/attr= :type "guid")
      zx/text)
    :due-days
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/days
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbt-days/due-days
      zx/text)
    :child
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/child
      (zx/attr= :type "guid")
      zx/text)))

(defn ->tt-entry [loc]
  (utils/into-map
    :account
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/acct
      (zx/attr= :type "guid")
      zx/text)
    :amount
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/amount
      zx/text)
    :type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/type
      zx/text)))

(defn ->taxtable [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/guid
      (zx/attr= :type "guid")
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/name
      zx/text)
    :refcount
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/refcount
      zx/text)
    :invisible?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/invisible
      zx/text)
    :parent
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/parent
      (zx/attr= :type "guid")
      zx/text)
    :entries
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/entries
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncTaxTableEntry
      ->tt-entry)
    :child
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/child
      (zx/attr= :type "guid")
      zx/text)))

(defn ->entry [loc]
  (utils/into-map
    :guid
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/guid
      zx/text)
    :billable?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/billable
      zx/text)
    :invoice
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/invoice
      (zx/attr= :type "guid")
      zx/text)
    :bill
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/bill
      (zx/attr= :type "guid")
      zx/text)
    :date-recorded
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/date
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :date-entered
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/entered
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date
      zx/text)
    :description
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/description
      zx/text)
    :action
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/action
      zx/text)
    :quantity
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/qty
      zx/text)
    :account
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-acct
      zx/text)
    :account
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-acct
      zx/text)
    :price
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-price
      zx/text)
    :price
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-price
      zx/text)
    :taxable?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxable
      zx/text)
    :taxable?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxable
      zx/text)
    :tax-table
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxtable
      zx/text)
    :tax-table
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxtable
      zx/text)
    :tax-included?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxincluded
      zx/text)
    :tax-included?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxincluded
      zx/text)
    :discount-type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-type
      zx/text)
    :discount-how
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-how
      zx/text)
    :discount
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-discount
      zx/text)
    :payment
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-pay
      zx/text)))

(defn ->recurrance [loc]
  (utils/into-map
    :start
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/start
      :gdate zx/text)
    :period-type
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/period_type
      zx/text)
    :multiplier
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/mult
      zx/text)
    :weekend-adjustment
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/weekend_adj
      zx/text)))

(defn ->schedxaction [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/id
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/name
      zx/text)
    :account
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/templ-acct
      zx/text)
    :enabled?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/enabled
      zx/text)
    :start
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/start
      :gdate zx/text)
    :end
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/end
      :gdate zx/text)
    :last
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/last
      :gdate zx/text)
    :schedule
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/schedule
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/recurrence
      ->recurrance)
    :auto-create?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/autoCreate
      zx/text)
    :auto-create-notify?
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/autoCreateNotify
      zx/text)
    :advance-create-days
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/advanceCreateDays
      zx/text)
    :advance-remind-days
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/advanceRemindDays
      zx/text)
    :instance-count
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/instanceCount
      zx/text)))

(defn ->tempxaction [loc]
  (utils/into-map
    :accounts
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/account
      ->account)
    :transactions
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/transaction
      ->transaction)))

(defn ->budget [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/id
      (zx/attr= :type "guid")
      zx/text)
    :name
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/name
      zx/text)
    :description
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/description
      zx/text)
    :num-periods
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/num-periods
      zx/text)
    :recurrence
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/recurrence
      (zx/attr= :version "1.0.0")
      ->recurrance)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/slots
      (->frame))))

(defn ->book [loc]
  (utils/into-map
    :id
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id
      (zx/attr= :type "guid")
      zx/text)
    :slots
    (zx/xml1-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots
      (->frame))
    :commodities
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/commodity
      (zx/attr= :version "2.0.0")
      ->commodity)
    :customers
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncCustomer
      (zx/attr= :version "2.0.0")
      ->customer)
    :vendors
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncVendor
      (zx/attr= :version "2.0.0")
      ->vendor)
    :employees
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncEmployee
      (zx/attr= :version "2.0.0")
      ->employee)
    :jobs
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncJob
      (zx/attr= :version "2.0.0")
      ->job)
    :invoices
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncInvoice
      (zx/attr= :version "2.0.0")
      ->invoice)
    :billing-terms
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncBillTerm
      (zx/attr= :version "2.0.0")
      ->billterm)
    :tax-tables
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncTaxTable
      (zx/attr= :version "2.0.0")
     ->taxtable)
    :entries
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncEntry
      (zx/attr= :version "2.0.0")
      ->entry)
    :schedxactions
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/schedxaction
      ->schedxaction)
    :tempxactions
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/template-transactions
      ->tempxaction)
    :budgets
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/budget
      ->budget)
    :counters
    (into {}
      (zx/xml-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        countdata-pair))
    :prices
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/pricedb
      (zx/attr= :version "1")
      :price
      ->price)
    :accounts
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/account
      (zx/attr= :version "2.0.0")
      ->account)
    :transactions
    (zx/xml-> loc
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/transaction
      (zx/attr= :version "2.0.0")
      ->transaction)))

(defn ->document [loc]
  (utils/into-map
    :book
    (zx/xml1-> loc
      :gnc-v2
      :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      (zx/attr= :version "2.0.0")
      ->book)
    :counters
    (into {}
      (zx/xml-> loc
        :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        countdata-pair))))
