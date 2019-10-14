# Parser for Gnucash data format

This library is to help with parsing and emitting XML documents of the [Gnucash XML format](https://wiki.gnucash.org/wiki/GnuCash_XML_format)

## Usage

Add to your [leiningen](https://leiningen.org/) or [Boot](https://boot-clj.com/) dependencies:

```
[com.besenczy/gnucash "0.2.1"]
```

Importing and exporting documents can be done the following way:

```clojure
(require '[com.besenczy.gnucash.core :as gnucash])

(def document-str (slurp "Accounting.gnucash"))
(def imported (gnucash/parse-str document-str))
(def exported (gnucash/emit-str imported))

```

The imported document is conformed according to the spec `::com.besenczy.gnucash.specs/document`

