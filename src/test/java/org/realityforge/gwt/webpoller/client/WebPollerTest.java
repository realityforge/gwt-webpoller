package org.realityforge.gwt.webpoller.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.mockito.Mockito;
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
    assertNull( WebPoller.newWebPoller( true ) );
    final TestWebPoller.Factory factory = new TestWebPoller.Factory();
    WebPoller.register( factory );
    assertNotNull( WebPoller.newWebPoller( true ) );
    assertTrue( WebPoller.deregister( factory ) );
    assertNull( WebPoller.newWebPoller( true ) );
    assertFalse( WebPoller.deregister( factory ) );
  }

  @Test
  public void handlerInteractions()
  {
    final TestWebPoller webSocket = new TestWebPoller( new SimpleEventBus() );

    {
      final OpenEvent.Handler handler = mock( OpenEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addOpenHandler( handler );
      webSocket.onOpen();
      verify( handler, only() ).onOpenEvent( Mockito.<OpenEvent>anyObject() );
      registration.removeHandler();
      webSocket.onOpen();
      verify( handler, atMost( 1 ) ).onOpenEvent( Mockito.<OpenEvent>anyObject() );
    }

    {
      final CloseEvent.Handler handler = mock( CloseEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addCloseHandler( handler );
      webSocket.onClose();
      final CloseEvent expected = new CloseEvent( webSocket );
      verify( handler, only() ).onCloseEvent( Mockito.<CloseEvent>refEq( expected, "source" ) );
      registration.removeHandler();
      webSocket.onClose();
      verify( handler, atMost( 1 ) ).onCloseEvent( Mockito.<CloseEvent>anyObject() );
    }

    {
      final MessageEvent.Handler handler = mock( MessageEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addMessageHandler( handler );
      webSocket.onMessage( "Blah" );
      final MessageEvent expected = new MessageEvent( webSocket, "Blah" );
      verify( handler, only() ).onMessageEvent( Mockito.<MessageEvent>refEq( expected, "source" ) );
      registration.removeHandler();
      webSocket.onMessage( "Blah" );
      verify( handler, atMost( 1 ) ).onMessageEvent( Mockito.<MessageEvent>anyObject() );
    }

    {
      final ErrorEvent.Handler handler = mock( ErrorEvent.Handler.class );
      final HandlerRegistration registration = webSocket.addErrorHandler( handler );
      webSocket.onError();
      verify( handler, only() ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
      registration.removeHandler();
      webSocket.onError();
      verify( handler, atMost( 1 ) ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
    }
  }
}
