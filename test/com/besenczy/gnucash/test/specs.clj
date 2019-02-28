(ns com.besenczy.gnucash.test.specs
  (:require
   [com.besenczy.gnucash.specs.numeric :as numeric]
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.slot :as slot]
   [com.besenczy.gnucash.specs.entities :as entities]
   [com.besenczy.gnucash.test.common :refer [is=]]
   [java-time :as jt]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]))

(deftest guid
  (testing "guid entry should conform to spec"
    (is= #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66"
      (spec/conform ::common/guid "c8d46fc6d8aff3951364615b3de94a66"))))

(deftest numeric
  (testing "numeric entry should conform to spec"
    (let [data "42"
          conformed (spec/conform ::numeric/natural data)]
      (is= conformed 42)
      (is= data (spec/unform ::numeric/natural conformed)))
    (let [data "42"
          conformed (spec/conform ::numeric/integer data)]
      (is= conformed 42)
      (is= data (spec/unform ::numeric/integer conformed)))
    (let [data "-42"
          conformed (spec/conform ::numeric/integer data)]
      (is= conformed -42)
      (is= data (spec/unform ::numeric/integer conformed)))
    (let [data "111111/111111"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num 111111 :den 111111})
      (is= data (spec/unform ::numeric/fraction conformed)))
    (let [data "-111111/111111"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num -111111 :den 111111})
      (is= data (spec/unform ::numeric/fraction conformed)))
    (let [data "11/10000"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num 11 :den 10000})
      (is= data (spec/unform ::numeric/fraction conformed)))
    (let [data "-11/10000"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num -11 :den 10000})
      (is= data (spec/unform ::numeric/fraction conformed)))
    (let [data "11/-111111"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num 11 :den -111111})
      (is= data (spec/unform ::numeric/fraction conformed)))
    (let [data "-11/-111111"
          conformed (spec/conform ::numeric/fraction data)]
      (is= conformed {:num -11 :den -111111})
      (is= data (spec/unform ::numeric/fraction conformed)))))

(deftest slots
  (testing "guid slot value should conform to spec"
    (let [value [:guid "d6e87f32ce9c437ffee9704ad7fc8bf4"]
          conformed (spec/conform ::slot/value [:guid "d6e87f32ce9c437ffee9704ad7fc8bf4"])]
      (is= conformed {:type :guid, :value #uuid "d6e87f32-ce9c-437f-fee9-704ad7fc8bf4"})
      (is= value (spec/unform ::slot/value conformed))))
  (testing "integer slot value should conform to spec"
    (is=
      (spec/conform ::slot/value [:integer "11"])
      {:type :integer, :value 11}))

  (testing "numeric slot value should conform to spec"
    (is=
      (spec/conform ::slot/value [:numeric "11/11111"])
      {:type :numeric, :value {:num 11 :den 11111}}))

  (testing "gdate slot value should conform to spec"
    (is=
      (spec/conform ::slot/value [:gdate "2001-01-01"])
      {:type :gdate, :value (jt/local-date "2001-01-01")}))

  (testing "timespec slot value should conform to spec"
    (is=
      (spec/conform ::slot/value [:timespec "2001-01-01 10:10:10 +1000"])
      {:type :timespec, :value (jt/zoned-date-time "2001-01-01T10:10:10+10:00")}))

  (testing "string slot values should conform to spec"
    (is=
      (spec/conform ::slot/value [:string "11"])
      {:type :string, :value "11"})
    (is=
      (spec/conform ::slot/value [:string "11/11111"])
      {:type :string, :value "11/11111"})
    (is=
      (spec/conform ::slot/value [:string "2001-01-01"])
      {:type :string, :value "2001-01-01"})
    (is=
      (spec/conform ::slot/value [:string "2001-01-01 10:10:10 +1000"])
      {:type :string, :value "2001-01-01 10:10:10 +1000"}))
  (testing "slot frame entity should conform to spec"
    (is=
      (spec/conform ::slot/value
        [:frame
         {"baz" [:integer "11"]
          "bazz" [:gdate "2001-01-01"]
          "bazzz" [:timespec "2001-01-01 10:10:10 +1000"]
          "bazzzz" [:numeric "11/11111"]
          "bax" [:string "Bla Bla Bla"]}])
      {:type :frame
       :value
       {"baz" {:type :integer, :value 11},
        "bazz" {:type :gdate, :value (jt/local-date "2001-01-01")},
        "bazzz" {:type :timespec, :value (jt/zoned-date-time "2001-01-01T10:10:10+10:00")},
        "bazzzz" {:type :numeric, :value {:num 11 :den 11111}},
        "bax" {:type :string, :value "Bla Bla Bla"}}}))
  (testing "nested slot frame entity should conform to spec"
    (is=
      (spec/conform ::slot/frame
        {"foo"
         [:guid "d6e87f32ce9c437ffee9704ad7fc8bf4"]
         "bar"
         [:frame
          {"baz" [:integer "11"]
           "bazz" [:gdate "2001-01-01"]
           "bazzz" [:timespec "2001-01-01 10:10:10 +1000"]
           "bazzzz" [:numeric "11/11111"]
           "bazzzzz" [:string "Bla Bla Bla"]}]})
      {"foo"
       {:type :guid, :value #uuid "d6e87f32-ce9c-437f-fee9-704ad7fc8bf4"}
       "bar"
       {:type :frame
        :value
        {"baz" {:type :integer, :value 11}
         "bazz" {:type :gdate, :value (jt/local-date "2001-01-01")}
         "bazzz" {:type :timespec, :value (jt/zoned-date-time "2001-01-01T10:10:10+10:00")}
         "bazzzz" {:type :numeric, :value {:num 11 :den 11111}}
         "bazzzzz" {:type :string, :value "Bla Bla Bla"}}}})))

(deftest price
  (testing "price entity should conform to spec"
    (is=
      (spec/conform ::entities/price
        {:id "87546b912076bf6944d3f32ae7d8c70a"
         :commodity {:id "EUR", :space "ISO4217"}
         :currency {:id "GBP", :space "ISO4217"}
         :date " 2015-03-29 00:00:00 +0000 "
         :source "user:xfer-dialog"
         :value "73/100"})
      {:id #uuid "87546b91-2076-bf69-44d3-f32ae7d8c70a"
       :commodity {:id "EUR", :space "ISO4217"}
       :currency {:id "GBP", :space "ISO4217"}
       :date
       (spec/conform ::common/datetime
         "2015-03-29 00:00:00 +0000")
       :source "user:xfer-dialog"
       :value {:num 73 :den 100}})))

(deftest account
  (testing "account entity should conform to spec"
    (testing "with lots"
      (is=
        (spec/conform ::entities/account
          {:id "ed0a209cc78f680c45a87851ed232236",
           :name "XSX",
           :type "RECEIVABLE",
           :parent "78ff533d1a66deb75cfb9a6a3e2c7cf8",
           :commodity {:id "GBP", :space "ISO4217"},
           :unit "100",
           :lots
           [{:id "00000000000000000000000000000000"}
            {:id "02be4b91bfe90a4a7098fdb3bf53cc7d",
             :slots
             {"gncInvoice"
              [:frame
               {"invoice-guid" [:guid "6a350cbdc78221e92a8c456e5def308f"]}],
              "title" [:string "Invoice 000026"]}}
            {:id "341b2690a35da27d6a21018085941222",
             :slots
             {"gncInvoice"
              [:frame
               {"invoice-guid" [:guid "ff36e86f090d10ffc56cae9d6cc75af2"]}],
              "title" [:string "Invoice 000048"]}}
            {:id "552060fc3f7f13d963da721ea77fc493",
             :slots
             {"gncInvoice"
              [:frame
               {"invoice-guid" [:guid "69838c1d577c728ab657e79bbf38d5de"]}],
              "title" [:string "Invoice 000022"]}}
            {:id "5c2704146ce7d16f96998b7f10732762",
             :slots
             {"gncInvoice"
              [:frame
               {"invoice-guid" [:guid "006176a8640a67805948b7189d73c240"]}],
              "title" [:string "Invoice 000043"]}}],
           :slots {"color" [:string "Not Set"]}})
        {:id #uuid "ed0a209c-c78f-680c-45a8-7851ed232236",
         :name "XSX",
         :type :receivable,
         :parent #uuid "78ff533d-1a66-deb7-5cfb-9a6a3e2c7cf8",
         :commodity {:id "GBP", :space "ISO4217"},
         :unit 100,
         :lots
         [{:id #uuid "00000000-0000-0000-0000-000000000000"}
          {:id #uuid "02be4b91-bfe9-0a4a-7098-fdb3bf53cc7d",
           :slots
           {"gncInvoice"
            {:type :frame,
             :value
             {"invoice-guid"
              {:type :guid,
               :value #uuid "6a350cbd-c782-21e9-2a8c-456e5def308f"}}},
            "title" {:type :string, :value "Invoice 000026"}}}
          {:id #uuid "341b2690-a35d-a27d-6a21-018085941222",
           :slots
           {"gncInvoice"
            {:type :frame,
             :value
             {"invoice-guid"
              {:type :guid,
               :value #uuid "ff36e86f-090d-10ff-c56c-ae9d6cc75af2"}}},
            "title" {:type :string, :value "Invoice 000048"}}}
          {:id #uuid "552060fc-3f7f-13d9-63da-721ea77fc493",
           :slots
           {"gncInvoice"
            {:type :frame,
             :value
             {"invoice-guid"
              {:type :guid,
               :value #uuid "69838c1d-577c-728a-b657-e79bbf38d5de"}}},
            "title" {:type :string, :value "Invoice 000022"}}}
          {:id #uuid "5c270414-6ce7-d16f-9699-8b7f10732762",
           :slots
           {"gncInvoice"
            {:type :frame,
             :value
             {"invoice-guid"
              {:type :guid,
               :value #uuid "006176a8-640a-6780-5948-b7189d73c240"}}},
            "title" {:type :string, :value "Invoice 000043"}}}],
         :slots {"color" {:type :string, :value "Not Set"}}}))
    (testing "without lots"
      (is=
        (spec/conform ::entities/account
          {:id "e921b069743f8d36e04e3eb786e12a52"
           :name "Root Account"
           :type "ROOT"
           :commodity {:id "GBP", :space "ISO4217"}
           :unit "100"})
        {:id #uuid "e921b069-743f-8d36-e04e-3eb786e12a52"
         :name "Root Account"
         :type :root
         :commodity {:id "GBP", :space "ISO4217"}
         :unit 100}))
    (testing "with empty lots"
      (is= ::spec/invalid
        ;; :lots should be non-empty or non-existent
        (spec/conform ::entities/account
          {:id "e921b069743f8d36e04e3eb786e12a52"
           :name "Root Account"
           :type "ROOT"
           :lots []
           :commodity {:id "GBP", :space "ISO4217"}
           :unit "100"})
        ))))

(deftest transaction
  (testing "transaction entity should conform to spec"
    (is=
      (spec/conform ::entities/transaction
        {:id "9e4e370632f9454112276bf8f1eed7c3"
         :currency {:id "GBP", :space "ISO4217"}
         :date-entered "2015-04-15 09:27:17 +0100"
         :date-posted "2014-12-16 10:59:00 +0000"
         :description "Initial Shares Value"
         :slots
         {"date-posted" [:gdate " 2014-12-16 "], "notes" [:string ""]}
         :splits
         [{:id "952f2cc5f291b94fbb0aff3e24a53e19"
           :reconciled-state "n"
           :value "10000/100"
           :quantity "10000/100"
           :account "7e5c347630c6da87a289d7630efe4124"}
          {:id "f778d66368075f897b68230bdca2e18d"
           :reconciled-state "n"
           :value "-10000/100"
           :quantity "-10000/100"
           :account "066fd7527cdf2a072baa16baa62ebfae"}]})
      {:id #uuid "9e4e3706-32f9-4541-1227-6bf8f1eed7c3"
       :currency {:id "GBP", :space "ISO4217"}
       :date-entered
       (jt/zoned-date-time "2015-04-15T09:27:17+01:00")
       :date-posted
       (jt/zoned-date-time "2014-12-16T10:59Z")
       :description "Initial Shares Value"
       :slots
       {"date-posted"
        {:type :gdate,
         :value (jt/local-date "2014-12-16")},
        "notes" {:type :string, :value ""}}
       :splits
       [{:id #uuid "952f2cc5-f291-b94f-bb0a-ff3e24a53e19"
         :reconciled-state "n"
         :value {:num 10000 :den 100}
         :quantity {:num 10000 :den 100}
         :account #uuid "7e5c3476-30c6-da87-a289-d7630efe4124"}
        {:id #uuid "f778d663-6807-5f89-7b68-230bdca2e18d"
         :reconciled-state "n"
         :value {:num -10000 :den 100}
         :quantity {:num -10000 :den 100}
         :account #uuid "066fd752-7cdf-2a07-2baa-16baa62ebfae"}]})
    (testing "without splits"
      (is= ::spec/invalid
        (spec/conform ::entities/transaction
          {:id "9e4e370632f9454112276bf8f1eed7c3"
           :currency {:id "GBP", :space "ISO4217"}
           :date-entered "2015-04-15 09:27:17 +0100"
           :date-posted "2014-12-16 10:59:00 +0000"
           :description "Initial Shares Value"
           :slots
           {"date-posted" [:gdate " 2014-12-16 "], "notes" [:string ""]}})))
    (testing "with empty splits"
      (is= ::spec/invalid
        (spec/conform ::entities/transaction
          {:id "9e4e370632f9454112276bf8f1eed7c3"
           :currency {:id "GBP", :space "ISO4217"}
           :date-entered "2015-04-15 09:27:17 +0100"
           :date-posted "2014-12-16 10:59:00 +0000"
           :description "Initial Shares Value"
           :slots
           {"date-posted" [:gdate " 2014-12-16 "], "notes" [:string ""]}
           :splits []})))))

(deftest billterm
  (testing "billing-terms entity should conform to spec"
    (is=
      (spec/conform ::entities/billterm
        {:guid "c8d46fc6d8aff3951364615b3de94a66"
         :name "Due On Receipt"
         :description "Payment is due immediately"
         :refcount "2"
         :invisible? "0"
         :child "f082418d653a200058590c25f5bd726f"})
      {:guid #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66"
       :name "Due On Receipt"
       :description "Payment is due immediately"
       :refcount 2
       :invisible? false
       :child #uuid "f082418d-653a-2000-5859-0c25f5bd726f"})))

(deftest taxtable
  (testing "tax-table entity should conform to spec"
    (is=
      (spec/conform ::entities/taxtable
        {:guid "4abe1dea253f747e97bdeb5c87c6b72d"
          :name "VAT Reclaim"
          :refcount "0"
          :invisible? "1"
          :parent "722f16a0318e9de14cf53ddfddfdeb4f"
          :entries
         [{:account "a11a9c48ae2addb88b834ecfacc20c22"
           :amount "2000000/100000"
           :type "PERCENT"}]})
      {:guid #uuid "4abe1dea-253f-747e-97bd-eb5c87c6b72d"
       :name "VAT Reclaim"
       :refcount 0
       :invisible? true
       :parent #uuid "722f16a0-318e-9de1-4cf5-3ddfddfdeb4f"
       :entries
       [{:account #uuid "a11a9c48-ae2a-ddb8-8b83-4ecfacc20c22"
         :amount {:num 2000000 :den 100000}
         :type :percent}]})
    (testing "with empty and missing entries"
      (is= ::spec/invalid
        ;; entries must exist
        (spec/conform ::entities/taxtable
          {:guid "4abe1dea253f747e97bdeb5c87c6b72d"
           :name "VAT Reclaim"
           :refcount "0"
           :invisible? "1"
           :parent "722f16a0318e9de14cf53ddfddfdeb4f"}))
      (is= ::spec/invalid
        ;; entries cannot be empty
        (spec/conform ::entities/taxtable
          {:guid "4abe1dea253f747e97bdeb5c87c6b72d"
           :name "VAT Reclaim"
           :refcount "0"
           :invisible? "1"
           :parent "722f16a0318e9de14cf53ddfddfdeb4f"
           :entries []})))))

(deftest customer
  (testing "customer entity should conform to spec"
    (is=
      (spec/conform ::entities/customer
        {:active? "1"
          :credit-limit "0/1"
         :slots
         {"last-posted-to-acct" [:guid "ed0a209cc78f680c45a87851ed232236"]
          "payment" [:frame {"last_acct" [:guid "c287231d815bd4b86fc62907cf3eaa46"]}]}
          :terms "bb032b5b31a7f8316f38d097e8db26c8"
          :use-tax-table? "1"
          :name "Big Bank Co"
          :currency {:id "GBP", :space "ISO4217"}
          :id "1"
          :billing-address
          {:name "AAAAA"
           :line1 "FOOO road"
           :line2 "BAAR House"
           :line3 "London"
           :line4 "E15 1NG"}
          :tax-included "NO"
          :discount "0/1"
          :tax-table "8506a23ee160adbf9c550addd27500f9"
         :guid "303123f619a89243a4b5e6281c1d591c"})
      {:active? true
       :credit-limit {:num 0 :den 1}
       :slots
       {"last-posted-to-acct"
        {:type :guid, :value #uuid "ed0a209c-c78f-680c-45a8-7851ed232236"}
        "payment"
        {:type :frame
         :value
         {"last_acct"
          {:type :guid, :value #uuid "c287231d-815b-d4b8-6fc6-2907cf3eaa46"}}}}
       :terms #uuid "bb032b5b-31a7-f831-6f38-d097e8db26c8"
       :use-tax-table? true
       :name "Big Bank Co"
       :currency {:id "GBP", :space "ISO4217"}
       :id "1"
       :billing-address
       {:name "AAAAA"
        :line1 "FOOO road"
        :line2 "BAAR House"
        :line3 "London"
        :line4 "E15 1NG"}
       :tax-included "NO"
       :discount {:num 0 :den 1}
       :tax-table #uuid "8506a23e-e160-adbf-9c55-0addd27500f9"
       :guid #uuid "303123f6-19a8-9243-a4b5-e6281c1d591c"})))

(deftest vendor
  (testing "vendor entity should conform to spec"
    (is=
      (spec/conform ::entities/vendor
        {:active? "1"
         :slots
         {"last-posted-to-acct" [:guid "d6e87f32ce9c437ffee9704ad7fc8bf4"]
          "payment" [:frame {"last_acct" [:guid "c287231d815bd4b86fc62907cf3eaa46"]}]}
         :terms "c8d46fc6d8aff3951364615b3de94a66"
         :use-tax-table? "1"
         :name "Sundry"
         :currency {:id "GBP", :space "ISO4217"}
         :id "000001"
         :billing-address {:name "Daniel Dinnyes", :line1 "at home"}
         :tax-included "YES"
         :tax-table "722f16a0318e9de14cf53ddfddfdeb4f"
         :guid "98950c25fa395af68bf1397ab522d1ad"})
      {:active? true
       :slots
       {"last-posted-to-acct"
        {:type :guid :value #uuid "d6e87f32-ce9c-437f-fee9-704ad7fc8bf4"}
        "payment"
        {:type :frame
         :value
         {"last_acct"
          {:type :guid :value #uuid "c287231d-815b-d4b8-6fc6-2907cf3eaa46"}}}}
       :terms #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66"
       :use-tax-table? true
       :name "Sundry"
       :currency {:id "GBP", :space "ISO4217"}
       :id "000001"
       :billing-address {:name "Daniel Dinnyes", :line1 "at home"}
       :tax-included "YES"
       :tax-table #uuid "722f16a0-318e-9de1-4cf5-3ddfddfdeb4f"
       :guid #uuid "98950c25-fa39-5af6-8bf1-397ab522d1ad"})))

(deftest employee
  (testing "employee entity should conform to spec"
    (is=
      (spec/conform ::entities/employee
        {:active? "1"
         :workday "0/1"
         :slots
         {"last-posted-to-acct" [:guid "d6e87f32ce9c437ffee9704ad7fc8bf4"]
          "payment"
          [:frame {"last_acct" [:guid "b4f05202e1e85c2f86284cead3737d19"]}]}
         :username "dadinn"
         :rate "0/1"
         :currency {:id "GBP", :space "ISO4217"}
         :language "German"
         :id "000001"
         :billing-address {:name "Daniel Dinnyes", :line1 "Bla"}
         :guid "03a20cda38a4e4c0873cab3d104d85a2"})
      {:active? true
       :workday {:num 0 :den 1}
       :slots
       {"last-posted-to-acct"
        {:type :guid :value #uuid "d6e87f32-ce9c-437f-fee9-704ad7fc8bf4"}
        "payment"
        {:type :frame
         :value
         {"last_acct"
          {:type :guid
           :value #uuid "b4f05202-e1e8-5c2f-8628-4cead3737d19"}}}}
       :username "dadinn"
       :rate {:num 0 :den 1}
       :currency {:id "GBP", :space "ISO4217"}
       :language "German"
       :id "000001"
       :billing-address {:name "Daniel Dinnyes", :line1 "Bla"}
       :guid #uuid "03a20cda-38a4-e4c0-873c-ab3d104d85a2"})))

(deftest job
  (testing "job entity should conform to spec"
    (is=
      (spec/conform ::entities/job
        {:guid "7ed7f4f40882668a1d78f6efee5ecce9"
         :id "000001"
         :name "Big Bank Co."
         :reference "0000666"
         :owner
         {:type "gncCustomer"
          :id "303123f619a89243a4b5e6281c1d591c"}
         :active? "0"})
      {:guid #uuid "7ed7f4f4-0882-668a-1d78-f6efee5ecce9"
       :id "000001"
       :name "Big Bank Co."
       :reference "0000666"
       :owner
       {:type :customer
        :id #uuid "303123f6-19a8-9243-a4b5-e6281c1d591c"}
       :active? false})))

(deftest invoice
  (testing "invoice entity should conform to spec"
    (is=
      (spec/conform ::entities/invoice
        {:posted "2016-09-07 00:00:00 +0100"
         :slots {"credit-note" [:integer "0"]}
         :terms "f082418d653a200058590c25f5bd726f"
         :opened "2016-09-07 00:00:00 +0100"
         :currency {:id "GBP", :space "ISO4217"}
         :posttxn "dbc0ca7d748bcca7e5fa097d286fad2d"
         :reference "00000666"
         :postacc "ed0a209cc78f680c45a87851ed232236"
         :postlot "5c2704146ce7d16f96998b7f10732762"
         :active? "1"
         :id "000046"
         :owner {:type "gncJob", :id "7ed7f4f40882668a1d78f6efee5ecce9"}
         :guid "006176a8640a67805948b7181d73c240"})
      {:posted (jt/zoned-date-time "2016-09-07T00:00+01:00")
       :slots {"credit-note" {:type :integer, :value 0}}
       :terms #uuid "f082418d-653a-2000-5859-0c25f5bd726f"
       :opened (jt/zoned-date-time "2016-09-07T00:00+01:00")
       :currency {:id "GBP", :space "ISO4217"}
       :posttxn #uuid "dbc0ca7d-748b-cca7-e5fa-097d286fad2d"
       :reference "00000666"
       :postacc #uuid "ed0a209c-c78f-680c-45a8-7851ed232236"
       :postlot #uuid "5c270414-6ce7-d16f-9699-8b7f10732762"
       :active? true
       :id "000046"
       :owner
       {:type :job, :id #uuid "7ed7f4f4-0882-668a-1d78-f6efee5ecce9"}
       :guid #uuid "006176a8-640a-6780-5948-b7181d73c240"})))

(deftest entry
  (testing "entry entity should conform to spec"
    (is=
      (spec/conform ::entities/entry
        {:description "Week Ending 2018-12-05"
         :entered "2019-02-04 11:28:30 +0000"
         :discount-type "PERCENT"
         :tax-included? "0"
         :taxable? "1"
         :account "c54f4792499e2e8d64edfd58f5a97abd"
         :invoice "843a93ee7dafad81d2c9d7d2fde1ab50"
         :date "2018-12-05 12:00:00 +0000"
         :quantity "2500000/1000000"
         :tax-table "75c1fd112acb3ac94f96086cc4b7131c"
         :price "650000000/1000000"
         :discount-how "PRETAX"
         :discount "1500000/1000000"
         :guid "05204b4bbaa083c0afe2a4b40f9211b3"})
      [:invoice-entry
       {:description "Week Ending 2018-12-05"
        :entered
        (jt/zoned-date-time "2019-02-04T11:28:30Z")
        :discount-type :percent
        :tax-included? false
        :taxable? true
        :account #uuid "c54f4792-499e-2e8d-64ed-fd58f5a97abd"
        :invoice #uuid "843a93ee-7daf-ad81-d2c9-d7d2fde1ab50"
        :date
        (jt/zoned-date-time "2018-12-05T12:00Z")
        :quantity {:num 2500000 :den 1000000}
        :tax-table #uuid "75c1fd11-2acb-3ac9-4f96-086cc4b7131c"
        :price {:num 650000000 :den 1000000}
        :discount-how :pretax
        :discount {:num 1500000 :den 1000000}
        :guid #uuid "05204b4b-baa0-83c0-afe2-a4b40f9211b3"}])))

(deftest shedxaction
  (testing "schedxaction entity should conform to spec"
    (is=
      (spec/conform ::entities/schedxaction
        {:schedule
         [{:start "2018-01-01", :period-type "day", :multiplier "2"}]
         :advance-remind-days "1"
         :advance-create-days "0"
         :name "Monthly Fees"
         :start "2018-01-01"
         :enabled? "y"
         :account "9e4139d88a5e7a57a380089bf41a6711"
         :instance-count "24"
         :id "0425966151a0c18fa58065d41fdfed24"
         :auto-create? "n"
         :last "2018-12-03"
         :auto-create-notify? "n"})
      {:schedule
       [{:start (jt/local-date "2018-01-01")
         :period-type :day
         :multiplier 2}]
       :advance-remind-days 1
       :advance-create-days 0
       :name "Monthly Fees"
       :start (jt/local-date "2018-01-01")
       :enabled? true
       :account #uuid "9e4139d8-8a5e-7a57-a380-089bf41a6711"
       :instance-count 24
       :id #uuid "04259661-51a0-c18f-a580-65d41fdfed24"
       :auto-create? false
       :last (jt/local-date "2018-12-03")
       :auto-create-notify? false})
    (testing "with empty schedule"
      (is= ::spec/invalid
        ;; :schedule should be non-empty or non-existent
        (spec/conform ::entities/schedxaction
          {:schedule []
           :advance-remind-days "1"
           :advance-create-days "0"
           :name "Monthly Fees"
           :start "2018-01-01"
           :enabled? "y"
           :account "9e4139d88a5e7a57a380089bf41a6711"
           :instance-count "24"
           :id "0425966151a0c18fa58065d41fdfed24"
           :auto-create? "n"
           :last "2018-12-03"
           :auto-create-notify? "n"})))))

(deftest budget
  (testing "budget entity should conform to spec"
    (is=
      (spec/conform ::entities/budget
        {:id "cc73b3648202e3406b1ee9061f397f57"
         :name "Unnamed Budget"
         :num-periods "12"
         :recurrence
         {:start "2019-01-01", :period-type "month", :multiplier "1"}
         :slots
         {"3ebc81195e2b0d188260a5b92181ab50"
          [:frame
           {"9" [:numeric "6/1"]
            "3" [:numeric "6/1"]
            "4" [:numeric "6/1"]
            "8" [:numeric "6/1"]
            "7" [:numeric "6/1"]
            "5" [:numeric "6/1"]
            "6" [:numeric "6/1"]
            "1" [:numeric "6/1"]
            "0" [:numeric "6/1"]
            "11" [:numeric "6/1"]
            "2" [:numeric "6/1"]
            "10" [:numeric "6/1"]}]
          "68aa1816af8d8025f873488153196a7b"
          [:frame
           {"9" [:numeric "9/-100"]
            "3" [:numeric "9/-100"]
            "4" [:numeric "9/-100"]
            "8" [:numeric "2/-1000"]
            "7" [:numeric "9/-100"]
            "5" [:numeric "9/-100"]
            "6" [:numeric "9/-100"]
            "1" [:numeric "9/-100"]
            "0" [:numeric "9/-100"]
            "11" [:numeric  "0/10"]
            "2" [:numeric "9/-100"]
            "10" [:numeric "9/-100"]}]
          "7402f9d2c881069783237df27928513f"
          [:frame
           {"9" [:numeric "2/-100"]
            "3" [:numeric "2/-100"]
            "4" [:numeric "3/-100"]
            "8" [:numeric "2/-100"]
            "7" [:numeric "2/-100"]
            "5" [:numeric "3/-100"]
            "6" [:numeric "2/-100"]
            "1" [:numeric "3/-100"]
            "0" [:numeric "2/-100"]
            "11" [:numeric "2/-100"]
            "2" [:numeric "3/-100"]
            "10" [:numeric "2/-100"]}]}})
      {:id #uuid "cc73b364-8202-e340-6b1e-e9061f397f57"
       :name "Unnamed Budget"
       :num-periods 12
       :recurrence
       {:start (jt/local-date "2019-01-01")
        :period-type :month
        :multiplier 1}
       :slots
       {"3ebc81195e2b0d188260a5b92181ab50"
        {:type :frame
         :value
         {"9" {:type :numeric, :value {:num 6 :den 1}}
          "3" {:type :numeric, :value {:num 6 :den 1}}
          "4" {:type :numeric, :value {:num 6 :den 1}}
          "8" {:type :numeric, :value {:num 6 :den 1}}
          "7" {:type :numeric, :value {:num 6 :den 1}}
          "5" {:type :numeric, :value {:num 6 :den 1}}
          "6" {:type :numeric, :value {:num 6 :den 1}}
          "1" {:type :numeric, :value {:num 6 :den 1}}
          "0" {:type :numeric, :value {:num 6 :den 1}}
          "11" {:type :numeric, :value {:num 6 :den 1}}
          "2" {:type :numeric, :value {:num 6 :den 1}}
          "10" {:type :numeric, :value {:num 6 :den 1}}}}
        "68aa1816af8d8025f873488153196a7b"
        {:type :frame
         :value
         {"9" {:type :numeric, :value {:num 9, :den -100}},
          "3" {:type :numeric, :value {:num 9, :den -100}},
          "4" {:type :numeric, :value {:num 9, :den -100}},
          "8" {:type :numeric, :value {:num 2, :den -1000}},
          "7" {:type :numeric, :value {:num 9, :den -100}},
          "5" {:type :numeric, :value {:num 9, :den -100}},
          "6" {:type :numeric, :value {:num 9, :den -100}},
          "1" {:type :numeric, :value {:num 9, :den -100}},
          "0" {:type :numeric, :value {:num 9, :den -100}},
          "11" {:type :numeric, :value {:num 0, :den 10}},
          "2" {:type :numeric, :value {:num 9, :den -100}},
          "10" {:type :numeric, :value {:num 9, :den -100}}}}
        "7402f9d2c881069783237df27928513f"
        {:type :frame
         :value
         {"9" {:type :numeric, :value {:num 2, :den -100}},
          "3" {:type :numeric, :value {:num 2, :den -100}},
          "4" {:type :numeric, :value {:num 3, :den -100}},
          "8" {:type :numeric, :value {:num 2, :den -100}},
          "7" {:type :numeric, :value {:num 2, :den -100}},
          "5" {:type :numeric, :value {:num 3, :den -100}},
          "6" {:type :numeric, :value {:num 2, :den -100}},
          "1" {:type :numeric, :value {:num 3, :den -100}},
          "0" {:type :numeric, :value {:num 2, :den -100}},
          "11" {:type :numeric, :value {:num 2, :den -100}},
          "2" {:type :numeric, :value {:num 3, :den -100}},
          "10" {:type :numeric, :value {:num 2, :den -100}}}}}})))
