package cgc.surveillancesystem;

import cgc.utils.Communicator;
import cgc.utils.Entity;
import cgc.utils.Locatable;
import cgc.utils.Maintainable;
import cgc.utils.messages.Message;
import cgc.utils.messages.UpdatedHealth;
import cgc.utils.messages.UpdatedLocation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * the T-Rex Monitor class simulates how the real T-Rex monitor would be. This class
 * Will simulate the health it will nto read the biometrics exactly but will keep track of that as a property.
 * It will also simulate the movement of the T-Rex. It is up to the implementor to decide how the T-rex will wonder
 * around the enclosure. The T-REX will NOT leave the enclosure! this can be a feature added AFTER the fact if there is
 * time. There is a Timer and TimerTask to be used for changing data over time, like the x and y coordinates.
 * The timer and timer task might place a message in the blocking queue to perform an action. the main threads run will loop using the
 * blocking queue this will make the thread wait efficiently without using a busy wait.
 *
 * The TRexMonitor may receive EmergencyMode message from surveillance
 *     1. it will inject Dino, put itself in emergency mode.
 *     2. After injecting dino, the TRex Monitor will sendMessage back to SurveillanceSystem which then will send back
 *        message to cgc and cgc will update the gui appropriately.
 */
public class TRexMonitor extends Thread implements Maintainable, Locatable, Communicator {
    // Maybe add other coordinate space (square space? ... or circle if someone wants to do
    // circle math) to make sure
    // that TRex doesn't go outside.
    private Point2D GPS;
    private SurveillanceSystem surveillanceSystem;
    private boolean isTranquilized;
    private boolean healthStatus;
    private PriorityBlockingQueue<Message> messages;
    private boolean run;

    public TRexMonitor(SurveillanceSystem surveillanceSystem) {
        this.run = true;
        this.surveillanceSystem = surveillanceSystem;
        this.messages = new PriorityBlockingQueue<>();

        startTRexTimer();
        start();
    }

    /**
     * Be in a loop and check messages, it will block
     * and wait for messages. That way, the thread is not in a busy wait.
     *
     */
    @Override
    public void run() {
        // TODO: this will call processMessage accordingly.
        while (run) {
            try {
                Message m = this.messages.take();
                processMessage(m);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Instantiates timer and schedules timer tasks to
     * change x,y coordinates, and anything else that needs to happen over time
     */
    private void startTRexTimer() {

    }

    /**
     *  This will inject the T-Rex with the tranq if there is one available.
     */
    private void inject() {

    }


    /**
     * send message to surveillance system.
     */
    private void reportHealth(boolean healthStatus) {
        //TODO Send a message to the surveillanceSystem with health Status
        UpdatedHealth updatedHealth = new UpdatedHealth(Entity.TREX, 1, healthStatus);
        this.surveillanceSystem.sendMessage(updatedHealth);
    }

    /**
     * send message to surveillanceSystem.
     */
    private void updateLocation(Point2D loc) {
        //TODO send a message to the surveillance with updated location
    }

    /**
     * this will take a message and store it in the blocking queue to be processed later.
     * @param m
     */
    @Override
    public synchronized void sendMessage(Message m) {
        //TODO Store this message in the queue for processing later
        this.messages.put(m);
    }

    private void processMessage(Message message) {
        // TODO: process message using instanceof

    }
}

