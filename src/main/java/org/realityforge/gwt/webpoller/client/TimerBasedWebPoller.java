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
  protected void doStop()
  {
    stopTimer();
    super.doStop();
  }

  @Override
  protected void doStart()
  {
    super.doStart();
    if ( isLongPoll() )
    {
      poll();
    }
    else
    {
      startTimer();
    }
  }

  private void startTimer()
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

    _timer.scheduleRepeating( getPollDuration() );
  }

  private void stopTimer()
  {
    if ( null != _timer )
    {
      _timer.cancel();
      _timer = null;
    }
  }
}
