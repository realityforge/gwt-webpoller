package org.realityforge.gwt.webpoller.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.mockito.Mockito;
import org.realityforge.gwt.webpoller.client.WebPoller.RequestFactory;
import org.realityforge.gwt.webpoller.client.event.StartEvent;
import org.realityforge.gwt.webpoller.client.event.StopEvent;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class WebPollerTest
{
  @Test
  public void registryTest()
  {
    final RequestFactory requestFactory = mock( RequestFactory.class );
    assertNull( WebPoller.newWebPoller( requestFactory, true ) );
    final TestWebPoller.Factory factory = new TestWebPoller.Factory();
    WebPoller.register( factory );
    assertNotNull( WebPoller.newWebPoller( requestFactory, true ) );
    assertTrue( WebPoller.deregister( factory ) );
    assertNull( WebPoller.newWebPoller( requestFactory, true ) );
    assertFalse( WebPoller.deregister( factory ) );
  }

  @Test
  public void handlerInteractions()
  {
    final TestWebPoller webPoller = new TestWebPoller( new SimpleEventBus(), mock( RequestFactory.class ), true );

    {
      final StartEvent.Handler handler = mock( StartEvent.Handler.class );
      final HandlerRegistration registration = webPoller.addStartHandler( handler );
      webPoller.onStart();
      verify( handler, only() ).onStartEvent( refEq( new StartEvent( webPoller ), "source" ) );
      registration.removeHandler();
      webPoller.onStart();
      verify( handler, atMost( 1 ) ).onStartEvent( any( StartEvent.class ) );
    }

    {
      final StopEvent.Handler handler = mock( StopEvent.Handler.class );
      final HandlerRegistration registration = webPoller.addStopHandler( handler );
      webPoller.onStop();
      final StopEvent expected = new StopEvent( webPoller );
      verify( handler, only() ).onStopEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      webPoller.onStop();
      verify( handler, atMost( 1 ) ).onStopEvent( any( StopEvent.class ) );
    }

    {
      final MessageEvent.Handler handler = mock( MessageEvent.Handler.class );
      final HandlerRegistration registration = webPoller.addMessageHandler( handler );
      webPoller.onMessage( "Blah" );
      final MessageEvent expected = new MessageEvent( webPoller, "Blah" );
      verify( handler, only() ).onMessageEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      webPoller.onMessage( "Blah" );
      verify( handler, atMost( 1 ) ).onMessageEvent( any( MessageEvent.class ) );
    }

    {
      final ErrorEvent.Handler handler = mock( ErrorEvent.Handler.class );
      final HandlerRegistration registration = webPoller.addErrorHandler( handler );
      webPoller.onError();
      verify( handler, only() ).onErrorEvent( any( ErrorEvent.class ) );
      registration.removeHandler();
      webPoller.onError();
      verify( handler, atMost( 1 ) ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
    }
  }
}
