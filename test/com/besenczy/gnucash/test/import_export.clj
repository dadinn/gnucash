(ns com.besenczy.gnucash.test.import-export
  (:require
   [com.besenczy.gnucash.export :as export]
   [com.besenczy.gnucash.import :as import]
   [com.besenczy.gnucash.specs.common :as common-specs]
   [com.besenczy.gnucash.specs.entities :as entity-specs]
   [com.besenczy.gnucash.specs.slot :as slot-specs]
   [com.besenczy.gnucash.specs.book :as book]
   [com.besenczy.gnucash.specs :as specs]
   [com.besenczy.gnucash.test.common :refer [is=]]
   [clojure.spec.alpha :as spec]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as spectest]
   [clojure.zip :as z]
   [clojure.data.xml :as x]
   [clojure.test :refer :all]))

(defmacro deftest-recursive [sym & body]
  (let [limit (or (-> sym meta :limit) 3)]
    `(deftest ~sym
       (binding [spec/*recursion-limit* ~limit]
         ~@body))))

(deftest-recursive ^{:limit 10} slot-frame
  (testing "check that slots frame records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::slot-specs/frame))]
        (let [exported (export/frame-contents record)
              exported (apply x/element :testing nil exported)
              imported ((import/frame) (z/xml-zip exported))]
          (is= record imported))))))

(deftest counters
  (testing "check that countdata records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::common-specs/counters))]
        (let [exported (map export/countdata-element record)
              imported (map (comp import/countdata-pair z/xml-zip) exported)
              imported (apply concat imported)
              imported (into {} imported)]
          (is= record imported))))))

(deftest commodity
  (testing "check commodity records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::common-specs/commodity))]
        (let [exported (export/commodity-element :test/commodity record)
              imported (import/commodity (z/xml-zip exported))]
          (is= record imported))))))

(deftest price
  (testing "check price records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/price))]
        (let [exported (export/price-element record)
              imported (import/price (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive account
  (testing "check account records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/account))]
        (let [exported (export/account-element record)
              imported (import/account (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive transaction
  (testing "check transaction records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/transaction))]
        (let [exported (export/transaction-element record)
              imported (import/transaction (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive customer
  (testing "check customer records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/customer))]
        (let [exported (export/customer-element record)
              imported (import/customer (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive vendor
  (testing "check vendor records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/vendor))]
        (let [exported (export/vendor-element record)
              imported (import/vendor (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive employee
  (testing "check employee records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/employee))]
        (let [exported (export/employee-element record)
              imported (import/employee (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive job
  (testing "check job records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/job))]
        (let [exported (export/job-element record)
              imported (import/job (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive invoice
  (testing "check invoice records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/invoice))]
        (let [exported (export/invoice-element record)
              imported (import/invoice (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive billterm
  (testing "check billterm records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/billterm))]
        (let [exported (export/billterm-element record)
              imported (import/billterm (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive taxtable
  (testing "check taxtable records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/taxtable))]
        (let [exported (export/taxtable-element record)
              imported (import/taxtable (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive entry
  (testing "check entry records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/entry))]
        (let [exported (export/entry-element record)
              imported (import/entry (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive budget
  (testing "check budget records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/budget))]
        (let [exported (export/budget-element record)
              imported (import/budget (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive schedxaction
  (testing "check schedxaction records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::entity-specs/schedxaction))]
        (let [exported (export/schedxaction-element record)
              imported (import/schedxaction (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive tempxaction
  (testing "check tempxaction records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (first (gen/sample (spec/gen ::book/tempxactions)))]
        (let [exported (export/tempxaction-element record)
              imported (import/tempxaction (z/xml-zip exported))]
          (is= record imported))))))

(deftest-recursive document
  (testing "check document records get reimported correctly"
    (testing "when generated via specs"
      (doseq [record (gen/sample (spec/gen ::specs/document))]
        (let [exported (export/document-element record)
              imported (import/document (z/xml-zip exported))]
          (is= record imported))))))
