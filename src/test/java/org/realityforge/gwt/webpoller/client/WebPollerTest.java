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
    assertNull( WebPoller.newWebPoller( requestFactory ) );
    final TestWebPoller.Factory factory = new TestWebPoller.Factory();
    WebPoller.register( factory );
    assertNotNull( WebPoller.newWebPoller( requestFactory ) );
    assertTrue( WebPoller.deregister( factory ) );
    assertNull( WebPoller.newWebPoller( requestFactory ) );
    assertFalse( WebPoller.deregister( factory ) );
  }

  @Test
  public void handlerInteractions()
  {
    final TestWebPoller webPoller = new TestWebPoller( new SimpleEventBus(), mock( RequestFactory.class ) );

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

  @Test
  public void polling()
  {
    final TestWebPoller webPoller = new TestWebPoller( new SimpleEventBus(), mock( RequestFactory.class ) );
    webPoller.setLongPoll( false );
    webPoller.start();

    assertFalse( webPoller.isInPoll() );
    webPoller.poll();
    assertEquals( webPoller._pollCount, 1 );
    webPoller.poll();
    assertEquals( webPoller._pollCount, 1 );
    assertTrue( webPoller.isInPoll() );
    webPoller.pollReturned();
    assertEquals( webPoller._pollCount, 1 );
    assertFalse( webPoller.isInPoll() );

    webPoller.stop();

    // Now test long polling

    webPoller.setLongPoll( true );
    webPoller.start();

    webPoller.poll();
    assertEquals( webPoller._pollCount, 2 );
    webPoller.pollReturned();
    assertEquals( webPoller._pollCount, 3 );
    assertTrue( webPoller.isInPoll() );
    webPoller.stop();

    //poll returned after stop should result in no more polls
    webPoller.pollReturned();
    assertEquals( webPoller._pollCount, 3 );
    assertFalse( webPoller.isInPoll() );
  }

  @Test
  public void basicWorkflow()
  {
    final int errorCountThreshold = 7;

    final TestWebPoller webPoller = new TestWebPoller( new SimpleEventBus(), mock( RequestFactory.class ) );

    final StartEvent.Handler startHandler = mock( StartEvent.Handler.class );
    webPoller.addStartHandler( startHandler );
    final StopEvent.Handler stopHandler = mock( StopEvent.Handler.class );
    webPoller.addStopHandler( stopHandler );
    final MessageEvent.Handler messageHandler = mock( MessageEvent.Handler.class );
    webPoller.addMessageHandler( messageHandler );
    final ErrorEvent.Handler errorHandler = mock( ErrorEvent.Handler.class );
    webPoller.addErrorHandler( errorHandler );

    // Test start
    {
      verify( startHandler, never() ).onStartEvent( any( StartEvent.class ) );
      assertFalse( webPoller.isActive() );
      assertFalse( webPoller.isLongPoll() );

      webPoller.setErrorCountThreshold( errorCountThreshold );
      webPoller.setLongPoll( true );
      assertTrue( webPoller.isLongPoll() );

      webPoller.start();
      assertTrue( webPoller.isActive() );
      verify( startHandler, atMost( 1 ) ).onStartEvent( any( StartEvent.class ) );

      try
      {
        webPoller.setLongPoll( false );
        fail( "Should not be able to setLongPoll on started webPoller" );
      }
      catch ( final IllegalStateException e )
      {
        //Ignore
      }

      try
      {
        webPoller.setErrorCountThreshold( errorCountThreshold );
        fail( "Should not be able to setErrorCountThreshold on started webPoller" );
      }
      catch ( final IllegalStateException e )
      {
        //Ignore
      }
    }

    // Try start again...
    {
      try
      {
        webPoller.start();
        fail( "Expected to fail to start" );
      }
      catch ( final IllegalStateException ise )
      {
        assertTrue( webPoller.isActive() );
        verify( startHandler, atMost( 1 ) ).onStartEvent( any( StartEvent.class ) );
      }
    }

    // Test stop
    {
      verify( stopHandler, never() ).onStopEvent( any( StopEvent.class ) );
      assertTrue( webPoller.isActive() );

      webPoller.stop();
      assertFalse( webPoller.isActive() );
      verify( stopHandler, atMost( 1 ) ).onStopEvent( any( StopEvent.class ) );
    }

    // Try to stop again...
    {
      try
      {
        webPoller.stop();
        fail( "Expected to fail to stop" );
      }
      catch ( final IllegalStateException ise )
      {
        assertFalse( webPoller.isActive() );
        verify( stopHandler, atMost( 1 ) ).onStopEvent( any( StopEvent.class ) );
      }
    }

    // Restart so can test data...
    {
      webPoller.start();
      assertTrue( webPoller.isActive() );
      verify( startHandler, atMost( 2 ) ).onStartEvent( any( StartEvent.class ) );
    }

    //Does data flow through
    {
      final String data = "Blah!";
      webPoller.onMessage( data );
      verify( messageHandler, atMost( 1 ) ).onMessageEvent( refEq( new MessageEvent( webPoller, data ), "source" ) );
    }

    //Error state handling
    {
      assertFalse( webPoller.inError() );
      webPoller.onError();
      verify( errorHandler, atMost( 1 ) ).onErrorEvent( any( ErrorEvent.class ) );
      assertTrue( webPoller.inError() );

      //A few more errors but not enough to close the poller
      for ( int i = 1; i < errorCountThreshold; i++ )
      {
        webPoller.onError();
      }
      verify( errorHandler, atMost( errorCountThreshold ) ).onErrorEvent( any( ErrorEvent.class ) );
      assertTrue( webPoller.inError() );
      assertTrue( webPoller.isActive() );

      // This should result in shutdown of poller
      webPoller.onError();
      assertFalse( webPoller.isActive() );
      assertFalse( webPoller.inError() );
      verify( errorHandler, atMost( errorCountThreshold + 1 ) ).onErrorEvent( any( ErrorEvent.class ) );
      verify( stopHandler, atMost( 2 ) ).onStopEvent( any( StopEvent.class ) );
    }
  }
}
