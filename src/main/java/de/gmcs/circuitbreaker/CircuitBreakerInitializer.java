package de.gmcs.circuitbreaker;

public class CircuitBreakerInitializer {

  private String root;
/*
  private List<URL> getRootUrls () {
    List<URL> result = new ArrayList<> ();

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    while (cl != null) {
      if (cl instanceof URLClassLoader) {
        URL[] urls = ((URLClassLoader) cl).getURLs();
        result.addAll (Arrays.asList (urls));
      }
      cl = cl.getParent();
    }
    return result;
  }
*/
  public void setRoot(String root) {
    this.root = root;
  }
}
