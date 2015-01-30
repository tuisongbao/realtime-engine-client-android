package com.tuisongbao.android.engine.common;

/**
 *
 * The base of engine exception
 */
public class BaseEngineException extends Exception
{
    private static final long serialVersionUID = 1L;

    public static final int A2DM_HTTP_ERROR = 0;

    private final int mCode;

    public BaseEngineException(int theCode, String theMessage)
    {
      super(theMessage);
      this.mCode = theCode;
    }

    public BaseEngineException(String message, Throwable cause)
    {
      super(message, cause);
      this.mCode = -1;
    }

    public BaseEngineException(Throwable cause)
    {
      super(cause);
      this.mCode = -1;
    }

    public int getCode()
    {
      return this.mCode;
    }
}
