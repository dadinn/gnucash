(ns com.besenczy.gnucash.test.specs
  (:require
   [com.besenczy.gnucash.specs.common :as common]
   [com.besenczy.gnucash.specs.entities :as entities]
   [java-time :as jt]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]))

(defmacro is= [x y] `(is (= ~x ~y)))

(deftest guid
  (testing "guid entry should conform to spec"
    (is= #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66"
      (spec/conform ::common/guid "c8d46fc6d8aff3951364615b3de94a66"))))

(deftest numeric
  (testing "numeric entry should conform to spec"
    (is= 42 (spec/conform ::common/numeric "42"))
    (is= -42 (spec/conform ::common/numeric "-42"))
    (is= 1 (spec/conform ::common/numeric "111111/111111"))
    (is= -1 (spec/conform ::common/numeric "-111111/111111"))
    (is= 11/10000 (spec/conform ::common/numeric "11/10000"))
    (is= -11/10000 (spec/conform ::common/numeric "-11/10000"))
    (is= -11/111111 (spec/conform ::common/numeric "11/-111111"))
    (is= 11/111111 (spec/conform ::common/numeric "-11/-111111"))))

(deftest price
  (testing "price entity should conform to spec"
    (is=
      (spec/conform ::entities/price
        {:id "87546b912076bf6944d3f32ae7d8c70a",
         :commodity {:id "EUR", :space "ISO4217"},
         :currency {:id "GBP", :space "ISO4217"},
         :date " 2015-03-29 00:00:00 +0000 ",
         :source "user:xfer-dialog",
         :value "73/100"})
      {:id #uuid "87546b91-2076-bf69-44d3-f32ae7d8c70a",
       :commodity {:id "EUR", :space "ISO4217"},
       :currency {:id "GBP", :space "ISO4217"},
       :date
       (spec/conform ::common/datetime
         "2015-03-29 00:00:00 +0000")
       :source "user:xfer-dialog"
       :value 73/100})))

(deftest account
  (testing "account entity should conform to spec"
    (is=
      (spec/conform ::entities/account
        {:id "e921b069743f8d36e04e3eb786e12a52",
         :name "Root Account",
         :type "ROOT",
         :commodity {:id "GBP", :space "ISO4217"},
         :unit "100"})
      {:id #uuid "e921b069-743f-8d36-e04e-3eb786e12a52",
       :name "Root Account",
       :type :root,
       :commodity {:id "GBP", :space "ISO4217"},
       :unit 100})))

(deftest transaction
  (testing "transaction entity should conform to spec"
    (is=
      (spec/conform ::entities/transaction
        '{:id "9e4e370632f9454112276bf8f1eed7c3",
          :currency {:id "GBP", :space "ISO4217"},
          :date-entered "2015-04-15 09:27:17 +0100",
          :date-posted "2014-12-16 10:59:00 +0000",
          :description "Initial Shares Value",
          :slots {"date-posted" " 2014-12-16 "},
          :splits
          ({:id "952f2cc5f291b94fbb0aff3e24a53e19",
            :reconciled-state "n",
            :value "10000/100",
            :quantity "10000/100",
            :account "7e5c347630c6da87a289d7630efe4124"}
           {:id "f778d66368075f897b68230bdca2e18d",
            :reconciled-state "n",
            :value "-10000/100",
            :quantity "-10000/100",
            :account "066fd7527cdf2a072baa16baa62ebfae"})})
      {:id #uuid "9e4e3706-32f9-4541-1227-6bf8f1eed7c3"
       :currency {:id "GBP", :space "ISO4217"}
       :date-entered
       (jt/zoned-date-time "2015-04-15T09:27:17+01:00")
       :date-posted
       (jt/zoned-date-time "2014-12-16T10:59Z")
       :description "Initial Shares Value"
       :slots
       {"date-posted"
        (jt/local-date "2014-12-16")}
       :splits
       [{:id #uuid "952f2cc5-f291-b94f-bb0a-ff3e24a53e19"
         :reconciled-state "n"
         :value 100
         :quantity 100
         :account #uuid "7e5c3476-30c6-da87-a289-d7630efe4124"}
        {:id #uuid "f778d663-6807-5f89-7b68-230bdca2e18d"
         :reconciled-state "n"
         :value -100
         :quantity -100
         :account #uuid "066fd752-7cdf-2a07-2baa-16baa62ebfae"}]})))

(deftest billterm
  (testing "billing-terms entity should conform to spec"
    (is=
      (spec/conform ::entities/billterm
        {:guid "c8d46fc6d8aff3951364615b3de94a66",
         :name "Due On Receipt",
         :description "Payment is due immediately",
         :refcount "2",
         :invisible? "0",
         :child "f082418d653a200058590c25f5bd726f"})
      {:guid #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66",
       :name "Due On Receipt",
       :description "Payment is due immediately",
       :refcount 2,
       :invisible? false,
       :child #uuid "f082418d-653a-2000-5859-0c25f5bd726f"})))

(deftest taxtable
  (testing "tax-table entity should conform to spec"
    (is=
      (spec/conform ::entities/taxtable
        {:guid "4abe1dea253f747e97bdeb5c87c6b72d",
          :name "VAT Reclaim",
          :refcount "0",
          :invisible? "1",
          :parent "722f16a0318e9de14cf53ddfddfdeb4f",
          :entries
         [{:account "a11a9c48ae2addb88b834ecfacc20c22",
           :amount "2000000/100000",
           :type "PERCENT"}]})
      {:guid #uuid "4abe1dea-253f-747e-97bd-eb5c87c6b72d",
       :name "VAT Reclaim",
       :refcount 0,
       :invisible? true,
       :parent #uuid "722f16a0-318e-9de1-4cf5-3ddfddfdeb4f",
       :entries
       [{:account #uuid "a11a9c48-ae2a-ddb8-8b83-4ecfacc20c22",
         :amount 20,
         :type :percent}]})))

(deftest customer
  (testing "customer entity should conform to spec"
    (is=
      (spec/conform ::entities/customer
        {:active? "1",
          :credit-limit "0/1",
          :slots
          {"last-posted-to-acct" "ed0a209cc78f680c45a87851ed232236",
           "payment" {"last_acct" "c287231d815bd4b86fc62907cf3eaa46"}},
          :terms "bb032b5b31a7f8316f38d097e8db26c8",
          :use-tax-table? "1",
          :name "Big Bank Co",
          :currency {:id "GBP", :space "ISO4217"},
          :id "1",
          :billing-address
          {:name "AAAAA",
           :line1 "FOOO road",
           :line2 "BAAR House",
           :line3 "London",
           :line4 "E15 1NG"},
          :tax-included "NO",
          :discount "0/1",
          :tax-table "8506a23ee160adbf9c550addd27500f9"
         :guid "303123f619a89243a4b5e6281c1d591c"})
      {:active? true,
       :credit-limit 0,
       :slots
       {"last-posted-to-acct" "ed0a209cc78f680c45a87851ed232236",
        "payment" {"last_acct" "c287231d815bd4b86fc62907cf3eaa46"}},
       :terms #uuid "bb032b5b-31a7-f831-6f38-d097e8db26c8",
       :use-tax-table? true,
       :name "Big Bank Co",
       :currency {:id "GBP", :space "ISO4217"},
       :id "1",
       :billing-address
       {:name "AAAAA",
        :line1 "FOOO road",
        :line2 "BAAR House",
        :line3 "London",
        :line4 "E15 1NG"},
       :tax-included "NO",
       :discount 0,
       :tax-table #uuid "8506a23e-e160-adbf-9c55-0addd27500f9",
       :guid #uuid "303123f6-19a8-9243-a4b5-e6281c1d591c"})))

(deftest vendor
  (testing "vendor entity should conform to spec"
    (is=
      (spec/conform ::entities/vendor
        {:active? "1",
         :slots
         {"last-posted-to-acct" "d6e87f32ce9c437ffee9704ad7fc8bf4",
          "payment" {"last_acct" "c287231d815bd4b86fc62907cf3eaa46"}},
         :terms "c8d46fc6d8aff3951364615b3de94a66",
         :use-tax-table? "1",
         :name "Sundry",
         :currency {:id "GBP", :space "ISO4217"},
         :id "000001",
         :billing-address {:name "Daniel Dinnyes", :line1 "at home"},
         :tax-included "YES",
         :tax-table "722f16a0318e9de14cf53ddfddfdeb4f",
         :guid "98950c25fa395af68bf1397ab522d1ad"})
      {:active? true,
       :slots
       {"last-posted-to-acct" "d6e87f32ce9c437ffee9704ad7fc8bf4",
        "payment" {"last_acct" "c287231d815bd4b86fc62907cf3eaa46"}},
       :terms #uuid "c8d46fc6-d8af-f395-1364-615b3de94a66",
       :use-tax-table? true,
       :name "Sundry",
       :currency {:id "GBP", :space "ISO4217"},
       :id "000001",
       :billing-address {:name "Daniel Dinnyes", :line1 "at home"},
       :tax-included "YES",
       :tax-table #uuid "722f16a0-318e-9de1-4cf5-3ddfddfdeb4f",
       :guid #uuid "98950c25-fa39-5af6-8bf1-397ab522d1ad"})))

(deftest employee
  (testing "employee entity should conform to spec"
    (is=
      (spec/conform ::entities/employee
        {:active? "1",
         :workday "0/1",
         :slots
         {"last-posted-to-acct" "d6e87f32ce9c437ffee9704ad7fc8bf4",
          "payment" {"last_acct" "b4f05202e1e85c2f86284cead3737d19"}},
         :username "dadinn",
         :rate "0/1",
         :currency {:id "GBP", :space "ISO4217"},
         :language "German",
         :id "000001",
         :billing-address {:name "Daniel Dinnyes", :line1 "Bla"},
         :guid "03a20cda38a4e4c0873cab3d104d85a2"})
      {:active? true,
       :workday 0,
       :slots
       {"last-posted-to-acct" "d6e87f32ce9c437ffee9704ad7fc8bf4",
        "payment" {"last_acct" "b4f05202e1e85c2f86284cead3737d19"}},
       :username "dadinn",
       :rate 0,
       :currency {:id "GBP", :space "ISO4217"},
       :language "German",
       :id "000001",
       :billing-address {:name "Daniel Dinnyes", :line1 "Bla"},
       :guid #uuid "03a20cda-38a4-e4c0-873c-ab3d104d85a2"})))

(deftest job
  (testing "job entity should conform to spec"
    (is=
      (spec/conform ::entities/job
        {:guid "7ed7f4f40882668a1d78f6efee5ecce9",
         :id "000001",
         :name "Big Bank Co.",
         :reference "0000666",
         :owner
         {:type "gncCustomer"
          :id "303123f619a89243a4b5e6281c1d591c"},
         :active? "0"})
      {:guid #uuid "7ed7f4f4-0882-668a-1d78-f6efee5ecce9",
       :id "000001",
       :name "Big Bank Co.",
       :reference "0000666",
       :owner
       {:type :customer
        :id #uuid "303123f6-19a8-9243-a4b5-e6281c1d591c"},
       :active? false})))

(deftest invoice
  (testing "invoice entity should conform to spec"
    (is=
      (spec/conform ::entities/invoice
        {:posted "2016-09-07 00:00:00 +0100",
         :slots {"credit-note" "0"},
         :terms "f082418d653a200058590c25f5bd726f",
         :opened "2016-09-07 00:00:00 +0100",
         :currency {:id "GBP", :space "ISO4217"},
         :posttxn "dbc0ca7d748bcca7e5fa097d286fad2d",
         :reference "00000666",
         :postacc "ed0a209cc78f680c45a87851ed232236",
         :postlot "5c2704146ce7d16f96998b7f10732762",
         :active "1",
         :id "000046",
         :owner {:type "gncJob", :id "7ed7f4f40882668a1d78f6efee5ecce9"},
         :guid "006176a8640a67805948b7181d73c240"})
      {:posted (jt/zoned-date-time "2016-09-07T00:00+01:00")
       :slots {"credit-note" 0},
       :terms #uuid "f082418d-653a-2000-5859-0c25f5bd726f",
       :opened (jt/zoned-date-time "2016-09-07T00:00+01:00")
       :currency {:id "GBP", :space "ISO4217"},
       :posttxn #uuid "dbc0ca7d-748b-cca7-e5fa-097d286fad2d",
       :reference "00000666",
       :postacc #uuid "ed0a209c-c78f-680c-45a8-7851ed232236",
       :postlot #uuid "5c270414-6ce7-d16f-9699-8b7f10732762",
       :active true,
       :id "000046",
       :owner
       {:type :job, :id #uuid "7ed7f4f4-0882-668a-1d78-f6efee5ecce9"},
       :guid #uuid "006176a8-640a-6780-5948-b7181d73c240"})))

(deftest entry
  (testing "entry entity should conform to spec"
    (is=
      (spec/conform ::entities/entry
        {:description "Week Ending 2018-12-05",
         :date-entered "2019-02-04 11:28:30 +0000",
         :discount-type "PERCENT",
         :tax-included? "0",
         :taxable? "1",
         :account "c54f4792499e2e8d64edfd58f5a97abd",
         :invoice "843a93ee7dafad81d2c9d7d2fde1ab50",
         :date-recorded "2018-12-05 12:00:00 +0000",
         :quantity "2500000/1000000",
         :tax-table "75c1fd112acb3ac94f96086cc4b7131c",
         :price "650000000/1000000",
         :discount-how "PRETAX",
         :guid "05204b4bbaa083c0afe2a4b40f9211b3"})
      [:invoice-entry
       {:description "Week Ending 2018-12-05",
        :date-entered
        (jt/zoned-date-time "2019-02-04T11:28:30Z")
        :discount-type :percent,
        :tax-included? false,
        :taxable? true,
        :account #uuid "c54f4792-499e-2e8d-64ed-fd58f5a97abd",
        :invoice #uuid "843a93ee-7daf-ad81-d2c9-d7d2fde1ab50",
        :date-recorded
        (jt/zoned-date-time "2018-12-05T12:00Z")
        :quantity 5/2,
        :tax-table #uuid "75c1fd11-2acb-3ac9-4f96-086cc4b7131c",
        :price 650,
        :discount-how :pretax,
        :guid #uuid "05204b4b-baa0-83c0-afe2-a4b40f9211b3"}])))

(deftest shedxaction
  (testing "schedxaction entity should conform to spec"
    (is=
      (spec/conform ::entities/schedxaction
        {:schedule
         [{:start "2018-01-01", :period-type "day", :multiplier "2"}],
         :advance-remind-days "1",
         :advance-create-days "0",
         :name "Monthly Fees",
         :start "2018-01-01",
         :enabled? "y",
         :account "9e4139d88a5e7a57a380089bf41a6711",
         :instance-count "24",
         :id "0425966151a0c18fa58065d41fdfed24",
         :auto-create? "n",
         :last "2018-12-03",
         :auto-create-notify? "n"})
      {:schedule
       [{:start (jt/local-date "2018-01-01")
         :period-type :day,
         :multiplier 2}],
       :advance-remind-days 1,
       :advance-create-days 0,
       :name "Monthly Fees",
       :start (jt/local-date "2018-01-01")
       :enabled? true,
       :account #uuid "9e4139d8-8a5e-7a57-a380-089bf41a6711",
       :instance-count 24,
       :id #uuid "04259661-51a0-c18f-a580-65d41fdfed24",
       :auto-create? false,
       :last (jt/local-date "2018-12-03")
       :auto-create-notify? false})))

(deftest budget
  (testing "budget entity should conform to spec"
    (is=
      (spec/conform ::entities/budget
        {:id "cc73b3648202e3406b1ee9061f397f57",
         :name "Unnamed Budget",
         :num-periods "12",
         :recurrence
         {:start "2019-01-01", :period-type "month", :multiplier "1"},
         :slots
         {"3ebc81195e2b0d188260a5b92181ab50"
          {"9" "6/1",
           "3" "6/1",
           "4" "6/1",
           "8" "6/1",
           "7" "6/1",
           "5" "6/1",
           "6" "6/1",
           "1" "6/1",
           "0" "6/1",
           "11" "6/1",
           "2" "6/1",
           "10" "6/1"},
          "68aa1816af8d8025f873488153196a7b"
          {"9" "9/-100",
           "3" "9/-100",
           "4" "9/-100",
           "8" "2/-1000",
           "7" "9/-100",
           "5" "9/-100",
           "6" "9/-100",
           "1" "9/-100",
           "0" "9/-100",
           "11" "0/10",
           "2" "9/-100",
           "10" "9/-100"},
          "7402f9d2c881069783237df27928513f"
          {"9" "2/-100",
           "3" "2/-100",
           "4" "3/-100",
           "8" "2/-100",
           "7" "2/-100",
           "5" "3/-100",
           "6" "2/-100",
           "1" "3/-100",
           "0" "2/-100",
           "11" "2/-100",
           "2" "3/-100",
           "10" "2/-100"}}})
      {:id #uuid "cc73b364-8202-e340-6b1e-e9061f397f57",
       :name "Unnamed Budget",
       :num-periods 12,
       :recurrence
       {:start (jt/local-date "2019-01-01")
        :period-type :month,
        :multiplier 1},
       :slots
       {"3ebc81195e2b0d188260a5b92181ab50"
        {"9" 6,
         "3" 6,
         "4" 6,
         "8" 6,
         "7" 6,
         "5" 6,
         "6" 6,
         "1" 6,
         "0" 6,
         "11" 6,
         "2" 6,
         "10" 6},
        "68aa1816af8d8025f873488153196a7b"
        {"9" "9/-100",
         "3" "9/-100",
         "4" "9/-100",
         "8" "2/-1000",
         "7" "9/-100",
         "5" "9/-100",
         "6" "9/-100",
         "1" "9/-100",
         "0" "9/-100",
         "11" 0,
         "2" "9/-100",
         "10" "9/-100"},
        "7402f9d2c881069783237df27928513f"
        {"9" "2/-100",
         "3" "2/-100",
         "4" "3/-100",
         "8" "2/-100",
         "7" "2/-100",
         "5" "3/-100",
         "6" "2/-100",
         "1" "3/-100",
         "0" "2/-100",
         "11" "2/-100",
         "2" "3/-100",
         "10" "2/-100"}}})))
