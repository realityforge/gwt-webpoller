package org.realityforge.gwt.webpoller.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.event.StartEvent;
import org.realityforge.gwt.webpoller.client.event.StopEvent;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.gwt.webpoller.client.html5.Html5WebPoller;

public abstract class WebPoller
{
  public interface RequestFactory
  {
    @Nonnull
    Request newRequest();
  }

  public interface Factory
  {
    @Nonnull
    WebPoller newWebPoller( @Nonnull RequestFactory requestFactory, boolean longPoll );
  }

  private static Factory g_factory;
  private final EventBus _eventBus;
  private final RequestFactory _requestFactory;
  private final boolean _longPoll;
  private boolean _active;

  public static WebPoller newWebPoller( @Nonnull final RequestFactory requestFactory, final boolean longPoll )
  {
    if ( null == g_factory && GWT.isClient() )
    {
      register( new Html5WebPoller.Factory() );
    }
    return ( null != g_factory ) ? g_factory.newWebPoller( requestFactory, longPoll ) : null;
  }

  public static void register( @Nonnull final Factory factory )
  {
    g_factory = factory;
  }

  public static boolean deregister( @Nonnull final Factory factory )
  {
    if ( g_factory != factory )
    {
      return false;
    }
    else
    {
      g_factory = null;
      return true;
    }
  }

  protected WebPoller( @Nonnull final EventBus eventBus,
                       @Nonnull final RequestFactory requestFactory,
                       final boolean longPoll )
  {
    _eventBus = eventBus;
    _requestFactory = requestFactory;
    _longPoll = longPoll;
  }

  /**
   * @return true if the poller is active.
   */
  public boolean isActive()
  {
    return _active;
  }

  /**
   * Start polling.
   *
   * @throws IllegalStateException if the poller is already active.
   */
  public final void start()
    throws IllegalStateException
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Start invoked on active poller" );
    }
    doStart();
  }

  /**
   * Stop polling.
   *
   * @throws IllegalStateException if the poller is not active.
   */
  public final void stop()
    throws IllegalStateException
  {
    if ( !isActive() )
    {
      throw new IllegalStateException( "Stop invoked on inactive poller" );
    }
    doStop();
  }

  protected final RequestFactory getRequestFactory()
  {
    return _requestFactory;
  }

  protected final boolean isLongPoll()
  {
    return _longPoll;
  }

  protected final EventBus getEventBus()
  {
    return _eventBus;
  }

  /**
   * Sub-classes should override this method to provide functionality.
   */
  protected void doStop()
  {
    _active = false;
    _errorCount = 0;
    onStop();
  }

  /**
   * Sub-classes should override this method to provide functionality.
   */
  protected void doStart()
  {
    _active = true;
    onStart();
  }

  /**
   */

  @Nonnull
  public final HandlerRegistration addStartHandler( @Nonnull StartEvent.Handler handler )
  {
    return getEventBus().addHandler( StartEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addStopHandler( @Nonnull StopEvent.Handler handler )
  {
    return getEventBus().addHandler( StopEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addMessageHandler( @Nonnull MessageEvent.Handler handler )
  {
    return getEventBus().addHandler( MessageEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler )
  {
    return getEventBus().addHandler( ErrorEvent.getType(), handler );
  }

  /**
   * Fire a Start event.
   */
  protected final void onStart()
  {
    _eventBus.fireEventFromSource( new StartEvent( this ), this );
  }

  /**
   * Fire a Stop event.
   */
  protected final void onStop()
  {
    _eventBus.fireEventFromSource( new StopEvent( this ), this );
  }

  /**
   * Fire a Message event.
   */
  protected final void onMessage( final String data )
  {
    _eventBus.fireEventFromSource( new MessageEvent( this, data ), this );
  }

  /**
   * Fire an Error event.
   */
  protected final void onError()
  {
    _eventBus.fireEventFromSource( new ErrorEvent( this ), this );
  }
}
