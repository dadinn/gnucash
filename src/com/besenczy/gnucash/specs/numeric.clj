(ns com.besenczy.gnucash.specs.numeric
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec]))

(spec/def ::natural
  (spec/with-gen
    (spec/and string?
      (partial re-matches #"[0-9]+")
      (spec/conformer
        (fn [s] (edn/read-string s))
        (fn [x] (pr-str x))))
    #(gen/fmap
       (fn [x] (pr-str x))
       (spec/gen pos-int?))))

(spec/def ::integer
  (spec/with-gen
    (spec/and string?
      (partial re-matches #"-?[0-9]+")
      (spec/conformer
        (fn [s] (edn/read-string s))
        (fn [x] (pr-str x))))
    #(gen/fmap
       (fn [x] (pr-str x))
       (spec/gen int?))))

(spec/def ::fraction
  (spec/with-gen
    (spec/and string?
      (partial re-matches #"-?[0-9]+/-?[0-9]+")
      (spec/conformer
        (fn [s]
          (let [[x y] (string/split s #"/")]
            {:num (edn/read-string x)
             :den (edn/read-string y)}))
        (fn [{:keys [num den]}]
          {:pre [(int? num) (int? den)]}
          (string/join "/" [num den]))))
    #(gen/fmap
       (fn [[x y]] (if (zero? y) (pr-str x) (pr-str (/ x y))))
       (spec/gen (spec/tuple int? int?)))))
