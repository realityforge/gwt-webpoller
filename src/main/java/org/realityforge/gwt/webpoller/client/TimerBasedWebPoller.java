package org.realityforge.gwt.webpoller.client;

import com.google.gwt.user.client.Timer;
import javax.annotation.Nonnull;

/**
 * A web-poller that used the standard Timer class to schedule requests.
 */
public class TimerBasedWebPoller
  extends WebPoller
{
  private Timer _timer;

  public static class Factory
    implements WebPoller.Factory
  {
    @Nonnull
    @Override
    public WebPoller newWebPoller()
    {
      return new TimerBasedWebPoller();
    }
  }

  @Override
  protected void startTimer()
  {
    doStartTimer( getInterRequestDuration() );
  }

  protected boolean isTimerActive()
  {
    return null != _timer;
  }

  private void doStartTimer( final int pollDuration )
  {
    stopTimer();
    _timer = new Timer()
    {
      @Override
      public void run()
      {
        poll();
      }
    };

    _timer.scheduleRepeating( pollDuration );
  }

  @Override
  protected void stopTimer()
  {
    doStopTimer();
  }

  private void doStopTimer()
  {
    if ( null != _timer )
    {
      _timer.cancel();
      _timer = null;
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
