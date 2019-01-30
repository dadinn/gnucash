(ns com.besenczy.gnucash.specs.common
  (:require
   [com.besenczy.gnucash.specs.commodity :as cmdty]
   [clojure.edn :as edn]
   [clojure.string :as string]
   [java-time :as jt]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec])
  (:import
   [java.util UUID]))

(defn parse-guid [s]
  (->> (list 8 4 4 4 12)
    (reduce
      (fn [[slices acc] n]
        [(seq (drop n slices))
         (let [part (take n slices)
               part (apply str part)]
           (conj acc part))])
      [(seq s) []])
    second
    (string/join "-")
    (UUID/fromString)))

(spec/def ::guid
  (spec/with-gen
    (spec/and string?
      (partial re-matches #"[0-9a-f]{32}")
      (spec/conformer
        parse-guid
        (fn [id] (.replace (str id) "-" ""))))
    #(gen/fmap
       (fn [uuid] (-> uuid str (.replace "-" "")))
       (gen/uuid))))

(def date-fmt (jt/formatter "yyyy-MM-dd"))
(def datetime-fmt (jt/formatter "yyyy-MM-dd HH:mm:ss ZZZ"))

(spec/def ::date
  (spec/and string?
    (comp
      (partial re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}")
      string/trim)
    (spec/conformer
      (fn [s] (jt/local-date date-fmt (string/trim s)))
      (fn [dt] (jt/format date-fmt dt)))))

(spec/def ::datetime
  (spec/and string?
    (comp
      (partial re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} [+-][0-9]{4}")
      string/trim)
    (spec/conformer
      (fn [s] (jt/zoned-date-time datetime-fmt (string/trim s)))
      (fn [t] (jt/format datetime-fmt t)))))

(spec/def ::numeric
  (spec/and string?
    (partial re-matches #"-?[0-9]+/?-?[0-9]*")
    (spec/conformer
      (fn [s]
        (let [[x & xs] (map edn/read-string (string/split s #"/"))]
          (if (seq xs) (apply / (cons x xs)) x)))
      (fn [s] (pr-str s)))))

(spec/def ::boolean-num
  (spec/and
    #{"0" "1"}
    (spec/conformer
      {"0" false "1" true}
      {true "1" false "0"})))

(spec/def ::boolean-char
  (spec/and #{"y" "n"}
    (spec/conformer
      {"y" true "n" false}
      {true "y" false "n"})))

(spec/def ::commodity
  (spec/keys
    :req-un
    [::cmdty/id
     ::cmdty/space]
    :opt-un
    [::cmdty/name
     ::cmdty/xcode
     ::cmdty/fraction
     ::cmdty/get-quotes
     ::cmdty/quote-source
     ::cmdty/quote-timezone]))
