package org.realityforge.gwt.webpoller.client;

import elemental2.dom.DomGlobal;
import javax.annotation.Nonnull;

/**
 * A web-poller that used the standard Timer class to schedule requests.
 */
public class TimerBasedWebPoller
  extends WebPoller
{
  private double _intervalTimerId;

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
    return 0 != _intervalTimerId;
  }

  private void doStartTimer( final int pollDuration )
  {
    stopTimer();
    _intervalTimerId = DomGlobal.setInterval( t -> poll(), pollDuration );
  }

  @Override
  protected void stopTimer()
  {
    doStopTimer();
  }

  private void doStopTimer()
  {
    if ( 0 != _intervalTimerId )
    {
      DomGlobal.clearInterval( _intervalTimerId );
      _intervalTimerId = 0;
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
