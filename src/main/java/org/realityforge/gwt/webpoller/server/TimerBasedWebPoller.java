package org.realityforge.gwt.webpoller.server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
  private final ReentrantReadWriteLock _timerLock = new ReentrantReadWriteLock( true );

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
    _timerLock.readLock().lock();
    try
    {
      return null != _future;
    }
    finally
    {
      _timerLock.readLock().unlock();
    }
  }

  private void doStartTimer( final int pollDuration )
  {
    _timerLock.writeLock().lock();

    try
    {
      stopTimer();
      final Runnable command = new Runnable()
      {
        @Override
        public void run()
        {
          poll();
        }
      };
      _future = _timer.scheduleAtFixedRate( command, 0, pollDuration, TimeUnit.MILLISECONDS );
    }
    finally
    {
      _timerLock.writeLock().unlock();
    }
  }

  @Override
  protected void stopTimer()
  {
    doStopTimer();
  }

  private void doStopTimer()
  {
    _timerLock.writeLock().lock();
    try
    {
      if ( null != _future )
      {
        _future.cancel( true );
        _future = null;
      }
    }
    finally
    {
      _timerLock.writeLock().unlock();
    }
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
}
