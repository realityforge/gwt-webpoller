package org.realityforge.gwt.webpoller.client.event;

import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.HashMap;
import org.realityforge.gwt.webpoller.client.TestWebPoller;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class EventBasedWebPollerListenerTest
{
  @Test
  public void handlerInteractions()
  {
    final EventBasedWebPollerListener listener = new EventBasedWebPollerListener();
    final TestWebPoller webPoller = new TestWebPoller();

    {
      final StartEvent.Handler handler = mock( StartEvent.Handler.class );
      final HandlerRegistration registration = listener.addStartHandler( handler );
      listener.onStart( webPoller );
      verify( handler, only() ).onStartEvent( refEq( new StartEvent( webPoller ), "source" ) );
      registration.removeHandler();
      listener.onStart( webPoller );
      verify( handler, atMost( 1 ) ).onStartEvent( any( StartEvent.class ) );
    }

    {
      final StopEvent.Handler handler = mock( StopEvent.Handler.class );
      final HandlerRegistration registration = listener.addStopHandler( handler );
      listener.onStop( webPoller );
      final StopEvent expected = new StopEvent( webPoller );
      verify( handler, only() ).onStopEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      listener.onStop( webPoller );
      verify( handler, atMost( 1 ) ).onStopEvent( any( StopEvent.class ) );
    }

    {
      final MessageEvent.Handler handler = mock( MessageEvent.Handler.class );
      final HandlerRegistration registration = listener.addMessageHandler( handler );
      final HashMap<String, String> context = new HashMap<String, String>();
      listener.onMessage( webPoller, context, "Blah" );
      final MessageEvent expected = new MessageEvent( webPoller, context, "Blah" );
      verify( handler, only() ).onMessageEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      listener.onMessage( webPoller, context, "Blah" );
      verify( handler, atMost( 1 ) ).onMessageEvent( any( MessageEvent.class ) );
    }

    {
      final ErrorEvent.Handler handler = mock( ErrorEvent.Handler.class );
      final HandlerRegistration registration = listener.addErrorHandler( handler );
      final Throwable exception = new Throwable();
      listener.onError( webPoller, exception );
      final ErrorEvent expected = new ErrorEvent( webPoller, exception );
      verify( handler, only() ).onErrorEvent( refEq( expected, "source" ) );
      registration.removeHandler();
      listener.onError( webPoller, exception );
      verify( handler, atMost( 1 ) ).onErrorEvent( any( ErrorEvent.class ) );
    }
  }
}
