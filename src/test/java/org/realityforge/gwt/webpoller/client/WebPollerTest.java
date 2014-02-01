package org.realityforge.gwt.webpoller.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.mockito.Mockito;
import org.realityforge.gwt.webpoller.client.WebPoller.RequestFactory;
import org.realityforge.gwt.webpoller.client.event.CloseEvent;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.gwt.webpoller.client.event.OpenEvent;
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
      final OpenEvent.Handler handler = mock( OpenEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addOpenHandler( handler );
      webSocket.onOpen();
      verify( handler, only() ).onOpenEvent( refEq( new OpenEvent( webSocket ), "source" ) );
      registration.removeHandler();
      webSocket.onOpen();
      verify( handler, atMost( 1 ) ).onOpenEvent( any( OpenEvent.class ) );
    }

    {
      final CloseEvent.Handler handler = mock( CloseEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addCloseHandler( handler );
      webSocket.onClose();
      final CloseEvent expected = new CloseEvent( webSocket );
      verify( handler, only() ).onCloseEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      webSocket.onClose();
      verify( handler, atMost( 1 ) ).onCloseEvent( any( CloseEvent.class ) );
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
