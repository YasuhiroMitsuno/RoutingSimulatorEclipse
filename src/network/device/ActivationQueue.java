package network.device;

abstract class ActivationQueue<T>  extends Thread {
    protected static final int MAX_SIZE = 100;
    protected final Object[] queue;
    protected int tail;
    protected int head;
    protected int count;

    public ActivationQueue() {
        this.queue = new Object[MAX_SIZE];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    public void run() {
        while(true) {
            fetch(take());
            // try {
            //     Thread.sleep(10);
            // } catch (InterruptedException e) {
            // }
        }
    }

    public synchronized Boolean put(T t) {
        /*
        if (count >= queue.length) {
            System.out.println("Can't Forward");
            return false;
        }
        */
        while (count >= queue.length) {
            try {
                wait();
	      } catch (InterruptedException e) {
            }
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
    
    @SuppressWarnings("unchecked")
    public T top() {
        return (T)queue[head];
    }

    public int size() {
        return count;
    }

    protected abstract void fetch(T t);

    public String ratioString() {
        String str = "";
        for(int i=0;i<10;i++) {
                if (i<10*(count+MAX_SIZE/10-1)/MAX_SIZE) {
                str += "■";
            } else {
                str += "□";
            }
        }
        return str;
    }    
}
