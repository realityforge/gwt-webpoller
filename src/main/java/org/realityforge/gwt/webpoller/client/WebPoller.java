package org.realityforge.gwt.webpoller.client;

import com.google.gwt.core.shared.GWT;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
  private WebPollerListener _listener = NullWebPollerListener.INSTANCE;
  private RequestFactory _requestFactory;
  private boolean _longPoll;
  private boolean _active;
  private boolean _paused;
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

  public void setListener( @Nullable final WebPollerListener listener )
  {
    _listener = null == listener ? NullWebPollerListener.INSTANCE : listener;
  }

  /**
   * Pause the active poller.
   * The poller will cease actually polling until resume is called.
   *
   * @throws IllegalStateException if poller is not active or poller is already paused
   */
  public void pause()
  {
    if ( !isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke pause when poller is inactive" );
    }
    if ( isPaused() )
    {
      throw new IllegalStateException( "Attempt to invoke pause when poller is already paused" );
    }
    _paused = true;
  }

  /**
   * Resume an already paused poller.
   * The poller will resume polling.
   *
   * @throws IllegalStateException if poller is not active or poller is not paused.
   */
  public void resume()
  {
    if ( !isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke resume when poller is inactive" );
    }
    if ( !isPaused() )
    {
      throw new IllegalStateException( "Attempt to invoke resume when poller is not paused" );
    }
    _paused = false;
    poll();
  }

  /**
   * @return true if poller is paused.
   */
  public boolean isPaused()
  {
    return _paused;
  }

  /**
   * Sub-classes should override this method to provide functionality.
   */
  protected void doStop()
  {
    if ( null != _request )
    {
      _request.cancel();
      _request = null;
    }
    _paused = false;
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

  /**
   * Fire a Start event.
   */
  protected final void onStart()
  {
    _listener.onStart( this );
  }

  /**
   * Fire a Stop event.
   */
  protected final void onStop()
  {
    _listener.onStop( this );
  }

  /**
   * Fire a Message event.
   */
  protected final void onMessage( @Nonnull final Map<String, String> context,
                                  @Nonnull final String data )
  {
    _listener.onMessage( this, context, data );
    resetErrorState();
  }

  /**
   * Fire an Error event.
   * If the number of successive errors reaches a threshold then shut-down the poller.
   */
  protected final void onError( @Nonnull final Throwable exception )
  {
    _listener.onError( this, exception );
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
    if ( !isInPoll() && !isPaused() )
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
