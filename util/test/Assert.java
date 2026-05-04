package util.test;

import static org.junit.Assert.fail;

public class Assert {
  public static <T extends Throwable> T assertThrows(Class<T> throwable, Runnable function) {
    try {
      function.run();
    } catch (Throwable error) {
      if (error.getClass() == throwable) {
        return throwable.cast(error);
      }

      fail("Expected " + throwable.toString() + " but got " + error.getClass());
    }

    fail("Expected " + throwable.toString() + " but got no exception");
    return null;
  }

  public static void assertNotEquals(Object unexpected, Object actual) {
    assertNotEquals(null, unexpected, actual);
  }

  public static void assertNotEquals(String message, Object unexpected, Object actual) {
    if (unexpected != null || actual != null) {
      if (unexpected == null || unexpected.equals(actual)) {
        message = message == null ? "" : message;
        fail(message + (message.equals("") ? "" : " ") + unexpected + " was not expected but still found");
      }
    }
  }
}
