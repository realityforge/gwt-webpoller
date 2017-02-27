package org.realityforge.gwt.webpoller.server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.gwt.webpoller.client.WebPoller;

/**
 * A web-poller that used the standard Timer class to schedule requests.
 */
public class TimerBasedWebPoller
  extends WebPoller
{
  @Nonnull
  private final ScheduledExecutorService _timer;
  @Nullable
  private ScheduledFuture<?> _future;
  private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock( true );

  public static class Factory
    implements WebPoller.Factory
  {
    private final ScheduledExecutorService _service;

    public Factory( @Nonnull final ScheduledExecutorService service )
    {
      _service = service;
    }

    @Nonnull
    @Override
    public WebPoller newWebPoller()
    {
      return new TimerBasedWebPoller( _service );
    }
  }

  public TimerBasedWebPoller( @Nonnull final ScheduledExecutorService timer )
  {
    _timer = timer;
  }

  @Override
  protected void startTimer()
  {
    doStartTimer( getInterRequestDuration() );
  }

  protected boolean isTimerActive()
  {
    return withLock( _lock.readLock(), () -> null != _future );
  }

  private void doStartTimer( final int pollDuration )
  {
    withLock( _lock.writeLock(), () ->
    {
      stopTimer();
      _future = _timer.scheduleAtFixedRate( this::poll, 0, pollDuration, TimeUnit.MILLISECONDS );
    } );
  }

  @Override
  protected void stopTimer()
  {
    doStopTimer();
  }

  private void doStopTimer()
  {
    withLock( _lock.writeLock(), () ->
    {
      if ( null != _future )
      {
        _future.cancel( true );
        _future = null;
      }
    } );
  }

  @Override
  protected void startErrorTimer()
  {
    doStartTimer( getInterErrorDuration() );
  }

  @Override
  protected void stopErrorTimer()
  {
    doStopTimer();
  }

  @Override
  public void start()
  {
    withLock( _lock.writeLock(), super::start );
  }

  @Override
  public void stop()
  {
    withLock( _lock.writeLock(), super::stop );
  }

  @Override
  public void pause()
  {
    withLock( _lock.writeLock(), super::pause );
  }

  @Override
  public void resume()
  {
    withLock( _lock.writeLock(), super::resume );
  }

  @Override
  public boolean isPaused()
  {
    return withLock( _lock.readLock(), super::isPaused );
  }

  @Override
  public boolean isActive()
  {
    return withLock( _lock.readLock(), super::isActive );
  }

  @Override
  public boolean inError()
  {
    return withLock( _lock.readLock(), super::inError );
  }

  @Override
  protected void resetErrorState()
  {
    withLock( _lock.writeLock(), () -> super.resetErrorState() );
  }

  @Override
  protected void incErrorCount()
  {
    withLock( _lock.writeLock(), () -> super.incErrorCount() );
  }

  @Override
  protected boolean isInPoll()
  {
    return withLock( _lock.readLock(), () -> super.isInPoll() );
  }

  @Override
  protected void pollReturned()
  {
    withLock( _lock.writeLock(), () -> super.pollReturned() );
  }

  @Override
  protected void poll()
  {
    withLock( _lock.writeLock(), () -> super.poll() );
  }

  private <T> T withLock( final Lock lock, final Supplier<T> action )
  {
    lock.lock();
    try
    {
      return action.get();
    }
    finally
    {
      lock.unlock();
    }
  }

  private void withLock( final Lock lock, final Runnable action )
  {
    lock.lock();
    try
    {
      action.run();
    }
    finally
    {
      lock.unlock();
    }
  }
}
