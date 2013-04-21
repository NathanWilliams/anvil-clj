(ns anvil-clj.devel
  "Where I start repl experiments from!"
  (:use [clojure.tools.namespace.repl :only (refresh)])
  (:require [anvil-clj.world :as world]
            [anvil-clj.region-file :as rf]
            [gloss.core :as gc]
            [gloss.data.bytes :as gb]
            [gloss.io :as gio])
  (:import java.io.ByteArrayInputStream
           java.util.zip.GZIPInputStream
           java.util.zip.InflaterInputStream))



(def test-data (world/load-binary-file "sample_files/r.0.0.mca"))
(def test-header (rf/read-header test-data))

(def offset 69632)
(def size 4096)

(def test-chunk (rf/read-chunk test-data offset size))

