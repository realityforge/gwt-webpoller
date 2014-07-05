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
