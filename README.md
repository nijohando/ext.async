# ext.async

[![Clojars Project](https://img.shields.io/clojars/v/jp.nijohando/ext.async.svg)](https://clojars.org/jp.nijohando/ext.async)
[![CircleCI](https://circleci.com/gh/nijohando/ext.async.svg?style=shield)](https://circleci.com/gh/nijohando/ext.async)

core.async helper utilities.

     markdown
## Installation

#### Ligningen / Boot

```clojure
[jp.nijohando/ext.async "0.1.0"]
```

#### Clojure CLI / deps.edn

```clojure
jp.nijohando/ext.async {:mvn/version "0.1.0"}
```

## Usage

Clojure

```clojure
(require '[jp.nijohando.ext.async :as xa]
         '[clojure.core.async :as ca])
```

CloureScript

```clojure
(require '[jp.nijohando.ext.async :as xa :include-macros true]
         '[clojure.core.async :as ca :include-macros true])
```

### Extended read and write macros

ext.async provides read( `<!` , `<!!` ) and write ( `>!` , `>!!` ) macros that work in the same way as core.async's one

```clojure
(def c (ca/chan 1))

(xa/>!! c "foo")
(xa/<!! c)
(ca/go
  (xa/>! c "bar"))
(ca/go
  (xa/<! c)) 
```

The difference is error handling integrated with [nijohando/failable](https://github.com/nijohando/failable)

#### Read and write timeout

These macros accept timeout option.

```clojure
(xa/<!! c :timeout 1000)
(xa/>!! c "foo" :timeout 1000)
(ca/go
  (xa/<! c :timeout 1000)
  (xa/>! c "foo" :timeout 1000))
```

Read and write macros with the option return a failure object if it times out.

```clojure
(xa/<!! c :timeout 1000)
;=> #jp.nijohando.failable.Failure{
;     :jp.nijohando.failable/reason :jp.nijohando.ext.async/timeout}
```

#### Writing to closed channel

Write macros return a failure object if the channel is closed.

```clojure
(ca/close! c)
(xa/>!! c)
;=> #jp.nijohando.failable.Failure{
;     :jp.nijohando.failable/reason :jp.nijohando.ext.async/closed}
```

#### Error handling using nijohando/failable

Timeout and write error are expressed as a failure object, it can be handled by nijohando/failable utilities.

```clojure
(require '[jp.nijohando.failable :as f])
(f/if-succ [x (xa/>!! c "foo" :timeout 5000)]
  (prn "success")
  (condp = @x
    ::xa/timeout (prn "timeout!")
    ::xa/closed  (prn "channel closed!")))
```


## License

© 2018 nijohando  

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

