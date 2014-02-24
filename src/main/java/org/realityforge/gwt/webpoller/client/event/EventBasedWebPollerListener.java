package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.Map;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.WebPollerListener;

public class EventBasedWebPollerListener
  implements WebPollerListener
{
  private final EventBus _eventBus;

  public EventBasedWebPollerListener()
  {
    this( new SimpleEventBus() );
  }

  public EventBasedWebPollerListener( @Nonnull final EventBus eventBus )
  {
    _eventBus = eventBus;
  }

  @Nonnull
  public final HandlerRegistration addStartHandler( @Nonnull StartEvent.Handler handler )
  {
    return _eventBus.addHandler( StartEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addStopHandler( @Nonnull StopEvent.Handler handler )
  {
    return _eventBus.addHandler( StopEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addMessageHandler( @Nonnull MessageEvent.Handler handler )
  {
    return _eventBus.addHandler( MessageEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler )
  {
    return _eventBus.addHandler( ErrorEvent.getType(), handler );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void onStart( @Nonnull final WebPoller webPoller )
  {
    _eventBus.fireEventFromSource( new StartEvent( webPoller ), webPoller );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void onStop( @Nonnull final WebPoller webPoller )
  {
    _eventBus.fireEventFromSource( new StopEvent( webPoller ), webPoller );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEmptyMessage( @Nonnull final WebPoller webPoller )
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void onMessage( @Nonnull final WebPoller webPoller,
                               @Nonnull final Map<String, String> context,
                               @Nonnull final String data )
  {
    _eventBus.fireEventFromSource( new MessageEvent( webPoller, context, data ), webPoller );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void onError( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
  {
    _eventBus.fireEventFromSource( new ErrorEvent( webPoller, exception ), webPoller );
  }
}
