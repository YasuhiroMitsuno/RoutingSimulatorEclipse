package network.device;

import network.datagram.L2.Frame;

/* Activation Input Queue Class */
class ActivationInputQueue<T extends Frame> extends ActivationQueue<T> {
    protected static final int MAX_SIZE = 5000;
    protected Port delegate;
    public void setDelegate(Port delegate) {
        this.delegate = delegate;
    }

    public void run() {
        while(true) {
            fetch(take());
            //                try {
            //                    Thread.sleep(1);
            //                } catch (InterruptedException e) {
            //                }
        }
    }
    
    public synchronized Boolean put(T t) {
        if (count >= queue.length) {
            System.out.println("Frame Lost");
            return false;
        }
        queue[tail] = t;
        tail = (tail + 1) % queue.length;
        count++;
        notifyAll();
        return true;
    }

    @SuppressWarnings("unchecked")
    public synchronized T take() {
        while (count <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        T t  = (T)queue[head];
        head = (head + 1) % queue.length;
        count--;
        notifyAll();
        return t;
    }
    
    protected void fetch(T t) {
        delegate.fetch((Frame)t);
    }
}

