gwt-webpoller
-------------

[![Build Status](https://secure.travis-ci.org/realityforge/gwt-webpoller.png?branch=master)](http://travis-ci.org/realityforge/gwt-webpoller)

A simple library to provide a simple web poller for GWT.

Quick Start
===========

The simplest way to use the library is to add the following dependency
into the build system. i.e.

```xml
<dependency>
   <groupId>org.realityforge.gwt.webpoller</groupId>
   <artifactId>gwt-webpoller</artifactId>
   <version>0.6</version>
   <scope>provided</scope>
</dependency>
```

Then you add the following snippet into the .gwt.xml file.

```xml
<module rename-to='myapp'>
  ...

  <!-- Enable the websocket library -->
  <inherits name="org.realityforge.gwt.webpoller.WebPoller"/>
</module>
```

Then you can interact with the WebPoller from within the browser.

```java
final WebPoller webPoller = WebPoller.newWebPoller();
final RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, "http://example.com/someUrl" );
webPoller.setRequestFactory( new HttpRequestFactory( requestBuilder ) );
webPoller.setListener( new WebPollerListenerAdapter()
{
  @Override
  public void onStart( @Nonnull final WebPoller webPoller )
  {
    // Polling started!
  }

  @Override
  public void onStop( @Nonnull final WebPoller webPoller )
  {
    // Polling stopped!
  }

  @Override
  public void onMessage( @Nonnull final WebPoller webPoller,
                         @Nonnull final Map<String, String> context,
                         @Nonnull final String data )
  {
    //Handle message
  }

  @Override
  public void onError( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
  {
    //Handle error
  }
} );
webPoller.start();
...
webPoller.stop();
```

This should be sufficient to put together a simple WebPoller application.

A very simple example of this code is available in the
[gwt-webpoller-example](https://github.com/realityforge/gwt-webpoller-example)
project.
