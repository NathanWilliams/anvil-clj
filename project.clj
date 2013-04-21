(defproject anvil-clj "0.1.0-SNAPSHOT"
  :description "Minecraft anivl file library"
  :url "https://github.com/NathanWilliams/anvil-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.2"]]}}

  :dependencies [[org.clojure/clojure "1.5.0"]
                 [nbt-clj "0.1.1-SNAPSHOT"] ; My nbt library
                 [me.raynes/fs "1.4.0"]     ; Filesystem functions
                 [gloss "0.2.2-beta5"]]
  :main anvil-clj.devel)
