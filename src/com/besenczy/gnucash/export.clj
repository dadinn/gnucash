(ns com.besenczy.gnucash.export
  (:require
   [com.besenczy.gnucash.utils :as utils]
   [clojure.edn :as edn]
   [clojure.java.io :as jio]
   [clojure.string :as s]
   [java-time :as jt]
   [clojure.zip :as z]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as x]))

(def empty-content? (comp utils/empty-seq? :content))

(defn filter-nonempty-contents
  ([contents]
   (remove empty-content? contents))
  ([contents & more-contents]
   (filter-nonempty-contents
     (apply concat (cons contents more-contents)))))

(defn xml-element [tag attr & contents]
  (apply x/element tag attr
    (let [contents (apply concat contents)
          contents (interpose "\n" contents)]
      (if (< 1 (count contents))
        (cons "\n" contents)
        contents))))

(declare frame-contents)

(def primitive-types #{:string :integer :numeric :guid :gdate :timespec})

(defn slot-value [t v kw]
  [(name t)
   (cond
     (= t :frame) (frame-contents kw v)
     (primitive-types t) [v]
     :else (throw (ex-info "Unknown slot type" {:type t :value v})))])

(defn frame-contents
  ([m]
   (frame-contents :slot m))
  ([kw m]
   (map
     (fn [[k [t v]]]
       (x/element kw nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/key nil k)
         (let [[t v] (slot-value t v kw)]
           (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fslot/value {:type t} v))))
     (seq m))))

(defn commodity-element
  ([tag content]
   (commodity-element tag nil content))
  ([tag attr {:keys [id space name get-quotes quote-source quote-timezone xcode fraction]}]
   (xml-element tag attr
     (filter-nonempty-contents
       [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/id nil id)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/space nil space)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/name nil name)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/get_quotes nil get-quotes)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/quote_source nil quote-source)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/quote_tz nil quote-timezone)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/xcode nil xcode)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/fraction nil fraction)]))))

(defn price-element [{:keys [id commodity currency date source type value]}]
  (xml-element :price nil
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/id {:type "guid"} id)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/commodity commodity)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/currency currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/time nil date)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/source nil source)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/type nil type)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fprice/value nil value)])))

(defn pricedb-element [prices]
  (apply x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/pricedb
    {:version "1"}
    (filter-nonempty-contents
      (map price-element prices))))

(defn address-element
  ([tag content]
   (address-element tag {:version "2.0.0"} content))
  ([tag attr {:keys [name line1 line2 line3 line4 phone fax email]}]
   (xml-element tag attr
     (filter-nonempty-contents
       [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/name nil name)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr1 nil line1)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr2 nil line2)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr3 nil line3)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/addr4 nil line4)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/phone nil phone)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/fax nil fax)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Faddr/email nil email)]))))

(defn customer-element [{:keys [guid id name active? discount credit-limit currency terms tax-table   billing-address shipping-address tax-included use-tax-table? notes slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncCustomer
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/id nil id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/active nil active?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/discount nil discount)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/credit nil credit-limit)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/currency nil currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/terms
         {:type "guid"} terms)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/taxtable
         {:type "guid"} tax-table)
       (address-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/addr billing-address)
       (address-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/shipaddr shipping-address)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/taxincluded nil tax-included)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/use-tt nil use-tax-table?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/notes nil notes)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcust/slots nil (frame-contents slots))])))

(defn vendor-element [{:keys [guid id name active? currency terms tax-table billing-address tax-included use-tax-table? notes slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncVendor
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/id nil id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/active nil active?)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/currency currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/terms
         {:type "guid"} terms)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/taxtable
         {:type "guid"} tax-table)
       (address-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/addr billing-address)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/taxincluded nil tax-included)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/use-tt nil use-tax-table?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/notes nil notes)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fvendor/slots nil (frame-contents slots))])))

(defn employee-element [{:keys [guid id username active? currency workday rate billing-address language slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncEmployee
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/id nil id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/username nil username)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/active nil active?)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/currency currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/workday nil workday)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/rate nil rate)
       (address-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/addr billing-address)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/language nil language)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Femployee/slots
         nil (frame-contents slots))])))

(defn owner-element
  ([tag record]
   (owner-element tag {:version "2.0.0"} record))
  ([tag attr {:keys [type id]}]
   (x/element tag attr
     (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fowner/type nil type)
     (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fowner/id {:type "guid"} id))))

(defn job-element [{:keys [guid id name reference owner active?]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncJob
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/id nil id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/reference nil reference)
       (owner-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/owner owner)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fjob/active nil active?)])))

(defn invoice-element [{:keys [guid id owner billto reference active? currency opened posted postlot posttxn postacc terms notes slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncInvoice
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/guid {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/id nil id)
       (owner-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/owner owner)
       (owner-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/billto billto)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/billing_id nil reference)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/active nil active?)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/currency currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/opened nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil opened))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/posted nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil posted))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/postlot
         {:type "guid"} postlot)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/posttxn
         {:type "guid"} posttxn)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/postacc
         {:type "guid"} postacc)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/terms
         {:type "guid"} terms)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/notes nil notes)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Finvoice/slots
         nil (frame-contents slots))])))

(defn billterm-element [{:keys [guid name description refcount invisible? parent due-days child]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncBillTerm
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/desc nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/refcount nil refcount)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/invisible nil invisible?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/parent
         {:type "guid"} parent)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/days nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbt-days/due-days nil due-days))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbillterm/child
         {:type "guid"} child)])))

(defn ttentry-element [{:keys [account amount type]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncTaxTableEntry nil
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/acct {:type "guid"} account)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/amount nil amount)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftte/type nil type)])))

(defn taxtable-element [{:keys [guid name refcount invisible? parent entries child]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncTaxTable
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/refcount nil refcount)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/invisible nil invisible?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/parent
         {:type "guid"} parent)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/entries nil
         (map ttentry-element entries))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/child
         {:type "guid"} child)])))

(defn entry-element [{:keys [guid billable? invoice bill date entered description action quantity account price taxable? tax-table tax-included? discount-type discount-how discount payment]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncEntry
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/guid
         {:type "guid"} guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/date nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil date))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/entered nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil entered))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/description nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/action nil action)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/billable nil billable?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/qty nil quantity)])
    (cond
      invoice
      (filter-nonempty-contents
        [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/invoice
           {:type "guid"} invoice)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-acct
           {:type "guid"} account)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-price nil price)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxable nil taxable?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxtable
           {:type "guid"} tax-table)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxincluded nil tax-included?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-type nil discount-type)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-how nil discount-how)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-discount nil discount)])
      bill
      (filter-nonempty-contents
        [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/bill
           {:type "guid"} bill)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-acct
           {:type "guid"} account)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-price nil price)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxable nil taxable?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxtable
           {:type "guid"} tax-table)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxincluded nil tax-included?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-pay nil payment)]))))

(defn lot-element [{:keys [id slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/lot nil
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Flot/id nil id)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Flot/slots nil
         (frame-contents slots))])))

(defn account-element [{:keys [id name description code type parent commodity unit lots slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/account
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/id
         {:type "guid"} id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/description nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/code nil code)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/type nil type)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/parent
         {:type "guid"} parent)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity commodity)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/commodity-scu nil unit)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/lots nil
         (map lot-element lots))
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fact/slots nil
         (frame-contents slots))])))

(defn split-element [{:keys [id reconciled-state reconciled-date value quantity account memo action lot]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/split nil
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/id {:type "guid"} id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/reconciled-state nil reconciled-state)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/reconcile-date nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil reconciled-date))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/value nil value)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/quantity nil quantity)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/account {:type "guid"} account)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/memo nil memo)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/action nil action)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsplit/lot {:type "guid"} lot)])))

(defn transaction-element [{:keys [id currency date-entered date-posted description num slots splits]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/transaction
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/id {:type "guid"} id)
       (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/currency currency)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/date-entered nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil date-entered))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/date-posted nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil date-posted))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/description nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/num nil num)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/slots
         nil (frame-contents slots))
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftrn/splits
         nil (map split-element splits))])))

(defn recurrence-element
  ([content]
   (recurrence-element
     :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/recurrence
     {:version "1.0.0"}
     content))
  ([tag content]
   (recurrence-element tag {:version "1.0.0"} content))
  ([tag attr {:keys [start period-type multiplier weekend-adjustment]}]
   (xml-element tag attr
     (filter-nonempty-contents
       [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/start nil
          (x/element :gdate nil start))
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/period_type nil period-type)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/mult nil multiplier)
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Frecurrence/weekend_adj nil weekend-adjustment)]))))

(defn schedxaction-element
  [{:keys [id name account enabled? start schedule auto-create? auto-create-notify? advance-create-days advance-remind-days end last instance-count]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/schedxaction
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/id
         {:type "guid"} id)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/name nil name)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/templ-acct
         {:type "guid"} account)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/enabled nil enabled?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/start nil
         (x/element :gdate nil start))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/end nil
         (x/element :gdate nil end))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/last nil
         (x/element :gdate nil last))
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/schedule nil
         (map recurrence-element schedule))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/autoCreate nil auto-create?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/autoCreateNotify nil auto-create-notify?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/advanceCreateDays nil advance-create-days)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/advanceRemindDays nil advance-remind-days)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fsx/instanceCount nil instance-count)])))

(defn tempxactions-element [{:keys [accounts transactions]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/template-transactions nil
    (filter-nonempty-contents
      (map account-element accounts)
      (map transaction-element transactions))))

(defn budget-element [{:keys [id name description num-periods recurrence slots]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/budget
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/id
         {:type "guid"} id)
       (x/element
         :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/name nil name)
       (x/element
         :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/description nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/num-periods nil num-periods)
       (recurrence-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/recurrence recurrence)
       (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbgt/slots nil (frame-contents slots))])))

(defn countdata-element [[k v]]
  (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
    {:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd/type k} v))

(defn book-element [{:keys [id slots commodities customers vendors employees jobs invoices billing-terms tax-tables entries schedxactions tempxactions budgets counters prices accounts transactions]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
    {:version "2.0.0"}
    [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id {:type "guid"} id)
     (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots nil (frame-contents slots))
     (pricedb-element prices)]
    (map
      (fn [content]
        (commodity-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/commodity
          {:version "2.0.0"} content))
      commodities)
    (map customer-element customers)
    (map vendor-element vendors)
    (map employee-element employees)
    (map job-element jobs)
    (map invoice-element invoices)
    (map billterm-element billing-terms)
    (map taxtable-element tax-tables)
    (map entry-element entries)
    (map account-element accounts)
    (map transaction-element transactions)
    (map schedxaction-element schedxactions)
    [(tempxactions-element tempxactions)]
    (map budget-element budgets)
    (map countdata-element counters)))

(def prefixes
  {:xmlns/gnc "http://www.gnucash.org/XML/gnc"
   :xmlns/act "http://www.gnucash.org/XML/act"
   :xmlns/book "http://www.gnucash.org/XML/book"
   :xmlns/cd "http://www.gnucash.org/XML/cd"
   :xmlns/cmdty "http://www.gnucash.org/XML/cmdty"
   :xmlns/price "http://www.gnucash.org/XML/price"
   :xmlns/slot "http://www.gnucash.org/XML/slot"
   :xmlns/split "http://www.gnucash.org/XML/split"
   :xmlns/sx "http://www.gnucash.org/XML/sx"
   :xmlns/trn "http://www.gnucash.org/XML/trn"
   :xmlns/ts "http://www.gnucash.org/XML/ts"
   :xmlns/fs "http://www.gnucash.org/XML/fs"
   :xmlns/bgt "http://www.gnucash.org/XML/bgt"
   :xmlns/recurrence "http://www.gnucash.org/XML/recurrence"
   :xmlns/lot "http://www.gnucash.org/XML/lot"
   :xmlns/addr "http://www.gnucash.org/XML/addr"
   :xmlns/owner "http://www.gnucash.org/XML/owner"
   :xmlns/billterm "http://www.gnucash.org/XML/billterm"
   :xmlns/bt-days "http://www.gnucash.org/XML/bt-days"
   :xmlns/bt-prox "http://www.gnucash.org/XML/bt-prox"
   :xmlns/cust "http://www.gnucash.org/XML/cust"
   :xmlns/employee "http://www.gnucash.org/XML/employee"
   :xmlns/entry "http://www.gnucash.org/XML/entry"
   :xmlns/invoice "http://www.gnucash.org/XML/invoice"
   :xmlns/job "http://www.gnucash.org/XML/job"
   :xmlns/order "http://www.gnucash.org/XML/order"
   :xmlns/taxtable "http://www.gnucash.org/XML/taxtable"
   :xmlns/tte "http://www.gnucash.org/XML/tte"
   :xmlns/vendor "http://www.gnucash.org/XML/vendor"
   })

(defn document-element [{:keys [book counters]}]
  (xml-element :gnc-v2 prefixes
    [(book-element book)]
    (map countdata-element counters)))
