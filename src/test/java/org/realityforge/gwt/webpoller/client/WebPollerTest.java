package org.realityforge.gwt.webpoller.client;

import java.util.HashMap;
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

    final TestWebPoller webPoller = new TestWebPoller();
    webPoller.setRequestFactory( new TestRequestFactory() );

    assertEquals( webPoller.getErrorCountThreshold(), 5 );
    assertEquals( webPoller.getPollDuration(), 2000 );

    final WebPollerListener listener = mock( WebPollerListener.class );
    webPoller.setListener( listener );

    // Test start
    {
      verify( listener, never() ).onStart( webPoller );
      assertFalse( webPoller.isActive() );
      assertFalse( webPoller.isLongPoll() );

      webPoller.setPollDuration( 50 );
      webPoller.setErrorCountThreshold( errorCountThreshold );
      webPoller.setLongPoll( true );
      assertTrue( webPoller.isLongPoll() );

      webPoller.start();
      assertTrue( webPoller.isActive() );
      verify( listener, atMost( 1 ) ).onStart( webPoller );

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

      try
      {
        webPoller.setPollDuration( 50 );
        fail( "Should not be able to setPollDuration on started webPoller" );
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
      final HashMap<String, String> context = new HashMap<String, String>();
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
