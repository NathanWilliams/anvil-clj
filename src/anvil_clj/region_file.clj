(ns anvil-clj.region-file
  "Anvil is based directly from the region file format:
  http://www.minecraftwiki.net/wiki/Region_file_format
  With the only difference being the chunk format inside"
  (:require [gloss.core :as gc]
            [gloss.data.bytes :as gb]
            [gloss.io :as gio]
            [nbt-clj.nbt :as nbt])
  (:import java.io.ByteArrayInputStream
           java.util.zip.GZIPInputStream
           java.util.zip.InflaterInputStream))


;
; Reading a region file header
;

(defn- decode-location [data]
  ; 4 bytes, split 3/1
  ; Top 3 bytes are a file offset in 4K sectors
  ; Bottom byte is the size in 4K sectors
  { :offset (* 4096 (bit-shift-right data 8))
    :size   (* 4096 (bit-and data 0xFF))})

(gc/defcodec- location-frame
  (gc/compile-frame   :int32
                      identity          ;writer transform
                      decode-location)) ;reader transform


(gc/defcodec file-header
  ; 4 byte location  * 1024 entries = 4096 bytes
  ; 4 byte timestamp * 1024 entries = 4096 bytes
  (gc/ordered-map 
    :locations  (gc/finite-frame 4096
                  (gc/repeated location-frame :prefix :none))
    :timestamps (gc/finite-frame 4096
                  (gc/repeated :int32         :prefix :none))))


(defn select-bytes [data offset length]
  "Select a range of bytes from a byte buffer"
  (gb/take-bytes
    (gb/drop-bytes (gb/create-buf-seq data) offset)
    length))

(defn read-header [data]
  "Extract the header record from a region / anvil file." 
  (gio/decode file-header
    (select-bytes data 0 8192)))


;
; Reading a chunk from a region file
;

(gc/defcodec compression-type
  ;The file format and minecraft support both
  ;But so far only ZLib is actually used
  (gc/enum :byte {:GZip 1
                  :ZLib 2}))

(def compressor identity) ;TODO: replace when we add writing to the file

(defn extractor [{:keys [compression-type data]}]
  "Used as a post-decoder by the chunk codec.
  Decompresses either GZip or ZLib"
  (let [byte-stream (ByteArrayInputStream. (byte-array (count data) data))
        stream (if (= compression-type :GZip)
                 (GZIPInputStream. byte-stream)
                 (InflaterInputStream. byte-stream))]
    (loop [result []]
      (let [buf (byte-array 1024) ;We don't know how big the decompressed data will be, so we read up to 1k at a time
            len (.read stream buf)]
        (if (< len 0)
          (gio/to-byte-buffer result)
          (recur (concat result (take len (seq buf)))))))))


(gc/defcodec chunk-codec
  ;The byte layout of a chunk in a region file
  (gc/finite-frame :int32
    (gc/compile-frame
      (gc/ordered-map
        :compression-type compression-type
        :data (gc/repeated :byte :prefix :none))
      compressor
      extractor)))


(defn read-chunk [data offset size]
  "Read a chunk and decode the NBT data inside"
  (when-not (= 0 size)
    (nbt/decode-nbt
      (gio/decode chunk-codec
                  (select-bytes data
                                offset 
                                size)
                  false))))  ;This tells Gloss to ignore any padding in this "sector"



