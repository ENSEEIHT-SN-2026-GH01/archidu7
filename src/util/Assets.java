package util;

public class Assets {
  public static final String ASSET_FOLDER = "assets";

  public static String asset(String name) {
    return ASSET_FOLDER + "/" + name;
  }
}
