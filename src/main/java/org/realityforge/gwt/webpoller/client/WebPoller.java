package org.realityforge.gwt.webpoller.client;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WebPoller
{
  private static final Logger LOG = Logger.getLogger( WebPoller.class.getName() );

  /**
   * The duration between polls when not using long polling.
   */
  public static final int DEFAULT_INTER_REQUEST_DURATION = 2000;
  /**
   * The duration between attempts when in error.
   */
  public static final int DEFAULT_INTER_ERROR_DURATION = 2000;
  /**
   * The number of error before the poller is marked as failed.
   */
  public static final int DEFAULT_ERROR_COUNT_THRESHOLD = 5;

  public interface Factory
  {
    @Nonnull
    WebPoller newWebPoller();
  }

  private static Factory g_factory;

  private final RequestContext _requestContext = new WebPollerRequestContext();
  private WebPollerListener _listener = NullWebPollerListener.INSTANCE;
  private RequestFactory _requestFactory;
  private boolean _active;
  private boolean _paused;
  private int _errorCount;
  private int _interRequestDuration = DEFAULT_INTER_REQUEST_DURATION;
  private int _interErrorDuration = DEFAULT_INTER_ERROR_DURATION;
  /**
   * The number of errors before the poller is marked as failed.
   */
  private int _errorCountThreshold = DEFAULT_ERROR_COUNT_THRESHOLD;
  private Request _request;
  private Level _logLevel = Level.FINEST;

  public static WebPoller newWebPoller()
  {
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

  public Level getLogLevel()
  {
    return _logLevel;
  }

  public void setLogLevel( final Level logLevel )
  {
    _logLevel = logLevel;
  }

  /**
   * @return true if the poller is active.
   */
  public boolean isActive()
  {
    return _active;
  }

  /**
   * @return true if active and the last poll resulted in error, false otherwise.
   */
  public boolean inError()
  {
    return isActive() && _errorCount > 0;
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

  public final int getInterRequestDuration()
  {
    return _interRequestDuration;
  }

  public final void setInterRequestDuration( final int interRequestDuration )
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setInterRequestDuration when poller active" );
    }
    _interRequestDuration = interRequestDuration;
  }

  public int getInterErrorDuration()
  {
    return _interErrorDuration;
  }

  public void setInterErrorDuration( final int interErrorDuration )
  {
    if ( isActive() )
    {
      throw new IllegalStateException( "Attempt to invoke setInterErrorDuration when poller active" );
    }
    _interErrorDuration = interErrorDuration;
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
    if ( isTimerActive() )
    {
      stopTimer();
    }
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
   * Stop the timer that is triggering the polling if any exists.
   */
  protected abstract void stopTimer();

  /**
   * Return true if the timer or the error timer is active.
   */
  protected abstract boolean isTimerActive();

  /**
   * Sub-classes should override this method to provide functionality.
   */
  protected final void doStart()
  {
    _active = true;
    onStart();
    initialPoll();
  }

  private void initialPoll()
  {
    if ( 0 >= _interRequestDuration )
    {
      poll();
    }
    else
    {
      log( "Starting WebPoller timer" );
      startTimer();
    }
  }

  protected abstract void startErrorTimer();

  /**
   *
   */
  protected abstract void stopErrorTimer();

  /**
   * Start the timer that triggers the polling.
   */
  protected abstract void startTimer();

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
    if ( 0 != _errorCount )
    {
      log( "Resetting WebPoller error state. Stopping error timer." );
      _errorCount = 0;
      stopErrorTimer();
      initialPoll();
    }
  }

  /**
   * Fire a Start event.
   */
  protected final void onStart()
  {
    log( "WebPoller start message." );
    _listener.onStart( this );
  }

  /**
   * Fire a Stop event.
   */
  protected final void onStop()
  {
    log( "WebPoller stop message." );
    _listener.onStop( this );
  }

  /**
   * Fire a Message event.
   */
  protected final void onMessage( @Nonnull final Map<String, String> context,
                                  @Nonnull final String data )
  {
    log( "WebPoller message received: " + data );
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
    log( "WebPoller error " + _errorCount + "/" + _errorCountThreshold );
    if ( _errorCount > _errorCountThreshold )
    {
      log( "WebPoller exceeded error threshold " + _errorCountThreshold + ". Stopping WebPoller" );
      stopErrorTimer();
      doStop();
    }
    else if ( 1 == _errorCount )
    {
      log( "WebPoller starting error timer." );
      startErrorTimer();
    }
  }

  /**
   * @return true if a poll request is outstanding.
   */
  protected boolean isInPoll()
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
    /*
      We immediately poll if we are active and the inter-request duration is 0.
      However we will not immediately re-issue a poll if in error and the inter-error
      duration is not 0.
     */
    if ( isActive() && 0 >= _interRequestDuration &&
         ( !inError() || 0 >= _interErrorDuration ) )
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
   * Perform actual polling.
   */
  protected void doPoll()
  {
    log( "Performing Poll" );
    try
    {
      _request = getRequestFactory().newRequest( getRequestContext() );
      log( "Poll Scheduled" );
    }
    catch ( final Exception e )
    {
      log( "Poll Scheduled Error. Starting Error timer." );
      getRequestContext().onError( e );
    }
  }

  private void log( final String message )
  {
    if ( LOG.isLoggable( getLogLevel() ) )
    {
      LOG.log( getLogLevel(), message + " @ " + System.currentTimeMillis() );
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
