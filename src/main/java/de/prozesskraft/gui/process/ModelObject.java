package de.prozesskraft.gui.process;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ModelObject
{

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String PropertyName, PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(PropertyName, listener);
	}
	
	public void removePropertyChangeListener(String PropertyName, PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(PropertyName, listener);
	}
	
	public void firePropertyChange(String PropertyName, Object oldValue, Object newValue)
	{
		changeSupport.firePropertyChange(PropertyName, oldValue, newValue);
	}

}
