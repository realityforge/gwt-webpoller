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
    final TestWebPoller webSocket = new TestWebPoller( new SimpleEventBus(), mock( RequestFactory.class ), true );

    {
      final StartEvent.Handler handler = mock( StartEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addStartHandler( handler );
      webSocket.onStart();
      verify( handler, only() ).onStartEvent( refEq( new StartEvent( webSocket ), "source" ) );
      registration.removeHandler();
      webSocket.onStart();
      verify( handler, atMost( 1 ) ).onStartEvent( any( StartEvent.class ) );
    }

    {
      final StopEvent.Handler handler = mock( StopEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addStopHandler( handler );
      webSocket.onStop();
      final StopEvent expected = new StopEvent( webSocket );
      verify( handler, only() ).onStopEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      webSocket.onStop();
      verify( handler, atMost( 1 ) ).onStopEvent( any( StopEvent.class ) );
    }

    {
      final MessageEvent.Handler handler = mock( MessageEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addMessageHandler( handler );
      webSocket.onMessage( "Blah" );
      final MessageEvent expected = new MessageEvent( webSocket, "Blah" );
      verify( handler, only() ).onMessageEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      webSocket.onMessage( "Blah" );
      verify( handler, atMost( 1 ) ).onMessageEvent( any( MessageEvent.class ) );
    }

    {
      final ErrorEvent.Handler handler = mock( ErrorEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addErrorHandler( handler );
      webSocket.onError();
      verify( handler, only() ).onErrorEvent( any( ErrorEvent.class ) );
      registration.removeHandler();
      webSocket.onError();
      verify( handler, atMost( 1 ) ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
    }
  }
}
