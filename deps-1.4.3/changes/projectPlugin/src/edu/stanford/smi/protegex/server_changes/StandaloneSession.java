package edu.stanford.smi.protegex.server_changes;

import edu.stanford.smi.protege.server.RemoteSession;

public class StandaloneSession implements RemoteSession {
	private static StandaloneSession instance;
	
	public static StandaloneSession getInstance() {
		if (instance == null) {
			instance = new StandaloneSession();
		}
		return instance;
	}
	
	private StandaloneSession() {}
	
	public int getId() {
	    return -1;
	}

	public boolean allowDelegation() {
        return false;
    }

    public void setDelegate(String delegateUserName) {
        throw new IllegalAccessError("delegation not allowed");
    }

    public String getUserName() {
		return null;
	}

	public String getRealUserName() {
        return null;
    }

    public String getUserIpAddress() {
		return null;
	}

    public long getStartTime() {
        return 0;
    }

}
