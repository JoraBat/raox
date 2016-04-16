package ru.bmstu.rk9.rao.ui.process.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.ProcessLogicException;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;

public abstract class BlockNode extends Node {

	private static final long serialVersionUID = 1L;

	public static final String SOURCE_CONNECTION_UPDATED = "SourceConnectionUpdated";
	public static final String TARGET_CONNECTION_UPDATED = "TargetConnectionUpdated";
	public static final String PROCESS_MARKER = "ru.bmstu.rk9.rao.ui.ProcessMarker";
	public static final String PROPERTY_NAME = "ShowNodeName";

	protected CopyOnWriteArrayList<Connection> sourceConnections;
	protected CopyOnWriteArrayList<Connection> targetConnections;
	private final Map<String, Integer> dockNames = new HashMap<>();
	private boolean showName = true;

	public BlockNode() {
		super(ProcessColors.BLOCK_COLOR.getRGB());
		sourceConnections = new CopyOnWriteArrayList<Connection>();
		targetConnections = new CopyOnWriteArrayList<Connection>();
	}

	public final boolean getShowName() {
		return showName;
	}

	public final void setShowName(boolean showName) {
		boolean previousValue = this.showName;
		this.showName = showName;
		getListeners().firePropertyChange(PROPERTY_NAME, previousValue, showName);
	}

	public final boolean addConnection(Connection connection) {
		if (connection.getSourceBlockNode() == this) {
			if (!sourceConnections.contains(connection)) {
				if (sourceConnections.add(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetBlockNode() == this) {
			if (!targetConnections.contains(connection)) {
				if (targetConnections.add(connection)) {
					getListeners().firePropertyChange(TARGET_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final boolean removeConnection(Connection connection) {
		if (connection.getSourceBlockNode() == this) {
			if (sourceConnections.contains(connection)) {
				if (sourceConnections.remove(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetBlockNode() == this) {
			if (targetConnections.contains(connection)) {
				if (targetConnections.remove(connection)) {
					getListeners().firePropertyChange(TARGET_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final List<Connection> getSourceConnections() {
		return sourceConnections;
	}

	public final List<Connection> getTargetConnections() {
		return targetConnections;
	}

	public final int getDocksCount(String dockName) {
		return dockNames.get(dockName);
	}

	public final void registerDock(String dockName) {
		if (dockNames.containsKey(dockName))
			throw new ProcessLogicException("Dock already registered: " + getDockFullName(dockName));

		dockNames.put(dockName, 0);
	}

	public final void captureDock(String dockName) {
		if (!dockNames.containsKey(dockName))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dockName));

		dockNames.put(dockName, dockNames.get(dockName) + 1);
	}

	public final void releaseDock(String dockName) {
		if (!dockNames.containsKey(dockName))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dockName));

		int count = dockNames.get(dockName);
		if (count == 0)
			throw new ProcessLogicException("Dock already released: " + getDockFullName(dockName));

		count--;
		dockNames.put(dockName, count);
	}

	private final String getDockFullName(String dockName) {
		return getName() + "." + dockName;
	}

	public final void validateConnections(IResource file, int sourceConnections, int targetConnections)
			throws CoreException {
		if (getSourceConnections().size() == sourceConnections && getTargetConnections().size() == targetConnections)
			return;

		IMarker marker = file.createMarker(BlockNode.PROCESS_MARKER);
		marker.setAttribute(IMarker.MESSAGE, "Not all docks are connected");
		marker.setAttribute(IMarker.LOCATION, getName());
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(NODE_MARKER, getID());
	}

	public abstract BlockConverterInfo createBlock();

	public abstract void validateProperty(IResource file) throws CoreException;
}
