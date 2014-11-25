package au.com.addstar.skyblock.misc;

public interface ValueCallback<T>
{
	public void onComplete(boolean success, T value, Throwable exception);
}
