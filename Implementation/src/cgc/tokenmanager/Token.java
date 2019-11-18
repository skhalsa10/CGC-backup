package cgc.tokenmanager;

import cgc.Communicator;
import cgc.Locatable;
import cgc.Maintainable;
import cgc.messages.Message;

import java.awt.*;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class Token extends Thread implements Locatable, Maintainable, Communicator {
    protected int tokenID;
    protected Point GPSLocation;
    protected boolean healthStatus;
    protected PriorityBlockingQueue<Message> messages;
    protected TokenManager tokenManager;
    //TODO should there be a property here to determine if it is ready for pickup?
    //TODO what about one to determine if it is currently in  a car?
    //TODO what about one if it is on the southlot or north lot?


    public Token(int tokenID, TokenManager tokenManager) {
        this.tokenID = tokenID;
        this.tokenManager = tokenManager;
        this.healthStatus = true;
        this.messages = new PriorityBlockingQueue<>();
    }

    protected abstract void startTokenTimer();

    protected abstract void processMessage(Message m);
}