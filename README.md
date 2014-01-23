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
   <version>0.1</version>
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
final WebPoller webPoller = WebPoller.newWebPoller( true );
if ( null != webPoller )
{
  webPoller.addOpenHandler( new OpenEvent.Handler()
  {
    @Override
    public void onOpenEvent( @Nonnull final OpenEvent event )
    {
      // Connected!
    }
  } );
  webPoller.addMessageHandler( new MessageEvent.Handler()
  {
    @Override
    public void onMessageEvent( @Nonnull final MessageEvent event )
    {
      //Handle message
    }
  } );
  webPoller.connect( "someurl.ext" );
}
```

This should be sufficient to put together a simple WebPoller application.

A very simple example of this code is available in the
[gwt-webpoller-example](https://github.com/realityforge/gwt-webpoller-example)
project.
