package org.realityforge.gwt.webpoller.client;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.gwt.webpoller.client.event.StartEvent;
import org.realityforge.gwt.webpoller.client.event.StopEvent;

public abstract class WebPoller
{
  /**
   * The duration between polls when not using long polling.
   */
  private static final int DEFAULT_POLL_DURATION = 2000;
  /**
   * The number of error before the poller is marked as failed.
   */
  private static final int DEFAULT_ERROR_COUNT_THRESHOLD = 5;

  public interface Factory
  {
    @Nonnull
    WebPoller newWebPoller();
  }

  private static Factory g_factory;

  private final RequestContext _requestContext = new WebPollerRequestContext();
  private final EventBus _eventBus;
  private RequestFactory _requestFactory;
  private boolean _longPoll;
  private boolean _active;
  private int _errorCount;
  private int _pollDuration = DEFAULT_POLL_DURATION;
  /**
   * The number of errors before the poller is marked as failed.
   */
  private int _errorCountThreshold = DEFAULT_ERROR_COUNT_THRESHOLD;
  private Request _request;

  public static WebPoller newWebPoller()
  {
    if ( null == g_factory && GWT.isClient() )
    {
      register( new TimerBasedWebPoller.Factory() );
    }
    return ( null != g_factory ) ? g_factory.newWebPoller() : null;
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

  protected WebPoller( @Nonnull final EventBus eventBus )
  {
    _eventBus = eventBus;
  }

  /**
   * @return true if the poller is active.
   */
  public boolean isActive()
  {
    return _active;
  }

  /**
   * @return false if stopped, otherwise true if the the last poll resulted in error.
   */
  public boolean inError()
  {
    return _errorCount > 0;
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
    if ( null == _requestFactory )
    {
      throw new IllegalStateException( "Start invoked but no RequestFactory specified" );
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

  public void setRequestFactory( @Nullable final RequestFactory requestFactory )
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setRequestFactory when poller active" );
    }
    _requestFactory = requestFactory;
  }

  public final RequestFactory getRequestFactory()
  {
    return _requestFactory;
  }

  /**
   * @return the number of errors before poller is stopped.
   */
  public final int getErrorCountThreshold()
  {
    return _errorCountThreshold;
  }

  /**
   * Set the number of errors before poller is stopped.
   */
  public void setErrorCountThreshold( final int errorCountThreshold )
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setErrorCountThreshold when poller active" );
    }
    _errorCountThreshold = errorCountThreshold;
  }

  public final int getPollDuration()
  {
    return _pollDuration;
  }

  public final void setPollDuration( final int pollDuration )
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setPollDuration when poller active" );
    }
    _pollDuration = pollDuration;
  }

  public final boolean isLongPoll()
  {
    return _longPoll;
  }

  public void setLongPoll( final boolean longPoll )
    throws IllegalStateException
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setLongPoll when poller active" );
    }
    _longPoll = longPoll;
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
    if ( null != _request  )
    {
      _request.cancel();
      _request = null;
    }
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
   * Invoked after a successful poll returning no data.
   */
  protected final void onEmptyPollResult()
  {
    resetErrorState();
  }

  /**
   * Invoked after a successful poll, regardless of whether data was received or not.
   */
  private void resetErrorState()
  {
    _errorCount = 0;
  }

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
  protected final void onMessage( @Nonnull final Map<String, String> context,
                                  @Nonnull final String data )
  {
    _eventBus.fireEventFromSource( new MessageEvent( this, context, data ), this );
    resetErrorState();
  }

  /**
   * Fire an Error event.
   * If the number of successive errors reaches a threshold then shut-down the poller.
   */
  protected final void onError( @Nonnull final Throwable exception )
  {
    _eventBus.fireEventFromSource( new ErrorEvent( this, exception ), this );
    _errorCount++;
    if ( _errorCount > _errorCountThreshold )
    {
      doStop();
    }
  }

  /**
   * @return true if a poll request is outstanding.
   */
  protected final boolean isInPoll()
  {
    return null != _request;
  }

  /**
   * This should be invoked when the poll has completed.
   * It may be overriden by sub-classes to perform other cleanup.
   */
  protected void pollReturned()
  {
    _request = null;
    if ( isActive() && isLongPoll() )
    {
      poll();
    }
  }

  /**
   * Orchestrate the polling.
   */
  protected final void poll()
  {
    if ( !isInPoll() )
    {
      doPoll();
    }
  }

  protected final RequestContext getRequestContext()
  {
    return _requestContext;
  }

  /**
   * Sub-classes should override to perform actual polling.
   */
  protected void doPoll()
  {
    try
    {
      _request = getRequestFactory().newRequest( getRequestContext() );
    }
    catch ( final Exception e )
    {
      getRequestContext().onError( e );
    }
  }

  private class WebPollerRequestContext
    implements RequestContext
  {
    @Override
    public void onEmptyMessage()
    {
      WebPoller.this.onEmptyPollResult();
      WebPoller.this.pollReturned();
    }

    @Override
    public void onMessage( @Nonnull final Map<String, String> context, @Nonnull final String data )
    {
      WebPoller.this.onMessage( context, data );
      WebPoller.this.pollReturned();
    }

    @Override
    public void onError( @Nonnull final Throwable exception )
    {
      WebPoller.this.onError( exception );
      WebPoller.this.pollReturned();
    }
  }
}
