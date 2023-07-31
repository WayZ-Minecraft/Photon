package com.photon.ui.base;

public interface EventListener {
	
	void onEvent(PhotonEvent event);
	
	public class PhotonEvent {

	    public static final int BUTTON_CLICKED_EVENT = 0;
	    public static final int SLIDER_STATE_CHANGED_EVENT = 1;
	    public static final int CHECK_BOX_CLICKED_EVENT = 2;

	    private Object source;
	    private int type;
	    
	    public PhotonEvent(Object source, int type) {
	        this.source = source;
	        this.type = type;
	    }

	    public Object getSource() { return this.source; }

	    public int getType() { return this.type; }
	}
}