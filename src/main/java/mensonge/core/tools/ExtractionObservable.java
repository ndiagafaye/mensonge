package mensonge.core.tools;

public class ExtractionObservable extends BetterObservable
{
	public void notifyInProgressAction(String message)
	{
		callWithObservers("onInProgressAction", message);
	}

	public void notifyCompletedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}
	
	public void notifyFailedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}
}