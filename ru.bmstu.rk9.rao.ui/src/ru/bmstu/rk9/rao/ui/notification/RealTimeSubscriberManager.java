package ru.bmstu.rk9.rao.ui.notification;

import ru.bmstu.rk9.rao.ui.run.RuntimeComponents;

public class RealTimeSubscriberManager extends
		DefferedSubscriberManager<Runnable> {
	@Override
	protected void registerExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.addScheduledAction(runnable);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.removeScheduledAction(runnable);
	}
}
