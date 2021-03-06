package mensonge.core.tools;

/**
 * Permet à un cache d'être observable quand il est mis à jour
 * 
 */
public class CacheObservable extends BetterObservable implements ActionMessageObservable
{
	protected void notifyUpdateCache(long newCacheSize)
	{
		callWithObservers("onUpdateCache", newCacheSize);
	}

	@Override
	public void notifyInProgressAction(String message)
	{
		callWithObservers("onInProgressAction", message);
	}

	@Override
	public void notifyCompletedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}

	@Override
	public void notifyFailedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}
}
