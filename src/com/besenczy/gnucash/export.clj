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
     (concat (cons contents more-contents)))))

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
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Ftaxtable/child nil)])))

(defn entry-element [{:keys [guid billable? invoice bill date entered description action quantity account price taxable? tax-table tax-included? discount-type discount-how discount payment]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/GncEntry
    {:version "2.0.0"}
    (filter-nonempty-contents
      [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/guid nil guid)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/date nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil date))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/entered nil
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fts/date nil entered))
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/description nil description)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/action nil action)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/billable nil billable?)
       (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/qty nil quantity)]
      (if invoice
        [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/invoice
           {:type "guid"} invoice)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-acct account)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-price price)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxable taxable?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxtable tax-table)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-taxincluded tax-included?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-type discount-type)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-disc-how discount-how)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/i-discount discount)])
      (if bill
        [(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/bill
           {:type "guid"} bill)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-acct account)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-price price)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxable taxable?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxtable tax-table)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-taxincluded tax-included?)
         (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fentry/b-pay payment)]))))

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

(defn schedxaction-element [{:keys []}])
(defn template-element [{:keys []}])
(defn budget-element [{:keys []}])

(defn countdata-element [[k v]]
  (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
    {:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd/type k} v))

(defn book-element [{:keys [id slots commodities customers vendors employees jobs invoices billing-terms tax-tables entries schedxactions tempxactions budgets counters prices accounts transactions]}]
  (xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book {:version "2.0.0"}
    ;[(x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id {:type "guid"} id)]
    ;[(xml-element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots nil (frame-contents slots))]
    ;[(pricedb-element prices)]
    ;(map commodity-element commodities)
    ;(map customer-element customers)
    ;(map vendor-element vendors)
    ;(map employee-element employees)
    ;(map job-element jobs)
    ;(map invoice-element invoices)
    ;(map billterm-element billing-terms)
    ;(map taxtable-element tax-tables)
    ;(map entry-element entries)
    ;(map countdata-element counters)
    (map account-element accounts)
    ;(map transaction-element transactions)
    )
  #_
  (apply x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book {:version "2.0.0"}
    (apply list
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id {:type "guid"} id)
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/slots nil (->frame slots))
      (->pricedb prices)
      (->count-data counters))))

(apply x/element :test {:foo "bar"} [4 5 6])

(defn document-element [{:keys [book counters]}]
  (xml-element :gnc-v2 nil
    [(book-element book)]
    (map countdata-element counters)))
