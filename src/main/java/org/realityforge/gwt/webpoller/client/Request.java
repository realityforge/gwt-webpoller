package org.realityforge.gwt.webpoller.client;

/**
 * An abstract representation of a request.
 */
public interface Request
{
  /**
   * Cancel a running request.
   */
  void cancel();
}
