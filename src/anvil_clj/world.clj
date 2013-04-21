(ns anvil-clj.world
  "A 'world' is a minecraft save game.
  This library finds saves on the local computer, 
  lists them and determines the correct files to load for the different dimensions in Minecraft"
  (:require [me.raynes.fs :as fs]
            [gloss.io :only [to-byte-buffer] :as gloss]
            [clojure.java.io :only [input-stream] :as io]))

(def os
  "The current OS"
  ({"Mac OS X" :mac
    "Linux" :linux
    "Windows" :windows} ;TODO: verify this is correct
   (System/getProperty "os.name")))

(def minecraft-directory
  "Where the minecraft directory is expected on the current OS"
  (case os
    :mac (fs/expand-home "~/Library/Application Support/minecraft")
    :linux (fs/expand-home "~/.minecraft")
    :windows "%APP_DATA%/.minecraft")) ; FIXME: I am not sure how to expand %APP_DATA%

(def worlds-directory
  "The full path of where Minecraft worlds (saves) are expected on this computer"
  (str minecraft-directory "/saves"))

(defn- list-directory [path]
  (map #(str path "/" %)
    (fs/with-cwd path
      (fs/list-dir ".")))) 

(defn- is-world? [path]
  "Does basic validation on a path to determine if it is a Minecraft world"
  (and (fs/directory? path) 
       (fs/directory? (str path "/region"))))

(defn list-worlds []
  "List the Minecraft worlds on this computer.
  This returns a map between the world name and the world directory"
  (apply merge
    (map (fn [d] {(fs/base-name d) d})
      (filter is-world? (list-directory worlds-directory)))))

(defn dimension-path [world dimension]
  "Given a world name and a dimension, returns the path to anvil files.
  Dimesions are:
    :overworld
    :nether
    :end"
  (str ((list-worlds) world)
       ({ :overworld  "/region"
          :nether     "/DIM-1/region"
          :end        "/DIM1/region"} dimension)))


(defn- load-binary-file- [path]
  "Loads a file into a byte buffer for consumption by Gloss"
  (with-open [f (io/input-stream path)]
    (let [ba (byte-array (fs/size path))]
      (.read f ba)
      (gloss/to-byte-buffer ba))))
        
; TODO This is to be replaced by a smarter memoization cache
; We need to limit the amount loaded into memory, and
; we need the ability to invalidate cache entries when the file changes
; either by a running game / editor, or a future version of this library
; (one with write support!)
(def load-binary-file 
  "Loads a file into a byte buffer for consumption by Gloss and caches it"
  (memoize load-binary-file-))

(defn region-pos->filename [reg-x reg-z]
  "Takes region based XZ coordinates and returns the matching filename"
  (str "r." reg-x "." reg-z ".mca"))

