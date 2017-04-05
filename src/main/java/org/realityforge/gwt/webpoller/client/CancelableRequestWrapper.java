package org.realityforge.gwt.webpoller.client;

public final class CancelableRequestWrapper
  implements Request
{
  private Request _request;
  private boolean _cancelled;

  public void setRequest( final Request request )
  {
    _request = request;
    if ( _cancelled )
    {
      cancel();
    }
  }

  @Override
  public void cancel()
  {
    if ( null != _request )
    {
      _request.cancel();
      _request = null;
    }
    _cancelled = true;
  }
}
