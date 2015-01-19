package me.geso.apimock;

@FunctionalInterface
public interface APIMockCallback {
	public Object run(APIMockContext c) throws Exception;
}
