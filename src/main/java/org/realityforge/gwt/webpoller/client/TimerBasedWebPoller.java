package org.realityforge.gwt.webpoller.client;

import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.RequestFactory;
import org.realityforge.gwt.webpoller.client.WebPoller;

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
    public WebPoller newWebPoller( @Nonnull final RequestFactory requestFactory )
    {
      return new TimerBasedWebPoller( new SimpleEventBus(), requestFactory );
    }
  }

  public TimerBasedWebPoller( @Nonnull final EventBus eventBus,
                              @Nonnull final RequestFactory requestFactory )
  {
    super( eventBus, requestFactory );
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
