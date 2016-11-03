package org.realityforge.gwt.webpoller.client;

import java.util.HashMap;
import java.util.logging.Level;
import org.realityforge.gwt.webpoller.client.TestWebPoller.TestRequestFactory;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class WebPollerTest
{
  @Test
  public void registryTest()
  {
    assertNull( WebPoller.newWebPoller() );
    final TestWebPoller.Factory factory = new TestWebPoller.Factory();
    WebPoller.register( factory );
    assertNotNull( WebPoller.newWebPoller() );
    assertTrue( WebPoller.deregister( factory ) );
    assertNull( WebPoller.newWebPoller() );
    assertFalse( WebPoller.deregister( factory ) );
  }

  @Test
  public void polling()
  {
    final TestWebPoller webPoller = new TestWebPoller();
    webPoller.setRequestFactory( new TestRequestFactory() );
    webPoller.setInterRequestDuration( 100 );
    webPoller.setLogLevel( Level.OFF );
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

    webPoller.setInterRequestDuration( 0 );
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
  public void pollInError()
  {
    final TestWebPoller webPoller = new TestWebPoller();
    webPoller.setRequestFactory( new TestRequestFactory() );
    webPoller.setInterRequestDuration( 100 );
    webPoller.setInterErrorDuration( 100 );
    webPoller.setErrorCountThreshold( 2 );

    final WebPollerListener listener = mock( WebPollerListener.class );
    webPoller.setListener( listener );

    webPoller.start();

    assertFalse( webPoller.isInPoll() );
    webPoller.poll();
    assertEquals( webPoller._pollCount, 1 );
    webPoller.poll();
    assertEquals( webPoller._pollCount, 1 );
    assertTrue( webPoller.isInPoll() );

    assertEquals( webPoller._startErrorTimerCount, 0 );
    final Throwable exception1 = new Throwable();
    webPoller.onError( exception1 );
    assertTrue( webPoller.inError() );
    assertEquals( webPoller._startErrorTimerCount, 1 );
    assertEquals( webPoller._stopErrorTimerCount, 0 );
    verify( listener ).onError( webPoller, exception1 );

    // Poll completes...
    webPoller.pollReturned();
    assertEquals( webPoller._pollCount, 1 );
    assertFalse( webPoller.isInPoll() );

    // Error Timer triggers another poll
    webPoller.poll();
    assertEquals( webPoller._pollCount, 2 );
    assertTrue( webPoller.isInPoll() );

    final Throwable exception2 = new Throwable();
    webPoller.onError( exception2 );
    verify( listener ).onError( webPoller, exception2 );
    assertTrue( webPoller.inError() );
    assertEquals( webPoller._startErrorTimerCount, 1 );
    assertEquals( webPoller._stopErrorTimerCount, 0 );

    // Poll completes...
    webPoller.pollReturned();
    assertEquals( webPoller._pollCount, 2 );
    assertFalse( webPoller.isInPoll() );

    // Error Timer triggers another poll
    webPoller.poll();
    assertEquals( webPoller._pollCount, 3 );
    assertTrue( webPoller.isInPoll() );

    assertEquals( webPoller._stopTimerCount, 0 );
    final Throwable exception3 = new Throwable();
    webPoller.onError( exception3 );
    verify( listener ).onError( webPoller, exception3 );
    verify( listener ).onStop( webPoller );
    assertEquals( webPoller._startErrorTimerCount, 1 );
    assertEquals( webPoller._stopErrorTimerCount, 1 );
    assertEquals( webPoller._stopTimerCount, 0 );
    assertFalse( webPoller.isInPoll() );
    assertFalse( webPoller.isActive() );
    assertFalse( webPoller.inError() );
  }

  @Test
  public void basicWorkflow()
  {
    final int errorCountThreshold = 7;

    final TestWebPoller webPoller = new TestWebPoller();
    webPoller.setRequestFactory( new TestRequestFactory() );

    assertEquals( webPoller.getErrorCountThreshold(), WebPoller.DEFAULT_ERROR_COUNT_THRESHOLD );
    assertEquals( webPoller.getInterRequestDuration(), WebPoller.DEFAULT_INTER_REQUEST_DURATION );
    assertEquals( webPoller.getInterErrorDuration(), WebPoller.DEFAULT_INTER_ERROR_DURATION );

    final WebPollerListener listener = mock( WebPollerListener.class );
    webPoller.setListener( listener );

    // Test start
    {
      verify( listener, never() ).onStart( webPoller );
      assertFalse( webPoller.isActive() );

      webPoller.setInterRequestDuration( 50 );
      assertEquals( webPoller.getInterRequestDuration(), 50 );

      webPoller.setInterErrorDuration( 52 );
      assertEquals( webPoller.getInterErrorDuration(), 52 );

      webPoller.setErrorCountThreshold( errorCountThreshold );
      assertEquals( webPoller.getErrorCountThreshold(), errorCountThreshold );

      webPoller.setInterRequestDuration( 0 );
      assertEquals( webPoller.getInterRequestDuration(), 0 );

      webPoller.start();
      assertTrue( webPoller.isActive() );
      verify( listener, times( 1 ) ).onStart( webPoller );

      try
      {
        webPoller.setInterRequestDuration( 23 );
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

      try
      {
        webPoller.setInterRequestDuration( 50 );
        fail( "Should not be able to setInterRequestDuration on started webPoller" );
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
        verify( listener, atMost( 1 ) ).onStart( webPoller );
      }
    }

    // Test stop
    {
      verify( listener, never() ).onStop( webPoller );
      assertTrue( webPoller.isActive() );

      webPoller.stop();
      assertFalse( webPoller.isActive() );
      verify( listener, atMost( 1 ) ).onStop( webPoller );
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
        verify( listener, atMost( 1 ) ).onStop( webPoller );
      }
    }

    // Restart so can test data...
    {
      webPoller.start();
      assertTrue( webPoller.isActive() );
      verify( listener, atMost( 2 ) ).onStart( webPoller );
    }

    //Does data flow through
    {
      final String data = "Blah!";
      final HashMap<String, String> context = new HashMap<>();
      webPoller.onMessage( context, data );
      verify( listener, atMost( 1 ) ).onMessage( webPoller, context, data );
    }

    //Error state handling
    {
      assertFalse( webPoller.inError() );
      final Exception exception = new Exception();
      webPoller.onError( exception );
      verify( listener, atMost( 1 ) ).onError( webPoller, exception );
      assertTrue( webPoller.inError() );

      //A few more errors but not enough to close the poller
      for ( int i = 1; i < errorCountThreshold; i++ )
      {
        webPoller.onError( new Exception() );
      }
      verify( listener, atMost( errorCountThreshold ) ).onError( eq( webPoller ), any( Exception.class ) );
      assertTrue( webPoller.inError() );
      assertTrue( webPoller.isActive() );

      // This should result in shutdown of poller
      webPoller.onError( new Exception() );
      assertFalse( webPoller.isActive() );
      assertFalse( webPoller.inError() );
      verify( listener, atMost( errorCountThreshold + 1 ) ).onError( eq( webPoller ), any( Exception.class ) );
      verify( listener, atMost( 2 ) ).onStop( webPoller );
    }
  }


  @Test
  public void pauseResume()
  {
    final TestWebPoller webPoller = new TestWebPoller();
    webPoller.setRequestFactory( new TestRequestFactory() );

    try
    {
      webPoller.pause();
      fail( "Able to pause an inactive poller" );
    }
    catch ( final IllegalStateException ise )
    {
      //Expected
    }
    try
    {
      webPoller.resume();
      fail( "Able to resume an inactive poller" );
    }
    catch ( final IllegalStateException ise )
    {
      //Expected
    }

    webPoller.start();
    try
    {
      webPoller.resume();
      fail( "Able to resume an unpaused poller" );
    }
    catch ( final IllegalStateException ise )
    {
      //Expected
    }

    assertFalse( webPoller.isPaused() );
    webPoller.pause();
    assertTrue( webPoller.isPaused() );

    try
    {
      webPoller.pause();
      fail( "Able to pause a paused poller" );
    }
    catch ( final IllegalStateException ise )
    {
      //Expected
    }

    webPoller.poll();
    assertEquals( webPoller._pollCount, 0 );

    webPoller.resume();
    assertEquals( webPoller._pollCount, 1 );

    webPoller.pause();
    webPoller.stop();
    assertFalse( webPoller.isPaused() );
  }
}
