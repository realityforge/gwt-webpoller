## 0.9.7:

* Update the `RequestFactory.newRequest(...)` method so that it can return null. When null is returned
  the WebPoller skips that poll attempt.

## 0.9.6:

* Introduce the `CancelableRequestFactory` support class.

## 0.9.5:

* Before calling poll check that WebPoller is active.

## 0.9.4:

* Introduce CancelableRequestWrapper to support deferred cancel requests.

## 0.9.3:

* Move to Java 8 and GWT 2.8.
* Add a `ReentrantReadWriteLock` around access to critical resources in `server.TimerBasedWebPoller`.
* Synchronize access to Request.cancel() method in server mode to avoid doubly canceling futures.

## 0.9.2:

* Handle `CancellationException` gracefully in `AbstractJaxrsHttpRequestFactory`.

## 0.9.1:

* Require explicit registration of WebPoller factory in context of GWT application rather than
  relying on implicit registration. i.e The following needs to be added before creating poller
  `WebPoller.register( new TimerBasedWebPoller.Factory() );`

## 0.9:

* Add server package that supports using WebPoller from within an enterprise java context.
* Ensure that the WebPoller can be loaded when the GWT libraries are not present
  on the classpath.

## 0.8:

* Fix the default WebPoller log level to be FINEST rather than OFF which results in
  the WebPoller logging at the highest log level.

## 0.7:

* Fix bug in IE handling headers due to GWT parsing null header values.
* Refactor WebPoller to remove "LongPoll" flag and "PollDuration" configuration. Replace
  concepts with "InterRequestDuration" setting that determines the amount of time between
  one request completing and the next request starting.
* Add "InterErrorDuration" setting that determines the amount of time between
  one error and re-attempting the next request.
* Add WebPoller.setLogLevel() method to make it easier to debug WebPoller.

## 0.6:

* Move WebPoller to using the WebPollerListener interface as the primary means of notifying library
  consumers of WebPoller events. Add an EventBasedWebPollerListener to support the previous
  event-oriented mechanisms of integrating with the WebPoller library.

## 0.5:

* Add a method to AbstractHttpRequestFactory that determines whether a response is
  an error. The default implementation treats all non-200 status code responses as
  errors.

## 0.4:

* Restore Java 6 compatibility.

## 0.3:

* Support pausing and resuming an active poller.

## 0.2:

* Rework the poller so that the RequestFactory can be changed as long as the
  WebPoller is not active. It no longer needs to be supplied when creating the
  WebPoller.

## 0.1:

* Initial release
